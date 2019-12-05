package com.analysys.demo;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.analysys.AnalysysAgent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;

/**
 * @Copyright © 2019 EGuan Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2019-07-01 00:48
 * @Author: Wang-X-C
 */
public class UserSettingActivity extends AppCompatActivity {
    Context mContext = null;
    String tag = "analysys";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_setting);
        mContext = this.getApplicationContext();
    }

    public void onClick(View view) {
        int viewId = view.getId();
        if (viewId == R.id.alias) {//设置aliasId，
            AnalysysAgent.alias(mContext, "s4af4a7g90af5ad");
        } else if (viewId == R.id.identify) {//淘宝店铺使用该功能时，只关注访客用户或店铺会员，不关注设备信息
            AnalysysAgent.identify(mContext, "identifyId");
            Log.i(tag, "不产生事件");
        } else if (viewId == R.id.reset) {//清除本地现有的用户属性,包括通用属性
            AnalysysAgent.reset(mContext);
            Log.i(tag, "不产生事件");
        } else if (viewId == R.id.getDistinctId) {
            String id = AnalysysAgent.getDistinctId(mContext);
            Log.i(tag, " Distinct id ：" + id);
        } else if (viewId == R.id.automaticCollection) {// 设置不做自动采集
            AnalysysAgent.setAutomaticCollection(mContext, false);
            Log.i(tag, "不产生事件");
        } else if (viewId == R.id.moreIgnoredAutomaticCollection) {// 设置忽略页面
            List<String> arrayList = new ArrayList<>();
            arrayList.add("com.analysys.compatibilitydemo.activity.MainActivity");
            AnalysysAgent.setIgnoredAutomaticCollectionActivities(mContext, arrayList);
            Log.i(tag, "不产生事件");
        } else if (viewId == R.id.getIgnoredAutomaticCollection) {
            List<String> activityNames =
                    AnalysysAgent.getIgnoredAutomaticCollection(mContext);
            if (activityNames != null) {
                for (String name : activityNames) {
                    Log.i(tag, "忽略自动采集的页面名称：" + name);
                }
            }
            Log.i(tag, "不产生事件");
        } else if (viewId == R.id.setURL) {//设置URL数据上传地址
            AnalysysAgent.setUploadURL(mContext, "");
            Log.i(tag, "不产生事件");
        } else if (viewId == R.id.setIntervalTime) {//上传的时间间隔定为20秒上传一次
            AnalysysAgent.setIntervalTime(mContext, 20);
            Log.i(tag, "不产生事件");
        } else if (viewId == R.id.setEventCount) {//设置上传条数
            AnalysysAgent.setMaxEventSize(mContext, 20);
            Log.i(tag, "不产生事件");
        } else if (viewId == R.id.flush) {//需要立刻将所有数据上传
            AnalysysAgent.flush(mContext);
        } else if (viewId == R.id.setMaxCacheSize) {//设置本地数据缓存上限值为2000条
            AnalysysAgent.setMaxCacheSize(mContext, 2000);
            Log.i(tag, "不产生事件");
        } else if (viewId == R.id.getPresetProperties) {//设置本地数据缓存上限值为2000条
            Map<String, Object> properties = AnalysysAgent.getPresetProperties(mContext);
            Log.i(tag, "预置属性：\n" + properties);
        }
    }
}
