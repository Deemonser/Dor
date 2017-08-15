package com.deemons.dor.download.temporary;

import com.deemons.dor.download.entity.DownloadBean;

import java.io.File;

/**
 * 创建者      chenghaohao
 * 创建时间     2017/8/14 17:19
 * 包名       com.deemons.dor.download.temporary
 * 描述
 */

public class TemporaryBean {

    public DownloadBean bean;

    public long contentLength;
    public String lastModify;

    public boolean rangeSupport;
    public boolean serverFileChanged;

    public int maxRetryCount;
    public int maxThreads;


    public String filePath;
    public String tempPath;
    public String lmfPath;


    public TemporaryBean(DownloadBean bean) {
        this.bean = bean;
    }


    public File tempFile() {
        return new File(tempPath);
    }

    public File file() {
        return new File(filePath);
    }


    public File lastModifyFile() {
        return new File(lmfPath);
    }
}
