package com.deemons.dor.download.check;

import android.text.TextUtils;

import com.deemons.dor.download.constant.DownloadApi;
import com.deemons.dor.download.entity.DownloadBean;
import com.deemons.dor.download.file.FileHelper;
import com.deemons.dor.download.task.AlreadyDownloaded;
import com.deemons.dor.download.task.ContinueDownload;
import com.deemons.dor.download.task.MultiThreadDownload;
import com.deemons.dor.download.task.NormalDownload;
import com.deemons.dor.download.task.Task;
import com.deemons.dor.download.temporary.TemporaryBean;

import java.io.File;
import java.io.IOException;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import retrofit2.Response;

import static android.os.Environment.DIRECTORY_DOWNLOADS;
import static android.os.Environment.getExternalStoragePublicDirectory;
import static android.text.TextUtils.concat;
import static com.deemons.dor.download.constant.Constant.CACHE;
import static com.deemons.dor.download.constant.Constant.DOWNLOAD_RECORD_FILE_DAMAGED;
import static com.deemons.dor.download.constant.Constant.REQUEST_RETRY_HINT;
import static com.deemons.dor.download.constant.Constant.TEST_RANGE_SUPPORT;
import static com.deemons.dor.download.constant.Constant.URL_ILLEGAL;
import static com.deemons.dor.utils.ResponesUtils.contentLength;
import static com.deemons.dor.utils.ResponesUtils.empty;
import static com.deemons.dor.utils.ResponesUtils.fileName;
import static com.deemons.dor.utils.ResponesUtils.formatStr;
import static com.deemons.dor.utils.ResponesUtils.getPaths;
import static com.deemons.dor.utils.ResponesUtils.lastModify;
import static com.deemons.dor.utils.ResponesUtils.log;
import static com.deemons.dor.utils.ResponesUtils.mkdirs;
import static com.deemons.dor.utils.ResponesUtils.notSupportRange;
import static com.deemons.dor.utils.ResponesUtils.retry;
import static java.io.File.separator;

/**
 * 创建者      chenghaohao
 * 创建时间     2017/8/14 10:47
 * 包名       com.deemons.dor.download.check
 * 描述
 */

public class CheckHelper implements ICheckHelper {

    private static final int defaultMaxRetryCont = 3;
    private static final int defaultMaxThreads = 7;

    private FileHelper mFileHelper;
    private DownloadApi mApi;

    private int maxRetryCount;
    private int maxThreads;

    private String savePath;

    public CheckHelper(DownloadApi api) {
        this(api, null);
    }

    public CheckHelper(DownloadApi api, String savePath) {
        this(api, savePath, defaultMaxRetryCont);
    }

    public CheckHelper(DownloadApi api, String savePath, int maxRetryCount) {
        this(api, savePath, maxRetryCount, defaultMaxThreads);
    }

    public CheckHelper(DownloadApi api, String savePath, int maxRetryCount, int maxThreads) {
        mApi = api;
        this.maxRetryCount = maxRetryCount;
        this.maxThreads = maxThreads;

        if (TextUtils.isEmpty(savePath)) {
            this.savePath = getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS).getPath();
        } else {
            this.savePath = savePath;
        }

