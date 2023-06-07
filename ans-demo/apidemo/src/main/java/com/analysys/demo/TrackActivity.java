package com.analysys.demo;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.analysys.AnalysysAgent;
import com.analysys.apidemo.R;

import java.util.HashMap;
import java.util.Map;

public class TrackActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track);
    }

    public void onClick(View view) {
        if (view.getId() == R.id.btn_track) {
            //统计用户确认订单的事件
            AnalysysAgent.track(this, "confirmOrder");
            Toast.makeText(this, "触发事件：confirmOrder", Toast.LENGTH_SHORT).show();
        } else {
            //用户购买某一商品需支付2000元
            Map<String, Object> eventInfo = new HashMap<>();
            eventInfo.put("name", "phone");
            eventInfo.put("money", 2000);
            AnalysysAgent.track(this, "payment", eventInfo);
            Toast.makeText(this, "触发事件：payment [name = phone, money = 2000]", Toast.LENGTH_SHORT).show();
        }
    }
}
