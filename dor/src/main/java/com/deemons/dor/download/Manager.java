package com.deemons.dor.download;

import android.content.Context;

import com.deemons.dor.download.check.CheckHelper;
import com.deemons.dor.download.check.ICheckHelper;
import com.deemons.dor.download.constant.DownloadApi;
import com.deemons.dor.download.db.DataBaseHelper;
import com.deemons.dor.download.entity.DownloadBean;
import com.deemons.dor.download.entity.Status;
import com.deemons.dor.download.file.FileHelper;
import com.deemons.dor.download.load.ILoadHelper;
import com.deemons.dor.download.load.LoadHelper;
import com.deemons.dor.download.temporary.TemporaryBean;
import com.deemons.dor.download.temporary.TemporaryRecord;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import retrofit2.Retrofit;

import static android.os.Environment.DIRECTORY_DOWNLOADS;
import static android.os.Environment.getExternalStoragePublicDirectory;

/**
 * 创建者      chenghaohao
 * 创建时间     2017/8/14 10:36
 * 包名       com.deemons.dor
 * 描述
 */

public class Manager {

    private static final int DEFAULT_MAX_RETRY_CONT = 3;
    private static final int DEFAULT_MAX_THREADS = 4;

    private final ICheckHelper mCheckHelper;
    private final  ILoadHelper mLoadHelper;

    public Manager(Context context, Retrofit retrofit) {
        DataBaseHelper dataBaseHelper = DataBaseHelper.getSingleton(context);
        FileHelper fileHelper = new FileHelper(getMaxThreads());

        DownloadApi downloadApi = retrofit.create(DownloadApi.class);
        mCheckHelper = new CheckHelper(fileHelper,
                downloadApi,
                getSavePath(),
                getMaxRetryCount(),
                getMaxThreads()
        );

        mLoadHelper = new LoadHelper(dataBaseHelper, fileHelper, downloadApi);


    }

    private int getMaxRetryCount() {
        return DEFAULT_MAX_RETRY_CONT;
    }

    private String getSavePath() {
        return getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS).getPath();
    }

    private int getMaxThreads() {
        return DEFAULT_MAX_THREADS;
    }

    public Observable<Status> download(DownloadBean downloadBean) {
        return Observable.just(downloadBean)
                .flatMap(new Function<DownloadBean, Observable<TemporaryBean>>() {
                    @Override
                    public Observable<TemporaryBean> apply(@NonNull DownloadBean downloadBean) throws Exception {
                        return mCheckHelper.dispatchCheck(downloadBean);
                    }
                })
                .flatMap(new Function<TemporaryBean, Observable<Status>>() {
                    @Override
                    public Observable<Status> apply(@NonNull TemporaryBean bean) throws Exception {
                        return mLoadHelper.dispatchDownload(bean);
                    }
                });
    }


    // ==============================================

    private Map<String, TemporaryRecord> map = new HashMap<>();

    public void add(String url, TemporaryRecord record) {
        map.put(url, record);
    }

    public boolean contain(String url) {
        return map.get(url) != null;
    }

    public void delete(String url) {
        map.remove(url);
    }




    public File[] getFiles(String url) {
        return map.get(url).getFiles();
    }


}
