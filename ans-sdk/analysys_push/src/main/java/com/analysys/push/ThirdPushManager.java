package com.analysys.push;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.text.TextUtils;

import com.analysys.AnalysysAgent;
import com.analysys.utils.ExceptionUtil;
import com.analysys.utils.InternalAgent;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * @Copyright © 2018 EGuan Inc. All rights reserved.
 * @Description: 通过第三方推送实现的消息透传功能管理类
 * @Version: 1.0
 * @Create: 2018/5/28 15:50
 * @Author: chris
 */
public class ThirdPushManager {

    private static Map<String, String> cachePushIds = null;
    private Context mContext = null;

    private ThirdPushManager() {
        cachePushIds = new HashMap<String, String>();
        cachePushIds.put(PushProvider.JPUSH, "");
        cachePushIds.put(PushProvider.GETUI, "");
        cachePushIds.put(PushProvider.BAIDU, "");
        cachePushIds.put(PushProvider.XIAOMI, "");
        cachePushIds.put(PushProvider.ALIYUN, "");

        cachePushIds.put(PushProvider.HUAWEI, "");
        cachePushIds.put(PushProvider.OPPO, "");
        cachePushIds.put(PushProvider.VIVO, "");
        cachePushIds.put(PushProvider.MEIZU, "");
        cachePushIds.put(PushProvider.XINGE, "");
    }

    /** 判断Push是否存在，目的1：以后模块化，可能不存在该功能；目的2：此类为供用户调用接口参数类，不参予混淆 */

    public static ThirdPushManager getInstance(Context context) {
        if (InternalAgent.isEmpty(Holder.INSTANCE.mContext)) {
            if (!InternalAgent.isEmpty(context)) {
                Holder.INSTANCE.mContext = context;
            }
        }
        return Holder.INSTANCE;
    }

    /**
     * @param provider
     * @param pushId
     */
    public void enablePush(String provider, String pushId) {
        //处理推送各平台PushID重传的问题,TODO:改用内存cache的方式
        //解决思路:将每次上传的各平台的PushID与SDK缓存到sp中的PushID作对比，如果不同，保存sp再上传
        try {
            boolean canPush = InternalAgent.checkClass(Constants.PUSH_PACKAGE,
                    Constants.PUSH_CLASS);
            if (!canPush) {
                InternalAgent.w("该版本未集成Push功能");
                return;
            }
            if (InternalAgent.isEmpty(provider)) {
                InternalAgent.w("传入参数Push平台值为空");
                return;
            }
            if (InternalAgent.isEmpty(pushId)) {
                InternalAgent.w("传入参数PushId值为空");
                return;
            }
            String persistentPushID = cachePushIds.get(provider);
            if (persistentPushID.equals(pushId)) {
                return;
            } else {
                cachePushIds.put(provider, pushId);
                AnalysysAgent.profileSet(mContext, provider, pushId);
            }
        } catch (Throwable ignore) {
            ExceptionUtil.exceptionThrow(ignore);
        }
    }

    /**
     * 追踪活动事件接口
     *
     * @param campaign 通过三方Push透传过来的消息
     * @param isClick 参数为true，代表已经点击；否则为false，表示推送下发，用户未响应
     * @param listener 可为空，提供给用户的回调接口
     */
    public void trackCampaign(String campaign, boolean isClick, PushListener listener) {
        InternalAgent.i("trackCampaign campaign: " + campaign + ", isClick: " + isClick);
        boolean canPush = InternalAgent.checkClass(Constants.PUSH_PACKAGE, Constants.PUSH_CLASS);
        if (!canPush) {
            InternalAgent.w("该版本未集成Push功能");
            return;
        }
        if (InternalAgent.isEmpty(campaign)) {
            InternalAgent.w("传入参数campaign的透传消息值为空");
            return;
        }

        Map<String, Object> customParams = getParamsFromJson(campaign);
        if (customParams == null) {
            InternalAgent.e("customParams is null");
            return;
        }
        if (isClick) {
            AnalysysAgent.track(mContext, InternalAgent.PUSH_EVENT_CLICK_MSG, customParams);
            dealPushCallback(customParams, listener);
        } else {
            AnalysysAgent.track(mContext, InternalAgent.PUSH_EVENT_RECEIVER_MSG, customParams);
        }
    }

