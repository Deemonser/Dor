package com.deemons.dor.download.entity;


import static com.deemons.dor.download.constant.Flag.NORMAL;

/**
 * 创建者      chenghaohao
 * 创建时间     2017/8/11 16:40
 * 包名       com.deemons.network.download.entity
 * 描述
 */

public class Status {

    public int mFlag = NORMAL;

    public boolean isChunked;
    public long totalSize;
    public long downloadSize;
    public long startTimeStamp;
    public String speed;

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
                ", progress=" + downloadSize * 100f / totalSize +
                ", speed='" + speed + '\'' +
                '}';
    }
}
