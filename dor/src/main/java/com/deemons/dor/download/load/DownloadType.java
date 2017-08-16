package com.deemons.dor.download.load;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 创建者      chenghaohao
 * 创建时间     2017/8/16 13:52
 * 包名       com.deemons.dor.download.load
 * 描述
 */

public class DownloadType {

    public static final int NORMAL = 0x200;
    public static final int CONTINUE = 0x400;
    public static final int MULTI_THREAD = 0x600;
    public static final int ALREADY = 0x800;


    @IntDef({NORMAL,CONTINUE,MULTI_THREAD,ALREADY})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {

    }

}
