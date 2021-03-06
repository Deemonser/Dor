package com.deemons.dor.download.load;

import com.deemons.dor.download.entity.Status;
import com.deemons.dor.download.temporary.TemporaryBean;

import java.io.IOException;
import java.text.ParseException;

import io.reactivex.Observable;

/**
 * author： deemons
 * date:    2017/8/13
 * desc:
 */

public interface ILoadHelper {

    Observable<Status> dispatchDownload(TemporaryBean temporaryBean) throws IOException, ParseException;



}
