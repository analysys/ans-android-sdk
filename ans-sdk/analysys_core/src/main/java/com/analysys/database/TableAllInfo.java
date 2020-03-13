package com.analysys.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import com.analysys.utils.CommonUtils;
import com.analysys.utils.Constants;
import com.analysys.utils.ExceptionUtil;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @Copyright © 2018 EGuan Inc. All rights reserved.
 * @Description: 表操作
 * @Version: 1.0
 * @Create: 2018/2/3 17:31
 * @Author: WXC
 */
public class TableAllInfo {
    private Context mContext;

    public static TableAllInfo getInstance(Context context) {
        Holder.INSTANCE.initContext(context);
        return Holder.INSTANCE;
    }

    private TableAllInfo() {
    }

    /**
     * 存储数据
     */
    public void insert(String info, String type) {
        if (CommonUtils.isEmpty(info) || mContext == null) {
            return;
        }
        info = CommonUtils.dbEncrypt(info);
        if (!TextUtils.isEmpty(info)) {
            ContentValues values = new ContentValues();
            values.put(DBConfig.TableAllInfo.Column.INFO, info);
            values.put(DBConfig.TableAllInfo.Column.SIGN, DBConfig.Status.FLAG_SAVE);
            values.put(DBConfig.TableAllInfo.Column.TYPE, type);
            values.put(DBConfig.TableAllInfo.Column.INSERT_DATE, System.currentTimeMillis());


            try {
                Uri uri = EventTableMetaData.getTableFZ(mContext);
                mContext.getContentResolver().insert(uri, values);
            } catch (Throwable ignore) {
                ExceptionUtil.exceptionThrow(ignore);
            }
        }
    }


    /**
     * 查询上传
     */
    public JSONArray select() {
        JSONArray array = new JSONArray();
        Cursor cursor = null;
        try {
            if (CommonUtils.isEmpty(mContext)) {
                return null;
            }

            Uri uri = EventTableMetaData.getTableFZ(mContext);
            String[] projection = new String[]{
                    DBConfig.TableAllInfo.Column.INFO,
                    DBConfig.TableAllInfo.Column.ID,
                    DBConfig.TableAllInfo.Column.TYPE
            };
            String sortOrder = DBConfig.TableAllInfo.Column.ID + " asc " + " LIMIT 0," + Constants.MAX_SEND_COUNT;
            cursor = mContext.getContentResolver().query(uri, projection,null,null,sortOrder);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String info = cursor.getString(
                            cursor.getColumnIndexOrThrow(DBConfig.TableAllInfo.Column.INFO));
                    int id = cursor.getInt(
                            cursor.getColumnIndexOrThrow(DBConfig.TableAllInfo.Column.ID));
                    updateSign(mContext, id);
                    info = CommonUtils.dbDecrypt(info);
                    if (CommonUtils.isEmpty(info)) {
                        continue;
                    }
                    array.put(new JSONObject(info));
                } while (cursor.moveToNext());
            }
            return array;
        } catch (Throwable ignore) {
            ExceptionUtil.exceptionThrow(ignore);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return array;
    }


    /**
     * 查询条数
     */
    public long selectCount() {
        Cursor cursor = null;
        long count = 0l;
        try {
            if (mContext != null) {

                Uri uri = EventTableMetaData.getTableFZ(mContext);
                cursor = mContext.getContentResolver().query(uri,null,null,null,null);
                if (cursor != null) {
                    count = cursor.getCount();
                }
            }

        } catch (Throwable e) {
            ExceptionUtil.exceptionThrow(e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return count;
    }

    /*
     * 删除数据
     */
    public void deleteData() {
        try {
            if (mContext == null) {
                return;
            }

            Uri uri = EventTableMetaData.getTableFZ(mContext);
            mContext.getContentResolver().delete(uri,DBConfig.TableAllInfo.Column.SIGN + "=?",new String[]{String.valueOf(DBConfig.Status.FLAG_UPLOADING)});
        } catch (Throwable ignore) {
            ExceptionUtil.exceptionThrow(ignore);
        }
    }

    /**
     * 删除多条数据
     */
    public void delete(int count) {
        Cursor cursor = null;
        try {
            if (mContext == null) {
                return;
            }

            Uri uri = EventTableMetaData.getTableFZ(mContext);
            String sortOrder = DBConfig.TableAllInfo.Column.ID + " desc "+" LIMIT 0," + count;
            cursor = mContext.getContentResolver().query(uri,new String[]{DBConfig.TableAllInfo.Column.ID},null,null,sortOrder);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(
                            DBConfig.TableAllInfo.Column.ID));

                    mContext.getContentResolver().delete(uri, DBConfig.TableAllInfo.Column.ID + "=?", new String[]{String.valueOf(id)});
                } while (cursor.moveToNext());
            }
        } catch (Throwable e) {
            ExceptionUtil.exceptionThrow(e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * 删除所有数据
     */
    public void deleteAll() {
        try {
            if (mContext == null) {
                return;
            }

            Uri uri = EventTableMetaData.getTableFZ(mContext);
            mContext.getContentResolver().delete(uri,null,null);

        } catch (Throwable ignore) {
            ExceptionUtil.exceptionThrow(ignore);
        }
    }


    /**
     * 更新标记
     *
     * @param context
     * @param id
     */
    private void updateSign(Context context, int id) {
        ContentValues values = new ContentValues();
        values.put(DBConfig.TableAllInfo.Column.SIGN, DBConfig.Status.FLAG_UPLOADING);

        Uri uri = EventTableMetaData.getTableFZ(mContext);
        context.getContentResolver().update(uri, values, DBConfig.TableAllInfo.Column.ID + "=?",
                new String[]{String.valueOf(id)});
    }

    static class Holder {
        private static final TableAllInfo INSTANCE = new TableAllInfo();
    }

    private void initContext(Context context) {

        if (CommonUtils.isEmpty(mContext)) {
            if (!CommonUtils.isEmpty(context)) {
                mContext = context;
            }
        }

    }
}
