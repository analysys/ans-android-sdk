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
    public static final String APP_KEY = "a217639dbb1f6a9c";
    public static final String UPLOAD_URL = "https://arkpaastest.analysys.cn:4089";
    private static final String SOCKET_URL = "wss://arkpaastest.analysys.cn:4091";
    private static final String CONFIG_URL = "https://arkpaastest.analysys.cn:4089";
    private static  AnsApplication instance;


    private boolean isDebug = true;

    public static AnsApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        // 设置严苛模式
        strictMode();
        // 初始化ARouter
        initRouter();

        // 初始化方舟SDK
        initAnalsysy();

        // 尝试初始化对应模块
        if ("compatibility".equals(BuildConfig.Build_Type)) {// 尝试初始化三方兼容模块
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
        // 热图数据采集（默认关闭）
        config.setAutoHeatMap(false);
        // pageView自动上报总开关（默认开启）
        config.setAutoTrackPageView(true);
        // fragment-pageView自动上报开关（默认关闭）
        config.setAutoTrackFragmentPageView(false);
        // 点击自动上报开关（默认关闭）
        config.setAutoTrackClick(false);

        config.setEnableException(true);
        // 初始化
        AnalysysAgent.init(this, config);
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
