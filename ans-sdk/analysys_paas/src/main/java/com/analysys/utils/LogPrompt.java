package com.analysys.utils;

import android.text.TextUtils;

import org.json.JSONArray;

/**
 * @Copyright © 2018 EGuan Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2018/11/14 14:43
 * @Author: Wang-X-C
 */
public class LogPrompt {

    private static final String SUCCESS = ": set success.";
    private static final String FAILED = ": set failed.";

    private static final String KEY_SUCCESS = SUCCESS + " Current key: ";
    private static final String KEY_FAILED = FAILED + " Current key: ";

    private static final String CHANNEL_SUCCESS = SUCCESS + " Current channel: ";
    private static final String CHANNEL_FAILED = FAILED + " Current channel: ";

    private static final String NOT_EMPTY = FAILED + " Can not be empty!";

    private static final String ID_EMPTY = FAILED + " Id can not be empty!";
    static final String KEY_EMPTY = " Key can not be empty!";
    static final String VALUE_EMPTY = " Value can not be empty!";
    static final String ARRAY_SIZE_ERROR = " The length of the property value array needs " +
            "to be 1-100!";
    static final String MAP_SIZE_ERROR = " The length of the property key-value pair needs" +
            " to be 1-100!";
    static final String ID_LENGTH_ERROR = " The length of the id needs to be 1-255!";
    static final String TYPE_ERROR = "Property value invalid, support type: " +
            "String/Number/boolean/String collection/String array!";
    final static String FRONT = "[ ";
    final static String NAMING_ERR = "] does not conform to naming rules!";
    final static String RESERVED_ERR = " ] is a reserved field!";
    private static final String INIT_SUCCESS = "Init Android Analysys Java sdk success, version: ";
    private static final String INIT_FAILED = "Please init Analysys Android SDK .";
    private static final String ENCRYPT_SUCCESS = "Encrypt success.";
    private static final String ENCRYPT_FIELD = "Encrypt field.";
    private static final String PROCESS_ERR = "Send message failed, please send message in the " +
            "main process!";
    private static final String KEY_ERR = "Please make sure that the appKey in manifest matched " +
            "with init API!";
    private static final String NOT_NETWORK = "No network, Please check the network.";
    private static final String UPLOAD_SUCCESS = "Data uploaded successfully.";
    private static final String UPLOAD_FAILED = "Data uploaded failed.";
    static final String ERR_HEAD_VALUE = "The length of the property value string [";
    static final String ERR_HEAD_KEY = "The length of the property key string [";
    static final String WHAT_LENGTH_ERR = "] needs to be 1-99!";
    static final String KEY_LENGTH_ERR = "] needs to be 1-99!";
    static final String VALUE_LENGTH_ERR = "] needs to be 1-255!";
    static final String ARRAY_SIZE_ERR = " The length of the property value array needs to" +
            " be 1-100!";
    public static final String TEMPLATE_ERR = "SDK template loading exception.";
    public static final String URL_ERR = " Please check the upload URL.";


    public static void showCheckTimeLog(long netTime, long nowTime, long absDiff) {
        ANSLog.d("收到服务器的时间：" + CommonUtils.timeConversion(netTime));
        ANSLog.d("本地时间：" + CommonUtils.timeConversion(nowTime));
        ANSLog.d("时间相差：" + (absDiff / 1000) + " 秒，数据将会进行时间校准。");
    }

    /**
     * 打印log日志
     */
    public static void showLog(String apiName) {
        if (!TextUtils.isEmpty(apiName)) {
            // 日志格式：应用接口名称 + 结果 (+ 失败详细信息)
            ANSLog.w(apiName + LogPrompt.FAILED);
        }
    }

    public static void showLog(String apiName, String logDetailed) {
        if (!TextUtils.isEmpty(apiName)) {
            // 日志格式：应用接口名称 + 结果 (+ 失败详细信息)
            ANSLog.w(apiName + LogPrompt.FAILED + logDetailed);
        }
    }

    public static void encryptLog(boolean success) {
        if (success) {
            ANSLog.d(ENCRYPT_SUCCESS);
        } else {
            ANSLog.w(ENCRYPT_FIELD);
        }
    }

    public static void showLog(String apiName, boolean success) {

        if (success) {
            ANSLog.d(apiName + SUCCESS);
        } else {
            ANSLog.w(apiName + FAILED);
        }
    }

    public static void showInitLog(boolean success) {
        if (success) {
            ANSLog.d(INIT_SUCCESS + Constants.DEV_SDK_VERSION);
        } else {
            ANSLog.w(INIT_FAILED);
        }
    }

    public static void showChannelLog(boolean success, String channel) {
        if (success) {
            ANSLog.d(Constants.API_INIT + CHANNEL_SUCCESS + channel);
        } else {
            ANSLog.w(Constants.API_INIT + CHANNEL_FAILED + channel);
        }
    }

    public static void showKeyLog(boolean success, String key) {
        if (success) {
            ANSLog.d(Constants.API_INIT + KEY_SUCCESS + key);
        } else {
            ANSLog.w(Constants.API_INIT + KEY_FAILED + key);
        }
    }

    public static void processFailed() {
        ANSLog.w(PROCESS_ERR);
    }

    public static void keyFailed() {
        ANSLog.w(KEY_ERR);
    }

    /**
     * 网络状态异常
     */
    public static void networkErr() {
        ANSLog.w(NOT_NETWORK);
    }

    /**
     * 异常日志打印
     */
    public static void showErrLog(String log) {
        ANSLog.w(log);
    }

    /**
     * 打印上传值
     */
    public static void showSendMessage(String url, JSONArray data) {
        ANSLog.d("Send message to server:" + url + "\n data:  " + data);
    }

    /**
     * 上传返回值打印
     */
    public static void showReturnCode(String code) {
        ANSLog.d("return code：" + code);
    }

    /**
     * 上传结果打印
     */
    public static void showSendResults(boolean isSuccess) {
        if (isSuccess) {
            ANSLog.d("Data uploaded successfully.");
        } else {
            ANSLog.w("Data uploaded failed.");
        }
    }
}