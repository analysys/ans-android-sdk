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
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DBConfig.TableAllInfo.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

}
