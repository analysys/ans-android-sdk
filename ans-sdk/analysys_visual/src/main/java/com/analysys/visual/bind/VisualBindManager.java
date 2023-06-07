package com.analysys.visual.bind;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewTreeObserver;

import com.analysys.allgro.plugin.ASMProbeHelp;
import com.analysys.process.AgentProcess;
import com.analysys.ui.RootView;
import com.analysys.ui.WindowUIHelper;
import com.analysys.utils.ANSLog;
import com.analysys.utils.ActivityLifecycleUtils;
import com.analysys.utils.AnalysysUtil;
import com.analysys.utils.AnsReflectUtils;
import com.analysys.utils.CommonUtils;
import com.analysys.utils.ExceptionUtil;
import com.analysys.visual.VisualManager;
import com.analysys.visual.bind.event.EventFactory;
import com.analysys.visual.bind.event.impl.BaseEvent;
import com.analysys.visual.bind.event.impl.CallOnceAccessibilityListener;
import com.analysys.visual.bind.event.impl.EventAccessibilityBase;
import com.analysys.visual.bind.event.impl.EventWebView;
import com.analysys.visual.utils.VisualIpc;
import com.analysys.visual.utils.WebViewBindHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

/**
 * @Copyright © 2019 Analysys Inc. All rights reserved.
 * @Description: 可视化事件绑定管理类，主要功能：
 * * 1、调试模式下处理ws服务器下发的绑定指令
 * * 2、检测WindowManager根节点变化，绑定和解绑根节点
 * @Create: 2019-11-28 11:58
 * @author: hcq
 */
public class VisualBindManager {

    public static final String TAG = VisualManager.TAG;

    private static final VisualBindManager INSTANCE = new VisualBindManager();

    private boolean isEditing;

    private List<BaseEvent> EMPTY = new ArrayList<>();

    public static VisualBindManager getInstance() {
        return INSTANCE;
    }

    private VisualBindManager() {
    }

    /**
     * 当前绑定的页面根节点信息，必须在mHandler线程访问
     */
    private final List<RootView> mListRootView = new ArrayList<>();

    private boolean mInited;

    /**
     * 1、检测页面变化
     * 2、初始化event list
     */
    private void init() {
        ANSLog.i(TAG, "init visual bind");
        mWorkThread = new HandlerThread("visual_bind");
        mWorkThread.start();
        mHandler = new Handler(mWorkThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                handleMessageImpl(msg);
            }
        };

        ASMProbeHelp.getInstance().registerHookObserver(new VisualASMListener());
        boolean hook = hookWM();
        if (!hook) {
            ANSLog.i(TAG, "init window manager hook failed");
            ActivityLifecycleUtils.addCallback(mLifecycleCallback);
            if (WindowUIHelper.getAllWindowViews() != null) {
                ANSLog.i(TAG, "check window");
                Message msg = mHandler.obtainMessage(MSG_REFRESH_ROOT_VIEWS);
                mHandler.sendMessageDelayed(msg, REFRESH_ROOT_VIEWS_INTERVAL);
            }
        }

