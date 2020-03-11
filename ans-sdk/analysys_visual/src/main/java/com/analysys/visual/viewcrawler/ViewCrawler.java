package com.analysys.visual.viewcrawler;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.JsonWriter;

import com.analysys.utils.ActivityLifecycleUtils;
import com.analysys.utils.InternalAgent;
import com.analysys.visual.VisualAgent;
import com.analysys.visual.utils.Constants;
import com.analysys.visual.utils.EGJSONUtils;
import com.analysys.visual.utils.EgPair;
import com.analysys.visual.utils.UIHelper;
import com.analysys.visual.utils.VisUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

@TargetApi(16)
public class ViewCrawler {

    private static final String SHARED_PREF_BINDINGS_KEY = "viewcrawler.bindings";
    private static final String SHARED_PREF_EDITS_FILE = "viewcrawler.sp";
    private static final int MESSAGE_INITIALIZE_CHANGES = 0;
    private static final int MESSAGE_CONNECT_TO_EDITOR = 1;
    private static final int MESSAGE_SEND_STATE_FOR_EDITING = 2;
    private static final int MESSAGE_SEND_DEVICE_INFO = 3;
    private static final int MESSAGE_EVENT_BINDINGS_RECEIVED = 4;
    private static final int MESSAGE_HANDLE_EDITOR_BINDINGS_RECEIVED = 5;
    private static final int MESSAGE_HANDLE_EDITOR_CLOSED = 6;
    private static final int MESSAGE_HANDLE_SEND_EVENT_SERVER = 7;
    private static final int MESSAGE_CHECK_DLG = 8;
    private static final int EMULATOR_CONNECT_ATTEMPT_INTERVAL_MILLIS = 1000 * 30;
    private static final String TAG = "VisualViewCrawler";
    private final Context mContext;
    private final DynamicEventTracker mDynamicEventTracker;
    private final EditState mEditState;
    private final Map<String, String> mDeviceInfo;
    private final ViewCrawlerHandler mMessageThreadHandler;
    private final float mScaledDensity;
    private final int width;
    private final int height;
    //for process event info by ws
    private final String WS_KEY_EVENTS = "events";
    private final String WS_KEY_PAYLOAD = "payload";
    private final String WS_KEY_EVENT_TYPE = "recordtype";
    private final String WS_EVENTINFO_DOWN = "all";
    private final String WS_EVENTINFO_APPEND = "add";
    private final String WS_EVENTINFO_UPDATE = "update";
    private final String WS_EVENTINFO_DELETE = "delete";
    //    path and self json
    private Map<String, JSONObject> mMemoryPathAndJson = null;
    private final Handler mMainThreadHandler;

