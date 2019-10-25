package com.analysys.compatibilitydemo.utils;

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
    private static SharedPreferences.Editor editor = null;
    private static SharedPreferences preferences = null;

    private static SharedPreferences getPreferences(Context context) {
        if (context != null) {
            context = context.getApplicationContext();
        }
        if (preferences == null && context != null) {
            preferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
            return preferences;
        } else {
            return preferences;
        }
    }

    private static SharedPreferences.Editor getEditor(Context context) {
        if (editor == null) {
            editor = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE).edit();
            return editor;
        } else {
            return editor;
        }
    }

    public static boolean getBoolean(Context context, String key, boolean defValue) {
        if (context != null) {
            return getPreferences(context).getBoolean(key, defValue);
        }
        return defValue;
    }

    public static float getFloat(Context context, String key, float defValue) {
        if (context != null) {
            return getPreferences(context).getFloat(key, defValue);
        }
        return defValue;
    }

    public static int getInt(Context context, String key, int defValue) {
        if (context != null) {
            return getPreferences(context).getInt(key, defValue);
        }
        return defValue;
    }

    public static long getLong(Context context, String key, long defValue) {
        if (context != null) {
            return getPreferences(context).getLong(key, defValue);
        }
        return defValue;
    }

    public static String getString(Context context, String key, String defValue) {
        if (context != null) {
            return getPreferences(context).getString(key, defValue);
        }
        return defValue;
    }

    public static void setBoolean(Context context, String key, boolean value) {
        if (context != null) {
            getEditor(context).putBoolean(key, value).commit();
        }
    }

    public static void setFloat(Context context, String key, float value) {
        if (context != null) {
            getEditor(context).putFloat(key, value).commit();
        }
    }

    public static void setInt(Context context, String key, int value) {
        if (context != null) {
            getEditor(context).putInt(key, value).commit();
        }
    }

    public static void setLong(Context context, String key, long value) {
        if (context != null) {
            getEditor(context).putLong(key, value).commit();
        }
    }

    public static void setString(Context context, String key, String value) {
        if (context != null) {
            getEditor(context).putString(key, value).commit();
        }
    }

    public static void remove(Context context, String key) {
        if (context != null) {
            getEditor(context).remove(key).commit();
        }
    }
}
