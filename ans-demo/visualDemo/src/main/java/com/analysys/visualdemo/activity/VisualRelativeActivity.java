package com.analysys.visualdemo.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.analysys.visualdemo.R;


public class VisualRelativeActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("相对布局");
        setContentView(R.layout.activity_visual_relative);
    }

    public void onClick(View v) {
        int id = v.getId();
    }
}
