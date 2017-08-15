package com.deemons.dor.download.load;

import com.deemons.dor.download.entity.Status;
import com.deemons.dor.download.task.Task;

import java.io.IOException;
import java.text.ParseException;

import io.reactivex.Observable;

/**
 * author： deemons
 * date:    2017/8/15
 * desc:
 */

public class LoadHelper implements ILoadHelper {

    @Override
    public Observable<Status> dispatchDownload(Task task) throws IOException, ParseException {
        task.prepareDownload();
        return task.startDownload();
    }

    @Override
    public void checkRange() {

    }

    @Override
    public void multiThreadLoad() {

    }

    @Override
    public void singleThreadLoad() {

    }
}
