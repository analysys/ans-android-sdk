package com.analysys.ipc;

import android.os.Parcel;
import android.os.Parcelable;

public class BytesParcelable implements Parcelable {

    public byte[] data;
    public boolean finish;

    protected BytesParcelable(Parcel in) {
        data = in.createByteArray();
        finish = in.readByte() != 0;
    }

    public BytesParcelable(byte[] data, boolean finish) {
        this.data = data;
        this.finish = finish;
    }

    public BytesParcelable() {
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByteArray(data);
        dest.writeByte((byte) (finish ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<BytesParcelable> CREATOR = new Creator<BytesParcelable>() {
        @Override
        public BytesParcelable createFromParcel(Parcel in) {
            return new BytesParcelable(in);
        }

        @Override
        public BytesParcelable[] newArray(int size) {
            return new BytesParcelable[size];
        }
    };

    public void appendData(BytesParcelable bp) {
        if (bp.data.length == 0) {
            return;
        }
        if (data == null || data.length == 0) {
            data = bp.data;
            return;
        }
        byte[] result = new byte[data.length + bp.data.length];
        System.arraycopy(data, 0, result, 0, data.length);
        System.arraycopy(bp.data, 0, result, data.length, bp.data.length);
        data = result;
    }
}