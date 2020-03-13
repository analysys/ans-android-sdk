package com.analysys.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import com.analysys.database.EventTableMetaData;

/**
 * @Copyright © 2019 EGuan Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2019/3/27 14:32
 * @Author: Wang-X-C
 */
public class SharedUtil {

    public static boolean getBoolean(Context context, String key, boolean defValue) {
        boolean values = defValue;
        try {
            String strValues = getString(context,key,"","boolean");

            if(!TextUtils.isEmpty(strValues)){
                values =  Boolean.parseBoolean(strValues);
            }
        } catch (Throwable ignore) {
            ExceptionUtil.exceptionThrow(ignore);
        }
        return values;
    }

    public static float getFloat(Context context, String key, float defValue) {
        float values = defValue;
        try {
            String strValues = getString(context, key, "","float");
            if (!TextUtils.isEmpty(strValues)) {
                values = CommonUtils.parseFloat(strValues, defValue);
            }
        } catch (Throwable ignore) {
            ExceptionUtil.exceptionThrow(ignore);
        }

        return values;
    }

    public static int getInt(Context context, String key, int defValue) {
        int values = defValue;

        try {
            String strValues = getString(context, key, "","int");
            if (!TextUtils.isEmpty(strValues)) {
                values = CommonUtils.parseInt(strValues, defValue);
            }
        } catch (Throwable ignore){
            ExceptionUtil.exceptionThrow(ignore);
        }

        return values;
    }

    public static long getLong(Context context, String key, long defValue) {
        long values = defValue;
        try {
            String strValues = getString(context, key, "","long");
            if (!TextUtils.isEmpty(strValues)) {
                values = CommonUtils.parseLong(strValues, defValue);
            }
        }catch (Throwable ignore) {
            ExceptionUtil.exceptionThrow(ignore);
        }
        return values;
    }

    public static String getString(Context context, String key, String defValue) {
        return getString(context, key, defValue, "string");
    }

    private static String getString(Context context, String key, String defValue,String type) {
        String values = null;
        Cursor cursor = null;
        try {
            Uri uri = EventTableMetaData.getTABLE_SP(context);
            cursor = context.getContentResolver().query(uri, new String[]{key,type}, defValue, null, null);
            if (cursor != null && cursor.getCount() >= 1) {
                if (cursor.moveToPosition(0)) {
                    values = cursor.getString(0);
                }
            }
        } catch (Throwable ignore) {
            ExceptionUtil.exceptionThrow(ignore);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        if (values == null) {
            values = defValue;
        }

        return values;
    }

    public static void setBoolean(Context context, String key, boolean value) {

        try {
            Uri uri = EventTableMetaData.getTABLE_SP(context);
            ContentValues contentValues = new ContentValues();
            contentValues.put("key", key);
            contentValues.put("values", value);
            contentValues.put("type","boolean");

            context.getContentResolver().insert(uri, contentValues);
        } catch (Throwable ignore) {
            ExceptionUtil.exceptionThrow(ignore);
        }
    }

    public static void setFloat(Context context, String key, float value) {
        try {
            Uri uri = EventTableMetaData.getTABLE_SP(context);
            ContentValues contentValues = new ContentValues();
            contentValues.put("key", key);
            contentValues.put("values", value);
            contentValues.put("type","float");

            context.getContentResolver().insert(uri, contentValues);
        } catch (Throwable ignore) {
            ExceptionUtil.exceptionThrow(ignore);
        }
    }

    public static void setInt(Context context, String key, int value) {
        Uri uri = null;
        ContentValues contentValues = null;
        try {
            uri = EventTableMetaData.getTABLE_SP(context);
            contentValues = new ContentValues();
            contentValues.put("key", key);
            contentValues.put("values", value);
            contentValues.put("type","int");

            context.getContentResolver().insert(uri, contentValues);
        } catch (Throwable ignore) {
            if (ignore != null && ignore.getMessage().contains("IllegalArgumentException")) {
                try {
                    if (uri != null && contentValues != null) {
                        context.getContentResolver().insert(uri, contentValues);
                    }
                } catch (Throwable ignore1) {
                    ExceptionUtil.exceptionThrow(ignore1);
                }

            } else {
                ExceptionUtil.exceptionThrow(ignore);
            }

        }

    }

    public static void setLong(Context context, String key, long value) {
        try {
            Uri uri = EventTableMetaData.getTABLE_SP(context);
            ContentValues contentValues = new ContentValues();
            contentValues.put("key", key);
            contentValues.put("values", value);
            contentValues.put("type","long");

            context.getContentResolver().insert(uri, contentValues);
        } catch (Throwable ignore) {
            ExceptionUtil.exceptionThrow(ignore);
        }

    }

    public static void setString(Context context, String key, String value) {

        try {
            Uri uri = EventTableMetaData.getTABLE_SP(context);
            ContentValues contentValues = new ContentValues();
            contentValues.put("key", key);
            contentValues.put("values", value);
            contentValues.put("type","string");

            context.getContentResolver().insert(uri, contentValues);
        } catch (Throwable ignore) {
            ExceptionUtil.exceptionThrow(ignore);
        }

    }

    public static void remove(Context context, String key) {

        try{
            Uri uri = EventTableMetaData.getTABLE_SP(context);
            ContentValues contentValues = new ContentValues();
            contentValues.put("key",key);

            context.getContentResolver().update(uri,contentValues,null,null);
        }catch (Throwable ignore){
            ExceptionUtil.exceptionThrow(ignore);
        }
    }

}
