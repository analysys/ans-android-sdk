package com.analysys.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @Copyright © 2018 EGuan Inc. All rights reserved.
 * @Description: 建库建表
 * @Version: 1.0
 * @Create: 2018/2/3 17:31
 * @Author: WXC
 */
class DBHelper extends SQLiteOpenHelper {

    DBHelper(Context context) {
        super(context, DBConfig.TableAllInfo.DBNAME, null, DBConfig.TableAllInfo.VERSION);
//        createTables();
    }

//    public static DBHelper getInstance(Context context) {
//        if (CommonUtils.isEmpty(mContext)) {
//            mContext = context;
//        }
//        return Holder.INSTANCE;
//    }

    /**
     * 建表
     */
//    private void createTable(String createSQL) {
//        try {
//            SQLiteDatabase db = getWritableDatabase();
//            if (!DBUtils.isTableExist(db)) {
//                db.execSQL(createSQL);
//            }
//        } catch (Throwable e) {
//            //ANSLog.e(e);
//        }
//    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DBConfig.TableAllInfo.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }



//    private static class Holder {
//        private static final DBHelper INSTANCE = new DBHelper(mContext);
//    }
}
