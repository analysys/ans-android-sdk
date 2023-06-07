package com.analysys.demo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.analysys.AnalysysAgent;
import com.analysys.apidemo.R;
import com.analysys.process.AgentProcess;
import com.analysys.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class ProbeActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_probe);
        Switch switch0 = findViewById(R.id.auto_track_fragment);
        switch0.setChecked(AgentProcess.getInstance().getConfig().isAutoTrackFragmentPageView());
        switch0.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                AgentProcess.getInstance().getConfig().setAutoTrackFragmentPageView(isChecked);
            }
        });

        switch0 = findViewById(R.id.auto_track_click);
        switch0.setChecked(AgentProcess.getInstance().getConfig().isAutoTrackClick());
        switch0.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                AgentProcess.getInstance().getConfig().setAutoTrackClick(isChecked);
            }
        });

        String pageName = ProbeClickTestActivity.class.getName();

        CheckBox checkBox = findViewById(R.id.auto_track_click_ignore);
        checkBox.setText(checkBox.getText() + pageName);
        checkBox.setChecked(AgentProcess.getInstance().isThisPageInAutoClickBlackList(pageName));
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    List<String> list = new ArrayList<>();
                    list.add(pageName);
                    AnalysysAgent.setAutoClickBlackListByPages(list);
                } else {
                    AnalysysAgent.setAutoClickBlackListByPages(null);
                }
            }
        });

        Button btn = findViewById(R.id.btn_open_fragment_page);
        checkBox = findViewById(R.id.auto_track_click_ignore_button);
        checkBox.setChecked(AgentProcess.getInstance().isThisViewTypeInAutoClickBlackList(btn.getClass()));
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    List<Class> list = new ArrayList<>();
                    list.add(btn.getClass());
                    AnalysysAgent.setAutoClickBlackListByViewTypes(list);
                } else {
                    AnalysysAgent.setAutoClickBlackListByViewTypes(null);
                }
            }
        });

        Button btnOpenPage = findViewById(R.id.btn_open_click_page);
        btnOpenPage.setText(btnOpenPage.getText() + pageName);
    }

    public void onClick(View view) {
        if (view.getId() == R.id.btn_open_fragment_page) {
            Intent intent = new Intent(this, ProbeFragmentTestActivity.class);
            intent.putExtra(EXTRA_ACTIVITY_TITLE, "Fragment PV测试页面");
            intent.putExtra(EXTRA_EVENT_NAME, Constants.PAGE_VIEW);
            startActivity(intent);
        } else if (view.getId() == R.id.btn_open_click_page) {
            Intent intent = new Intent(this, ProbeClickTestActivity.class);
            intent.putExtra(EXTRA_ACTIVITY_TITLE, "点击测试页面");
            intent.putExtra(EXTRA_EVENT_NAME, Constants.USER_CLICK);
            startActivity(intent);
        }
    }
}
