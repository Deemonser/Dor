package com.deemons.dor.download.load;

/**
 * author： deemons
 * date:    2017/8/13
 * desc:
 */

interface ILoadHelper {
    void checkRange();

    void multiThreadLoad();

    void singleThreadLoad();
}
