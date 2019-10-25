package com.analysys.visualdemo.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.analysys.visualdemo.R;

public class ViaualConstraintActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("约束布局");
        setContentView(R.layout.activity_viaual_constraint);
    }

    public void onClick(View v) {
        int id = v.getId();
    }
}
