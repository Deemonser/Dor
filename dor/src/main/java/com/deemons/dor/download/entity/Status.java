package com.deemons.dor.download.entity;


import com.deemons.dor.download.constant.Flag;

/**
 * 创建者      chenghaohao
 * 创建时间     2017/8/11 16:40
 * 包名       com.deemons.network.download.entity
 * 描述
 */

public class Status {

    public Flag mFlag;

    public boolean isChunked;
    public long totalSize;
    public long downloadSize;
    public long speed;

    public Status() {
    }

    public Status(long downloadSize, long totalSize) {
        this.downloadSize = downloadSize;
        this.totalSize = totalSize;
    }

    public Status(boolean isChunked, long totalSize, long downloadSize) {
        this.isChunked = isChunked;
        this.totalSize = totalSize;
        this.downloadSize = downloadSize;
    }


    @Override
    public String toString() {
        return "Status{" +
                "mFlag=" + mFlag +
                ", isChunked=" + isChunked +
                ", totalSize=" + totalSize +
                ", downloadSize=" + downloadSize +
                '}';
    }
}
