package com.deemons.dor.error.handler;

import android.content.Context;

import com.deemons.dor.error.handler.listener.ErrorListener;


/**
 * Created by jess on 9/2/16 13:47
 * Contact with jess.yan.effort@gmail.com
 */
public class ErrorHandlerFactory {
    public final String TAG = this.getClass().getSimpleName();
    private Context mContext;
    private ErrorListener mErrorListener;

    public ErrorHandlerFactory(Context mContext, ErrorListener mErrorListener) {
        this.mErrorListener = mErrorListener;
        this.mContext = mContext;
    }

    /**
     *  处理错误
     * @param throwable
     */
    public void handleError(Throwable throwable) {
        mErrorListener.handleError(mContext, (Exception) throwable);
    }
}
