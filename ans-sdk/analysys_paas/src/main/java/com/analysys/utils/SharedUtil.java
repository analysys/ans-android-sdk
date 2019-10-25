package com.analysys.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * @Copyright © 2019 EGuan Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2019/3/27 14:32
 * @Author: Wang-X-C
 */
public class SharedUtil {

    private static final String FILE_NAME = "fz.d";
    private static SharedPreferences.Editor mEditor = null;
    private static SharedPreferences mPreferences = null;

    public static boolean getBoolean(Context context, String key, boolean defValue) {

        if (initPreferences(context)) {
            return mPreferences.getBoolean(key, defValue);
        }
        return defValue;
    }

    public static float getFloat(Context context, String key, float defValue) {
        if (initPreferences(context)) {
            return mPreferences.getFloat(key, defValue);
        }
        return defValue;
    }

    public static int getInt(Context context, String key, int defValue) {

        if (initPreferences(context)) {
            return mPreferences.getInt(key, defValue);
        }
        return defValue;
    }

    public static long getLong(Context context, String key, long defValue) {
        if (initPreferences(context)) {
            return mPreferences.getLong(key, defValue);
        }
        return defValue;
    }

    public static String getString(Context context, String key, String defValue) {
        if (initPreferences(context)) {
            return mPreferences.getString(key, defValue);
        }
        return defValue;
    }

    public static void setBoolean(Context context, String key, boolean value) {

        if (initEditor(context)) {
            mEditor.putBoolean(key, value).commit();
        }
    }

    public static void setFloat(Context context, String key, float value) {
        if (initEditor(context)) {
            mEditor.putFloat(key, value).commit();
        }
    }

    public static void setInt(Context context, String key, int value) {
        if (initEditor(context)) {
            mEditor.putInt(key, value).commit();
        }
    }

    public static void setLong(Context context, String key, long value) {
        if (initEditor(context)) {
            mEditor.putLong(key, value).commit();
        }
    }

    public static void setString(Context context, String key, String value) {
        if (initEditor(context)) {
            mEditor.putString(key, value).commit();
        }
    }

    public static void remove(Context context, String key) {
        if (initEditor(context)) {
            mEditor.remove(key).commit();
        }
    }

    private static SharedPreferences getPreferences(Context context) {
        if (mPreferences == null && context != null) {
            mPreferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        } else {
            mPreferences = null;
        }
        return mPreferences;
    }

    private static SharedPreferences.Editor getEditor(Context context) {
        if (mEditor == null && context != null) {
            if (mPreferences != null) {
                mEditor = mPreferences.edit();
            } else {
                mPreferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
                if (mPreferences != null) {
                    mEditor = mPreferences.edit();
                } else {
                    mEditor = null;
                }
            }
        } else {
            mEditor = null;
        }
        return mEditor;
    }

    private static boolean initPreferences(Context context) {
        if (mPreferences == null && context != null) {
            getPreferences(context.getApplicationContext());
        }
        return mPreferences != null;
    }

    private static boolean initEditor(Context context) {
        if (mEditor == null && context != null) {
            getEditor(context.getApplicationContext());
        }
        return mEditor != null;
    }

}
