package com.analysys.visualdemo;

import android.content.Context;
import android.util.Log;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.facade.template.IProvider;
import com.analysys.AnalysysAgent;

@Route(path = "/visualDemo/api")
public class VisualDemoInit implements IProvider {

    private static final String SOCKET_URL = "wss://192.168.8.76:4091";
    private static final String CONFIG_URL = "https://192.168.8.76:4089";

    @Override
    public void init(Context context) {
        Log.e("jeven", "VisualDemoProvider--->init() call");
        if (context != null) {
            context = context.getApplicationContext();
        }
        if (context != null) {
            // 设置长连接Url
            AnalysysAgent.setVisitorDebugURL(context, SOCKET_URL);
            // 设置配置下发Url
            AnalysysAgent.setVisitorConfigURL(context, CONFIG_URL);
        }
    }
}