    private SensorHelper mSensorHelper;
    public ViewCrawler(Context context) {
        mContext = context;
        mEditState = new EditState();
        mDeviceInfo = VisUtils.getDeviceInfo(context);
        DisplayMetrics dm = Resources.getSystem().getDisplayMetrics();
        mScaledDensity = dm.scaledDensity;
        width = dm.widthPixels;
        height = dm.heightPixels;
        final HandlerThread thread = new HandlerThread(ViewCrawler.class.getCanonicalName());
        thread.setPriority(Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        mMessageThreadHandler = new ViewCrawlerHandler(context, thread.getLooper());

        mDynamicEventTracker = new DynamicEventTracker();
        mSensorHelper = new SensorHelper();
        mMainThreadHandler = new Handler(Looper.getMainLooper());
    }

    public void startUpdates() {
        mMessageThreadHandler.start();
        applyPersistedUpdates();

        mMainThreadHandler.postDelayed(mCheckDlgRunnable, CHECK_DLG_DELAY);
    }

    public void applyPersistedUpdates() {
        mMessageThreadHandler.sendMessage(mMessageThreadHandler.obtainMessage
                (MESSAGE_INITIALIZE_CHANGES));
    }

    /**
     * 记录当前界面是否有dialog在显示
     * */
    private boolean mIsDlgShowing;

    private long CHECK_DLG_DELAY = 500;

    /**
     * 是否含有对话框配置
     */
    private boolean mContainsDlgConfig;

    /**
     * 检测dialog显示
     */
    private Runnable mCheckDlgRunnable = new Runnable() {
        public void run() {
            if (mContainsDlgConfig || mMessageThreadHandler.isConnected()) {
                boolean isShowingDlg = false;
                Activity activity = ActivityLifecycleUtils.getCurrentActivity();
                if (activity != null) {
                    List<ViewSnapshot.RootViewInfo> listView = UIHelper.getActivityDialogs(activity);
                    if (listView != null && !listView.isEmpty()) {
                        isShowingDlg = true;
                    }
                }
                if (mIsDlgShowing != isShowingDlg) {
                    mIsDlgShowing = isShowingDlg;
                    mMessageThreadHandler.applyEventBindings();
                }
            }
            mMainThreadHandler.postDelayed(this, CHECK_DLG_DELAY);
        }
    };

    /**
     * 保存当前与历史绑定事件Presistent
     */
    public void setEventBindings(JSONArray bindings) {
        if (bindings != null) {
            final Message msg =
                    mMessageThreadHandler.obtainMessage(ViewCrawler.MESSAGE_EVENT_BINDINGS_RECEIVED);
            msg.obj = bindings;
            mMessageThreadHandler.sendMessage(msg);
        }
    }

    public void sendEventToSocketServer(JSONObject eventInfo) {
        final Message message = mMessageThreadHandler.obtainMessage
                (MESSAGE_HANDLE_SEND_EVENT_SERVER);
        message.obj = eventInfo;
        mMessageThreadHandler.sendMessage(message);
    }

    public void connectToEditor() {
        mMessageThreadHandler.connectToEditor();
        if (!mMessageThreadHandler.isConnected()) {
            mSensorHelper.installConnectionSensor();
        }
    }

    private class EmulatorConnector implements Runnable {
        private volatile boolean mStopped;

        public EmulatorConnector() {
            //            mStopped = true;
            mStopped = false;
        }

        @Override
        public void run() {
            if (!mStopped) {
                final Message message = mMessageThreadHandler.obtainMessage
                        (MESSAGE_CONNECT_TO_EDITOR);
                mMessageThreadHandler.sendMessage(message);
            }

            mMessageThreadHandler.postDelayed(this, EMULATOR_CONNECT_ATTEMPT_INTERVAL_MILLIS);
        }

        public void start() {
            mStopped = false;
            mMessageThreadHandler.post(this);
        }

        public void stop() {
            mStopped = true;
            mMessageThreadHandler.removeCallbacks(this);
        }
    }

    private ActivityLifecycleUtils.BaseLifecycleCallback mLifecycleCallback = new ActivityLifecycleUtils.BaseLifecycleCallback() {
        @Override
        public void onActivityResumed(Activity activity) {
            mEditState.onActivityResumed();
        }
    };

    private class SensorHelper implements FlipGesture.OnFlipGestureListener {

        private final FlipGesture mFlipGesture;
        private final EmulatorConnector mEmulatorConnector;
        private boolean mIsSensorRegistered;

        public SensorHelper() {
            mFlipGesture = new FlipGesture(this);
            mEmulatorConnector = new EmulatorConnector();
            installConnectionSensor();
            if (ActivityLifecycleUtils.getCurrentActivity() != null) {
                mEditState.onActivityResumed();
            }
        }

        @Override
        public void onFlipGesture() {
            uninstallConnectionSensor();
            final Message message = mMessageThreadHandler.obtainMessage(MESSAGE_CONNECT_TO_EDITOR);
            mMessageThreadHandler.sendMessage(message);
        }

        private void installConnectionSensor() {
            if (isInEmulator()) {
                mEmulatorConnector.start();
            } else if (!(mIsSensorRegistered || !VisualAgent.isInDebug())) {
                final SensorManager sensorManager =
                        (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
                final Sensor accelerometer =
                        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                sensorManager.registerListener(mFlipGesture, accelerometer,
                        SensorManager.SENSOR_DELAY_NORMAL);
                mIsSensorRegistered = true;
            }
            ActivityLifecycleUtils.addCallback(mLifecycleCallback);
        }

        private void uninstallConnectionSensor() {
            if (isInEmulator()) {
                mEmulatorConnector.stop();
            } else if (mIsSensorRegistered && VisualAgent.isInDebug()) {
                final SensorManager sensorManager = (SensorManager) mContext.getSystemService
                        (Context.SENSOR_SERVICE);
                sensorManager.unregisterListener(mFlipGesture);
                mFlipGesture.reset();
                mIsSensorRegistered = false;
            }
            ActivityLifecycleUtils.removeCallback(mLifecycleCallback);
        }

        private boolean isInEmulator() {
            if (!"goldfish".equals(Build.HARDWARE.toLowerCase()) && !"ranchu".equals(Build.HARDWARE.toLowerCase())) {
                return false;
            }

            if (!Build.BRAND.toLowerCase().startsWith("generic") && !"android".equals(Build.BRAND.toLowerCase())
                    && !"google".equals(Build.BRAND.toLowerCase())) {
                return false;
            }

            if (!Build.DEVICE.toLowerCase().startsWith("generic")) {
                return false;
            }

            if (!Build.PRODUCT.toLowerCase().contains("sdk")) {
                return false;
            }

            if (!Build.MODEL.toLowerCase(Locale.US).contains("sdk")) {
                return false;
            }

            return true;
        }
    }

    private class ViewCrawlerHandler extends Handler {

        private final Lock mStartLock;
        private final EditProtocol mProtocol;
        private final Map<String, EgPair<String, JSONObject>> mEditorEventBindings;
        private final Set<EgPair<String, JSONObject>> mPersistentEventBindings;
        private final Set<EgPair<String, JSONObject>> mOriginalEventBindings;
        private EditorConnection mEditorConnection;
        private ViewSnapshot mSnapshot;

        public ViewCrawlerHandler(Context context, Looper looper) {
            super(looper);
            mSnapshot = null;
            String resourcePackage = context.getPackageName();
            final ResourceIds resourceIds = new BaseResourceReader.Ids(resourcePackage, context);

            mProtocol = new EditProtocol(resourceIds);
            mOriginalEventBindings = new HashSet<>();
            mEditorEventBindings = new HashMap<>();
            mPersistentEventBindings = new HashSet<>();
            mStartLock = new ReentrantLock();
            mStartLock.lock();
        }

        public void start() {
            mStartLock.unlock();
        }

        @Override
        public void handleMessage(Message msg) {

            mStartLock.lock();
            try {
                final int what = msg.what;
                switch (what) {
                    case MESSAGE_INITIALIZE_CHANGES:
                        loadKnownChanges();
                        break;
                    case MESSAGE_CONNECT_TO_EDITOR:
                        connectToEditor();
                        if (!isConnected()) {
                            mSensorHelper.installConnectionSensor();
                        }
                        break;
                    case MESSAGE_SEND_DEVICE_INFO:
                        sendDeviceInfo();
                        break;
                    case MESSAGE_SEND_STATE_FOR_EDITING:
                        sendSnapshot((JSONObject) msg.obj);
                        break;
                    case MESSAGE_EVENT_BINDINGS_RECEIVED:
                        handleEventBindingsReceived((JSONArray) msg.obj);
                        break;
                    //event_binding_request事件触发
                    case MESSAGE_HANDLE_EDITOR_BINDINGS_RECEIVED:
                        handleEditorBindingsReceived((JSONObject) msg.obj);
                        break;
                    case MESSAGE_HANDLE_EDITOR_CLOSED:
                        handleEditorClosed();
                    case MESSAGE_HANDLE_SEND_EVENT_SERVER:
                        //新加入逻辑,主动上发埋点事件至socketServer
                        handleEventInfoToServer((JSONObject) msg.obj);
                        break;
                    default:
                        break;
                }
            } catch (Throwable e) {
            } finally {
                mStartLock.unlock();
            }
        }

        /**
         * Load the experiment ids and variants already in persistent storage into
         * into our set of seen experiments, so we don't double track them.
         * <p>
         * Load stored changes (AB, tweaks and event bindings) from persistent storage.
         */
        private void loadKnownChanges() {
            final SharedPreferences preferences = getSharedPreferences();
            final String storedBindings = preferences.getString(SHARED_PREF_BINDINGS_KEY, null);
            mPersistentEventBindings.clear();
            loadEventBindings(storedBindings);
            applyEventBindings();
        }

        private void loadEventBindings(String eventBindings) {
            if (!TextUtils.isEmpty(eventBindings)) {
                try {
                    final JSONArray bindings = new JSONArray(eventBindings);
                    mPersistentEventBindings.clear();
                    for (int i = 0; i < bindings.length(); i++) {
                        final JSONObject event = bindings.getJSONObject(i);
                        final String targetActivity = EGJSONUtils.optionalStringKey(event,
                                "target_page");
                        mPersistentEventBindings.add(new EgPair<>(targetActivity, event));
                    }
                } catch (Throwable ignore) {
                    InternalAgent.i(TAG, "JSON error when loading event bindings, clearing " +
                            "persistent " + "memory", ignore);
                    final SharedPreferences preferences = getSharedPreferences();
                    final SharedPreferences.Editor editor = preferences.edit();
                    editor.remove(SHARED_PREF_BINDINGS_KEY);
                    editor.apply();
                }
            }
        }

        /**
         * Try to connect to the remote interactive editor, if a connection does not already exist.
         */
        private void connectToEditor() {
            if (mEditorConnection != null && mEditorConnection.isValid()) {
                InternalAgent.v(TAG, "There is already a valid connection to an events editor.");
                return;
            }

            Socket sslSocket = null;
            final String url = InternalAgent.getString(mContext, Constants.SP_DEBUG_VISUAL_URL, "");
            if (url.startsWith("wss")) {
                SSLSocketFactory foundSSLFactory;
                try {
                    final SSLContext sslContext = SSLContext.getInstance("TLS");
                    sslContext.init(null, null, null);
                    foundSSLFactory = sslContext.getSocketFactory();
                } catch (Throwable ignore) {
                    InternalAgent.i(TAG, "System has no SSL support. Built-in events editor will " +
                            "not be " +
                            "available", ignore);
                    foundSSLFactory = null;
                }
                final SSLSocketFactory socketFactory = foundSSLFactory;
                if (null == socketFactory) {
                    InternalAgent.v(TAG, "SSL is not available on this device, no connection will" +
                            " be " +
                            "attempted to the events editor.");
                    return;
                }
                try {
                    sslSocket = socketFactory.createSocket();
                } catch (Throwable ignore) {
                    InternalAgent.e(ignore);
                }
            } else {
                final SocketFactory socketFactory = SocketFactory.getDefault();
                try {
                    sslSocket = socketFactory.createSocket();
                } catch (Throwable ignore) {
                    InternalAgent.e(ignore);
                }
            }
            InternalAgent.d(TAG, "WebSocket url: " + url);
            try {
                mEditorConnection = new EditorConnection(new URI(url), new Editor(), sslSocket);
            } catch (final URISyntaxException e) {
                InternalAgent.e(TAG, "Error parsing URI " + url + " for editor WebSocket", e);
            } catch (final EditorConnection.EditorConnectionException e) {
                InternalAgent.e(TAG, "Error connecting to URI " + url, e);
            } catch (Throwable ignore) {
                InternalAgent.e(ignore);
            }
        }

        /**
         * Send a string error message to the connected web UI.
         */
        private void sendError(String errorMessage) {
            if (mEditorConnection == null || !mEditorConnection.isValid() || !mEditorConnection
                    .isConnected()) {
                return;
            }

            final JSONObject errorObject = new JSONObject();
            try {
                errorObject.put("error_message", errorMessage);
            } catch (Throwable ignore) {
                InternalAgent.e(TAG, "Apparently impossible JSONException", ignore);
            }

            final OutputStreamWriter writer = new OutputStreamWriter(mEditorConnection
                    .getBufferedOutputStream());
            try {
                writer.write("{\"type\": \"error\", ");
                writer.write("\"payload\": ");
                writer.write(errorObject.toString());
                writer.write("}");
            } catch (Throwable ignore) {
                InternalAgent.e(TAG, "Can't write error message to editor", ignore);
            } finally {
                try {
                    writer.close();
                } catch (Throwable ignore) {
                    InternalAgent.e(TAG, "Could not close output writer to editor", ignore);
                }
            }
        }

        /**
         * Report on device info to the connected web UI.
         */
        private void sendDeviceInfo() {
            if (mEditorConnection == null || !mEditorConnection.isValid() || !mEditorConnection
                    .isConnected()) {
                return;
            }

            final OutputStream out = mEditorConnection.getBufferedOutputStream();
            final JsonWriter j = new JsonWriter(new OutputStreamWriter(out));

            try {
                j.beginObject();
                j.name("type").value("device_info_response");
                j.name("payload").beginObject();
                j.name("device_type").value("Android");
                j.name("device_name").value(Build.BRAND + "/" + Build.MODEL);
                j.name("scaled_density").value(mScaledDensity);
                j.name("width").value(width);
                j.name("height").value(height);
                for (final Map.Entry<String, String> entry : mDeviceInfo.entrySet()) {
                    j.name(entry.getKey()).value(entry.getValue());
                }
                j.endObject(); // payload
                j.endObject();
            } catch (Throwable ignore) {
                InternalAgent.e(TAG, "Can't write device_info to server", ignore);
            } finally {
                try {
                    j.close();
                } catch (Throwable ignore) {
                    InternalAgent.e(TAG, "Can't close websocket writer", ignore);
                }
            }
        }

        /**
         * Send a snapshot response, with crawled views and screenshot image, to the connected
         * web UI.
         */
        private void sendSnapshot(JSONObject message) {
            final long startSnapshot = System.currentTimeMillis();
            try {
                final JSONObject payload = message.getJSONObject("payload");
                if (payload.has("config")) {
                    mSnapshot = mProtocol.readSnapshotConfig(payload);
                    InternalAgent.v(TAG, "Initializing snapshot with configuration");
                }
            } catch (final JSONException e) {
                InternalAgent.e(TAG, "Payload with snapshot config required with snapshot " +
                        "request", e);
                sendError("Payload with snapshot config required with snapshot request");
                return;
            } catch (final EditProtocol.BadInstructionsException e) {
                InternalAgent.e(TAG, "Editor sent malformed message with snapshot request", e);
                sendError(e.getMessage());
                return;
            } catch (Throwable ignore) {
                return;
            }

            if (null == mSnapshot) {
                sendError("No snapshot configuration (or a malformed snapshot configuration) was " +
                        "sent.");
                InternalAgent.e(TAG, "editor is misconfigured, sent a snapshot request without a" +
                        " valid configuration.");
                return;
            }
            // ELSE config is valid:



            final OutputStream out = mEditorConnection.getBufferedOutputStream();
            final OutputStreamWriter writer = new OutputStreamWriter(out);
            try {

                writer.write("{");
                writer.write("\"type\": \"snapshot_response\",");
                writer.write("\"payload\": {");
                {
                    writer.write("\"activities\":");
                    writer.flush();
                    mSnapshot.snapshots(out);
                }

                final long snapshotTime = System.currentTimeMillis() - startSnapshot;
                writer.write(",\"snapshot_time_millis\": ");
                writer.write(Long.toString(snapshotTime));

                writer.write("}"); // } payload
                writer.write("}"); // } whole message
                writer.flush();
            } catch (final Throwable ignore) {
                InternalAgent.e(TAG, "Can't write snapshot request to server", ignore);
            } finally {
                try {
                    writer.close();
                } catch (Throwable ignore) {
                    InternalAgent.e(TAG, "Can't close writer.", ignore);
                }
            }
        }

        /**
         * Accept and apply a persistent event binding from a non-interactive source.
         */
        private void handleEventBindingsReceived(JSONArray eventBindings) {
            final SharedPreferences preferences = getSharedPreferences();
            final SharedPreferences.Editor editor = preferences.edit();
            editor.putString(SHARED_PREF_BINDINGS_KEY, eventBindings.toString());
            editor.apply();

            loadEventBindings(eventBindings.toString());

            applyEventBindings();
        }

        /**
         * Accept and apply a temporary event binding from the connected UI.
         */
        private void handleEditorBindingsReceived(JSONObject message) {
            JSONArray temp;
            String recordtype;
            try {
                recordtype = message.optString(WS_KEY_EVENT_TYPE);
                // 埋点下发消息用于绑定控件
                final JSONObject payload = message.getJSONObject(WS_KEY_PAYLOAD);
                temp = payload.getJSONArray(WS_KEY_EVENTS);
            } catch (Throwable ignore) {
                InternalAgent.e(TAG, "Bad event bindings received", ignore);
                return;
            }

            if (temp != null && temp.length() == 0) {
                mMemoryPathAndJson = new HashMap<>();
            }
            if (mMemoryPathAndJson == null) {
                mMemoryPathAndJson = new HashMap<>();
            }

            if (temp != null && temp.length() > 0) {
                for (int i = 0; i < temp.length(); i++) {
                    JSONObject obj = temp.optJSONObject(i);
                    if (obj != null & obj.length() > 0) {
                        if (obj.has("path")) {
                            String path = obj.optString("path");
                            if (WS_EVENTINFO_DOWN.equals(recordtype)) {
                                mMemoryPathAndJson.put(path, obj);
                            } else if (WS_EVENTINFO_APPEND.equals(recordtype)) {
                                mMemoryPathAndJson.put(path, obj);
                            } else if (WS_EVENTINFO_UPDATE.equals(recordtype)) {
                                mMemoryPathAndJson.put(path, obj);
                            } else if (WS_EVENTINFO_DELETE.equals(recordtype)) {
                                mMemoryPathAndJson.remove(path);
                            } else {
                                //low then v4.0.6
                                if (obj.has(WS_EVENTINFO_DELETE)) {
                                    mMemoryPathAndJson.remove(path);
                                } else {
                                    mMemoryPathAndJson.put(path, obj);
                                }
                            }
                        }
                    }
                }
            }

            JSONArray tempArr = new JSONArray();
            if (mMemoryPathAndJson.size() > 0) {
                for (Map.Entry<String, JSONObject> vo : mMemoryPathAndJson.entrySet()) {
                    JSONObject value = vo.getValue();
                    if (value != null) {
                        tempArr.put(value);
                    }
                }
            }

            final int eventCount = tempArr.length();

            mEditorEventBindings.clear();
            if (!mPersistentEventBindings.isEmpty() && mOriginalEventBindings.isEmpty()) {
                mOriginalEventBindings.addAll(mPersistentEventBindings);
                for (EgPair<String, JSONObject> eventBinding : mPersistentEventBindings) {
                    try {
                        mEditorEventBindings.put(eventBinding.second.get("path").toString(),
                                eventBinding);
                    } catch (Throwable ignore) {
                        InternalAgent.e(ignore);
                    }
                }
                mPersistentEventBindings.clear();
            }

            for (int i = 0; i < eventCount; i++) {
                try {
                    final JSONObject event = tempArr.getJSONObject(i);
                    final String targetActivity = EGJSONUtils.optionalStringKey(event,
                            "target_page");
                    mEditorEventBindings.put(event.get("path").toString(), new EgPair<>(targetActivity, event));
                } catch (Throwable ignore) {
                    InternalAgent.e(TAG, "Bad event binding received from editor in " + tempArr
                            .toString(), ignore);
                }
            }

            applyEventBindings();
        }

        /**
         * Clear state associated with the editor now that the editor is gone.
         */
        private void handleEditorClosed() {
            mSensorHelper.installConnectionSensor();
            mEditorEventBindings.clear();
            mPersistentEventBindings.addAll(mOriginalEventBindings);
            mOriginalEventBindings.clear();

            // Free (or make available) snapshot memory
            mSnapshot = null;
            InternalAgent.v(TAG, "Editor closed- freeing snapshot");
            mEditorConnection.close();
            applyEventBindings();
        }

        private boolean isConnected() {
            return mEditorConnection != null && mEditorConnection.isConnected();
        }

        /**
         * Reads our JSON-stored edits from memory and submits them to our EditState. Overwrites
         * any existing edits at the time that it is run.
         * <p>
         * applyEventBindings should be called any time we load event bindings,
         * from disk or when we receive new edits from event bindings from our persistent storage
         */
        private void applyEventBindings() {
            final List<EgPair<String, BaseViewVisitor>> newVisitors = new ArrayList<>();
            {
                if (mEditorEventBindings.size() == 0 && mOriginalEventBindings.size() == 0) {
                    for (EgPair<String, JSONObject> changeInfo : mPersistentEventBindings) {
                        try {
                            final BaseViewVisitor visitor =
                                    mProtocol.readEventBinding(changeInfo.second,
                                            mDynamicEventTracker);
                            newVisitors.add(new EgPair<>(changeInfo.first,
                                    visitor));
                        } catch (final EditProtocol.InapplicableInstructionsException e) {
                            InternalAgent.i(TAG, e.getMessage());
                        } catch (final EditProtocol.BadInstructionsException e) {
                            InternalAgent.e(TAG, "Bad persistent event binding cannot be applied" +
                                    ".", e);
                        } catch (Throwable ignore) {
                        }
                    }
                }
            }

            {
                for (EgPair<String, JSONObject> changeInfo : mEditorEventBindings.values()) {
                    try {
                        final BaseViewVisitor visitor =
                                mProtocol.readEventBinding(changeInfo.second, mDynamicEventTracker);
                        newVisitors.add(new EgPair<>(changeInfo.first,
                                visitor));
                    } catch (final EditProtocol.InapplicableInstructionsException e) {
                        InternalAgent.i(TAG, e.getMessage());
                    } catch (final EditProtocol.BadInstructionsException e) {
                        InternalAgent.e(TAG, "Bad editor event binding cannot be applied.", e);
                    } catch (Throwable ignore) {
                    }
                }
            }

            final Map<String, List<BaseViewVisitor>> editMap = new HashMap<>();
            final int totalEdits = newVisitors.size();
            mContainsDlgConfig = false;
            for (int i = 0; i < totalEdits; i++) {
                final EgPair<String, BaseViewVisitor> next = newVisitors.get(i);
                final List<BaseViewVisitor> mapElement;
                if (next.first != null && next.first.endsWith(UIHelper.DIALOG_SUFFIX)) {
                    mContainsDlgConfig = true;
                }
                if (editMap.containsKey(next.first)) {
                    mapElement = editMap.get(next.first);
                } else {
                    mapElement = new ArrayList<>();
                    editMap.put(next.first, mapElement);
                }
                mapElement.add(next.second);
            }

            mEditState.setEdits(editMap);
        }

        private void handleEventInfoToServer(JSONObject eventInfo) {
            if (mEditorConnection == null || !mEditorConnection.isValid() || !mEditorConnection
                    .isConnected()) {
                return;
            }

            final OutputStream out = mEditorConnection.getBufferedOutputStream();
            final OutputStreamWriter writer = new OutputStreamWriter(out);

            try {
                writer.write(eventInfo.toString());
                writer.flush();
            } catch (Throwable ignore) {
                InternalAgent.e(TAG, "Can't write event_info to server", ignore);
            } finally {
                try {
                    writer.close();
                } catch (Throwable ignore) {
                    InternalAgent.e(TAG, "Can't close websocket writer", ignore);
                }
            }
        }

        private SharedPreferences getSharedPreferences() {
            return mContext.getSharedPreferences(SHARED_PREF_EDITS_FILE, Context.MODE_PRIVATE);
        }
    }

    private class Editor implements EditorConnection.Editor {

        @Override
        public void sendSnapshot(JSONObject message) {
            final Message msg = mMessageThreadHandler.obtainMessage(ViewCrawler
                    .MESSAGE_SEND_STATE_FOR_EDITING);
            msg.obj = message;
            mMessageThreadHandler.sendMessage(msg);
        }

        @Override
        public void bindEvents(JSONObject message) {
            final Message msg = mMessageThreadHandler.obtainMessage(ViewCrawler
                    .MESSAGE_HANDLE_EDITOR_BINDINGS_RECEIVED);
            msg.obj = message;
            mMessageThreadHandler.sendMessage(msg);
        }

        @Override
        public void sendDeviceInfo() {
            final Message msg = mMessageThreadHandler.obtainMessage(ViewCrawler
                    .MESSAGE_SEND_DEVICE_INFO);
            mMessageThreadHandler.sendMessage(msg);
        }

        @Override
        public void cleanup() {
            final Message msg = mMessageThreadHandler.obtainMessage(ViewCrawler
                    .MESSAGE_HANDLE_EDITOR_CLOSED);
            mMessageThreadHandler.sendMessage(msg);
        }
    }
}
