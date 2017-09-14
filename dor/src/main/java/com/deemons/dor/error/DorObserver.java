package com.deemons.dor.error;

import android.util.Log;

import com.deemons.dor.receiver.NetStateManager;

import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;

/**
 * 创建者      chenghaohao
 * 创建时间     2017/9/14 16:20
 * 包名       com.deemons.dor.error
 * 描述
 */

public abstract class DorObserver<T> implements Observer<T> {
    @Override
    public void onSubscribe(@NonNull Disposable d) {

    }

    @Override
    public void onError(@NonNull Throwable e) {
        if (!NetStateManager.getManager().getNetState().isAvailable()) {
            Log.e("NetConsumer", "网络不可用");
        }
        e.printStackTrace();
    }

    @Override
    public void onComplete() {

    }
}
