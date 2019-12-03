package com.analysys.compatibilitydemo.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.analysys.compatibilitydemo.R;


public class SecondActivity extends AppCompatActivity {

    TextView textView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        textView = (TextView) findViewById(R.id.textView);
        try {
            Intent intent = getIntent();
            Uri uri = intent.getData();
            String host = uri.getHost();
            String path = uri.getPath();
            String key = uri.getQuery();
            textView.setText("host:" + host + ",path:" + path + ",key:" + key);
        } catch (Exception e) {

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
