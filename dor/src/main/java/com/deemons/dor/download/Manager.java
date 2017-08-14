package com.deemons.dor.download;

import com.deemons.dor.download.check.ICheckHelper;
import com.deemons.dor.download.entity.DownloadBean;
import com.deemons.dor.download.entity.Status;
import com.deemons.dor.download.load.ILoadHelper;
import com.deemons.dor.download.task.Task;
import com.deemons.dor.download.temporary.TemporaryRecord;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;

/**
 * 创建者      chenghaohao
 * 创建时间     2017/8/14 10:36
 * 包名       com.deemons.dor
 * 描述
 */

public class Manager {

    ICheckHelper mCheckHelper;

    ILoadHelper mLoadHelper;

    public Observable<Status> download(DownloadBean downloadBean) {
        return Observable.just(downloadBean)
                .flatMap(new Function<DownloadBean, Observable<Task>>() {
                    @Override
                    public Observable<Task> apply(@NonNull DownloadBean downloadBean) throws Exception {
                        return mCheckHelper.dispatchCheck(downloadBean);
                    }
                })
                .flatMap(new Function<Task, Observable<Status>>() {
                    @Override
                    public Observable<Status> apply(@NonNull Task task) throws Exception {
                        return mLoadHelper.dispatchDownload(task);
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


    /**
     * read last modify string
     *
     * @param url key
     * @return last modify
     */
    public String readLastModify(String url) {
        try {
            return map.get(url).readLastModify();
        } catch (IOException e) {
            //TODO log
            //If read failed,return an empty string.
            //If we send empty last-modify,server will response 200.
            //That means file changed.
            return "";
        }
    }

    public boolean fileExists(String url) {
        return map.get(url).file().exists();
    }

    public File[] getFiles(String url) {
        return map.get(url).getFiles();
    }




}
