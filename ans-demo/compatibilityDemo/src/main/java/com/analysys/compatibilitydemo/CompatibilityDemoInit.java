package com.analysys.compatibilitydemo;

import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Process;
import android.util.Log;

import com.alibaba.sdk.android.push.CloudPushService;
import com.alibaba.sdk.android.push.CommonCallback;
import com.alibaba.sdk.android.push.noonesdk.PushServiceFactory;
import com.analysys.AnalysysAgent;
import com.analysys.compatibilitydemo.push.GeTuiIntentService;
import com.analysys.compatibilitydemo.push.GeTuiService;
import com.analysys.compatibilitydemo.utils.ThreadPool;
import com.analysys.push.PushProvider;
import com.baidu.android.pushservice.PushConstants;
import com.igexin.sdk.PushManager;
import com.tencent.android.tpush.XGIOperateCallback;
import com.tencent.android.tpush.XGPushConfig;
import com.tencent.android.tpush.XGPushManager;
import com.tencent.android.tpush.common.Constants;
import com.tencent.smtt.sdk.QbSdk;
import com.xiaomi.mipush.sdk.MiPushClient;

import java.util.List;

import cn.jpush.android.api.JPushInterface;

public class CompatibilityDemoInit {
    private static final String TAG = "CompatibilityDemoInit";
    private static final String XIAOMI_APP_ID = "2882303761517835296";
    private static final String XIAOMI_APP_KEY = "5711783571296";
    private static final String BAIDU_APP_KEY = "dicFHwycZXtvbwUqgvgZU6Ay";
    private static Context mContext;

    public static void init(Context context) {
        Log.e("jeven", "CompatibilityDemoInit--->init() call");
        if (context != null) {
            mContext = context.getApplicationContext();
        }
        if (mContext != null) {
            initPushServices();
            initX5WebView();
        }
    }

    private static void initPushServices() {
        ThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                initGeTuiPush();
                initJPush();
                initBaiDuPush();
                initXiaoMiPush();
                initXgPush();
            }
        });
        initCloudChannel();

    }
    /**
     * 初始化云推送通道
     */
    private static void initCloudChannel() {
        createNotificationChannel();
        PushServiceFactory.init(mContext);
        CloudPushService pushService = PushServiceFactory.getCloudPushService();
        Log.d(TAG,"deviceId:"+pushService.getDeviceId()+",utdeviceid:"+pushService.getUTDeviceId());
        if(shouldInit()){
            if (pushService.getDeviceId() != null) {
                AnalysysAgent.setPushID(mContext, PushProvider.ALIYUN, pushService.getDeviceId());
            }
        }

        pushService.register(mContext, new CommonCallback() {
            @Override
            public void onSuccess(String response) {
                Log.d(TAG, "init cloudchannel success");
            }
            @Override
            public void onFailed(String errorCode, String errorMessage) {
                Log.d(TAG, "init cloudchannel failed -- errorcode:" + errorCode + " -- errorMessage:" + errorMessage);
            }
        });
    }

    private static void initXgPush() {

        if(!shouldInit()){
            return;
        }

        //开启信鸽的日志输出，线上版本不建议调用
        XGPushConfig.enableDebug(mContext, true);
//        XGPushConfig.getToken(mContext);

        /*
        注册信鸽服务的接口
        如果仅仅需要发推送消息调用这段代码即可
        */
        XGPushManager.registerPush(mContext, new XGIOperateCallback() {
            @Override
            public void onSuccess(Object data, int flag) {
                //token在设备卸载重装的时候有可能会变
                Log.d("TPush", "注册成功，设备token为：" + data);
            }

            @Override
            public void onFail(Object data, int errCode, String msg) {
                Log.d("TPush", "注册失败，错误码：" + errCode + ",错误信息：" + msg);
            }
        });

        // 获取token
        XGPushConfig.getToken(mContext);
    }

    private static void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            // 通知渠道的id
            String id = "1";
            // 用户可以看到的通知渠道的名字.
            CharSequence name = "notification channel";
            // 用户可以看到的通知渠道的描述
            String description = "notification description";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(id, name, importance);
            // 配置通知渠道的属性
            mChannel.setDescription(description);
            // 设置通知出现时的闪灯（如果 android 设备支持的话）
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.RED);
            // 设置通知出现时的震动（如果 android 设备支持的话）
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            //最后在notificationmanager中创建该通知渠道
            mNotificationManager.createNotificationChannel(mChannel);
        }
    }

    //小米推送
    private static void initXiaoMiPush() {
        if(shouldInit()){
            MiPushClient.registerPush(mContext, XIAOMI_APP_ID, XIAOMI_APP_KEY);
        }
    }

    //个推初始化
    private static void initGeTuiPush() {
        PushManager.getInstance().initialize(mContext, GeTuiService.class);
        PushManager.getInstance().registerPushIntentService(mContext, GeTuiIntentService.class);
    }

    //极光初始化
    private static void initJPush() {
        JPushInterface.setDebugMode(true);
        JPushInterface.init(mContext);
    }

    //百度推送
    private static void initBaiDuPush() {
        com.baidu.android.pushservice.PushManager.startWork(mContext,
                PushConstants.LOGIN_TYPE_API_KEY, BAIDU_APP_KEY);
    }

    private static void initX5WebView() {
        QbSdk.PreInitCallback cb = new QbSdk.PreInitCallback() {

            @Override
            public void onViewInitFinished(boolean arg0) {
                //x5內核初始化完成的回调，为true表示x5内核加载成功，否则表示x5内核加载失败，会自动切换到系统内核。
                Log.d("analysys", " onViewInitFinished is " + arg0);
            }

            @Override
            public void onCoreInitFinished() {
            }
        };
        //x5内核初始化接口
        QbSdk.initX5Environment(mContext, cb);
    }

    private static boolean shouldInit() {
        ActivityManager am = ((ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE));
        List<ActivityManager.RunningAppProcessInfo> processInfos = am.getRunningAppProcesses();
        String mainProcessName = mContext.getApplicationInfo().processName;
        int myPid = Process.myPid();
        for (ActivityManager.RunningAppProcessInfo info : processInfos) {
            if (info.pid == myPid && mainProcessName.equals(info.processName)) {
                return true;
            }
        }
        return false;
    }
}
