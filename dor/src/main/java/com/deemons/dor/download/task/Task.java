package com.deemons.dor.download.task;

import com.deemons.dor.download.entity.Status;
import com.deemons.dor.download.temporary.TemporaryRecord;

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

import static com.deemons.dor.utils.ResponesUtils.log;

/**
 * author： deemons
 * date:    2017/8/13
 * desc:
 */

public abstract class Task {

    protected TemporaryRecord record;

    protected Task(TemporaryRecord record) {
        this.record = record;
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
                        record.start();
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
                        record.update(status);
                        return status;
                    }
                })
                .doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        log(errorLog());
                        record.error();
                    }
                })
                .doOnComplete(new Action() {
                    @Override
                    public void run() throws Exception {
                        log(completeLog());
                        record.complete();
                    }
                })
                .doOnCancel(new Action() {
                    @Override
                    public void run() throws Exception {
                        log(cancelLog());
                        record.cancel();
                    }
                })
                .doFinally(new Action() {
                    @Override
                    public void run() throws Exception {
                        log(finishLog());
                        record.finish();
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


}
