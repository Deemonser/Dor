package com.deemons.dor.download;

import com.deemons.dor.download.check.ICheckHelper;
import com.deemons.dor.download.entity.DownloadBean;
import com.deemons.dor.download.entity.Status;
import com.deemons.dor.download.load.ILoadHelper;
import com.deemons.dor.download.task.Task;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;

/**
 * 创建者      chenghaohao
 * 创建时间     2017/8/14 10:36
 * 包名       com.deemons.dor
 * 描述
 */

public class Manager {

    ICheckHelper mCheckHelper;

    ILoadHelper mLoadHelper;

    public Observable<Status> download(DownloadBean downloadBean) {
        return Observable.just(downloadBean)
                .flatMap(new Function<DownloadBean, Observable<Task>>() {
                    @Override
                    public Observable<Task> apply(@NonNull DownloadBean downloadBean) throws Exception {
                        return mCheckHelper.dispatchCheck(downloadBean);
                    }
                })
                .flatMap(new Function<Task, Observable<Status>>() {
                    @Override
                    public Observable<Status> apply(@NonNull Task task) throws Exception {
                        return mLoadHelper.dispatchDownload(task);
                    }
                });
    }

}
