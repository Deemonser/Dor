package com.deemons.dor.download.task;

import com.deemons.dor.download.entity.Status;
import com.deemons.dor.download.temporary.TemporaryRecord;

import org.reactivestreams.Publisher;

import io.reactivex.Flowable;

import static com.deemons.dor.download.constant.Constant.ALREADY_DOWNLOAD_HINT;

/**
 * author： deemons
 * date:    2017/8/13
 * desc:
 */

public class AlreadyDownloaded extends Task {

    public AlreadyDownloaded(TemporaryRecord record) {
        super(record);
    }

    @Override
    protected Publisher<Status> download() {
        return Flowable.just(new Status(record.getContentLength(), record.getContentLength()));
    }

    @Override
    protected String prepareLog() {
        return ALREADY_DOWNLOAD_HINT;
    }
}