package com.analysys.ipc;

import android.os.IBinder;
import android.os.RemoteException;

import com.analysys.utils.ANSLog;

import java.util.Map;

import static com.analysys.ipc.IpcManager.TAG;

/**
 * @Copyright © 2020 Analysys Inc. All rights reserved.
 * @Description:
 * @Create: 2020/8/3 6:25 PM
 * @author: huchangqing
 */
public class MainProcessBinder extends IAnalysysMain.Stub {

    private IIpcProxy mProxy;

    public void setProxy(IIpcProxy proxy) {
        if (mProxy == proxy) {
            return;
        }
        ANSLog.i(TAG, "set main proxy");
        mProxy = proxy;
    }

    @Override
    public void setClientBinder(String processName, IBinder client) throws RemoteException {
        IAnalysysClient clientBinder = IAnalysysClient.Stub.asInterface(client);
        IpcManager.getInstance().addClientBinder(processName, clientBinder);
    }

    @Override
    public void reportVisualEvent(String eventId, String eventPageName, Map properties) throws RemoteException {
        if (mProxy != null) {
            mProxy.reportVisualEvent(eventId, eventPageName, properties);
        }
    }

    public void sendVisualEditEvent2Client(String processName) {
        if (mProxy != null) {
            mProxy.sendVisualEditEvent2Client(processName);
        }
    }
}