        // 初始化时可能已经有页面已经展示了，需要补绑事件
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    refreshRootViews();
                } catch (Throwable ignore) {
                    ExceptionUtil.exceptionThrow(ignore);
                }
            }
        });
    }

    /**
     * 替换WindowManagerGlobal的mViews
     * WindowManager如果hook成功，可以实时检测activity、dialog、float window、popup window等页面切换
     * 否则使用ActivityLifecycleCallback检测activity、使用定时检测其它类型页面
     */
    private boolean hookWM() {
        Object global = WindowUIHelper.getWindowManagerGlobal();
        if (global == null) {
            return false;
        }
        List views = WindowUIHelper.getGlobalViews(false);
        if (views == null) {
            return false;
        }
        NewArrayList newViews = new NewArrayList();
        newViews.addAll(views);
        return WindowUIHelper.setGlobalViews(global, newViews);
    }

    /**
     * 是否处于编辑状态
     */
    public boolean isEditing() {
        return isEditing;
    }

    /**
     * 设置编辑状态
     *
     * @param editing 编辑状态：使用ws返回数据，上报回显不上报数据，非编辑状态：使用configure接口返回数据，不上报回显上报数据
     */
    public void setEditing(boolean editing) {
        if (isEditing == editing) {
            return;
        }
        ANSLog.i(TAG, "setEditing: " + editing);
        isEditing = editing;
        setEventList(EMPTY);
        if (!isEditing) {
            loadConfigFromLocal();
        }
    }

    public void postRunnableAtFrontOfQueue(Runnable runnable) {
        if (mHandler != null) {
            mHandler.postAtFrontOfQueue(runnable);
        }
    }

    class NewArrayList extends ArrayList<View> {
        @Override
        public boolean add(View view) {
            if (!AgentProcess.getInstance().isDataCollectEnable()) {
                return super.add(view);
            }
            if (view != null) {
                RootView rootView = WindowUIHelper.getRootView(view);
                if (rootView != null) {
                    ANSLog.i(TAG, "new window attached");
                    requestReBindRootView(rootView);
                }
            }
            return super.add(view);
        }

        @Override
        public View remove(int index) {
            if (!AgentProcess.getInstance().isDataCollectEnable()) {
                return super.remove(index);
            }
            View view = get(index);
            if (view != null) {
                RootView rootView = WindowUIHelper.getRootView(view);
                if (rootView != null) {
                    ANSLog.i(TAG, "window removed");
                    requestUnbindRootView(rootView);
                }
            }
            return super.remove(index);
        }
    }

    /**
     * 最坏的情况下WindowManager hook失败、定时检测失效，使用ActivityLifecycleCallback保证activity能检测到
     */
    private final ActivityLifecycleUtils.BaseLifecycleCallback mLifecycleCallback = new ActivityLifecycleUtils.BaseLifecycleCallback() {

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            View contentView = activity.findViewById(android.R.id.content);
            if (contentView == null) {
                return;
            }
            RootView rootView = new RootView(contentView.getRootView(), WindowUIHelper.getActivityName(activity));
            ANSLog.i(TAG, "activity onCreate " + rootView.pageName);
            requestReBindRootView(rootView);
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            View contentView = activity.findViewById(android.R.id.content);
            if (contentView == null) {
                return;
            }
            RootView rootView = new RootView(contentView.getRootView(), WindowUIHelper.getActivityName(activity));
            ANSLog.i(TAG, "activity onDestroyed " + rootView.pageName);
            requestUnbindRootView(rootView);
        }
    };

    /**
     * asm 通知AccessibilityDelegate事件发生，判断是否需要触发
     */
    public void newAccessibilityEvent(View view, int eventType, boolean force) {
        if (view == null || !mInited) {
            return;
        }
        RootView rootView = WindowUIHelper.getRootView(view);
        if (rootView == null) {
            return;
        }
        int hashCode = rootView.hashCode;
        List<BaseEvent> listEvent = getEventList();
        for (BaseEvent event : listEvent) {
            if (!(event instanceof EventAccessibilityBase)) {
                continue;
            }
            EventAccessibilityBase accessibilityEvent = (EventAccessibilityBase) event;
            if (accessibilityEvent.getAccessibilityEventType() != eventType) {
                continue;
            }
            List<View> listView = event.getBindViews(hashCode);
            if (listView == null) {
                continue;
            }
            for (View bindView : listView) {
                if (bindView == view) {
                    View.AccessibilityDelegate bindDelegate = accessibilityEvent.getDelegate(hashCode, view);
                    // 只有极端情况下监听器被替换了才使用asm
                    if (!(bindDelegate instanceof CallOnceAccessibilityListener) || force) {
                        accessibilityEvent.call(view, eventType);
                    }
                    return;
                }
            }
        }
    }

    /**
     * 定时检测Window Views，主要是activity、dialog、float window、popup window
     */
    private void refreshRootViews() {
//        ANSLog.i(TAG, "refreshRootViews");
        List<RootView> listRootView = WindowUIHelper.getAllWindowViews();
        if (listRootView == null) {
            return;
        }

        // 找到已经移除的根节点
        List<RootView> listUnBind = new ArrayList<>();
        for (int i = mListRootView.size() - 1; i >= 0; i--) {
            RootView rootView = mListRootView.get(i);
            if (!listRootView.contains(rootView)) {
                listUnBind.add(rootView);
            }
        }

        // 解绑移除的根节点
        for (RootView rootView : listUnBind) {
            requestUnbindRootView(rootView);
        }

        // 绑定新的根节点
        for (RootView rootView : listRootView) {
            if (!mListRootView.contains(rootView)) {
                requestReBindRootView(rootView);
            }
        }
    }

    /**
     * 解绑页面，移除所有注册到该页面的事件监听器
     */
    private void unBindRootView(RootView rootView) {
        int idx = mListRootView.indexOf(rootView);
        if (idx < 0) {
            ANSLog.e(TAG, "unBindRootView not exists");
            return;
        }
        rootView = mListRootView.get(idx);
        ViewTreeObserver observer = rootView.view.getViewTreeObserver();
        if (observer.isAlive()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                observer.removeOnGlobalLayoutListener(rootView.globalLayoutListener);
            }
            observer.removeOnScrollChangedListener(rootView.scrollChangedListener);
        }
