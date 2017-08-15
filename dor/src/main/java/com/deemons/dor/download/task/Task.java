package com.deemons.dor.download.task;

import com.deemons.dor.download.constant.DownloadApi;
import com.deemons.dor.download.db.DataBaseHelper;
import com.deemons.dor.download.entity.Status;
import com.deemons.dor.download.file.FileHelper;
import com.deemons.dor.download.temporary.TemporaryBean;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;

import java.io.IOException;
import java.text.ParseException;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

import static com.deemons.dor.download.constant.Flag.COMPLETED;
import static com.deemons.dor.download.constant.Flag.FAILED;
import static com.deemons.dor.download.constant.Flag.PAUSED;
import static com.deemons.dor.download.constant.Flag.STARTED;
import static com.deemons.dor.utils.ResponesUtils.log;

/**
 * author： deemons
 * date:    2017/8/13
 * desc:
 */

public abstract class Task {

    protected TemporaryBean mBean;
    protected DataBaseHelper dataBaseHelper;
    protected FileHelper mFileHelper;
    protected DownloadApi mApi;

    public Task(TemporaryBean mBean, DataBaseHelper dataBaseHelper, FileHelper mFileHelper, DownloadApi mApi) {
        this.mBean = mBean;
        this.dataBaseHelper = dataBaseHelper;
        this.mFileHelper = mFileHelper;
        this.mApi = mApi;
    }


    public void prepareDownload() throws IOException, ParseException {
        log(prepareLog());
    }

    long downloadSize = 0;

    public Observable<Status> startDownload() {
        return Flowable.just(1)
                .doOnSubscribe(new Consumer<Subscription>() {
                    @Override
                    public void accept(Subscription subscription) throws Exception {
                        log(startLog());
                        start();
                    }
                })
                .flatMap(new Function<Integer, Publisher<Status>>() {
                    @Override
                    public Publisher<Status> apply(Integer integer) throws Exception {
                        return download();
                    }
                })
                .observeOn(Schedulers.io())
                .map(new Function<Status, Status>() {
                    @Override
                    public Status apply(@NonNull Status status) throws Exception {
                        if (status.downloadSize - downloadSize > 100000L) {
                            log("Thread: " + Thread.currentThread().getName() + " update DB: " + status.downloadSize);
                            downloadSize = status.downloadSize;
                        }
                        update(status);
                        return status;
                    }
                })
                .doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        log(errorLog());
                        error();
                    }
                })
                .doOnComplete(new Action() {
                    @Override
                    public void run() throws Exception {
                        log(completeLog());
                        complete();
                    }
                })
                .doOnCancel(new Action() {
                    @Override
                    public void run() throws Exception {
                        log(cancelLog());
                        cancel();
                    }
                })
                .doFinally(new Action() {
                    @Override
                    public void run() throws Exception {
                        log(finishLog());
                        finish();
                    }
                })
                .toObservable();
    }

    protected abstract Publisher<Status> download();

    protected String prepareLog() {
        return "";
    }

    protected String startLog() {
        return "";
    }

    protected String completeLog() {
        return "";
    }

    protected String errorLog() {
        return "";
    }

    protected String cancelLog() {
        return "";
    }

    protected String finishLog() {
        return "";
    }


    public void start() {
        if (dataBaseHelper.recordNotExists(mBean.bean.url)) {
            dataBaseHelper.insertRecord(mBean.bean, STARTED);
        } else {
            dataBaseHelper.updateRecord(mBean.bean.url, mBean.bean.saveName, mBean.bean.savePath, STARTED);
        }
    }

    public void update(Status status) {
        dataBaseHelper.updateStatus(mBean.bean.url, status);
    }

    public void error() {
        dataBaseHelper.updateRecord(mBean.bean.url, FAILED);
    }

    public void complete() {
        dataBaseHelper.updateRecord(mBean.bean.url, COMPLETED);
    }

    public void cancel() {
        dataBaseHelper.updateRecord(mBean.bean.url, PAUSED);
    }

    public void finish() {
        dataBaseHelper.closeDataBase();
    }

}
