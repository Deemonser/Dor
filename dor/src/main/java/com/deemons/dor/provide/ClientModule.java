package com.deemons.dor.provide;

import android.content.Context;
import android.text.TextUtils;

import com.deemons.dor.cookie.PersistentCookieJar;
import com.deemons.dor.def.RequestIntercept;
import com.deemons.dor.error.core.RxErrorHandler;
import com.deemons.dor.error.handler.listener.ErrorListener;
import com.deemons.dor.inte.GlobeHttpHandler;
import com.deemons.dor.utils.FileUtils;
import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.rx_cache2.internal.RxCache;
import io.victoralbertos.jolyglot.GsonSpeaker;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import static com.deemons.dor.utils.CheckUtils.checkNotNull;


/**
 * 创建者     chenghaohao
 * 创建时间   2017/5/4 17:10
 * 包名       com.deemons.network.provide
 * 描述
 * 更新者     chenghaohao
 * 更新时间   2017/5/4 17:10
 * 更新描述    retrofit
 */
@Module
public class ClientModule {
    private static final int TIME_OUT = 10;


    @Singleton
    @Provides
    Retrofit provideRetrofit(Retrofit.Builder builder, OkHttpClient client, HttpUrl httpUrl) {
        return builder
                .baseUrl(httpUrl)//域名
                .client(client)//设置okhttp
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())//使用rxjava
                .addConverterFactory(ScalarsConverterFactory.create())//解析json 为 string，如果不引用此，也可用 JsonObject （非 JSONObject）接收
                .addConverterFactory(GsonConverterFactory.create())//使用Gson
                .build();
    }

    /**
     * 提供OkhttpClient
     *
     * @param okHttpClient
     * @return
     */
    @Singleton
    @Provides
    OkHttpClient provideClient(OkHttpClient.Builder okHttpClient, CookieJar cookieJar, Interceptor requestIntercept
            , List<Interceptor> interceptors) {
        OkHttpClient.Builder builder = okHttpClient
                .connectTimeout(TIME_OUT, TimeUnit.SECONDS)
                .readTimeout(TIME_OUT, TimeUnit.SECONDS)
                .cookieJar(cookieJar)
                .addNetworkInterceptor(requestIntercept);
        if (interceptors != null && interceptors.size() > 0) {//如果外部提供了interceptor的数组则遍历添加
            for (Interceptor interceptor : interceptors) {
                builder.addInterceptor(interceptor);
            }
        }
        return builder.build();
    }


    @Singleton
    @Provides
    Retrofit.Builder provideRetrofitBuilder() {
        return new Retrofit.Builder();
    }


    @Singleton
    @Provides
    OkHttpClient.Builder provideClientBuilder() {
        return new OkHttpClient.Builder();
    }


    @Singleton
    @Provides
    Interceptor provideIntercept(RequestIntercept requestIntercept) {
        return requestIntercept;//自定义的拦截器
    }


    /**
     * 提供RXCache客户端
     *
     * @param cacheDirectory RxCache缓存路径
     * @return
     */
    @Singleton
    @Provides
    RxCache provideRxCache(@Named("RxCacheDirectory") File cacheDirectory) {
        return new RxCache.Builder()
                .persistence(cacheDirectory, new GsonSpeaker());
    }


    /**
     * 需要单独给RxCache提供缓存路径
     * 提供RxCache缓存地址
     */
    @Singleton
    @Provides
    @Named("RxCacheDirectory")
    File provideRxCacheDirectory(File cacheDir) {
        File cacheDirectory = new File(cacheDir, "RxCache");
        return FileUtils.makeDirs(cacheDirectory);
    }

    /**
     * 提供处理Rxjava错误的管理器
     *
     * @return
     */
    @Singleton
    @Provides
    RxErrorHandler proRxErrorHandler(Context context, ErrorListener listener) {
        return RxErrorHandler
                .builder()
                .with(context)
                .responseErrorListener(listener)
                .build();
    }


    private Context mContext;
    private HttpUrl mApiUrl;
    private GlobeHttpHandler mHandler;
    private List<Interceptor> mInterceptors;
    private ErrorListener mErroListener;
    private File mCacheFile;
    private CookieJar cookieJar;
    private Gson gson;

    /**
     * @author: jess
     * @date 8/5/16 11:03 AM
     * @description: 设置baseurl
     */
    private ClientModule(Builder builder) {
        this.mContext = builder.mContext;
        this.mApiUrl = builder.apiUrl;
        this.mHandler = builder.handler;
        this.mInterceptors = builder.interceptors;
        this.mErroListener = builder.mErrorListener;
        this.mCacheFile = builder.cacheFile;
        this.cookieJar = builder.cookieJar;
        this.gson = builder.gson;
    }

    public static Builder builder() {
        return new Builder();
    }


    @Singleton
    @Provides
    Context provideContext() {
        return mContext;
    }


    @Singleton
    @Provides
    List<Interceptor> provideInterceptors() {
        return mInterceptors;
    }


    @Singleton
    @Provides
    HttpUrl provideBaseUrl() {
        return mApiUrl;
    }


    @Singleton
    @Provides
    GlobeHttpHandler provideGlobeHttpHandler() {
        return mHandler == null ? GlobeHttpHandler.EMPTY : mHandler;//打印请求信息
    }


    @Singleton
    @Provides
    CookieJar provideCookieJar(PersistentCookieJar defaultCookieJar) {
        return cookieJar != null ? cookieJar : defaultCookieJar;
    }


    @Singleton
    @Provides
    Gson provideGson() {
        return gson != null ? gson : new Gson();
    }


    /**
     * 提供缓存地址
     */
    @Singleton
    @Provides
    File provideCacheFile(Context context) {
        return mCacheFile == null ? FileUtils.getCacheFile(context) : mCacheFile;
    }


    /**
     * 提供处理Rxjava错误的管理器的回调
     *
     * @return
     */
    @Singleton
    @Provides
    ErrorListener provideResponseErroListener() {
        return mErroListener == null ? ErrorListener.EMPTY : mErroListener;
    }


    public static final class Builder {
        private Context mContext;
        private HttpUrl apiUrl = HttpUrl.parse("https://api.github.com/");
        private GlobeHttpHandler handler;
        private List<Interceptor> interceptors = new ArrayList<>();
        private ErrorListener mErrorListener;
        private HttpLoggingInterceptor mLoggingInterceptor;
        private File cacheFile;
        private CookieJar cookieJar;
        private Gson gson;

        private Builder() {
        }

        public Builder baseUrl(String baseUrl) {//基础url
            if (TextUtils.isEmpty(baseUrl)) {
                throw new IllegalArgumentException("baseUrl can not be empty");
            }
            this.apiUrl = HttpUrl.parse(baseUrl);
            return this;
        }

        public Builder context(Context context) {
            mContext = context;
            return this;
        }

        public Builder globeHttpHandler(GlobeHttpHandler handler) {//用来处理http响应结果
            this.handler = handler;
            return this;
        }

        public Builder addInterceptor(Interceptor interceptor) {//动态添加任意个interceptor
            this.interceptors.add(interceptor);
            return this;
        }

        public Builder cookieJar(CookieJar cookieJar) {
            this.cookieJar = cookieJar;
            return this;
        }

        public Builder gson(Gson gson) {
            this.gson = gson;
            return this;
        }

        public Builder responseErrorListener(ErrorListener listener) {//处理所有Rxjava的onError逻辑
            this.mErrorListener = listener;
            return this;
        }


        public Builder cacheFile(File cacheFile) {
            this.cacheFile = cacheFile;
            return this;
        }

        public Builder logLevel(HttpLoggingInterceptor.Level level) {
            mLoggingInterceptor = new HttpLoggingInterceptor();
            mLoggingInterceptor.setLevel(level);
            return this;
        }


        public ClientModule build() {
            checkNotNull(mContext, "Context is required");
            checkNotNull(apiUrl, "baseUrl is required");

            if (mLoggingInterceptor == null) {
                logLevel(HttpLoggingInterceptor.Level.NONE);
            }
            addInterceptor(mLoggingInterceptor);
            return new ClientModule(this);
        }


    }

}