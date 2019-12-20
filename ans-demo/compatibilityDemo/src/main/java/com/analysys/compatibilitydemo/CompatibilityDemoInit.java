package com.analysys.compatibilitydemo;

import android.content.Context;
import android.util.Log;

import com.analysys.compatibilitydemo.push.GeTuiIntentService;
import com.analysys.compatibilitydemo.push.GeTuiService;
import com.analysys.compatibilitydemo.utils.ThreadPool;
import com.baidu.android.pushservice.PushConstants;
import com.igexin.sdk.PushManager;
import com.tencent.smtt.sdk.QbSdk;
import com.xiaomi.mipush.sdk.MiPushClient;

import cn.jpush.android.api.JPushInterface;

public class CompatibilityDemoInit {

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
            }
        });
    }

    //小米推送
    private static void initXiaoMiPush() {
        MiPushClient.registerPush(mContext, XIAOMI_APP_ID, XIAOMI_APP_KEY);
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
}
