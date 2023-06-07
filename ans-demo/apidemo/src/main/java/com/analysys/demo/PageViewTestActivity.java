package com.analysys.demo;

import android.os.Bundle;
import android.widget.TextView;

import com.analysys.ANSAutoPageTracker;
import com.analysys.apidemo.R;

import java.util.HashMap;
import java.util.Map;

public class PageViewTestActivity extends BaseActivity implements ANSAutoPageTracker {

    private Map<String, Object> pageProperties;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page_view_test);
        TextView tv = findViewById(R.id.tv_page_name);
        pageProperties = new HashMap<>();
        pageProperties.put("name", "iphone");
        pageProperties.put("price", 4000);
        tv.setText("页面名字：" + getClass().getName() + "\n\n\n自定义信息 " + pageProperties.toString());
    }

    @Override
    public Map<String, Object> registerPageProperties() {
        return pageProperties;
    }

    @Override
    public String registerPageUrl() {
        return null;
    }
}
