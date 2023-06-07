package com.analysys.visual;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;
import android.util.SparseArray;

import com.analysys.utils.ANSLog;
import com.analysys.utils.ExceptionUtil;
import com.analysys.visual.cmd.CmdDeviceInfoImpl;
import com.analysys.visual.cmd.CmdEventBindImpl;
import com.analysys.visual.cmd.CmdSendToServerImpl;
import com.analysys.visual.cmd.CmdSnapshotImpl;
import com.analysys.visual.cmd.ICmdHandler;
import com.analysys.visual.conn.ConnManager;
import com.analysys.visual.utils.VisualIpc;

import java.util.HashMap;
import java.util.Map;

/**
 * @Copyright © 2018 EGuan Inc. All rights reserved.
 * @Description: 提供调用可视化模块的接口类
 * @Version: 1.0
 * @Create: 2018/3/22 下午8:11
 * @Author: chris
 */
public class VisualManager {

    public static final String TAG = "analysys.visual";
    public static final String KEY_EVENT_ID = "event_id";
    public static final String KEY_EVENT_PAGE_NAME = "event_page_name";
    public static final String KEY_EVENT_PROPERTIES = "event_properties";

    private static VisualManager instance;
    private String mUrl;
    private ConnManager mConnManager;
    private Handler mThreadHandler;
    private SparseArray<ICmdHandler> mCmdImplArray;
    private boolean isStarted;

    public static final int MESSAGE_CONNECT_TO_EDITOR = 1;
    public static final int MESSAGE_SEND_SNAPSHOT = 2;
    public static final int MESSAGE_SEND_DEVICE_INFO = 3;
    public static final int MESSAGE_HANDLE_EDITOR_BINDINGS_RECEIVED = 5;
    public static final int MESSAGE_HANDLE_EDITOR_CLOSED = 6;
    public static final int MESSAGE_HANDLE_SEND_EVENT_SERVER = 7;

//    DebugViewManager dvManager = new DebugViewManager();

    private VisualManager() {
    }

    public static VisualManager getInstance() {
        if (instance == null) {
            synchronized (VisualManager.class) {
                if (instance == null) {
                    instance = new VisualManager();
                }
            }
        }
        return instance;
    }

    public String getUrl() {
        return mUrl;
    }

    public void connectManual() {
        if (!mConnManager.isConnected()) {
            mConnManager.connectManual();
        }
    }

    public boolean isStarted() {
        return isStarted;
    }

    public void start(String url) {
        isStarted = true;
        mUrl = url;
        mCmdImplArray = new SparseArray<>();
        mCmdImplArray.put(MESSAGE_SEND_DEVICE_INFO, new CmdDeviceInfoImpl());
        mCmdImplArray.put(MESSAGE_SEND_SNAPSHOT, new CmdSnapshotImpl());
        mCmdImplArray.put(MESSAGE_HANDLE_EDITOR_BINDINGS_RECEIVED, new CmdEventBindImpl());
        mCmdImplArray.put(MESSAGE_HANDLE_SEND_EVENT_SERVER, new CmdSendToServerImpl());

        HandlerThread handlerThread = new HandlerThread(VisualManager.class.getCanonicalName());
        handlerThread.start();
        mThreadHandler = new Handler(handlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                try {
                    switch (msg.what) {
                        case MESSAGE_CONNECT_TO_EDITOR:
                            mConnManager.doConnect();
                            if (!mConnManager.isConnected()) {
                                ANSLog.i(TAG, "WS connect failed. url:" + mUrl);
                                mConnManager.registerSensor();
                            } else {
                                ANSLog.i(TAG, "WS connect success. url:" + mUrl);
                            }
                            break;
                        case MESSAGE_HANDLE_EDITOR_CLOSED:
                            ANSLog.i(TAG, "WS closed");
                            handleEditorClosed();
                            break;
                        default:
                            ICmdHandler handler = mCmdImplArray.get(msg.what);
                            if (handler != null) {
                                handler.handleCmd(msg.obj, mConnManager.getNewOutputStream());
                            }
                            break;
                    }
                } catch (Throwable ignore) {
                    ExceptionUtil.exceptionThrow(ignore);
                }
            }
        };
        mConnManager = new ConnManager(mThreadHandler);
        mConnManager.registerSensor();

//        Handler mainHandler = new Handler(Looper.getMainLooper());
//        mainHandler.post(new Runnable() {
//            @Override
//            public void run() {
//                dvManager.init();
//            }
//        });
    }

    public void feedbackEvent(String eventId, String eventPageName, Map<String, Object> properties) {
        if (TextUtils.isEmpty(eventId) || (mConnManager != null && !mConnManager.isConnected())) {
            return;
        }
        final Message message = mThreadHandler.obtainMessage
                (MESSAGE_HANDLE_SEND_EVENT_SERVER);
        Map<String, Object> eventWrapper = new HashMap<>();
        eventWrapper.put(KEY_EVENT_ID, eventId);
        eventWrapper.put(KEY_EVENT_PAGE_NAME, eventPageName);
        eventWrapper.put(KEY_EVENT_PROPERTIES, properties);
        message.obj = eventWrapper;
        mThreadHandler.sendMessage(message);
    }

    /**
     * Clear state associated with the editor now that the editor is gone.
     */
    private void handleEditorClosed() {
        mConnManager.registerSensor();
        mConnManager.close();

        // Free (or make available) snapshot memory
        CmdSnapshotImpl snapshotImpl = (CmdSnapshotImpl) mCmdImplArray.get(MESSAGE_SEND_SNAPSHOT);
        if (snapshotImpl != null) {
            snapshotImpl.clear();
        }

        VisualIpc.getInstance().setVisualEditing(false);
    }
}
