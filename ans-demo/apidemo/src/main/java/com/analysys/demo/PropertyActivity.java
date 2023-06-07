package com.analysys.demo;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.analysys.AnalysysAgent;
import com.analysys.apidemo.R;
import com.analysys.utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PropertyActivity extends BaseActivity {

    class PropertyAction {
        String name;
        String eventName;
        Runnable runnable;

        PropertyAction(String name, String eventName, Runnable runnable) {
            this.name = name;
            this.eventName = eventName;
            this.runnable = runnable;
        }

        public void doAction() {
            runnable.run();
        }
    }

    public void onClick(View view) {
        AnalysysAgent.track(this, "track_test");
        Toast.makeText(this, "触发成功", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_property);
        ListView listView = findViewById(R.id.list_view);
        List<PropertyAction> list = new ArrayList<>();
        list.add(new PropertyAction("注册单个通用属性", Constants.PROFILE_SET, new Runnable() {
            @Override
            public void run() {
                //购买一年腾讯会员，今年内只需设置一次即可
                AnalysysAgent.registerSuperProperty(PropertyActivity.this, "member", "VIP");
                Toast.makeText(PropertyActivity.this, "注册[member = VIP]，本操作不产生事件，请触发事件后查看结果", Toast.LENGTH_LONG).show();
            }
        }));
        list.add(new PropertyAction("注册多个通用属性", Constants.PROFILE_SET, new Runnable() {
            @Override
            public void run() {
                //购买了一年腾讯会员和设置了用户的年龄
                Map<String, Object> property = new HashMap<>();
                property.put("age", "20");
                property.put("member", "VIP");
                AnalysysAgent.registerSuperProperties(PropertyActivity.this, property);
                Toast.makeText(PropertyActivity.this, "注册[age = 20，member = VIP]，本操作不产生事件，请触发事件后查看结果", Toast.LENGTH_LONG).show();
            }
        }));
        list.add(new PropertyAction("获取单个通用属性", Constants.PROFILE_INCREMENT, new Runnable() {
            @Override
            public void run() {
                //查看已经设置的“member”的通用属性
                Object singleSuperProperty = AnalysysAgent.getSuperProperty(PropertyActivity.this, "member");
                Toast.makeText(PropertyActivity.this, "member属性值为" + (singleSuperProperty == null ? "空" : singleSuperProperty.toString()), Toast.LENGTH_LONG).show();
            }
        }));
        list.add(new PropertyAction("获取所有通用属性", Constants.PROFILE_INCREMENT, new Runnable() {
            @Override
            public void run() {
                Map<String, Object> getProperty = AnalysysAgent.getSuperProperties(PropertyActivity.this);
                StringBuilder properties = new StringBuilder();
                if (getProperty != null) {
                    for (Map.Entry<String, Object> entry : getProperty.entrySet()) {
                        properties.append("\n").append(entry.getKey()).append(" = " + entry.getValue());
                    }
                }
                String props = properties.toString();
                Toast.makeText(PropertyActivity.this, "所有通用属性值为" + (TextUtils.isEmpty(props) ? "空" : props), Toast.LENGTH_LONG).show();
            }
        }));
        list.add(new PropertyAction("删除单个通用属性", Constants.PROFILE_SET_ONCE, new Runnable() {
            @Override
            public void run() {
                //删除设置的用户年龄属性
                AnalysysAgent.unRegisterSuperProperty(PropertyActivity.this, "age");
                Toast.makeText(PropertyActivity.this, "删除属性age，本操作不产生事件，请触发事件后查看结果", Toast.LENGTH_LONG).show();
            }
        }));
        list.add(new PropertyAction("删除所有通用属性", Constants.PROFILE_SET_ONCE, new Runnable() {
            @Override
            public void run() {
                AnalysysAgent.clearSuperProperties(PropertyActivity.this);
                Toast.makeText(PropertyActivity.this, "操作成功，本操作不产生事件，请触发事件后查看结果", Toast.LENGTH_LONG).show();
            }
        }));

        List<String> listStr = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            listStr.add(list.get(i).name);
        }
        listView.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                android.R.id.text1,
                listStr));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                list.get(position).doAction();
            }
        });
    }
}
