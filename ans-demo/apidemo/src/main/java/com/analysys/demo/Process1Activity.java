package com.analysys.demo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.ActionBar;

import com.analysys.apidemo.R;

public class Process1Activity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process1);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("进程1");
        }
    }

    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btn_ipc) {
        } else if (id == R.id.btn_process2) {
            Intent intent = new Intent(this, Process2Activity.class);
            intent.putExtra(EXTRA_ACTIVITY_TITLE, "进程2");
            startActivity(intent);
        }
    }
}