        mFileHelper = new FileHelper(maxThreads);
    }

    @Override
    public Observable<Task> dispatchCheck(DownloadBean downloadBean) {
        return Observable.just(downloadBean)
                .map(new Function<DownloadBean, TemporaryBean>() {
                    @Override
                    public TemporaryBean apply(@NonNull DownloadBean downloadBean) throws Exception {
                        return getTemporary(downloadBean);
                    }
                })
                .flatMap(new Function<TemporaryBean, ObservableSource<Task>>() {
                    @Override
                    public ObservableSource<Task> apply(TemporaryBean bean) throws Exception {
                        return getDownloadType(bean);
                    }
                });


    }


    /**
     * get download type.
     *
     * @param bean
     * @return download type
     */
    private Observable<Task> getDownloadType(final TemporaryBean bean) {
        return Observable.just(bean)
                .flatMap(new Function<TemporaryBean, ObservableSource<TemporaryBean>>() {
                    @Override
                    public ObservableSource<TemporaryBean> apply(TemporaryBean temporaryBean)
                            throws Exception {
                        return checkUrl(temporaryBean);
                    }
                })
                .flatMap(new Function<TemporaryBean, ObservableSource<TemporaryBean>>() {
                    @Override
                    public ObservableSource<TemporaryBean> apply(TemporaryBean temporaryBean) throws Exception {
                        return checkRange(temporaryBean);
                    }
                })
                .flatMap(new Function<TemporaryBean, ObservableSource<Task>>() {
                    @Override
                    public ObservableSource<Task> apply(TemporaryBean temporaryBean) throws Exception {
                        return temporaryBean.file().exists() ? existsType(temporaryBean) : nonExistsType(temporaryBean);
                    }
                });
    }


    /**
     * Gets the download type of file non-existence.
     *
     * @param temporaryBean
     * @return Download Type
     */
    private Observable<Task> nonExistsType(TemporaryBean temporaryBean) {
        return Observable.just(temporaryBean)
                .flatMap(new Function<TemporaryBean, ObservableSource<Task>>() {
                    @Override
                    public ObservableSource<Task> apply(TemporaryBean bean)
                            throws Exception {
                        return Observable.just(generateNonExistsType(bean));
                    }
                });
    }

    /**
     * Gets the download type of file existence.
     *
     * @param temporaryBean
     * @return Download Type
     */
    private Observable<Task> existsType(TemporaryBean temporaryBean) {
        return Observable.just(temporaryBean)
                .map(new Function<TemporaryBean, TemporaryBean>() {
                    @Override
                    public TemporaryBean apply(TemporaryBean bean) throws Exception {
                        bean.lastModify = mFileHelper.readLastModify(bean.lastModifyFile());
                        return bean;
                    }
                })
                .flatMap(new Function<TemporaryBean, ObservableSource<TemporaryBean>>() {
                    @Override
                    public ObservableSource<TemporaryBean> apply(TemporaryBean bean) throws Exception {
                        return checkFileUpdate(bean);
                    }
                })
                .flatMap(new Function<TemporaryBean, ObservableSource<Task>>() {
                    @Override
                    public ObservableSource<Task> apply(TemporaryBean bean)
                            throws Exception {
                        return Observable.just(generateFileExistsType(bean));
                    }
                });
    }


    private boolean checkFileExist(TemporaryBean bean) {
        return bean.file().exists();
    }

    public Observable<TemporaryBean> checkFileWhole(TemporaryBean bean) {
        return null;
    }

    public Observable<TemporaryBean> checkFileUpdate(final TemporaryBean bean) {
        return mApi.checkFileByHead(bean.lastModify, bean.bean.url)
                .doOnNext(new Consumer<Response<Void>>() {
                    @Override
                    public void accept(Response<Void> response) throws Exception {
                        saveFileState(bean, response);
                    }
                })
                .map(new Function<Response<Void>, TemporaryBean>() {
                    @Override
                    public TemporaryBean apply(Response<Void> response) throws Exception {
                        return bean;
                    }
                });
    }





    /**
     * check url
     *
     * @param bean TemporaryBean
     * @return empty
     */
    private Observable<TemporaryBean> checkUrl(final TemporaryBean bean) {
        return mApi.check(bean.bean.url)
                .flatMap(new Function<Response<Void>, ObservableSource<Object>>() {
                    @Override
                    public ObservableSource<Object> apply(@NonNull Response<Void> resp)
                            throws Exception {
                        if (!resp.isSuccessful()) {
                            return checkUrlByGet(bean);
                        } else {
                            return saveFileInfo(bean.bean.url, resp);
                        }
                    }
                })
                .compose(retry(REQUEST_RETRY_HINT, bean.maxRetryCount))
                .map(new Function<Object, TemporaryBean>() {
                    @Override
                    public TemporaryBean apply(@NonNull Object o) throws Exception {
                        return bean;
                    }
                });
    }

    private ObservableSource<Object> saveFileInfo(final String url, final Response<Void> resp) {
        return Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(ObservableEmitter<Object> emitter) throws Exception {
                saveFileInfo(url, resp);
                emitter.onNext(new Object());
                emitter.onComplete();
            }
        });
    }

    private ObservableSource<Object> checkUrlByGet(final TemporaryBean bean) {
        return mApi.checkByGet(bean.bean.url)
                .doOnNext(new Consumer<Response<Void>>() {
                    @Override
                    public void accept(Response<Void> response) throws Exception {
                        if (!response.isSuccessful()) {
                            throw new IllegalArgumentException(formatStr(URL_ILLEGAL, bean.bean.url));
                        } else {
                            saveFileInfo(bean.bean.url, response);
                        }
                    }
                })
                .map(new Function<Response<Void>, Object>() {
                    @Override
                    public Object apply(Response<Void> response) throws Exception {
                        return new Object();
                    }
                })
                .compose(retry(REQUEST_RETRY_HINT, bean.maxRetryCount));
    }


    public Observable<TemporaryBean> checkRange(final TemporaryBean bean) {
        return mApi.checkRangeByHead(TEST_RANGE_SUPPORT, bean.bean.url)
                .doOnNext(new Consumer<Response<Void>>() {
                    @Override
                    public void accept(@NonNull Response<Void> voidResponse) throws Exception {
                        saveRangeInfo(bean, voidResponse);
                        saveFileInfo(bean, voidResponse);
                    }
                })
                .compose(retry(REQUEST_RETRY_HINT, bean.maxRetryCount))
                .map(new Function<Object, TemporaryBean>() {
                    @Override
                    public TemporaryBean apply(@NonNull Object o) throws Exception {
                        return bean;
                    }
                });
    }


    private TemporaryBean getTemporary(DownloadBean downloadBean) {
        TemporaryBean temporaryBean = new TemporaryBean(downloadBean);
        temporaryBean.maxThreads = maxThreads;
        temporaryBean.maxRetryCount = maxRetryCount;

        String realSavePath;
        if (empty(downloadBean.savePath)) {
            realSavePath = savePath;
            downloadBean.savePath = savePath;
        } else {
            realSavePath = downloadBean.savePath;
        }
        String cachePath = concat(realSavePath, separator, CACHE).toString();
        mkdirs(realSavePath, cachePath);

        String[] paths = getPaths(downloadBean.saveName, realSavePath);
        temporaryBean.filePath = paths[0];
        temporaryBean.tempPath = paths[1];
        temporaryBean.lmfPath = paths[2];
        return temporaryBean;
    }


    /**
     * Save file info
     *
     * @param temporaryBean TemporaryBean
     * @param response      response
     */
    public void saveFileInfo(TemporaryBean temporaryBean, Response<?> response) {
        if (empty(temporaryBean.bean.saveName)) {
            temporaryBean.bean.saveName = fileName(temporaryBean.bean.url, response);
        }
        temporaryBean.contentLength = contentLength(response);
        temporaryBean.lastModify = lastModify(response);
    }

    /**
     * Save range info
     *
     * @param temporaryBean TemporaryBean
     * @param response      response
     */
    public void saveRangeInfo(TemporaryBean temporaryBean, Response<?> response) {
        temporaryBean.rangeSupport = !notSupportRange(response);
    }


    /**
     * Save file state, change or not change.
     *
     * @param temporaryBean TemporaryBean
     * @param response      response
     */
    public void saveFileState(TemporaryBean temporaryBean, Response<Void> response) {
        if (response.code() == 304) {
            temporaryBean.serverFileChanged = false;
        } else if (response.code() == 200) {
            temporaryBean.serverFileChanged = true;
        }
    }


    /**
     * return file not exists download type.
     *
     * @param bean TemporaryBean
     * @return download type
     */
    public Task generateNonExistsType(TemporaryBean bean) {
        return getNormalType(bean);
    }

    /**
     * return file exists download type
     *
     * @param bean TemporaryBean
     * @return download type
     */
    public Task generateFileExistsType(TemporaryBean bean) {
        Task type;
        if (bean.serverFileChanged) {
            type = getNormalType(bean);
        } else {
            type = getServerFileChangeType(bean);
        }
        return type;
    }


    private Task getNormalType(TemporaryBean bean) {
        Task type;
        if (bean.rangeSupport) {
            type = new MultiThreadDownload(bean);
        } else {
            type = new NormalDownload(bean);
        }
        return type;
    }

    private Task getServerFileChangeType(TemporaryBean bean) {
        if (bean.rangeSupport) {
            return supportRangeType(bean);
        } else {
            return notSupportRangeType(bean);
        }
    }


    private boolean fileChanged(TemporaryBean bean) {
        return bean.serverFileChanged;
    }


    private Task supportRangeType(TemporaryBean bean) {
        if (needReDownload(bean)) {
            return new MultiThreadDownload(bean);
        }
        try {
            if (multiDownloadNotComplete(bean)) {
                return new ContinueDownload(bean);
            }
        } catch (IOException e) {
            return new MultiThreadDownload(bean);
        }
        return new AlreadyDownloaded(bean);
    }


    private Task notSupportRangeType(TemporaryBean bean) {
        if (normalDownloadNotComplete(bean)) {
            return new NormalDownload(bean);
        } else {
            return new AlreadyDownloaded(bean);
        }
    }

    private boolean multiDownloadNotComplete(TemporaryBean bean) throws IOException {
        return mFileHelper.fileNotComplete(bean.tempFile());
    }

    private boolean normalDownloadNotComplete(TemporaryBean bean) {
        return bean.file().length() != bean.contentLength;
    }

    private boolean needReDownload(TemporaryBean bean) {
        return tempFileNotExists(bean) || tempFileDamaged(bean);
    }

    private boolean tempFileDamaged(TemporaryBean bean) {
        try {
            return mFileHelper.tempFileDamaged(bean.tempFile(), bean.contentLength);
        } catch (IOException e) {
            log(DOWNLOAD_RECORD_FILE_DAMAGED);
            return true;
        }
    }

    private boolean tempFileNotExists(TemporaryBean bean) {
        return !new File(bean.tempPath).exists();
    }


}
