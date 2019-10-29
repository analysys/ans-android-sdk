package com.analysys.demo;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.analysys.ANSAutoPageTracker;
import com.analysys.AnalysysAgent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Copyright © 2019 EGuan Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2019-07-01 00:48
 * @Author: Wang-X-C
 */
public class ProfileActivity extends AppCompatActivity implements ANSAutoPageTracker {
    Context mContext = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        mContext = this.getApplicationContext();
    }

    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.singleProfile) {//要统计分析使用邮箱登录的用户
            AnalysysAgent.profileSet(mContext, "Email", "yonghu@163.com");
        } else if (id == R.id.multipleProfile) {//要统计用户的登录方式邮箱登录,微信登录
            Map<String, Object> profileSet = new HashMap<>();
            profileSet.put("Email", "yonghu@163.com");
            profileSet.put("WeChatID", "weixinhao");
            AnalysysAgent.profileSet(mContext, profileSet);
        } else if (id == R.id.singleProfileSetOnce) {//要统计用户的激活时间
            AnalysysAgent.profileSetOnce(mContext, "activationTime", "1521594686781");
        } else if (id == R.id.multipleProfileSetOnce) {//要统计激活时间和首次登陆时间
            Map<String, Object> setOnceProfile = new HashMap<>();
            setOnceProfile.put("activationTime", "1521594686781");
            setOnceProfile.put("loginTime", "1521594726781");
            AnalysysAgent.profileSetOnce(mContext, setOnceProfile);
        } else if (id == R.id.singleProfileIncrement) {//用户年龄增加了一岁
            AnalysysAgent.profileIncrement(mContext, "age", 1);
        } else if (id == R.id.multipleProfileIncrement) {//用户年龄增加了一岁积分增加了200
            Map<String, Number> incrementProfile = new HashMap<>();
            incrementProfile.put("age", 1);
            incrementProfile.put("integral", 200);
            AnalysysAgent.profileIncrement(mContext, incrementProfile);
        } else if (id == R.id.singleProfileUnset) {// 要删除已经设置的用户年龄这一属性,
            AnalysysAgent.profileUnset(mContext, "age");
        } else if (id == R.id.multipleProfileDelete) {//清除用户的所有属性
            AnalysysAgent.profileDelete(mContext);
        } else if (id == R.id.singleProfileAppend) {//添加用户爱好
            AnalysysAgent.profileAppend(mContext, "hobby", "Music");
        } else if (id == R.id.multipleProfileAppend) {
            Map<String, Object> map = new HashMap<>();
            map.put("hobby", "PlayBasketball");
            map.put("sports", "Run");
            AnalysysAgent.profileAppend(mContext, map);
        } else if (id == R.id.listProfileAppend) {
            List<String> list = new ArrayList<>();
            list.add("PlayBasketball");
            list.add("music");
            AnalysysAgent.profileAppend(mContext, "hobby", list);
        }

    }

    @Override
    public Map<String, Object> registerPageProperties() {
        Map<String, Object> map = new HashMap<>();
        map.put("pageName", "用户属性页");
        map.put("pageDetails", "设置用户各个属性");
        return map;
    }

    @Override
    public String registerPageUrl() {
        return "analysysapp://com.analysys.demo/second?" +
                "utm_campaign_id=10000292&utm_campaign=AAA&" +
                "utm_medium=1&utm_source=1&utm_content=1&utm_term=1";
    }
}
