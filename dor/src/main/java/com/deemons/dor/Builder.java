package com.deemons.dor;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.deemons.dor.utils.CheckUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;

import okhttp3.CertificatePinner;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.CallAdapter;
import retrofit2.Converter;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * 创建者      chenghaohao
 * 创建时间     2017/8/18 9:23
 * 包名       com.deemons.dor
 * 描述
 */

public class Builder {
    Context context;
    HttpUrl apiUrl = HttpUrl.parse("https://api.github.com/");
    long connectTimeout;
    long readTimeout;
    List<Interceptor> interceptors = new ArrayList<>();
    List<Interceptor> networkInterceptors = new ArrayList<>();
    List<CallAdapter.Factory> callAdapters = new ArrayList<>();
    List<Converter.Factory> converters = new ArrayList<>();
    HashMap<String,String> headers;
    HttpLoggingInterceptor loggingInterceptor;
    SSLSocketFactory sSLSocketFactory;
    HostnameVerifier hostnameVerifier;
    CertificatePinner certificatePinner;
    CookieJar cookieJar;

    String downloadSavePath;
    int maxRetryCount;
    int maxThreadCount;

    Builder() {
        callAdapters.add(RxJava2CallAdapterFactory.create());//使用rxjava
        converters.add(ScalarsConverterFactory.create());//解析json 为 string，如果不引用此，也可用 JsonObject （非 JSONObject）接收
        converters.add(GsonConverterFactory.create());//使用Gson

        logLevel(HttpLoggingInterceptor.Level.NONE);
    }

    public Builder baseUrl(String baseUrl) {
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
     * @return Dor.Builder
     */
    public Builder context(@NonNull Context context) {
        this.context = context.getApplicationContext();
        return this;
    }


    public Builder header(String name,String value) {
        if (headers == null) {
            headers = new HashMap<>();
        }
        headers.put(name, value);
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
     * @return Dor.Builder
     */
    public Builder addInterceptor(@NonNull Interceptor interceptor) {
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
     * @return Dor.Builder
     */
    public Builder addNetworkInterceptor(@NonNull Interceptor interceptor) {
        this.networkInterceptors.add(interceptor);
        return this;
    }


    /**
     * 自定义 CookieJar
     *
     * @param cookieJar CookieJar
     * @return Dor.Builder
     */
    public Builder cookieJar(CookieJar cookieJar) {
        this.cookieJar = cookieJar;
        return this;
    }


    public Builder logLevel(@NonNull HttpLoggingInterceptor.Level level) {
        loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(level);
        return this;
    }

    public Builder downloadPath(String downloadSavePath) {
        this.downloadSavePath = downloadSavePath;
        return this;
    }

    public Builder maxRetryCount(int maxRetryCount) {
        this.maxRetryCount = maxRetryCount;
        return this;
    }


    public Builder maxThreadCount(int maxThreadCount) {
        this.maxThreadCount = maxThreadCount;
        return this;
    }


    public Builder setAllCallAdapterFactory(@NonNull List<CallAdapter.Factory> list) {
        callAdapters = list;
        return this;
    }

    public Builder setAllConverterFactory(@NonNull List<Converter.Factory> list) {
        converters = list;
        return this;
    }

    public Builder setSSLSocketFactory(@NonNull SSLSocketFactory sSLSocketFactory) {
        this.sSLSocketFactory = sSLSocketFactory;
        return this;
    }

    public Builder setHostnameVerifier(@NonNull HostnameVerifier hostnameVerifier) {
        this.hostnameVerifier = hostnameVerifier;
        return this;
    }

    public Builder setCertificatePinner(@NonNull CertificatePinner certificatePinner) {
        this.certificatePinner = certificatePinner;
        return this;
    }

    public List<CallAdapter.Factory> getAllCallAdapterFactory() {
        return callAdapters;
    }

    public List<Converter.Factory> getAllConverterFactory() {
        return converters;
    }


}
