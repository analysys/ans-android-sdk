package com.analysys.strategy;

import android.content.Context;
import android.text.TextUtils;

import com.analysys.utils.Constants;
import com.analysys.utils.SharedUtil;

import org.json.JSONObject;

/**
 * @Copyright © 2018 EGuan Inc. All rights reserved.
 * @Description: 逻辑分发 策略处理
 * @Version: 1.0
 * @Create: 2018/2/3 17:31
 * @Author: WXC
 */

public class PolicyManager {
    /**
     * 获取发送满足条数
     * 服务器下发 > 用户设置 > 默认值
     */
    public static long getEventCount(Context context) {
        long serviceCount = SharedUtil.getLong(context, Constants.SP_SERVICE_EVENT_COUNT, -1L);
        if (serviceCount != -1) {
            return serviceCount;
        }
        long userCount = SharedUtil.getLong(context, Constants.SP_USER_EVENT_COUNT, -1L);
        if (userCount != -1) {
            return userCount;
        }
        return 0;
        //return Constants.EVENT_COUNT;
    }

    /**
     * 获取发送间隔时间
     * 服务器下发 > 用户设置 > 默认值
     */
    public static long getIntervalTime(Context context) {
        long serviceIntervalTime = SharedUtil.getLong(context, Constants.SP_SERVICE_INTERVAL_TIME
                , -1L);
        if (serviceIntervalTime != -1) {
            return serviceIntervalTime;
        }
        long userIntervalTime = SharedUtil.getLong(context, Constants.SP_USER_INTERVAL_TIME, -1L);
        if (userIntervalTime != -1) {
            return userIntervalTime;
        }
        //return Constants.INTERVAL_TIME;
        return 0;
    }

    /**
     * 判断策略级别
     */
    public static BaseSendStatus getPolicyType(Context context) {
        long policyNo = SharedUtil.getLong(context, Constants.SP_POLICY_NO, -1L);
        if (policyNo != -1) {
            return new ServicePolicy();
        }
        long intervalTime = SharedUtil.getLong(context, Constants.SP_USER_INTERVAL_TIME, -1L);
        long eventCount = SharedUtil.getLong(context, Constants.SP_USER_EVENT_COUNT, -1L);
        if (intervalTime != -1 || eventCount != -1) {
            return new UserPolicy();
        }
        return new DefaultPolicy();
    }

    /**
     * 存储服务器返回值
     */
    public static void analysysStrategy(Context context, JSONObject json) {
        if (json == null || json.length() <= 0) {
            return;
        }
        long policyNo = json.optLong(Constants.SERVICE_POLICY_NO, -1L);
        SharedUtil.setLong(context, Constants.SP_POLICY_NO, policyNo);

        long eventCount = json.optLong(Constants.SERVICE_EVENT_COUNT, -1L);
        SharedUtil.setLong(context, Constants.SP_SERVICE_EVENT_COUNT, eventCount);

        long timerInterval = json.optLong(Constants.SERVICE_TIMER_INTERVAL, -1L);
        if (timerInterval > -1) {
            timerInterval = timerInterval * 1000;
        }
        SharedUtil.setLong(context, Constants.SP_SERVICE_INTERVAL_TIME, timerInterval);

        long failCount = json.optLong(Constants.SERVICE_FAIL_COUNT, -1L);
        SharedUtil.setLong(context, Constants.SP_FAIL_COUNT, failCount);

        long failTryDelay = json.optLong(Constants.SERVICE_FAIL_TRY_DELAY, -1L);
        if (failTryDelay > -1) {
            failTryDelay = failTryDelay * 1000;
        }
        SharedUtil.setLong(context, Constants.SP_FAIL_TRY_DELAY, failTryDelay);

        int debugMode = json.optInt(Constants.SERVICE_DEBUG_MODE, -1);
        SharedUtil.setInt(context, Constants.SP_SERVICE_DEBUG, debugMode);

        String url = json.optString(Constants.SERVICE_SERVER_URL, "");
        if (!TextUtils.isEmpty(url) && (url.startsWith(Constants.HTTP) || url.startsWith(Constants.HTTPS))) {
            SharedUtil.setString(context, Constants.SP_SERVICE_URL, url);
        }
        String hash = json.optString(Constants.SERVICE_HASH, "");
        SharedUtil.setString(context, Constants.SP_SERVICE_HASH, hash);
    }
}
