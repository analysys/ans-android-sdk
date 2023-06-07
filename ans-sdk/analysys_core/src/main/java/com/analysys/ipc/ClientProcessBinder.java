package com.analysys.ipc;

import android.os.RemoteException;

import com.analysys.utils.ANSLog;
import com.analysys.utils.ActivityLifecycleUtils;

import static com.analysys.ipc.IpcManager.TAG;

/**
 * @Copyright © 2020 Analysys Inc. All rights reserved.
 * @Description:
 * @Create: 2020/8/3 7:00 PM
 * @author: huchangqing
 */
public class ClientProcessBinder extends IAnalysysClient.Stub {

    private IIpcProxy mProxy;

    public void setProxy(IIpcProxy proxy) {
        if (mProxy == proxy) {
            return;
        }
        ANSLog.i(TAG, "set client proxy");
        mProxy = proxy;
    }

    @Override
    public BytesParcelable getVisualSnapshotData(String config, boolean force) throws RemoteException {
        if (mProxy != null) {
            return mProxy.getVisualSnapshotData(config, force);
        }
        return null;
    }

    @Override
    public void clearVisualSnapshot() throws RemoteException {
        if (mProxy != null) {
            mProxy.clearVisualSnapshot();
        }
    }

    @Override
    public void onVisualEditEvent(String data) throws RemoteException {
        if (mProxy != null) {
            mProxy.onVisualEditEvent(data);
        }
    }

    @Override
    public void setVisualEditing(boolean editing) throws RemoteException {
        if (mProxy != null) {
            mProxy.setVisualEditing(editing);
        }
    }

    @Override
    public void reloadVisualEventLocal() throws RemoteException {
        if (mProxy != null) {
            mProxy.reloadVisualEventLocal();
        }
    }

    @Override
    public boolean isInFront() throws RemoteException {
        return ActivityLifecycleUtils.isActivityResumed();
    }
}
