package com.analysys.process;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.analysys.AnalysysConfig;
import com.analysys.AutomaticAcquisition;
import com.analysys.database.TableAllInfo;
import com.analysys.hybrid.HybridBridge;
import com.analysys.network.UploadManager;
import com.analysys.push.PushListener;
import com.analysys.utils.ANSLog;
import com.analysys.utils.ANSThreadPool;
import com.analysys.utils.ActivityLifecycleUtils;
import com.analysys.utils.AnalysysUtil;
import com.analysys.utils.CheckUtils;
import com.analysys.utils.CommonUtils;
import com.analysys.utils.Constants;
import com.analysys.utils.CrashHandler;
import com.analysys.utils.InternalAgent;
import com.analysys.utils.LogPrompt;
import com.analysys.utils.NumberFormat;
import com.analysys.utils.SharedUtil;

import org.json.JSONObject;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

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

    private static long cacheMaxCount = 0;

    private Application mApp = null;
    private String mTitle = "", mUrl = "";
    private Map<String, Object> properties;
    
    private AnalysysConfig mConfig = new AnalysysConfig();

    public static AgentProcess getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * 初始化接口 config,不调用初始化接口: 获取不到key/channel,页面自动采集失效,电池信息采集失效
     */
    public void init(final Context context, final AnalysysConfig config) {
        CrashHandler.getInstance().setCallback(new CrashHandler.CrashCallBack() {
            @Override
            public void onAppCrash(Throwable e) {
                CrashHandler.getInstance().reportException(context,e,CrashHandler.CrashType.crash_auto);
            }
        });
        if (config != null) {
            mConfig = config;
        }
        AnalysysUtil.init(context);
        ActivityLifecycleUtils.initLifecycle();
        ActivityLifecycleUtils.addCallback(new AutomaticAcquisition());
        ANSThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (context != null) {
                        TemplateManage.initMould(context);
                        saveKey(context, mConfig.getAppKey());
                        saveChannel(context, mConfig.getChannel());
                        if (CommonUtils.isMainProcess(context)) {
                            // 同时设置 UploadUrl/WebSocketUrl/ConfigUrl
                            setBaseUrl(context, mConfig.getBaseUrl());
                            // 设置首次启动是否发送
                            Constants.isAutoProfile = mConfig.isAutoProfile();
                            // 设置加密类型
                            Constants.encryptType = mConfig.getEncryptType().getType();
                            // 设置渠道归因是否开启
                            Constants.autoInstallation = mConfig.isAutoInstallation();
                            // 重置PV计数器值
                            CommonUtils.resetCount(context.getFilesDir().getAbsolutePath());
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
                        LifeCycleConfig.initUploadConfig(context);
                        LogPrompt.showInitLog(true);
                    } else {
                        LogPrompt.showInitLog(false);
                    }
                } catch (Throwable ignored) {
                }
            }
        });
    }

    /**
     * debug 信息处理
     */
    public void setDebug(final int debug) {
        ANSThreadPool.execute(new Runnable() {
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
    public void appStart(final boolean isFromBackground, long startTime) {
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
            JSONObject eventData = DataAssemble.getInstance(context).getEventData(
                    Constants.API_APP_START, Constants.STARTUP, null, startUpMap);
            eventData.put(Constants.X_WHEN, startTime);
            trackEvent(context, Constants.API_APP_START, Constants.STARTUP, eventData);
            if (CommonUtils.isFirstStart(context)) {
                sendProfileSetOnce(context, 0);
                if (Constants.autoInstallation) {
                    sendFirstInstall(context);
                }
            }
        } catch (Throwable ignored) {
        }
    }

    /**
     * 应用关闭
     */
    public void appEnd(String eventTime, final JSONObject realTimeField) {
        try {
            long time = NumberFormat.convertToLong(eventTime);
            if (time > 0) {
                Context context = AnalysysUtil.getContext();
                if (context != null && realTimeField != null) {
                    JSONObject endData = DataAssemble.getInstance(context).getEventData(
                            Constants.API_APP_END, Constants.END, null, null);
                    endData.put(Constants.X_WHEN, time);
                    JSONObject xContData = endData.optJSONObject(Constants.X_CONTEXT);
                    CommonUtils.mergeJson(realTimeField, xContData);
                    endData.put(Constants.X_CONTEXT, xContData);
                    trackEvent(context, Constants.API_APP_END, Constants.END, endData);
                }
            }
        } catch (Throwable ignored) {
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
        ANSThreadPool.execute(new Runnable() {
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
                            Constants.API_PAGE_VIEW, Constants.PAGE_VIEW,
                            pageInfo, autoCollectPageInfo);

                    trackEvent(context, Constants.API_PAGE_VIEW, Constants.PAGE_VIEW, eventData);
                } catch (Throwable ignored) {
                }
            }
        });
    }

    /**
     * hybrid 使用 pageView 方法
     */
    public void hybridPageView(final String pageName, final Map<String, Object> pageDetail) {
        ANSThreadPool.execute(new Runnable() {
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
                            Constants.API_PAGE_VIEW,
                            Constants.PAGE_VIEW, pageInfo, null);
                    trackEvent(context,
                            Constants.API_PAGE_VIEW, Constants.PAGE_VIEW, eventData);
                } catch (Throwable ignored) {
                }
            }
        });
    }

    /**
     * 页面信息处理
     */
    public void autoCollectPageView(final Map<String, Object> pageInfo) throws Exception {
        Context context = AnalysysUtil.getContext();
        if (context != null) {
            JSONObject eventData = DataAssemble.getInstance(context).getEventData(
                    Constants.API_PAGE_VIEW, Constants.PAGE_VIEW, pageInfo, null);
            trackEvent(context, Constants.API_PAGE_VIEW, Constants.PAGE_VIEW, eventData);
        }
    }

    /**
     * Touch事件处理
     */
    void pageTouchInfo(final Map<String, Object> screenDetail) {
        try {
            Context context = AnalysysUtil.getContext();
            Map<String, Object> screenInfo = CommonUtils.deepCopy(screenDetail);
            if (context != null) {
                JSONObject eventData = DataAssemble.getInstance(context).getEventData(
                        Constants.API_APP_CLICK, Constants.APP_CLICK, null, screenInfo);
                trackEvent(context, Constants.API_APP_CLICK, Constants.APP_CLICK, eventData);
            }
        } catch (Throwable ignored) {
        }
    }

    /**
     * 多个事件信息处理
     */
    public void track(final String eventName, final Map<String, Object> eventDetail) {
        ANSThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Map<String, Object> eventInfo = CommonUtils.deepCopy(eventDetail);
                    Context context = AnalysysUtil.getContext();
                    if (context == null) {
                        LogPrompt.showLog(Constants.API_TRACK, false);
                        return;
                    }
                    JSONObject eventData;
                    if (isPushTrack(eventName)) {
                        updateLastOperateTime(context);
                        eventData = DataAssemble.getInstance(context).getEventData(
                                Constants.API_TRACK, Constants.TRACK,
                                null, eventInfo, eventName);
                    } else {
                        eventData = DataAssemble.getInstance(context).getEventData(
                                Constants.API_TRACK, Constants.TRACK,
                                eventInfo, null, eventName);
                    }
                    trackEvent(context, Constants.API_TRACK, eventName, eventData);
                } catch (Throwable ignored) {
                }
            }
        });
    }

    private void updateLastOperateTime(Context context) {
        // 判断 session 是否需要重置
        SessionManage.getInstance(context).resetSession(false);
        // 更新最后一次用户操作时间
        CommonUtils.setIdFile(context, Constants.SP_LAST_PAGE_CHANGE,
                String.valueOf(System.currentTimeMillis()));
    }

    /**
     * distinct id 存储
     */
    public void identify(final String distinctId) {
        ANSThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                Context context = AnalysysUtil.getContext();
                if (context == null) {
                    return;
                }
                if (!CheckUtils.checkIdLength(distinctId)) {
                    LogPrompt.showLog(Constants.SP_DISTINCT_ID, LogBean.getLog());
                    return;
                }
                CommonUtils.setIdFile(context, Constants.SP_DISTINCT_ID, distinctId);

            }
        });
    }

    /**
     * 获取distinctId
     */
    public String getDistinctId() {
        Context context = AnalysysUtil.getContext();
        if (context != null) {
            return CommonUtils.getDistinctId(context);
        }
        return "";
    }

    /**
     * alias id
     */
    public void alias(final String aliasId, final String originalId) {
        ANSThreadPool.execute(new Runnable() {
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
                            original = CommonUtils.getDistinctId(context);
                        } else {
                            SharedUtil.setString(context, Constants.SP_ORIGINAL_ID, original);
                        }
                        CommonUtils.setIdFile(context, Constants.SP_ALIAS_ID, aliasId);
                        SharedUtil.setInt(context, Constants.SP_IS_LOGIN, 1);

                        Map<String, Object> aliasMap = new HashMap<>();
                        aliasMap.put(Constants.ORIGINAL_ID, original);

                        JSONObject eventData = DataAssemble.getInstance(context).getEventData(
                                Constants.API_ALIAS, Constants.ALIAS, aliasMap, null);

                        if (!CommonUtils.isEmpty(eventData)) {
                            trackEvent(context, Constants.API_ALIAS, Constants.ALIAS, eventData);
                            sendProfileSetOnce(context, 0);
                        } else {
                            LogPrompt.showLog(Constants.API_ALIAS, false);
                        }
                    }
                } catch (Throwable throwable) {
                }
            }
        });
    }

    public void alias(final String aliasId) {
        ANSThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Context context = AnalysysUtil.getContext();
                    if (context != null) {
                        if (!CheckUtils.checkIdLength(aliasId)) {
                            LogPrompt.showLog(Constants.API_ALIAS, LogBean.getLog());
                            return;
                        }
                        String original = CommonUtils.getDistinctId(context);
                        SharedUtil.setString(context, Constants.SP_ORIGINAL_ID, original);
                        CommonUtils.setIdFile(context, Constants.SP_ALIAS_ID, aliasId);
                        SharedUtil.setInt(context, Constants.SP_IS_LOGIN, 1);

                        Map<String, Object> aliasMap = new HashMap<>();
                        aliasMap.put(Constants.ORIGINAL_ID, original);

                        JSONObject eventData = DataAssemble.getInstance(context).getEventData(
                                Constants.API_ALIAS, Constants.ALIAS, aliasMap, null);

                        if (!CommonUtils.isEmpty(eventData)) {
                            trackEvent(context, Constants.API_ALIAS, Constants.ALIAS, eventData);
                            sendProfileSetOnce(context, 0);
                        } else {
                            LogPrompt.showLog(Constants.API_ALIAS, false);
                        }
                    }
                } catch (Throwable ignored) {
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
        ANSThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Context context = AnalysysUtil.getContext();
                    if (context != null) {
                        Map<String, Object> profileInfo = CommonUtils.deepCopy(profileDetail);
                        JSONObject eventData = DataAssemble.getInstance(context).getEventData(
                                Constants.API_PROFILE_SET,
                                Constants.PROFILE_SET, profileInfo, null);
                        trackEvent(context, Constants.API_PROFILE_SET,
                                Constants.PROFILE_SET, eventData);
                    }
                } catch (Throwable ignored) {
                }
            }
        });
    }

    /**
     * profileSetOnce json
     */
    public void profileSetOnce(final Map<String, Object> profileDetail) {
        ANSThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Context context = AnalysysUtil.getContext();
                    if (context != null) {
                        Map<String, Object> profileInfo = CommonUtils.deepCopy(profileDetail);
                        JSONObject eventData = DataAssemble.getInstance(context).getEventData(
                                Constants.API_PROFILE_SET_ONCE,
                                Constants.PROFILE_SET_ONCE, profileInfo, null);

                        trackEvent(context, Constants.API_PROFILE_SET_ONCE,
                                Constants.PROFILE_SET_ONCE, eventData);
                    } else {
                        LogPrompt.showLog(Constants.API_PROFILE_SET_ONCE, false);
                    }
                } catch (Throwable ignored) {
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
        ANSThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Context context = AnalysysUtil.getContext();
                    if (context != null) {
                        Map<String, ? extends Number> profileInfo =
                                CommonUtils.deepCopy(profileDetail);
                        JSONObject eventData = DataAssemble.getInstance(context).getEventData(
                                Constants.API_PROFILE_INCREMENT,
                                Constants.PROFILE_INCREMENT, profileInfo, null);

                        trackEvent(context, Constants.API_PROFILE_INCREMENT,
                                Constants.PROFILE_INCREMENT, eventData);
                    } else {
                        LogPrompt.showLog(Constants.API_PROFILE_INCREMENT, false);
                    }
                } catch (Throwable ignored) {
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
        ANSThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Context context = AnalysysUtil.getContext();
                    if (context != null) {
                        Map<String, Object> profileInfo = CommonUtils.deepCopy(profileDetail);
                        JSONObject eventData = DataAssemble.getInstance(context).getEventData(
                                Constants.API_PROFILE_APPEND,
                                Constants.PROFILE_APPEND, profileInfo, null);

                        trackEvent(context, Constants.API_PROFILE_APPEND,
                                Constants.PROFILE_APPEND, eventData);
                    } else {
                        LogPrompt.showLog(Constants.API_PROFILE_APPEND, false);
                    }
                } catch (Throwable ignored) {
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
        ANSThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Context context = AnalysysUtil.getContext();
                    if (context != null && !CommonUtils.isEmpty(propertyKey)) {
                        Map<String, Object> profileMap = new HashMap<>();
                        profileMap.put(propertyKey, "");

                        JSONObject eventData = DataAssemble.getInstance(context).getEventData(
                                Constants.API_PROFILE_UNSET,
                                Constants.PROFILE_UNSET, profileMap, null);

                        trackEvent(context, Constants.API_PROFILE_UNSET,
                                Constants.PROFILE_UNSET, eventData);

                    } else {
                        LogPrompt.showLog(Constants.API_PROFILE_UNSET, false);
                    }
                } catch (Throwable ignored) {
                }
            }
        });
    }

    /**
     * 清除所有用户属性
     */
    public void profileDelete() {
        ANSThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Context context = AnalysysUtil.getContext();
                    if (context == null) {
                        return;
                    }
                    JSONObject eventData = DataAssemble.getInstance(context).getEventData(
                            Constants.API_PROFILE_DELETE,
                            Constants.PROFILE_DELETE, null, null);

                    trackEvent(context, Constants.API_PROFILE_DELETE,
                            Constants.PROFILE_DELETE, eventData);

                } catch (Throwable ignored) {
                }
            }
        });
    }

    /**
     * 注册单条通用属性
     */
    public void registerSuperProperty(final String key, final Object value) {
        ANSThreadPool.execute(new Runnable() {
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
                        saveSuperProperty(context, property);
                    } else {
                        LogPrompt.showLog(Constants.API_REGISTER_SUPER_PROPERTY, LogBean.getLog());
                    }
                } catch (Throwable ignored) {
                }

            }
        });
    }

    /**
     * 注册多条公共属性
     */
    public void registerSuperProperties(final Map<String, Object> propertyDetail) {
        ANSThreadPool.execute(new Runnable() {
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
                        saveSuperProperty(context, propertyInfo);
                    }
                } catch (Throwable ignored) {
                }
            }

        });
    }


    /**
     * 用户获取super Property
     */
    public Map<String, Object> getSuperProperty() {
        try {
            Context context = AnalysysUtil.getContext();
            if (context == null) {
                return new HashMap<>();
            }
            String superProperty = SharedUtil.getString(context,
                    Constants.SP_SUPER_PROPERTY, null);
            if (!CommonUtils.isEmpty(superProperty)) {
                return CommonUtils.jsonToMap(new JSONObject(superProperty));
            }
        } catch (Throwable ignored) {
        }
        return new HashMap<>();
    }

    /**
     * 用户获取超级属性
     */
    public Object getSuperProperty(String propertyKey) {
        try {
            Context context = AnalysysUtil.getContext();
            if (context != null && !CommonUtils.isEmpty(propertyKey)) {
                String superProperty = SharedUtil.getString(
                        context, Constants.SP_SUPER_PROPERTY, null);
                if (!CommonUtils.isEmpty(superProperty)) {
                    return new JSONObject(superProperty).opt(propertyKey);
                }
            }
        } catch (Throwable ignored) {
        }
        return null;
    }

    /**
     * 删除超级属性
     */
    public void unregisterSuperProperty(final String superPropertyName) {
        ANSThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Context context = AnalysysUtil.getContext();
                    if (context != null && !CommonUtils.isEmpty(superPropertyName)) {
                        String property = SharedUtil.getString(
                                context, Constants.SP_SUPER_PROPERTY, null);

                        if (!CommonUtils.isEmpty(property)) {
                            JSONObject json = new JSONObject(property);
                            json.remove(superPropertyName);
                            SharedUtil.setString(
                                    context, Constants.SP_SUPER_PROPERTY, String.valueOf(json));
                            LogPrompt.showLog(
                                    Constants.API_UNREGISTER_SUPER_PROPERTY, true);
                        }
                    }
                } catch (Throwable ignored) {
                }
            }
        });
    }

    /**
     * 删除所有超级属性
     */
    public void clearSuperProperty() {
        ANSThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Context context = AnalysysUtil.getContext();
                    if (context != null) {
                        SharedUtil.remove(context, Constants.SP_SUPER_PROPERTY);
                        LogPrompt.showLog(
                                Constants.API_CLEAR_SUPER_PROPERTIES, true);
                    }
                } catch (Throwable ignored) {
                }
            }
        });
    }

    /**
     * 用户自定义上传间隔时间
     */
    public void setIntervalTime(final long time) {
        ANSThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                Context context = AnalysysUtil.getContext();
                if (context != null && 1 < time) {
                    SharedUtil.setLong(
                            context, Constants.SP_USER_INTERVAL_TIME, time * 1000);
                }
            }
        });
    }

    /**
     * 用户自定义上传条数
     */
    public void setMaxEventSize(final long count) {
        ANSThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                Context context = AnalysysUtil.getContext();
                if (context != null && 1 < count) {
                    SharedUtil.setLong(
                            context, Constants.SP_USER_EVENT_COUNT, count);
                }
            }
        });
    }

    /**
     * 获取最大缓存条数
     */
    public long getMaxCacheSize() {
        long count;
        count = cacheMaxCount;
        if (count < 1) {
            count = Constants.MAX_CACHE_COUNT;
        }
        return count;
    }

    /**
     * 设置最大缓存条数
     */
    public void setMaxCacheSize(final long count) {
        ANSThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                if (100 < count) {
                    cacheMaxCount = count;
                }
            }
        });
    }

    /**
     * 清除本地所有id、超级属性、重新生成id
     */
    public void reset() {
        ANSThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Context context = AnalysysUtil.getContext();
                    if (context != null) {
                        resetUserInfo(context);
                        CommonUtils.setIdFile(context, Constants.SP_UUID, "");
                        LogPrompt.showLog(Constants.API_RESET, true);
                        sendProfileSetOnce(context, 1);
                    }
                } catch (Throwable throwable) {
                    LogPrompt.showLog(Constants.API_RESET, false);
                }
            }
        });
    }


    /**
     * 存储url
     */
    public void setUploadURL(final String url) {
        ANSThreadPool.execute(new Runnable() {
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
     * 是否自动采集
     */
    public void automaticCollection(final boolean isAuto) {
        ANSThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                Context context = AnalysysUtil.getContext();
                if (context != null) {
                    SharedUtil.setBoolean(context, Constants.SP_IS_COLLECTION, isAuto);
                }
            }
        });
    }

    /**
     * 获取自动采集开关状态
     */
    public boolean getAutomaticCollection() {
        Context context = AnalysysUtil.getContext();
        if (context != null) {
            return SharedUtil.getBoolean(
                    context, Constants.SP_IS_COLLECTION, true);
        }
        return true;
    }

    /**
     * 获取忽略自动采集的页面
     */
    public List<String> getIgnoredAutomaticCollection() {
        Context context = AnalysysUtil.getContext();
        if (context != null) {
            String activities = SharedUtil.getString(
                    context, Constants.SP_IGNORED_COLLECTION, null);
            if (!CommonUtils.isEmpty(activities)) {
                return CommonUtils.toList(activities);
            }
        }
        return new ArrayList<>();
    }

    /**
     * 忽略多个页面自动采集
     */
    public void setIgnoredAutomaticCollection(final List<String> activitiesName) {
        ANSThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Context context = AnalysysUtil.getContext();
                    if (context == null) {
                        return;
                    }
                    if (CommonUtils.isEmpty(activitiesName)) {
                        SharedUtil.remove(context, Constants.SP_IGNORED_COLLECTION);
                        return;
                    }
                    String activities = SharedUtil.getString(context,
                            Constants.SP_IGNORED_COLLECTION, null);
                    if (CommonUtils.isEmpty(activities)) {
                        SharedUtil.setString(
                                context, Constants.SP_IGNORED_COLLECTION,
                                CommonUtils.toString(activitiesName));
                    } else {
                        Set<String> pageNames = CommonUtils.toSet(activities);
                        if (pageNames == null) {
                            pageNames = new HashSet<>();
                        }
                        pageNames.addAll(activitiesName);
                        SharedUtil.setString(context,
                                Constants.SP_IGNORED_COLLECTION,
                                CommonUtils.toString(pageNames));
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
        ANSThreadPool.execute(new Runnable() {
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
                    properties.put("$first_visit_time", CommonUtils.getFirstStartTime(context));
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
        } catch (Throwable ignored) {
        }
        return new HashMap<>();
    }

    /**
     * 拦截监听 URL
     */
    public void interceptUrl(String url, Object view) {
        try {
            if (!CommonUtils.isEmpty(url)) {
                String decodedURL = java.net.URLDecoder.decode(url, "UTF-8");
                if (decodedURL.startsWith(ANALYSYS_AGENT)) {
                    HybridBridge.getInstance().execute(decodedURL, view);
                }
            }
        } catch (Throwable ignored) {
        }
    }

    /**
     * UA 增加 AnalysysAgent/Hybrid 字段
     */
    public void setHybridModel(Object webView) {
        if (!CommonUtils.isEmpty(webView)) {
            try {
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
            } catch (Throwable ignored) {
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
            }
        } catch (Throwable ignored) {
        }
    }

    /**
     * 设置可视化base mUrl
     */
    private void setVisitorBaseURL(final String url) {
        ANSThreadPool.execute(new Runnable() {
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
                } catch (Throwable ignored) {
                }
            }
        });
    }

    /**
     * 设置可视化websocket服务器地址
     */
    public void setVisitorDebugURL(final String url) {
        ANSThreadPool.execute(new Runnable() {
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
                } catch (Throwable ignored) {
                }
            }
        });
    }

    /**
     * 设置线上请求埋点配置的服务器地址
     */
    public void setVisitorConfigURL(final String url) {
        ANSThreadPool.execute(new Runnable() {
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
                } catch (Throwable ignored) {
                }
            }
        });
    }

    public void enablePush(final String provider, final String pushId) {
        ANSThreadPool.execute(new Runnable() {
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
                } catch (Throwable ignored) {
                }
            }
        });
    }

    public void trackCampaign(final String campaign,
                              final boolean isClick, final PushListener listener) {
        ANSThreadPool.execute(new Runnable() {
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
                } catch (Throwable ignored) {
                }
            }
        });
    }

    /**
     * 存储 upload url
     */
    private void saveUploadUrl(Context context, String uploadUrl) throws MalformedURLException {
        // 判断是否进行时间校准且为主进程
        if (Constants.isTimeCheck && CommonUtils.isMainProcess(context)) {
            UploadManager.getInstance(context).sendGetTimeMessage();
        }
        changeUrlResetUser(context, uploadUrl);
        SharedUtil.setString(context, Constants.SP_USER_URL, uploadUrl);
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
                resetUserInfo(context);
            }
            LogPrompt.showKeyLog(true, key);
        } else {
            LogPrompt.showKeyLog(false, key);
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
            LogPrompt.showChannelLog(true, channel);
        } else {
            LogPrompt.showChannelLog(false, channel);
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
    private void sendFirstInstall(Context context) throws Exception {
        if (context != null) {
            JSONObject eventData = DataAssemble.getInstance(context).getEventData(
                    Constants.API_FIRST_INSTALL, Constants.FIRST_INSTALL,
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
            resetUserInfo(context);
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
    private void sendProfileSetOnce(Context context, int type) throws Exception {
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
                    Constants.API_PROFILE_SET_ONCE,
                    Constants.PROFILE_SET_ONCE, null, profileInfo);
            trackEvent(context,
                    Constants.API_PROFILE_SET_ONCE, Constants.PAGE_VIEW, eventData);
        }
    }

    /**
     * 存储通用属性
     */
    private void saveSuperProperty(Context context,
                                   Map<String, Object> superProperty) throws Exception {
        JSONObject propertyInfo;
        if (!CommonUtils.isEmpty(superProperty)) {
            String sharedProperty = SharedUtil.getString(
                    context, Constants.SP_SUPER_PROPERTY, null);
            if (!CommonUtils.isEmpty(sharedProperty)) {
                propertyInfo = new JSONObject(sharedProperty);
                CommonUtils.mergeJson(new JSONObject(superProperty), propertyInfo);
            } else {
                propertyInfo = new JSONObject(superProperty);
            }
            SharedUtil.setString(
                    context, Constants.SP_SUPER_PROPERTY, String.valueOf(propertyInfo));
        }
    }

    /**
     * reset 接口 重置所有属性
     */
    private void resetUserInfo(Context context) {
        // 重置首次访问
        SharedUtil.remove(context, Constants.SP_FIRST_START_TIME);
        // 重置首日访问
        SharedUtil.remove(context, Constants.DEV_IS_FIRST_DAY);
        // 重置 通用属性
        SharedUtil.remove(context, Constants.SP_SUPER_PROPERTY);
        // 重置 alias id
        CommonUtils.setIdFile(context, Constants.SP_ALIAS_ID, "");
        // 重置identify
        CommonUtils.setIdFile(context, Constants.SP_DISTINCT_ID, "");
        // 修改 isLogin
        SharedUtil.remove(context, Constants.SP_IS_LOGIN);
        // 重置original id
        SharedUtil.remove(context, Constants.SP_ORIGINAL_ID);
        //  清空数据库
        TableAllInfo.getInstance(context).deleteAll();
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
                resetUserInfo(context);
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
            UploadManager.getInstance(context).sendManager(eventName, eventData);
        }
    }

    /**
     * 校验数据是否符合上传格式
     */
    private boolean checkoutEvent(JSONObject eventData) {
        if (CommonUtils.isEmpty(eventData.optString(Constants.APP_ID))) {
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

    public AnalysysConfig getConfig() {
        return mConfig;
    }

//    /**
//     * 设置远程调试
//     */
//    private void setDebugging(int debug) {
//        try {
//            if (Build.VERSION.SDK_INT >= 19 && (debug == 1 || debug == 2)) {
//                WebView.setWebContentsDebuggingEnabled(true);
//            }
//        } catch (Throwable t) {
//
//        }
//    }
//
//
//    /**
//     * 初始化流量审计SDK
//     */
//    private void deviceInit() {
//        CommonUtils.reflexUtils(
//                "com.analysys.track.AnalysysTracker",
//                "init",
//                new Class[]{Context.class, String.class, String.class},
//                mContext, "", "");
//    }

}
