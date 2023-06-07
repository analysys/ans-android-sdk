package com.analysys.visual.cmd;

import android.text.TextUtils;

import com.analysys.ipc.BytesParcelable;
import com.analysys.ipc.IpcManager;
import com.analysys.utils.ANSLog;
import com.analysys.utils.ExceptionUtil;
import com.analysys.visual.VisualManager;
import com.analysys.visual.utils.VisualIpc;

import org.json.JSONObject;

import java.io.OutputStream;

/**
 * @Copyright © 2019 Analysys Inc. All rights reserved.
 * @Description: 获取页面结构和截图
 * @Create: 2019-11-29 10:24
 * @author: hcq
 */
public class CmdSnapshotImpl implements ICmdHandler {

    private static final String TAG = VisualManager.TAG;

    private String mConfig;
    private String mFrontProcessName;

    @Override
    public void handleCmd(Object cmd, OutputStream out) {
        try {
            final JSONObject payload = ((JSONObject) cmd).getJSONObject("payload");
            // 第一次才会有
            if (payload.has("config")) {
                ANSLog.i(TAG, "snapshot init config");
                mConfig = payload.toString();
            }
        } catch (final Throwable ignore) {
            ExceptionUtil.exceptionThrow(ignore);
            return;
        }
        String processName = IpcManager.getInstance().getFrontProcessName();
        boolean forceRefresh = TextUtils.isEmpty(processName)
                || TextUtils.isEmpty(mFrontProcessName)
                || !TextUtils.equals(mFrontProcessName, processName);
        mFrontProcessName = processName;

        BytesParcelable bp = VisualIpc.getInstance().getVisualSnapshotData(mConfig, forceRefresh);
        try {
            if (bp != null && bp.data != null && bp.data.length != 0) {
                out.write(bp.data);
            }
        } catch (final Throwable ignore) {
            ExceptionUtil.exceptionPrint(ignore);
        } finally {
            try {
                out.close();
            } catch (final Throwable ignore) {
                ExceptionUtil.exceptionPrint(ignore);
            }
        }
    }

    public void clear() {
        VisualIpc.getInstance().clearVisualSnapshot();
    }
}
