package com.analysys.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.analysys.utils.ExceptionUtil;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Copyright © 2018 EGuan Inc. All rights reserved.
 * @Description: openDb, CloseDb
 * @Version: 1.0
 * @Create: 2018/2/3 17:31
 * @Author: WXC
 */
class DBManage {
    private AtomicInteger mOpenCounter = new AtomicInteger();
    private DBHelper dbHelper;
    private SQLiteDatabase db;

    public static DBManage getInstance(Context context) {
        Holder.INSTANCE.initDatabaseHelper(context);
        return Holder.INSTANCE;
    }

    public SQLiteDatabase openDB(Context context) {

        try {
            if (mOpenCounter.incrementAndGet() == 1) {
                checkDb(context);
            }
        } catch (Exception e) {
            ExceptionUtil.exceptionThrow(e);
        }

        return db;
    }

    public void closeDB() {
        if (mOpenCounter.decrementAndGet() == 0) {
            try {
                if (db != null) {
                    db.close();
                }
            } catch (Exception e) {
                ExceptionUtil.exceptionThrow(e);
            } finally {
                db = null;
            }
        }
    }

    /**
     * DB重置
     */
    public void resetDB() {
        dbHelper = null;
        db = null;
    }

    /**
     * 校验DB是否为null，并对db赋值
     */
    private void checkDb(Context mContext) {

        if (dbHelper == null) {
            if (mContext != null) {
                dbHelper = new DBHelper(mContext);
            }
        }

        if (db == null && dbHelper != null) {
            db = dbHelper.getWritableDatabase();
        }
    }


    private void initDatabaseHelper(Context context) {
        checkDb(context);
    }

    private static class Holder {
        private static final DBManage INSTANCE = new DBManage();
    }

    private DBManage() {
    }
}
