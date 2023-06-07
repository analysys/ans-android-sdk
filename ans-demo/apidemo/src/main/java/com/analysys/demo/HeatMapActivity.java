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

public class HeatMapActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heatmap);
        Switch autoTrack = findViewById(R.id.auto_track);
        autoTrack.setChecked(AgentProcess.getInstance().getConfig().isAutoHeatMap());
        autoTrack.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                AgentProcess.getInstance().getConfig().setAutoHeatMap(isChecked);
            }
        });

        String testPageName = HeatMapTestActivity.class.getName();

        Button btnOpenPage = findViewById(R.id.btn_open_page);
        btnOpenPage.setText(btnOpenPage.getText() + testPageName);

        CheckBox cbIgnorePage = findViewById(R.id.cb_ignore_page);
        cbIgnorePage.setText(cbIgnorePage.getText() + testPageName);
        cbIgnorePage.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    List<String> list = new ArrayList<>();
                    list.add(testPageName);
                    AnalysysAgent.setHeatMapBlackListByPages(list);
                } else {
                    AnalysysAgent.setHeatMapBlackListByPages(null);
                }
            }
        });
    }

    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btn_open_page) {
            Intent intent = new Intent(this, HeatMapTestActivity.class);
            intent.putExtra(BaseActivity.EXTRA_ACTIVITY_TITLE, "热图页面测试");
            intent.putExtra(BaseActivity.EXTRA_EVENT_NAME, Constants.APP_CLICK);
            startActivity(intent);
        }
        if (id == R.id.btn_open_fragment_page) {

        }
    }
}