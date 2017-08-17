package com.deemons.dor.download.load;

import com.deemons.dor.download.constant.DownloadApi;
import com.deemons.dor.download.db.DataBaseHelper;
import com.deemons.dor.download.entity.DownloadRange;
import com.deemons.dor.download.entity.Status;
import com.deemons.dor.download.file.FileHelper;
import com.deemons.dor.download.temporary.TemporaryBean;
import com.deemons.dor.utils.ResponesUtils;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Response;

import static com.deemons.dor.download.constant.Constant.ALREADY_DOWNLOAD_HINT;
import static com.deemons.dor.download.constant.Constant.CONTINUE_DOWNLOAD_PREPARE;
import static com.deemons.dor.download.constant.Constant.MULTITHREADING_DOWNLOAD_PREPARE;
import static com.deemons.dor.download.constant.Constant.NORMAL_DOWNLOAD_PREPARE;
import static com.deemons.dor.download.constant.Constant.NORMAL_RETRY_HINT;
import static com.deemons.dor.download.constant.Constant.RANGE_DOWNLOAD_STARTED;
import static com.deemons.dor.download.constant.Constant.RANGE_RETRY_HINT;
import static com.deemons.dor.download.constant.Flag.COMPLETED;
import static com.deemons.dor.download.constant.Flag.FAILED;
import static com.deemons.dor.download.constant.Flag.PAUSED;
import static com.deemons.dor.download.constant.Flag.STARTED;
import static com.deemons.dor.utils.ResponesUtils.formatStr;
import static com.deemons.dor.utils.ResponesUtils.log;

/**
 * author： deemons
 * date:    2017/8/15
 * desc:
 */

public class LoadHelper implements ILoadHelper {


    private DataBaseHelper dataBaseHelper;
    private FileHelper mFileHelper;
    private DownloadApi mApi;
    private boolean showSpeed;
    long downloadSize = 0;


    public LoadHelper(DataBaseHelper dataBaseHelper, FileHelper fileHelper, DownloadApi api) {
        this.dataBaseHelper = dataBaseHelper;
        mFileHelper = fileHelper;
        mApi = api;
    }

    @Override
    public Observable<Status> dispatchDownload(TemporaryBean temporaryBean) throws IOException, ParseException {

        prepareDownload(temporaryBean);
        return startDownload(temporaryBean);
    }


    private void prepareDownload(TemporaryBean temporaryBean) throws IOException, ParseException {
        switch (temporaryBean.getDownLoadType()) {
            case DownloadType.NORMAL:
                log(NORMAL_DOWNLOAD_PREPARE);
                mFileHelper.prepareDownload(
                        temporaryBean.lastModifyFile(),
                        temporaryBean.file(),
                        temporaryBean.contentLength,
                        temporaryBean.lastModify);
                break;
            case DownloadType.CONTINUE:
                log(CONTINUE_DOWNLOAD_PREPARE);
                break;
            case DownloadType.MULTI_THREAD:
                log(MULTITHREADING_DOWNLOAD_PREPARE);
                mFileHelper.prepareDownload(
                        temporaryBean.lastModifyFile(),
                        temporaryBean.tempFile(),
                        temporaryBean.file(),
                        temporaryBean.contentLength,
                        temporaryBean.lastModify);
                break;
            case DownloadType.ALREADY:
                log(ALREADY_DOWNLOAD_HINT);
                break;
            default:
                break;
        }
    }


