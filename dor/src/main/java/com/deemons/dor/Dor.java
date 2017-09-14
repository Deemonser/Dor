package com.deemons.dor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;

import com.deemons.dor.cookie.PersistentCookieJar;
import com.deemons.dor.cookie.cache.MemoryCookieCache;
import com.deemons.dor.cookie.persistence.SharedPrefsCookiePersistor;
import com.deemons.dor.download.LoadManager;
import com.deemons.dor.download.entity.DownloadBean;
import com.deemons.dor.download.entity.Status;
import com.deemons.dor.interceptor.AddHeaderInterceptor;
import com.deemons.dor.utils.CheckUtils;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import okhttp3.CookieJar;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.CallAdapter;
import retrofit2.Converter;
import retrofit2.Retrofit;


/**
 * 创建者      chenghaohao
 * 创建时间     2017/8/4 16:51
 * 包名       com.deemons.network.provide
 * 描述
 * <p>
 * 1.网络请求封装
 * 2.保持Retrofit特性，动态代理
 * 3.使用rxCache做网络缓存
 * 4.结合使用rxJava2
 * 5.配置简化，但保留其灵活可配置
 * 6.默认Gson解析，支持解析为String
 * 7.支持文件上传，下载，进度回调，断点续传
 * 8.https证书验签
 * 9.自动管理Cookie，做持久化，并可操作
 * 10.日志管理
 * 11.统一结果回调
 * 12.网络状态管理（网络不可用、网络错误）
 */
public final class Dor {

    private static final int TIME_OUT = 10 * 1000;//10秒
    @SuppressLint("StaticFieldLeak")
    private static volatile Dor dor;
    private static volatile LoadManager mDownload;
    @SuppressLint("StaticFieldLeak")
    private static LoadManager.Builder downloadBuilder;

    private Retrofit retrofit;

    private Dor(Builder builder) {
        //初始化默认值
        builder.init();

        //添加 增加 Header 拦截器
        if (builder.headers != null) {
            builder.interceptors.add(new AddHeaderInterceptor(builder.headers));
        }

        //添加 Log 拦截器
        builder.interceptors.add(builder.loggingInterceptor);

        retrofit = initRetrofit(builder);

        downloadBuilder = LoadManager.builder()
                .context(builder.context)
                .retrofit(retrofit)
                .downloadSavePath(builder.downloadSavePath)
                .maxRetryCount(builder.maxRetryCount)
                .maxThreadCount(builder.maxThreadCount);


    }


    /**
     * Dor 初始化
     *
     * @param context ApplicationContext
     */
    public static void init(Context context) {
        init(builder().context(context));
    }


    /**
     * Dor 初始化
     *
     * @param builder Dor.Builder Dor.builder()
     */
    public static void init(@NonNull Builder builder) {
        CheckUtils.checkNotNull(builder.context, "context is required");

        if (dor == null) {
            synchronized (Dor.class) {
                if (dor == null) {
                    dor = new Dor(builder);
                }
            }
        }
    }


    /**
     * 初始化 Dor 所需的 Builder
     *
     * @return Builder
     */
    public static Builder builder() {
        return new Builder();
    }

    public static Retrofit getRetrofit() {
        CheckUtils.checkNotNull(dor, "Pleas init Dor first!");
        return dor.retrofit;
    }


    public static Observable<Status> download(String url) {
        return download(url, null);
    }


    public static Observable<Status> download(String url, String saveName) {
        return download(url, saveName, null);
    }


    public static Observable<Status> download(String url, String saveName, String savePath) {
        return download(new DownloadBean.Builder(url).setSaveName(saveName)
                .setSavePath(savePath).build());
    }


    public static Observable<Status> download(DownloadBean downloadBean) {
        CheckUtils.checkNotNull(dor, "Pleas init Dor first!");
        return getDownloadManager().download(downloadBean);
    }

    public static <T> T getAPI(Class<T> service) {
        return getRetrofit().create(service);
    }


    public static <T> T getCache(Class<T> classProviders) {
        //        return getRxCache().using(classProviders);
        return null;
    }


    private Retrofit initRetrofit(Builder builder) {
        Retrofit.Builder retrofitBuilder = new Retrofit.Builder()
                .baseUrl(builder.apiUrl)//域名
                .client(getOkHttpClient(builder));//设置okhttp


        for (CallAdapter.Factory factory : builder.callAdapters) {
            retrofitBuilder.addCallAdapterFactory(factory);
        }

        for (Converter.Factory factory : builder.converters) {
            retrofitBuilder.addConverterFactory(factory);
        }

        return retrofitBuilder.build();
    }


    //    private RxCache initRxCache(Builder builder) {
    //        return new RxCache.Builder()
    //                .persistence(getCacheFile(), new GsonSpeaker());
    //    }


    private static LoadManager getDownloadManager() {
        if (mDownload == null) {
            synchronized (Dor.class) {
                if (mDownload == null) {
                    mDownload = new LoadManager(downloadBuilder);
                }
            }
        }
        return mDownload;
    }


    private long getConnectTimeout(long connectTimeout) {
        if (connectTimeout == 0) {
            connectTimeout = TIME_OUT;
        }
        return connectTimeout;
    }

    private long getReadTimeout(long readTimeout) {
        if (readTimeout == 0) {
            readTimeout = TIME_OUT;
        }
        return readTimeout;
    }


    /**
     * 获取 cookieJar
     *
     * @param cookieJar
     * @return CookieJar
     */
    private CookieJar getCookieJar(Context context, CookieJar cookieJar) {
        if (cookieJar == null) {
            MemoryCookieCache cookieCache = MemoryCookieCache.getInstant();
            SharedPrefsCookiePersistor cookiePersistor = SharedPrefsCookiePersistor.getInstant(context);
            cookieJar = PersistentCookieJar.getInstant(cookieCache, cookiePersistor);
        }
        return cookieJar;
    }

    /**
     * 获取 OkHttpClient.Builder
     *
     * @return OkHttpClient.Builder
     */
    private OkHttpClient.Builder getOkBuilder() {
        return new OkHttpClient.Builder();
    }


    /**
     * 获取 OKHTTPClient
     *
     * @param builder
     * @return OKHttpClient
     */
    private OkHttpClient getOkHttpClient(Builder builder) {
        OkHttpClient.Builder okBuilder = getOkBuilder()
                .connectTimeout(getConnectTimeout(builder.connectTimeout), TimeUnit.MILLISECONDS)
                .readTimeout(getReadTimeout(builder.readTimeout), TimeUnit.MILLISECONDS)
                .cookieJar(getCookieJar(builder.context, builder.cookieJar));


        //addNetworkInterceptor 与 addInterceptor 的区别见下
        // https://github.com/square/okhttp/wiki/Interceptors
        for (Interceptor interceptor : builder.networkInterceptors) {
            okBuilder.addNetworkInterceptor(interceptor);
        }

        for (Interceptor interceptor : builder.interceptors) {
            okBuilder.addInterceptor(interceptor);
        }

        if (builder.sSLSocketFactory != null) {
            okBuilder.socketFactory(builder.sSLSocketFactory);
        }

        if (builder.hostnameVerifier != null) {
            okBuilder.hostnameVerifier(builder.hostnameVerifier);
        }

        if (builder.certificatePinner != null) {
            okBuilder.certificatePinner(builder.certificatePinner);
        }


        return okBuilder.build();
    }


}
