package com.analysys.utils;

import android.util.Log;

import com.analysys.BuildConfig;

/**
 * @Copyright © 2019 EGuan Inc. All rights reserved.
 * @Description: 异常情况处理
 * @Version: 1.0
 * @Create: 2019/11/29 15:33
 * @Author: WP
 */
public class ExceptionUtil {

    /**
     * 异常情况处理
     * @param e
     */
    public static void exceptionThrow(Throwable e){
        if(BuildConfig.DEBUG){
            throw new RuntimeException(e);
        }
    }

    /**
     * 异常打印
     * @param e
     */
    public static void exceptionPrint(Throwable e) {
        if(BuildConfig.DEBUG) {
            e.printStackTrace();
        }
    }
}