//        ANSLog.i(TAG, "-unbind root view " + rootView.pageName);

        // 清除进行中的绑定
        idx = -1;
        for (int i = 0; i < mListBindRunnable.size(); i++) {
            RootView rv = mListBindRunnable.get(i).mRootView;
            if (rv.equals(rootView)) {
                idx = i;
                break;
            }
        }
        if (idx >= 0) {
            Runnable runnable = mListBindRunnable.get(idx);
            mHandler.removeCallbacks(runnable);
            mListBindRunnable.remove(idx);
        }

        // 解绑
        List<BaseEvent> listEvent = getEventList();
        for (int i = 0; i < listEvent.size(); i++) {
            BaseEvent event = listEvent.get(i);
            if (!(event instanceof EventWebView)) {
                try {
                    event.unbind(rootView);
                } catch (Throwable ignore) {
                    if (ignore.getClass().getName().contains("CalledFromWrongThreadException")) {
                        changeToMainHandler();
                        i--;
                    } else {
                        ANSLog.i(TAG, "-unbind fail " + rootView.pageName + ", event list: " + listEvent.size());
                        ExceptionUtil.exceptionThrow(ignore);
                    }
                }
            }
        }
        // 清除当前页面的hybrid注入
        WebViewBindHelper.getInstance().clearHybridInPage(rootView.hashCode);
        mListRootView.remove(rootView);
    }

    /**
     * 延迟绑定列表
     */
    private final List<DelayBindRunnable> mListBindRunnable = new ArrayList<>();

    /**
     * 延迟绑定间隔时间
     */
    private static final int REBIND_ROOT_VIEW_INTERVAL = 200;

    /**
     * 延迟解绑，解决popup window里的元素点击后dismiss导致埋点无法上报的问题
     */
    private void requestUnbindRootView(final RootView rootView) {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    unBindRootView(rootView);
                } catch (Throwable ignore) {
                    ExceptionUtil.exceptionThrow(ignore);
                }
            }
        }, 500);
    }

    public void rebindCurrentActivityView() {
        if (!AgentProcess.getInstance().isDataCollectEnable() || !mInited) {
            return;
        }
        Activity activity = ActivityLifecycleUtils.getCurrentActivity();
        if (activity != null) {
            try {
                View view = activity.findViewById(android.R.id.content);
                if (view != null) {
                    RootView rootView = WindowUIHelper.getRootView(view.getRootView());
                    if (rootView != null) {
                        requestReBindRootView(rootView);
                    }
                }
            } catch (Throwable ignore) {
                ExceptionUtil.exceptionPrint(ignore);
            }
        }
    }

    private void requestReBindRootView(final RootView rootView) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    reBindRootView(rootView);
                } catch (Throwable ignore) {
                    ExceptionUtil.exceptionThrow(ignore);
                }
            }
        });
    }

    /**
     * 重新绑定一个页面
     * 绑定前在页面根节点加入GlobalLayout监听器，layout变化时重新绑定，页面显示初期onGlobalLayout可能会频繁回调
     * 因此采用延迟绑定机制，避免重复绑定影响性能
     */
    private void reBindRootView(final RootView rootView) {
        // 将rootView加入根节点列表中
        if (!mListRootView.contains(rootView)) {
            mListRootView.add(rootView);
            ViewTreeObserver observer = rootView.view.getViewTreeObserver();
            if (observer.isAlive()) {
                rootView.globalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        boolean isAlive = WindowUIHelper.isRootViewAlive(rootView.view);
                        if (isAlive) {
//                            ANSLog.i(TAG, "requestReBindRootView on global layout " + rootView.pageName);
                            requestReBindRootView(rootView);
                        }
                    }
                };
                observer.addOnGlobalLayoutListener(rootView.globalLayoutListener);

                // ListView、RecyclerView等变化时不会触发onGlobalLayout
                rootView.scrollChangedListener = new ViewTreeObserver.OnScrollChangedListener() {
                    @Override
                    public void onScrollChanged() {
                        requestReBindRootView(rootView);
                    }
                };
                observer.addOnScrollChangedListener(rootView.scrollChangedListener);
            }
        }

        // 没有事件的情况下不执行绑定
        if (isEventEmpty()) {
//            ANSLog.i(TAG, "reBindRootView empty event");
            return;
        }

        DelayBindRunnable runnable;
        int idx = -1;
        for (int i = 0; i < mListBindRunnable.size(); i++) {
            RootView rv = mListBindRunnable.get(i).mRootView;
            if (rv.equals(rootView)) {
                idx = i;
                break;
            }
        }
        // 已经存在相同的绑定，取消已经存在的绑定
        if (idx >= 0) {
            runnable = mListBindRunnable.get(idx);
            if (runnable.waitToLong()) {
                ANSLog.w(TAG, "reBindRootView wait too long: " + rootView.pageName);
            } else {
                // 消息队列中存在相同的绑定，取消原来的绑定
//                ANSLog.i(TAG, "reBindRootView repost runnable " + runnable.toString());
                mHandler.removeCallbacks(runnable);
                mHandler.postDelayed(runnable, REBIND_ROOT_VIEW_INTERVAL);
            }
        } else {
            runnable = new DelayBindRunnable(rootView, System.currentTimeMillis());
//            ANSLog.i(TAG, "reBindRootView add runnable: " + runnable.toString());
            mListBindRunnable.add(runnable);
            // 添加绑定消息
            mHandler.postDelayed(runnable, REBIND_ROOT_VIEW_INTERVAL);
        }
    }

    class DelayBindRunnable implements Runnable {
        private final RootView mRootView;
        private long mTime;

        DelayBindRunnable(RootView rootView, long time) {
            mRootView = rootView;
            mTime = time;
        }

        /**
         * 极端情况下onGlobalLayout频繁调用，runnable可能反复被取消导致事件不能绑定
         * 如果一个runnable诞生后超过5秒没有绑定，则强制绑定
         */
        boolean waitToLong() {
            return Math.abs(System.currentTimeMillis() - mTime) > 5000;
        }

        @Override
        public void run() {
            // 执行绑定
            List<BaseEvent> listEvent = getEventList();
            JSONArray jaHybrid = new JSONArray();
//            ANSLog.i(TAG, "-bind root view " + mRootView.pageName + ", " + this + ", event count: " + listEvent.size());
            for (int i = 0; i < listEvent.size(); i++) {
                BaseEvent event = listEvent.get(i);
                try {
                    if (event instanceof EventWebView) {
                        if (event.inRange(mRootView)) {
                            jaHybrid.put(new JSONObject(event.strEvent));
                        }
                    } else {
                        event.unbind(mRootView);
                        event.bind(mRootView);
                    }
                } catch (Throwable ignore) {
                    if (ignore.getClass().getName().contains("CalledFromWrongThreadException")) {
                        changeToMainHandler();
                        i--;
                    } else {
                        ANSLog.i(TAG, "-bind fail " + mRootView.pageName + ", " + this + ", event list: " + listEvent.size());
                        ExceptionUtil.exceptionThrow(ignore);
                    }
                }
            }
            if (jaHybrid.length() > 0) {
                WebViewBindHelper.getInstance().bindWebViewInPage(mRootView.view, jaHybrid.toString());
            }
            mListBindRunnable.remove(this);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof DelayBindRunnable) {
                DelayBindRunnable dbr = (DelayBindRunnable) obj;
                return dbr.mRootView.equals(mRootView);
            }
            return false;
        }
    }

    /**
     * 定时检测页面根节点消息，只有在Window Manager hook失败的情况下才会发送该消息
     */
    private static final int MSG_REFRESH_ROOT_VIEWS = 1;

    /**
     * 检测页面根节点间隔时间
     */
    private static final int REFRESH_ROOT_VIEWS_INTERVAL = 500;

    private final Handler mMainHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            handleMessageImpl(msg);
        }
    };

    private Handler mHandler;

    private HandlerThread mWorkThread;

    private void handleMessageImpl(Message msg) {
        if (!AgentProcess.getInstance().isDataCollectEnable()) {
            return;
        }
        if (msg.what == MSG_REFRESH_ROOT_VIEWS) {
            if (!isEventEmpty()) {
                try {
                    refreshRootViews();
                } catch (Throwable ignore) {
                    ExceptionUtil.exceptionThrow(ignore);
                }
            }
            msg = mHandler.obtainMessage(MSG_REFRESH_ROOT_VIEWS);
            mHandler.sendMessageDelayed(msg, REFRESH_ROOT_VIEWS_INTERVAL);
        }
    }

    private void changeToMainHandler() {
        if (mHandler != mMainHandler) {
            mWorkThread.quit();
            mHandler = mMainHandler;
            ANSLog.i(TAG, "--change to main thread");
        }
    }

