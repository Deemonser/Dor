package com.deemons.dor.interceptor;

import android.content.Context;

import com.deemons.dor.error.core.NetworkErrorCode;
import com.deemons.dor.error.core.NetworkException;
import com.deemons.dor.utils.NetUtils;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 创建者      chenghaohao
 * 创建时间     2017/8/7 19:36
 * 包名       com.deemons.network.interceptor
 * 描述
 */

public class ExceptionInterceptor implements Interceptor {

    Context mContext;

    public ExceptionInterceptor(Context context) {
        mContext = context;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {

        if (!NetUtils.isConnected(mContext)) {
            throw new NetworkException(NetworkErrorCode.ERROR_NO_INTERNET, "network is not connect");
        }

        Request request = chain.request();


        Response response = chain.proceed(request);


        if (response.code() != 200) {
            throw new NetworkException(NetworkErrorCode.ERROR_NO_200, "response's code is " + response.code());
        }


        return response;
    }


}
