package com.deemons.dor.error.core;

/**
 * 创建者      chenghaohao
 * 创建时间     2017/8/7 17:20
 * 包名       com.deemons.network.error.core
 * 描述
 */

public class NetworkException extends RuntimeException {
    private int errorCode;

    public NetworkException(int code, String msg) {
        super(msg);
        this.errorCode = code;
    }

    public int getErrorCode() {
        return errorCode;
    }

}
