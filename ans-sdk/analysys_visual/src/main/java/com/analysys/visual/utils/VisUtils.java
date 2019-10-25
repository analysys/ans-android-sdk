package com.analysys.visual.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Base64;

import com.analysys.utils.InternalAgent;

import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @Copyright © 2018 EGuan Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2018/12/21 15:24
 * @Author: Wang-X-C
 */
public class VisUtils {

    /**
     * 获取 设备唯一ID 优先级: IMEI AndroidID SerialNumber UUID（自动生成）
     *
     * @return 设备唯一ID
     */
    static String ID_KEY = "visual_uuid";
    private static Map<String, String> mDeviceInfo;

    public static Map<String, String> getDeviceInfo(Context context) {
        if (mDeviceInfo == null) {
            final Map<String, String> deviceInfo = new HashMap<String, String>();
            deviceInfo.put("$android_lib_version", InternalAgent.DEV_SDK_VERSION_NAME);
            deviceInfo.put("$android_os", "Android");
            deviceInfo.put("$android_os_version", Build.VERSION.RELEASE == null ? "UNKNOWN" :
                    Build.VERSION.RELEASE);
            deviceInfo.put("$android_manufacturer", Build.MANUFACTURER == null ? "UNKNOWN" :
                    Build.MANUFACTURER);
            deviceInfo.put("$android_brand", Build.BRAND == null ? "UNKNOWN" : Build.BRAND);
            deviceInfo.put("$android_model", Build.MODEL == null ? "UNKNOWN" : Build.MODEL);
            deviceInfo.put("$device_id", getDeviceID(context));
            try {
                final PackageManager manager = context.getPackageManager();
                final PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
                deviceInfo.put("$android_app_version", info.versionName);
                deviceInfo.put("$android_app_version_code", Integer.toString(info.versionCode));
            } catch (final PackageManager.NameNotFoundException e) {
            }
            mDeviceInfo = Collections.unmodifiableMap(deviceInfo);
        }
        return mDeviceInfo;
    }

    public static String getDeviceID(Context context) {

        String id = InternalAgent.getString(context, ID_KEY, null);
        if (!InternalAgent.isEmpty(id)) {
            return id;
        } else {
            // 通过IMEI获取ID
            id = getIMEI(context);
            if (!InternalAgent.isEmpty(id)) {
                InternalAgent.setString(context, ID_KEY, id);
                return id;
            }
            // 通过AndroidID获取ID
            id = getAndroidID(context);
            if (!InternalAgent.isEmpty(id)) {
                InternalAgent.setString(context, ID_KEY, id);
                return id;
            }
            id = getSerialNum(context);
            if (!InternalAgent.isEmpty(id)) {
                InternalAgent.setString(context, ID_KEY, id);
                return id;
            }
            id = generateID(context);
            if (!InternalAgent.isEmpty(id)) {
                InternalAgent.setString(context, ID_KEY, id);
                return id;
            }
            return id;
        }
    }

    private static String getAndroidID(Context context) {
        String id = "";
        try {
            id = Settings.System.getString(context.getContentResolver(),
                    Settings.Secure.ANDROID_ID);
        } catch (Throwable e) {
        }
        return id;
    }

    /**
     * 获取 SerialNum
     */
    private static String getSerialNum(Context context) {
        if (Build.VERSION.SDK_INT < 26) {
            return Build.SERIAL;
        } else {
            if (InternalAgent.checkPermission(context, Manifest.permission.READ_PHONE_STATE)) {
                // 反射获取
                try {
                    Class<?> forName = Class.forName("android.os.Build");
                    Method getSerialMethod = forName.getMethod("getSerial");
                    String result = (String) getSerialMethod.invoke(forName.newInstance());
                    return result;
                } catch (Throwable e) {
                }
            }
            return null;
        }
    }

    /**
     * 获取 IMEI
     */
    public static String getIMEI(Context context) {
        String imei = "";
        try {
            if (!InternalAgent.checkPermission(context, Manifest.permission.READ_PHONE_STATE)) {
                return imei;
            }
            TelephonyManager telephonyMgr =
                    (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            imei = telephonyMgr.getDeviceId();
            return imei;
        } catch (Throwable e) {
            return imei;
        }
    }

    /**
     * 生成规则: appkey+时间截,取MD5,Base64编码前10位,求值。
     */
    private static String generateID(Context context) {
        String key = InternalAgent.getAppId(context);
        String timeStamp = String.valueOf(System.currentTimeMillis());
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] digest1 = digest.digest(new String(key + timeStamp).getBytes());
            byte[] encode = Base64.encode(digest1, 0, 10, Base64.DEFAULT);
            return new String(encode);
        } catch (Throwable e) {
            return String.valueOf(System.currentTimeMillis());
        }
    }
}
