package com.sbv.linkdroid;

import android.util.Log;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class Utils {
    private static final String TAG = "Utils";

    private static TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[]{};
                }
            }
    };

    private static HostnameVerifier insecureHostnameVerifier = new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };

    public static SSLSocketFactory createInsecureSslSocketFactory() {
        try {
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new SecureRandom());
            return sslContext.getSocketFactory();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public static X509TrustManager getInsecureTrustManager() {
        return (X509TrustManager) trustAllCerts[0];
    }

    public static HostnameVerifier getInsecureHostnameVerifier() {
        return insecureHostnameVerifier;
    }
}
