package com.deemons.download;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.Path;
import retrofit2.http.Url;

/**
 * 创建者      chenghaohao
 * 创建时间     2017/9/13 15:05
 * 包名       com.deemons.download
 * 描述
 */

public interface API {
    String HOST = "http://gank.io/api/";


    /**
     * 技术文章列表
     */
    @GET("data/{tech}/{num}/{page}")
    Observable<String> getTechList(@Path("tech") String tech, @Path("num") int num, @Path("page") int page);

    /**
     * 妹纸列表
     */
    @GET("data/福利/{num}/{page}")
    Observable<String> getGirlList(@Path("num") int num, @Path("page") int page);

    /**
     * 随机妹纸图
     */
    @GET("random/data/福利/{num}")
    Observable<String> getRandomGirl(@Path("num") int num);


//    @GET("data/福利/{num}/{page}")
//    Call<ResponseBody> getGirlPic(@Path("num") int num, @Path("page") int page);

    @GET() //也可以不填，然后在参数里面指定全路径的URL
    Call<ResponseBody> getGirlPic(@Url String url);


    /**
     * method 表示请求的方法，区分大小写
     * path表示路径
     * hasBody表示是否有请求体
     */
    @HTTP(method = "GET", path = "data/福利/{num}/{page}", hasBody = false)
    Call<ResponseBody> getGirlPic(@Path("num") int num, @Path("page") int page);



}
