package com.analysys.demo;


import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;


import androidx.appcompat.app.AppCompatActivity;

import com.analysys.apidemo.R;
import com.analysys.process.AgentProcess;

import org.json.JSONObject;

/**
 * @Copyright © 2020 Analysys Inc. All rights reserved.
 * @Description:
 * @Create: 2020/7/22 11:06 AM
 * @author: huchangqing
 */
public class BaseActivity extends AppCompatActivity implements AgentProcess.IEventObserver {


    public static final String EXTRA_ACTIVITY_TITLE = "title";
    public static final String EXTRA_EVENT_NAME = "event_name";

    private String mEventName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String title = null;
        if (intent != null) {
            title = intent.getStringExtra(EXTRA_ACTIVITY_TITLE);
            mEventName = intent.getStringExtra(EXTRA_EVENT_NAME);
        }
//        ActionBar actionBar = getSupportActionBar();
//        if (actionBar != null) {
//            if (TextUtils.isEmpty(title)) {
//                actionBar.setDisplayShowTitleEnabled(false);
//            } else {
//                actionBar.setTitle(title);
//            }
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        TrackEventObserver.getInstance().setObserver(mEventName, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        TrackEventObserver.getInstance().setObserver(null, null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.base_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_info) {
            showMsgDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    private void showMsgDialog() {
        InfoDialog dialog = new InfoDialog(mEventName);
        dialog.show(getSupportFragmentManager(), "info");
    }

    @Override
    public void onEvent(String eventName, JSONObject sendData) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                hint(findViewById(R.id.action_info));
            }
        });
    }

    public void hint(View view) {
        if (view == null) {
            return;
        }
        AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
        alphaAnimation.setDuration(300);
        alphaAnimation.setRepeatCount(3);
        view.startAnimation(alphaAnimation);
    }
}
