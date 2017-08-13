package com.deemons.dor.error.core;

import android.content.Context;

import com.deemons.dor.error.handler.ErrorHandlerFactory;
import com.deemons.dor.error.handler.listener.ErrorListener;
import com.deemons.dor.utils.CheckUtils;


/**
 * Created by jess on 9/2/16 13:27
 * Contact with jess.yan.effort@gmail.com
 */
public class RxErrorHandler {
    public final String TAG = this.getClass().getSimpleName();
    private ErrorHandlerFactory mHandlerFactory;

    private RxErrorHandler(Builder builder) {
        this.mHandlerFactory = builder.errorHandlerFactory;
    }

    public static Builder builder() {
        return new Builder();
    }

    public ErrorHandlerFactory getHandlerFactory() {
        return mHandlerFactory;
    }

    public static final class Builder {
        private Context context;
        private ErrorListener mErrorListener;
        private ErrorHandlerFactory errorHandlerFactory;

        private Builder() {
        }

        public Builder with(Context context) {
            this.context = context;
            return this;
        }

        public Builder responseErrorListener(ErrorListener errorListener) {
            this.mErrorListener = errorListener;
            return this;
        }

        public RxErrorHandler build() {
            CheckUtils.checkNotNull(context,"context is required");
            CheckUtils.checkNotNull(mErrorListener,"responseErrorListener is required");


            this.errorHandlerFactory = new ErrorHandlerFactory(context, mErrorListener);

            return new RxErrorHandler(this);
        }
    }


}