//    /**
//     * 添加事件响应者
//     */
//    public void addEventAction(IEventAction action) {
//        synchronized (mEventActions) {
//            mEventActions.add(action);
//        }
//    }

    /**
     * 事件对象列表，使用setEventList和getEventList访问
     */
    private List<BaseEvent> mListEvent = new ArrayList<>();

    /**
     * 从SharedPreferences导入事件
     */
    public void loadConfigFromLocal() {
        if (isEditing) {
            return;
        }
        try {
            final SharedPreferences preferences = getSharedPreferences();
            final String storedEvents = preferences.getString(SHARED_PREF_BINDINGS_KEY, null);
            ANSLog.i(TAG, "load config from local");
            refreshEvents(storedEvents);
        } catch (Throwable ignore) {
            ANSLog.e(TAG, "load config from local fail: " + ignore.getMessage(), ignore);
            ExceptionUtil.exceptionPrint(ignore);
        }
    }

    /**
     * 保存事件到SharedPreferences
     */
    public void saveConfig(String eventBindings) {
        try {
            final SharedPreferences preferences = getSharedPreferences();
            preferences.edit().putString(SHARED_PREF_BINDINGS_KEY, eventBindings).apply();
        } catch (Throwable ignore) {
            ExceptionUtil.exceptionThrow(ignore);
        }
    }

    private final static String METHOD_GET = "GET";
    private final static int TIME_OUT_SEC = 30 * 1000;

    /**
     * 从服务获取埋点信息
     */
    public boolean loadConfigFromServer(final String strUrl) {
        try {
            HttpURLConnection cn;
            URL url = new URL(strUrl);
            if (strUrl.startsWith("https")) {
                cn = (HttpsURLConnection) url.openConnection();
                if (CommonUtils.getSSLSocketFactory(AnalysysUtil.getContext()) != null) {
                    ((HttpsURLConnection) cn).setSSLSocketFactory(CommonUtils.getSSLSocketFactory(AnalysysUtil.getContext()));
                } else {
                    ((HttpsURLConnection) cn).setHostnameVerifier(new HostnameVerifier() {
                        @Override
                        public boolean verify(String hostname, SSLSession session) {
                            return CommonUtils.verifyHost(hostname, strUrl);
                        }
                    });
                }
            } else {
                cn = (HttpURLConnection) url.openConnection();
            }
            cn.setRequestMethod(METHOD_GET);
            cn.setDoInput(true);
            cn.setConnectTimeout(TIME_OUT_SEC);
            cn.connect();
            InputStream is = cn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();
            String response = sb.toString();
            JSONObject jsonObject = new JSONObject(response);

            int code = jsonObject.optInt("code");
            if (code == 0) {
                String eventBindings = jsonObject.getString("data");
                if (ANSLog.isShowLog) {
                    showConfigureDataLog(jsonObject.getJSONArray("data"));
                }
//                ANSLog.i(TAG, "Get visual config list success: " + eventBindings);
                saveConfig(eventBindings);
                if (!isEditing) {
                    refreshEvents(eventBindings);
                    VisualIpc.getInstance().reloadVisualEventLocal();
                }
                return true;
            }
            ANSLog.e(TAG, "Get visual config list failed: " + response);
        } catch (Throwable ignore) {
            ANSLog.e(TAG, "Get visual config list failed: " + ignore.getMessage(), ignore);
            ExceptionUtil.exceptionPrint(ignore);
        }
        return false;
    }

    private void showConfigureDataLog(JSONArray data) throws JSONException {
        JSONArray jaLog = new JSONArray();
        for (int i = 0; i < data.length(); i++) {
            JSONObject jo = data.getJSONObject(i);
            String eventId = jo.optString("event_id", "");
            String eventName = jo.optString("event_name", "");
            JSONObject joLog = new JSONObject();
            joLog.put("event_id", eventId);
            joLog.put("event_name", eventName);
            jaLog.put(joLog);
        }
        ANSLog.i(TAG, "Get visual config list success: " + jaLog.toString());
    }

    private static final String SHARED_PREF_EDITS_FILE = "viewcrawler.sp";
    private static final String SHARED_PREF_BINDINGS_KEY = "viewcrawler.bindings";

    private SharedPreferences getSharedPreferences() {
        return AnalysysUtil.getContext().getSharedPreferences(SHARED_PREF_EDITS_FILE, Context.MODE_PRIVATE);
    }

    /**
     * 刷新事件列表
     */
    private void refreshEvents(String events) {
        if (TextUtils.isEmpty(events)) {
            ANSLog.i(TAG, "refreshEvents empty");
            return;
        }
        try {
            List<BaseEvent> listEvent = new ArrayList<>();
            JSONArray jaEvent = new JSONArray(events);
            for (int i = 0; i < jaEvent.length(); i++) {
                BaseEvent event = EventFactory.createEvent(EventFactory.RECORD_TYPE_ADD, jaEvent.getJSONObject(i));
                if (event != null) {
                    listEvent.add(event);
                }
            }
            setEventList(listEvent);
        } catch (Throwable ignore) {
            ANSLog.e(TAG, "refreshEvents json error: " + events, ignore);
            saveConfig(null);
            ExceptionUtil.exceptionThrow(ignore);
        }
    }

    /**
     * 编辑状态更新事件列表
     */
    public void updateEventsEditing(String data) {
        ANSLog.i(TAG, "update events: " + data);
        try {
            JSONObject message = new JSONObject(data);
            String recordType = message.optString(EventFactory.KEY_RECORD_TYPE);
            JSONObject payload = message.getJSONObject(EventFactory.KEY_PAYLOAD);
            JSONArray jaEvents = payload.getJSONArray(EventFactory.KEY_EVENTS);
            mergeEventsEditing(recordType, jaEvents);
        } catch (Throwable ignore) {
            ANSLog.e(TAG, "updateEvents json error", ignore);
            ExceptionUtil.exceptionThrow(ignore);
        }
    }

    private JSONArray mJAEditEvent = new JSONArray();

    public JSONArray getJAEditEvent() {
        return mJAEditEvent;
    }

    /**
     * 编辑状态合并事件列表
     */
    private void mergeEventsEditing(String recordType, JSONArray jaEventsMerge) {
        try {
            List<BaseEvent> listEventMerge = new ArrayList<>();
            JSONArray jaMerge = new JSONArray();
            for (int i = 0; i < jaEventsMerge.length(); i++) {
                JSONObject joEventMerge = jaEventsMerge.getJSONObject(i);
                BaseEvent event = EventFactory.createEvent(recordType, joEventMerge);
                if (event != null) {
                    listEventMerge.add(event);
                    jaMerge.put(joEventMerge);
                }
            }
            List<BaseEvent> listEvent = getEventList();
            if (!listEvent.isEmpty()) {
                doMergeEventList(listEvent, listEventMerge, mJAEditEvent, jaMerge);
            } else {
                listEvent = listEventMerge;
                mJAEditEvent = jaMerge;
            }

            setEventList(listEvent);
        } catch (Throwable ignore) {
            ANSLog.e(TAG, "mergeEvents json error", ignore);
            ExceptionUtil.exceptionThrow(ignore);
        }
    }

    private void doMergeEventList(List<BaseEvent> listEvent, List<BaseEvent> listEventMerge, JSONArray jaEvent, JSONArray jaMerge) throws JSONException {
        for (int i = 0; i < listEventMerge.size(); i++) {
            BaseEvent eventMerge = listEventMerge.get(i);
            JSONObject joEventMerge = jaMerge.getJSONObject(i);
            boolean find = false;
            for (int j = listEvent.size() - 1; j >= 0; j--) {
                BaseEvent event = listEvent.get(j);
                if (event.equals(eventMerge)) {
                    find = true;
                    if (eventMerge.recordType.equals(EventFactory.RECORD_TYPE_DELETE)) {
                        listEvent.remove(j);
                        List values = (List) AnsReflectUtils.getField(jaEvent, "values");
                        values.remove(j);
                    } else {
                        listEvent.set(j, eventMerge);
                        jaEvent.put(j, joEventMerge);
                    }
                    break;
                }
            }
            if (find) {
                continue;
            }
            if (!eventMerge.recordType.equals(EventFactory.RECORD_TYPE_DELETE)) {
                listEvent.add(eventMerge);
                jaEvent.put(joEventMerge);
            }
        }
    }

    /**
     * 判断事件列表是否为空，没有可视化埋点的情况下不做检测和绑定
     */
    private synchronized boolean isEventEmpty() {
        return mListEvent.isEmpty();
    }

    /**
     * 获取hybrid埋点列表
     */
    public String getHybridEventList() {
        List<BaseEvent> listEvent = getEventList();
        JSONArray ja = new JSONArray();
        for (BaseEvent event : listEvent) {
            if (event.availableForHybrid()) {
                try {
                    ja.put(new JSONObject(event.strEvent));
                } catch (Throwable ignore) {
                    ExceptionUtil.exceptionThrow(ignore);
                }
            }
        }
        return ja.toString();
    }

    /**
     * js sdk获取原生属性
     */
    public String getNativeProperty(Object view, String strRelate) {
        List<BaseEvent> listEvent = getEventList();
        for (BaseEvent event : listEvent) {
            if (event instanceof EventWebView) {
                return ((EventWebView) event).getNativeProperty(view, strRelate);
            }
        }
        return null;
    }

    /**
     * 获取事件列表，多线程同步且返回列表拷贝
     */
    private synchronized List<BaseEvent> getEventList() {
        return new ArrayList<>(mListEvent);
    }

    /**
     * 替换事件列表，多线程同步，替换前解绑所有原有事件
     */
    private synchronized void setEventList(final List<BaseEvent> listEvent) {
        if (listEvent == null) {
//            ANSLog.i(TAG, "setEventList null");
            return;
        }
        if (mListEvent.isEmpty() && listEvent.isEmpty()) {
//            ANSLog.i(TAG, "setEventList empty");
            return;
        }

        // 有可视化数据的前提下才启动绑定逻辑
        if (!mInited) {
            init();
            mInited = true;
        }

//        ANSLog.i(TAG, "setEventList new event count: " + listEvent.size());
        final List<BaseEvent> listEventExists = mListEvent;
//        ANSLog.i(TAG, "setEventList old event count: " + listEventExists.size());
        mListEvent = listEvent;

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    // 清除即将绑定的事件
                    for (DelayBindRunnable run : mListBindRunnable) {
//                        ANSLog.i(TAG, "setEventList remove exists runnable " + run.toString());
                        mHandler.removeCallbacks(run);
                    }
                    mListBindRunnable.clear();

                    // 解绑原有事件
                    boolean hasHybrid = false;
                    for (BaseEvent event : listEventExists) {
                        if (!(event instanceof EventWebView)) {
                            try {
                                event.unbindAll();
                            } catch (Throwable ignore) {
                                ExceptionUtil.exceptionThrow(ignore);
                            }
                        } else if (!hasHybrid) {
                            hasHybrid = true;
                        }
                    }

                    if (hasHybrid) {
                        WebViewBindHelper.getInstance().unBindAll();
                    }

                    if (mListEvent.isEmpty()) {
                        return;
                    }

                    // 重新绑定
//                    ANSLog.i(TAG, "setEventList rebind root view count: " + mListRootView.size());
                    for (RootView rView : mListRootView) {
                        reBindRootView(rView);
                    }
                } catch (Throwable ignore) {
                    ExceptionUtil.exceptionThrow(ignore);
                }
            }
        };
        mHandler.post(runnable);
    }

    public void report(BaseEvent event) {
        ANSLog.i(TAG, "Report event: " + event.eventId + ", properties: " + event.getProperties());
        VisualIpc.getInstance().reportVisualEvent(event.eventId, event.eventPageName, event.getProperties());
    }

    public void reportHybrid(String eventId, String eventInfo, String extraEditInfo) {
        try {
            if (TextUtils.isEmpty(eventId)) {
                return;
            }
            BaseEvent hybridEvent = null;
            List<BaseEvent> listEvent = getEventList();
            for (BaseEvent event : listEvent) {
                if (eventId.equals(event.eventId)) {
                    if (event instanceof EventWebView) {
                        ((EventWebView) event).setAllProperties(eventInfo, extraEditInfo);
                        hybridEvent = event;
                    }
                    break;
                }
            }
            if (hybridEvent != null) {
                report(hybridEvent);
            }
        } catch (Throwable ignore) {
            ExceptionUtil.exceptionThrow(ignore);
        }
    }

}
