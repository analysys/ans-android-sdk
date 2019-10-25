package com.analysys.visualdemo.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.analysys.visualdemo.R;


public class ViaualAbsoulteActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("绝对布局");
        setContentView(R.layout.activity_viaual_absoulte);
    }

    public void onClick(View v) {
        int id = v.getId();
    }
}
