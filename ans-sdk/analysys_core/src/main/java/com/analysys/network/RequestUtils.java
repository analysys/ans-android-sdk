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

    static long getRequest(String httpUrl) {
        HttpURLConnection connection = null;
//        InputStream is = null;
//        BufferedReader br = null;
//        String result = null;
        long date = 0;
        try {
            // 创建远程url连接对象
            URL url = new URL(httpUrl);
            // 通过远程url连接对象打开一个连接，强转成httpURLConnection类
            connection = (HttpURLConnection) url.openConnection();
            // 设置连接方式：get
            connection.setRequestMethod("GET");
            // 设置连接主机服务器的超时时间：20 * 1000毫秒
            connection.setConnectTimeout(20 * 1000);
            // 设置读取远程返回的数据时间：20 * 1000毫秒
            connection.setReadTimeout(20 * 1000);
            // 发送请求
            connection.connect();
//            ANSLog.e("网络请求成功" + connection.getResponseCode());
//            ANSLog.e("get获取时间：" + connection.getDate());
            date = connection.getDate();
            // 通过connection连接，获取输入流
//            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
//                ANSLog.e("网络请求成功");
//                is = connection.getInputStream();
//                 封装输入流is，并指定字符集
//                br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
//                 存放数据
//                StringBuffer sbf = new StringBuffer();
//                String temp = null;
//                while ((temp = br.readLine()) != null) {
//                    sbf.append(temp);
//                    sbf.append("\r\n");
//                }
//                result = sbf.toString();
//            }
        } catch (Exception e) {
        } finally {
            // 关闭远程连接
            if (connection != null) {
                connection.disconnect();
            }
        }
        return date;
    }
}