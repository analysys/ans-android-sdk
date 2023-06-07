package com.analysys.visual;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;

import com.analysys.network.NetworkUtils;
import com.analysys.utils.ANSLog;
import com.analysys.utils.CommonUtils;
import com.analysys.utils.ExceptionUtil;
import com.analysys.utils.InternalAgent;
import com.analysys.visual.bind.VisualBindManager;
import com.analysys.visual.utils.Constants;

/**
 * @Copyright © 2018 EGuan Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2018/12/19 15:53
 * @Author: Wang-X-C
 */
public class VisualAgent {

    /**
     * 设置基础
     */
    public static void setVisualBaseURL(Context context, String url) {
//        InternalAgent.setString(context, Constants.SP_DEBUG_VISUAL_URL,
//                Constants.WSS + url + Constants.WSS_PORT + getParams(context));
//        InternalAgent.setString(context, Constants.SP_GET_STRATEGY_URL,
//                com.analysys.utils.Constants.HTTPS + url + Constants.HTTPS_PORT + "/configure");
    }

    /**
     * 设置可视化websocket服务器地址
     */
    public static void setVisitorDebugURL(Context context, String url) {
        try {
            if (!CommonUtils.isMainProcess(context)) {
                return;
            }
            if (VisualManager.getInstance().isStarted()) {
                return;
            }
            String getUrl;
            if (InternalAgent.isEmpty(context) || InternalAgent.isEmpty(url)) {
                throw new IllegalArgumentException();
            }
            if (url.startsWith(Constants.WS)) {
                getUrl = InternalAgent.checkUrl(url);
            } else if (url.startsWith(Constants.WSS)) {
                getUrl = InternalAgent.checkUrl(url);
            } else {
                throw new IllegalArgumentException();
            }
            if (!InternalAgent.isEmpty(getUrl)) {
//                /** 拼接完成url地址，读取配置文件，获取反射路径，反射调用传入地址 */
//                InternalAgent.setString(context, Constants.SP_DEBUG_VISUAL_URL,
//                        getUrl + getParams(context));
                getUrl += getParams(context);
                ANSLog.i(VisualManager.TAG, "Set visual debug url success: " + getUrl);
                VisualManager.getInstance().start(getUrl);
            }
        } catch (Throwable ignore) {
            ANSLog.i(VisualManager.TAG, "Set visual debug url fail: " + (context == null) + ", " + url);
            ExceptionUtil.exceptionPrint(ignore);
        }
    }

    /**
     * 地址拼接
     */
    private static String getParams(Context context) {
        try {
            StringBuilder sb = new StringBuilder("?");
            sb.append(Constants.PARA_KEY)
                    .append("=")
                    .append(InternalAgent.getAppId(context))
                    .append("&");

            sb.append(Constants.PARA_VERSION)
                    .append("=")
                    .append(InternalAgent.getVersionName(context))
                    .append("&");

            sb.append(Constants.PARA_PLATFORM)
                    .append("=")
                    .append(Constants.PLATFORM_ANDROID);

            return sb.toString();
        } catch (Throwable e) {
            return "";
        }
    }

    /**
     * 设置线上请求埋点配置的服务器地址
     */
    public static void setVisitorConfigURL(Context context, String url) {
        try {
            String getUrl;
            if (InternalAgent.isEmpty(context) || InternalAgent.isEmpty(url)) {
                throw new IllegalArgumentException();
            }
            if (url.startsWith("http://")) {
                getUrl = InternalAgent.checkUrl(url);
            } else if (url.startsWith("https://")) {
                getUrl = InternalAgent.checkUrl(url);
            } else {
                throw new IllegalArgumentException();
            }
            if (!InternalAgent.isEmpty(getUrl)) {
                // 先读本地配置，避免网络卡顿导致事件绑定延迟
                VisualBindManager.getInstance().loadConfigFromLocal();
//                /** 拼接成完整的url，读取配置文件，获取反射路径，反射调用传入地址 */
//                InternalAgent.setString(context, Constants.SP_GET_STRATEGY_URL, getUrl +
//                        "/configure");
                if (CommonUtils.isMainProcess(context)) {
                    getUrl += ("/configure" + getParams(context));
                    ANSLog.i(VisualManager.TAG, "Set visual config url success: " + getUrl);
                    if (NetworkUtils.isNetworkAvailable(context)) {
                        loadConfigFromServer(getUrl);
                    } else {
                        ANSLog.i(VisualManager.TAG, "wait network available");
                        waitNetAvailable(context, getUrl);
                    }
                }
            }
        } catch (Throwable ignore) {
            ANSLog.i(VisualManager.TAG, "Set visual config url fail: " + (context == null) + ", " + url);
            ExceptionUtil.exceptionPrint(ignore);
        }
    }

    static class NetReceiver extends BroadcastReceiver {

        private String url;

        NetReceiver(String url) {
            this.url = url;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (NetworkUtils.isNetworkAvailable(context)) {
                    ANSLog.i(VisualManager.TAG, "network available, url: " + url);
                    context.unregisterReceiver(this);
                    loadConfigFromServer(url);
                }
            } catch (Throwable e) {
                ExceptionUtil.exceptionPrint(e);
            }
        }
    }

    ;

    private static void waitNetAvailable(final Context ctx, final String url) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        ctx.registerReceiver(new NetReceiver(url), filter);
    }

    private static void loadConfigFromServer(final String url) {
        // 单独起一个线程，不使用已有队列，避免阻塞
        new Thread(new Runnable() {
            @Override
            public void run() {
                VisualBindManager.getInstance().loadConfigFromServer(url);
            }
        }).start();
    }
}
