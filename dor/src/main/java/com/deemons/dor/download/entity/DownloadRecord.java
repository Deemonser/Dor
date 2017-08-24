package com.deemons.dor.download.entity;


import static com.deemons.dor.constant.Flag.NORMAL;

/**
 * Author: Season(ssseasonnn@gmail.com)
 * Date: 2016/11/14
 * Time: 11:31
 * FIXME
 */
public class DownloadRecord {
    public int id = -1;
    public String url;
    public String saveName;
    public String savePath;
    public Status status;
    public int flag = NORMAL;
    public String extra1;
    public String extra2;
    public String extra3;
    public String extra4;
    public String extra5;
    public long date; //格林威治时间,毫秒
    public String missionId;

    public DownloadRecord() {
    }



}
