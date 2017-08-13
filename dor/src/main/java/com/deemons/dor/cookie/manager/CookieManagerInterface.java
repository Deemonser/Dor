package com.deemons.dor.cookie.manager;

import java.util.List;

import okhttp3.Cookie;

/**
 * 创建者      chenghaohao
 * 创建时间     2017/5/5 17:17
 * 包名       com.deemons.network.cookie
 * 描述
 */

public interface CookieManagerInterface {



    /**
     * 添加
     *
     * @param cookie cookie
     */
    void addCookie(Cookie cookie);

    /**
     * 删除
     *
     * @param CookieName CookieName
     */
    void deleteCookie(String CookieName);

    /**
     * 获取Cookie
     *
     * @param cookieName cookieName
     * @return Cookie
     */
    Cookie getCookie(String cookieName);

    /**
     * 获取所以Cookie
     *
     * @return List<Cookie>
     */
    List<Cookie> getCookies();

}
