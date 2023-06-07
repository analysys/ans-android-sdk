package com.analysys.ipc;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;

public class AnalysysBinderCursor extends MatrixCursor {

    static final String KEY_BINDER = "analysys_binder";
    static final String[] COLUMNS = {"binder"};

    private Bundle mBinderExtra = new Bundle();

    public AnalysysBinderCursor(IBinder binder) {
        super(COLUMNS);

        if (binder != null) {
            Parcelable value = new BinderParcelable(binder);
            mBinderExtra.putParcelable(KEY_BINDER, value);
        }
    }

    @Override
    public Bundle getExtras() {
        return mBinderExtra;
    }

    public static final IBinder getBinder(Cursor cursor) {
        Bundle bundle = cursor.getExtras();
        bundle.setClassLoader(BinderParcelable.class.getClassLoader());
        BinderParcelable parcelBinder = bundle.getParcelable(KEY_BINDER);
        return parcelBinder.getBinder();
    }

}