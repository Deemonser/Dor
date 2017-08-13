package com.deemons.dor.download.listener;

/**
 * 创建者      chenghaohao
 * 创建时间     2017/8/9 17:51
 * 包名       com.deemons.network.download
 * 描述
 */

public interface ProgressListener {

    void progress(long total, long bytesLoaded);

}
