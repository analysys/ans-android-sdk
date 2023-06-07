package com.analysys;

import android.content.Context;
import android.hardware.SensorManager;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;

import com.analysys.process.AgentProcess;
import com.analysys.push.PushListener;
import com.analysys.thread.AnsLogicThread;
import com.analysys.thread.PriorityCallable;
import com.analysys.utils.ANSLog;
import com.analysys.utils.AnalysysSSManager;
import com.analysys.utils.AnalysysUtil;
import com.analysys.utils.CommonUtils;
import com.analysys.utils.Constants;
import com.analysys.utils.CrashHandler;
import com.analysys.utils.ExceptionUtil;
import com.analysys.utils.InitChecker;
import com.analysys.utils.SharedUtil;

import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Copyright © 2018 EGuan Inc. All rights reserved.
 * @Description: API
 * @Version: 1.0
 * @Create: 2018/2/2
 * @Author: Wang-X-C
 */
public class AnalysysAgent {
    /**
     * 初始化接口
     *
     * @param config 初始化配置信息
     */
    public static void init(Context context, AnalysysConfig config) {
        if (!AgentProcess.getInstance().isDataCollectEnable()) {
            return;
        }
        AgentProcess.getInstance().init(context, config);
    }

    /**
     * 上报激活接口, 针对uniapp增加
     * */
    public static void trackFirstInstall() {
        ANSLog.d("trackFirstInstall");
        if (AnalysysUtil.getContext() == null) {
            return;
        }
        ANSLog.d("trackFirstInstall2");
        if (CommonUtils.isFirstStart(AnalysysUtil.getContext())) {
            // 逻辑不熟悉，先保证能发送
            ANSLog.d("trackFirstInstall4");
            Constants.autoInstallation = true;
            Constants.isFirstInstall = true;
            AgentProcess.getInstance().appStart(false, System.currentTimeMillis());
        }
    }

    /**
     * 页面关闭事件上报接口, 针对uniapp增加
     * */
    public static void trackPageClose() {
        if (AnalysysUtil.getContext() == null) {
            return;
        }
        String txt = SharedUtil.getString(AnalysysUtil.getContext(), Constants.PAGE_CLOSE_INFO, "");
        if (TextUtils.isEmpty(txt)) {
            return;
        }
        if (!AgentProcess.getInstance().getConfig().isAutoPageViewDuration()) {
            if (!TextUtils.isEmpty(txt)) {
                SharedUtil.setString(AnalysysUtil.getContext(), Constants.PAGE_CLOSE_INFO, null);
            }
            return;
        }
        try {
            txt = new String(Base64.decode(txt.getBytes(), Base64.NO_WRAP));
            JSONObject jo = new JSONObject(txt);
            long startTime = jo.getLong(Constants.PV_START_TIME);
            String timeStr = SharedUtil.getString(AnalysysUtil.getContext(), Constants.LAST_OP_TIME, "");
            long time = CommonUtils.parseLong(timeStr, 0);
            long pageStayTime = time - startTime;
            if (pageStayTime > 0) {
                jo.put(Constants.PAGE_STAY_TIME, pageStayTime);
                jo.remove(Constants.PV_START_TIME);
                AgentProcess.getInstance().track(Constants.PAGE_CLOSE, CommonUtils.jsonToMap(jo), time);
            }
            SharedUtil.setString(AnalysysUtil.getContext(), Constants.PAGE_CLOSE_INFO, null);
        } catch (Throwable e) {
            ExceptionUtil.exceptionPrint(e);
        }
    }

    /**
     * 不采集页面热图
     * @param pages 忽略的页面集合
     */
    public static void setHeatMapBlackListByPages(List<String> pages) {
        if (!AgentProcess.getInstance().isDataCollectEnable()) {
            return;
        }
        AgentProcess.getInstance().setHeatMapBlackListByPages(pages);
    }


    /**
     * 采集热图页面百名单
     * @param pages 只采集的页面集合
     */
    public static void setHeatMapWhiteListByPages(List<String> pages) {
        if (!AgentProcess.getInstance().isDataCollectEnable()) {
            return;
        }
        AgentProcess.getInstance().setHeatMapWhiteListByPages(pages);
    }

