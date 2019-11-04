package com.analysys.strategy;

import android.content.Context;

import com.analysys.database.TableAllInfo;
import com.analysys.network.UploadManager;
import com.analysys.utils.Constants;
import com.analysys.utils.SharedUtil;

/**
 * @Copyright © 2018 EGuan Inc. All rights reserved.
 * @Description: 服务器策略判断
 * @Version: 1.0
 * @Create: 2018/3/16 17:53
 * @Author: WXC
 */
class ServicePolicy extends BaseSendStatus {


    /**
     * 服务器策略处理
     * 网络不通返回false
     * 判断debug模式是否打开
     * 策略为 0:智能发送,判断db内事件条数是否大于设置条数,当前时间减去上次发送时间是否大于设置的间隔时间
     * 策略为 1:实时发送,
     * 策略为 2:间隔发送,
     */
    @Override
    public boolean isSend(Context context) {
        if (context == null) {
            return false;
        }
        int serviceDebug = SharedUtil.getInt(context, Constants.SP_SERVICE_DEBUG, -1);
        if (serviceDebug == 1 || serviceDebug == 2) {
            return true;
        }
        long policyNo = SharedUtil.getLong(context, Constants.SP_POLICY_NO, -1L);
        if (policyNo == 0) {
            return intelligentPolicy(context);
        } else if (policyNo == 1) {
            return true;
        } else if (policyNo == 2) {
            return intervalPolicy(context);
        }
        return false;
    }

    private boolean intelligentPolicy(Context context) {
        long eventCount = PolicyManager.getEventCount(context);
        long dbCount = TableAllInfo.getInstance(context).selectCount();
        //数据库size大于设置
        if (eventCount < dbCount) {
            return true;
        }
        long sendTime = SharedUtil.getLong(context, Constants.SP_SEND_TIME, 0L);
        long intervalTime = PolicyManager.getIntervalTime(context);
        long timeDiff = Math.abs(System.currentTimeMillis() - sendTime);
        if (intervalTime < timeDiff) {
            return true;
        } else {
            UploadManager.getInstance(context).sendUploadDelayedMessage(intervalTime - timeDiff);
            return false;
        }
    }

    private boolean intervalPolicy(Context context) {
        long intervalTime = PolicyManager.getIntervalTime(context);
        long sendTime = SharedUtil.getLong(context, Constants.SP_SEND_TIME, 0L);
        long timeDiff = Math.abs(System.currentTimeMillis() - sendTime);
        if (intervalTime < timeDiff) {
            return true;
        } else {
            UploadManager.getInstance(context).sendUploadDelayedMessage(intervalTime - timeDiff);
            return false;
        }
    }
}
