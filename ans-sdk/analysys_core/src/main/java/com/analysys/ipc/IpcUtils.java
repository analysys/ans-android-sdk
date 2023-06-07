package com.analysys.ipc;

import android.app.ActivityManager;
import android.content.Context;
import android.text.TextUtils;

import com.analysys.utils.ANSLog;
import com.analysys.utils.AnalysysUtil;
import com.analysys.utils.ExceptionUtil;

/**
 * @Copyright © 2020 Analysys Inc. All rights reserved.
 * @Description:
 * @Create: 2020/8/4 10:19 AM
 * @author: huchangqing
 */
public class IpcUtils {

    private static String sProcessName;

    public static String getCurrentProcessName() {
        ANSLog.d("====getCurrentProcessName===",sProcessName);
        if (TextUtils.isEmpty(sProcessName)) {
            try {
                ANSLog.d("====ProcessName isEmpty===",sProcessName);
                int pid = android.os.Process.myPid();
                ActivityManager manager = (ActivityManager) AnalysysUtil.getContext().getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
                for (ActivityManager.RunningAppProcessInfo process : manager.getRunningAppProcesses()) {
                    if (process.pid == pid) {
                        sProcessName = process.processName;
                        break;
                    }
                }
                ANSLog.d("====ProcessName isEmpty done===",sProcessName);
                new Exception().printStackTrace();
            } catch (Throwable ignore) {
                ExceptionUtil.exceptionThrow(ignore);
            }
        }
        return sProcessName;
    }
}
