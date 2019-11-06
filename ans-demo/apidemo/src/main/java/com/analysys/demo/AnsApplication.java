package com.analysys.demo;

import android.app.Application;
import android.os.StrictMode;

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
    public static final int DEBUG_MODE = 2;
    public static final String APP_KEY = "androidtest";
    public static final String UPLOAD_URL = "http://192.168.10.91:8089";
    private static final String SOCKET_URL = "ws://192.168.10.91:9091";
    private static final String CONFIG_URL = "http://192.168.10.91:8089";

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
        }
    }

    /**
     * 初始化方舟SDK相关API
     */
    private void initAnalsysy() {
        AnalysysAgent.setDebugMode(this, DEBUG_MODE);
        //  设置 debug 模式，值：0、1、2
        AnalysysConfig config = new AnalysysConfig();
        // 设置key(目前使用电商demo的key)
        config.setAppKey(APP_KEY);
        // 设置渠道
        config.setChannel("AnalsysyDemo");
        // 设置追踪新用户的首次属性
        config.setAutoProfile(true);
        // 设置使用AES加密
        config.setEncryptType(EncryptEnum.AES);
        // 设置服务器时间校验
        config.setAllowTimeCheck(true);
        // 时间最大允许偏差为5分钟
        config.setMaxDiffTimeInterval(5 * 60);
        // 开启渠道归因
        config.setAutoInstallation(true);
        // 初始化
        AnalysysAgent.init(this, config);
        // 开启热图数据采集
        AnalysysAgent.setAutoHeatMap(true);
        // 设置数据上传/更新地址
        AnalysysAgent.setUploadURL(this, UPLOAD_URL);
        // 设置 WebSocket 连接 Url
        AnalysysAgent.setVisitorDebugURL(this, SOCKET_URL);
        // 设置配置下发 Url
        AnalysysAgent.setVisitorConfigURL(this, CONFIG_URL);
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
