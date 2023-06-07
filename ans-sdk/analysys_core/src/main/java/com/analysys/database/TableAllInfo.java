package com.analysys.database;

import android.annotation.SuppressLint;
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

import java.util.ArrayList;
import java.util.List;

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
            } catch (Throwable e) {
                ExceptionUtil.exceptionThrow(e);
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
                List<Integer> ids = new ArrayList<>();
                do {
                    String info = cursor.getString(
                            cursor.getColumnIndexOrThrow(DBConfig.TableAllInfo.Column.INFO));
                    int id = cursor.getInt(
                            cursor.getColumnIndexOrThrow(DBConfig.TableAllInfo.Column.ID));
                    ids.add(id);
                    //updateSign(mContext, id);
                    info = CommonUtils.dbDecrypt(info);
                    if (CommonUtils.isEmpty(info)) {
                        continue;
                    }
                    array.put(new JSONObject(info));
                } while (cursor.moveToNext());
                //更新状态为 上传中
                if (!updateAll(mContext, ids)) {
                    return null;
                }
            }
            return array;
        } catch (Throwable e) {
            ExceptionUtil.exceptionThrow(e);
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
        long count = 0L;
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

    /**
     * 删除数据
     */
    public void deleteData() {
        try {
            if (mContext == null) {
                return;
            }

            Uri uri = EventTableMetaData.getTableFZ(mContext);
            mContext.getContentResolver().delete(uri,DBConfig.TableAllInfo.Column.SIGN + "=?",new String[]{String.valueOf(DBConfig.Status.FLAG_UPLOADING)});
        } catch (Throwable e) {
            ExceptionUtil.exceptionThrow(e);
        }
    }

    /**
     * 删除多条数据
     */
    public void delete(int count) {
        try {
            if (mContext == null) {
                return;
            }
            Uri uri = EventTableMetaData.getTableFZ(mContext);
            // DELETE FROM table WHERE id IN(SELECT id FROM table ORDER BY id DESC LIMIT 0,3)
            String where = DBConfig.TableAllInfo.Column.ID +
                    " in(select " + DBConfig.TableAllInfo.Column.ID +
                    " from " + DBConfig.TableAllInfo.TABLE_NAME + " order by "
                    + DBConfig.TableAllInfo.Column.ID +
                    " desc limit 0," + count + ")";
            mContext.getContentResolver().delete(uri, where, null);
        } catch (Throwable e) {
            ExceptionUtil.exceptionThrow(e);
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

        } catch (Throwable e) {
            ExceptionUtil.exceptionThrow(e);
        }
    }




    /**
     * 更新标记
     */
    private boolean updateAll(Context context, List<Integer> ids) {
        if (ids == null || ids.size() == 0) {
            return false;
        }

        String[] strIds = new String[ids.size()];
        //where id in (?,?,?,?,?)
        StringBuilder selection = new StringBuilder(" in (");
        for (int i = 0; i < ids.size(); i++) {
            strIds[i] = String.valueOf(ids.get(i));
            if (i == ids.size() - 1) {
                selection.append("?) ");
            } else {
                selection.append("?,");
            }
        }
        ContentValues values = new ContentValues();
        values.put(DBConfig.TableAllInfo.Column.SIGN, DBConfig.Status.FLAG_UPLOADING);

        Uri uri = EventTableMetaData.getTableFZ(mContext);
        int result = context.getContentResolver().update(uri, values, DBConfig.TableAllInfo.Column.ID + selection.toString(), strIds);
        return result > 0;
    }

    static class Holder {
        @SuppressLint("StaticFieldLeak")
        private static final TableAllInfo INSTANCE = new TableAllInfo();
    }

    private void initContext(Context context) {

        if (CommonUtils.isEmpty(mContext)) {
            if (!CommonUtils.isEmpty(context)) {
                mContext = context;
            }
        }

    }

    /**
     * 查询上传
     */
    public JSONArray selectUpload() {
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
            String sortOrder = DBConfig.TableAllInfo.Column.ID + " asc ";
            cursor = mContext.getContentResolver().query(uri, projection,null,null,sortOrder);

            if (cursor != null && cursor.moveToFirst()) {
                List<Integer> ids = new ArrayList<>();
                do {
                    String info = cursor.getString(
                            cursor.getColumnIndexOrThrow(DBConfig.TableAllInfo.Column.INFO));
                    int id = cursor.getInt(
                            cursor.getColumnIndexOrThrow(DBConfig.TableAllInfo.Column.ID));
                    ids.add(id);
                    //updateSign(mContext, id);
                    info = CommonUtils.dbDecrypt(info);
                    if (CommonUtils.isEmpty(info)) {
                        continue;
                    }
                    array.put(new JSONObject(info));
                } while (cursor.moveToNext());
                //更新状态为 上传中
                if (!updateAll(mContext, ids)) {
                    return null;
                }
            }
            return array;
        } catch (Throwable e) {
            ExceptionUtil.exceptionThrow(e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return array;
    }
}
