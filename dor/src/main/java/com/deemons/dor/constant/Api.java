package com.deemons.dor.constant;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.HEAD;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * Author: Season(ssseasonnn@gmail.com)
 * Date: 2016/10/19
 * Time: 10:02
 * Download Api
 */
public interface Api {

    /**
     * 下载的文件流
     * @param range 标记 下载的区间段 例如：“0-1024”
     * @param url   地址
     * @return Observable
     */
    @GET
    @Streaming
    Flowable<Response<ResponseBody>> download(@Header("Range") String range, @Url String url);

    @HEAD
    Observable<Response<Void>> check(@Url String url);

    @GET
    Observable<Response<Void>> checkByGet(@Url String url);

    @HEAD
    Observable<Response<Void>> checkRangeByHead(@Header("Range") String range,
                                                @Url String url);

    @HEAD
    Observable<Response<Void>> checkFileByHead(@Header("If-Modified-Since") String lastModify,
                                               @Url String url);



    @Multipart
    @POST()
    Observable<ResponseBody> uploads(
            @Url String url,
            @Part MultipartBody.Part file);


}
