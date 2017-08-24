package com.deemons.dor.download;

import android.content.Context;
import android.text.TextUtils;

import com.deemons.dor.constant.Api;
import com.deemons.dor.download.check.CheckHelper;
import com.deemons.dor.download.check.ICheckHelper;
import com.deemons.dor.download.db.DataBaseHelper;
import com.deemons.dor.download.entity.DownloadBean;
import com.deemons.dor.download.entity.Status;
import com.deemons.dor.download.file.FileHelper;
import com.deemons.dor.download.load.ILoadHelper;
import com.deemons.dor.download.load.LoadHelper;
import com.deemons.dor.download.temporary.TemporaryBean;

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

public class LoadManager {

    private static final int DEFAULT_MAX_RETRY_COUNT = 3;
    private static final int DEFAULT_MAX_THREADS = 3;

    private final ICheckHelper mCheckHelper;
    private final ILoadHelper mLoadHelper;
    private int maxRetryCount;
    private int maxThreadCount;
    private String savePath;


    public LoadManager(Builder builder) {
        this.maxRetryCount = builder.maxRetryCount;
        this.maxThreadCount = builder.maxThreadCount;
        this.savePath = builder.downloadSavePath;

        DataBaseHelper dataBaseHelper = DataBaseHelper.getSingleton(builder.context);
        FileHelper fileHelper = new FileHelper(getMaxThreads(), builder.context.getApplicationInfo().uid);

        Api downloadApi = builder.retrofit.create(Api.class);
        mCheckHelper = new CheckHelper(fileHelper,
                downloadApi,
                getSavePath(),
                getMaxRetryCount(),
                getMaxThreads()
        );

        mLoadHelper = new LoadHelper(dataBaseHelper, fileHelper, downloadApi);
    }

    public static LoadManager.Builder builder() {
        return new Builder();
    }

    private int getMaxRetryCount() {
        return maxRetryCount < 1 ? DEFAULT_MAX_RETRY_COUNT : maxRetryCount;
    }

    private String getSavePath() {
        return TextUtils.isEmpty(savePath) ?
                getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS).getPath() :
                savePath;
    }

    private int getMaxThreads() {
        return maxThreadCount < 1 ? DEFAULT_MAX_THREADS : maxThreadCount;
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

    public static final class Builder {
        Context context;
        int maxRetryCount;
        int maxThreadCount;
        String downloadSavePath;
        Retrofit retrofit;

        private Builder() {
        }

        public LoadManager.Builder retrofit(@NonNull Retrofit retrofit) {
            this.retrofit = retrofit;
            return this;
        }

        public LoadManager.Builder context(@NonNull Context context) {
            this.context = context;
            return this;
        }

        public LoadManager.Builder maxRetryCount(int maxRetryCount) {
            this.maxRetryCount = maxRetryCount;
            return this;
        }

        public LoadManager.Builder maxThreadCount(int maxThreadCount) {
            this.maxThreadCount = maxThreadCount;
            return this;
        }

        public LoadManager.Builder downloadSavePath(String downloadSavePath) {
            this.downloadSavePath = downloadSavePath;
            return this;
        }

    }


    // ==============================================

//    private Map<String, TemporaryRecord> map = new HashMap<>();
//
//    public void add(String url, TemporaryRecord record) {
//        map.put(url, record);
//    }
//
//    public boolean contain(String url) {
//        return map.get(url) != null;
//    }
//
//    public void delete(String url) {
//        map.remove(url);
//    }
//
//
//    public File[] getFiles(String url) {
//        return map.get(url).getFiles();
//    }
//

}
