package com.analysys.demo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.analysys.AnalysysAgent;
import com.analysys.apidemo.R;
import com.analysys.process.AgentProcess;
import com.analysys.utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Copyright © 2020 Analysys Inc. All rights reserved.
 * @Description:
 * @Create: 2020/7/22 11:37 AM
 * @author: huchangqing
 */
public class PageViewActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page_view);
        Switch autoTrack = findViewById(R.id.auto_track);
        autoTrack.setChecked(AgentProcess.getInstance().getConfig().isAutoTrackPageView());
        autoTrack.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                AgentProcess.getInstance().getConfig().setAutoTrackPageView(isChecked);
            }
        });

        String testPageName = PageViewTestActivity.class.getName();

        Button btnOpenPage = findViewById(R.id.btn_open_page);
        btnOpenPage.setText(btnOpenPage.getText() + testPageName);

        CheckBox checkBox = findViewById(R.id.cb_ignore_page);
        checkBox.setChecked(AgentProcess.getInstance().isThisPageInPageViewBlackList(testPageName));
        checkBox.setText(checkBox.getText() + testPageName);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    List<String> list = new ArrayList<>();
                    list.add(testPageName);
                    AnalysysAgent.setPageViewBlackListByPages(list);
                } else {
                    AnalysysAgent.setPageViewBlackListByPages(null);
                }
            }
        });
    }

    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btn_open_page) {
            Intent intent = new Intent(this, PageViewTestActivity.class);
            intent.putExtra(BaseActivity.EXTRA_ACTIVITY_TITLE, "页面采集测试");
            intent.putExtra(BaseActivity.EXTRA_EVENT_NAME, Constants.PAGE_VIEW);
            startActivity(intent);
        } else if (id == R.id.btn_manual_sigle) {
            //服务正在开展某个活动,需要统计活动页面时
            AnalysysAgent.pageView(this, "活动页");
            Toast.makeText(this, "调用成功，统计【活动页】", Toast.LENGTH_LONG).show();
        } else if (id == R.id.btn_manual_multi) {
            //购买一部iPhone手机,手机价格为8000元
            Map<String, Object> page = new HashMap<>();
            page.put("commodityName", "iPhone");
            page.put("commodityPrice", 8000);
            AnalysysAgent.pageView(this, "商品页", page);
            Toast.makeText(this, "调用成功，统计【商品页】，属性为[commodityName = iPhone, commodityPrice = 8000]", Toast.LENGTH_LONG).show();
        }
    }
}
