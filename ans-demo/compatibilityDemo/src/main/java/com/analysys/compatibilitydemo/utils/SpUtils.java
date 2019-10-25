//package com.analysys.compatibilitydemo.utils;
//
//import android.content.Context;
//import android.content.SharedPreferences;
//import android.os.Build;
//
///**
// * @Copyright © 2018 EGuan Inc. All rights reserved.
// * @Description: shared 使用
// * @Version: 1.0
// * @Create: 2018/2/3
// * @Author: WXC
// */
//
//public class SpUtils {
//
//    private static final String FILE_NAME = "fz.d";
//    private static Context mContext = null;
//
//    /**
//     * 存储数据到SharedPreferences
//     *
//     * @param context
//     * @param key
//     * @param object
//     */
//    public static void setParam(Context context, String key, Object object) {
//
//        try {
//            if (context == null) {
//                if (mContext != null) {
//                    context = mContext;
//                } else {
//                    return;
//                }
//            }
//            String type = object.getClass().getSimpleName();
//            SharedPreferences sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
//            SharedPreferences.Editor editor = sp.edit();
//            if ("String".equals(type)) {
//                editor.putString(key, (String) object);
//            } else if ("Integer".equals(type)) {
//                editor.putInt(key, Integer.valueOf(object + ""));
//            } else if ("Boolean".equals(type)) {
//                editor.putBoolean(key, Boolean.valueOf(object + ""));
//            } else if ("Float".equals(type)) {
//                editor.putFloat(key, Float.valueOf(object + ""));
//            } else if ("Long".equals(type)) {
//                editor.putLong(key, Long.valueOf(object + ""));
//            }
//            if (Build.VERSION.SDK_INT > 8) {
//                editor.apply();
//            } else {
//                editor.commit();
//            }
//        } catch (Throwable e) {
//
//        }
//    }
//
//    /**
//     * 从SharedPreferences中读取数据
//     *
//     * @param context
//     * @param key
//     * @param defaultObject
//     * @return
//     */
//    public static Object getParam(Context context, String key, Object defaultObject) {
//        try {
//            if (context == null) {
//                if (mContext != null) {
//                    context = mContext;
//                } else {
//                    return defaultObject;
//                }
//            }
//            String type = defaultObject.getClass().getSimpleName();
//            SharedPreferences sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
//            if (defaultObject == null) {
//                return null;
//            }
//            if ("String".equals(type)) {
//                return sp.getString(key, defaultObject.toString());
//            } else if ("Integer".equals(type)) {
//                return sp.getInt(key, Integer.valueOf(defaultObject.toString()));
//            } else if ("Boolean".equals(type)) {
//                return sp.getBoolean(key, Boolean.valueOf(defaultObject.toString()));
//            } else if ("Float".equals(type)) {
//                return sp.getFloat(key, Float.valueOf(defaultObject.toString()));
//            } else if ("Long".equals(type)) {
//                return sp.getLong(key, Long.valueOf(defaultObject.toString()));
//            }
//
//        } catch (Throwable e) {
//
//        }
//        return defaultObject;
//    }
//}
