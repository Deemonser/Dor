package com.deemons.dor.error.handler.listener;

import android.content.Context;

/**
 * Created by jess on 9/2/16 13:58
 * Contact with jess.yan.effort@gmail.com
 */
public interface ErrorListener {
    void handleError(Context context, Exception e);

    ErrorListener EMPTY = new ErrorListener() {
        @Override
        public void handleError(Context context, Exception e) {

        }
    };
}
