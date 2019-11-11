package com.analysys.basic;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.List;

/**
 * @Copyright © 2018 EGuan Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2018/11/20 15:50
 * @Author: Wang-X-C
 */
public class BasicUtils {

    public static int source = 1;

    /**
     * 获取应用启动来源
     */
    public static String getStartSource(Context context) {
        switch (source) {
            case 1:
                return "icon";
            case 2:
                return "msg";
            case 3:
                return "url";
            default:
                break;
        }
        return "icon";
    }

    public static String getSourceDetail(Context context) {
        Intent intent = ((Activity) context).getIntent();
        if (intent != null) {
            return String.valueOf(intent.getData());
        }
        return null;
    }

    /**
     * IMEI
     */
    public static String getIMEI(Context context) {
        try {
            TelephonyManager tm =
                    (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            Class clazz = tm.getClass();
            Method getImei = clazz.getDeclaredMethod("getImei", int.class);
            Object imei1 = getImei.invoke(tm, 0);
            if (imei1 != null) {
                return String.valueOf(imei1);
            }
        } catch (Throwable e) {
        }
        return null;
    }

    /**
     * IMEI2
     */
    public static String getIMEI2(Context context) {
        try {
            TelephonyManager tm =
                    (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            Class clazz = tm.getClass();
            Method getImei = clazz.getDeclaredMethod("getImei", int.class);
            Object imei2 = getImei.invoke(tm, 1);
            if (imei2 != null) {
                return String.valueOf(imei2);
            }
        } catch (Throwable e) {
        }
        return null;
    }

    /**
     * IMSI
     */
    public static String getIMSI1(Context context) {
        TelephonyManager telephony =
                (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        Class<?> telephonyClass;
        try {
            telephonyClass = Class.forName(telephony.getClass().getName());
            Method m2 = telephonyClass.getDeclaredMethod("getSubscriberId", new Class[]{int.class});
            Object imsi1 = m2.invoke(telephony, 0);
            if (imsi1 != null) {
                return String.valueOf(imsi1);
            }
        } catch (Throwable e) {
        }
        return null;
    }

    /**
     * IMSI
     */
    public static String getIMSI2(Context context) {
        TelephonyManager telephony =
                (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        Class<?> telephonyClass;
        try {
            telephonyClass = Class.forName(telephony.getClass().getName());
            Method m2 = telephonyClass.getDeclaredMethod("getSubscriberId", new Class[]{int.class});
            Object imsi2 = m2.invoke(telephony, 1);
            if (imsi2 != null) {
                return String.valueOf(imsi2);
            }
        } catch (Throwable e) {
        }
        return null;
    }

    /**
     * CpuModel
     */
    public static String getCPUModel(Context context) {
        String cpuInfo = "/proc/cpuinfo";
        String info = "", module = "";
        try {
            FileReader fr = new FileReader(cpuInfo);
            BufferedReader localBufferedReader = new BufferedReader(fr);
            while ((info = localBufferedReader.readLine()) != null) {
                if (info.contains("Hardware")) {
                    module = info.split(":")[1];
                }
            }
            localBufferedReader.close();
            return module;
        } catch (Throwable e) {
        }
        return null;
    }

    /**
     * Application binary interface
     */
    public static String getCPUAbi(Context context) {

        return android.os.Build.CPU_ABI;
    }

    /**
     * 获取手机CPU序列号
     * cpu序列号(16位) 读取失败为"0000000000000000"
     */
    public static String getCPUSerial(Context context) {
        String cpuInfo = "", cpu = "", cpuSerialNum = "";
        try {
            //读取CPU信息
            Process pp = Runtime.getRuntime().exec("cat/proc/cpuinfo");
            InputStreamReader ir = new InputStreamReader(pp.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);
            //查找CPU序列号
            for (int i = 1; i < 100; i++) {
                cpuInfo = input.readLine();
                if (cpuInfo != null) {
                    //查找到序列号所在行
                    if (cpuInfo.indexOf("Serial") > -1) {
                        //提取序列号
                        cpu = cpuInfo.substring(cpuInfo.indexOf(":") + 1, cpuInfo.length());
                        //去空格
                        cpuSerialNum = cpu.trim();
                        break;
                    }
                } else {
                    break;
                }
                return cpuSerialNum;
            }
        } catch (Throwable e) {
        }
        return null;
    }

    /**
     * 屏幕密度
     */
    public static String getScreenDensity(Context context) {
        try {
            DisplayMetrics dm = context.getResources().getDisplayMetrics();
            return String.valueOf(dm.densityDpi);
        } catch (Throwable e) {
        }
        return null;
    }

    /**
     * 获取经纬度信息
     */
    public static String getLocation(Context context) {

        try {
            String locationProvider = null;
            LocationManager locationManager =
                    (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            List<String> providers = locationManager.getProviders(true);
            if (providers.contains(LocationManager.NETWORK_PROVIDER)) {
                locationProvider = LocationManager.NETWORK_PROVIDER;
            } else if (providers.contains(LocationManager.GPS_PROVIDER)) {
                //如果是GPS定位
                locationProvider = LocationManager.GPS_PROVIDER;
            } else {
                return null;
            }
            if (!checkPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                    && !checkPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                return null;
            }
            Location location = locationManager.getLastKnownLocation(locationProvider);
            if (location != null) {
                DecimalFormat df = new DecimalFormat("#.000");
                return df.format(location.getLongitude()) + "-" + df.format(location.getLatitude());
            }
        } catch (Throwable e) {
        }
        return null;
    }

    private static boolean checkPermission(Context context, String permission) {
        boolean result = false;
        if (Build.VERSION.SDK_INT >= 23) {
            try {
                Class<?> clazz = Class.forName("android.content.Context");
                Method method = clazz.getMethod("checkSelfPermission", String.class);
                int rest = (Integer) method.invoke(context, permission);
                result = rest == PackageManager.PERMISSION_GRANTED;
            } catch (Throwable e) {
                result = false;
            }
        } else {
            PackageManager pm = context.getPackageManager();
            if (pm.checkPermission(permission, context.getPackageName()) == PackageManager
                    .PERMISSION_GRANTED) {
                result = true;
            }
        }
        return result;
    }
}
