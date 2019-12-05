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

            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
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
        long count = 0l;
        try {
            if (mContext != null) {
//                SQLiteDatabase db = DBManage.getInstance(mContext).openDB();
//                if (db != null) {
//
//                    DBHelper.getInstance(mContext).createTable(
//                            TableAllInfo.CREATE_TABLE);
//
//                    cursor = db.query(TableAllInfo.TABLE_NAME,
//                            null, null, null,
//                            null, null, null);
//
//                    if (cursor != null) {
//                        return cursor.getCount();
//                    }
//
//                }

                Uri uri = EventTableMetaData.getTableFZ(mContext);
                cursor = mContext.getContentResolver().query(uri,null,null,null,null);
                if(cursor!=null){
                    count = cursor.getCount();
                }
            }

        } catch (Throwable e) {
            ExceptionUtil.exceptionThrow(e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
//            DBManage.getInstance(mContext).closeDB();
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
//            SQLiteDatabase db = DBManage.getInstance(mContext).openDB();
//            if (db == null) {
//                return;
//            }
//            DBHelper.getInstance(mContext).createTable(TableAllInfo.CREATE_TABLE);

            Uri uri = EventTableMetaData.getTableFZ(mContext);
            mContext.getContentResolver().delete(uri,DBConfig.TableAllInfo.Column.SIGN + "=?",new String[]{String.valueOf(DBConfig.Status.FLAG_UPLOADING)});
//            db.delete(
//                    DBConfig.TableAllInfo.TABLE_NAME,
//                    DBConfig.TableAllInfo.Column.SIGN + "=?",
//                    new String[]{String.valueOf(DBConfig.Status.FLAG_UPLOADING)});
        } catch (Throwable e) {
            ExceptionUtil.exceptionThrow(e);
        }
//        finally {
//            DBManage.getInstance(mContext).closeDB();
//        }
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
//            SQLiteDatabase db = DBManage.getInstance(mContext).openDB();
//            if (db == null) {
//                return;
//            }
//            DBHelper.getInstance(mContext).createTable(DBConfig.TableAllInfo.CREATE_TABLE);

//            cursor = db.query(DBConfig.TableAllInfo.TABLE_NAME,
//                    new String[]{DBConfig.TableAllInfo.Column.ID},
//                    null, null, null, null
//                    , DBConfig.TableAllInfo.Column.ID + " desc", "0," + count);

            Uri uri = EventTableMetaData.getTableFZ(mContext);
            String sortOrder = DBConfig.TableAllInfo.Column.ID + " desc "+" LIMIT 0," + count;
            cursor = mContext.getContentResolver().query(uri,new String[]{DBConfig.TableAllInfo.Column.ID},null,null,sortOrder);


            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {

                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(
                            DBConfig.TableAllInfo.Column.ID));

//                    db.delete(DBConfig.TableAllInfo.TABLE_NAME,
//                            DBConfig.TableAllInfo.Column.ID + "=?",
//                            new String[]{String.valueOf(id)});

                    mContext.getContentResolver().delete(uri,DBConfig.TableAllInfo.Column.ID + "=?",new String[]{String.valueOf(id)});
                }
            }

        } catch (Throwable e) {
            ExceptionUtil.exceptionThrow(e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
//            DBManage.getInstance(mContext).closeDB();
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
//            SQLiteDatabase db = DBManage.getInstance(mContext).openDB();
//            if (db == null) {
//                return;
//            }
//            DBHelper.getInstance(mContext).createTable(DBConfig.TableAllInfo.CREATE_TABLE);
//            db.delete(DBConfig.TableAllInfo.TABLE_NAME, null, null);

            Uri uri = EventTableMetaData.getTableFZ(mContext);
            mContext.getContentResolver().delete(uri,null,null);

        } catch (Throwable e) {
            ExceptionUtil.exceptionThrow(e);
        }
//        finally {
//            DBManage.getInstance(mContext).closeDB();
//        }
    }

//    /**
//     * 数据查询 cursor
//     *
//     * @param db
//     * @return
//     */
//    private Cursor selectCursor(SQLiteDatabase db) {
//        if (db != null) {
//            return db.query(true,
//                    DBConfig.TableAllInfo.TABLE_NAME,
//                    new String[]{
//                            DBConfig.TableAllInfo.Column.INFO,
//                            DBConfig.TableAllInfo.Column.ID,
//                            DBConfig.TableAllInfo.Column.TYPE
//                    },
//                    null, null,
//                    null, null,
//                    DBConfig.TableAllInfo.Column.ID + " asc",
//                    "0," + Constants.MAX_SEND_COUNT);
//        }
//        return null;
//    }

    /**
     * 更新标记
     *
     * @param context
     * @param id
     */
    private void updateSign(Context context, int id) {
        ContentValues values = new ContentValues();
        values.put(DBConfig.TableAllInfo.Column.SIGN, DBConfig.Status.FLAG_UPLOADING);
//        db.update(DBConfig.TableAllInfo.TABLE_NAME, values,
//                DBConfig.TableAllInfo.Column.ID + "=?",
//                new String[]{String.valueOf(id)});

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
