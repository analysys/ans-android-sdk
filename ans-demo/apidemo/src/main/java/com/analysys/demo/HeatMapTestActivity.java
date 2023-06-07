package com.analysys.demo;

import android.os.Bundle;
import android.widget.TextView;

import com.analysys.apidemo.R;

public class HeatMapTestActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heatmap_test);
        TextView tv = findViewById(R.id.tv_page_name);
        tv.setText("页面名字：" + getClass().getName());
    }
}
