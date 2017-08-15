package com.deemons.dor.download.task;

import com.deemons.dor.download.temporary.TemporaryBean;

import java.io.IOException;
import java.text.ParseException;

import static com.deemons.dor.download.constant.Constant.MULTITHREADING_DOWNLOAD_CANCEL;
import static com.deemons.dor.download.constant.Constant.MULTITHREADING_DOWNLOAD_COMPLETED;
import static com.deemons.dor.download.constant.Constant.MULTITHREADING_DOWNLOAD_FAILED;
import static com.deemons.dor.download.constant.Constant.MULTITHREADING_DOWNLOAD_FINISH;
import static com.deemons.dor.download.constant.Constant.MULTITHREADING_DOWNLOAD_PREPARE;
import static com.deemons.dor.download.constant.Constant.MULTITHREADING_DOWNLOAD_STARTED;

/**
 * author： deemons
 * date:    2017/8/13
 * desc:
 */

public class MultiThreadDownload extends ContinueDownload {

    public MultiThreadDownload(TemporaryBean record) {
        super(record);
    }

    @Override
    public void prepareDownload() throws IOException, ParseException {
        super.prepareDownload();
        mFileHelper.prepareDownload(
                mBean.lastModifyFile(),
                mBean.tempFile(),
                mBean.file(),
                mBean.contentLength,
                mBean.lastModify);
    }

    @Override
    protected String prepareLog() {
        return MULTITHREADING_DOWNLOAD_PREPARE;
    }

    @Override
    protected String startLog() {
        return MULTITHREADING_DOWNLOAD_STARTED;
    }

    @Override
    protected String completeLog() {
        return MULTITHREADING_DOWNLOAD_COMPLETED;
    }

    @Override
    protected String errorLog() {
        return MULTITHREADING_DOWNLOAD_FAILED;
    }

    @Override
    protected String cancelLog() {
        return MULTITHREADING_DOWNLOAD_CANCEL;
    }

    @Override
    protected String finishLog() {
        return MULTITHREADING_DOWNLOAD_FINISH;
    }
}
