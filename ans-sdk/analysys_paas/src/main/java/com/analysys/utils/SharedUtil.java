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

//    private static final String FILE_NAME = "fz.d";
//    private static SharedPreferences.Editor mEditor = null;
//    private static SharedPreferences mPreferences = null;

    public static boolean getBoolean(Context context, String key, boolean defValue) {

        String strValues = getString(context,key,"");
        boolean values = defValue;
        if(!TextUtils.isEmpty(strValues)){
            values =  Boolean.parseBoolean(strValues);
        }

        return values;
    }

    public static float getFloat(Context context, String key, float defValue) {

        float values = defValue;

        String strValues = getString(context,key,"");
        if(strValues!=null&&!strValues.equals("")&&strValues.length()>0){
            values = Float.parseFloat(strValues);
        }

        return values;
    }

    public static int getInt(Context context, String key, int defValue) {

        int values = defValue;

        String strValues = getString(context,key,"");
        if(strValues!=null&&!strValues.equals("")&&strValues.length()>0){
            values = Integer.parseInt(strValues);
        }


        return values;
    }

    public static long getLong(Context context, String key, long defValue) {

        long values = defValue;
        String strValues = getString(context,key,"");
        if(strValues!=null&&!strValues.equals("")&&strValues.length()>0){
            values = Long.parseLong(strValues);
        }

        return values;
    }

    public static String getString(Context context, String key, String defValue) {
        String values = null;
        Cursor cursor = null;
        try {
            Uri uri = EventTableMetaData.getTABLE_SP(context);
            cursor = context.getContentResolver().query(uri, new String[]{key}, null, null, null);
            if (cursor != null && cursor.getCount() >= 1) {
                if (cursor.moveToPosition(0)) {
                    values = cursor.getString(0);
                }

            }

            if (values == null) {
                values = defValue;
            }
        } catch (Exception e) {
            ExceptionUtil.exceptionThrow(e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }


        return values;
    }

    public static void setBoolean(Context context, String key, boolean value) {

//        if (initEditor(context)) {
//            mEditor.putBoolean(key, value).commit();
//        }
        setString(context,key,String.valueOf(value));
    }

    public static void setFloat(Context context, String key, float value) {
//        if (initEditor(context)) {
//            mEditor.putFloat(key, value).commit();
//        }
        setString(context,key,String.valueOf(value));
    }

    public static void setInt(Context context, String key, int value) {
//        if (initEditor(context)) {
//            mEditor.putInt(key, value).commit();
//        }
        setString(context,key,String.valueOf(value));
    }

    public static void setLong(Context context, String key, long value) {
//        if (initEditor(context)) {
//            mEditor.putLong(key, value).commit();
//        }

        setString(context,key,String.valueOf(value));

    }

    public static void setString(Context context, String key, String value) {
//        if (initEditor(context)) {
//            mEditor.putString(key, value).commit();
//        }

        Uri uri = EventTableMetaData.getTABLE_SP(context);
        ContentValues contentValues = new ContentValues();
        contentValues.put("key",key);
        contentValues.put("values",value);

        context.getContentResolver().insert(uri,contentValues);
    }

    public static void remove(Context context, String key) {
//        if (initEditor(context)) {
//            mEditor.remove(key).commit();
//        }

        Uri uri = EventTableMetaData.getTABLE_SP(context);
        ContentValues contentValues = new ContentValues();
        contentValues.put("key",key);

        context.getContentResolver().update(uri,contentValues,null,null);

    }

//    private static SharedPreferences getPreferences(Context context) {
//        if (mPreferences == null && context != null) {
//            mPreferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
//        } else {
//            mPreferences = null;
//        }
//        return mPreferences;
//    }
//
//    private static SharedPreferences.Editor getEditor(Context context) {
//        if (mEditor == null && context != null) {
//            if (mPreferences != null) {
//                mEditor = mPreferences.edit();
//            } else {
//                mPreferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
//                if (mPreferences != null) {
//                    mEditor = mPreferences.edit();
//                } else {
//                    mEditor = null;
//                }
//            }
//        } else {
//            mEditor = null;
//        }
//        return mEditor;
//    }
//
//    private static boolean initPreferences(Context context) {
//        if (mPreferences == null && context != null) {
//            getPreferences(context.getApplicationContext());
//        }
//        return mPreferences != null;
//    }
//
//    private static boolean initEditor(Context context) {
//        if (mEditor == null && context != null) {
//            getEditor(context.getApplicationContext());
//        }
//        return mEditor != null;
//    }

}
