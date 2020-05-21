package com.analysys.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.text.TextUtils;

import com.analysys.userinfo.UserInfo;

/**
 * @Copyright © 2019 Analysys Inc. All rights reserved.
 * @Description: google广告id
 * @Create: 2019-12-24 17:19
 * @author: hcq
 */
public class AdvertisingIdUitls {

    public static void setAdvertisingId() {
        Context context = AnalysysUtil.getContext();
        if (!CommonUtils.isMainProcess(context)) {
            return;
        }
//        String adId = SharedUtil.getString(context, Constants.SP_ADID, null);
        String adId = UserInfo.getADID();
        if (!TextUtils.isEmpty(adId)) {
            return;
        }
        try {
            PackageManager pm = context.getPackageManager();
            pm.getPackageInfo("com.android.vending", 0);
        } catch (Throwable ignore) {
            return;
        }

        AdvertisingConnection connection = new AdvertisingConnection();
        Intent intent = new Intent(
                "com.google.android.gms.ads.identifier.service.START");
        intent.setPackage("com.google.android.gms");
        try {
            context.bindService(intent, connection, Context.BIND_AUTO_CREATE);
        } catch (Throwable ignore) {
            ExceptionUtil.exceptionThrow(ignore);
        }
    }

    private static final class AdvertisingConnection implements ServiceConnection {
        public void onServiceConnected(ComponentName name, IBinder service) {
            Context context = AnalysysUtil.getContext();
            if (context == null) {
                return;
            }
            try {
                AdvertisingInterface adInterface = new AdvertisingInterface(service);
                String id = adInterface.getId();
                if (!TextUtils.isEmpty(id)) {
//                    SharedUtil.setString(context, Constants.SP_ADID, id);
                    UserInfo.setADID(id);
                }
            } catch (Throwable ignore) {
                ExceptionUtil.exceptionThrow(ignore);
            } finally {
                try {
                    context.unbindService(this);
                } catch (Throwable ignore) {
                    ExceptionUtil.exceptionThrow(ignore);
                }
            }
        }

        public void onServiceDisconnected(ComponentName name) {
        }
    }

    private static final class AdvertisingInterface implements IInterface {
        private IBinder binder;

        public AdvertisingInterface(IBinder pBinder) {
            binder = pBinder;
        }

        public IBinder asBinder() {
            return binder;
        }

        public String getId() {
            String id = null;
            Parcel data = null;
            Parcel reply = null;
            try {
                data = Parcel.obtain();
                reply = Parcel.obtain();
                data.writeInterfaceToken("com.google.android.gms.ads.identifier.internal.IAdvertisingIdService");
                binder.transact(1, data, reply, 0);
                reply.readException();
                id = reply.readString();
            } catch (Throwable ignore) {
                ExceptionUtil.exceptionThrow(ignore);
            } finally {
                try {
                    reply.recycle();
                    data.recycle();
                } catch (Throwable ignore) {
                    ExceptionUtil.exceptionThrow(ignore);
                }
            }
            return id;
        }
    }
}