package com.analysys.database;

import android.annotation.SuppressLint;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.Process;
import android.text.TextUtils;

import com.analysys.AnsRamControl;
import com.analysys.ipc.AnalysysBinderCursor;
import com.analysys.ipc.IAnalysysMain;
import com.analysys.ipc.IpcManager;
import com.analysys.utils.ExceptionUtil;


/**
 * @Copyright © 2019 EGuan Inc. All rights reserved.
 * @Description: 数据操作
 * @Version: 1.0
 * @Create: 2019/11/25 19:22
 * @Author: WP
 */
public class AnsContentProvider extends ContentProvider {

    private Context mContext = null;
    private static UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private SharedPreferencesHelp sharedPreferencesHelp = null;

    @Override
    public boolean onCreate() {
        mContext = this.getContext();

        if (mContext != null) {
            String authority = mContext.getPackageName() + EventTableMetaData.PROVIDER_NAME;

            uriMatcher.addURI(authority, EventTableMetaData.TABLE_FZ, EventTableMetaData.TABLE_FZ_DIR);
            uriMatcher.addURI(authority, EventTableMetaData.TABLE_SP, EventTableMetaData.TABLE_SP_DIR);
            uriMatcher.addURI(authority, EventTableMetaData.TABLE_RAM, EventTableMetaData.TABLE_RAM_DIR);
            uriMatcher.addURI(authority, EventTableMetaData.TABLE_IPC, EventTableMetaData.TABLE_IPC_DIR);
        }
        return true;
    }

    /**
     * 进行数据库查询操作
     *
     * @param uri
     * @param projection
     * @param selection
     * @param selectionArgs
     * @param sortOrder
     * @return
     * @throws Exception
     */
    private Cursor queryDb(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) throws Exception {
        Cursor cursor = null;
        SQLiteDatabase db = DBManage.getInstance(mContext).openDB(mContext);
        if (db != null) {
            cursor = db.query(DBConfig.TableAllInfo.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
        }
        return cursor;
    }


    @SuppressLint("Recycle")
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if (uri == null) {
            return null;
        }
        if (Process.myUid() != Binder.getCallingUid()) {
            return null;
        }
        Cursor cursor = null;
        switch (uriMatcher.match(uri)) {
            case EventTableMetaData.TABLE_FZ_DIR: {
                try {
                    cursor = queryDb(uri, projection, selection, selectionArgs, sortOrder);
                } catch (SQLiteDatabaseCorruptException sql) {
                    dataCorruptException();
                } catch (SQLiteException sqLiteException) {
                    tableException(sqLiteException);
                } catch (Throwable ignore) {
                    ExceptionUtil.exceptionThrow(ignore);
                }
            }
            break;
            case EventTableMetaData.TABLE_SP_DIR: {
                String spValues = "";
                try {
                    String spKey = null;
                    String type = null;
                    if (projection != null && projection.length >= 2) {
                        spKey = projection[0];
                        type = projection[1];
                    }

                    checkSp();

                    if (mContext != null) {
                        SharedPreferences sharedPreferences = sharedPreferencesHelp.getPreferences(mContext);
                        if (sharedPreferences != null && spKey != null) {

                            if(type!=null){

                                if(type.equals("boolean")){
                                    if(sharedPreferences.contains(spKey)){
                                        boolean spbool = sharedPreferences.getBoolean(spKey,false);
                                        spValues = String.valueOf(spbool);
                                    }

                                } else if(type.equals("int")){
                                    if(sharedPreferences.contains(spKey)){
                                        int spint = sharedPreferences.getInt(spKey,0);
                                        spValues = String.valueOf(spint);
                                    }

                                } else if(type.equals("float")){
                                    if(sharedPreferences.contains(spKey)){
                                        float spfloat = sharedPreferences.getFloat(spKey,0f);
                                        spValues = String.valueOf(spfloat);
                                    }

                                } else if(type.equals("long")) {
                                    if(sharedPreferences.contains(spKey)){
                                        long splong = sharedPreferences.getLong(spKey,0l);
                                        spValues = String.valueOf(splong);
                                    }

                                } else if(type.equals("string")){
                                    if(sharedPreferences.contains(spKey)) {
                                        spValues = sharedPreferences.getString(spKey, selection);
                                    }
                                }
                            }
                        }
                    }
                } catch (Throwable ignore) {
                    ExceptionUtil.exceptionThrow(ignore);
                } finally {
                    MatrixCursor matrixCursor = new MatrixCursor(new String[]{"column_name"});
                    matrixCursor.addRow(new Object[]{spValues});

                    cursor = matrixCursor;
                }
            }
            break;
            case EventTableMetaData.TABLE_RAM_DIR: {
                try {
                    String key = projection[0];
                    String defValue = projection[1];
                    String ramValue = AnsRamControl.RamData.getInstance().getMap().get(key);
                    MatrixCursor matrixCursor = new MatrixCursor(new String[]{"column_name"});
                    if (ramValue != null) {
                        matrixCursor.addRow(new Object[]{ramValue});
                    } else {
                        matrixCursor.addRow(new Object[]{defValue});
                    }
                    cursor = matrixCursor;
                } catch (Throwable e) {
                    ExceptionUtil.exceptionThrow(e);
                }
            }
            break;
            case EventTableMetaData.TABLE_IPC_DIR: {
                IAnalysysMain iMain = IpcManager.getInstance().getMainBinder();
                IBinder binder = null;
                if (iMain != null) {
                    binder = iMain.asBinder();
                }
                return new AnalysysBinderCursor(binder);
            }
            default:
                break;
        }

        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        String type = EventTableMetaData.CONTENT_TYPE;
        switch (uriMatcher.match(uri)) {
            case EventTableMetaData.TABLE_FZ_DIR: {
                type = EventTableMetaData.CONTENT_TYPE;
            }
            break;
            case EventTableMetaData.TABLE_SP_DIR: {
                type = EventTableMetaData.CONTENT_TYPE_ITEM;
            }
            break;
            case EventTableMetaData.TABLE_RAM_DIR: {
                type = EventTableMetaData.CONTENT_TYPE_RAM;
            }
            break;
            case EventTableMetaData.TABLE_IPC_DIR: {
                type = EventTableMetaData.CONTENT_TYPE_IPC;
            }
            break;
            default:
                break;
        }
        return type;
    }

