package com.deemons.dor.upload;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 创建者      chenghaohao
 * 创建时间     2017/9/6 17:21
 * 包名       com.deemons.dor.upload
 * 描述
 */

public abstract  class RetrofitCallback<T> implements Callback<T> {
    @Override
    public void onResponse(Call<T> call, Response<T> response) {

    }

    @Override
    public void onFailure(Call<T> call, Throwable t) {

    }

    //用于进度的回调
    public abstract void onLoading(long total, long progress) ;
}
