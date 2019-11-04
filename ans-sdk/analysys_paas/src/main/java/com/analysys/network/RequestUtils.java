package com.analysys.network;

import android.content.Context;
import android.text.TextUtils;

import com.analysys.utils.ANSLog;
import com.analysys.utils.CommonUtils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;

/**
 * @Copyright © 2018 EGuan Inc. All rights reserved.
 * @Description: 网络请求
 * @Version: 1.0
 * @Create: 2018/2/3 17:31
 * @Author: Wang-X-C
 */
class RequestUtils {
    /**
     * HTTP
     */
    static String postRequest(String url, String value, String spv,
                              Map<String, String> headInfo) {
        String response = null;
        InputStream is = null;
        ByteArrayOutputStream bos = null;
        PrintWriter pw;
        try {
            response = "";
            URL urlP;
            HttpURLConnection connection;
            byte[] buffer = new byte[1024];
            urlP = new URL(url);
            connection = (HttpURLConnection) urlP.openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setConnectTimeout(20 * 1000);
            connection.setReadTimeout(20 * 1000);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("spv", spv);
            if (headInfo != null) {
                Set<String> keys = headInfo.keySet();
                for (String key : keys) {
                    connection.setRequestProperty(key, headInfo.get(key));
                }
            }
            pw = new PrintWriter(connection.getOutputStream());
            pw.print(value);
            pw.flush();
            pw.close();
            //获取数据
            if (HttpURLConnection.HTTP_OK == connection.getResponseCode()) {
                CommonUtils.timeCalibration(connection.getDate());
                is = connection.getInputStream();
                bos = new ByteArrayOutputStream();
                int len;
                while (-1 != (len = is.read(buffer))) {
                    bos.write(buffer, 0, len);
                }
                bos.flush();
                return bos.toString("utf-8");
            } else if (HttpURLConnection.HTTP_ENTITY_TOO_LARGE == connection.getResponseCode()) {
                response = "413";
            }
        } catch (Throwable e) {
            ANSLog.e(e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Throwable e) {

                }
            }
            if (bos != null) {
                try {
                    bos.close();
                } catch (Throwable e) {
                }
            }
        }
        return response;
    }

    /**
     * HTTPS
     */
    static String postRequestHttps
    (Context context, String path, String requestData,
     String spv, Map<String, String> headInfo) {
        HttpsURLConnection connection = null;
        OutputStream outputStream = null;
        try {
            URL url = new URL(path);
            connection = (HttpsURLConnection) url.openConnection();
            connection.setSSLSocketFactory(CommonUtils.getSSLSocketFactory(context));
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(20 * 1000);
            connection.setReadTimeout(20 * 1000);
            connection.setRequestProperty("spv", spv);
            if (headInfo != null) {
                Set<String> keys = headInfo.keySet();
                for (String key : keys) {
                    connection.setRequestProperty(key, headInfo.get(key));
                }
            }
            connection.connect();

            if (!TextUtils.isEmpty(requestData)) {
                outputStream = connection.getOutputStream();
                outputStream.write(requestData.getBytes("UTF-8"));
            }
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                CommonUtils.timeCalibration(connection.getDate());
                return CommonUtils.readStream(connection.getInputStream());
            } else {
                return null;
            }
        } catch (Throwable e) {
            ANSLog.e(e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (Throwable e) {
                }
            }
        }
        return null;
    }
}