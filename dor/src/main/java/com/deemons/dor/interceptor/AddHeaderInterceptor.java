package com.deemons.dor.interceptor;

import android.support.annotation.NonNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * author： deemons
 * date:    2017/8/19
 * desc:    添加公用请求头的拦截器
 */

public class AddHeaderInterceptor implements Interceptor {

    private HashMap<String, String> headers;

    public AddHeaderInterceptor(@NonNull HashMap<String, String> headers) {
        this.headers = headers;
    }


    @Override
    public Response intercept(Chain chain) throws IOException {

        Request.Builder builder = chain.request().newBuilder();

        for (Map.Entry<String, String> entry : headers.entrySet()) {
            builder.addHeader(entry.getKey(), entry.getValue());
        }

        return chain.proceed(builder.build());
    }
}