    /**
     * 进行数据库插入操作
     *
     * @param uri
     * @param values
     * @return
     */
    private Uri insertDb(Uri uri, ContentValues values) throws Exception {

        long count = -2l;
        SQLiteDatabase db = DBManage.getInstance(mContext).openDB(mContext);
        if (db != null) {
            count = db.insert(DBConfig.TableAllInfo.TABLE_NAME, null, values);
        }

        Uri tmpUri = ContentUris.withAppendedId(uri, count);
        return tmpUri;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Uri tmpUri = uri;
        if (uri == null || uriMatcher == null) {
            return tmpUri;
        }
        if (Process.myUid() != Binder.getCallingUid()) {
            return tmpUri;
        }
        switch (uriMatcher.match(uri)) {
            case EventTableMetaData.TABLE_FZ_DIR: {
                synchronized (this){
                    try {

                        if (values == null || values.size() == 0) {
                            return tmpUri;
                        }

                        tmpUri = insertDb(uri, values);

                    } catch (SQLiteDatabaseCorruptException sql) {
                        dataCorruptException();
                        try {
                            tmpUri = insertDb(uri, values);
                        } catch (Throwable ignore) {
                            ExceptionUtil.exceptionThrow(ignore);
                        }
                    } catch (SQLiteException sqLiteException) {
                        tableException(sqLiteException);
                        try {
                            tmpUri = insertDb(uri, values);
                        } catch (Throwable ignore) {
                            ExceptionUtil.exceptionThrow(ignore);
                        }
                    } catch (Throwable ignore) {
                        ExceptionUtil.exceptionThrow(ignore);
                    }
                }
            }
            break;
            case EventTableMetaData.TABLE_SP_DIR: {

                try {
                    checkSp();
                    if (mContext != null && sharedPreferencesHelp != null) {
                        SharedPreferences.Editor editor = sharedPreferencesHelp.getEditor(mContext);
                        if (editor != null) {
                            if(values.getAsString("type").equals("boolean")){
                                editor.putBoolean(values.getAsString("key"),values.getAsBoolean("values")).commit();
                            } else if(values.getAsString("type").equals("int")){
                                editor.putInt(values.getAsString("key"),values.getAsInteger("values")).commit();
                            } else if (values.getAsString("type").equals("float")){
                                editor.putFloat(values.getAsString("key"),values.getAsFloat("values")).commit();
                            } else if (values.getAsString("type").equals("long")){
                                editor.putLong(values.getAsString("key"),values.getAsLong("values")).commit();
                            }else if(values.getAsString("type").equals("string")){
                                editor.putString(values.getAsString("key"), values.getAsString("values")).commit();
                            }
                        }
                    }
                }catch (Throwable ignore) {
                    ExceptionUtil.exceptionThrow(ignore);
                }

            }
            break;
            case EventTableMetaData.TABLE_RAM_DIR: {

                try {
                    String key = values.getAsString("key");
                    String value = values.getAsString("values");

                    AnsRamControl.RamData.getInstance().getMap().put(key, value);

                } catch (Throwable ignore) {
                    ExceptionUtil.exceptionThrow(ignore);
                }

            }
            break;
            default:
                break;
        }
        if (mContext != null && mContext.getContentResolver() != null) {
            mContext.getContentResolver().notifyChange(uri, null);
        }
        return tmpUri;
    }

