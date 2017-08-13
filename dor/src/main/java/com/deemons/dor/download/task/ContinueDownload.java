package com.deemons.dor.download.task;

import com.deemons.dor.download.entity.Status;
import com.deemons.dor.download.temporary.TemporaryRecord;
import com.deemons.dor.utils.ResponesUtils;

import org.reactivestreams.Publisher;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Response;

import static com.deemons.dor.download.constant.Constant.CONTINUE_DOWNLOAD_CANCEL;
import static com.deemons.dor.download.constant.Constant.CONTINUE_DOWNLOAD_COMPLETED;
import static com.deemons.dor.download.constant.Constant.CONTINUE_DOWNLOAD_FAILED;
import static com.deemons.dor.download.constant.Constant.CONTINUE_DOWNLOAD_FINISH;
import static com.deemons.dor.download.constant.Constant.CONTINUE_DOWNLOAD_PREPARE;
import static com.deemons.dor.download.constant.Constant.CONTINUE_DOWNLOAD_STARTED;
import static com.deemons.dor.download.constant.Constant.RANGE_RETRY_HINT;
import static com.deemons.dor.utils.ResponesUtils.formatStr;

/**
 * author： deemons
 * date:    2017/8/13
 * desc:
 */

public class ContinueDownload extends Task {

    public ContinueDownload(TemporaryRecord record) {
        super(record);
    }

    @Override
    protected Publisher<Status> download() {
        List<Publisher<Status>> tasks = new ArrayList<>();
        for (int i = 0; i < record.getMaxThreads(); i++) {
            tasks.add(rangeDownload(i));
        }
        return Flowable.mergeDelayError(tasks);
    }

    @Override
    protected String prepareLog() {
        return CONTINUE_DOWNLOAD_PREPARE;
    }

    @Override
    protected String startLog() {
        return CONTINUE_DOWNLOAD_STARTED;
    }

    @Override
    protected String completeLog() {
        return CONTINUE_DOWNLOAD_COMPLETED;
    }

    @Override
    protected String errorLog() {
        return CONTINUE_DOWNLOAD_FAILED;
    }

    @Override
    protected String cancelLog() {
        return CONTINUE_DOWNLOAD_CANCEL;
    }

    @Override
    protected String finishLog() {
        return CONTINUE_DOWNLOAD_FINISH;
    }

    /**
     * 分段下载任务
     *
     * @param index 下载编号
     * @return Observable
     */
    private Publisher<Status> rangeDownload(final int index) {
        return record.rangeDownload(index)
                .subscribeOn(Schedulers.io())  //Important!
                .flatMap(new Function<Response<ResponseBody>, Publisher<Status>>() {
                    @Override
                    public Publisher<Status> apply(Response<ResponseBody> response) throws Exception {
                        return save(index, response.body());
                    }
                })
                .compose(ResponesUtils.<Status>retry2(formatStr(RANGE_RETRY_HINT, index), record.getMaxRetryCount()));
    }

    /**
     * 保存断点下载的文件,以及下载进度
     *
     * @param index    下载编号
     * @param response 响应值
     * @return Flowable
     */
    private Publisher<Status> save(final int index, final ResponseBody response) {

        Flowable<Status> flowable = Flowable.create(new FlowableOnSubscribe<Status>() {
            @Override
            public void subscribe(FlowableEmitter<Status> emitter) throws Exception {
                record.save(emitter, index, response);
            }
        }, BackpressureStrategy.LATEST)
                .replay(1)
                .autoConnect();
        return flowable.throttleFirst(100, TimeUnit.MILLISECONDS).mergeWith(flowable.takeLast(1))
                .subscribeOn(Schedulers.newThread());
    }
}