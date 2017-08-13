package com.deemons.dor.utils;

import java.net.URL;

import okhttp3.Cookie;
import okhttp3.internal.http.HttpDate;

/**
 * author： deemons
 * date:    2017/8/5
 * desc:
 */

public class CookieUtils {


    /**
     * 更改 Cookie 的过期时间
     *
     * @param cookie  cookie
     * @param expires 过期时间， HttpDate.MAX_DATE 是永久
     * @return Cookie
     */
    public static Cookie changeCookieExpiresAt(Cookie cookie, long expires) {
        Cookie.Builder builder = new Cookie.Builder()
                .expiresAt(expires)
                .name(cookie.name())
                .value(cookie.value())
                .path(cookie.path());

        if (cookie.hostOnly()) {
            builder.hostOnlyDomain(cookie.domain());
        } else {
            builder.domain(cookie.domain());
        }

        if (cookie.httpOnly()) {
            builder.httpOnly();
        }

        if (cookie.secure()) {
            builder.secure();
        }

        return builder.build();
    }

    public static Cookie.Builder copy(Cookie cookie) {
        Cookie.Builder builder = new Cookie.Builder();
        if (cookie == null) {
            return builder;
        }

        builder.expiresAt(cookie.expiresAt())
                .name(cookie.name())
                .value(cookie.value())
                .path(cookie.path());

        if (cookie.hostOnly()) {
            builder.hostOnlyDomain(cookie.domain());
        } else {
            builder.domain(cookie.domain());
        }

        if (cookie.httpOnly()) {
            builder.httpOnly();
        }

        if (cookie.secure()) {
            builder.secure();
        }

        return builder;
    }


    public static Cookie.Builder buildCookie(String name, String value, URL url) {
        Cookie.Builder builder = new Cookie.Builder();
        builder = builder.name(name);
        builder = builder.value(value);
        builder = builder.expiresAt(HttpDate.MAX_DATE);
        builder = builder.hostOnlyDomain(url.getHost());
        builder = builder.path("/");
        builder = builder.httpOnly();
        return builder;
    }

    /**
     * 返回 Cookie 的 信息
     *
     * @param cookie Cookie
     * @return String
     */
    public static String toString(Cookie cookie) {

        return null;
    }


}