    /**
     * PageView自动上报-设置页面级黑名单
     * @param pages 页面名称列表
     */
    public static void setPageViewBlackListByPages(List<String> pages) {
        if (!AgentProcess.getInstance().isDataCollectEnable()) {
            return;
        }
        AgentProcess.getInstance().setPageViewBlackListByPages(pages);
    }

    /**
     * PageView自动上报-设置页面级白名单
     * @param pages
     */
    public static void setPageViewWhiteListByPages(List<String> pages){
        if (!AgentProcess.getInstance().isDataCollectEnable()) {
            return;
        }
        AgentProcess.getInstance().setPageViewWhiteListByPages(pages);
    }

    /**
     * 点击自动上报-设置页面级黑名单
     * @param pages 页面名称列表
     */
    public static void setAutoClickBlackListByPages(List<String> pages) {
        if (!AgentProcess.getInstance().isDataCollectEnable()) {
            return;
        }
        AgentProcess.getInstance().setAutoClickBlackListByPages(pages);
    }
    /**
     * 点击自动上报-设置页面级白名单
     *
     * @param pages 页面名称列表
     */
    public static void setAutoClickWhiteListByPages(List<String> pages) {
        if (!AgentProcess.getInstance().isDataCollectEnable()) {
            return;
        }
        AgentProcess.getInstance().setAutoClickWhiteListByPages(pages);
    }

    /**
     * 点击自动上报-设置元素类型级黑名单
     * @param viewTypes 控件元素类列表
     */
    public static void setAutoClickBlackListByViewTypes(List<Class> viewTypes) {
        if (!AgentProcess.getInstance().isDataCollectEnable()) {
            return;
        }
        AgentProcess.getInstance().setAutoClickBlackListByViewTypes(viewTypes);
    }

    /**
     * 点击自动上报-设置元素类型级白名单
     * @param viewTypes 控件元素类列表
     */
    public static void setAutoClickWhiteListByViewTypes(List<Class> viewTypes) {
        if (!AgentProcess.getInstance().isDataCollectEnable()) {
            return;
        }
        AgentProcess.getInstance().setAutoClickWhiteListByViewTypes(viewTypes);
    }


    /**
     * 点击自动上报-设置元素级黑名单
     * @param element 控件元素对象
     */
    public static void setAutoClickBlackListByView(View element) {
        if (!AgentProcess.getInstance().isDataCollectEnable()) {
            return;
        }
        AgentProcess.getInstance().setAutoClickBlackListByView(element);
    }


    /**
     * 点击自动上报-设置元素类型级白名单
     *
     * @param element 控件元素对象
     */
    public static void setAutoClickWhiteListByView(View element) {
        if (!AgentProcess.getInstance().isDataCollectEnable()) {
            return;
        }
        AgentProcess.getInstance().setAutoClickWhiteListByView(element);
    }


    /**
     * debug 模式
     *
     * @param debugMode 0、关闭debug模式
     * 1、打开Debug模式,但该模式下发送的数据仅用于调试，不计入平台数据统计
     * 2、打开Debug模式,该模式下发送的数据可计入平台数据统计
     */
    public static void setDebugMode(Context context, int debugMode) {
        AnalysysUtil.init(context);
        if (!AgentProcess.getInstance().isDataCollectEnable()) {
            return;
        }
        AgentProcess.getInstance().setDebug(context, debugMode);
    }

    /**
     * 设置上传数据地址
     *
     * @param url 数据上传地址,
     * 数据上传地址必须以"http://"或"https://"开头,必须携带端口号
     * 长度小于255字符
     */
    public static void setUploadURL(Context context, String url) {
        if (!AgentProcess.getInstance().isDataCollectEnable()) {
            return;
        }
        AgentProcess.getInstance().setUploadURL(url);
    }

    /**
     * 设置时间和用户属性的监听
     *
     * @param context
     * @param listener
     */
    public static void setObserverListener(Context context, ObserverListener listener) {
        if (!AgentProcess.getInstance().isDataCollectEnable()) {
            return;
        }
        AgentProcess.getInstance().setObserverListener(listener);
    }

    /**
     * 设置上传间隔时间
     * debug 设置为 0 时,数据上传时间间隔,单位:秒
     *
     * @param flushInterval 间隔时间,time值大于1
     */
    public static void setIntervalTime(Context context, long flushInterval) {
        if (!AgentProcess.getInstance().isDataCollectEnable()) {
            return;
        }
        AgentProcess.getInstance().setIntervalTime(flushInterval);
    }

