package com.analysys.demo;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.analysys.AnalysysAgent;
import com.analysys.hybrid.HybridBridge;

import java.util.HashMap;
import java.util.Map;

/**
 * @Copyright © 2019 EGuan Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2019-07-01 00:48
 * @Author: Wang-X-C
 */
public class PropertyActivity extends AppCompatActivity {

    Context mContext = null;
    private static final String TAG = PropertyActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_property);
        mContext = this.getApplicationContext();
    }

    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.singleRegisterSuperProperty) {//购买一年腾讯会员，今年内只需设置一次即可
            AnalysysAgent.registerSuperProperty(mContext, "member", "VIP");
            Log.i(TAG, "不产生事件");
        } else if (id == R.id.multipleRegisterSuperProperty) {//购买了一年腾讯会员和设置了用户的年龄
            Map<String, Object> property = new HashMap<>();
            property.put("age", "20");
            property.put("member", "VIP");
            AnalysysAgent.registerSuperProperties(mContext, property);
            Log.i(TAG, "不产生事件");
        } else if (id == R.id.singleUnregisterSuperProperty) {//删除设置的用户年龄属性
            AnalysysAgent.unRegisterSuperProperty(mContext, "age");
            Log.i(TAG, "不产生事件");
        } else if (id == R.id.multipleClearSuperProperty) {//清除所有已经设置的用户数属性
            AnalysysAgent.clearSuperProperties(mContext);
            Log.i(TAG, "不产生事件");
        } else if (id == R.id.getSuperProperty) {//查看已经设置的“member”的通用属性
            Object singleSuperProperty = AnalysysAgent.getSuperProperty(mContext, "member");
            if (singleSuperProperty != null) {
                Log.i(TAG, "属性:Value = " + singleSuperProperty.toString());
            }
            Log.i(TAG, "不产生事件");
        } else if (id == R.id.getSuperProperties) {//查看所有已经设置的通用属性
            Map<String, Object> getProperty = AnalysysAgent.getSuperProperties(mContext);
            String properties = "";
            if (getProperty != null) {
                for (Map.Entry<String, Object> entry : getProperty.entrySet()) {
                    Log.i(TAG, "属性:Key = " + entry.getKey() + ", Value = " + entry.getValue());
                }
                Log.i(TAG, "不产生事件");
            }
        }
    }

}
