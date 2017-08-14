package com.deemons.dor.download.check;

import com.deemons.dor.download.entity.DownloadBean;
import com.deemons.dor.download.temporary.TemporaryBean;

import io.reactivex.Observable;

/**
 * author： deemons
 * date:    2017/8/13
 * desc:
 */

public interface ICheckHelper {

    Observable<TemporaryBean> dispatchCheck(DownloadBean downloadBean);

    Observable<TemporaryBean> checkFileExist();

    Observable<TemporaryBean> checkFileWhole();

    Observable<TemporaryBean> checkFileUpdate();

    Observable<TemporaryBean> checkRecordFile();

}
