package com.analysys.process;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.util.LruCache;

import com.analysys.AnalysysConfig;
import com.analysys.AnsIDControl;
import com.analysys.AnsRamControl;
import com.analysys.AutomaticAcquisition;
import com.analysys.ObserverListener;
import com.analysys.easytouch.EasytouchProcess;
import com.analysys.hybrid.HybridBridge;
import com.analysys.network.UploadManager;
import com.analysys.push.PushListener;
import com.analysys.userinfo.UserInfo;
import com.analysys.utils.ANSLog;
import com.analysys.utils.AThreadPool;
import com.analysys.utils.ActivityLifecycleUtils;
import com.analysys.utils.AdvertisingIdUitls;
import com.analysys.utils.AnalysysUtil;
import com.analysys.utils.AnsReflectUtils;
import com.analysys.utils.CheckUtils;
import com.analysys.utils.CommonUtils;
import com.analysys.utils.Constants;
import com.analysys.utils.CrashHandler;
import com.analysys.utils.ExceptionUtil;
import com.analysys.utils.InternalAgent;
import com.analysys.utils.LogPrompt;
import com.analysys.utils.SharedUtil;

import org.json.JSONObject;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @Copyright © 2018 EGuan Inc. All rights reserved.
 * @Description: 数据处理分发
 * @Version: 1.0
 * @Create: 2018/2/3 17:31
 * @Author: Wang-X-C
 */
public class AgentProcess {

    private final String GET_SETTINGS = "getSettings";
    private final String GET_USER_AGENT = "getUserAgentString";
    private final String SET_USER_AGENT = "setUserAgentString";
    private final String START = "start";
    private final String ANALYSYS_AGENT = "analysysagent";


    private Application mApp = null;
    private String mTitle = "", mUrl = "";
    private Map<String, Object> properties;

    private AnalysysConfig mConfig = new AnalysysConfig();

    public static AgentProcess getInstance() {
        return Holder.INSTANCE;
    }


    /**
     * 只能初始化一次
     */
    private boolean mInited;

    /**
     * 设置网络上传策略
     *
     * @param networkType
     */
    public void setUploadNetworkType(int networkType) {
        AnsRamControl.getInstance().setUploadNetworkType(networkType);
    }

    /**
     * 获取网络上传策略
     *
     * @return
     */
    public int getUploadNetworkType() {
        return AnsRamControl.getInstance().getUploadNetworkType();
    }

    /**
     * 初始化接口 config,不调用初始化接口: 获取不到key/channel,页面自动采集失效,电池信息采集失效
     */
    public void init(final Context context, final AnalysysConfig config) {
        if (mInited) {
            ANSLog.e("多次调用AnalysysAgent.init，请保证只初始化一次");
            return;
        }
        mInited = true;
        AnalysysUtil.init(context);
        CrashHandler.getInstance().setCallback(new CrashHandler.CrashCallBack() {
            @Override
            public void onAppCrash(Throwable e) {
                CrashHandler.getInstance().reportException(context, e, CrashHandler.CrashType.crash_auto);
            }
        });
        if (config != null) {
            mConfig = config;
        }
        ActivityLifecycleUtils.initLifecycle();
        ActivityLifecycleUtils.addCallback(new AutomaticAcquisition());
        AThreadPool.initHighPriorityExecutor(new Callable() {
            @Override
            public Object call() throws Exception {
                try {
                    Context cx = AnalysysUtil.getContext();
                    if (cx != null) {
                        TemplateManage.initMould(cx);
                        saveKey(cx, mConfig.getAppKey());
                        saveChannel(cx, mConfig.getChannel());
                        if (CommonUtils.isMainProcess(cx)) {

                            //首次安装渠道归因状态设置，避免后面把状态冲了
                            Constants.isFirstInstall = CommonUtils.isFirstStart(context);

                            // 同时设置 UploadUrl/WebSocketUrl/ConfigUrl
                            setBaseUrl(cx, mConfig.getBaseUrl());
                            // 设置首次启动是否发送
                            Constants.isAutoProfile = mConfig.isAutoProfile();
                            // 设置加密类型
                            Constants.encryptType = mConfig.getEncryptType().getType();
                            // 设置渠道归因是否开启
                            Constants.autoInstallation = mConfig.isAutoInstallation();

                            //重启重置ID数据
                            AnsIDControl.resetID();

                            long MaxDiffTimeInterval = mConfig.getMaxDiffTimeInterval();
                            if (0 <= MaxDiffTimeInterval) {
                                // 用户忽略最大时间差值
                                Constants.ignoreDiffTime = mConfig.getMaxDiffTimeInterval();
                            }
                        }
                        // 设置时间校准是否开启
                        Constants.isTimeCheck = mConfig.isTimeCheck();
//                        if (AgentProcess.getInstance().getConfig().isAutoHeatMap()) {
//                            SystemIds.getInstance().parserId();
//                        }
                        LifeCycleConfig.initUploadConfig(cx);
                        LogPrompt.showInitLog(true);
                    } else {
                        LogPrompt.showInitLog(false);
                    }
                    AdvertisingIdUitls.setAdvertisingId();
                    return true;
                } catch (Throwable ignore) {
                    ExceptionUtil.exceptionThrow(ignore);
                    return true;
                }

            }
        });
    }

    public void setObserverListener(ObserverListener listener) {
        EasytouchProcess.getInstance().setObserverListener(listener);
    }
    /**
     * debug 信息处理
     */
    public void setDebug(final int debug) {
        AThreadPool.asyncHighPriorityExecutor(new Runnable() {
            @Override
            public void run() {
                Context context = AnalysysUtil.getContext();
                if (context == null || debug < 0 || 2 < debug) {
                    Log.w("analysys", Constants.API_SET_DEBUG_MODE + ": set failed!");
                    return;
                }
                debugResetUserInfo(context, debug);
                SharedUtil.setInt(context, Constants.SP_USER_DEBUG, debug);
                if (debug != 0) {
                    ANSLog.isShowLog = true;
                }
                LogPrompt.showLog(Constants.API_SET_DEBUG_MODE, true);
            }
        });
    }

    /**
     * 安装后首次启动/应用启动
     */
    public void appStart(final boolean isFromBackground,final long currentTime) {
        try {
            Context context = AnalysysUtil.getContext();
            if (context == null) {
                return;
            }
            HashMap<String, Object> startUpMap = new HashMap<>();
            startUpMap.put(Constants.DEV_IS_FROM_BACKGROUND, isFromBackground);
            if (!CommonUtils.isEmpty(Constants.utm)) {
                startUpMap.putAll(Constants.utm);
            }
            JSONObject eventData = DataAssemble.getInstance(context).getEventData(currentTime,
                    Constants.API_APP_START, Constants.STARTUP, null, startUpMap);
            trackEvent(context, Constants.API_APP_START, Constants.STARTUP, eventData);
            if (Constants.isFirstInstall) {
                Constants.isFirstInstall = false;
                sendProfileSetOnce(context, 0, currentTime);
                if (Constants.autoInstallation) {
                    sendFirstInstall(context, currentTime);
                }
            }
        } catch (Throwable ignore) {
            ExceptionUtil.exceptionThrow(ignore);
        }
    }

    /**
     * 应用关闭
     */
    public void appEnd(final String eventTime, final JSONObject realTimeField) {
        try {
            if (!TextUtils.isEmpty(eventTime)) {
                long time = CommonUtils.parseLong(eventTime, 0);
                if (time > 0) {
                    Context context = AnalysysUtil.getContext();
                    if (context != null && realTimeField != null) {
                        JSONObject endData = DataAssemble.getInstance(context).getEventData(time,
                                Constants.API_APP_END, Constants.END, null, null);
                        endData.put(Constants.X_WHEN, time);
                        JSONObject xContData = endData.optJSONObject(Constants.X_CONTEXT);
                        CommonUtils.mergeJson(realTimeField, xContData);
                        endData.put(Constants.X_CONTEXT, xContData);
                        trackEvent(context, Constants.API_APP_END, Constants.END, endData);
                    }
                }
            }
        } catch (Throwable ignored) {
            ExceptionUtil.exceptionThrow(ignored);
        }
    }

