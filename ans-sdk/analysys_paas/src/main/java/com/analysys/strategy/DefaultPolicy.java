package com.analysys.strategy;

import android.content.Context;

/**
 * @Copyright © 2018 EGuan Inc. All rights reserved.
 * @Description: 本地策略判断
 * @Version: 1.0
 * @Create: 2018/3/16 17:53
 * @Author: WXC
 */
class DefaultPolicy extends BaseSendStatus {
    /**
     * 判断网络是否通
     * 判断是否为WiFi
     * 断db内事件条数是否大于设置条数
     * 当前时间减去上次发送时间是否大于设置的间隔时间
     */
    @Override
    public boolean isSend(Context context) {
        return true;
    }
}