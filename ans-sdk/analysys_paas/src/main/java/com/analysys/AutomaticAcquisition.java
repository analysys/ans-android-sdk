package com.analysys;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.view.ViewTreeObserver;

import com.analysys.deeplink.DeepLink;
import com.analysys.process.AgentProcess;
import com.analysys.process.HeatMap;
import com.analysys.process.SessionManage;
import com.analysys.utils.ANSLog;
import com.analysys.utils.ANSThreadPool;
import com.analysys.utils.ActivityLifecycleUtils;
import com.analysys.utils.CommonUtils;
import com.analysys.utils.Constants;
import com.analysys.utils.NumberFormat;
import com.analysys.utils.SharedUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Copyright © 2018 EGuan Inc. All rights reserved.
 * @Description: 自动采集页面信息
 * @Version: 1.0
 * @Create: 2018/3/4
 * @Author: Wang-X-C
 */
public class AutomaticAcquisition extends ActivityLifecycleUtils.BaseLifecycleCallback {

    private static final int TRACK_APP_END = 0x01;
    private static final int SAVE_END_INFO = TRACK_APP_END + 1;
    private String filePath = null;
    private Context context = null;
    /**
     * 应用启动时间
     */
    private long appStartTime = 0;
    private boolean fromBackground = false;
    private ViewTreeObserver.OnGlobalLayoutListener layoutListener;

    private HandlerThread mWorkThread = new HandlerThread("WorkThread");
    private Handler mHandler;

