package com.deemons.dor.error.core;

/**
 * 创建者      chenghaohao
 * 创建时间     2017/8/7 17:20
 * 包名       com.deemons.network.error.core
 * 描述
 */

public interface NetworkErrorCode {


    /**
     * 网络不可用
     */
    int ERROR_NO_INTERNET = 0X10;



    /**
     * 请求状态 非 200
     */
    int ERROR_NO_200 = 0X200;


    /**
     * 解析错误
     */
    int ERROR_NO_PARSE = 0X300;


    /**
     * 自定义错误
     */
    int ERROR_OTHER = 0X1000;

}

