package com.deemons.dor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.deemons.dor.cookie.PersistentCookieJar;
import com.deemons.dor.cookie.cache.MemoryCookieCache;
import com.deemons.dor.cookie.persistence.SharedPrefsCookiePersistor;
import com.deemons.dor.utils.CheckUtils;

import java.io.File;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import io.rx_cache2.internal.RxCache;
import io.victoralbertos.jolyglot.GsonSpeaker;
import okhttp3.CertificatePinner;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;


/**
 * 创建者      chenghaohao
 * 创建时间     2017/8/4 16:51
 * 包名       com.deemons.network.provide
 * 描述
 * <p>
 * 1.网络请求Net
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
public final class Net {

    private static final int TIME_OUT = 10 * 1000;//10秒
    @SuppressLint("StaticFieldLeak")
    private static volatile Net net;


    private HttpUrl mApiUrl;
    private Context context;
    private long connectTimeout;
    private long readTimeout;
    private CertificatePinner mCertificatePinner;
    private List<Interceptor> mInterceptors;
    private List<Interceptor> mNetworkInterceptors;
    private File mCacheFile;
    private CookieJar cookieJar;
    private Retrofit retrofit;
    private RxCache rxCache;


    /**
     * Net 初始化
     *
     * @param context ApplicationContext
     * @param baseUrl Host
     */
    public static void init(Context context, String baseUrl) {
        init(builder().context(context).baseUrl(baseUrl));
    }


    /**
     * Net 初始化
     *
     * @param builder Net.Builder Net.builder()
     */
    public static void init(@NonNull Net.Builder builder) {
        CheckUtils.checkNotNull(builder.apiUrl, "baseUrl is required");
        CheckUtils.checkNotNull(builder.context, "context is required");

        if (builder.mLoggingInterceptor == null) {
            builder.logLevel(HttpLoggingInterceptor.Level.NONE);
        }
        builder.addInterceptor(builder.mLoggingInterceptor);

        if (net == null) {
            synchronized (Net.class) {
                if (net == null) {
                    net = new Net(builder);
                }
            }
        }
    }


    /**
     * 初始化的 Builder
     *
     * @return
     */
    public static Net.Builder builder() {
        return new Net.Builder();
    }

    public static Retrofit getRetrofit() {
        CheckUtils.checkNotNull(net, "Pleas init Net first!");
        return net.retrofit;
    }

    public static <T> T getService(Class<T> service) {
        return getRetrofit().create(service);
    }


    public static RxCache getRxCache() {
        CheckUtils.checkNotNull(net, "Pleas init Net first!");
        return net.rxCache;
    }


    public static <T> T getCache(Class<T> classProviders) {
        return getRxCache().using(classProviders);
    }

    private Net(Builder builder) {
        this.mApiUrl = builder.apiUrl;
        this.context = builder.context;
        this.cookieJar = builder.cookieJar;
        this.connectTimeout = builder.connectTimeout;
        this.readTimeout = builder.readTimeout;
        this.mInterceptors = builder.interceptors;
        this.mNetworkInterceptors = builder.networkInterceptors;
        this.mCacheFile = builder.cacheFile;
        this.mCertificatePinner = builder.certificatePinner;

        retrofit = initRetrofit();
        rxCache = initRxCache();
    }


    private Retrofit initRetrofit() {
        return new Retrofit.Builder()
                .baseUrl(mApiUrl)//域名
                .client(getOkHttpClient())//设置okhttp
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())//使用rxjava
                .addConverterFactory(ScalarsConverterFactory.create())//解析json 为 string，如果不引用此，也可用 JsonObject （非 JSONObject）接收
                .addConverterFactory(GsonConverterFactory.create())//使用Gson
                .build();
    }


    private RxCache initRxCache() {
        return new RxCache.Builder()
                .persistence(getCacheFile(), new GsonSpeaker());
    }


    private long getConnectTimeout() {
        if (connectTimeout == 0) {
            connectTimeout = TIME_OUT;
        }
        return connectTimeout;
    }

    private long getReadTimeout() {
        if (readTimeout == 0) {
            readTimeout = TIME_OUT;
        }
        return readTimeout;
    }


    /**
     * 获取 cookieJar
     *
     * @return CookieJar
     */
    private CookieJar getCookieJar() {
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
     * 应用的拦截器
     *
     * @return List
     */
    private List<Interceptor> getNetworkInterceptors() {
        return mNetworkInterceptors;
    }


    /**
     * 全局的拦截器
     *
     * @return List
     */
    private List<Interceptor> getInterceptors() {
        return mInterceptors;
    }


    private CertificatePinner getCertificatePinner() {
        return null;
    }

    /**
     * 获取 OKHTTPClient
     *
     * @return OKHttpClient
     */
    private OkHttpClient getOkHttpClient() {
        OkHttpClient.Builder builder = getOkBuilder()
                .connectTimeout(getConnectTimeout(), TimeUnit.MILLISECONDS)
                .readTimeout(getReadTimeout(), TimeUnit.MILLISECONDS)
                .cookieJar(getCookieJar());






        //addNetworkInterceptor 与 addInterceptor 的区别见下
        // https://github.com/square/okhttp/wiki/Interceptors
        if (getNetworkInterceptors() != null && getNetworkInterceptors().size() > 0) {
            for (Interceptor interceptor : getNetworkInterceptors()) {
                builder.addNetworkInterceptor(interceptor);
            }
        }

        if (getInterceptors() != null && getInterceptors().size() > 0) {
            for (Interceptor interceptor : getInterceptors()) {
                builder.addInterceptor(interceptor);
            }
        }


        if (getCertificatePinner() != null) {
            builder.certificatePinner(getCertificatePinner());
        }


        return builder.build();
    }


    private File getCacheFile() {
        if (mCacheFile == null) {
            mCacheFile = new File(context.getCacheDir(), "RxCache");
        }
        if (!mCacheFile.exists()) {
            mCacheFile.mkdirs();
        }
        return mCacheFile;
    }


    public static final class Builder {
        private HttpUrl apiUrl = HttpUrl.parse("https://api.github.com/");
        private Context context;
        private long connectTimeout;
        private long readTimeout;
        private List<Interceptor> interceptors = new ArrayList<>();
        private List<Interceptor> networkInterceptors = new ArrayList<>();
        private HttpLoggingInterceptor mLoggingInterceptor;
        private File cacheFile;
        private CookieJar cookieJar;
        private CertificatePinner certificatePinner;

        private Builder() {
        }

        public Builder baseUrl(String baseUrl) {//基础url
            if (TextUtils.isEmpty(baseUrl)) {
                throw new IllegalArgumentException("baseUrl can not be empty");
            }
            this.apiUrl = HttpUrl.parse(baseUrl);
            CheckUtils.checkNotNull(this.apiUrl, "parse baseUrl error");
            return this;
        }


        /**
         * Context for sharedPre of Cookie
         *
         * @param context context
         * @return Net.Builder
         */
        public Builder context(Context context) {
            this.context = context.getApplicationContext();
            return this;
        }

        public Builder connectTimeOut(long connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        public Builder readTimeOut(long readTimeout) {
            this.readTimeout = readTimeout;
            return this;
        }


        /**
         * 动态添加任意个interceptor
         *
         * @param interceptor 拦截器
         * @return Net.Builder
         */
        public Builder addInterceptor(Interceptor interceptor) {
            this.interceptors.add(interceptor);
            return this;
        }

        /**
         * 动态添加任意个 NetworkInterceptor
         * <p>
         * addNetworkInterceptor 与 addInterceptor 的区别见下
         * https://github.com/square/okhttp/wiki/Interceptors
         *
         * @param interceptor 拦截器
         * @return Net.Builder
         */
        public Builder addNetworkInterceptor(Interceptor interceptor) {
            this.networkInterceptors.add(interceptor);
            return this;
        }


        /**
         * 设置 SSL 证书
         *
         * @param certificatePinner ssl证书
         * @return Net.Builder
         */
        public Builder setCertificatePinner(CertificatePinner certificatePinner) {
            this.certificatePinner = certificatePinner;
            return this;
        }

        /**
         * 自定义 CookieJar
         *
         * @param cookieJar CookieJar
         * @return Net.Builder
         */
        public Builder cookieJar(CookieJar cookieJar) {
            this.cookieJar = cookieJar;
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


    }


    protected static SSLSocketFactory getSSLSocketFactory(Context context, int[] certificates) {

        if (context == null) {
            throw new NullPointerException("context == null");
        }

        CertificateFactory certificateFactory;
        try {
            certificateFactory = CertificateFactory.getInstance("X.509");
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);

            for (int i = 0; i < certificates.length; i++) {
                InputStream certificate = context.getResources().openRawResource(certificates[i]);
                keyStore.setCertificateEntry(String.valueOf(i), certificateFactory.generateCertificate(certificate));

                if (certificate != null) {
                    certificate.close();
                }
            }
            SSLContext sslContext = SSLContext.getInstance("TLS");
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);
            sslContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());
            return sslContext.getSocketFactory();


        } catch (Exception e) {

        }
        return null;
    }

}
