package com.analysys.demo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.analysys.AnalysysAgent;

import java.util.HashSet;
import java.util.Set;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Description: 热图黑白名单测试
 * Author: fengzeyuan
 * Date: 2019-11-20 16:34
 * Version: 1.0
 */
public class HeatMapTestActivity extends AppCompatActivity {

    private Set<String> mPages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heatmap_test);
        mPages = new HashSet<>();
        mPages.add(MainActivity.class.getName());
        mPages.add(UserSettingActivity.class.getName());
    }

    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.openButton) {
            AnalysysAgent.getConfig().setAutoHeatMap(true);
            Toast.makeText(this, "重启后生效", Toast.LENGTH_LONG).show();
            view.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(HeatMapTestActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            }, 2000);
        } else if (id == R.id.closeButton) {
            AnalysysAgent.getConfig().setAutoHeatMap(false);
            Toast.makeText(this, "重启后生效", Toast.LENGTH_LONG).show();
            view.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(HeatMapTestActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            }, 2000);
        } else if (id == R.id.addButton) {
            // 设置热图白名单
            AnalysysAgent.setAutoHeatMapByPages(mPages);
        } else if (id == R.id.ignoreButton) {
            // 设置热图黑名单
            AnalysysAgent.setIgnoreHeatMaByPages(mPages);
        } else if (id == R.id.clearButton) {
            Set<String> pages = new HashSet<>();
            AnalysysAgent.setIgnoreHeatMaByPages(pages);
            AnalysysAgent.setAutoHeatMapByPages(pages);
        } else if (id == R.id.SingleProcessButton) {
            startActivity(new Intent(HeatMapTestActivity.this, SingleProcessTestActivity.class));
        }
    }
}
