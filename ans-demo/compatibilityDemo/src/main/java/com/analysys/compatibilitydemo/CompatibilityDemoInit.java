package com.analysys.compatibilitydemo;

import android.content.Context;
import android.util.Log;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.facade.template.IProvider;
import com.analysys.compatibilitydemo.push.GeTuiIntentService;
import com.analysys.compatibilitydemo.push.GeTuiService;
import com.analysys.compatibilitydemo.utils.ThreadPool;
import com.baidu.android.pushservice.PushConstants;
import com.igexin.sdk.PushManager;
import com.tencent.smtt.sdk.QbSdk;
import com.xiaomi.mipush.sdk.MiPushClient;

import cn.jpush.android.api.JPushInterface;

@Route(path = "/compatibilityDemo/api")
public class CompatibilityDemoInit implements IProvider {

    private static final String XIAOMI_APP_ID = "2882303761517835296";
    private static final String XIAOMI_APP_KEY = "5711783571296";
    private static final String BAIDU_APP_KEY = "2RHDBhc3wkkPVf4TqNzks4Tl";
    private Context mContext;

    @Override
    public void init(Context context) {
        Log.e("jeven", "CompatibilityDemoInit--->init() call");
        if (context != null) {
            mContext = context.getApplicationContext();
        }
        if (mContext != null) {
            initPushServices();
            initX5WebView();
        }
    }

    private void initPushServices() {
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
    private void initXiaoMiPush() {
        MiPushClient.registerPush(mContext, XIAOMI_APP_ID, XIAOMI_APP_KEY);
    }

    //个推初始化
    private void initGeTuiPush() {
        PushManager.getInstance().initialize(mContext, GeTuiService.class);
        PushManager.getInstance().registerPushIntentService(mContext, GeTuiIntentService.class);
    }

    //极光初始化
    private void initJPush() {
        JPushInterface.setDebugMode(true);
        JPushInterface.init(mContext);
    }

    //百度推送
    private void initBaiDuPush() {
        com.baidu.android.pushservice.PushManager.startWork(mContext,
                PushConstants.LOGIN_TYPE_API_KEY, BAIDU_APP_KEY);
    }

    private void initX5WebView() {
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
