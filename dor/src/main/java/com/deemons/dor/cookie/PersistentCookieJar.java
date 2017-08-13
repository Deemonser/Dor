/*
 * Copyright (C) 2016 Francisco José Montiel Navarro.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.deemons.dor.cookie;


import android.support.annotation.NonNull;
import android.util.Log;

import com.deemons.dor.cookie.cache.CookieCache;
import com.deemons.dor.cookie.persistence.CookiePersistor;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.HttpUrl;


public class PersistentCookieJar implements ClearableCookieJar {

    private static final String TAG = "Cookie";
    private CookieCache cache;
    private CookiePersistor persistor;

    private static PersistentCookieJar cookieJar;

    private boolean isDebug = false;


    public static PersistentCookieJar getInstant(CookieCache cache, CookiePersistor persistor) {
        if (cookieJar == null) {
            synchronized (PersistentCookieJar.class) {
                if (cookieJar == null) {
                    cookieJar = new PersistentCookieJar(cache, persistor);
                }
            }
        }
        return cookieJar;
    }


    private PersistentCookieJar(CookieCache cache, CookiePersistor persistor) {
        this.cache = cache;
        this.persistor = persistor;
        //加载到内存
        this.cache.addAll(persistor.loadAll());
    }

    @Override
    synchronized public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        cache.addAll(cookies);
        persistor.saveAll(filterPersistentCookies(cookies));

        printCookieForResponse(cookies);
    }


    /**
     * 过滤过期的Cookie
     *
     * @param cookies cookies
     * @return 未过期的cookie列表
     */
    private static List<Cookie> filterPersistentCookies(List<Cookie> cookies) {
        List<Cookie> persistentCookies = new ArrayList<>();

        for (Cookie cookie : cookies) {
            if (cookie.persistent()) {
                persistentCookies.add(cookie);
            }
        }
        return persistentCookies;
    }

    @Override
    synchronized public List<Cookie> loadForRequest(HttpUrl url) {
        List<Cookie> cookiesToRemove = new ArrayList<>();
        List<Cookie> validCookies = new ArrayList<>();

        for (Iterator<Cookie> it = cache.iterator(); it.hasNext(); ) {
            Cookie currentCookie = it.next();

            if (isCookieExpired(currentCookie)) {
                cookiesToRemove.add(currentCookie);
                it.remove();

            } else if (currentCookie.matches(url)) {
                validCookies.add(currentCookie);
            }
        }

        persistor.removeAll(cookiesToRemove);

        printCookieForRequest(validCookies);

        return validCookies;
    }

    /**
     * 打印 request 时的 Cookie
     *
     * @param cookies cookies
     */
    private void printCookieForRequest(List<Cookie> cookies) {
        if (!isDebug || cookies.size() == 0) {
            return;
        }

        StringBuilder builder = getCookieMsg(cookies);

        Log.d(TAG, "--> request: \n" + builder.toString());

    }


    /**
     * 打印 response 时的 Cookie
     *
     * @param cookies cookies
     */
    private void printCookieForResponse(List<Cookie> cookies) {
        if (!isDebug || cookies.size() == 0) {
            return;
        }

        StringBuilder builder = getCookieMsg(cookies);

        Log.d(TAG, "<-- response: \n" + builder.toString());

    }


    @NonNull
    private StringBuilder getCookieMsg(List<Cookie> cookies) {
        StringBuilder builder = new StringBuilder();

        for (Cookie cookie : cookies) {
            builder.append(cookie.name())
                    .append("=")
                    .append(cookie.value())
                    .append("; ")
                    .append(new Date(cookie.expiresAt()).toString())
                    .append("; ")
                    .append(cookie.domain())
                    .append("\n");
        }
        return builder;
    }

    private static boolean isCookieExpired(Cookie cookie) {
        return cookie.expiresAt() < System.currentTimeMillis();
    }

    @Override
    synchronized public void clearSession() {
        cache.clear();
        cache.addAll(persistor.loadAll());
    }

    @Override
    synchronized public void clear() {
        cache.clear();
        persistor.clear();
    }


    public void setDebug(boolean debug) {
        isDebug = debug;
    }

}
