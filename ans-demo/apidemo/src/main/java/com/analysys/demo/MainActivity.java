package com.analysys.demo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.alibaba.android.arouter.launcher.ARouter;
import com.analysys.ANSAutoPageTracker;
import com.analysys.AnalysysAgent;
import com.analysys.demo.allgroTest.AllgroTestActivity;

import java.util.HashMap;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;

/**
 * @Copyright © 2019 EGuan Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2019-07-01 00:48
 * @Author: Wang-X-C
 */
public class MainActivity extends AppCompatActivity implements ANSAutoPageTracker {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = getIntent();
        if (intent != null && intent.getData() != null) {
            // 判断如果是deepLink启动，设置启动来源为 3
            AnalysysAgent.launchSource(3);
        }


    }

    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.profileButton) {
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));
        } else if (id == R.id.propertyButton) {
            startActivity(new Intent(MainActivity.this, PropertyActivity.class));
        } else if (id == R.id.eventButton) {
            startActivity(new Intent(MainActivity.this, EventActivity.class));
        } else if (id == R.id.userButton) {
            startActivity(new Intent(MainActivity.this, UserSettingActivity.class));
        } else if (id == R.id.webViewButton) {
            startActivity(new Intent(MainActivity.this, WebViewActivity.class));
        } else if (id == R.id.visualDemoButton) {
            // 跳转可视化模块
            ARouter.getInstance().build("/visualDemo/TopVisualPage").navigation();
        } else if (id == R.id.TestHeatMapButton) {
            // 热图黑名单测试
            startActivity(new Intent(MainActivity.this, HeatMapTestActivity.class));
        } else if (id == R.id.TestAllgroButton) {
            // 跳转全埋点模块
            startActivity(new Intent(MainActivity.this, AllgroTestActivity.class));
        }
    }

    @Override
    public Map<String, Object> registerPageProperties() {
        //  $title为自动采集使用key，用户可覆盖
        Map<String, Object> properties = new HashMap<>();
        properties.put("$title", "详情页");
        return properties;
    }

    @Override
    public String registerPageUrl() {
        // 页面$url字段，将覆盖SDK默认字段
        return "HomePage";
    }

}
