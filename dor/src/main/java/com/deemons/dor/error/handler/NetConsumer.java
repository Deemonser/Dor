package com.deemons.dor.error.handler;

import android.util.Log;

import com.deemons.dor.error.core.NetworkErrorCode;
import com.deemons.dor.error.core.NetworkException;

import java.util.List;

import io.reactivex.annotations.NonNull;
import io.reactivex.exceptions.CompositeException;
import io.reactivex.functions.Consumer;

/**
 * 创建者      chenghaohao
 * 创建时间     2017/8/7 20:34
 * 包名       com.deemons.network.error.handler
 * 描述
 */

public abstract class NetConsumer implements Consumer<Throwable> {

    @Override
    public void accept(@NonNull Throwable t) throws Exception {
        t.printStackTrace();
        if (t instanceof CompositeException) {
            CompositeException compositeException = (CompositeException) t;

            List<Throwable> list = compositeException.getExceptions();
        }

        if (t instanceof NetworkException) {
            NetworkException networkException = (NetworkException) t;
            if (networkException.getErrorCode() == NetworkErrorCode.ERROR_NO_INTERNET) {
                Log.e("NetWorkException", "网络不可用", t);
            } else if (networkException.getErrorCode() == NetworkErrorCode.ERROR_NO_200) {
                Log.e("NetWorkException", "请求状态非200", t);
            }

        }
    }
}