    private  Map<String, Object> getParamsFromJson(String campaignJson) {
        HashMap<String, Object> customParams = null;
        try {
            customParams = new HashMap<String, Object>();
            JSONObject camp = new JSONObject(campaignJson);
            if (camp.has(Constants.PUSH_KEY_INFO)) {
                String campString = camp.optString(Constants.PUSH_KEY_INFO);
                camp = new JSONObject(campString);
            }
            String campaignID = camp.optString(Constants.PUSH_KEY_CAMPID);
            String action = camp.optString(Constants.PUSH_KEY_ACTION);
            int actionType = camp.optInt(Constants.PUSH_KEY_ACTIONTYPE);
            String cpd = camp.optString(Constants.PUSH_KEY_CPD);
            String campaignName = camp.optString(Constants.PUSH_KEY_CAMPID_NAME);
            String campaignMedium = camp.optString(Constants.PUSH_KEY_CAMPID_MEDIUM);
            String campaignSource = camp.optString(Constants.PUSH_KEY_CAMPID_SOURCE);
            String campaignContent = camp.optString(Constants.PUSH_KEY_CAMPID_CONTENT);
            String campaignTerm = camp.optString(Constants.PUSH_KEY_CAMPID_TERM);
            try {
                if (actionType <= 0 || actionType > 4) {
                    InternalAgent.e("推送信息下发的Action值不在规定范围[1,2,3,4]内,error:" +
                            actionType);
                }
            } catch (Throwable ignore) {
                ExceptionUtil.exceptionThrow(ignore);
                InternalAgent.e("推送信息下发的Action值不在规定范围[1,2,3,4]内,error:" +
                        actionType);
            }
            customParams.put(Constants.PUSH_EVENT_ACTION, action);
            customParams.put(Constants.PUSH_EVENT_ACTIONTYPE, actionType);
            customParams.put(Constants.PUSH_EVENT_COMPAIGNID, campaignID);
            customParams.put(Constants.PUSH_EVENT_CPD, cpd);
            customParams.put(Constants.PUSH_EVENT_CAMPID_NAME, campaignName);
            customParams.put(Constants.PUSH_EVENT_CAMPID_MEDIUM, campaignMedium);
            customParams.put(Constants.PUSH_EVENT_CAMPID_SOURCE, campaignSource);
            customParams.put(Constants.PUSH_EVENT_CAMPID_CONTENT, campaignContent);
            customParams.put(Constants.PUSH_EVENT_CAMPID_TERM, campaignTerm);
        } catch (Throwable ignore) {
            ExceptionUtil.exceptionThrow(ignore);
        }
        return customParams;
    }

    public void dealPushCallback(Map<String, Object> customParams, PushListener listener) {
        try {
            InternalAgent.i("dealPushCallback customParams: " + customParams);
            int code = (Integer) customParams.get(Constants.PUSH_EVENT_ACTIONTYPE);
            String action = (String) customParams.get(Constants.PUSH_EVENT_ACTION);
            String cpd = (String) customParams.get(Constants.PUSH_EVENT_CPD);
            InternalAgent.i("dealPushCallback code: " + code + ", action: " + action + ", cpd: " + cpd);
            switch (code) {
                case 1:
                    dealOpenApp(listener, action, customParams, cpd);
                    break;
                case 2:
                    dealOpenToSpecificApp(listener, action, customParams, cpd);
                    break;
                case 3:
                    dealOpenLinkedWeb(listener, action, customParams, cpd);
                    break;
                case 4:
                    AnalysysAgent.track(mContext, InternalAgent.PUSH_EVENT_PROCESS_SUCCESS,
                            customParams);
                    dealCustomAction(listener, action, cpd);
                    break;
                default:
                    break;
            }
        } catch (Throwable ignore) {
            ExceptionUtil.exceptionThrow(ignore);
        }
    }

    /**
     * 通过action打开app
     */
    private void dealOpenApp(PushListener listener, String action, Map<String,
            Object> customParams, String cpd) {
        try {
            if (mContext != null) {
                PackageManager pm = mContext.getPackageManager();
                String packageName = mContext.getPackageName();
                Intent intent = pm.getLaunchIntentForPackage(packageName);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                InternalAgent.i("dealOpenApp " + intent.toString());
                mContext.startActivity(intent);
                AnalysysAgent.track(mContext, InternalAgent.PUSH_EVENT_PROCESS_SUCCESS,
                        customParams);
            }
        } catch (Throwable ignore) {
            ExceptionUtil.exceptionThrow(ignore);
        }

        if (listener != null) {
            listener.execute(action, cpd);
        }
    }

    /**
     * 跳转到应用内指定页面
     */
    private void dealOpenToSpecificApp(PushListener listener, String activity,
                                       Map<String, Object> customParams, String cpd) {
        Intent intent = null;
        try {
            if (!TextUtils.isEmpty(activity) && mContext != null) {
                intent = new Intent(mContext, Class.forName(activity));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (!TextUtils.isEmpty(cpd)) {
                    intent.putExtra(Constants.PUSH_KEY_CPD, cpd);
                }
                mContext.startActivity(intent);
                AnalysysAgent.track(mContext, InternalAgent.PUSH_EVENT_PROCESS_SUCCESS,
                        customParams);
            }
        } catch (Throwable ignore) {
            ExceptionUtil.exceptionThrow(ignore);
        }
        if (listener != null) {
            listener.execute(activity, cpd);
        }
    }

    /**
     * 打开链接
     */
    private void dealOpenLinkedWeb(PushListener listener, String action,
                                   Map<String, Object> customParams, String cpd) {
        try {
            if (!TextUtils.isEmpty(action) && mContext != null) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setData(Uri.parse(action));
                if (!TextUtils.isEmpty(cpd)) {
                    intent.putExtra(Constants.PUSH_KEY_CPD, cpd);
                }
                mContext.startActivity(intent);
                AnalysysAgent.track(mContext, InternalAgent.PUSH_EVENT_PROCESS_SUCCESS, customParams);
            }
        } catch (Throwable ignore) {
            ExceptionUtil.exceptionThrow(ignore);
        }
        if (listener != null) {
            listener.execute(action, cpd);
        }
    }

    /**
     * 用户自定义操作
     */
    private void dealCustomAction(PushListener listener, String action, String cpd) {
        if (listener != null) {
            listener.execute(action, cpd);
        }
    }

    private static class Holder {
        public static final ThirdPushManager INSTANCE = new ThirdPushManager();
    }
}
