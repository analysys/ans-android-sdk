package com.analysys.visualdemo.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.analysys.visualdemo.R;

public class VisualTableActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("表格布局");
        setContentView(R.layout.activity_visual_table);
    }

    public void onClick(View v) {
        int id = v.getId();
    }
}
