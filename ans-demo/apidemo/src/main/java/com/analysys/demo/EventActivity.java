package com.analysys.demo;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.analysys.ANSAutoPageTracker;
import com.analysys.AnalysysAgent;

import java.util.HashMap;
import java.util.Map;

/**
 * @Copyright © 2019 EGuan Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2019-07-01 00:48
 * @Author: Wang-X-C
 */
public class EventActivity extends AppCompatActivity implements ANSAutoPageTracker {
    Context mContext = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);
        mContext = this.getApplicationContext();
    }

    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.singleTrack) {//统计用户确认订单的事件
            AnalysysAgent.track(mContext, "confirmOrder");
        } else if (id == R.id.multipleTrack) {//用户购买某一商品需支付2000元，
            Map<String, Object> track = new HashMap<>();
            track.put("money", 2000);
            AnalysysAgent.track(mContext.getApplicationContext(), "payment", track);
        } else if (id == R.id.singlePage) {//服务正在开展某个活动,需要统计活动页面时
            AnalysysAgent.pageView(mContext, "活动页");
        } else if (id == R.id.multiplePage) {//购买一部iPhone手机,手机价格为8000元
            Map<String, Object> page = new HashMap<>();
            page.put("commodityName", "iPhone");
            page.put("commodityPrice", 8000);
            AnalysysAgent.pageView(mContext, "商品页", page);
        }
    }

    @Override
    public Map<String, Object> registerPageProperties() {
        return null;
    }

    @Override
    public String registerPageUrl() {
        return null;
    }
}