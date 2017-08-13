package com.deemons.dor.cookie.manager;


import com.deemons.dor.cookie.cache.CookieCache;
import com.deemons.dor.cookie.cache.MemoryCookieCache;
import com.deemons.dor.cookie.persistence.CookiePersistor;
import com.deemons.dor.cookie.persistence.SharedPrefsCookiePersistor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import okhttp3.Cookie;

/**
 * author： deemons
 * date:    2017/8/5
 * desc:
 */

public class CookieManager implements CookieManagerInterface {

    private CookieCache mCache;
    private CookiePersistor mPersistor;

    private static volatile CookieManager cookieManager;

    private CookieManager(CookieCache cache, CookiePersistor persistor) {
        if (cache == null) {
            throw new NullPointerException("Cookie Cache is requested");
        }
        if (persistor == null) {
            throw new NullPointerException("Cookie persistor is requested");
        }

        this.mCache = cache;
        this.mPersistor = persistor;
    }


    /**
     * 获取 CookieManager 实例
     *
     * @param cookieCache CookieCache , 默认值 MemoryCookie.getInstant
     * @return CookieManager
     */
    public static CookieManager getInstant(CookieCache cookieCache, CookiePersistor persistor) {
        if (cookieManager == null) {
            synchronized (CookieManager.class) {
                if (cookieManager == null) {
                    cookieManager = new CookieManager(cookieCache, persistor);
                }
            }
        }
        return cookieManager;
    }


    /**
     * 获取 CookieManager 实例
     * 默认使用 MemoryCookieCache
     *
     * @return CookieManager
     */
    public static CookieManager getInstant() {
        return getInstant(MemoryCookieCache.getInstant(), SharedPrefsCookiePersistor.getInstant());
    }

    @Override
    public void addCookie(Cookie cookie) {
        ArrayList<Cookie> list = new ArrayList<>();
        list.add(cookie);
        mCache.addAll(list);
        mPersistor.saveAll(list);
    }

    @Override
    public void deleteCookie(String CookieName) {
        for (Iterator<Cookie> it = mCache.iterator(); it.hasNext(); ) {
            Cookie cookie = it.next();
            if (cookie.name().equals(CookieName)) {
                it.remove();
                ArrayList<Cookie> list = new ArrayList<>();
                list.add(cookie);
                mPersistor.removeAll(list);
            }
        }
    }


    @Override
    public Cookie getCookie(String cookieName) {
        for (Cookie cookie : mCache) {
            if (cookie.name().equals(cookieName)) {
                return cookie;
            }
        }
        return null;
    }

    @Override
    public List<Cookie> getCookies() {
        List<Cookie> copy = new ArrayList<>();
        for (Cookie aMCache : mCache) {
            copy.add(aMCache);
        }
        return copy;

    }
}
