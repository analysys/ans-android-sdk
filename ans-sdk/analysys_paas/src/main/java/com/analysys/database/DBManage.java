package com.analysys.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.analysys.utils.CommonUtils;

/**
 * @Copyright © 2018 EGuan Inc. All rights reserved.
 * @Description: openDb, CloseDb
 * @Version: 1.0
 * @Create: 2018/2/3 17:31
 * @Author: WXC
 */
class DBManage {

    private DBHelper dbHelper;
    private Context mContext;
    private SQLiteDatabase db;

    public static synchronized DBManage getInstance(Context context) {
        Holder.INSTANCE.initContext(context);
        Holder.INSTANCE.initDatabaseHelper(context);
        return Holder.INSTANCE;
    }

    public synchronized SQLiteDatabase openDB() {
        db = dbHelper.getWritableDatabase();
        return db;
    }

    public synchronized void closeDB() {
        try {
            if (!CommonUtils.isEmpty(db)) {
                db.close();
            }
        } finally {
            db = null;
        }
    }

    private void initContext(Context context) {
        if (CommonUtils.isEmpty(mContext)) {
            if (!CommonUtils.isEmpty(context)) {
                mContext = context;
            }
        }
    }

    private void initDatabaseHelper(Context context) {
        if (CommonUtils.isEmpty(dbHelper)) {
            dbHelper = new DBHelper(context);
        }
    }

    private static class Holder {
        private static final DBManage INSTANCE = new DBManage();
    }

    private DBManage() {
    }
}
