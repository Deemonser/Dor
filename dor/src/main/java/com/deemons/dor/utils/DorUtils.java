package com.deemons.dor.utils;

import android.content.Context;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

/**
 * 创建者      chenghaohao
 * 创建时间     2017/8/18 15:32
 * 包名       com.deemons.dor.utils
 * 描述
 */

public class DorUtils {

    private static SSLSocketFactory getSSLSocketFactory(Context context, String... fileNames) {

        CertificateFactory certificateFactory;
        try {
            certificateFactory = CertificateFactory.getInstance("X.509");
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);

            for (int i = 0; i < fileNames.length; i++) {
                String fileName = fileNames[i];
                InputStream inputStream = context.getApplicationContext().getAssets().open(fileName);
                keyStore.setCertificateEntry(fileName, certificateFactory.generateCertificate(inputStream));

                if (inputStream != null) {
                    inputStream.close();
                }
            }

            SSLContext sslContext = SSLContext.getInstance("TLS");
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);
            sslContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());
            return sslContext.getSocketFactory();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


}