    /**
     * 本地缓存上限值
     *
     * @param size 上传条数,count值大于1
     */
    public static void setMaxCacheSize(Context context, long size) {
        if (!AgentProcess.getInstance().isDataCollectEnable()) {
            return;
        }
        AgentProcess.getInstance().setMaxCacheSize(size);
    }

    /**
     * 读取最大缓存条数
     */
    public static long getMaxCacheSize(Context context) {
        return (long) AnsLogicThread.sync(new PriorityCallable(AnsLogicThread.PriorityLevel.HIGH) {
            @Override
            public Object call() throws Exception {
                return AgentProcess.getInstance().getMaxCacheSize();
            }
        });
    }

    /**
     * 设置事件最大上传条数
     * 当 debug 设置 0 时,数据累积条数大于设置条数后触发上传
     *
     * @param size 上传条数,size值大于1
     */
    public static void setMaxEventSize(Context context, long size) {
        if (!AgentProcess.getInstance().isDataCollectEnable()) {
            return;
        }
        AgentProcess.getInstance().setMaxEventSize(size);
    }

    /**
     * 手动上传
     * 调用该接口立即上传数据
     */
    public static void flush(Context context) {
        if (!AgentProcess.getInstance().isDataCollectEnable()) {
            return;
        }
        if (InitChecker.check(Constants.API_FLUSH)) {
            AgentProcess.getInstance().flush();
        }
    }

    /**
     * 身份关联.
     * 新distinctID关联到原有originalID
     * originalID 原始id. 该变量将会映射到aliasID,
     *
     * @param aliasId 长度大于0且小于255字符
     * @param originalId 可以是现在使用也可以是历史使用的id,不局限于本地正使用的distinctId,
     * 若为空则使用本地的distinctId,长度大于0且小于255字符
     */
    @Deprecated
    public static void alias(Context context, String aliasId, String originalId) {
        if (!AgentProcess.getInstance().isDataCollectEnable()) {
            return;
        }
        if (InitChecker.check(Constants.API_ALIAS)) {
            AgentProcess.getInstance().alias(aliasId, originalId);
        }
    }

    public static void alias(Context context, String aliasId) {
        if (!AgentProcess.getInstance().isDataCollectEnable()) {
            return;
        }
        if (InitChecker.check(Constants.API_ALIAS)) {
            AgentProcess.getInstance().alias(aliasId);
        }
    }

    /**
     * 用户ID设置
     *
     * @param distinctId 唯一身份标识,长度大于0且小于255字符
     */
    public static void identify(Context context, String distinctId) {
        if (!AgentProcess.getInstance().isDataCollectEnable()) {
            return;
        }
        AgentProcess.getInstance().identify(distinctId);
    }

    /**
     * 获取匿名 id
     * 如果用户通过identify接口设置，则返回设置id ；
     * 如未设置，则返回代码自动生成的uuid
     */
    public static String getDistinctId(Context context) {
        return AgentProcess.getInstance().getDistinctId();
    }

    /**
     * 清除本地设置
     */
    public static void reset(Context context) {
        if (!AgentProcess.getInstance().isDataCollectEnable()) {
            return;
        }
        if (InitChecker.check(Constants.API_RESET)) {
            AgentProcess.getInstance().reset();
        }
    }

    /**
     * 添加事件
     *
     * @param eventName 事件名称,以字母或$开头,可以包含大小写字母/数字/ _ / $,不支持中文和乱码,最大长度99字符
     */
    public static void track(Context context, String eventName) {
        if (!AgentProcess.getInstance().isDataCollectEnable()) {
            return;
        }
        if (InitChecker.check(Constants.API_TRACK)) {
            AgentProcess.getInstance().track(eventName, null, System.currentTimeMillis());
        }
    }

    /**
     * 添加多属性事件
     *
     * @param eventName 事件名称,以字母或$开头,可以包含大小写字母/数字/ _ /$,不支持中文和乱码,长度必须小于99字符
     * @param properties 事件属性,最多包含100条,
     * 且key以字母或 $ 开头,包括大小写字母/数字/ _ / $,最大长度99字符,不支持乱码和中文,
     * value支持部分类型：String/Number/boolean/集合/数组,若为字符串,最大长度255字符
     */
    public static void track(Context context, String eventName, Map<String, Object> properties) {
        if (!AgentProcess.getInstance().isDataCollectEnable()) {
            return;
        }
        if (InitChecker.check(Constants.API_TRACK)) {
            AgentProcess.getInstance().track(eventName, properties, System.currentTimeMillis());
        }
    }

