package com.analysys.process;

import android.content.Context;
import android.text.TextUtils;
import android.util.JsonReader;

import com.analysys.utils.CommonUtils;
import com.analysys.utils.ExceptionUtil;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @Copyright © 2019 EGuan Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2019/3/10 17:33
 * @Author: Wang-X-C
 */
public class LifeCycleConfig {

    private static final String CONFIG_FILE_NAME = "LifeCycleConfig.json";
    public static JSONObject uploadHeadJson = null;
    public static JSONObject encryptJson = null;
    public static JSONObject visualBase = null;
    public static JSONObject visual = null;
    public static JSONObject visualConfig = null;
    public static JSONObject pushParse = null;
    public static JSONObject pushClick = null;
    public static JSONObject probe = null;
    static JSONObject configJson = null;

    /**
     * 初始加密
     */
    public static void initUploadConfig(Context context) {
        try {
            if (configJson == null) {
                String config = CommonUtils.getMould(context, CONFIG_FILE_NAME);
                configJson = new JSONObject(config);
            }
            if (configJson != null) {
                uploadHeadJson = configJson.optJSONObject("Upload");
                encryptJson = configJson.optJSONObject("Encrypt");
            }
        } catch (Throwable ignore) {
            ExceptionUtil.exceptionThrow(ignore);
        }
    }

    /**
     * 初始化可视化
     */
    public static void initVisualConfig(Context context) throws JSONException {
        if (configJson == null) {
            String config = CommonUtils.getMould(context, CONFIG_FILE_NAME);
            if (!TextUtils.isEmpty(config)) {
                configJson = new JSONObject(config);
            }
        }
        if (configJson != null) {
            visualBase = configJson.optJSONObject("VisualBase");
            visual = configJson.optJSONObject("Visual");
            visualConfig = configJson.optJSONObject("VisualConfig");
        }
    }

    /**
     * 初始化推送
     */
    public static void initPushConfig(Context context) throws JSONException {
        if (configJson == null) {
            String config = CommonUtils.getMould(context, CONFIG_FILE_NAME);
            configJson = new JSONObject(config);
        }
        if (configJson != null) {
            pushParse = configJson.optJSONObject("PushParse");
            pushClick = configJson.optJSONObject("PushClick");
        }
    }

    /**
     * 初始化probe
     */
    public static void initProbeConfig(Context context) throws JSONException {
        if (configJson == null) {
            String config = CommonUtils.getMould(context, CONFIG_FILE_NAME);
            configJson = new JSONObject(config);
        }
        if (configJson != null) {
            probe = configJson.optJSONObject("Probe");
        }
    }
}
