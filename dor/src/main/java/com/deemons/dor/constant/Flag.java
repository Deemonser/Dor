package com.deemons.dor.constant;

/**
 * 创建者      chenghaohao
 * 创建时间     2017/8/11 17:17
 * 包名       com.deemons.network.download.core
 * 描述
 */

public interface Flag {
    int NORMAL = 9990;      //未下载
    int WAITING = 9991;     //等待中
    int STARTED = 9992;     //已开始下载
    int PAUSED = 9993;      //已暂停
    int CANCELED = 9994;    //已取消
    int COMPLETED = 9995;   //已完成
    int FAILED = 9996;      //下载失败
    int INSTALL = 9997;     //安装中,暂未使用
    int INSTALLED = 9998;   //已安装,暂未使用
    int DELETED = 9999;     //已删除
}
