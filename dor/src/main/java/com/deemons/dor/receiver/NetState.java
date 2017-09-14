package com.deemons.dor.receiver;

/**
 * 创建者      chenghaohao
 * 创建时间     2017/9/14 11:07
 * 包名       com.deemons.dor.receiver
 * 描述       网络状态
 */

public class NetState {

    private boolean isAvailable;

    public boolean isMobileConnect;
    public boolean isAvailableOfMobile;
    public boolean isWifiConnect;
    public boolean isAvailableOfWifi;

    public NetState(boolean isMobileConnect, boolean isAvailableOfMobile, boolean isWifiConnect, boolean isAvailableOfWifi) {
        this.isMobileConnect = isMobileConnect;
        this.isAvailableOfMobile = isAvailableOfMobile;
        this.isWifiConnect = isWifiConnect;
        this.isAvailableOfWifi = isAvailableOfWifi;

        isAvailable = isAvailableOfMobile || isAvailableOfWifi;
    }

    /**
     * 网络是否可用，包含 移动数据 和 WiFi
     * @return
     */
    public boolean isAvailable() {
        return isAvailable;
    }

}
