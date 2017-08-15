package com.deemons.dor.download.check;

import com.deemons.dor.download.entity.DownloadBean;
import com.deemons.dor.download.task.Task;

import io.reactivex.Observable;

/**
 * author： deemons
 * date:    2017/8/13
 * desc:
 */

public interface ICheckHelper {

    Observable<Task> dispatchCheck(DownloadBean downloadBean);



}