    /**
     * 添加页面
     *
     * @param pageName 页面标识，为字符串,最大长度255字符
     */
    public static void pageView(Context context, String pageName) {
        if (!AgentProcess.getInstance().isDataCollectEnable()) {
            return;
        }
        if (InitChecker.check(Constants.API_PAGE_VIEW)) {
            AgentProcess.getInstance().pageView(context, pageName, null);
        }
    }

    /**
     * 添加页面信息
     *
     * @param pageName 页面标识,字符串,最大长度255字符
     * @param properties 页面信息,最多包含100条,
     * 且key以字母或 $ 开头,包括大小写字母/数字/ _ / $,最大长度99字符,不支持乱码和中文,
     * value支持部分类型：String/Number/boolean/集合/数组,若为字符串,最大长度255字符
     */
    public static void pageView(Context context, String pageName, Map<String, Object> properties) {
        if (!AgentProcess.getInstance().isDataCollectEnable()) {
            return;
        }
        if (InitChecker.check(Constants.API_PAGE_VIEW)) {
            AgentProcess.getInstance().pageView(context, pageName, properties);
        }
    }

    /**
     * 应用启动来源，
     * 参数为 1. icon启动 默认值为icon启动
     * 参数为 2. msg 启动
     * 参数为 3. deepLink启动
     * 参数为 5. 其他方式启动
     */
    public static void launchSource(int source) {
        if (!AgentProcess.getInstance().isDataCollectEnable()) {
            return;
        }
        Constants.sourceNum = source;
    }


    /**
     * 注册单个通用属性
     *
     * @param superPropertyName 属性名称,以字母或$开头,可以包含大小写字母/数字/ _ /$,
     * 不支持中文和乱码,长度必须小于99字符
     * @param superPropertyValue 属性值,支持部分类型：String/Number/boolean/集合/数组;
     * 若为字符串,则最大长度255字符;
     * 若为数组或集合,则最多包含100条,且key约束条件与属性名称一致,value最大长度255字符
     */
    public static void registerSuperProperty(Context context, String superPropertyName,
                                             Object superPropertyValue) {
        if (!AgentProcess.getInstance().isDataCollectEnable()) {
            return;
        }
        AgentProcess.getInstance().registerSuperProperty(superPropertyName,
                superPropertyValue);
    }

    /**
     * 注册多个通用属性
     *
     * @param superProperty 事件公共属性,最多包含100条,
     * 且key以字母或 $ 开头,包括大小写字母/数字/ _ / $,最大长度99字符,不支持乱码和中文,
     * value支持部分类型：String/Number/boolean/集合/数组,若为字符串,最大长度255字符
     */
    public static void registerSuperProperties(Context context, Map<String, Object> superProperty) {
        if (!AgentProcess.getInstance().isDataCollectEnable()) {
            return;
        }
        AgentProcess.getInstance().registerSuperProperties(superProperty);
    }

    /**
     * 注册预置事件用户自定义属性
     * @param context
     * @param property
     */
    public static void registerPreEventUserProperties (Context context, Map<String,Object> property) {
        if (!AgentProcess.getInstance().isDataCollectEnable()) {
            return;
        }
        AgentProcess.getInstance().registerPreEventUserProperties(property);
    }

    public static void unRegisterPreEventUserProperties (Context context, String propertyName) {
        if (!AgentProcess.getInstance().isDataCollectEnable()) {
            return;
        }
        AgentProcess.getInstance().unregisterPreEventUserProperty(propertyName);
    }

    /**
     * 删除单个通用属性
     *
     * @param superPropertyName 属性名称,以字母或$开头,可以包含大小写字母/数字/ _ /$,
     * 不支持中文和乱码,长度必须小于99字符
     */
    public static void unRegisterSuperProperty(Context context, String superPropertyName) {
        if (!AgentProcess.getInstance().isDataCollectEnable()) {
            return;
        }
        AgentProcess.getInstance().unregisterSuperProperty(superPropertyName);
    }

    /**
     * 清除所有通用属性
     */
    public static void clearSuperProperties(Context context) {
        if (!AgentProcess.getInstance().isDataCollectEnable()) {
            return;
        }
        AgentProcess.getInstance().clearSuperProperty();
    }

