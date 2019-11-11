package com.analysys.basic;

import android.content.Context;

/**
 * @Copyright © 2019 EGuan Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2019/4/15 13:13
 * @Author: Wang-X-C
 */
public class BasicAgent {

    /**
     * 获取应用启动来源
     */
    public static String getStartSource(Context context) {

        return BasicUtils.getStartSource(context);
    }

    public static String getSourceDetail(Context context) {

        return BasicUtils.getSourceDetail(context);
    }

    /**
     * IMEI
     */
    public static String getIMEI(Context context) {

        return BasicUtils.getIMEI(context);
    }

    /**
     * IMEI2
     */
    public static String getIMEI2(Context context) {

        return BasicUtils.getIMEI2(context);
    }

    /**
     * IMSI
     */
    public static String getIMSI1(Context context) {
        return BasicUtils.getIMSI1(context);
    }

    /**
     * IMSI
     */
    public static String getIMSI2(Context context) {

        return BasicUtils.getIMSI2(context);
    }

    /**
     * CpuModel
     */
    public static String getCPUModel(Context context) {

        return BasicUtils.getCPUModel(context);
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

        return BasicUtils.getCPUSerial(context);
    }

    /**
     * 屏幕密度
     */
    public static String getScreenDensity(Context context) {

        return BasicUtils.getScreenDensity(context);
    }

    /**
     * 获取经纬度信息
     */
    public static String getLocation(Context context) {

        return BasicUtils.getLocation(context);
    }
}
