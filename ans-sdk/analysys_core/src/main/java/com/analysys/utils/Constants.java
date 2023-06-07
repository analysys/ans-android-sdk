package com.analysys.utils;

import com.analysys.BuildConfig;

import java.util.Map;

/**
 * @Copyright © 2018 EGuan Inc. All rights reserved.
 * @Description: 常量类
 * @Version: 1.0
 * @Create: 2018/2/3 17:31
 * @Author: Wang-X-C
 */

public class Constants {

    public static final String DEV_SDK_VERSION = BuildConfig.SDK_VERSION;

    public static final String PAGE_VIEW = "$pageview";
    public static final String PAGE_CLOSE = "page_close";
    public static final String STARTUP = "$startup";
    public static final String END = "$end";
    public static final String ALIAS = "$alias";
    public static final String ORIGINAL_ID = "$original_id";
    public static final String TRACK = "$track";
    public static final String APP_CLICK = "$app_click";
    public static final String USER_CLICK = "$user_click";
    public static final String FIRST_INSTALL = "$first_installation";
    public static final String APP_CRASH_DATA = "$app_crash";


    public static final String DURATION_TIME = "$duration";
    public static final String NETWORK_TYPE = "$network";
    public static final String FIRST_DAY = "$is_first_day";
    public static final String TIME_CALIBRATED = "$is_time_calibrated";
    public static final String IS_LOGIN = "$is_login";
    public static final String SESSION_ID = "$session_id";

    public static final String PROFILE = "$profile";
    public static final String PROFILE_SET = "$profile_set";
    public static final String PROFILE_UNSET = "$profile_unset";
    public static final String PROFILE_DELETE = "$profile_delete";
    public static final String PROFILE_APPEND = "$profile_append";
    public static final String PROFILE_SET_ONCE = "$profile_set_once";
    public static final String PROFILE_INCREMENT = "$profile_increment";

    public static final String PAGE_URL = "$url";
    public static final String PAGE_TITLE = "$title";
    public static final String PAGE_STAY_TIME = "pageStayTime";
    public static final String PAGE_REFERRER = "$referrer";
    public static final String PARENT_URL = "$parent_url";

    public static final String PAGE_WIDTH = "$page_width";
    public static final String PAGE_HEIGHT = "$page_height";
    public static final String TOUCH_SCREEN_DPI = "$screen_dpi";
    public static final String TOUCH_SCREEN_SCALE = "$screen_scale";
    public static final String TOUCH_CLICK_X = "$click_x";
    public static final String TOUCH_CLICK_Y = "$click_y";
    public static final String TOUCH_ELEMENT_X = "$element_x";
    public static final String TOUCH_ELEMENT_Y = "$element_y";

    public static final String ELEMENT_PATH = "$element_path";

    public static final String TOUCH_ELEMENT_CLICKABLE = "$element_clickable";
    public static final String ELEMENT_TYPE = "$element_type";
    public static final String ELEMENT_CONTENT = "$element_content";
    public static final String ELEMENT_POSITION = "$element_position";
    public static final String ELEMENT_ID = "$element_id";

    public static final String X_CONTEXT = "xcontext";
    public static final String APP_ID = "appid";
    static final String X_WHO = "xwho";
    static final String X_WHAT = "xwhat";
    public static final String X_WHEN = "xwhen";

    public static final String SP_FIRST_START_TIME = "firstStartTime";
    public static final String SP_AUTO_PROFILE = "autoProfile";

    public static final String SP_CHANNEL = "appChannel";
    public static final String SP_APP_KEY = "appKey";
    public static final String SP_SEND_TIME = "uploadTime";
    public static final String SP_SEND_SUCCESS = "sendSuccess";


    /************start 用户相关ID***********/
    public static final String SP_ALIAS_ID = "aliasId";
    public static final String SP_DISTINCT_ID = "distinctId";
    public static final String SP_UUID = "uuid";
    public static final String SP_ADID = "adid";
    public static final String SP_ANDID = "androidId";

    public static final String SP_ORIGINAL_ID = "originalId";

    public static final String SP_MAC = "macAddress";
    public static final String SP_IMEI = "imeiAddress";
    /************end 用户相关ID************/

    /****************存储到内存中的值***********/
    public static final String RAM_NET_WORK_TYPE = "ram_net_work_type";
    public static final String RAM_CACHE_MAX_COUNT = "ram_cache_max_count";
    public static final String RAM_DATA_COLLECT_ENABLE = "ram_data_collect_enable";
    /****************************************/

    public static final String SP_SUPER_PROPERTY = "superProperty";
    public static final String SP_PRE_USER_PROPERTY = "preUserProperty";
    public static final String SP_JS_SUPER_PROPERTY = "js_superProperty";
    public static final String SP_USER_URL = "userUrl";

