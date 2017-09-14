package com.deemons.dor.receiver;

/**
 * 创建者      chenghaohao
 * 创建时间     2017/9/14 9:52
 * 包名       com.deemons.dor.receiver
 * 描述
 */

public class NetStateManager {

    private static NetStateManager sManager = new NetStateManager();

    private NetStateManager() {
    }

    public static NetStateManager getManager() {
        return sManager;
    }


    private NetState mNetState;
    private NetworkChangeListener mListener;

    public synchronized void setNetState(NetState state) {
        mNetState = state;
        if (mListener != null) {
            mListener.change(state);
        }
    }


    public void setNetworkChangeListener(NetworkChangeListener listener) {
        mListener = listener;
    }

    public NetState getNetState() {
        return mNetState;
    }


    interface NetworkChangeListener {
        void change(NetState state);
    }

}
