package com.analysys.compatibilitydemo.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.analysys.AnalysysAgent;
import com.analysys.compatibilitydemo.R;


public class SecondActivity extends AppCompatActivity {

    TextView textView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        textView = (TextView) findViewById(R.id.textView);
        Intent intent = getIntent();
        Uri uri = intent.getData();
        String host = uri.getHost();
        String path = uri.getPath();
        String key = uri.getQuery();
        textView.setText("host:" + host + ",path:" + path + ",key:" + key);
        if (uri != null) {
            // 判断如果是deepLink启动，设置启动来源为 3
            AnalysysAgent.launchSource(3);
        }
    }

    //@Override
    //protected void onResume() {
    //    super.onResume();
    //    SecondActivity.this.finish();
    //}
    @Override
    protected void onStop() {
        super.onStop();
        Runtime.getRuntime().gc();
    }
}
