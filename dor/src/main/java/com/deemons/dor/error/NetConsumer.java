package com.deemons.dor.error;

import android.util.Log;

import com.deemons.dor.receiver.NetStateManager;

import io.reactivex.annotations.NonNull;
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
        if (!NetStateManager.getManager().getNetState().isAvailable()) {
            Log.e("NetConsumer", "网络不可用");
        }
        t.printStackTrace();
    }
}
