package com.analysys.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.analysys.utils.CommonUtils;
import com.analysys.utils.Constants;

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

    /**
     * 存储数据
     */
    public synchronized void insert(String info, String type) {
        if (CommonUtils.isEmpty(info)) {
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
                if (CommonUtils.isEmpty(mContext)) {
                    return;
                }
                SQLiteDatabase db = DBManage.getInstance(mContext).openDB();
                if (CommonUtils.isEmpty(db)) {
                    return;
                }
                DBHelper.getInstance(mContext).createTable(DBConfig.TableAllInfo.CREATE_TABLE);
                db.insert(DBConfig.TableAllInfo.TABLE_NAME, null, values);
            } catch (Throwable e) {
            } finally {
                DBManage.getInstance(mContext).closeDB();
                info = null;
            }
        }
    }

    /**
     * 查询上传
     */
    public synchronized JSONArray select() {
        JSONArray array = new JSONArray();
        Cursor cursor = null;
        try {
            if (CommonUtils.isEmpty(mContext)) {
                return null;
            }
            SQLiteDatabase db = DBManage.getInstance(mContext).openDB();
            if (CommonUtils.isEmpty(db)) {
                return new JSONArray();
            }
            DBHelper.getInstance(mContext).createTable(DBConfig.TableAllInfo.CREATE_TABLE);
            cursor = selectCursor(db);
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    String info = cursor.getString(
                            cursor.getColumnIndexOrThrow(DBConfig.TableAllInfo.Column.INFO));
                    int id = cursor.getInt(
                            cursor.getColumnIndexOrThrow(DBConfig.TableAllInfo.Column.ID));
                    updateSign(db, id);
                    info = CommonUtils.dbDecrypt(info);
                    if (CommonUtils.isEmpty(info)) {
                        continue;
                    }
                    array.put(new JSONObject(info));
                }
            }
            return array;
        } catch (Throwable e) {
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            DBManage.getInstance(mContext).closeDB();
        }
        return array;
    }

    /**
     * 查询条数
     */
    public synchronized long selectCount() {
        Cursor cursor = null;
        try {
            if (mContext != null) {
                SQLiteDatabase db = DBManage.getInstance(mContext).openDB();
                if (db != null) {

                    DBHelper.getInstance(mContext).createTable(
                            DBConfig.TableAllInfo.CREATE_TABLE);

                    cursor = db.query(DBConfig.TableAllInfo.TABLE_NAME,
                            null, null, null,
                            null, null, null);

                    if (cursor != null) {
                        return cursor.getCount();
                    }

                }
            }

        } catch (Throwable e) {
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            DBManage.getInstance(mContext).closeDB();
        }
        return 0;
    }

    /**
     * 删除数据
     */
    public synchronized void deleteData() {
        try {
            if (mContext == null) {
                return;
            }
            SQLiteDatabase db = DBManage.getInstance(mContext).openDB();
            if (db == null) {
                return;
            }
            DBHelper.getInstance(mContext).createTable(DBConfig.TableAllInfo.CREATE_TABLE);
            db.delete(
                    DBConfig.TableAllInfo.TABLE_NAME,
                    DBConfig.TableAllInfo.Column.SIGN + "=?",
                    new String[]{String.valueOf(DBConfig.Status.FLAG_UPLOADING)});
        } catch (Throwable e) {
        } finally {
            DBManage.getInstance(mContext).closeDB();
        }
    }

    /**
     * 删除多条数据
     */
    public synchronized void delete(int count) {
        Cursor cursor = null;
        try {
            if (mContext == null) {
                return;
            }
            SQLiteDatabase db = DBManage.getInstance(mContext).openDB();
            if (db == null) {
                return;
            }
            DBHelper.getInstance(mContext).createTable(DBConfig.TableAllInfo.CREATE_TABLE);

            cursor = db.query(DBConfig.TableAllInfo.TABLE_NAME,
                    new String[]{DBConfig.TableAllInfo.Column.ID},
                    null, null, null, null
                    , DBConfig.TableAllInfo.Column.ID + " desc", "0," + count);

            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {

                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(
                            DBConfig.TableAllInfo.Column.ID));

                    db.delete(DBConfig.TableAllInfo.TABLE_NAME,
                            DBConfig.TableAllInfo.Column.ID + "=?",
                            new String[]{String.valueOf(id)});
                }
            }

        } catch (Throwable e) {
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            DBManage.getInstance(mContext).closeDB();
        }
    }

    /**
     * 删除所有数据
     */
    public synchronized void deleteAll() {
        try {
            if (mContext == null) {
                return;
            }
            SQLiteDatabase db = DBManage.getInstance(mContext).openDB();
            if (db == null) {
                return;
            }
            DBHelper.getInstance(mContext).createTable(DBConfig.TableAllInfo.CREATE_TABLE);
            db.delete(DBConfig.TableAllInfo.TABLE_NAME, null, null);
        } catch (Throwable e) {
        } finally {
            DBManage.getInstance(mContext).closeDB();
        }
    }

    /**
     * 数据查询 cursor
     * @param db
     * @return
     */
    private Cursor selectCursor(SQLiteDatabase db) {
        if (db != null) {
            return db.query(true,
                    DBConfig.TableAllInfo.TABLE_NAME,
                    new String[]{
                            DBConfig.TableAllInfo.Column.INFO,
                            DBConfig.TableAllInfo.Column.ID,
                            DBConfig.TableAllInfo.Column.TYPE
                    },
                    null, null,
                    null, null,
                    DBConfig.TableAllInfo.Column.ID + " asc",
                    "0," + Constants.MAX_SEND_COUNT);
        }
        return null;
    }

    /**
     * 更新标记
     * @param db
     * @param id
     */
    private void updateSign(SQLiteDatabase db, int id) {
        ContentValues values = new ContentValues();
        values.put(DBConfig.TableAllInfo.Column.SIGN, DBConfig.Status.FLAG_UPLOADING);
        db.update(DBConfig.TableAllInfo.TABLE_NAME, values,
                DBConfig.TableAllInfo.Column.ID + "=?",
                new String[]{String.valueOf(id)});
    }

    public static class Holder {
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
