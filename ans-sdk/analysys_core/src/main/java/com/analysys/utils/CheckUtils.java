package com.analysys.utils;

import android.text.TextUtils;

import com.analysys.process.LogBean;
import com.analysys.process.TemplateManage;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @Copyright © 2019 EGuan Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2019/2/22 16:43
 * @Author: Wang-X-C
 */
public class CheckUtils {

    /**
     * 校验数据是否符合上传格式
     */
    public static JSONObject checkField(JSONObject eventInfo) {
        if (eventInfo != null) {
            String appId = eventInfo.optString(Constants.APP_ID);
            long xWhen = eventInfo.optLong(Constants.X_WHEN);
            String xWho = eventInfo.optString(Constants.X_WHO);
            String xWhat = eventInfo.optString(Constants.X_WHAT);
            JSONObject xContext = eventInfo.optJSONObject(Constants.X_CONTEXT);
            if (TextUtils.isEmpty(appId) || TextUtils.isEmpty(appId.trim())
                    || xWhen == 0
                    || TextUtils.isEmpty(xWho) || TextUtils.isEmpty(xWho.trim())
                    || TextUtils.isEmpty(xWhat) || TextUtils.isEmpty(xWhat.trim())
                    || CommonUtils.isEmpty(xContext)) {
                return null;
            }
        }
        return eventInfo;
    }

    public static boolean checkIdLength(String id) {
        if (CommonUtils.isEmpty(id) || id.length() > 255) {
            LogBean.setDetails(Constants.CODE_FAILED, LogPrompt.ID_LENGTH_ERROR);
            return false;
        }
        return true;
    }

    public static boolean checkOriginalIdLength(String id) {
        if (id.length() > 255) {
            LogBean.setDetails(Constants.CODE_FAILED, LogPrompt.ID_LENGTH_ERROR);
            return false;
        }
        return true;
    }

    /**
     * 过滤掉value为空的数据
     */
    public static void checkToMap(Map<String, Object> map, String key, String value) {
        try {
            if (!CommonUtils.isEmpty(key) && !CommonUtils.isEmpty(value)) {
                map.put(key, value);
            }
        } catch (Throwable ignore) {
            ExceptionUtil.exceptionThrow(ignore);
            //ANSLog.e(e);
        }
    }

    /**
     * 用户接口传参校验
     */
    public static boolean checkParameter(String apiName, Map<String, Object> parameters) {
        if (parameters != null) {
            LogBean.resetLogBean();
            if (TemplateManage.userKeysLimit != 0
                    && TemplateManage.userKeysLimit < parameters.size()) {
                LogPrompt.showLog(apiName, LogPrompt.MAP_SIZE_ERROR);
            }
            Set<String> keys = parameters.keySet();
            String key;
            Object value;
            for (Iterator<String> iterator = keys.iterator(); iterator.hasNext(); ) {
                key = iterator.next();
                value = parameters.get(key);
                if (key == null) {
                    iterator.remove();
                    LogBean.setDetails(Constants.CODE_FAILED, LogPrompt.KEY_EMPTY);
                    showCheckParameterLog(apiName);
                    continue;
                }
                if (Constants.CODE_FAILED
                        == reflexCheckParameter(apiName, key, TemplateManage.contextKey)) {
                    showCheckParameterLog(apiName);
                    continue;
                }
                if (Constants.CODE_FAILED
                        == reflexCheckParameter(apiName, value, TemplateManage.contextValue)) {
                    showCheckParameterLog(apiName);
                    continue;
                }
                if (LogBean.getCode() == Constants.CODE_CUT_OFF
                        && !TextUtils.isEmpty(LogBean.getValue())) {
                    parameters.put(key, LogBean.getValue());
                    LogBean.setValue(null);
                }
                showCheckParameterLog(apiName);
            }
        }
        return true;
    }

    private static void showCheckParameterLog(String apiName) {
        if (Constants.API_PROFILE_UNSET.equals(apiName)) {
            LogBean.setDetails(Constants.CODE_SUCCESS, null);
        } else if (LogBean.getCode() != Constants.CODE_SUCCESS) {
            LogPrompt.showLog(apiName, LogBean.getLog());
        }
    }

    /**
     * 校验track事件eventName
     */
    public static boolean checkTrackEventName(String eventName, String eventInfo) {
        if (TemplateManage.ruleMould == null) {
            return false;
        }
        JSONObject trackMould = TemplateManage.ruleMould.optJSONObject(eventName);
        if (CommonUtils.isEmpty(trackMould)) {
            return true;
        }
        if (trackMould == null) {
            return true;
        }
        JSONArray funcList = trackMould.optJSONArray(Constants.FUNC_LIST);
        if (CommonUtils.isEmpty(funcList)) {
            return true;
        }
        String path;
        for (int i = 0; i < funcList.length(); i++) {
            path = funcList.optString(i);
            CommonUtils.reflexUtils(
                    CommonUtils.getClassPath(path),
                    CommonUtils.getMethod(path),
                    new Class[]{Object.class}, eventInfo);
            if (LogBean.getCode() != Constants.CODE_SUCCESS) {
                return false;
            }
        }
        return true;
    }

    /**
     * 通过反射funcList内方法校验key
     */
    private static int reflexCheckParameter(String apiName, Object data, JSONArray methodArray) {
        if (methodArray == null) {
            return Constants.CODE_CUT_OFF;
        }
        String path;
        for (int i = 0; i < methodArray.length(); i++) {
            path = methodArray.optString(i);
            CommonUtils.reflexUtils(
                    CommonUtils.getClassPath(path),
                    CommonUtils.getMethod(path),
                    new Class[]{Object.class}, data);
        }
        return LogBean.getCode();
    }
}