    /**
     * 获取单个通用属性
     *
     * @param key 属性名称,以字母或$开头,可以包含大小写字母/数字/ _ /$,不支持中文和乱码,长度必须小于99字符
     */
    public static Object getSuperProperty(Context context, final String key) {
        return AnsLogicThread.sync(new PriorityCallable(AnsLogicThread.PriorityLevel.HIGH) {
            @Override
            public Object call() throws Exception {
                return AgentProcess.getInstance().getSuperProperty(key);
            }
        });
    }

    /**
     * 获取全部通用属性
     */
    public static Map<String, Object> getSuperProperties(Context context) {
        return (Map<String, Object>) AnsLogicThread.sync(new PriorityCallable(AnsLogicThread.PriorityLevel.HIGH) {
            @Override
            public Object call() throws Exception {
                return AgentProcess.getInstance().getSuperProperty();
            }
        });
    }

    /**
     * 设置用户单个属性,如果之前存在,则覆盖,否则,创建
     *
     * @param propertyName 属性名称,以字母或$开头,可以包含大小写字母/数字/ _ /$,不支持中文和乱码,长度必须小于99字符
     * @param propertyValue 属性的值,支持部分类型：String/Number/boolean/集合/数组;
     * 若为字符串,则最大长度255字符;
     * 若为数组或集合,则最多包含100条,且key约束条件与属性名称一致,value最大长度255字符
     */
    public static void profileSet(Context context, String propertyName, Object propertyValue) {
        if (!AgentProcess.getInstance().isDataCollectEnable()) {
            return;
        }
        if (InitChecker.check(Constants.API_PROFILE_SET)) {
            AgentProcess.getInstance().profileSet(propertyName, propertyValue);
        }
    }

    /**
     * 设置用户的多个属性,如果之前存在,则覆盖,否则,创建
     *
     * @param property 属性列表,最多包含100条,
     * 且key以字母或 $ 开头,包括大小写字母/数字/ _ / $,最大长度99字符,不支持乱码和中文,
     * value若为字符串,最大长度255字符
     */
    public static void profileSet(Context context, Map<String, Object> property) {
        if (!AgentProcess.getInstance().isDataCollectEnable()) {
            return;
        }
        if (InitChecker.check(Constants.API_PROFILE_SET)) {
            AgentProcess.getInstance().profileSet(property);
        }
    }

    /**
     * 设置用户单个固有属性
     *
     * @param propertyName 属性名称,以字母或$开头,可以包含大小写字母/数字/ _ /$,不支持中文和乱码,长度必须小于99字符
     * @param propertyValue 属性的值,支持部分类型：String/Number/boolean/集合/数组;
     * 若为字符串,则最大长度255字符;
     * 若为数组或集合,则最多包含100条,且key约束条件与属性名称一致,value最大长度255字符
     */
    public static void profileSetOnce(Context context, String propertyName, Object propertyValue) {
        if (!AgentProcess.getInstance().isDataCollectEnable()) {
            return;
        }
        if (InitChecker.check(Constants.API_PROFILE_SET_ONCE)) {
            AgentProcess.getInstance().profileSetOnce(propertyName, propertyValue);
        }
    }

    /**
     * 设置用户多个固有属性
     * 与用户行为相关的 属性设置 如首次启动
     *
     * @param property 属性列表,最多包含100条,
     * 且key以字母或 $ 开头,包括大小写字母/数字/ _ / $,最大长度99字符,不支持乱码和中文,
     * value若为字符串,最大长度255字符
     */
    public static void profileSetOnce(Context context, Map<String, Object> property) {
        if (!AgentProcess.getInstance().isDataCollectEnable()) {
            return;
        }
        if (InitChecker.check(Constants.API_PROFILE_SET_ONCE)) {
            AgentProcess.getInstance().profileSetOnce(property);
        }
    }

    /**
     * 设置用户属性的单个相对变化值(相对增加,减少)
     *
     * @param propertyName 属性名称,以字母或$开头,可以包含大小写字母/数字/ _ /$,不支持中文和乱码,长度必须小于99字符
     * @param propertyValue 属性的值,value支持类型:Number
     */
    public static void profileIncrement(Context context, String propertyName, Number
            propertyValue) {
        if (!AgentProcess.getInstance().isDataCollectEnable()) {
            return;
        }
        if (InitChecker.check(Constants.API_PROFILE_INCREMENT)) {
            AgentProcess.getInstance().profileIncrement(propertyName, propertyValue);
        }
    }

