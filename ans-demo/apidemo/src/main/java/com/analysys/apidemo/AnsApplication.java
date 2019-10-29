package com.analysys.apidemo;

import android.app.Application;
import android.os.StrictMode;

import com.alibaba.android.arouter.facade.template.IProvider;
import com.alibaba.android.arouter.launcher.ARouter;
import com.analysys.AnalysysAgent;
import com.analysys.AnalysysConfig;
import com.analysys.EncryptEnum;

/**
 * @Copyright © 2019 EGuan Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2019-07-01 00:48
 * @Author: Wang-X-C
 */
public class AnsApplication extends Application {

    public static final String UPLOAD_URL = "https://192.168.8.76:4089";
    private boolean isDebug = true;

    @Override
    public void onCreate() {
        super.onCreate();
        // 设置严苛模式
        strictMode();
        // 初始化ARouter
        initRouter();

        // 初始化方舟SDK
        initAnalsysy();

        // 尝试初始化对应模块
        switch (BuildConfig.Build_Type) {
            case "compatibility":
                // 尝试初始化三方兼容模块
                ARouter.getInstance().build("/compatibilityDemo/api").navigation();
            case "visual":
                // 尝试初始化可视化模块
                ARouter.getInstance().build("/visualDemo/api").navigation();
                break;
        }


    }


    /**
     * 初始化方舟SDK相关API
     */
    private void initAnalsysy() {
        AnalysysAgent.setDebugMode(this, 2);
        //  设置 debug 模式，值：0、1、2
        AnalysysConfig config = new AnalysysConfig();
        // 设置key(目前使用电商demo的key)
        config.setAppKey("ecommercedemo");
        // 设置渠道
        config.setChannel("AnalsysyDemo");
        // 设置追踪新用户的首次属性
        config.setAutoProfile(true);
        // 设置使用AES加密
        config.setEncryptType(EncryptEnum.AES);
        // 初始化
        AnalysysAgent.init(this, config);
        AnalysysAgent.setAutoHeatMap(false);
        // 设置数据上传/更新地址
        AnalysysAgent.setUploadURL(this, UPLOAD_URL);
    }

    /**
     * 设置严苛模式
     */
    private void strictMode() {
        if (isDebug) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build());
        }
    }

    /**
     * 初始化Router
     */
    private void initRouter() {
        if (isDebug) {
            ARouter.openLog();
            ARouter.openDebug();
        }
        ARouter.init(this);
    }
}
