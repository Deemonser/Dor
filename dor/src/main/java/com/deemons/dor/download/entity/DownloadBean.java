package com.deemons.dor.download.entity;

/**
 * 创建者      chenghaohao
 * 创建时间     2017/8/11 16:30
 * 包名       com.deemons.network.download.entity
 * 描述
 */

public class DownloadBean {

    public String url;
    public String saveName;
    public String savePath;
    public String extra1;
    public String extra2;
    public String extra3;
    public String extra4;
    public String extra5;

    public DownloadBean(String url, String saveName, String savePath) {
        this.url = url;
        this.saveName = saveName;
        this.savePath = savePath;
    }
}
