package com.deemons.dor.download.check;

import com.deemons.dor.download.entity.DownloadBean;
import com.deemons.dor.download.file.IFileHelper;
import com.deemons.dor.download.temporary.TemporaryBean;

import io.reactivex.Observable;
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

    @Override
    public Observable<TemporaryBean> dispatchCheck(DownloadBean downloadBean) {
        return Observable.just(downloadBean)
                .flatMap(new Function<DownloadBean, Observable<TemporaryBean>>() {
                    @Override
                    public Observable<TemporaryBean> apply(@NonNull DownloadBean downloadBean) throws Exception {
                        return checkFileExist();
                    }
                });

    }

    private TemporaryBean getTemporary(DownloadBean downloadBean) {
        
        return null;
    }

    @Override
    public Observable<TemporaryBean> checkFileExist() {
        return null;
    }

    @Override
    public Observable<TemporaryBean> checkFileWhole() {
        return null;
    }

    @Override
    public Observable<TemporaryBean> checkFileUpdate() {
        return null;
    }

    @Override
    public Observable<TemporaryBean> checkRecordFile() {
        return null;
    }
}
