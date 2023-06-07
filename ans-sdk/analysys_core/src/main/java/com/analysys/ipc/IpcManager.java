package com.analysys.ipc;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.text.TextUtils;

import com.analysys.database.EventTableMetaData;
import com.analysys.utils.ANSLog;
import com.analysys.utils.ActivityLifecycleUtils;
import com.analysys.utils.AnalysysUtil;
import com.analysys.utils.AnsReflectUtils;
import com.analysys.utils.CommonUtils;
import com.analysys.utils.ExceptionUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Copyright © 2020 Analysys Inc. All rights reserved.
 * @Description:
 * @Create: 2020/8/4 2:43 PM
 * @author: huchangqing
 */
public class IpcManager {

    public static final String TAG = "analysys.ipc";

    private static IpcManager sInstance = new IpcManager();

    private IpcManager() {
    }

    public static IpcManager getInstance() {
        return sInstance;
    }

    /**
     * 主进程binder
     */
    private IAnalysysMain mainProcessBinder;

    /**
     * 当前进程binder（非主进程）
     */
    private ClientProcessBinder mClientProcessBinder;

    private PackageInfo mPackageInfo;

    /**
     * 当前前台进程名字
     */
    public String getFrontProcessName() {
        try {
            Context context = AnalysysUtil.getContext();
            ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(1);
            if (null != runningTaskInfos) {
                ComponentName topcmpName = runningTaskInfos.get(0).topActivity;
                if (null != topcmpName) {
                    if (mPackageInfo == null) {
                        mPackageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES);
                    }
                    for (ActivityInfo info : mPackageInfo.activities) {
                        if (TextUtils.equals(info.name, topcmpName.getClassName())) {
                            return info.processName;
                        }
                    }
                }
            }
        } catch (Throwable ignore) {
            ExceptionUtil.exceptionThrow(ignore);
        }
        return null;
    }

    /**
     * 绑定的非主进程（主进程）
     */
    private Map<String, IAnalysysClient> mClientBinderMap;

    public synchronized IAnalysysMain getMainBinder() {
        if (mainProcessBinder == null) {
            Context context = AnalysysUtil.getContext();
            if (CommonUtils.isMainProcess(context)) {
                mainProcessBinder = new MainProcessBinder();
                setupProxy();
            } else {
                queryMainBinderFromClient(context);
            }
        }
        return mainProcessBinder;
    }

    /**
     * 添加子进程binder（主进程）
     */
    public void addClientBinder(final String processName, IAnalysysClient binder) {
        ANSLog.i(TAG, "add client binder " + processName);
        if (TextUtils.isEmpty(processName) || binder == null) {
            return;
        }
        if (mClientBinderMap == null) {
            mClientBinderMap = new ConcurrentHashMap<>();
        }
        mClientBinderMap.put(processName, binder);
        ((MainProcessBinder) mainProcessBinder).sendVisualEditEvent2Client(processName);
        try {
            binder.asBinder().linkToDeath(new IBinder.DeathRecipient() {
                @Override
                public void binderDied() {
                    ANSLog.i(TAG, "remove client binder " + processName);
                    mClientBinderMap.remove(processName);
                }
            }, 0);
        } catch (Throwable ignore) {
            ExceptionUtil.exceptionThrow(ignore);
        }
    }

    /**
     * 判断应用是否在前台（主进程）
     */
    public boolean isAppInFront() {
        if (ActivityLifecycleUtils.isActivityResumed()) {
            return true;
        }
        if (mClientBinderMap != null) {
            try {
                for (IAnalysysClient client : mClientBinderMap.values()) {
                    if (client.isInFront()) {
                        return true;
                    }
                }
            } catch (Throwable ignore) {
                ExceptionUtil.exceptionPrint(ignore);
            }
        }
        return false;
    }

    public void init() {
        final Context context = AnalysysUtil.getContext();
        if (context == null) {
            return;
        }

        if (CommonUtils.isMainProcess(context)) {
            context.sendBroadcast(new Intent(ACTION_MAIN_BOOT));
            ANSLog.i(TAG, "main process init");
        } else {
            mClientProcessBinder = new ClientProcessBinder();
            setupProxy();
            queryMainBinderFromClient(context);
        }
    }

    private boolean mMainProcessBootReceiverRegistered;

    private BroadcastReceiver mMainProcessBootReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            queryMainBinderFromClient(context);
        }
    };

    private static final String ACTION_MAIN_BOOT = "action_main_boot";

    private void queryMainBinderFromClient(final Context context) {
        Uri uri = EventTableMetaData.getIpcUri(context);
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(uri, null, null, null, null);
            if (cursor != null) {
                if (mMainProcessBootReceiverRegistered) {
                    context.getApplicationContext().unregisterReceiver(mMainProcessBootReceiver);
                    mMainProcessBootReceiverRegistered = false;
                }
                IBinder binder = AnalysysBinderCursor.getBinder(cursor);
                if (binder != null) {
                    final String processName = IpcUtils.getCurrentProcessName();
                    binder.linkToDeath(new IBinder.DeathRecipient() {
                        @Override
                        public void binderDied() {
                            mainProcessBinder = null;
                            ANSLog.w(TAG, "unlink to main process: " + processName);
                            context.getApplicationContext().registerReceiver(mMainProcessBootReceiver, new IntentFilter(ACTION_MAIN_BOOT));
                            mMainProcessBootReceiverRegistered = true;
                        }
                    }, 0);
                    mainProcessBinder = IAnalysysMain.Stub.asInterface(binder);
                    ANSLog.i(TAG, "link to main process: " + processName);
                    mainProcessBinder.setClientBinder(processName, mClientProcessBinder);
                }
            }
        } catch (Throwable ignore) {
            ExceptionUtil.exceptionThrow(ignore);
        } finally {
            if (cursor != null) {
                try {
                    cursor.close();
                } catch (Throwable ignore) {
                }
            }
        }
    }

    private void setupProxy() {
        Object visualIpc = AnsReflectUtils.invokeStaticMethod("com.analysys.visual.utils.VisualIpc", "getInstance");
        if (!(visualIpc instanceof IIpcProxy)) {
            return;
        }
        IIpcProxy proxy = (IIpcProxy) visualIpc;
        if (CommonUtils.isMainProcess(AnalysysUtil.getContext())) {
            if (mainProcessBinder != null) {
                ((MainProcessBinder) mainProcessBinder).setProxy(proxy);
            }
        } else {
            if (mClientProcessBinder != null) {
                mClientProcessBinder.setProxy(proxy);
            }
        }
    }

    /**
     * 判断当前进程是否在前台
     */
    public boolean isCurrentProcessInFront() {
        String processName = IpcUtils.getCurrentProcessName();
        return TextUtils.equals(processName, getFrontProcessName());
    }

    public IAnalysysClient getFrontClientBinder() {
        if (mClientBinderMap != null) {
            String processName = getFrontProcessName();
            if (processName != null) {
                return mClientBinderMap.get(processName);
            }
        }
        return null;
    }

    public List<IAnalysysClient> getAllClientBinder() {
        if (mClientBinderMap != null) {
            return new ArrayList<>(mClientBinderMap.values());
        }
        return null;
    }

    public IAnalysysClient getClientBinder(String processName) {
        if (mClientBinderMap != null) {
            return mClientBinderMap.get(processName);
        }
        return null;
    }
}
