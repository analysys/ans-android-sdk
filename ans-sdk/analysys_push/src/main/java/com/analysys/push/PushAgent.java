package com.analysys.push;

import android.content.Context;

/**
 * @Copyright © 2018 EGuan Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2018/12/25 19:23
 * @Author: Wang-X-C
 */
public class PushAgent {

    /**
     * @param provider
     * @param pushId
     */
    public void enablePush(Context context, String provider, String pushId) {
        ThirdPushManager.getInstance(context).enablePush(provider, pushId);
    }

    /**
     * 追踪活动事件接口
     */
    public void trackCampaign(Context context, String campaign, boolean isClick,
                              PushListener listener) {
        ThirdPushManager.getInstance(context).trackCampaign(campaign, isClick, listener);
    }
}
