package com.deemons.dor.error.handler;


import com.deemons.dor.error.core.RxErrorHandler;

import org.reactivestreams.Subscriber;


/**
 * Created by jess on 9/2/16 14:41
 * Contact with jess.yan.effort@gmail.com
 */

public abstract class ErrorHandleSubscriber<T> implements Subscriber<T> {
    private ErrorHandlerFactory mHandlerFactory;

    public ErrorHandleSubscriber(RxErrorHandler rxErrorHandler){
        this.mHandlerFactory = rxErrorHandler.getHandlerFactory();
    }


    @Override
    public void onError(Throwable e) {
        e.printStackTrace();
        mHandlerFactory.handleError(e);
    }

}

