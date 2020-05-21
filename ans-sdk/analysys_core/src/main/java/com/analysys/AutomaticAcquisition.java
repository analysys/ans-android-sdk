package com.analysys;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.view.ViewTreeObserver;

import com.analysys.deeplink.DeepLink;
import com.analysys.network.NetworkUtils;
import com.analysys.process.AgentProcess;
import com.analysys.process.HeatMap;
import com.analysys.process.SessionManage;
import com.analysys.userinfo.UserInfo;
import com.analysys.utils.AThreadPool;
import com.analysys.utils.ActivityLifecycleUtils;
import com.analysys.utils.AnalysysUtil;
import com.analysys.utils.CommonUtils;
import com.analysys.utils.Constants;
import com.analysys.utils.ExceptionUtil;
import com.analysys.utils.SharedUtil;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.HashMap;
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
//    private long appStartTime = 0;
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
                try {
                    switch (msg.what) {
                        case TRACK_APP_END:
                            // 上报数据
                            trackAppEnd(msg);
                            break;
                        case SAVE_END_INFO:
                            int count = SharedUtil.getInt(AnalysysUtil.getContext(),Constants.PAGE_COUNT,0);
                            if (count != 0) {
                                saveEndInfoCache();
                                sendEmptyMessageDelayed(SAVE_END_INFO, Constants.TRACK_END_INVALID);
                            }
                            break;
                        default:
                            break;
                    }
                } catch (Throwable ignore) {
                    ExceptionUtil.exceptionThrow(ignore);
                }

            }
        };
    }

    @Override
    public void onActivityCreated(final Activity activity, Bundle savedInstanceState) {
        AnalysysConfig config = AgentProcess.getInstance().getConfig();

        if (config.isAutoTrackClick()) {
            AnalysysUtil.onActivityCreated(activity);
        }

        if (config.isAutoHeatMap()) {
            initHeatMap(new WeakReference<>(activity));
        }
    }

    @Override
    public void onActivityStarted(final Activity activity) {
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
        AThreadPool.asyncMiddlePriorityExecutor(new Runnable() {
            @Override
            public void run() {
                SharedUtil.setString(activity.getApplicationContext(),
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
                    } catch (Throwable ignore) {
                        ExceptionUtil.exceptionThrow(ignore);
                    }
                    activity.getWindow().getDecorView().post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                HeatMap.getInstance()
                                        .hookDecorViewClick(activity.getWindow().getDecorView());
                            } catch (Throwable ignore) {
                                ExceptionUtil.exceptionThrow(ignore);
                            }
                        }
                    });
                }
            }
        };
    }

    private void appStart(final WeakReference<Activity> wr) {
        final long currentTime = System.currentTimeMillis();
        final long appStartTime = SystemClock.elapsedRealtime();
        AThreadPool.asyncMiddlePriorityExecutor(new Runnable() {
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
                            SharedUtil.setString(activity.getApplicationContext(),
                                    Constants.SP_LAST_PAGE_CHANGE, changeTime);

                            activityStart(context,appStartTime,currentTime);
                            pageView(activity,currentTime);
                        }
                    }
                } catch (Throwable ignore) {
                    ExceptionUtil.exceptionThrow(ignore);
                }
            }
        });
    }

    /**
     * 页面自动采集
     */
    private void pageView(Activity activity,long currentTime) throws Throwable {
        if (activity != null && canTrackPageView(activity.getClass().getName())) {
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
            AgentProcess.getInstance().autoCollectPageView(properties,currentTime);
        }
    }

    /**
     * 判断是否上报（黑白名单）
     */
    private boolean canTrackPageView(String nowPageName) {
        // 黑白名单策略
        AgentProcess instance = AgentProcess.getInstance();
        boolean isAuto = instance.getConfig().isAutoTrackPageView();
        if (isAuto) {
            if (instance.isThisPageInPageViewBlackList(nowPageName)) {
                return false;
            } else if (instance.hasAutoPageViewWhiteList()) {
                return instance.isThisPageInPageViewWhiteList(nowPageName);
            }
            return true;
        }
        return false;
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
        SharedUtil.setString(context, Constants.SP_REFER, refer);
    }

    private String getReferrer(Context context) {
        return SharedUtil.getString(context, Constants.SP_REFER,"");
    }

    /**
     * 第一次进来的时候和end结束的时候重置rer
     * @param context
     */
    private void resetReferrer(Context context) {
        SharedUtil.setString(context, Constants.SP_REFER, "");
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
                AThreadPool.asyncMiddlePriorityExecutor(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // 调用AppEnd
                            appEnd();
                        }catch (Throwable ignore) {
                            ExceptionUtil.exceptionThrow(ignore);
                        }
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
    private void activityStart(final Context context,final long appStartTime,final long currentTime) throws Exception{
        int count = SharedUtil.getInt(AnalysysUtil.getContext(),Constants.PAGE_COUNT,0);
        if (count == 0) {
            // 重置页面来源信息
            //resetReferrer(context);
            // 1.移除AppEnd delay 任务
            mHandler.removeMessages(TRACK_APP_END);
            // 2.判断session是否超时（30s）
            if (isAppEnd(context)) {
                // a.走AppEnd 尝试补发流程
                trackAppEnd(makeTrackAppEndMsg());
                AgentProcess.getInstance().appStart(fromBackground,currentTime);
                SharedUtil.setString(context,
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
        String lastOperateTime = SharedUtil.getString(context, Constants.LAST_OP_TIME,"");
        if (!CommonUtils.isEmpty(lastOperateTime)) {
            long aLong = CommonUtils.parseLong(lastOperateTime, 0);
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
            long time = SystemClock.elapsedRealtime();
            long appStartTime = 0;
//            if (appStartTime == 0) {
                // 获取应用启动时间
                String startTime = SharedUtil.getString(context, Constants.APP_START_TIME,"");
                if (!TextUtils.isEmpty(startTime)) {
                    appStartTime = CommonUtils.parseLong(startTime, 0);
                }
//            }
            long durationTime = time - appStartTime;
            if (durationTime < 0 || appStartTime <= 0) {
                durationTime = 0;
            }
            realTimeData.put(Constants.DURATION_TIME, durationTime);
            // 读取网络状态
            realTimeData.put(Constants.NETWORK_TYPE, NetworkUtils.networkType(context,true));
            // 是否首日启动
            realTimeData.put(Constants.FIRST_DAY, CommonUtils.isFirstDay(context));
            // 是否校验
            realTimeData.put(Constants.TIME_CALIBRATED, true);
            // 是否登录
            realTimeData.put(Constants.IS_LOGIN, UserInfo.getLoginState());
            // session id
            realTimeData.put(Constants.SESSION_ID,
                    SessionManage.getInstance(context).getSessionId());
            // 存储数据
            SharedUtil.setString(context, Constants.APP_END_INFO,
                    new String(Base64.encode(String.valueOf(realTimeData).getBytes(),
                            Base64.NO_WRAP)));
            // 存储最后一次操作时间
            SharedUtil.setString(context, Constants.LAST_OP_TIME, String.valueOf(System.currentTimeMillis()));
        } catch (Throwable ignore) {
            ExceptionUtil.exceptionThrow(ignore);
        }
    }


    /**
     * 页面应用关闭
     */
    private void appEnd() {
        pageViewDecrease();
        int count = SharedUtil.getInt(AnalysysUtil.getContext(),Constants.PAGE_COUNT,0);
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
    private synchronized void pageViewIncrease() throws Exception{
        int count = SharedUtil.getInt(AnalysysUtil.getContext(),Constants.PAGE_COUNT,0);
        count += 1;
        SharedUtil.setInt(AnalysysUtil.getContext(),Constants.PAGE_COUNT,count);
    }

    /**
     * 页面关闭 Count减少
     */
    private synchronized void pageViewDecrease() {
        int count = SharedUtil.getInt(AnalysysUtil.getContext(),Constants.PAGE_COUNT,0);
        count -= 1;
        if (count < 0) {
            count = 0;
        }
        SharedUtil.setInt(AnalysysUtil.getContext(),Constants.PAGE_COUNT,count);
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
        String endInfo = SharedUtil.getString(context, Constants.APP_END_INFO,"");
        if (!TextUtils.isEmpty(endInfo)) {
            bundle.putString(Constants.APP_END_INFO,
                    new String(Base64.decode(endInfo.getBytes(), Base64.DEFAULT)));
        }
        String time = SharedUtil.getString(context, Constants.LAST_OP_TIME,"");
        if(!TextUtils.isEmpty(time)) {
            bundle.putString(Constants.LAST_OP_TIME, time);
        }
        msg.setData(bundle);
        return msg;
    }

    /**
     * 上报AppEnd
     */
    private void trackAppEnd(Message msg) {
        try {
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
//                        CommonUtils.setIdFile(context, Constants.APP_END_INFO, null);
                        SharedUtil.remove(AnalysysUtil.getContext(),Constants.APP_END_INFO);

                        // 3. 清空页面来源信息
                        resetReferrer(context);
                    } catch (Throwable ignore) {
                        ExceptionUtil.exceptionThrow(ignore);
                    }
                }
            }
        } catch (Throwable ignore) {
            ExceptionUtil.exceptionThrow(ignore);
        }

    }
}
