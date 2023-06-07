package com.analysys.ipc;

import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;

public class BinderParcelable implements Parcelable {

    private IBinder mBinder;

    public static final Creator<BinderParcelable> CREATOR = new Creator<BinderParcelable>() {
        @Override
        public BinderParcelable createFromParcel(Parcel source) {
            return new BinderParcelable(source);
        }

        @Override
        public BinderParcelable[] newArray(int size) {
            return new BinderParcelable[size];
        }
    };

    BinderParcelable(IBinder binder) {
        mBinder = binder;
    }

    BinderParcelable(Parcel source) {
        mBinder = source.readStrongBinder();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStrongBinder(mBinder);
    }

    public IBinder getBinder() {
        return mBinder;
    }
}