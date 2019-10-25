package com.analysys.visualdemo.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.analysys.visualdemo.R;
import com.analysys.visualdemo.activity.DActivity;

public class VisualAvtivityC extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_c);
    }

    public void goD(final View view) {
        startActivity(new Intent(this, DActivity.class));
    }
}