    /**
     * 页面信息处理
     */
    public void pageView(final Context context,
                         final String pageName, final Map<String, Object> pageDetail) {
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            mUrl = activity.getClass().getCanonicalName();
            mTitle = String.valueOf(activity.getTitle());
        }
        final long currentTime = System.currentTimeMillis();
        AThreadPool.asyncMiddlePriorityExecutor(new Runnable() {
            @Override
            public void run() {
                try {
                    Context context = AnalysysUtil.getContext();
                    if (context == null) {
                        return;
                    }
                    Map<String, Object> pageInfo = CommonUtils.deepCopy(pageDetail);
                    pageInfo.put(Constants.PAGE_TITLE, pageName);

                    Map<String, Object> autoCollectPageInfo = new HashMap<>();
                    if (!pageInfo.containsKey(Constants.PAGE_URL)) {
                        autoCollectPageInfo.put(Constants.PAGE_URL, mUrl);
                    }
                    if (!pageInfo.containsKey(Constants.PAGE_TITLE)) {
                        autoCollectPageInfo.put(Constants.PAGE_TITLE, mTitle);
                    }
                    JSONObject eventData = DataAssemble.getInstance(context).getEventData(
                            currentTime,Constants.API_PAGE_VIEW, Constants.PAGE_VIEW,
                            pageInfo, autoCollectPageInfo);

                    trackEvent(context, Constants.API_PAGE_VIEW, Constants.PAGE_VIEW, eventData);
                } catch (Throwable ignored) {
                    ExceptionUtil.exceptionThrow(ignored);
                }
            }
        });
    }

    /**
     * hybrid 使用 pageView 方法
     */
    public void hybridPageView(final String pageName, final Map<String, Object> pageDetail) {
        final long currentTime = System.currentTimeMillis();
        AThreadPool.asyncMiddlePriorityExecutor(new Runnable() {
            @Override
            public void run() {
                try {
                    Context context = AnalysysUtil.getContext();
                    if (context == null) {
                        return;
                    }
                    Map<String, Object> pageInfo = CommonUtils.deepCopy(pageDetail);
                    if (!CommonUtils.isEmpty(pageName) && pageDetail != null) {
                        pageDetail.put(Constants.PAGE_TITLE, pageName);
                    }
                    JSONObject eventData = DataAssemble.getInstance(context).getEventData(
                            currentTime,Constants.API_PAGE_VIEW,
                            Constants.PAGE_VIEW, pageInfo, null);
                    trackEvent(context,
                            Constants.API_PAGE_VIEW, Constants.PAGE_VIEW, eventData);
                } catch (Throwable ignore) {
                    ExceptionUtil.exceptionThrow(ignore);
                }
            }
        });
    }

    /**
     * 页面信息处理
     */
    public void autoCollectPageView(final Map<String, Object> pageInfo,final long currentTime) throws Throwable {
        Context context = AnalysysUtil.getContext();
        if (context != null) {
            JSONObject eventData = DataAssemble.getInstance(context).getEventData(
                    currentTime,Constants.API_PAGE_VIEW, Constants.PAGE_VIEW, pageInfo, null);
            trackEvent(context, Constants.API_PAGE_VIEW, Constants.PAGE_VIEW, eventData);
        }
    }

    /**
     * Touch事件处理
     */
    void pageTouchInfo(final Map<String, Object> screenDetail,final long currentTime) {
        try {
            Context context = AnalysysUtil.getContext();
            Map<String, Object> screenInfo = CommonUtils.deepCopy(screenDetail);
            if (context != null) {
                JSONObject eventData = DataAssemble.getInstance(context).getEventData(
                        currentTime,Constants.API_APP_CLICK, Constants.APP_CLICK, null, screenInfo);
                trackEvent(context, Constants.API_APP_CLICK, Constants.APP_CLICK, eventData);
            }
        } catch (Throwable ignore) {
            ExceptionUtil.exceptionThrow(ignore);
        }
    }

    /**
     * 点击自动上报
     *
     * @param clickInfo 点击上报信息
     */
    public void autoTrackViewClick(final Map<String, Object> clickInfo,final long currentTime) throws Throwable {
        Context context = AnalysysUtil.getContext();
        if (context != null) {
            JSONObject eventData = DataAssemble.getInstance(context).getEventData(currentTime,Constants.API_USER_CLICK, Constants.USER_CLICK, null, clickInfo);
            trackEvent(context, Constants.API_USER_CLICK, Constants.USER_CLICK, eventData);
        }
    }

    /**
     * 多个事件信息处理
     */
    public void track(final String eventName, final Map<String, Object> eventDetail) {
        final long currentTime = System.currentTimeMillis();
        AThreadPool.asyncLowPriorityExecutor(new Runnable() {
            @Override
            public void run() {
                try {
                    Map<String, Object> eventInfo = CommonUtils.deepCopy(eventDetail);
                    Context context = AnalysysUtil.getContext();
                    if (context == null || eventName == null) {
                        LogPrompt.showLog(Constants.API_TRACK, false);
                        return;
                    }
                    JSONObject eventData;
                    if (isPushTrack(eventName)) {
                        updateLastOperateTime(context);
                        eventData = DataAssemble.getInstance(context).getEventData(
                                currentTime,Constants.API_TRACK, Constants.TRACK,
                                null, eventInfo, eventName);
                    } else {
                        eventData = DataAssemble.getInstance(context).getEventData(
                                currentTime,Constants.API_TRACK, Constants.TRACK,
                                eventInfo, null, eventName);
                    }
                    trackEvent(context, Constants.API_TRACK, eventName, eventData);
                } catch (Throwable ignore) {
                    ExceptionUtil.exceptionThrow(ignore);
                }
            }
        });
    }

    private void updateLastOperateTime(Context context) {
        // 判断 session 是否需要重置
        SessionManage.getInstance(context).resetSession(false);
        // 更新最后一次用户操作时间
        SharedUtil.setString(context, Constants.SP_LAST_PAGE_CHANGE,
                String.valueOf(System.currentTimeMillis()));
    }

    /**
     * distinct id 存储
     */
    public void identify(final String distinctId) {
        AThreadPool.asyncHighPriorityExecutor(new Runnable() {
            @Override
            public void run() {
                if (!CheckUtils.checkIdLength(distinctId)) {
                    LogPrompt.showLog(Constants.SP_DISTINCT_ID, LogBean.getLog());
                    return;
                }
//                CommonUtils.setIdFile(context, Constants.SP_DISTINCT_ID, distinctId);
                EasytouchProcess.getInstance().setXwho(distinctId);
                UserInfo.setDistinctID(distinctId);

            }
        });
    }

    /**
     * 获取distinctId
     */
    public String getDistinctId() {
        return UserInfo.getDistinctID();
    }

    /**
     * alias id
     */
    public void alias(final String aliasId, final String originalId) {
        final long currentTime = System.currentTimeMillis();
        AThreadPool.asyncHighPriorityExecutor(new Runnable() {
            @Override
            public void run() {
                try {
                    Context context = AnalysysUtil.getContext();
                    if (context != null) {
                        if (!CheckUtils.checkIdLength(aliasId)) {
                            LogPrompt.showLog(Constants.API_ALIAS, LogBean.getLog());
                            return;
                        }
                        if (!CommonUtils.isEmpty(originalId)
                                && !CheckUtils.checkOriginalIdLength(originalId)) {
                            LogPrompt.showLog(Constants.API_ALIAS, LogBean.getLog());
                            return;
                        }
                        String original = originalId;
                        if (CommonUtils.isEmpty(original)) {
                            original = UserInfo.getDistinctID();
                        } else {
//                            SharedUtil.setString(context, Constants.SP_ORIGINAL_ID, original);
                            UserInfo.setOriginalID(original);
                        }
//                        CommonUtils.setIdFile(context, Constants.SP_ALIAS_ID, aliasId);
                        UserInfo.setAliasID(aliasId);

                        Map<String, Object> aliasMap = new HashMap<>();
                        aliasMap.put(Constants.ORIGINAL_ID, original);

                        JSONObject eventData = DataAssemble.getInstance(context).getEventData(
                                currentTime,Constants.API_ALIAS, Constants.ALIAS, aliasMap, null);

                        if (!CommonUtils.isEmpty(eventData)) {
                            EasytouchProcess.getInstance().setXwho(aliasId);
                            trackEvent(context, Constants.API_ALIAS, Constants.ALIAS, eventData);
                            sendProfileSetOnce(context, 0,currentTime);
                        } else {
                            LogPrompt.showLog(Constants.API_ALIAS, false);
                        }
                    }
                } catch (Throwable ignore) {
                    ExceptionUtil.exceptionThrow(ignore);
                }
            }
        });
    }

    public void alias(final String aliasId) {
        final long currentTime = System.currentTimeMillis();
        AThreadPool.asyncHighPriorityExecutor(new Runnable() {
            @Override
            public void run() {
                try {
                    Context context = AnalysysUtil.getContext();
                    if (context != null) {
                        if (!CheckUtils.checkIdLength(aliasId)) {
                            LogPrompt.showLog(Constants.API_ALIAS, LogBean.getLog());
                            return;
                        }
                        String original = UserInfo.getDistinctID();
//                        SharedUtil.setString(context, Constants.SP_ORIGINAL_ID, original);
                        UserInfo.setOriginalID(original);
//                        CommonUtils.setIdFile(context, Constants.SP_ALIAS_ID, aliasId);
                        UserInfo.setAliasID(aliasId);

                        Map<String, Object> aliasMap = new HashMap<>();
                        aliasMap.put(Constants.ORIGINAL_ID, original);

                        JSONObject eventData = DataAssemble.getInstance(context).getEventData(
                                currentTime,Constants.API_ALIAS, Constants.ALIAS, aliasMap, null);

                        if (!CommonUtils.isEmpty(eventData)) {
                            EasytouchProcess.getInstance().setXwho(aliasId);
                            trackEvent(context, Constants.API_ALIAS, Constants.ALIAS, eventData);
                            sendProfileSetOnce(context, 0,currentTime);
                        } else {
                            LogPrompt.showLog(Constants.API_ALIAS, false);
                        }
                    }
                } catch (Throwable ignored) {
                    ExceptionUtil.exceptionThrow(ignored);
                }
            }
        });
    }

    /**
     * profile set 键值对
     */
    public void profileSet(final String propertyKey, final Object propertyValue) {
        Map<String, Object> propertyMap = new HashMap<>();
        propertyMap.put(propertyKey, propertyValue);
        profileSet(propertyMap);
    }

    /**
     * profile set json
     */
    public void profileSet(final Map<String, Object> profileDetail) {
        final long currentTime = System.currentTimeMillis();
        AThreadPool.asyncHighPriorityExecutor(new Runnable() {
            @Override
            public void run() {
                try {
                    Context context = AnalysysUtil.getContext();
                    if (context != null) {
                        Map<String, Object> profileInfo = CommonUtils.deepCopy(profileDetail);
                        JSONObject eventData = DataAssemble.getInstance(context).getEventData(
                                currentTime,Constants.API_PROFILE_SET,
                                Constants.PROFILE_SET, profileInfo, null);
                        trackEvent(context, Constants.API_PROFILE_SET,
                                Constants.PROFILE_SET, eventData);
                    }
                } catch (Throwable ignore) {
                    ExceptionUtil.exceptionThrow(ignore);
                }
            }
        });
    }

    /**
     * profileSetOnce json
     */
    public void profileSetOnce(final Map<String, Object> profileDetail) {
        final long currentTime = System.currentTimeMillis();
        AThreadPool.asyncHighPriorityExecutor(new Runnable() {
            @Override
            public void run() {
                try {
                    Context context = AnalysysUtil.getContext();
                    if (context != null) {
                        Map<String, Object> profileInfo = CommonUtils.deepCopy(profileDetail);
                        JSONObject eventData = DataAssemble.getInstance(context).getEventData(
                                currentTime,Constants.API_PROFILE_SET_ONCE,
                                Constants.PROFILE_SET_ONCE, profileInfo, null);

                        trackEvent(context, Constants.API_PROFILE_SET_ONCE,
                                Constants.PROFILE_SET_ONCE, eventData);
                    } else {
                        LogPrompt.showLog(Constants.API_PROFILE_SET_ONCE, false);
                    }
                } catch (Throwable ignored) {
                    ExceptionUtil.exceptionThrow(ignored);
                }
            }
        });
    }

    /**
     * profile set 键值对
     */
    public void profileSetOnce(final String profileKey, final Object profileValue) {
        Context context = AnalysysUtil.getContext();
        if (context != null) {
            Map<String, Object> propertyMap = new HashMap<>();
            propertyMap.put(profileKey, profileValue);
            profileSetOnce(propertyMap);
        } else {
            LogPrompt.showLog(Constants.API_PROFILE_SET_ONCE, false);
        }
    }

    /**
     * profile increment
     */
    public void profileIncrement(final Map<String, ? extends Number> profileDetail) {
        final long currentTime = System.currentTimeMillis();
        AThreadPool.asyncHighPriorityExecutor(new Runnable() {
            @Override
            public void run() {
                try {
                    Context context = AnalysysUtil.getContext();
                    if (context != null) {
                        Map<String, ? extends Number> profileInfo =
                                CommonUtils.deepCopy(profileDetail);
                        JSONObject eventData = DataAssemble.getInstance(context).getEventData(
                                currentTime,Constants.API_PROFILE_INCREMENT,
                                Constants.PROFILE_INCREMENT, profileInfo, null);

                        trackEvent(context, Constants.API_PROFILE_INCREMENT,
                                Constants.PROFILE_INCREMENT, eventData);
                    } else {
                        LogPrompt.showLog(Constants.API_PROFILE_INCREMENT, false);
                    }
                } catch (Throwable ignore) {
                    ExceptionUtil.exceptionThrow(ignore);
                }
            }
        });
    }

    /**
     * profile increment
     */
    public void profileIncrement(final String profileKey, final Number profileValue) {
        Context context = AnalysysUtil.getContext();
        if (context != null) {
            Map<String, Number> profileMap = new HashMap<>();
            profileMap.put(profileKey, profileValue);
            profileIncrement(profileMap);
        } else {
            LogPrompt.showLog(Constants.API_PROFILE_INCREMENT, false);
        }
    }

    /**
     * 给一个列表类型的Profile增加一个元素
     */
    public void profileAppend(final Map<String, Object> profileDetail) {
        final long currentTime = System.currentTimeMillis();
        AThreadPool.asyncHighPriorityExecutor(new Runnable() {
            @Override
            public void run() {
                try {
                    Context context = AnalysysUtil.getContext();
                    if (context != null) {
                        Map<String, Object> profileInfo = CommonUtils.deepCopy(profileDetail);
                        JSONObject eventData = DataAssemble.getInstance(context).getEventData(
                                currentTime,Constants.API_PROFILE_APPEND,
                                Constants.PROFILE_APPEND, profileInfo, null);

                        trackEvent(context, Constants.API_PROFILE_APPEND,
                                Constants.PROFILE_APPEND, eventData);
                    } else {
                        LogPrompt.showLog(Constants.API_PROFILE_APPEND, false);
                    }
                } catch (Throwable ignore) {
                    ExceptionUtil.exceptionThrow(ignore);
                }
            }
        });
    }

    /**
     * profile append
     */
    public void profileAppend(final String propertyName, final Object propertyValue) {
        Context context = AnalysysUtil.getContext();
        if (context != null) {
            Map<String, Object> properMap = new HashMap<>();
            properMap.put(propertyName, propertyValue);
            profileAppend(properMap);
        } else {
            LogPrompt.showLog(Constants.API_PROFILE_APPEND, false);
        }
    }

    /**
     * 给一个列表类型的Profile增加一个或多个元素
     */
    public void profileAppend(final String profileKey, final List<Object> profileValue) {
        Context context = AnalysysUtil.getContext();
        if (context != null) {
            Map<String, Object> profileMap = new HashMap<>();
            profileMap.put(profileKey, profileValue);
            profileAppend(profileMap);
        } else {
            LogPrompt.showLog(Constants.API_PROFILE_APPEND, false);
        }
    }

    /**
     * 删除单个用户属性
     */
    public void profileUnset(final String propertyKey) {
        final long currentTime = System.currentTimeMillis();
        AThreadPool.asyncHighPriorityExecutor(new Runnable() {
            @Override
            public void run() {
                try {
                    Context context = AnalysysUtil.getContext();
                    if (context != null && !CommonUtils.isEmpty(propertyKey)) {
                        Map<String, Object> profileMap = new HashMap<>();
                        profileMap.put(propertyKey, "");

                        JSONObject eventData = DataAssemble.getInstance(context).getEventData(
                                currentTime,Constants.API_PROFILE_UNSET,
                                Constants.PROFILE_UNSET, profileMap, null);

                        trackEvent(context, Constants.API_PROFILE_UNSET,
                                Constants.PROFILE_UNSET, eventData);

                    } else {
                        LogPrompt.showLog(Constants.API_PROFILE_UNSET, false);
                    }
                } catch (Throwable ignore) {
                    ExceptionUtil.exceptionThrow(ignore);
                }
            }
        });
    }

    /**
     * 清除所有用户属性
     */
    public void profileDelete() {
        final long currentTime = System.currentTimeMillis();
        AThreadPool.asyncHighPriorityExecutor(new Runnable() {
            @Override
            public void run() {
                try {
                    Context context = AnalysysUtil.getContext();
                    if (context == null) {
                        return;
                    }
                    JSONObject eventData = DataAssemble.getInstance(context).getEventData(
                            currentTime,Constants.API_PROFILE_DELETE,
                            Constants.PROFILE_DELETE, null, null);

                    trackEvent(context, Constants.API_PROFILE_DELETE,
                            Constants.PROFILE_DELETE, eventData);

                } catch (Throwable ignore) {
                    ExceptionUtil.exceptionThrow(ignore);
                }
            }
        });
    }

    /**
     * Js注册单条通用属性
     *
     * @param key
     * @param value
     */
    public void registerJsSuperProperty(final String key, final Object value) {
        handleSuperProperty(key, value, Constants.SP_JS_SUPER_PROPERTY);
    }

    /**
     * 注册单条通用属性
     */
    public void registerSuperProperty(final String key, final Object value) {
        handleSuperProperty(key, value, Constants.SP_SUPER_PROPERTY);
    }

    private void handleSuperProperty(final String key, final Object value, final String type) {
        AThreadPool.asyncHighPriorityExecutor(new Runnable() {
            @Override
            public void run() {
                try {
                    Context context = AnalysysUtil.getContext();
                    if (context == null) {
                        return;
                    }
                    Map<String, Object> property = new HashMap<>();
                    property.put(key, value);

                    if (CheckUtils.checkParameter(Constants.API_REGISTER_SUPER_PROPERTY,
                            property)) {
                        if (LogBean.getCode() == Constants.CODE_SUCCESS) {
                            LogPrompt.showLog(Constants.API_REGISTER_SUPER_PROPERTY, true);
                        }
                        saveSuperProperty(context, property, type);
                    } else {
                        LogPrompt.showLog(Constants.API_REGISTER_SUPER_PROPERTY, LogBean.getLog());
                    }
                } catch (Throwable ignored) {
                    ExceptionUtil.exceptionThrow(ignored);
                }

            }
        });
    }

    /**
     * 注册js通用属性
     *
     * @param propertyDetail
     */
    public void registerJsSuperProperties(final Map<String, Object> propertyDetail) {
        handleSuperProperties(propertyDetail, Constants.SP_JS_SUPER_PROPERTY);
    }

    /**
     * 注册多条公共属性
     */
    public void registerSuperProperties(final Map<String, Object> propertyDetail) {
        handleSuperProperties(propertyDetail, Constants.SP_SUPER_PROPERTY);
    }

    private void handleSuperProperties(final Map<String, Object> propertyDetail, final String type) {
        AThreadPool.asyncHighPriorityExecutor(new Runnable() {
            @Override
            public void run() {
                try {
                    Context context = AnalysysUtil.getContext();
                    if (context == null) {
                        LogPrompt.showLog(
                                Constants.API_REGISTER_SUPER_PROPERTIES, LogBean.getLog());
                        return;
                    }
                    Map<String, Object> propertyInfo = CommonUtils.deepCopy(propertyDetail);
                    if (CheckUtils.checkParameter(
                            Constants.API_REGISTER_SUPER_PROPERTIES, propertyInfo)) {
                        if (LogBean.getCode() == Constants.CODE_SUCCESS) {
                            LogPrompt.showLog(
                                    Constants.API_REGISTER_SUPER_PROPERTIES, true);
                        }
                        saveSuperProperty(context, propertyInfo, type);
                    }
                } catch (Throwable ignore) {
                    ExceptionUtil.exceptionThrow(ignore);
                }
            }

        });
    }


    /**
     * 用户获取super Property
     */
    public Map<String, Object> getSuperProperty() {
        Map<String, Object> map = new HashMap<String, Object>(16);
        JSONObject mergeSuper = new JSONObject();
        try {
            Context context = AnalysysUtil.getContext();
            if (context == null) {
                return new HashMap<>();
            }
            String superProperty = SharedUtil.getString(context,
                    Constants.SP_SUPER_PROPERTY, null);

            String jsSuperProperty = SharedUtil.getString(context,
                    Constants.SP_JS_SUPER_PROPERTY, null);

            //用superProperty覆盖jsSuperProperty的方式合并
            JSONObject superPropertyJson = null;
            if (superProperty != null && superProperty.length() > 0) {
                superPropertyJson = new JSONObject(superProperty);
            }
            JSONObject jsSuperPropertyJson = null;
            if (jsSuperProperty != null && jsSuperProperty.length() > 0) {
                jsSuperPropertyJson = new JSONObject(jsSuperProperty);
            }
            if (superPropertyJson != null && jsSuperPropertyJson != null) {
                CommonUtils.mergeJson(superPropertyJson, jsSuperPropertyJson);
                mergeSuper = jsSuperPropertyJson;
            } else {
                if (superPropertyJson == null) {
                    //用jsSuperProperty
                    mergeSuper = jsSuperPropertyJson;
                }
                {
                    if (jsSuperPropertyJson == null) {
                        mergeSuper = superPropertyJson;
                    }
                }
            }

            if (mergeSuper != null) {
                map.putAll(CommonUtils.jsonToMap(mergeSuper));
            }

        } catch (Throwable ignore) {
            ExceptionUtil.exceptionThrow(ignore);
        }
        return map;
    }


    /**
     * 用户获取超级属性
     */
    public Object getSuperProperty(final String propertyKey) {
        Object obj = handleGetSuperProperty(propertyKey, Constants.SP_SUPER_PROPERTY);
        if (obj == null) {
            obj = handleGetSuperProperty(propertyKey, Constants.SP_JS_SUPER_PROPERTY);
        }
        return obj;
    }

    private Object handleGetSuperProperty(String propertyKey, String type) {
        try {
            Context context = AnalysysUtil.getContext();
            if (context != null && !CommonUtils.isEmpty(propertyKey)) {
                String superProperty = SharedUtil.getString(
                        context, type, null);
                if (!CommonUtils.isEmpty(superProperty)) {
                    return new JSONObject(superProperty).opt(propertyKey);
                }
            }
        } catch (Throwable throwable) {
            ExceptionUtil.exceptionThrow(throwable);
        }
        return null;
    }

    /**
     * 删除js超级属性
     *
     * @param superPropertyName
     */
    public void unregisterJsSuperProperty(final String superPropertyName) {
        handleSuperProperty(superPropertyName, Constants.SP_JS_SUPER_PROPERTY);
    }

    /**
     * 删除超级属性
     *
     * @param superPropertyName
     */
    public void unregisterSuperProperty(final String superPropertyName) {
        handleSuperProperty(superPropertyName, Constants.SP_SUPER_PROPERTY);
    }

    private void handleSuperProperty(final String superPropertyName, final String type) {
        AThreadPool.asyncHighPriorityExecutor(new Runnable() {
            @Override
            public void run() {
                try {
                    Context context = AnalysysUtil.getContext();
                    if (context != null && !CommonUtils.isEmpty(superPropertyName)) {
                        String property = SharedUtil.getString(
                                context, type, null);
                        if (!CommonUtils.isEmpty(property)) {
                            JSONObject json = new JSONObject(property);
                            json.remove(superPropertyName);
                            SharedUtil.setString(
                                    context, type, String.valueOf(json));
                            LogPrompt.showLog(
                                    Constants.API_UNREGISTER_SUPER_PROPERTY, true);
                        }
                    }
                } catch (Throwable ignore) {
                    ExceptionUtil.exceptionThrow(ignore);
                }
            }
        });
    }

    /**
     * 清空js通用属性
     */
    public void clearJsSuperProperty() {
        handleClearSuperProperty(Constants.SP_JS_SUPER_PROPERTY);
    }

    /**
     * 删除所有超级属性
     */
    public void clearSuperProperty() {
        handleClearSuperProperty(Constants.SP_SUPER_PROPERTY);
    }

    private void handleClearSuperProperty(final String type) {
        AThreadPool.asyncHighPriorityExecutor(new Runnable() {
            @Override
            public void run() {
                try {
                    Context context = AnalysysUtil.getContext();
                    if (context != null) {
                        SharedUtil.remove(context, type);
                        LogPrompt.showLog(
                                Constants.API_CLEAR_SUPER_PROPERTIES, true);
                    }
                } catch (Throwable ignore) {
                    ExceptionUtil.exceptionThrow(ignore);
                }
            }
        });
    }

    /**
     * 用户自定义上传间隔时间
     */
    public void setIntervalTime(final long time) {
        AThreadPool.asyncHighPriorityExecutor(new Runnable() {
            @Override
            public void run() {
                Context context = AnalysysUtil.getContext();
                if (context != null && 1 <= time) {
                    SharedUtil.setLong(
                            context, Constants.SP_USER_INTERVAL_TIME, time * 1000);
                    LogPrompt.showLog(Constants.API_SET_INTERVAL_TIME, true);
                } else {
                    LogPrompt.showLog(Constants.API_SET_INTERVAL_TIME, "time must be > 1,otherwise use default.");
                }
            }
        });
    }

    /**
     * 用户自定义上传条数
     */
    public void setMaxEventSize(final long count) {
        AThreadPool.asyncHighPriorityExecutor(new Runnable() {
            @Override
            public void run() {
                Context context = AnalysysUtil.getContext();
                if (context != null && 1 <= count) {
                    SharedUtil.setLong(
                            context, Constants.SP_USER_EVENT_COUNT, count);
                    LogPrompt.showLog(Constants.API_SET_MAX_EVENT_SIZE, true);
                } else {
                    LogPrompt.showLog(Constants.API_SET_MAX_EVENT_SIZE, "count must be > 1,otherwise use default.");

                }
            }
        });
    }

    /**
     * 获取最大缓存条数
     */
    public long getMaxCacheSize() {
        long count;
        count = AnsRamControl.getInstance().getCacheMaxCount();
        if (count < 1) {
            count = Constants.MAX_CACHE_COUNT;
        }
        return count;
    }

    /**
     * 设置最大缓存条数
     */
    public void setMaxCacheSize(final long count) {
        AThreadPool.asyncHighPriorityExecutor(new Runnable() {
            @Override
            public void run() {
                if (count >= 100 && count <= 10000) {
                    LogPrompt.showLog(Constants.API_SET_MAX_CACHE_SIZE,true);
                    AnsRamControl.getInstance().setCacheMaxCount(count);
                }else{
                    LogPrompt.showLog(Constants.API_SET_MAX_CACHE_SIZE,"count must be > 100 and <=10000,otherwise use default.");
                }
            }
        });
    }

    /**
     * 清除本地所有id、超级属性、重新生成id
     */
    public void reset() {
        final long currentTime = System.currentTimeMillis();
        AThreadPool.asyncHighPriorityExecutor(new Runnable() {
            @Override
            public void run() {
                try {
                    Context context = AnalysysUtil.getContext();
                    if (context != null) {
                        resetInfo(context);
//                        CommonUtils.setIdFile(context, Constants.SP_UUID, "");
                        LogPrompt.showLog(Constants.API_RESET, true);
                        sendProfileSetOnce(context, 1,currentTime);
                    }
                } catch (Throwable ignore) {
                    ExceptionUtil.exceptionThrow(ignore);
                    LogPrompt.showLog(Constants.API_RESET, false);
                }
            }
        });
    }


    /**
     * 存储url
     */
    public void setUploadURL(final String url) {
        AThreadPool.asyncHighPriorityExecutor(new Runnable() {
            @Override
            public void run() {
                try {
                    Context context = AnalysysUtil.getContext();
                    if (context != null && !CommonUtils.isEmpty(url)) {
                        String getUrl;
                        if (url.startsWith(Constants.HTTP)) {
                            getUrl = CommonUtils.checkUrl(url);
                        } else if (url.startsWith(Constants.HTTPS)) {
                            getUrl = CommonUtils.checkUrl(url);
                        } else {
                            LogPrompt.showLog(Constants.API_SET_UPLOAD_URL, false);
                            return;
                        }
                        if (!CommonUtils.isEmpty(getUrl)) {
                            saveUploadUrl(context, getUrl + "/up");
                        } else {
                            LogPrompt.showErrLog(LogPrompt.URL_ERR);
                        }
                    }
                } catch (Throwable ignored) {
                }
            }
        });
    }


    /**
     * 用户调用上传接口
     */
    public void flush() {
        AThreadPool.asyncHighPriorityExecutor(new Runnable() {
            @Override
            public void run() {
                Context context = AnalysysUtil.getContext();
                if (context != null) {
                    UploadManager.getInstance(context).flushSendManager();
                }
            }
        });
    }

    /**
     * 获取预置属性
     */
    public Map<String, Object> getPresetProperties() {
        try {
            Context context = AnalysysUtil.getContext();
            if (context != null) {
                if (properties == null) {
                    properties = new HashMap<>();
                    properties.put("$time_zone", InternalAgent.getTimeZone(context));
                    properties.put("$platform", "Android");
                    properties.put("$app_version", InternalAgent.getVersionName(context));
                    properties.put("$language", InternalAgent.getDeviceLanguage(context));
                    properties.put("$lib_version", InternalAgent.getLibVersion(context));
                    properties.put("$lib", "Android");
                    properties.put("$screen_width", InternalAgent.getScreenWidth(context));
                    properties.put("$screen_height", InternalAgent.getScreenHeight(context));
                    properties.put("$network", InternalAgent.getNetwork(context));
                    properties.put("$manufacturer", InternalAgent.getManufacturer(context));
                    properties.put("$mac", InternalAgent.getMac(context));
                    properties.put("$imei", InternalAgent.getIMEI(context));
                    properties.put("$first_visit_time", SharedUtil.getString(context,
                            Constants.SP_FIRST_START_TIME, ""));
                    properties.put("$brand", InternalAgent.getBrand(context));
                    properties.put("$model", InternalAgent.getDeviceModel(context));
                    properties.put("$os", "Android");
                    properties.put("$session_id",
                            SessionManage.getInstance(context).getSessionId());
                    return properties;
                } else {
                    return properties;
                }
            }
        } catch (Throwable ignore) {
            ExceptionUtil.exceptionThrow(ignore);
        }
        return new HashMap<>();
    }

    /**
     * 拦截监听 URL
     */
    public void interceptUrl(String url, Object view) {
        try {
            if (!CommonUtils.isEmpty(url)) {
                if (mWebViewUrlVisitCache == null) {
                    mWebViewUrlVisitCache = new LruCache<>(100);
                }
                long now = System.currentTimeMillis();
                Long lastVisitTime = mWebViewUrlVisitCache.get(url);
                mWebViewUrlVisitCache.put(url, now);
                if (lastVisitTime != null && Math.abs(now - lastVisitTime) < URL_VISIT_MIN_TIME_INTERVAL) {
                    return;
                }

                String decodedURL = java.net.URLDecoder.decode(url, "UTF-8");
                if (decodedURL.startsWith(ANALYSYS_AGENT)) {
                    HybridBridge.getInstance().execute(decodedURL, view);
                }

                // 调用原来的WebViewClient
                if (mWebViewClientCache != null) {
                    Object webViewClient = mWebViewClientCache.get(view.hashCode());
                    if (webViewClient != null) {
                        String webViewClientClzName = webViewClient.getClass().getName();
                        // 防止递归调用
                        StackTraceElement[] stack = (new Throwable()).getStackTrace();
                        for (int i = 0; i < stack.length; i++) {
                            StackTraceElement ste = stack[i];
                            String clzName = ste.getClassName();
                            if (TextUtils.equals(clzName, webViewClientClzName)) {
                                return;
                            }
                        }
                        AnsReflectUtils.invokeMethodByName(webViewClient, "shouldOverrideUrlLoading", new Object[]{view, url});
                    }
                }
            }
        } catch (Throwable ignore) {
            ExceptionUtil.exceptionThrow(ignore);
        }
    }

    // 解决WebViewClient覆盖问题
    private Map<Integer, Object> mWebViewClientCache;

    // 防止同一个url在极短时间内频繁调用
    private static final int URL_VISIT_MIN_TIME_INTERVAL = 100;
    private LruCache<String, Long> mWebViewUrlVisitCache;

    /**
     * UA 增加 AnalysysAgent/Hybrid 字段
     */
    public void setHybridModel(Object webView) {
        if (!CommonUtils.isEmpty(webView)) {
            try {
                // 修改agent
                Method getSettings = webView.getClass().getMethod(GET_SETTINGS);
                Object webSettings = getSettings.invoke(webView);
                if (webSettings != null) {
                    Method getUserAgentString = webSettings.getClass().getMethod(GET_USER_AGENT);
                    String userAgent = (String) getUserAgentString.invoke(webSettings);
                    Method setUserAgentString = webSettings.getClass().getMethod(
                            SET_USER_AGENT, String.class);
                    if (!CommonUtils.isEmpty(userAgent)) {
                        setUserAgentString.invoke(
                                webSettings, userAgent + Constants.HYBRID_AGENT);
                    } else {
                        setUserAgentString.invoke(webSettings, Constants.HYBRID_AGENT);
                    }
                }

                // 保存WebViewClient
                Object webViewClient = AnsReflectUtils.invokeMethod(webView, "getWebViewClient");
                if (webViewClient != null) {
                    if (mWebViewClientCache == null) {
                        mWebViewClientCache = new HashMap<>();
                    }
                    mWebViewClientCache.put(webView.hashCode(), webViewClient);
                }
            } catch (Throwable ignore) {
                ExceptionUtil.exceptionThrow(ignore);
            }
        }
    }

    /**
     * UA 删除 AnalysysAgent/Hybrid 字段
     */
    public void resetHybridModel(final Object webView) {
        try {
            if (webView != null) {
                Method getSettings = webView.getClass().getMethod(GET_SETTINGS);
                Object webSettings = getSettings.invoke(webView);
                if (webSettings != null) {
                    Method getUserAgentString = webSettings.getClass().getMethod(GET_USER_AGENT);
                    String userAgent = (String) getUserAgentString.invoke(webSettings);
                    Method setUserAgentString = webSettings.getClass().getMethod(SET_USER_AGENT,
                            String.class);
                    if (userAgent != null && (userAgent.contains(Constants.HYBRID_AGENT))) {
                        userAgent = userAgent.replace(Constants.HYBRID_AGENT, "");
                        setUserAgentString.invoke(webSettings, userAgent);
                    }
                }

                // 删除WebViewClient
                if (mWebViewClientCache != null) {
                    mWebViewClientCache.remove(webView.hashCode());
                }

                // 删除UrlVisitCache
                if (mWebViewClientCache != null) {
                    mWebViewClientCache.clear();
                }
            }
        } catch (Throwable ignore) {
            ExceptionUtil.exceptionThrow(ignore);
        }
    }

    /**
     * 设置可视化base mUrl
     */
    private void setVisitorBaseURL(final String url) {
        AThreadPool.asyncHighPriorityExecutor(new Runnable() {
            @Override
            public void run() {
                try {
                    Context context = AnalysysUtil.getContext();
                    if (context != null) {
                        LifeCycleConfig.initVisualConfig(context);
                        if (LifeCycleConfig.visualBase != null) {
                            setVisualUrl(context, LifeCycleConfig.visualBase.optString(START), url);
                        }
                    }
                } catch (Throwable ignore) {
                    ExceptionUtil.exceptionThrow(ignore);
                }
            }
        });
    }

    /**
     * 设置可视化websocket服务器地址
     */
    public void setVisitorDebugURL(final String url) {
        AThreadPool.asyncHighPriorityExecutor(new Runnable() {
            @Override
            public void run() {
                try {
                    Context context = AnalysysUtil.getContext();
                    if (context != null) {
                        LifeCycleConfig.initVisualConfig(context);
                        if (LifeCycleConfig.visual != null) {
                            setVisualUrl(context, LifeCycleConfig.visual.optString(START), url);
                        }
                    }
                } catch (Throwable ignore) {
                    ExceptionUtil.exceptionThrow(ignore);
                }
            }
        });
    }

    /**
     * 设置线上请求埋点配置的服务器地址
     */
    public void setVisitorConfigURL(final String url) {
        AThreadPool.asyncHighPriorityExecutor(new Runnable() {
            @Override
            public void run() {
                try {
                    Context context = AnalysysUtil.getContext();
                    if (context != null) {
                        LifeCycleConfig.initVisualConfig(context);
                        if (LifeCycleConfig.visualConfig != null) {
                            setVisualUrl(context,
                                    LifeCycleConfig.visualConfig.optString(START),
                                    url);
                        }
                    }
                } catch (Throwable ignore) {
                    ExceptionUtil.exceptionThrow(ignore);
                }
            }
        });
    }

    public void enablePush(final String provider, final String pushId) {
        AThreadPool.asyncHighPriorityExecutor(new Runnable() {
            @Override
            public void run() {
                try {
                    Context context = AnalysysUtil.getContext();
                    if (context != null) {
                        LifeCycleConfig.initPushConfig(context);
                        if (LifeCycleConfig.pushParse != null) {
                            String path = LifeCycleConfig.pushParse.optString(START);
                            int index = path.lastIndexOf(".");
                            CommonUtils.reflexUtils(
                                    path.substring(0, index),
                                    path.substring(index + 1),
                                    new Class[]{Context.class, String.class, String.class},
                                    context, provider, pushId);
                        }
                    }
                } catch (Throwable ignore) {
                    ExceptionUtil.exceptionThrow(ignore);
                }
            }
        });
    }

    public void trackCampaign(final String campaign,
                              final boolean isClick, final PushListener listener) {
        AThreadPool.asyncHighPriorityExecutor(new Runnable() {
            @Override
            public void run() {
                try {
                    Context context = AnalysysUtil.getContext();
                    if (context != null) {
                        LifeCycleConfig.initPushConfig(context);
                        if (LifeCycleConfig.pushClick != null) {
                            String path = LifeCycleConfig.pushClick.optString(START);
                            CommonUtils.reflexUtils(CommonUtils.getClassPath(path),
                                    CommonUtils.getMethod(path),
                                    new Class[]{Context.class, String.class,
                                            boolean.class, PushListener.class},
                                    context, campaign, isClick, listener);
                        }
                    }
                } catch (Throwable ignore) {
                    ExceptionUtil.exceptionThrow(ignore);
                }
            }
        });
    }

    /**
     * 存储 upload url
     */
    private void saveUploadUrl(Context context, String uploadUrl) throws MalformedURLException {

        changeUrlResetUser(context, uploadUrl);
        SharedUtil.setString(context, Constants.SP_USER_URL, uploadUrl);

        // 判断是否进行时间校准且为主进程
        if (Constants.isTimeCheck && CommonUtils.isMainProcess(context)) {
            UploadManager.getInstance(context).sendGetTimeMessage();
        }
    }

    /**
     * https 上传地址 设置
     */
    private void setBaseUrl(Context context, String baseUrl) throws Exception {
        if (!CommonUtils.isEmpty(baseUrl)) {
            setVisitorBaseURL(baseUrl);
            saveUploadUrl(context,
                    Constants.HTTPS + baseUrl + Constants.HTTPS_PORT + "/up");
        }
    }

    /**
     * 存储key
     * 使用缓存的key，与传入的key对比
     */
    private void saveKey(Context context, String key) {
        String appKey = getNewKey(context, key);
        String spKey = CommonUtils.getAppKey(context);
        if (!CommonUtils.isEmpty(appKey)) {
            SharedUtil.setString(context, Constants.SP_APP_KEY, appKey);
            if (!CommonUtils.isEmpty(spKey) && !TextUtils.equals(spKey, appKey)) {
                resetInfo(context);
            }
            LogPrompt.showKeyLog(true, appKey);
        } else {
            LogPrompt.showKeyLog(false, appKey);
        }
    }

    /**
     * 读取XML和init中的key，校验后返回
     */
    private String getNewKey(Context context, String key) {
        String xmlKey = CommonUtils.getManifestData(context, Constants.DEV_APPKEY);
        if (!CommonUtils.isEmpty(key)) {
            if (!CommonUtils.isEmpty(xmlKey)) {
                return TextUtils.equals(xmlKey, key) ? key : null;
            } else {
                return key;
            }
        } else {
            return xmlKey;
        }
    }

    /**
     * 存储channel
     */
    private void saveChannel(Context context, String channel) {
        String appChannel = getNewChannel(context, channel);
        if (!CommonUtils.isEmpty(appChannel)) {
            SharedUtil.setString(context, Constants.SP_CHANNEL, appChannel);
            LogPrompt.showChannelLog(true, appChannel);
        } else {
            LogPrompt.showChannelLog(false, appChannel);
        }
    }

    /**
     * 获取 xml channel和 init channel，优先xml
     */
    private String getNewChannel(Context context, String channel) {
        String xmlChannel = CommonUtils.getManifestData(context, Constants.DEV_CHANNEL);
        if (CommonUtils.isEmpty(xmlChannel) && !CommonUtils.isEmpty(channel)) {
            return channel;
        }
        return xmlChannel;
    }

    /**
     * 渠道归因
     */
    private void sendFirstInstall(Context context,final long currentTime) throws Throwable {
        if (context != null) {
            JSONObject eventData = DataAssemble.getInstance(context).getEventData(
                    currentTime,Constants.API_FIRST_INSTALL, Constants.FIRST_INSTALL,
                    null, Constants.utm);
            trackEvent(context, Constants.API_FIRST_INSTALL,
                    Constants.FIRST_INSTALL, eventData);
        }
    }

    /**
     * debug切换判断是否需要重置发送
     */
    private void debugResetUserInfo(Context context, int debugNum) {
        int debug = SharedUtil.getInt(context, Constants.SP_USER_DEBUG, 0);
        if (debug == 1 && (debugNum == 0 || debugNum == 2)) {
            resetInfo(context);
        }
    }

    /**
     * 判断是否为推送调用track，如果是跳过数据校验
     */
    private boolean isPushTrack(String eventName) {
        return Constants.PUSH_EVENT_RECEIVER_MSG.equals(eventName)
                || Constants.PUSH_EVENT_CLICK_MSG.equals(eventName)
                || Constants.PUSH_EVENT_PROCESS_SUCCESS.equals(eventName);

    }

    /**
     * 首次安装后是否发送profile_set_once
     */
    private void sendProfileSetOnce(Context context, int type,final long currentTime) throws Throwable {
        if (Constants.isAutoProfile) {
            Map<String, Object> profileInfo = new HashMap<>();
            if (type == 0) {
                profileInfo.put(Constants.DEV_FIRST_VISIT_TIME,
                        CommonUtils.getFirstStartTime(context));
                profileInfo.put(Constants.DEV_FIRST_VISIT_LANGUAGE,
                        Locale.getDefault().getLanguage());
            } else if (type == 1) {
                profileInfo.put(Constants.DEV_RESET_TIME,
                        CommonUtils.getTime());
            } else {
                return;
            }
            JSONObject eventData = DataAssemble.getInstance(context).getEventData(
                    currentTime,Constants.API_PROFILE_SET_ONCE,
                    Constants.PROFILE_SET_ONCE, null, profileInfo);
            trackEvent(context,
                    Constants.API_PROFILE_SET_ONCE, Constants.PAGE_VIEW, eventData);
        }
    }

    /**
     * 存储通用属性
     */
    private void saveSuperProperty(Context context,
                                   Map<String, Object> superProperty, String type) throws Exception {
        JSONObject propertyInfo;
        if (!CommonUtils.isEmpty(superProperty)) {
            String sharedProperty = SharedUtil.getString(
                    context, type, null);
            if (!CommonUtils.isEmpty(sharedProperty)) {
                propertyInfo = new JSONObject(sharedProperty);
                CommonUtils.mergeJson(new JSONObject(superProperty), propertyInfo);
            } else {
                propertyInfo = new JSONObject(superProperty);
            }
            SharedUtil.setString(
                    context, type, String.valueOf(propertyInfo));
        }
    }

    /**
     * reset 接口 重置所有属性
     */
    private void resetInfo(Context context) {
        AnsIDControl.resetInfo(context);
    }


    /**
     * 修改URL重置用户属性
     */
    private void changeUrlResetUser(Context context, String url) throws MalformedURLException {
        String spServiceUrl = SharedUtil.getString(
                context, Constants.SP_SERVICE_URL, null);
        if (CommonUtils.isEmpty(spServiceUrl)) {
            String spUserUrl = SharedUtil.getString(
                    context, Constants.SP_USER_URL, null);
            if (CommonUtils.isEmpty(spUserUrl)) {
                return;
            }
            String urlHost = new URL(url).getHost();
            String userUrlHost = new URL(spUserUrl).getHost();
            if (!CommonUtils.isEmpty(urlHost)
                    && !CommonUtils.isEmpty(userUrlHost)
                    && !urlHost.equals(userUrlHost)) {
                resetInfo(context);
            }
        }
    }

    /**
     * 接口数据处理
     */
    private void trackEvent(Context context, String apiName,
                            String eventName, JSONObject eventData) {
        if (!CommonUtils.isEmpty(eventName) && checkoutEvent(eventData)) {
            // 此处重置重传传次数，解决出于重传状态时，触发新事件重传次数不够三次
            //SharedUtil.remove(mContext, Constants.SP_FAILURE_COUNT);
            if (LogBean.getCode() == Constants.CODE_SUCCESS) {
                LogPrompt.showLog(apiName, true);
            }
            EasytouchProcess.getInstance().setEventMessage(eventData.toString());
            UploadManager.getInstance(context).sendManager(eventName, eventData);
        }
    }

    /**
     * 校验数据是否符合上传格式
     */
    private boolean checkoutEvent(JSONObject eventData) {
        if (eventData == null || CommonUtils.isEmpty(eventData.optString(Constants.APP_ID))) {
            LogPrompt.keyFailed();
            return false;
        }
        return true;
    }

    private void setVisualUrl(Context context, String path, String url) {
        int index = path.lastIndexOf(".");
        CommonUtils.reflexStaticMethod(
                path.substring(0, index),
                path.substring(index + 1),
                new Class[]{Context.class, String.class},
                context, url);
    }

    public void setHeatMapBlackListByPages(List<String> pages) {
        HeatMap.getInstance().setHeatMapBlackListByPages(pages);
    }

    public void setHeatMapWhiteListByPages(List<String> pages) {
        HeatMap.getInstance().setHeatMapWhiteListByPages(pages);
    }

    private static class Holder {
        public static final AgentProcess INSTANCE = new AgentProcess();
    }

    // -------- 全埋点-页面采集 ----------

    private HashSet<Integer> mPageViewBlackListByPages = new HashSet<>();
    private HashSet<Integer> mPageViewWhiteListByPages = new HashSet<>();

    public void setPageViewBlackListByPages(List<String> pages) {
        try {
            mPageViewBlackListByPages.clear();
            if (pages != null) {
                for (String page : pages) {
                    if (!TextUtils.isEmpty(page)) {
                        mPageViewBlackListByPages.add(page.hashCode());
                    }
                }
            }
        } catch (Throwable ignore) {
            ExceptionUtil.exceptionThrow(ignore);
        }

    }

    public void setPageViewWhiteListByPages(List<String> pages) {
        try {
            mPageViewWhiteListByPages.clear();
            if (pages != null) {
                for (String page : pages) {
                    if (!TextUtils.isEmpty(page)) {
                        mPageViewWhiteListByPages.add(page.hashCode());
                    }
                }
            }
        } catch (Throwable ignore) {
            ExceptionUtil.exceptionThrow(ignore);
        }

    }

    /**
     * 当前页面是忽略采集
     */
    public boolean isThisPageInPageViewBlackList(String pages) {
        if (mPageViewBlackListByPages == null || TextUtils.isEmpty(pages)) {
            return false;
        }
        return mPageViewBlackListByPages.contains(pages.hashCode());
    }

    /**
     * 是否只采集当前页面
     */
    public boolean isThisPageInPageViewWhiteList(String pages) {
        if (mPageViewWhiteListByPages == null || TextUtils.isEmpty(pages)) {
            return false;
        }
        return mPageViewWhiteListByPages.contains(pages.hashCode());
    }

    /**
     * 判断是否有点击上报白名单
     */
    public boolean hasAutoPageViewWhiteList() {
        if (mPageViewWhiteListByPages == null) {
            return false;
        }
        return !mPageViewWhiteListByPages.isEmpty();
    }

    // --------------全埋点-点击上报-------------
    // 页面级黑白名单
    private HashSet<Integer> mIgnoreByPages = new HashSet<>();
    private HashSet<Integer> mAutoByPages = new HashSet<>();


    // 控件类型级黑白名单
    private HashSet<Integer> mIgnoreByViewTypes = new HashSet<>();
    private HashSet<Integer> mAutoByByViewTypes = new HashSet<>();


    // 控件类型级黑白名单
    private HashSet<Integer> mIgnoreByView = new HashSet<>();
    private HashSet<Integer> mAutoByView = new HashSet<>();

    /**
     * 设置页面级黑名单
     */
    public void setAutoClickBlackListByPages(List<String> pages) {
        try {
            mIgnoreByPages.clear();
            if (pages != null && pages.size() > 0) {
                for (String page : pages) {
                    if (!TextUtils.isEmpty(page)) {
                        mIgnoreByPages.add(page.hashCode());
                    }
                }
            }
        } catch (Exception ignore) {
            ExceptionUtil.exceptionThrow(ignore);
        }
    }

    /**
     * 设置页面级白名单
     */
    public void setAutoClickWhiteListByPages(List<String> pages) {
        try {
            mAutoByPages.clear();
            for (String page : pages) {
                if (!TextUtils.isEmpty(page)) {
                    mAutoByPages.add(page.hashCode());
                }
            }
        } catch (Throwable ignore) {
            ExceptionUtil.exceptionThrow(ignore);
        }

    }

    /**
     * 判断页面是否在点击上报白名单
     */
    public boolean isThisPageInAutoClickWhiteList(String page) {
        return !TextUtils.isEmpty(page) && mAutoByPages.contains(page.hashCode());
    }

    /**
     * 判断页面是否在点击上报黑名单
     */
    public boolean isThisPageInAutoClickBlackList(String page) {
        return !TextUtils.isEmpty(page) && mIgnoreByPages.contains(page.hashCode());
    }


    /**
     * 设置元素类型级黑名单
     */
    public void setAutoClickBlackListByViewTypes(List<Class> viewTypes) {
        try {
            mIgnoreByViewTypes.clear();
            if (viewTypes != null) {
                for (Class<?> viewType : viewTypes) {
                    String viewTypeStr = viewType.getName();
                    if (!TextUtils.isEmpty(viewTypeStr)) {
                        mIgnoreByViewTypes.add(viewTypeStr.hashCode());
                    }
                }
            }
        } catch (Throwable ignore) {
            ExceptionUtil.exceptionThrow(ignore);
        }
    }

    /**
     * 设置元素类型级白名单
     */
    public void setAutoClickWhiteListByViewTypes(List<Class> viewTypes) {
        try {
            mAutoByByViewTypes.clear();
            if (viewTypes != null) {
                for (Class<?> viewType : viewTypes) {
                    String viewTypeStr = viewType.getName();
                    if (!TextUtils.isEmpty(viewTypeStr)) {
                        mAutoByByViewTypes.add(viewTypeStr.hashCode());
                    }
                }
            }
        } catch (Throwable ignore) {
            ExceptionUtil.exceptionThrow(ignore);
        }
    }

    /**
     * 判断元素类型是否在点击上报白名单
     */
    public boolean isThisViewTypeInAutoClickWhiteList(Class<?> viewType) {
        try {
            if (viewType != null) {
                String name = viewType.getName();
                return !TextUtils.isEmpty(name) && mAutoByByViewTypes.contains(name.hashCode());
            }
        } catch (Throwable ignore) {
            ExceptionUtil.exceptionThrow(ignore);
        }

        return false;
    }


    /**
     * 判断元素是否在点击上报黑名单
     */
    public boolean isThisViewTypeInAutoClickBlackList(Class<?> viewType) {
        try {
            if (viewType != null) {
                String name = viewType.getName();
                return !TextUtils.isEmpty(name) && mIgnoreByViewTypes.contains(name.hashCode());
            }
        } catch (Throwable ignore) {
            ExceptionUtil.exceptionThrow(ignore);
        }
        return false;
    }


    /**
     * 设置元素类型级黑名单
     */
    public void setAutoClickBlackListByView(Object element) {
        if (element != null) {
            mIgnoreByView.add(element.hashCode());
        }
    }

    /**
     * 设置元素类型级白名单
     */
    public void setAutoClickWhiteListByView(Object element) {
        if (element != null) {
            mAutoByView.add(element.hashCode());
        }
    }

    /**
     * 判断元素对象是否在点击上报黑名单
     */
    public boolean isThisViewInAutoClickBlackList(Object element) {
        if (element != null && mIgnoreByView != null) {
//            String name = element.getClass().getName();
            return mIgnoreByView.contains(element.hashCode());
        }
        return false;
    }

    /**
     * 判断元素对象是否在点击上报黑名单
     */
    public boolean isThisViewInAutoClickWhiteList(Object element) {
        if (element != null && mAutoByView != null) {
//            String name = element.getClass().getName();
            return mAutoByView.contains(element.hashCode());
        }
        return false;
    }


    /**
     * 判断是否有点击上报白名单
     */
    public boolean hasAutoClickWhiteList() {
        return !mAutoByView.isEmpty() || !mAutoByByViewTypes.isEmpty() || !mAutoByPages.isEmpty();
    }

    public AnalysysConfig getConfig() {
        return mConfig;
    }

}
