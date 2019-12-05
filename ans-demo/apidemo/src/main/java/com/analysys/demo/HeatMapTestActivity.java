package com.analysys.demo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.analysys.AnalysysAgent;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Description: 热图黑白名单测试
 * Author: fengzeyuan
 * Date: 2019-11-20 16:34
 * Version: 1.0
 */
public class HeatMapTestActivity extends AppCompatActivity {

    private List<String> mPages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heatmap_test);
        mPages = new ArrayList<>();
        mPages.add(MainActivity.class.getName());
        mPages.add(UserSettingActivity.class.getName());
    }

    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.ignoreButton) {
            // 设置热图黑名单
            AnalysysAgent.setHeatMapBlackListByPages(mPages);
        } else if (id == R.id.clearButton) {
            List<String> pages = new ArrayList<>();
            AnalysysAgent.setHeatMapBlackListByPages(pages);
            AnalysysAgent.setHeatMapWhiteListByPages(pages);
        } else if (id == R.id.SingleProcessButton) {
            startActivity(new Intent(HeatMapTestActivity.this, SingleProcessTestActivity.class));
        }
    }
}