    public AutomaticAcquisition() {
        // 线程运行起来
        mWorkThread.start();
        mHandler = new Handler(mWorkThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case TRACK_APP_END:
                        // 上报数据
                        trackAppEnd(msg);
                        break;
                    case SAVE_END_INFO:
                        int count = CommonUtils.readCount(filePath);
                        if (count != 0) {
                            saveEndInfoCache();
                            sendEmptyMessageDelayed(SAVE_END_INFO, Constants.TRACK_END_INVALID);
                        }
                        break;
                    default:
                        break;
                }
            }
        };
    }

    @Override
    public void onActivityCreated(final Activity activity, Bundle savedInstanceState) {
        if (AgentProcess.getInstance().getConfig().isAutoHeatMap()) {
            initHeatMap(new WeakReference<>(activity));
        }
    }

    @Override
    public void onActivityStarted(final Activity activity) {
        ANSLog.e("");
        if (context == null) {
            context = activity.getApplicationContext();
        }
        appStart(new WeakReference<>(activity));
    }

    @Override
    public void onActivityResumed(Activity activity) {
        if (AgentProcess.getInstance().getConfig().isAutoHeatMap()) {
            checkLayoutListener(new WeakReference<>(activity), true);
        }
    }

    @Override
    public void onActivityPaused(final Activity activity) {
        ANSThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                CommonUtils.setIdFile(activity.getApplicationContext(),
                        Constants.SP_LAST_PAGE_CHANGE,
                        String.valueOf(System.currentTimeMillis()));
            }
        });

    }

    @Override
    public void onActivityStopped(Activity activity) {
        activityStop(new WeakReference<>(activity));
    }

    private void initHeatMap(final WeakReference<Activity> wa) {
        layoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (wa == null) {
                    return;
                }
                final Activity activity = wa.get();
                if (activity != null) {
                    try {
                        HeatMap.getInstance().initPageInfo(activity);
                    } catch (Throwable ignored) {
                    }
                    activity.getWindow().getDecorView().post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                HeatMap.getInstance()
                                        .hookDecorViewClick(activity.getWindow().getDecorView());
                            } catch (Throwable ignored) {
                            }
                        }
                    });
                }
            }
        };
    }

    private void appStart(final WeakReference<Activity> wr) {
        ANSThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (wr != null) {
                        Activity activity = wr.get();
                        if (activity != null) {
                            Context context = activity.getApplicationContext();
                            filePath = activity.getFilesDir().getAbsolutePath();
                            // 1.尝试切session
                            sessionManage(context, activity.getIntent());

                           String changeTime = String.valueOf(System.currentTimeMillis());
                            // 2.存lastPageChange
                            CommonUtils.setIdFile(activity.getApplicationContext(),
                                    Constants.SP_LAST_PAGE_CHANGE, changeTime);

                            activityStart(context);
                            pageView(activity);
                        }
                    }
                } catch (Throwable ignored) {
                }
            }
        });
    }

    /**
     * 页面自动采集
     */
    private void pageView(Activity activity) throws Exception {
        if (activity != null) {
            Map<String, Object> properties = getRegisterProperties(activity);
            if (!properties.containsKey(Constants.PAGE_URL)) {
                String pageUrl = activity.getClass().getCanonicalName();
                if (!TextUtils.isEmpty(pageUrl)) {
                    properties.put(Constants.PAGE_URL, pageUrl);
                }
            }
            if (!properties.containsKey(Constants.PAGE_TITLE)) {
                properties.put(Constants.PAGE_TITLE, activity.getTitle());
            }
            pageInfo(activity.getApplicationContext(),
                    activity.getClass().getCanonicalName(), properties);
        }
    }

    /**
     * 获取注册属性
     */
    private Map<String, Object> getRegisterProperties(Activity activity) {
        Map<String, Object> property;
        String pageUrl = null;
        if (activity instanceof ANSAutoPageTracker) {
            ANSAutoPageTracker autoPageTracker = (ANSAutoPageTracker) activity;
            property = CommonUtils.deepCopy(autoPageTracker.registerPageProperties());
            pageUrl = autoPageTracker.registerPageUrl();
        } else {
            property = new HashMap<>();
        }
        if (!CommonUtils.isEmpty(pageUrl)) {
            property.put(Constants.PAGE_URL, pageUrl);
        }
        if (!property.containsKey(Constants.PAGE_URL)) {
            pageUrl = activity.getClass().getCanonicalName();
            property.put(Constants.PAGE_URL, pageUrl);
        } else {
            pageUrl = String.valueOf(property.get(Constants.PAGE_URL));
        }
        String ref = getReferrer(activity.getApplicationContext());
        if (!CommonUtils.isEmpty(ref)) {
            property.put(Constants.PAGE_REFERRER, ref);
        }
        setReferrer(activity.getApplicationContext(), pageUrl);
        return property;
    }

    private void setReferrer(Context context, String refer) {
        CommonUtils.setIdFile(context, Constants.SP_REFER, refer);
    }

    private String getReferrer(Context context) {
        return CommonUtils.getIdFile(context, Constants.SP_REFER);
    }

    private void resetReferrer(Context context) {
        CommonUtils.setIdFile(context, Constants.SP_REFER, "");
    }


    private void activityStop(WeakReference<Activity> activity) {
        if (activity != null) {
            Context context = activity.get();
            if (context != null) {
                // 热图部分逻辑不能在子线程执行
                if (AgentProcess.getInstance().getConfig().isAutoHeatMap()) {
                    checkLayoutListener(activity, false);
                }
                // 单队列线程执行
                ANSThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        // 调用AppEnd
                        appEnd();
                    }
                });
            }
        }
    }

    private void checkLayoutListener(WeakReference<Activity> wr, boolean isResume) {
        if (wr != null) {
            Activity activity = wr.get();
            if (layoutListener != null) {
                View rootView = activity.findViewById(android.R.id.content);
                if (isResume) {
                    rootView.getViewTreeObserver().addOnGlobalLayoutListener(layoutListener);
                } else {
                    if (Build.VERSION.SDK_INT > 15) {
                        rootView.getViewTreeObserver().removeOnGlobalLayoutListener(layoutListener);
                    }
                }
            }
        }
    }

    private void sessionManage(Context context, Intent intent) {
        Constants.utm = DeepLink.getUtmValue(intent);
        SessionManage.getInstance(context).resetSession(
                !CommonUtils.isEmpty(Constants.utm));
    }


    /**
     * 发送应用启动消息
     */
    private void activityStart(final Context context) {
        int count = CommonUtils.readCount(filePath);
        if (count == 0) {
            // 重置页面来源信息
            resetReferrer(context);
            // 1.移除AppEnd delay 任务
            mHandler.removeMessages(TRACK_APP_END);
            // 2.判断session是否超时（30s）
            if (isAppEnd(context)) {
                // a.走AppEnd 尝试补发流程
                trackAppEnd(makeTrackAppEndMsg());
                appStartTime = System.currentTimeMillis();
                AgentProcess.getInstance().appStart(fromBackground, appStartTime);
                CommonUtils.setIdFile(context,
                        Constants.APP_START_TIME, String.valueOf(appStartTime));
                // 用于判断是否是后台启动
                if (!fromBackground) {
                    fromBackground = true;
                }
            }
            // 4.开启定时存储任务(10s)（实时记录最后一次操作/appEnd 信息）
            mHandler.sendEmptyMessageDelayed(SAVE_END_INFO, Constants.APPEND_TIMER_DELAY_MILLIS);
        }
        pageViewIncrease();
    }

    /**
     * 判断是否超时
     */
    private static boolean isAppEnd(Context context) {
        long invalid = 0;
        String lastOperateTime = CommonUtils.getIdFile(context, Constants.LAST_OP_TIME);
        if (!CommonUtils.isEmpty(lastOperateTime)) {
            long aLong = NumberFormat.convertToLong(lastOperateTime);
            invalid = Math.abs(aLong - System.currentTimeMillis());
        }
        return invalid == 0 || invalid > Constants.BG_INTERVAL_TIME;
    }

    /**
     * 制造一个AppEndInfo 信息
     * 1.一次设置不可修改（onStart）
     * 2.实时设置(定时任务、onStop)
     * 3.固定参数 trackAppEnd()
     */
    private void saveEndInfoCache() {
        try {
            // 1. 存储实时更新数据
            JSONObject realTimeData = new JSONObject();
            // 存储使用时长
            long time = System.currentTimeMillis();
            if (appStartTime == 0) {
                // 获取应用启动时间
                String startTime = CommonUtils.getIdFile(context, Constants.APP_START_TIME);
                if (startTime != null) {
                    appStartTime = Long.valueOf(startTime);
                }
            }
            realTimeData.put(Constants.DURATION_TIME, time - appStartTime);
            // 读取网络状态
            realTimeData.put(Constants.NETWORK_TYPE, CommonUtils.networkType(context));
            // 是否首日启动
            realTimeData.put(Constants.FIRST_DAY, CommonUtils.isFirstDay(context));
            // 是否校验
            realTimeData.put(Constants.TIME_CALIBRATED, true);
            // 是否登录
            realTimeData.put(Constants.IS_LOGIN, CommonUtils.getLogin(context));
            // session id
            realTimeData.put(Constants.SESSION_ID,
                    SessionManage.getInstance(context).getSessionId());
            // 存储数据
            CommonUtils.setIdFile(context, Constants.APP_END_INFO,
                    new String(Base64.encode(String.valueOf(realTimeData).getBytes(),
                            Base64.NO_WRAP)));
            // 存储最后一次操作时间
            CommonUtils.setIdFile(context, Constants.LAST_OP_TIME, String.valueOf(time));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 应用自动采集页面信息
     */
    private void pageInfo(Context context, String pageUrl,
                          Map<String, Object> pageInfo) throws Exception {
        boolean isAuto = SharedUtil.getBoolean(
                context, Constants.SP_IS_COLLECTION, true);
        if (isAuto && isAutomaticCollection(context, pageUrl)) {
            AgentProcess.getInstance().autoCollectPageView(pageInfo);
        }
    }

    /**
     * 页面是否忽略自动采集，false不自动采集，true自动采集
     */
    private boolean isAutomaticCollection(Context context, String nowPageName) {
        String activities = SharedUtil.getString(
                context, Constants.SP_IGNORED_COLLECTION, null);
        if (CommonUtils.isEmpty(activities)) {
            return true;
        }
        List<String> pageNames = CommonUtils.toList(activities);
        for (String pageName : pageNames) {
            if (nowPageName.equals(pageName)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 页面应用关闭
     */
    private void appEnd() {
        pageViewDecrease();
        int count = CommonUtils.readCount(filePath);
        // 最后一个页面
        if (count < 1) {
            // 1.关闭定时任务 （实时记录最后一次操作/appEnd 信息）
            mHandler.removeMessages(SAVE_END_INFO);
            // 修整EndInfo先关信息，使其更加精确
            saveEndInfoCache();
            // 2.发送AppEnd delay 任务
            mHandler.sendMessageDelayed(makeTrackAppEndMsg(), Constants.BG_INTERVAL_TIME);
        }
    }

    /**
     * 页面新增 Count增加
     */
    private void pageViewIncrease() {
        int count = CommonUtils.readCount(filePath);
        count += 1;
        CommonUtils.writeCount(filePath, String.valueOf(count));
    }

    /**
     * 页面关闭 Count减少
     */
    private void pageViewDecrease() {
        int count = CommonUtils.readCount(filePath);
        count -= 1;
        if (count < 0) {
            count = 0;
        }
        CommonUtils.writeCount(filePath, String.valueOf(count));
    }

    /**
     * 构建 TrackAppEnd Message
     *
     * @return Message
     */

    private Message makeTrackAppEndMsg() {
        Message msg = Message.obtain();
        msg.what = TRACK_APP_END;
        Bundle bundle = new Bundle();
        String endInfo = CommonUtils.getIdFile(context, Constants.APP_END_INFO);
        if (endInfo != null) {
            bundle.putString(Constants.APP_END_INFO,
                    new String(Base64.decode(endInfo.getBytes(), Base64.DEFAULT)));
        }
        String time = CommonUtils.getIdFile(context, Constants.LAST_OP_TIME);
        bundle.putString(Constants.LAST_OP_TIME, time);
        msg.setData(bundle);
        return msg;
    }

    /**
     * 上报AppEnd
     */
    private void trackAppEnd(Message msg) {
        Bundle bundle = msg.getData();
        if (bundle != null) {
            String opt = bundle.getString(Constants.LAST_OP_TIME);
            String endInfo = bundle.getString(Constants.APP_END_INFO);
            if (!TextUtils.isEmpty(opt) && !TextUtils.isEmpty(endInfo)) {
                try {
                    // 获取实时更新数据时间
                    JSONObject data = new JSONObject(endInfo);
                    // 1. appEndTrack
                    AgentProcess.getInstance().appEnd(opt, data);
                    // 2. 清空config中appEndInfoCache
                    CommonUtils.setIdFile(context, Constants.APP_END_INFO, null);
                    // 3. 清空页面来源信息
                    resetReferrer(context);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
