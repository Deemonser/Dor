package com.deemons.dor.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;

/**
 * 创建者      chenghaohao
 * 创建时间     2017/9/14 9:33
 * 包名       com.deemons.dor.receiver
 * 描述
 */

public class NetWorkStateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        //检测API是不是小于23，因为到了API23之后getNetworkInfo(int networkType)方法被弃用
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {

            //获得ConnectivityManager对象
            ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            //获取ConnectivityManager对象对应的NetworkInfo对象
            //获取WIFI连接的信息
            NetworkInfo wifiNetworkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            //获取移动数据连接的信息
            NetworkInfo dataNetworkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            NetState state = new NetState(isConnected(dataNetworkInfo),
                    isAvailable(dataNetworkInfo),
                    isConnected(wifiNetworkInfo),
                    isAvailable(wifiNetworkInfo));

            NetStateManager.getManager().setNetState(state);

        } else {
            //API大于23时使用下面的方式进行网络监听

            //获得ConnectivityManager对象
            ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            //获取所有网络连接的信息
            Network[] networks = connMgr.getAllNetworks();

            boolean isMobile = false;
            boolean isAvailableOfMobile = false;
            boolean isWifi = false;
            boolean isAvailableOfWifi = false;

            //通过循环将网络信息逐个取出来
            for (int i = 0; i < networks.length; i++) {
                //获取ConnectivityManager对象对应的NetworkInfo对象
                NetworkInfo networkInfo = connMgr.getNetworkInfo(networks[i]);
                if (networkInfo == null) {
                } else if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                    isMobile = true;
                    isAvailableOfMobile = isAvailable(networkInfo);
                } else if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    isWifi = true;
                    isAvailableOfWifi = isAvailable(networkInfo);
                }
            }
            NetState state = new NetState(isMobile, isAvailableOfMobile, isWifi, isAvailableOfWifi);
            NetStateManager.getManager().setNetState(state);
        }
    }

    private boolean isConnected(NetworkInfo dataNetworkInfo) {
        return dataNetworkInfo != null && dataNetworkInfo.isConnected();
    }

    /**
     * 判定网络是否可用
     *
     * @param networkInfo 网络状态
     * @return 是 可用；否，不可用
     */
    private boolean isAvailable(NetworkInfo networkInfo) {
        return networkInfo != null && networkInfo.getState() == NetworkInfo.State.CONNECTED;
    }
}