    public static final String SP_USER_DEBUG = "userDebug";
    public static final String SP_USER_INTERVAL_TIME = "userIntervalTime";
    public static final String SP_USER_EVENT_COUNT = "userEventCount";
    public static final String SP_POLICY_NO = "policyNo";
    public static final String SP_SERVICE_EVENT_COUNT = "serviceEventCount";
    public static final String SP_SERVICE_INTERVAL_TIME = "serviceTimerInterval";
    public static final String SP_FAIL_COUNT = "failCount";
    public static final String SP_FAIL_TRY_DELAY = "failTryDelay";
    public static final String SP_SERVICE_DEBUG = "serviceDebug";
    public static final String SP_SERVICE_URL = "serviceUrl";
    public static final String SP_SERVICE_HASH = "serviceHash";
    public static final String SP_FAILURE_TIME = "failureTime";
    public static final String SP_FAILURE_COUNT = "failureCount";
    public static final String SP_IS_LOGIN = "isLogin";
    public static final String SP_REQUEST_VERSION = "requestVersion";
    public static final String SP_ENABLE_DATA_COLLECT = "enableDataCollect";

    /*****************start 页面自动采集********************/
    public static final String SP_SESSION_ID = "getSessionId";
    public static final String SP_EVENT_TIME = "lastEventTime";
    public static final String SP_START_DAY = "startDay";
    public static final String SP_LAST_PAGE_CHANGE = "pageEndTime"; //上一个页面时间，本次生效
    public static final String SP_DIFF_TIME = "diffTime";
    public static final String SP_CHECK_TIME = "checkTime";
//    public static final String SP_PAGE_END = "pageEnd";

    public static final String APP_START_TIME = "app_start_time";   //app开始时间，本次生效
    public static final String APP_END_INFO = "app_end_info";       //app结束当时数据信息
    public static final String PAGE_CLOSE_INFO = "page_close_info";
    public static final String PAGE_CLOSE_H5_INFO = "page_close_h5_info";
    public static final String LAST_OP_TIME = "last_op_time";
    public static String SP_REFER = "referrer";         //页面引用来源
    public static final String PV_START_TIME = "pv_start_time_eg";


    public static final String PAGE_COUNT = "page_count";



    /*****************end 页面自动采集********************/



    public static final String REAL_TIME_DATA = "realTimeData";

    public static final String SERVICE_CODE = "code";
    public static final String SERVICE_POLICY = "policy";
    public static final String SERVICE_POLICY_NO = "policyNo";
    public static final String SERVICE_EVENT_COUNT = "eventCount";
    public static final String SERVICE_TIMER_INTERVAL = "timerInterval";
    public static final String SERVICE_FAIL_COUNT = "failCount";
    public static final String SERVICE_FAIL_TRY_DELAY = "failTryDelay";
    public static final String SERVICE_DEBUG_MODE = "debugMode";
    public static final String SERVICE_SERVER_URL = "serverUrl";
    public static final String SERVICE_HASH = "hash";

    public static final String DEV_SYSTEM = "Android";
    public static final String DEV_IS_FROM_BACKGROUND = "$is_from_background";
    public static final String DEV_DURATION = "$duration";
    public static final String DEV_FIRST_VISIT_TIME = "$first_visit_time";
    public static final String DEV_RESET_TIME = "$reset_time";
    public static final String DEV_FIRST_VISIT_LANGUAGE = "$first_visit_language";
    public static final String DEV_CHANNEL = "ANALYSYS_CHANNEL";
    public static final String DEV_APPKEY = "ANALYSYS_APPKEY";
    static final String DEV_KEYSTONE = "ANALYSYS_KEYSTONE";

    public static final String PUSH_EVENT_RECEIVER_MSG = "$push_receiver_success";
    public static final String PUSH_EVENT_CLICK_MSG = "$push_click";
    public static final String PUSH_EVENT_PROCESS_SUCCESS = "$push_process_success";

    public static final String CRASH_DATA= "$crash_data";
    public static final String CRASH_TYPE = "$crash_type";

    public static final String HYBRID_AGENT = " AnalysysAgent/Hybrid";

    /** 默认满足条数上传 */
    public static final long EVENT_COUNT = 10;
    /** 默认间隔时间 */
    public static final long INTERVAL_TIME = 15 * 1000;
    /** 失败重传次数 */
    public static final long FAILURE_COUNT = 3;
    /** 重传时间间隔 */
    public static final int FAILURE_INTERVAL_TIME = 30 * 1000;

    /** track 事件名称长度 */
    static final long MAX_EVENT_NAME_LENGTH = 99;
    /** xContext内key值限制 */
    static final long MAX_KEY_LENGTH = 99;
    /** xContext内value值限制 */
    public static final long MAX_VALUE_LENGTH = 255;
    /** id值限制 */
    public static final long MAX_ID_LENGTH = 255;
    /** 发送最大条数 */
    public static final long MAX_SEND_COUNT = 100;
    /** 数组集合限制 */
    static final long MAX_ARRAY_SIZE = 100;
    /** 默认最大缓存条数 */
    public static final long MAX_CACHE_COUNT = 10000;
    /** 到达缓存上限后，继续存储，每次删除的条数 */
    public static final int DELETE_COUNT = 10;
    /** 退出后台信息记录间隔时间 */
    public static final long BG_INTERVAL_TIME = 30 * 1000;
    /** 退出后台信息发送延迟时间 */
    public static final long TRACK_END_INVALID = 10 * 1000;
    public static final long SESSION_INVALID = 30 * 1000;
    /** AppEnd 轮询器启动延迟时间 */
    public static final int APPEND_TIMER_DELAY_MILLIS = 200;


