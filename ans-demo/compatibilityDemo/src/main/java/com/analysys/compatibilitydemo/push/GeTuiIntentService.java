package com.analysys.compatibilitydemo.push;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.analysys.AnalysysAgent;
import com.analysys.push.PushListener;
import com.analysys.push.PushProvider;
import com.igexin.sdk.GTIntentService;
import com.igexin.sdk.message.GTCmdMessage;
import com.igexin.sdk.message.GTNotificationMessage;
import com.igexin.sdk.message.GTTransmitMessage;

public class GeTuiIntentService extends GTIntentService {
    PushListener listener = new PushListener() {
        @Override
        public void execute(String action, String jsonParams) {
            Log.d(TAG, "action : " + action);
            Log.d(TAG, "message : " + jsonParams);
        }
    };

    public GeTuiIntentService() {
    }

    @Override
    public void onReceiveServicePid(Context context, int i) {

    }

    @Override
    public void onReceiveClientId(Context context, String clientid) {
        Log.e(TAG, "onReceiveClientId -> " + "clientid = " + clientid);
        if(clientid!=null){
            AnalysysAgent.setPushID(context, PushProvider.GETUI, clientid);
        }
    }

    @Override
    public void onReceiveMessageData(Context context, GTTransmitMessage gtTransmitMessage) {
        // 透传消息，不会显示通知栏
        Log.e(TAG, "onReceiveMessageData -> " + "msg:" + gtTransmitMessage.toString());
        byte[] payload = gtTransmitMessage.getPayload();
        String data = new String(payload);
        AnalysysAgent.trackCampaign(context, data, false, listener);
    }

    @Override
    public void onReceiveOnlineState(Context context, boolean b) {

    }

    @Override
    public void onReceiveCommandResult(Context context, GTCmdMessage gtCmdMessage) {
        Log.e(TAG,"onReceiveCommandResult:"+gtCmdMessage.toString());
    }

    @Override
    public void onNotificationMessageArrived(Context context, GTNotificationMessage
            gtNotificationMessage) {
        // 通知栏消息
        Log.e(TAG, "onNotificationMessageArrived -> " + "msg:" + gtNotificationMessage.toString());
        String data = gtNotificationMessage.getContent();
        AnalysysAgent.trackCampaign(context, data, false, listener);
    }

    @Override
    public void onNotificationMessageClicked(Context context,
                                             GTNotificationMessage gtNotificationMessage) {
        // 点击通知栏消息
        Log.e(TAG, "onNotificationMessageClicked -> " + "msg:" + gtNotificationMessage.toString());
        String data = gtNotificationMessage.getContent();
        AnalysysAgent.trackCampaign(context, data, true, listener);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        Log.i(TAG, "Not yet implemented");
        return null;
    }
}
