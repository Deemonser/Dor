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

package com.deemons.dor.cookie.persistence;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import okhttp3.Cookie;

@SuppressLint("CommitPrefEdits")
public class SharedPrefsCookiePersistor implements CookiePersistor {

    private final SharedPreferences sharedPreferences;
    private static volatile SharedPrefsCookiePersistor cookiePersistor;


    private SharedPrefsCookiePersistor(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    public static SharedPrefsCookiePersistor getInstant(Context context) {
        if (cookiePersistor == null) {
            synchronized (SharedPrefsCookiePersistor.class) {
                if (cookiePersistor == null) {
                    SharedPreferences sp = context.getSharedPreferences("CookiePersistence", Context.MODE_PRIVATE);
                    cookiePersistor = new SharedPrefsCookiePersistor(sp);
                }
            }
        }
        return cookiePersistor;
    }


    public static SharedPrefsCookiePersistor getInstant() {
        if (cookiePersistor != null) {
            return cookiePersistor;
        }
        throw new NullPointerException("SharedPrefsCookiePersistor is null");
    }


    @Override
    public List<Cookie> loadAll() {
        List<Cookie> cookies = new ArrayList<>(sharedPreferences.getAll().size());

        for (Map.Entry<String, ?> entry : sharedPreferences.getAll().entrySet()) {
            String serializedCookie = (String) entry.getValue();
            Cookie cookie = new SerializableCookie().decode(serializedCookie);
            if (cookie != null) {
                cookies.add(cookie);
            }
        }
        return cookies;
    }

    @Override
    public void saveAll(Collection<Cookie> cookies) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        for (Cookie cookie : cookies) {
            editor.putString(createCookieKey(cookie), new SerializableCookie().encode(cookie));
        }
        editor.commit();
    }

    @Override
    public void removeAll(Collection<Cookie> cookies) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        for (Cookie cookie : cookies) {
            editor.remove(createCookieKey(cookie));
        }
        editor.commit();
    }

    private static String createCookieKey(Cookie cookie) {
        return (cookie.secure() ? "https" : "http") + "://" + cookie.domain() + cookie.path() + "|" + cookie.name();
    }

    @Override
    public void clear() {
        sharedPreferences.edit().clear().commit();
    }
}