    public Observable<Status> startDownload(final TemporaryBean temporaryBean) {
        return Flowable.just(temporaryBean)
                .doOnSubscribe(new Consumer<Subscription>() {
                    @Override
                    public void accept(Subscription subscription) throws Exception {
                        log(startLog());
                        start(temporaryBean);
                    }
                })
                .flatMap(new Function<TemporaryBean, Publisher<Status>>() {
                    @Override
                    public Publisher<Status> apply(TemporaryBean bean) throws Exception {
                        return download(bean);
                    }
                })
                .throttleFirst(1000, TimeUnit.MILLISECONDS)
                .map(new Function<Status, Status>() {
                    @Override
                    public Status apply(@NonNull Status status) throws Exception {
                        return showSpeed ? mFileHelper.getDownloadSpeed(status) : status;
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
                        update(status, temporaryBean);
                        return status;
                    }
                })
                .doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        log(errorLog());
                        error(temporaryBean);
                    }
                })
                .doOnComplete(new Action() {
                    @Override
                    public void run() throws Exception {
                        log(completeLog());
                        complete(temporaryBean);
                    }
                })
                .doOnCancel(new Action() {
                    @Override
                    public void run() throws Exception {
                        log(cancelLog());
                        cancel(temporaryBean);
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


    protected Publisher<Status> download(TemporaryBean bean) {

        Publisher<Status> publisher;
        switch (bean.getDownLoadType()) {
            case DownloadType.NORMAL:
                log(NORMAL_DOWNLOAD_PREPARE);
                publisher = getNormalDownload(bean);
                break;
            case DownloadType.CONTINUE:
                log(CONTINUE_DOWNLOAD_PREPARE);
                publisher = getContinueDownload(bean);
                break;
            case DownloadType.MULTI_THREAD:
                log(MULTITHREADING_DOWNLOAD_PREPARE);
                publisher = getContinueDownload(bean);
                break;
            case DownloadType.ALREADY:
                log(ALREADY_DOWNLOAD_HINT);
                publisher = getAlreadyDownload(bean);
                break;
            default:
                throw new NullPointerException("DownloadType is null");
        }

        return publisher;
    }

    private Flowable<Status> getAlreadyDownload(TemporaryBean bean) {
        return Flowable.just(new Status(bean.contentLength, bean.contentLength));
    }

    private Flowable<Status> getNormalDownload(final TemporaryBean bean) {
        return mApi.download(null, bean.bean.url)
                .flatMap(new Function<Response<ResponseBody>, Publisher<Status>>() {
                    @Override
                    public Publisher<Status> apply(Response<ResponseBody> response) throws Exception {
                        return save(response, bean);
                    }
                })
                .compose(ResponesUtils.<Status>retry2(NORMAL_RETRY_HINT, bean.maxRetryCount));
    }

    private Publisher<Status> getContinueDownload(TemporaryBean bean) {
        List<Publisher<Status>> tasks = new ArrayList<>();
        for (int i = 0; i < bean.maxThreads; i++) {
            tasks.add(rangeDownload(i, bean));
        }
        return Flowable.mergeDelayError(tasks);
    }

    private Publisher<Status> save(final Response<ResponseBody> response, final TemporaryBean bean) {
        return Flowable.create(new FlowableOnSubscribe<Status>() {
            @Override
            public void subscribe(FlowableEmitter<Status> e) throws Exception {
                mFileHelper.saveFile(e, bean.file(), response);
            }
        }, BackpressureStrategy.LATEST);
    }


    /**
     * 分段下载任务
     *
     * @param index 下载编号
     * @param bean
     * @return Observable
     */
    private Publisher<Status> rangeDownload(final int index, final TemporaryBean bean) {
        return Flowable
                .create(new FlowableOnSubscribe<DownloadRange>() {
                    @Override
                    public void subscribe(FlowableEmitter<DownloadRange> e) throws Exception {
                        DownloadRange range = mFileHelper.readDownloadRange(bean.tempFile(), index);
                        if (range.legal()) {
                            e.onNext(range);
                        }
                        e.onComplete();
                    }
                }, BackpressureStrategy.ERROR)
                .flatMap(new Function<DownloadRange, Publisher<Response<ResponseBody>>>() {
                    @Override
                    public Publisher<Response<ResponseBody>> apply(DownloadRange range)
                            throws Exception {
                        log("Thread: " + Thread.currentThread().getName() + "; " + RANGE_DOWNLOAD_STARTED, index, range.start, range.end);
                        String rangeStr = "bytes=" + range.start + "-" + range.end;
                        return mApi.download(rangeStr, bean.bean.url);
                    }
                })
                .subscribeOn(Schedulers.io())  //Important!
                .flatMap(new Function<Response<ResponseBody>, Publisher<Status>>() {
                    @Override
                    public Publisher<Status> apply(Response<ResponseBody> response) throws Exception {
                        return save(index, response.body(), bean);
                    }
                })
                .compose(ResponesUtils.<Status>retry2(formatStr(RANGE_RETRY_HINT, index), bean.maxRetryCount));
    }


    /**
     * 保存断点下载的文件,以及下载进度
     *
     * @param index    下载编号
     * @param response 响应值
     * @param bean
     * @return Flowable
     */
    private Publisher<Status> save(final int index, final ResponseBody response, final TemporaryBean bean) {

        Flowable<Status> flowable = Flowable.create(new FlowableOnSubscribe<Status>() {
            @Override
            public void subscribe(FlowableEmitter<Status> emitter) throws Exception {
                mFileHelper.saveFile(emitter, index, bean.tempFile(), bean.file(), response);
            }
        }, BackpressureStrategy.LATEST)
                .replay(1)
                .autoConnect();
        return flowable.throttleFirst(100, TimeUnit.MILLISECONDS)
                .mergeWith(flowable.takeLast(1))
                .subscribeOn(Schedulers.newThread());
    }


    public void start(TemporaryBean temporaryBean) {
        if (dataBaseHelper.recordNotExists(temporaryBean.bean.url)) {
            dataBaseHelper.insertRecord(temporaryBean.bean, STARTED);
        } else {
            dataBaseHelper.updateRecord(temporaryBean.bean.url, temporaryBean.bean.saveName, temporaryBean.bean.savePath, STARTED);
        }
    }

    public void update(Status status, TemporaryBean temporaryBean) {
        dataBaseHelper.updateStatus(temporaryBean.bean.url, status);
    }

    public void error(TemporaryBean temporaryBean) {
        dataBaseHelper.updateRecord(temporaryBean.bean.url, FAILED);
    }

    public void complete(TemporaryBean temporaryBean) {
        dataBaseHelper.updateRecord(temporaryBean.bean.url, COMPLETED);
    }

    public void cancel(TemporaryBean temporaryBean) {
        dataBaseHelper.updateRecord(temporaryBean.bean.url, PAUSED);
    }

    public void finish() {
        dataBaseHelper.closeDataBase();
    }


    protected String startLog() {
        return "startLog";
    }

    protected String completeLog() {
        return "completeLog";
    }

    protected String errorLog() {
        return "errorLog";
    }

    protected String cancelLog() {
        return "cancelLog";
    }

    protected String finishLog() {
        return "finishLog";
    }


}