    /**
     * 设置用户属性的多个相对变化值(相对增加,减少)
     * 如年龄等
     *
     * @param property 属性列表,最多包含100条,
     * 且key以字母或 $ 开头,包括大小写字母/数字/ _ / $,最大长度99字符,不支持乱码和中文,
     */
    public static void profileIncrement(Context context, Map<String, Number> property) {
        if (!AgentProcess.getInstance().isDataCollectEnable()) {
            return;
        }
        if (InitChecker.check(Constants.API_PROFILE_INCREMENT)) {
            AgentProcess.getInstance().profileIncrement(property);
        }
    }

    /**
     * 列表类型属性增加单个元素
     */
    public static void profileAppend(Context context, String propertyName, Object propertyValue) {
        if (!AgentProcess.getInstance().isDataCollectEnable()) {
            return;
        }
        if (InitChecker.check(Constants.API_PROFILE_APPEND)) {
            AgentProcess.getInstance().profileAppend(propertyName, propertyValue);
        }
    }

    /**
     * 列表类型属性增加多个元素
     *
     * @param propertyValue 新增的元素,属性名称,以字母或$开头,可以包含大小写字母/数字/ _ /$,不支持中文和乱码,长度必须小于99字符
     * 支持部分类型：String/Number/boolean/集合/数组;
     * 若为字符串,则最大长度255字符;
     * 若为数组或集合,则最多包含100条,且key约束条件与属性名称一致,value最大长度255字符
     */
    public static void profileAppend(Context context, Map<String, Object> propertyValue) {
        if (!AgentProcess.getInstance().isDataCollectEnable()) {
            return;
        }
        if (InitChecker.check(Constants.API_PROFILE_APPEND)) {
            AgentProcess.getInstance().profileAppend(propertyValue);
        }
    }

    /**
     * 列表类型属性增加多个元素
     *
     * @param propertyName 属性名称,以字母或$开头,可以包含大小写字母/数字/ _ /$,不支持中文和乱码,长度必须小于99字符
     * @param propertyValue 新增的元素集合,支持类型:String/Number/boolean,
     * 集合内最多包含100条,若为字符串,最大长度255字符
     */
    public static void profileAppend(Context context, String propertyName,
                                     List<Object> propertyValue) {
        if (!AgentProcess.getInstance().isDataCollectEnable()) {
            return;
        }
        if (InitChecker.check(Constants.API_PROFILE_APPEND)) {
            AgentProcess.getInstance().profileAppend(propertyName, propertyValue);
        }
    }

    /**
     * 删除单个用户属性
     *
     * @param propertyName 属性名称,以字母或$开头,可以包含大小写字母/数字/ _ /$,不支持中文和乱码,长度必须小于99字符
     */
    public static void profileUnset(Context context, String propertyName) {
        if (!AgentProcess.getInstance().isDataCollectEnable()) {
            return;
        }
        if (InitChecker.check(Constants.API_PROFILE_UNSET)) {
            AgentProcess.getInstance().profileUnset(propertyName);
        }
    }

    /**
     * 清除所有用户的属性
     */
    public static void profileDelete(Context context) {
        if (!AgentProcess.getInstance().isDataCollectEnable()) {
            return;
        }
        if (InitChecker.check(Constants.API_PROFILE_DELETE)) {
            AgentProcess.getInstance().profileDelete();
        }
    }

    /**
     * 获取预置属性
     */
    public static Map<String, Object> getPresetProperties(Context context) {
        return AgentProcess.getInstance().getSyncPresetProperties();
    }

    /**
     * 设置可视化websocket服务器地址
     *
     * @param url 数据上传地址,
     * 数据上传地址必须以"ws://"或"wss://"开头,必须携带端口号
     * 长度小于255字符
     */
    public static void setVisitorDebugURL(Context context, String url) {
        if (!AgentProcess.getInstance().isDataCollectEnable()) {
            return;
        }
        if (InitChecker.check(Constants.API_SET_VISITOR_DEBUG_URL)) {
            AgentProcess.getInstance().setVisitorDebugURL(url);
        }
    }

