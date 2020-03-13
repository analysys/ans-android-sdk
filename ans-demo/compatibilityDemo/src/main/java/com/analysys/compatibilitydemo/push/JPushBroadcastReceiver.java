package com.analysys.compatibilitydemo.push;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.analysys.AnalysysAgent;
import com.analysys.push.PushListener;
import com.analysys.push.PushProvider;

import cn.jpush.android.api.JPushInterface;

public class JPushBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = "JPushBroadcastReceiver";
    private Context mContext;
    PushListener listener = new PushListener() {
        @Override
        public void execute(String action, String jsonParams) {
            Log.e("EgPushTAG", "通知被点击");
        }
    };

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        Bundle bundle = intent.getExtras();
        if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) {
            String regId = bundle.getString(JPushInterface.EXTRA_REGISTRATION_ID);
            Log.d(TAG, "接收Registration Id: " + regId);
            //易观打开推送接口
            AnalysysAgent.setPushID(context, PushProvider.JPUSH, regId);
        } else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent.getAction())) {
            int notifactionId = bundle.getInt(JPushInterface.EXTRA_NOTIFICATION_ID);
            Log.d(TAG, "接收到推送下来的通知的ID: " + notifactionId);
            //接收到Push的通知
            AnalysysAgent.trackCampaign(context, bundle.getString(JPushInterface.EXTRA_EXTRA),
                    false);
        } else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {
            Log.d(TAG, "用户点击打开了通知");
            //易观添加活动推广接口，点击了Push推下来的通知
            AnalysysAgent.trackCampaign(context, bundle.getString(JPushInterface.EXTRA_EXTRA),
                    true, listener);
        }
    }
}
