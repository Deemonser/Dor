package com.deemons.dor.download.check;

import com.deemons.dor.download.constant.DownloadApi;
import com.deemons.dor.download.entity.DownloadBean;
import com.deemons.dor.download.file.IFileHelper;
import com.deemons.dor.download.task.Task;
import com.deemons.dor.download.temporary.TemporaryBean;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;

/**
 * 创建者      chenghaohao
 * 创建时间     2017/8/14 10:47
 * 包名       com.deemons.dor.download.check
 * 描述
 */

public class CheckHepler implements ICheckHelper {

    IFileHelper mFileHelper;
    DownloadApi api;

    @Override
    public Observable<Task> dispatchCheck(DownloadBean downloadBean) {
        return Observable.just(downloadBean)
                .map(new Function<DownloadBean, TemporaryBean>() {
                    @Override
                    public TemporaryBean apply(@NonNull DownloadBean downloadBean) throws Exception {
                        return getTemporary(downloadBean);
                    }
                })
                .flatMap(new Function<TemporaryBean, Observable<TemporaryBean>>() {
                    @Override
                    public Observable<TemporaryBean> apply(@NonNull TemporaryBean bean) throws Exception {
                        return checkFileExist(bean);
                    }
                }).flatMap(new Function<TemporaryBean, ObservableSource<Task>>() {
                    @Override
                    public ObservableSource<Task> apply(@NonNull TemporaryBean bean) throws Exception {
                        return null;
                    }
                });


    }

    @Override
    public Observable<TemporaryBean> checkFileExist(TemporaryBean bean) {

        return null;
    }

    @Override
    public Observable<TemporaryBean> checkFileWhole(TemporaryBean bean) {
        return null;
    }

    @Override
    public Observable<TemporaryBean> checkFileUpdate(TemporaryBean bean) {
        return null;
    }

    @Override
    public Observable<TemporaryBean> checkRecordFile(TemporaryBean bean) {
        return null;
    }

    private TemporaryBean getTemporary(DownloadBean downloadBean) {
        return new TemporaryBean(downloadBean);
    }


//    /**
//     * Save file info
//     *
//     * @param url      key
//     * @param response response
//     */
//    public void saveFileInfo(String url, Response<?> response) {
//        TemporaryRecord record = map.get(url);
//        if (empty(record.getSaveName())) {
//            record.setSaveName(fileName(url, response));
//        }
//        record.setContentLength(contentLength(response));
//        record.setLastModify(lastModify(response));
//    }
//
//    /**
//     * Save range info
//     *
//     * @param url      key
//     * @param response response
//     */
//    public void saveRangeInfo(String url, Response<?> response) {
//        map.get(url).setRangeSupport(!notSupportRange(response));
//    }
//
//
//    /**
//     * Save file state, change or not change.
//     *
//     * @param url      key
//     * @param response response
//     */
//    public void saveFileState(String url, Response<Void> response) {
//        if (response.code() == 304) {
//            map.get(url).setFileChanged(false);
//        } else if (response.code() == 200) {
//            map.get(url).setFileChanged(true);
//        }
//    }
//
//
//
//
//    /**
//     * return file not exists download type.
//     *
//     * @param url key
//     * @return download type
//     */
//    public Task generateNonExistsType(String url) {
//        return getNormalType(url);
//    }
//
//    /**
//     * return file exists download type
//     *
//     * @param url key
//     * @return download type
//     */
//    public Task generateFileExistsType(String url) {
//        Task type;
//        if (fileChanged(url)) {
//            type = getNormalType(url);
//        } else {
//            type = getServerFileChangeType(url);
//        }
//        return type;
//    }
//
//
//    private Task getNormalType(String url) {
//        Task type;
//        if (supportRange(url)) {
//            type = new MultiThreadDownload(map.get(url));
//        } else {
//            type = new NormalDownload(map.get(url));
//        }
//        return type;
//    }
//
//    private Task getServerFileChangeType(String url) {
//        if (supportRange(url)) {
//            return supportRangeType(url);
//        } else {
//            return notSupportRangeType(url);
//        }
//    }
//
//
//    private boolean fileChanged(String url) {
//        return map.get(url).isFileChanged();
//    }
//
//
//    private Task supportRangeType(String url) {
//        if (needReDownload(url)) {
//            return new MultiThreadDownload(map.get(url));
//        }
//        try {
//            if (multiDownloadNotComplete(url)) {
//                return new ContinueDownload(map.get(url));
//            }
//        } catch (IOException e) {
//            return new MultiThreadDownload(map.get(url));
//        }
//        return new AlreadyDownloaded(map.get(url));
//    }
//
//
//
//    private Task notSupportRangeType(String url) {
//        if (normalDownloadNotComplete(url)) {
//            return new NormalDownload(map.get(url));
//        } else {
//            return new AlreadyDownloaded(map.get(url));
//        }
//    }
//
//    private boolean multiDownloadNotComplete(String url) throws IOException {
//        return map.get(url).fileNotComplete();
//    }
//
//    private boolean normalDownloadNotComplete(String url) {
//        return !map.get(url).fileComplete();
//    }
//
//    private boolean needReDownload(String url) {
//        return tempFileNotExists(url) || tempFileDamaged(url);
//    }
//
//    private boolean tempFileDamaged(String url) {
//        try {
//            return map.get(url).tempFileDamaged();
//        } catch (IOException e) {
//            log(DOWNLOAD_RECORD_FILE_DAMAGED);
//            return true;
//        }
//    }
//
//    private boolean tempFileNotExists(String url) {
//        return !map.get(url).tempFile().exists();
//    }
//
//
//    private boolean supportRange(String url) {
//        return map.get(url).isSupportRange();
//    }
//


}
