package com.analysys.utils;

import android.content.Context;
import android.os.Build;

import com.analysys.network.NetworkUtils;
import com.analysys.process.AgentProcess;
import com.analysys.process.SessionManage;
import com.analysys.userinfo.UserInfo;

import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;

import javax.net.ssl.SSLSocketFactory;

/**
 * @Copyright © 2019 EGuan Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2019/2/24 11:06
 * @Author: Wang-X-C
 */
public class InternalAgent {
    public static final String DEV_SDK_VERSION_NAME = Constants.DEV_SDK_VERSION;
    public static final String PUSH_EVENT_RECEIVER_MSG = Constants.PUSH_EVENT_RECEIVER_MSG;
    public static final String PUSH_EVENT_CLICK_MSG = Constants.PUSH_EVENT_CLICK_MSG;
    public static final String PUSH_EVENT_PROCESS_SUCCESS = Constants.PUSH_EVENT_PROCESS_SUCCESS;

    public static String uri = null;

    public static String getSourceDetail(Context context) {
        return uri;
    }

    /**
     * 获取 Channel
     */
    public static String getChannel(Context context) {
        return CommonUtils.getChannel(context);
    }

    /**
     * 获取AppKey
     */
    public static String getAppId(Context context) {
        return CommonUtils.getAppKey(context);
    }

    /**
     * 获取distinct id 如果用户没有调用，获取androidId
     */
    public static String getUserId(Context context) {
        return CommonUtils.getUserId(context);
    }

    /**
     * 获取当前时间
     */
    public static Object getCurrentTime(Context context) {
        return System.currentTimeMillis();
    }

    /**
     * 是否进行了时间校准
     */
    public static Object isCalibrated(Context context) {
        return Constants.isCalibration;
    }

    /**
     * 获取时区
     */
    public static String getTimeZone(Context context) {
        return TimeZone.getDefault().getDisplayName(false, TimeZone.SHORT);
    }

    /**
     * 获取生产厂商
     */
    public static String getManufacturer(Context context) {
        return Build.MANUFACTURER;
    }

    /**
     * 获取应用版本名称
     */
    public static String getVersionName(Context context) {
        return CommonUtils.getVersionName(context);
    }

    public static String getDeviceModel(Context context) {
        return Build.MODEL;
    }

    public static String getOSVersion(Context context) {
        return Constants.DEV_SYSTEM + " " + Build.VERSION.RELEASE;
    }

    /**
     * 网路状态
     */
    public static String getNetwork(Context context) {
        return NetworkUtils.networkType(context,true);
    }

    /**
     * 获取当前的运营商
     */
    public static String getCarrierName(Context context) {
        return CommonUtils.getCarrierName(context);
    }

    /**
     * 获取屏幕宽度
     */
    public static Object getScreenWidth(Context context) {
        return CommonUtils.getScreenWidth(context);
    }

    /**
     * 获取 屏幕高度
     * 如果context是Activity获取的是物理的屏幕尺寸 如果不是获取的是Activity的尺寸
     */
    public static Object getScreenHeight(Context context) {

        return CommonUtils.getScreenHeight(context);
    }

    /**
     * 获取品牌
     */
    public static Object getBrand(Context context) {
        return Build.BRAND;
    }

    /**
     * 获取语言
     */
    public static Object getDeviceLanguage(Context context) {
        return Locale.getDefault().getLanguage();
    }

    /**
     * 是否首日访问
     */
    public static Object isFirstDay(Context context) {
        return CommonUtils.isFirstDay(context);
    }

    /**
     * 获取 session id
     */
    public static String getSessionId(Context context) {
        return SessionManage.getInstance(context).getSessionId();
    }

    /**
     * 是否首次启动
     */
    public static Object isFirstTime(Context context) {
        return CommonUtils.isFirstStart(context);
    }

    /**
     * 获取debug状态 服务器设置 > 用户设置 > 默认设置
     */
    public static Object getDebugMode(Context context) {
        return CommonUtils.getDebugMode(context);
    }

    /**
     * 获取 SDK 版本号
     */
    public static String getLibVersion(Context context) {
        return Constants.DEV_SDK_VERSION;
    }

    /**
     * 是否登录
     */
    public static boolean getLogin(Context context) {
        return UserInfo.getLoginState();
    }

    /**
     * 获取 IMEI
     */
    public static String getIMEI(Context context) {
        if (CommonUtils.isAutoCollect(context, Constants.META_DATA_IMEI)) {
            return CommonUtils.getIMEI(context);
        }
        return Constants.EMPTY;
    }

    /**
     * 获取mac地址
     */
    public static Object getMac(Context context) {
        if (CommonUtils.isAutoCollect(context, Constants.META_DATA_MAC)) {
            return CommonUtils.getMac(context);
        }
        return Constants.EMPTY;
    }

    /**
     * 获取device id
     */
    public static String getDeviceId(Context context) {
        if(AgentProcess.getInstance().getConfig().isAutoTrackDeviceId()){
//            return CommonUtils.getDeviceId(context);
            return UserInfo.getDeviceId();
        } else {
            return null;
        }
    }

    /**
     * 获取应用启动来源
     */
    public static String getLaunchSource(Context context) {
        return CommonUtils.getLaunchSource();
    }

//    /**
//     * 获取original id
//     */
//    public static Object getOriginalId(Context context) {
//        return CommonUtils.getOriginalId(context);
//    }

    public static boolean isEmpty(Object object) {
        return CommonUtils.isEmpty(object);
    }

    public static String networkType(Context context) {
        return NetworkUtils.networkType(context,true);
    }

    public static boolean isNetworkAvailable(Context context) {

        return NetworkUtils.isNetworkAvailable(context);
    }

    /** check **/

    public static boolean checkPermission(Context context, String permission) {
        return CommonUtils.checkPermission(context, permission);
    }

    public static boolean checkClass(String pkgName, String className) {
        return CommonUtils.checkClass(pkgName, className);
    }

    public static String checkUrl(String url) {
        return CommonUtils.checkUrl(url);
    }

    public static void checkEventName(Object eventInfo) {
        ParameterCheck.checkEventName(eventInfo);
    }

    public static void checkKey(Object objKey) {
        ParameterCheck.checkKey(objKey);
    }

    public static void checkValue(Object value) {
        ParameterCheck.checkValue(value);
    }

    /** SP **/

    public static void setString(Context context, String key, String defValue) {
        SharedUtil.setString(context, key, defValue);
    }

    public static String getString(Context context, String key, String defValue) {
        return SharedUtil.getString(context, key, defValue);
    }

    public static void setFloat(Context context, String key, float defValue) {
        SharedUtil.setFloat(context, key, defValue);
    }

    public static Float getFloat(Context context, String key, float defValue) {
        return SharedUtil.getFloat(context, key, defValue);
    }

    /** log **/

    public static void i(Object... object) {
        ANSLog.i(object);
    }

    public static void e(Object... object) {
        ANSLog.e(object);
    }

    public static void w(Object... object) {
        ANSLog.w(object);
    }

    public static void d(Object... object) {
        ANSLog.d(object);
    }

    public static void v(Object... object) {
        ANSLog.v(object);
    }

    public static SSLSocketFactory createSSL(Context context) {
        return CommonUtils.getSSLSocketFactory(context);
    }
}