    public static final String SAVE_TYPE = "$Event";

    public static final String THREAD_NAME = "send_data_thread";

    static final String PLATFORM = "Android";

    static final String KEY_RULE = "^(^[$a-zA-Z][$a-zA-Z0-9_]{0,})$";

    static final String API_INIT = "init";
    public static final String API_PROFILE_SET = "profileSet";
    public static final String API_PROFILE_SET_ONCE = "profileSetOnce";
    public static final String API_PROFILE_INCREMENT = "profileIncrement";
    public static final String API_PROFILE_APPEND = "profileAppend";
    public static final String API_PROFILE_UNSET = "profileUnset";
    public static final String API_PROFILE_DELETE = "profileDelete";
    public static final String API_REGISTER_SUPER_PROPERTY = "registerSuperProperty";
    public static final String API_REGISTER_SUPER_PROPERTIES = "registerSuperProperties";
    public static final String API_REGISTER_PREEVENT_USER_PROPERTIES = "registerPreEventUserProperties";
    public static final String API_UNREGISTER_SUPER_PROPERTY = "unRegisterSuperProperty";
    public static final String API_CLEAR_SUPER_PROPERTIES = "clearSuperProperties";
    public static final String API_GET_SUPER_PROPERTY = "getSuperProperty";
    public static final String API_RESET = "reset";

    public static final String API_SET_DEBUG_MODE = "setDebugMode";
    public static final String API_SET_UPLOAD_URL = "setUploadURL";
    public static final String API_TRACK = "track";
    public static final String API_PAGE_VIEW = "pageView";
    public static final String API_ALIAS = "alias";
    public static final String API_IDENTIFY = "identify";
    public static final String API_APP_START = "appStart";
    public static final String API_APP_END = "appEnd";
    public static final String API_APP_CLICK = "appClick";
    public static final String API_USER_CLICK = "userClick";
    public static final String API_FIRST_INSTALL = "firstInstallation";
    public static final String API_CRAHS_DATA = "app_crash";


    public static final String API_SET_INTERVAL_TIME = "setIntervalTime";
    public static final String API_SET_MAX_CACHE_SIZE = "setMaxCacheSize";
    public static final String API_SET_MAX_EVENT_SIZE = "setMaxEventSize";

    public static final String API_SET_AUTOMATIC_COLLECTION = "setAutomaticCollection";
    public static final String API_INTERCEPT_URL = "interceptUrl";
    public static final String API_SET_HYBRID_MODEL = "setHybridModel";
    public static final String API_RESET_HYBRID_MODEL = "resetHybridModel";

    public static final String API_FLUSH = "flush";
    public static final String API_SET_VISITOR_DEBUG_URL = "setVisitorDebugURL";
    public static final String API_SET_VISITOR_CONFIG_URL = "setVisitorConfigURL";
    public static final String API_SET_PUSH_ID = "setPushID";
    public static final String API_TRACK_COMPAIGN = "trackCampaign";
    public static final String API_REPORT_EXCEPTION = "reportException";

    public static final String HTTP = "http://";
    public static final String HTTPS = "https://";

    static final String META_DATA_IMEI = "ANALYSYS_AUTO_COLLECTION_IMEI";
    static final String META_DATA_MAC = "ANALYSYS_AUTO_COLLECTION_MAC";

    static final String EMPTY = "";

    /**
     * 4.0.4以上使用该套端口
     */
    public static final String HTTP_PORT = ":8089";
    public static final String HTTPS_PORT = ":4089";

    public static final String UTM_SOURCE = "$utm_source";
    public static final String UTM_MEDIUM = "$utm_medium";
    public static final String UTM_CAMPAIGN = "$utm_campaign";
    public static final String UTM_CAMPAIGN_ID = "$utm_campaign_id";
    public static final String UTM_CONTENT = "$utm_content";
    public static final String UTM_TERM = "$utm_term";

    public static final String FUNC_LIST = "checkFuncList";

    static final String FILE_NAME = "id.e";

    static final String COUNT_FILE_NAME = "/count.e";
    public static final int CODE_SUCCESS = 200;
    static final int CODE_FAILED = 201;
    static final int CODE_CUT_OFF = 202;
    static final int CODE_DELETE = 203;

    public static boolean isAutoProfile = true;
    public static int encryptType = 0;
    public static boolean autoInstallation = false;
    public static Map<String, Object> utm = null;

    // 用户设置是否允许时间校准
    public static boolean isTimeCheck = false;
    // 用户设置最大偏差时间
    public static long ignoreDiffTime = 30 * 1000;
    // 网络时间与本地时间差值
    public static long diffTime = 0;
    // 网络时间获取成功
    public static boolean isCalibration = false;

    public static boolean isFinishCalibration = false;
    // 应用启动来源
    public static int sourceNum = 1;

    public static boolean isFirstInstall = false;
}

