package com.deemons.dor.download;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.ResponseBody;

/**
 * 创建者      chenghaohao
 * 创建时间     2017/8/9 17:07
 * 包名       com.deemons.network.utils
 * 描述
 */

public class DownLoadManager {

    private static final String TAG = "DownLoadManager";

    private static final String APK_CONTENT_TYPE = "application/vnd.android.package-archive";

    private static final String PNG_CONTENT_TYPE = "image/png";

    private static final String JPG_CONTENT_TYPE = "image/jpg";

    private static String fileSuffix = "";

    public static boolean writeResponseBodyToDisk(Context context, ResponseBody body) {

        Log.d(TAG, "contentType:>>>>" + body.contentType().toString());

        String type = body.contentType().toString();

        if (type.equals(APK_CONTENT_TYPE)) {

            fileSuffix = ".apk";
        } else if (type.equals(PNG_CONTENT_TYPE)) {
            fileSuffix = ".png";
        } else if (type.equals(JPG_CONTENT_TYPE)) {
            fileSuffix = ".jpg";
        }


        String path = context.getExternalFilesDir(null) + File.separator + System.currentTimeMillis() + fileSuffix;

        Log.d(TAG, "path:>>>>" + path);

        try {
            // todo change the file location/name according to your needs
            File futureStudioIconFile = new File(path);

            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[4096];

                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;

                inputStream = body.byteStream();
                outputStream = new FileOutputStream(futureStudioIconFile);

                while (true) {
                    int read = inputStream.read(fileReader);

                    if (read == -1) {
                        break;
                    }

                    outputStream.write(fileReader, 0, read);

                    fileSizeDownloaded += read;

                    Log.d(TAG, "file download: " + fileSizeDownloaded + " of " + fileSize);
                }

                outputStream.flush();


                return true;
            } catch (IOException e) {
                return false;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            return false;
        }

    }
}
