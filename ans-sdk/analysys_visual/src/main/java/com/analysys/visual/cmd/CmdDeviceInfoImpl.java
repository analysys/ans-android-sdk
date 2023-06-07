package com.analysys.visual.cmd;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.JsonWriter;

import com.analysys.utils.ANSLog;
import com.analysys.utils.AnalysysUtil;
import com.analysys.utils.CommonUtils;
import com.analysys.utils.ExceptionUtil;
import com.analysys.utils.InternalAgent;
import com.analysys.visual.VisualManager;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CmdDeviceInfoImpl implements ICmdHandler {

    private static final String TAG = VisualManager.TAG;

    @Override
    public void handleCmd(Object cmd, OutputStream out) {
        DisplayMetrics dm = Resources.getSystem().getDisplayMetrics();
        Map<String, String> deviceInfo = getDeviceInfo(AnalysysUtil.getContext());

        final JsonWriter j = new JsonWriter(new OutputStreamWriter(out));

        try {
            j.beginObject();
            j.name("type").value("device_info_response");
            j.name("payload").beginObject();
            j.name("device_type").value("Android");
            j.name("device_name").value(Build.BRAND + "/" + Build.MODEL);
            j.name("scaled_density").value(dm.scaledDensity);
            j.name("width").value(dm.widthPixels);
            j.name("height").value(dm.heightPixels);
            for (final Map.Entry<String, String> entry : deviceInfo.entrySet()) {
                j.name(entry.getKey()).value(entry.getValue());
            }
            j.endObject(); // payload
            j.endObject();
            ANSLog.i(TAG, "Send ws command: device_info_response");
        } catch (Throwable e) {
            ANSLog.e(TAG, "send device_info to server fail", e);
        } finally {
            try {
                j.close();
            } catch (final IOException e) {
                ANSLog.e(TAG, "close websocket writer fail", e);
            }
        }
    }

    /**
     * 获取 设备唯一ID 优先级: IMEI AndroidID SerialNumber UUID（自动生成）
     */
    static String ID_KEY = "visual_uuid";
    private static Map<String, String> mDeviceInfo;

    //TODO 合并footstone后替换为utils方法
    private Map<String, String> getDeviceInfo(Context context) {
        if (mDeviceInfo == null) {
            final Map<String, String> deviceInfo = new HashMap<>();
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
            } catch (Throwable ignore) {
                ExceptionUtil.exceptionThrow(ignore);
            }
            mDeviceInfo = Collections.unmodifiableMap(deviceInfo);
        }
        return mDeviceInfo;
    }

    private String getDeviceID(Context context) {

        String id = InternalAgent.getString(context, ID_KEY, null);
        if (!InternalAgent.isEmpty(id)) {
            return id;
        } else {
            // 通过IMEI获取ID
            id = CommonUtils.getIMEI(context);
            if (!InternalAgent.isEmpty(id)) {
                InternalAgent.setString(context, ID_KEY, id);
                return id;
            }
            // 通过AndroidID获取ID
            id = CommonUtils.getAndroidID(context);
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

    /**
     * 获取 SerialNum
     */
    private String getSerialNum(Context context) {
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
