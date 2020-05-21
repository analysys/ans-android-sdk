package com.analysys.visual;

import android.content.Context;
import android.text.TextUtils;

import com.analysys.utils.InternalAgent;
import com.analysys.visual.utils.Constants;

/**
 * @Copyright © 2018 EGuan Inc. All rights reserved.
 * @Description: 可视化埋点协议请求配置类
 * @Version: 1.0
 * @Create: 2018/4/15 下午4:30
 * @Author: chris
 */
public class StrategyGet {

    private final static String PARA_KEY = "appKey";
    private final static String PARA_VERSION = "appVersion";
    private final static String PARA_PLATFORM = "lib";
    private final static String PLATFORM_ANDROID = "Android";
    private volatile Context mContext;

    private StrategyGet() {
    }

    private static class Holder {
        public static final StrategyGet INSTANCE = new StrategyGet();
    }

    public static StrategyGet getInstance(Context context) {
        if (Holder.INSTANCE.mContext == null && context != null) {
            Holder.INSTANCE.mContext = context.getApplicationContext();
        }
        return Holder.INSTANCE;
    }

    /**
     * http请求是否有可视化埋点协议,并应用
     */
    public void getVisualBindingConfig() {
        try {
            final boolean hasNetwork = InternalAgent.isNetworkAvailable(mContext);
            if (hasNetwork) {
                String userConfigUrl = getVisualConfigUrl();
                if (TextUtils.isEmpty(userConfigUrl)) {
                    return;
                }
                final String url = userConfigUrl + getParams();
//                InternalAgent.getNetExecutor().submit(new StrategyTask(mContext, url));
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        new StrategyTask(mContext, url).run();
                    }
                }).start();

            } else {
                InternalAgent.d("当前网络不可用");
            }
        } catch (Throwable ignore) {
            InternalAgent.e(ignore);
        }
    }

    /**
     * 读取本地可视化配置URL
     */
    private String getVisualConfigUrl() {
        String url = InternalAgent.getString(mContext, Constants.SP_GET_STRATEGY_URL, "");
        if (!TextUtils.isEmpty(url)) {
            return url;
        }
        return "";
    }

    private String getParams() {
        try {
            StringBuilder sb = new StringBuilder("?");
            sb.append(PARA_KEY).append("=").append(InternalAgent.getAppId(mContext)).append("&");
            sb.append(PARA_VERSION).append("=").append(InternalAgent.getVersionName(mContext)).append("&");
            sb.append(PARA_PLATFORM).append("=").append(PLATFORM_ANDROID);
            return sb.toString();
        } catch (Throwable ignore) {
            InternalAgent.e(ignore);
            return "";
        }
    }
}