    /**
     * 设置线上请求埋点配置的服务器地址
     *
     * @param url 数据上传地址,
     * 数据上传地址必须以"http://"或"https://"开头,必须携带端口号
     * 长度小于255字符
     */
    public static void setVisitorConfigURL(Context context, String url) {
        if (!AgentProcess.getInstance().isDataCollectEnable()) {
            return;
        }
        if (InitChecker.check(Constants.API_SET_VISITOR_CONFIG_URL)) {
            AgentProcess.getInstance().setVisitorConfigURL(url);
        }
    }

    /**
     * 设置推送ID
     *
     * @param provider 推送服务方
     * @param pushId 推送ID
     */
    public static void setPushID(Context context, String provider, String pushId) {
        if (!AgentProcess.getInstance().isDataCollectEnable()) {
            return;
        }
        if (InitChecker.check(Constants.API_SET_PUSH_ID)) {
            AgentProcess.getInstance().enablePush(provider, pushId);
        }
    }

    /**
     * 追踪活动推广
     *
     * @param campaign 活动推广消息
     * @param isClick 是否被点击
     */
    public static void trackCampaign(Context context, String campaign, boolean isClick) {
        if (!AgentProcess.getInstance().isDataCollectEnable()) {
            return;
        }
        if (InitChecker.check(Constants.API_TRACK_COMPAIGN)) {
            AgentProcess.getInstance().trackCampaign(campaign, isClick, null);
        }
    }

    /**
     * 追踪活动推广事件
     *
     * @param campaign 活动推广消息
     * @param isClick 活动通知是否点击打开
     * @param listener 活动推送的后续自定义属性和行为的监听
     */
    public static void trackCampaign(Context context, String campaign, boolean isClick,
                                     PushListener listener) {
        if (!AgentProcess.getInstance().isDataCollectEnable()) {
            return;
        }
        if (InitChecker.check(Constants.API_TRACK_COMPAIGN)) {
            AgentProcess.getInstance().trackCampaign(campaign, isClick, listener);
        }
    }

    /**
     * 拦截监听 URL
     */
    public static void interceptUrl(Context context, String url, Object webView) {
        if (!AgentProcess.getInstance().isDataCollectEnable()) {
            return;
        }
        if (InitChecker.check(Constants.API_INTERCEPT_URL)) {
            AgentProcess.getInstance().interceptUrl(url, webView);
        }
    }

    /**
     * 设置以注入方式实现Hybrid
     */
    public static void setAnalysysAgentHybrid(Object webView) {
        if (!AgentProcess.getInstance().isDataCollectEnable()) {
            return;
        }
        AgentProcess.getInstance().setAnalysysAgentHybrid(webView);
    }

    /**
     * 删除Hybrid注入
     */
    public static void resetAnalysysAgentHybrid(Object webView) {
        if (!AgentProcess.getInstance().isDataCollectEnable()) {
            return;
        }
        AgentProcess.getInstance().resetAnalysysAgentHybrid(webView);
    }

    /**
     * 设置UA
     */
    public static void setHybridModel(Context context, Object webView) {
        if (!AgentProcess.getInstance().isDataCollectEnable()) {
            return;
        }
        if (InitChecker.check(Constants.API_SET_HYBRID_MODEL)) {
            AgentProcess.getInstance().setHybridModel(webView);
        }
    }

    /**
     * 还原 User-Agent 中的字符串
     */
    public static void resetHybridModel(Context context, Object webView) {
        if (!AgentProcess.getInstance().isDataCollectEnable()) {
            return;
        }
        if (InitChecker.check(Constants.API_RESET_HYBRID_MODEL)) {
            AgentProcess.getInstance().resetHybridModel(webView);
        }
    }

    /**
     * 热图开启/关闭接口
     * is old PLZ call {@link com.analysys.AnalysysConfig#setAutoHeatMap(boolean)}
     */
    @Deprecated
    public static void setAutoHeatMap(boolean autoTrack) {
        if (!AgentProcess.getInstance().isDataCollectEnable()) {
            return;
        }
        AgentProcess.getInstance().getConfig().setAutoHeatMap(autoTrack);
    }

    /**
     * 自动采集页面信息
     * is old PLZ call {@link AnalysysConfig#setAutoTrackPageView(boolean)}
     *
     * @param isAuto 默认为true
     */
    @Deprecated
    public static void setAutomaticCollection(Context context, boolean isAuto) {
        if (!AgentProcess.getInstance().isDataCollectEnable()) {
            return;
        }
        AgentProcess.getInstance().getConfig().setAutoTrackPageView(isAuto);
    }

