package com.analysys.visual;

import android.content.Context;
import android.text.TextUtils;

import com.analysys.utils.InternalAgent;
import com.analysys.visual.utils.Constants;
import com.analysys.visual.viewcrawler.VisualManager;

/**
 * @Copyright © 2018 EGuan Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2018/12/19 15:53
 * @Author: Wang-X-C
 */
public class VisualAgent {

    private static boolean sInited;

    /**
     * 设置基础
     */
    public static void setVisualBaseURL(Context context, String url) {
        InternalAgent.setString(context, Constants.SP_DEBUG_VISUAL_URL,
                Constants.WSS + url + Constants.WSS_PORT + getParams(context));
        InternalAgent.setString(context, Constants.SP_GET_STRATEGY_URL,
                com.analysys.utils.Constants.HTTPS + url + Constants.HTTPS_PORT + "/configure");
        if (!TextUtils.isEmpty(url)) {
            init(context);
        }
    }

    /**
     * 设置可视化websocket服务器地址
     */
    public static void setVisitorDebugURL(Context context, String url) {
        try {
            String getUrl = "";
            if (InternalAgent.isEmpty(context) || InternalAgent.isEmpty(url)) {
                return;
            }
            if (url.startsWith(Constants.WS)) {
                getUrl = InternalAgent.checkUrl(url);
            } else if (url.startsWith(Constants.WSS)) {
                getUrl = InternalAgent.checkUrl(url);
            } else {
                return;
            }
            if (!InternalAgent.isEmpty(getUrl)) {
                /** 拼接完成url地址，读取配置文件，获取反射路径，反射调用传入地址 */
                InternalAgent.setString(context, Constants.SP_DEBUG_VISUAL_URL,
                        getUrl + getParams(context));

                final String configUrl = InternalAgent.getString(context, Constants.SP_GET_STRATEGY_URL, "");
                if(!TextUtils.isEmpty(configUrl)) {
                    init(context);
                }
            }
        } catch (Throwable e) {
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
            String getUrl = "";
            if (InternalAgent.isEmpty(context) || InternalAgent.isEmpty(url)) {
                return;
            }
            if (url.startsWith("http://")) {
                getUrl = InternalAgent.checkUrl(url);
            } else if (url.startsWith("https://")) {
                getUrl = InternalAgent.checkUrl(url);
            } else {
                return;
            }
            if (!InternalAgent.isEmpty(getUrl)) {
                /** 拼接成完整的url，读取配置文件，获取反射路径，反射调用传入地址 */
                InternalAgent.setString(context, Constants.SP_GET_STRATEGY_URL, getUrl +
                        "/configure");

                final String debugUrl = InternalAgent.getString(context, Constants.SP_DEBUG_VISUAL_URL, "");
                if(!TextUtils.isEmpty(debugUrl)) {
                    init(context);
                }
            }
        } catch (Throwable e) {
        }
    }

    /**
     * 初始化可视化
     */
    public static synchronized void init(Context context) {
        if (sInited) {
            return;
        }
        sInited = true;
        initVisual(context);
        StrategyGet.getInstance(context).getVisualBindingConfig();
        InternalAgent.d("Visual init: success.");
    }

    /**
     * 初始化可视化埋点功能 此方法可以自定义调用时机,延时调用,用来优化App初始化相关(放在App初始化完成后调用);
     * 也可以随init接口一起初始化
     *
     * TODO:优化方式可在init接口增加'是否延时加载可视化'参数,确定initVisual方法是否延时调用
     * hit:方法为内部方法,调用前，请确保已经调用init方法
     */
    private static void initVisual(final Context context) {
        final String url = InternalAgent.getString(context, "url", Constants.SP_DEBUG_VISUAL_URL);
        if (!TextUtils.isEmpty(url)) {
            VisualManager.getInstance(context);
        }
    }
}
