package com.analysys.visual.utils;

import com.analysys.AnalysysAgent;
import com.analysys.ipc.BytesParcelable;
import com.analysys.ipc.IAnalysysClient;
import com.analysys.ipc.IAnalysysMain;
import com.analysys.ipc.IIpcProxy;
import com.analysys.ipc.IpcManager;
import com.analysys.utils.ANSLog;
import com.analysys.utils.AnalysysUtil;
import com.analysys.utils.CommonUtils;
import com.analysys.utils.ExceptionUtil;
import com.analysys.visual.VisualManager;
import com.analysys.visual.bind.VisualBindManager;
import com.analysys.visual.bind.event.EventFactory;
import com.analysys.visual.cmd.SnapshotWrapper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @Copyright © 2020 Analysys Inc. All rights reserved.
 * @Description: 已加混淆，不能移动
 * @Create: 2020/8/4 3:07 PM
 * @author: huchangqing
 */
public class VisualIpc implements IIpcProxy {

    private static VisualIpc sInstance = new VisualIpc();

    private VisualIpc() {
    }

    public static VisualIpc getInstance() {
        return sInstance;
    }

    private SnapshotWrapper mSnapshotWrapper = new SnapshotWrapper();

    @Override
    public BytesParcelable getVisualSnapshotData(String config, boolean force) {
        if (CommonUtils.isMainProcess(AnalysysUtil.getContext())) {
            if (!VisualBindManager.getInstance().isEditing()) {
                setVisualEditing(true);
            }

            if (IpcManager.getInstance().isCurrentProcessInFront()) {
                mSnapshotWrapper.init(config);
                return new BytesParcelable(mSnapshotWrapper.getSnapshotData(force), true);
            } else {
                IAnalysysClient binder = IpcManager.getInstance().getFrontClientBinder();
                if (binder != null) {
                    BytesParcelable bpAll = new BytesParcelable();
                    while (true) {
                        try {
                            BytesParcelable bp = binder.getVisualSnapshotData(config, force);
                            if (bp == null || bp.data == null) {
                                return null;
                            }
                            bpAll.appendData(bp);
                            if (bp.finish) {
                                return bpAll;
                            }
                        } catch (Throwable ignore) {
                            ExceptionUtil.exceptionThrow(ignore);
                            return null;
                        }
                    }
                }
            }
        } else {
            mSnapshotWrapper.init(config);
            if (mSnapshotTmpData == null) {
                mSnapshotTmpData = mSnapshotWrapper.getSnapshotData(force);
            }
            if (mSnapshotTmpData != null) {
                int to = mSnapshotTmpDataIdx + MAX_BINDER_SIZE;
                if (to >= mSnapshotTmpData.length) {
                    to = mSnapshotTmpData.length;
                }
                byte[] data = Arrays.copyOfRange(mSnapshotTmpData, mSnapshotTmpDataIdx, to);

                boolean finish = false;
                if (to == mSnapshotTmpData.length) {
                    mSnapshotTmpDataIdx = 0;
                    mSnapshotTmpData = null;
                    finish = true;
                } else {
                    mSnapshotTmpDataIdx = to;
                }
                return new BytesParcelable(data, finish);
            }
        }
        return null;
    }

    private static final int MAX_BINDER_SIZE = 100 * 1024;
    private byte[] mSnapshotTmpData;
    private int mSnapshotTmpDataIdx;

    @Override
    public void clearVisualSnapshot() {
        mSnapshotWrapper.clear();

        if (CommonUtils.isMainProcess(AnalysysUtil.getContext())) {
            List<IAnalysysClient> listBinder = IpcManager.getInstance().getAllClientBinder();
            if (listBinder != null) {
                try {
                    for (IAnalysysClient binder : listBinder) {
                        binder.clearVisualSnapshot();
                    }
                } catch (Throwable ignore) {
                    ExceptionUtil.exceptionThrow(ignore);
                }
            }
        }
    }

    @Override
    public void onVisualEditEvent(String data) {
        VisualBindManager.getInstance().updateEventsEditing(data);

        if (CommonUtils.isMainProcess(AnalysysUtil.getContext())) {
            List<IAnalysysClient> listBinder = IpcManager.getInstance().getAllClientBinder();
            if (listBinder != null) {
                try {
                    for (IAnalysysClient binder : listBinder) {
                        binder.onVisualEditEvent(data);
                    }
                } catch (Throwable ignore) {
                    ExceptionUtil.exceptionThrow(ignore);
                }
            }
        }
    }

    @Override
    public void reportVisualEvent(String eventId, String eventPageName, Map properties) {
        if (CommonUtils.isMainProcess(AnalysysUtil.getContext())) {
            if (VisualBindManager.getInstance().isEditing()) {
                VisualManager.getInstance().feedbackEvent(eventId, eventPageName, properties);
            } else {
                AnalysysAgent.track(AnalysysUtil.getContext(), eventId, properties);
            }
        } else {
            IAnalysysMain iMain = IpcManager.getInstance().getMainBinder();
            if (iMain != null) {
                try {
                    iMain.reportVisualEvent(eventId, eventPageName, properties);
                } catch (Throwable ignore) {
                    ExceptionUtil.exceptionThrow(ignore);
                }
            }
        }
    }

    @Override
    public void setVisualEditing(boolean editing) {
        VisualBindManager.getInstance().setEditing(editing);
        if (CommonUtils.isMainProcess(AnalysysUtil.getContext())) {
            List<IAnalysysClient> listBinder = IpcManager.getInstance().getAllClientBinder();
            if (listBinder != null) {
                try {
                    for (IAnalysysClient binder : listBinder) {
                        binder.setVisualEditing(editing);
                    }
                } catch (Throwable ignore) {
                    ExceptionUtil.exceptionThrow(ignore);
                }
            }
        }
    }

    @Override
    public void sendVisualEditEvent2Client(String processName) {
        if (!VisualBindManager.getInstance().isEditing()) {
            return;
        }
        IAnalysysClient binder = IpcManager.getInstance().getClientBinder(processName);
        if (binder != null) {
            try {
                binder.setVisualEditing(true);
                JSONArray jaEvent = VisualBindManager.getInstance().getJAEditEvent();
                if (jaEvent.length() == 0) {
                    return;
                }
                JSONObject jo = new JSONObject();
                jo.put(EventFactory.KEY_RECORD_TYPE, EventFactory.RECORD_TYPE_ADD);
                JSONObject payload = new JSONObject();
                payload.put(EventFactory.KEY_EVENTS, jaEvent);
                jo.put(EventFactory.KEY_PAYLOAD, payload);
                binder.onVisualEditEvent(jo.toString());
            } catch (Throwable ignore) {
                ExceptionUtil.exceptionThrow(ignore);
            }
        }
    }

    @Override
    public void reloadVisualEventLocal() {
        if (CommonUtils.isMainProcess(AnalysysUtil.getContext())) {
            List<IAnalysysClient> listBinder = IpcManager.getInstance().getAllClientBinder();
            if (listBinder != null) {
                try {
                    for (IAnalysysClient binder : listBinder) {
                        binder.reloadVisualEventLocal();
                    }
                } catch (Throwable ignore) {
                    ExceptionUtil.exceptionThrow(ignore);
                }
            }
        } else {
            VisualBindManager.getInstance().loadConfigFromLocal();
        }
    }
}
