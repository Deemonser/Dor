package com.deemons.dor.utils;

import io.reactivex.ObservableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * 创建者      chenghaohao
 * 创建时间     2017/9/14 13:57
 * 包名       com.deemons.dor.utils
 * 描述
 */

public class RxUtils {


    public static <T> ObservableTransformer<T, T> io_main() {    //compose简化线程
        return upstream -> upstream.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

}
