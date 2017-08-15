package com.deemons.dor.download.task;

import com.deemons.dor.download.entity.Status;
import com.deemons.dor.download.temporary.TemporaryBean;
import com.deemons.dor.utils.ResponesUtils;

import org.reactivestreams.Publisher;

import java.io.IOException;
import java.text.ParseException;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.functions.Function;
import okhttp3.ResponseBody;
import retrofit2.Response;

import static com.deemons.dor.download.constant.Constant.NORMAL_DOWNLOAD_CANCEL;
import static com.deemons.dor.download.constant.Constant.NORMAL_DOWNLOAD_COMPLETED;
import static com.deemons.dor.download.constant.Constant.NORMAL_DOWNLOAD_FAILED;
import static com.deemons.dor.download.constant.Constant.NORMAL_DOWNLOAD_FINISH;
import static com.deemons.dor.download.constant.Constant.NORMAL_DOWNLOAD_PREPARE;
import static com.deemons.dor.download.constant.Constant.NORMAL_DOWNLOAD_STARTED;
import static com.deemons.dor.download.constant.Constant.NORMAL_RETRY_HINT;

/**
 * author： deemons
 * date:    2017/8/13
 * desc:
 */

public class NormalDownload extends Task {

    public NormalDownload(TemporaryBean record) {
        super(record);
    }

    @Override
    public void prepareDownload() throws IOException, ParseException {
        super.prepareDownload();
        mFileHelper.prepareDownload(mBean.lastModifyFile(), mBean.file(), mBean.contentLength, mBean.lastModify);
    }

    @Override
    protected Publisher<Status> download() {
        return mApi.download(null, mBean.bean.url)
                .flatMap(new Function<Response<ResponseBody>, Publisher<Status>>() {
                    @Override
                    public Publisher<Status> apply(Response<ResponseBody> response) throws Exception {
                        return save(response);
                    }
                })
                .compose(ResponesUtils.<Status>retry2(NORMAL_RETRY_HINT, mBean.maxRetryCount));
    }

    @Override
    protected String prepareLog() {
        return NORMAL_DOWNLOAD_PREPARE;
    }

    @Override
    protected String startLog() {
        return NORMAL_DOWNLOAD_STARTED;
    }

    @Override
    protected String completeLog() {
        return NORMAL_DOWNLOAD_COMPLETED;
    }

    @Override
    protected String errorLog() {
        return NORMAL_DOWNLOAD_FAILED;
    }

    @Override
    protected String cancelLog() {
        return NORMAL_DOWNLOAD_CANCEL;
    }

    @Override
    protected String finishLog() {
        return NORMAL_DOWNLOAD_FINISH;
    }

    private Publisher<Status> save(final Response<ResponseBody> response) {
        return Flowable.create(new FlowableOnSubscribe<Status>() {
            @Override
            public void subscribe(FlowableEmitter<Status> e) throws Exception {
                mFileHelper.saveFile(e, mBean.file(), response);
            }
        }, BackpressureStrategy.LATEST);
    }
}
