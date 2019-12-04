package com.analysys;

import android.content.Context;

import com.analysys.process.AgentProcess;
import com.analysys.process.ContextManager;
import com.analysys.push.PushListener;
import com.analysys.utils.Constants;
import com.analysys.utils.CrashHandler;

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
        ContextManager.setContext(context);
        AgentProcess.getInstance(context).init(config);
    }

    /**
     * 热图开启/关闭接口
     */
    public static void setAutoHeatMap(boolean autoTrack) {
        Constants.autoHeatMap = autoTrack;
    }

    /**
     * debug 模式
     *
     * @param debugMode 0、关闭debug模式
     * 1、打开Debug模式,但该模式下发送的数据仅用于调试，不计入平台数据统计
     * 2、打开Debug模式,该模式下发送的数据可计入平台数据统计
     */
    public static void setDebugMode(Context context, int debugMode) {
        AgentProcess.getInstance(context).setDebug(debugMode);
    }

    /**
     * 设置上传数据地址
     *
     * @param url 数据上传地址,
     * 数据上传地址必须以"http://"或"https://"开头,必须携带端口号
     * 长度小于255字符
     */
    public static void setUploadURL(Context context, String url) {
        AgentProcess.getInstance(context).setUploadURL(url);
    }

    /**
     * 设置上传间隔时间
     * debug 设置为 0 时,数据上传时间间隔,单位:秒
     *
     * @param flushInterval 间隔时间,time值大于1
     */
    public static void setIntervalTime(Context context, long flushInterval) {
        AgentProcess.getInstance(context).setIntervalTime(flushInterval);
    }

    /**
     * 本地缓存上限值
     *
     * @param size 上传条数,count值大于1
     */
    public static void setMaxCacheSize(Context context, long size) {
        AgentProcess.getInstance(context).setMaxCacheSize(size);
    }

    /**
     * 读取最大缓存条数
     */
    public static long getMaxCacheSize(Context context) {
        return AgentProcess.getInstance(context).getMaxCacheSize();
    }

    /**
     * 设置事件最大上传条数
     * 当 debug 设置 0 时,数据累积条数大于设置条数后触发上传
     *
     * @param size 上传条数,size值大于1
     */
    public static void setMaxEventSize(Context context, long size) {
        AgentProcess.getInstance(context).setMaxEventSize(size);
    }

    /**
     * 手动上传
     * 调用该接口立即上传数据
     */
    public static void flush(Context context) {
        AgentProcess.getInstance(context).flush();
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
        AgentProcess.getInstance(context).alias(aliasId, originalId);
    }

    public static void alias(Context context, String aliasId) {
        AgentProcess.getInstance(context).alias(aliasId);
    }

    /**
     * 用户ID设置
     *
     * @param distinctId 唯一身份标识,长度大于0且小于255字符
     */
    public static void identify(Context context, String distinctId) {
        AgentProcess.getInstance(context).identify(distinctId);
    }

    /**
     * 获取匿名 id
     * 如果用户通过identify接口设置，则返回设置id ；
     * 如未设置，则返回代码自动生成的uuid
     */
    public static String getDistinctId(Context context) {
        return AgentProcess.getInstance(context).getDistinctId();
    }

    /**
     * 清除本地设置
     */
    public static void reset(Context context) {
        AgentProcess.getInstance(context).reset();
    }

    /**
     * 添加事件
     *
     * @param eventName 事件名称,以字母或$开头,可以包含大小写字母/数字/ _ / $,不支持中文和乱码,最大长度99字符
     */
    public static void track(Context context, String eventName) {
        AgentProcess.getInstance(context).track(eventName, null);
    }

    /**
     * 添加多属性事件
     *
     * @param eventName 事件名称,以字母或$开头,可以包含大小写字母/数字/ _ /$,不支持中文和乱码,长度必须小于99字符
     * @param eventInfo 事件属性,最多包含100条,
     * 且key以字母或 $ 开头,包括大小写字母/数字/ _ / $,最大长度99字符,不支持乱码和中文,
     * value支持部分类型：String/Number/boolean/集合/数组,若为字符串,最大长度255字符
     */
    public static void track(Context context, String eventName, Map<String, Object> eventInfo) {
        AgentProcess.getInstance(context).track(eventName, eventInfo);
    }

    /**
     * 添加页面
     *
     * @param pageName 页面标识，为字符串,最大长度255字符
     */
    public static void pageView(Context context, String pageName) {
        AgentProcess.getInstance(context).pageView(context, pageName, null);
    }

    /**
     * 添加页面信息
     *
     * @param pageName 页面标识,字符串,最大长度255字符
     * @param pageInfo 页面信息,最多包含100条,
     * 且key以字母或 $ 开头,包括大小写字母/数字/ _ / $,最大长度99字符,不支持乱码和中文,
     * value支持部分类型：String/Number/boolean/集合/数组,若为字符串,最大长度255字符
     */
    public static void pageView(Context context, String pageName, Map<String, Object> pageInfo) {
        AgentProcess.getInstance(context).pageView(context, pageName, pageInfo);
    }

    /**
     * 应用启动来源，
     * 参数为 1. icon启动 默认值为icon启动
     * 参数为 2. msg 启动
     * 参数为 3. deepLink启动
     * 参数为 5. 其他方式启动
     */
    public static void launchSource(int source) {
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
        AgentProcess.getInstance(context).registerSuperProperty(superPropertyName,
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
        AgentProcess.getInstance(context).registerSuperProperties(superProperty);
    }

    /**
     * 删除单个通用属性
     *
     * @param superPropertyName 属性名称,以字母或$开头,可以包含大小写字母/数字/ _ /$,
     * 不支持中文和乱码,长度必须小于99字符
     */
    public static void unRegisterSuperProperty(Context context, String superPropertyName) {
        AgentProcess.getInstance(context).unregisterSuperProperty(superPropertyName);
    }

    /**
     * 清除所有通用属性
     */
    public static void clearSuperProperties(Context context) {
        AgentProcess.getInstance(context).clearSuperProperty();
    }

    /**
     * 获取单个通用属性
     *
     * @param key 属性名称,以字母或$开头,可以包含大小写字母/数字/ _ /$,不支持中文和乱码,长度必须小于99字符
     */
    public static Object getSuperProperty(Context context, String key) {
        return AgentProcess.getInstance(context).getSuperProperty(key);
    }

    /**
     * 获取全部通用属性
     */
    public static Map<String, Object> getSuperProperties(Context context) {
        return AgentProcess.getInstance(context).getSuperProperty();
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
        AgentProcess.getInstance(context).profileSet(propertyName, propertyValue);
    }

    /**
     * 设置用户的多个属性,如果之前存在,则覆盖,否则,创建
     *
     * @param property 属性列表,最多包含100条,
     * 且key以字母或 $ 开头,包括大小写字母/数字/ _ / $,最大长度99字符,不支持乱码和中文,
     * value若为字符串,最大长度255字符
     */
    public static void profileSet(Context context, Map<String, Object> property) {
        AgentProcess.getInstance(context).profileSet(property);
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
        AgentProcess.getInstance(context).profileSetOnce(propertyName, propertyValue);
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
        AgentProcess.getInstance(context).profileSetOnce(property);
    }

    /**
     * 设置用户属性的单个相对变化值(相对增加,减少)
     *
     * @param propertyName 属性名称,以字母或$开头,可以包含大小写字母/数字/ _ /$,不支持中文和乱码,长度必须小于99字符
     * @param propertyValue 属性的值,value支持类型:Number
     */
    public static void profileIncrement(Context context, String propertyName, Number
            propertyValue) {
        AgentProcess.getInstance(context).profileIncrement(propertyName, propertyValue);
    }

    /**
     * 设置用户属性的多个相对变化值(相对增加,减少)
     * 如年龄等
     *
     * @param property 属性列表,最多包含100条,
     * 且key以字母或 $ 开头,包括大小写字母/数字/ _ / $,最大长度99字符,不支持乱码和中文,
     */
    public static void profileIncrement(Context context, Map<String, Number> property) {
        AgentProcess.getInstance(context).profileIncrement(property);
    }

    /**
     * 列表类型属性增加单个元素
     */
    public static void profileAppend(Context context, String propertyName, Object propertyValue) {
        AgentProcess.getInstance(context).profileAppend(propertyName, propertyValue);
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
        AgentProcess.getInstance(context).profileAppend(propertyValue);
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
        AgentProcess.getInstance(context).profileAppend(propertyName, propertyValue);
    }

    /**
     * 删除单个用户属性
     *
     * @param propertyName 属性名称,以字母或$开头,可以包含大小写字母/数字/ _ /$,不支持中文和乱码,长度必须小于99字符
     */
    public static void profileUnset(Context context, String propertyName) {
        AgentProcess.getInstance(context).profileUnset(propertyName);
    }

    /**
     * 清除所有用户的属性
     */
    public static void profileDelete(Context context) {
        AgentProcess.getInstance(context).profileDelete();
    }

    /**
     * 自动采集页面信息
     *
     * @param isAuto 默认为true
     */
    public static void setAutomaticCollection(Context context, boolean isAuto) {
        AgentProcess.getInstance(context).automaticCollection(isAuto);
    }

    /**
     * 获取自动采集页面信息开关状态
     */
    public static boolean getAutomaticCollection(Context context) {
        return AgentProcess.getInstance(context).getAutomaticCollection();
    }

    /**
     * 忽略部分页面自动采集
     */
    public static void setIgnoredAutomaticCollectionActivities(Context context,
                                                               List<String> activitiesName) {
        AgentProcess.getInstance(context).setIgnoredAutomaticCollection(activitiesName);
    }

    /**
     * 获取忽略自动采集的页面
     */
    public static List<String> getIgnoredAutomaticCollection(Context context) {
        return AgentProcess.getInstance(context).getIgnoredAutomaticCollection();
    }

    /**
     * 获取预置属性
     * @param context
     * @return
     */
    public static Map<String, Object> getPresetProperties(Context context) {
        return AgentProcess.getInstance(context).getPresetProperties();
    }

    /**
     * 设置可视化websocket服务器地址
     *
     * @param url 数据上传地址,
     * 数据上传地址必须以"ws://"或"wss://"开头,必须携带端口号
     * 长度小于255字符
     */
    public static void setVisitorDebugURL(Context context, String url) {
        AgentProcess.getInstance(context).setVisitorDebugURL(url);
    }

    /**
     * 设置线上请求埋点配置的服务器地址
     *
     * @param url 数据上传地址,
     * 数据上传地址必须以"http://"或"https://"开头,必须携带端口号
     * 长度小于255字符
     */
    public static void setVisitorConfigURL(Context context, String url) {
        AgentProcess.getInstance(context).setVisitorConfigURL(url);
    }

    /**
     * 设置推送ID
     *
     * @param provider 推送服务方
     * @param pushId 推送ID
     */
    public static void setPushID(Context context, String provider, String pushId) {
        AgentProcess.getInstance(context).enablePush(provider, pushId);
    }

    /**
     * 追踪活动推广
     *
     * @param campaign 活动推广消息
     * @param isClick 是否被点击
     */
    public static void trackCampaign(Context context, String campaign, boolean isClick) {
        AgentProcess.getInstance(context).trackCampaign(campaign, isClick, null);
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
        AgentProcess.getInstance(context).trackCampaign(campaign, isClick, listener);
    }

    /**
     * 拦截监听 URL
     */
    public static void interceptUrl(Context context, String url, Object webView) {
        AgentProcess.getInstance(context).interceptUrl(url, webView);
    }

    /**
     * 设置UA
     */
    public static void setHybridModel(Context context, Object webView) {
        AgentProcess.getInstance(context).setHybridModel(webView);
    }

    /**
     * 还原 User-Agent 中的字符串
     */
    public static void resetHybridModel(Context context, Object webView) {
        AgentProcess.getInstance(context).resetHybridModel(webView);
    }

    /**
     * 上报异常
     * @param context
     * @param throwable
     */
    public static void reportException(Context context, Throwable throwable){
        CrashHandler.getInstance().reportException(context,throwable,CrashHandler.CrashType.crash_report);
    }
}

