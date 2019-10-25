package com.analysys.visual;

import android.content.Context;
import android.text.TextUtils;

import com.analysys.utils.InternalAgent;
import com.analysys.visual.viewcrawler.VisualManager;

import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * @Copyright © 2018 EGuan Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2018/4/25 上午11:58
 * @Author: chris
 */
public class StrategyTask implements Runnable {
    private final static String METHOD_GET = "GET";
    // 连接超时时间 30 秒
    private final static int TIME_OUT_SEC = 30 * 1000;
    final String mUrl;
    Context mContext;

    public StrategyTask(Context context, String url) {
        mUrl = url;
        mContext = context;
    }

    @Override
    public void run() {
        try {

            if (TextUtils.isEmpty(mUrl)) {
                InternalAgent.e("获取埋点配置接口的url为空");
                return; // 应该抛异常
            }
            HttpURLConnection cn = null;
            URL url = new URL(mUrl);
            if (mUrl.startsWith("https")) {
                cn = (HttpsURLConnection) url.openConnection();
                ((HttpsURLConnection) cn).setSSLSocketFactory(InternalAgent.createSSL(mContext));
            } else {
                cn = (HttpURLConnection) url.openConnection();
            }
            cn.setRequestMethod(METHOD_GET);
            cn.setDoInput(true);
            cn.setConnectTimeout(TIME_OUT_SEC);
            cn.connect();
            InputStream is = cn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line = null;
            StringBuffer sb = new StringBuffer();
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();
            String response = sb.toString();
            JSONObject jsonObject = new JSONObject(response);

            int code = jsonObject.optInt("code");
            if (code == 0) {
                VisualManager.getInstance(mContext).applyEventBindingEvents(jsonObject.getJSONArray("data"));
                //成功
                InternalAgent.d("Get visual  config success. visual config URL: " + mUrl);
            } else {
                InternalAgent.d("Get visual  config failed. visual config URL: " + mUrl);
            }
        } catch (ConnectTimeoutException e) {
            InternalAgent.d("Get visual  config failed. visual config URL: " + mUrl);
        } catch (Throwable e) {
            InternalAgent.d("Get visual  config failed. visual config URL: " + mUrl);
        }
    }
}
