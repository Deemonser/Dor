package com.deemons.dor.download.check;

import android.util.Log;

import com.deemons.dor.constant.Api;
import com.deemons.dor.download.entity.DownloadBean;
import com.deemons.dor.download.file.FileHelper;
import com.deemons.dor.download.load.DownloadType;
import com.deemons.dor.download.temporary.TemporaryBean;

import java.io.File;
import java.io.IOException;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import retrofit2.Response;

import static android.text.TextUtils.concat;
import static com.deemons.dor.constant.Constant.CACHE;
import static com.deemons.dor.constant.Constant.DOWNLOAD_RECORD_FILE_DAMAGED;
import static com.deemons.dor.constant.Constant.REQUEST_RETRY_HINT;
import static com.deemons.dor.constant.Constant.TEST_RANGE_SUPPORT;
import static com.deemons.dor.constant.Constant.URL_ILLEGAL;
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


    private FileHelper mFileHelper;
    private Api mApi;

    private int maxRetryCount;
    private int maxThreads;

    private String savePath;


    public CheckHelper(FileHelper fileHelper, Api api, String savePath, int maxRetryCount, int maxThreads) {
        mApi = api;
        this.maxRetryCount = maxRetryCount;
        this.maxThreads = maxThreads;
        this.savePath = savePath;

        mFileHelper = fileHelper;
    }

    @Override
    public Observable<TemporaryBean> dispatchCheck(DownloadBean downloadBean) {
        return Observable.just(downloadBean)
                .map(new Function<DownloadBean, TemporaryBean>() {
                    @Override
                    public TemporaryBean apply(@NonNull DownloadBean downloadBean) throws Exception {
                        return getTemporary(downloadBean);
                    }
                })
                .flatMap(new Function<TemporaryBean, ObservableSource<TemporaryBean>>() {
                    @Override
                    public ObservableSource<TemporaryBean> apply(TemporaryBean bean) throws Exception {
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
    private Observable<TemporaryBean> getDownloadType(final TemporaryBean bean) {
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
                .flatMap(new Function<TemporaryBean, ObservableSource<TemporaryBean>>() {
                    @Override
                    public ObservableSource<TemporaryBean> apply(TemporaryBean temporaryBean) throws Exception {
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
    private Observable<TemporaryBean> nonExistsType(TemporaryBean temporaryBean) {
        return Observable.just(temporaryBean)
                .flatMap(new Function<TemporaryBean, ObservableSource<TemporaryBean>>() {
                    @Override
                    public ObservableSource<TemporaryBean> apply(TemporaryBean bean)
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
    private Observable<TemporaryBean> existsType(TemporaryBean temporaryBean) {
        return Observable.just(temporaryBean)
                .map(new Function<TemporaryBean, TemporaryBean>() {
                    @Override
                    public TemporaryBean apply(TemporaryBean bean) throws Exception {
                        bean.lastModify = readLastModify(bean);
                        return bean;
                    }
                })
                .flatMap(new Function<TemporaryBean, ObservableSource<TemporaryBean>>() {
                    @Override
                    public ObservableSource<TemporaryBean> apply(TemporaryBean bean) throws Exception {
                        return checkFileUpdate(bean);
                    }
                })
                .flatMap(new Function<TemporaryBean, ObservableSource<TemporaryBean>>() {
                    @Override
                    public ObservableSource<TemporaryBean> apply(TemporaryBean bean)
                            throws Exception {
                        return Observable.just(generateFileExistsType(bean));
                    }
                });
    }

    private String readLastModify(TemporaryBean bean) {
        try {
            return mFileHelper.readLastModify(bean.lastModifyFile());
        } catch (IOException e) {
            //If read failed,return an empty string.
            //If we send empty last-modify,server will response 200.
            //That means file changed.
            return "";
        }
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

                        Log.d("CheckHelper", "checkUrl()");

                        if (!resp.isSuccessful()) {
                            return checkUrlByGet(bean);
                        } else {
                            saveFileInfo(bean, resp);
                            return Observable.just(new Object());
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


    private ObservableSource<Object> checkUrlByGet(final TemporaryBean bean) {
        return mApi.checkByGet(bean.bean.url)
                .doOnNext(new Consumer<Response<Void>>() {
                    @Override
                    public void accept(Response<Void> response) throws Exception {
                        Log.d("CheckHelper", "checkUrlByGet()");
                        if (!response.isSuccessful()) {
                            throw new IllegalArgumentException(formatStr(URL_ILLEGAL, bean.bean.url));
                        } else {
                            saveFileInfo(bean, response);
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

        String[] paths = getPaths(temporaryBean.bean.saveName, temporaryBean.bean.savePath);
        temporaryBean.filePath = paths[0];
        temporaryBean.tempPath = paths[1];
        temporaryBean.lmfPath = paths[2];
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
    public TemporaryBean generateNonExistsType(TemporaryBean bean) {
        return getNormalType(bean);
    }

    /**
     * return file exists download type
     *
     * @param bean TemporaryBean
     * @return download type
     */
    public TemporaryBean generateFileExistsType(TemporaryBean bean) {
        return bean.serverFileChanged ? getNormalType(bean) : getServerFileChangeType(bean);
    }


    private TemporaryBean getNormalType(TemporaryBean bean) {
        if (bean.rangeSupport) {
            bean.setDownloadType(DownloadType.MULTI_THREAD);
        } else {
            bean.setDownloadType(DownloadType.NORMAL);
        }
        return bean;
    }

    private TemporaryBean getServerFileChangeType(TemporaryBean bean) {
        return bean.rangeSupport ? supportRangeType(bean) : notSupportRangeType(bean);
    }



    private TemporaryBean supportRangeType(TemporaryBean bean) {
        if (needReDownload(bean)) {
            bean.setDownloadType(DownloadType.MULTI_THREAD);
            return bean;
        }
        try {
            if (multiDownloadNotComplete(bean)) {
                bean.setDownloadType(DownloadType.CONTINUE);
                return bean;
            }
        } catch (IOException e) {
            bean.setDownloadType(DownloadType.MULTI_THREAD);
            return bean;
        }
        bean.setDownloadType(DownloadType.ALREADY);
        return bean;
    }


    private TemporaryBean notSupportRangeType(TemporaryBean bean) {
        bean.setDownloadType(normalDownloadNotComplete(bean) ? DownloadType.NORMAL : DownloadType.ALREADY);
        return bean;
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
