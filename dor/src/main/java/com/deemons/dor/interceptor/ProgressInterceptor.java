package com.deemons.dor.interceptor;


import com.deemons.dor.Net;
import com.deemons.dor.download.listener.ProgressListener;
import com.deemons.dor.download.listener.ProgressResponseBody;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * 创建者      chenghaohao
 * 创建时间     2017/8/9 17:45
 * 包名       com.deemons.network.interceptor
 * 描述
 */

public class ProgressInterceptor implements Interceptor {

    private ProgressListener mListener;
    private static volatile ProgressInterceptor progressInterceptor;

    public static ProgressInterceptor getInstant() {
        if (progressInterceptor == null) {
            synchronized (Net.class) {
                if (progressInterceptor == null) {
                    progressInterceptor = new ProgressInterceptor();
                }
            }
        }
        return progressInterceptor;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());

        if (mListener != null) {
            response = response.newBuilder()
                    .body(new ProgressResponseBody(response.body(), mListener))
                    .build();
            mListener = null;
        }
        return response;
    }

    /**
     * 设置 progress 监听
     *
     * @param listener ProgressListener
     */
    public void setListener(ProgressListener listener) {
        mListener = listener;
    }
}