    /**
     * 获取自动采集页面信息开关状态
     * is old PLZ call {@link AnalysysConfig#isAutoTrackPageView()} ()}
     */
    @Deprecated
    public static boolean getAutomaticCollection(Context context) {
        return AgentProcess.getInstance().getConfig().isAutoTrackPageView();
    }


    private static List<String> IgnoredAcName = new ArrayList<>();

    /**
     * 忽略部分页面自动采集
     */
    @Deprecated
    public static void setIgnoredAutomaticCollectionActivities(Context context, List<String> activitiesName) {
        if (!AgentProcess.getInstance().isDataCollectEnable()) {
            return;
        }
        IgnoredAcName = activitiesName;
        AgentProcess.getInstance().setPageViewBlackListByPages(activitiesName);
    }

    /**
     * 获取忽略自动采集的页面
     * return empty ,PLZ call
     */
    @Deprecated
    public static List<String> getIgnoredAutomaticCollection(Context context) {
        return IgnoredAcName;
    }

    /**
     * 上报异常
     * @param context
     * @param throwable
     */
    public static void reportException(Context context, Throwable throwable) {
        if (!AgentProcess.getInstance().isDataCollectEnable()) {
            return;
        }
        if (InitChecker.check(Constants.API_REPORT_EXCEPTION)) {
            CrashHandler.getInstance().reportException(context, throwable, CrashHandler.CrashType.crash_report);
        }
    }

    /**
     * 设置单个view的ID
     * @param view
     * @param id
     */
    public static void setAnsViewID(View view, String id) {
        if (!AgentProcess.getInstance().isDataCollectEnable()) {
            return;
        }
        try {
            Class<?> threadClazz = Class.forName("com.analysys.allgro.AllegroUtils");
            Method method = threadClazz.getMethod("setViewIdResourceName", View.class, String.class);
            System.out.println(method.invoke(null, view, id));
        } catch (Throwable ignore) {
            ExceptionUtil.exceptionThrow(ignore);
        }
    }

    /**
     * 清除本地缓存的所有事件
     */
    public static void cleanDBCache() {
        if (!AgentProcess.getInstance().isDataCollectEnable()) {
            return;
        }
        AgentProcess.getInstance().cleanDBCache(AnalysysUtil.getContext());
    }

    /**
     * 设置网络发送策略
     * @param networkType
     */
    public static void setUploadNetworkType(int networkType) {
        if (!AgentProcess.getInstance().isDataCollectEnable()) {
            return;
        }
        AgentProcess.getInstance().setUploadNetworkType(networkType);
    }

    /**
     * 数据采集和上报开关
     */
    public static void setDataCollectEnable(boolean enable) {
        AgentProcess.getInstance().setDataCollectEnable(enable);
    }

    /**
     * 运动数据相关
     */
    public static void setCacheDataLength(Context context,int cacheDataLength) {
        AnalysysSSManager.getInstance(context).setCacheDataLength(cacheDataLength);
    }

    public static void setCollectReverse(Context context, boolean collectReverse) {
        AnalysysSSManager.getInstance(context).setCollectReverse(collectReverse);
    }

    public static void setRate(Context context,AnalysysSSManager.RATE rate) {
        AnalysysSSManager.getInstance(context).setRate(rate);
    }


    public static void setUseGravity(Context context,boolean useGravity) {
        AnalysysSSManager.getInstance(context).setUseGravity(useGravity);
    }



    public static void setListenDuration(Context context ,int listenDuration) {
        AnalysysSSManager.getInstance(context).setListenDuration(listenDuration);
    }

    public static void startListen (Context context) {
        AnalysysSSManager.getInstance(context).startListen();
    }

    public static void stopListen (Context context) {
        AnalysysSSManager.getInstance(context).stopListen();
    }

    /**
     * 网络发送策略
     */
    public interface AnalysysNetworkType {

        //不允许上传
        int AnalysysNetworkNONE = 0;

        //允许移动网络上传
        int AnalysysNetworkWWAN = 1 << 1;

        //允许wifi网络
        int AnalysysNetworkWIFI = 1 << 2;

        //允许所有网络
        int AnalysysNetworkALL = 0xFF;
    }
}