    private int deleteDb(Uri uri, String selection, String[] selectionArgs) throws Exception {
        int code = -3;
        SQLiteDatabase db = DBManage.getInstance(mContext).openDB(mContext);
        if (db != null) {
            code = db.delete(DBConfig.TableAllInfo.TABLE_NAME, selection, selectionArgs);
        }
        return code;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        if (uri == null) {
            return -2;
        }
        if (Process.myUid() != Binder.getCallingUid()) {
            return -4;
        }
        int code = -3;
        switch (uriMatcher.match(uri)) {
            case EventTableMetaData.TABLE_FZ_DIR: {
                try {
                    code = deleteDb(uri, selection, selectionArgs);
                } catch (SQLiteDatabaseCorruptException sql) {
                    dataCorruptException();
                } catch (SQLiteException sqLiteException) {
                    tableException(sqLiteException);
                } catch (Throwable ignore) {
                    ExceptionUtil.exceptionThrow(ignore);
                }
            }
            break;
            case EventTableMetaData.TABLE_SP_DIR: {

            }
            break;
            default:
                break;
        }
        if (mContext != null && mContext.getContentResolver() != null) {
            mContext.getContentResolver().notifyChange(uri, null);
        }
        return code;
    }

    private int updateDb(Uri uri, ContentValues values, String selection, String[] selectionArgs) throws Exception {
        int code = -3;
        SQLiteDatabase db = DBManage.getInstance(mContext).openDB(mContext);
        if (db != null) {
            code = db.update(DBConfig.TableAllInfo.TABLE_NAME, values, selection, selectionArgs);
        }
        return code;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (uri == null || values == null) {
            return -2;
        }
        if (Process.myUid() != Binder.getCallingUid()) {
            return -4;
        }
        int code = -3;
        try {
            switch (uriMatcher.match(uri)) {
                case EventTableMetaData.TABLE_FZ_DIR: {
                    try {
                        code =updateDb(uri, values, selection, selectionArgs);
                    } catch (SQLiteDatabaseCorruptException sql) {
                        dataCorruptException();
                    } catch (SQLiteException sqLiteException) {
                        tableException(sqLiteException);
                    } catch (Throwable ignore) {
                        ExceptionUtil.exceptionThrow(ignore);
                    }
                }
                break;
                case EventTableMetaData.TABLE_SP_DIR: {
                    checkSp();
                    if (mContext != null) {
                        SharedPreferences.Editor editor = sharedPreferencesHelp.getEditor(mContext);
                        if (editor != null) {
                            String keyValue = values.getAsString("key");
                            if(!TextUtils.isEmpty(keyValue)) {
                                editor.remove(keyValue).commit();
                            }
                        }
                    }
                }
                break;
                default:
                    break;
            }
            if (mContext != null && mContext.getContentResolver() != null) {
                mContext.getContentResolver().notifyChange(uri, null);
            }
        }catch (Throwable ignore) {
           ExceptionUtil.exceptionThrow(ignore);
        }

        return code;
    }

    /**
     * 异常删除db，然后重建db
     */
    private void dataCorruptException() {
        mContext.deleteDatabase(DBConfig.TableAllInfo.DBNAME);


        dbReset();

        try{
            checkDb();
        }catch (Throwable ignore){
            ExceptionUtil.exceptionThrow(ignore);
        }

    }


    /**
     * 数据库表不存在异常
     */
    private void tableException(SQLiteException sqLiteException) {
        if (sqLiteException != null && sqLiteException.getMessage() != null) {
            if (sqLiteException.getMessage().contains("no such table")) {
                dbReset();
            }
        }
    }


    /**
     * 数据库重置
     */
    private void dbReset() {
        DBManage.getInstance(mContext).resetDB();

    }

    /**
     * 校验DB是否为null，并对db赋值
     */
    private void checkDb() throws Exception{

        DBManage.getInstance(mContext).openDB(mContext);
    }

    /**
     * 校验SharedPreference
     */
    private void checkSp() {
        if (sharedPreferencesHelp == null) {
            sharedPreferencesHelp = new SharedPreferencesHelp();
        }
    }
}
