package com.deemons.dor.error.handler.listener;

import android.content.Context;

/**
 * 创建者      chenghaohao
 * 创建时间     2017/8/7 16:20
 * 包名       com.deemons.network.error.handler.listener
 * 描述
 */

public abstract class NetErrorListener implements ErrorListener {


    public class NetErrorCode {

        /**
         * 网络不可用
         */
        public static final int NOT_AVAILABLE = 0;


        /**
         * 网络连接错误
         */
        public static final int CONNECT_ERROR = 100;


        /**
         * 请求状态 非 200
         */
        public static final int NOT_200 = 200;


        /**
         * 解析错误
         */
        public static final int PARSE_ERROR = 300;
    }


    @Override
    public void handleError(Context context, Exception e) {


    }


    abstract void ErrorCallback(NetErrorCode status, Context context, Exception e);
}
