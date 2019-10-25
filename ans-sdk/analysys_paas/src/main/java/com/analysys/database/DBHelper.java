package com.analysys.database;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.database.sqlite.SQLiteOpenHelper;

import com.analysys.utils.CommonUtils;

/**
 * @Copyright © 2018 EGuan Inc. All rights reserved.
 * @Description: 建库建表
 * @Version: 1.0
 * @Create: 2018/2/3 17:31
 * @Author: WXC
 */
class DBHelper extends SQLiteOpenHelper {
    private static final String DBNAME = "analysys.data";
    private static final int VERSION = 1;
    private static Context mContext;

    public DBHelper(Context context) {
        super(context, DBNAME, null, VERSION);
        createTables();
    }

    public static DBHelper getInstance(Context context) {
        if (CommonUtils.isEmpty(mContext)) {
            mContext = context;
        }
        return Holder.INSTANCE;
    }

    /**
     * 建表
     */
    public void createTable(String createSQL) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            if (!DBUtils.isTableExist(db)) {
                db.execSQL(createSQL);
            }
        } catch (Throwable e) {
            //ANSLog.e(e);
        }
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DBConfig.TableAllInfo.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    @SuppressLint("SdCardPath")
    private void createTables() {
        SQLiteDatabase db = null;
        try {
            db = getWritableDatabase();
            if (CommonUtils.isEmpty(db)) {
                return;
            }
            if (!DBUtils.isTableExist(db)) {
                db.execSQL(DBConfig.TableAllInfo.CREATE_TABLE);
            }
        } catch (SQLiteDatabaseCorruptException e) {
            DBUtils.deleteDBFile("/data/data/" + mContext.getPackageName() + "/databases/" + DBNAME);
            createTables();
        } catch (Throwable e) {
            //ANSLog.e(e);
        }
    }

    private static class Holder {
        private static final DBHelper INSTANCE = new DBHelper(mContext);
    }
}
