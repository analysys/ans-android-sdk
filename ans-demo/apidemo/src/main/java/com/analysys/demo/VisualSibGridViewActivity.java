package com.analysys.demo;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SimpleAdapter;

import com.analysys.apidemo.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VisualSibGridViewActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visual_sib_grid_view);
        GridView gridView = findViewById(R.id.grid_view);
        List<Map<String, Object>> dataList = new ArrayList<>();
        int[] icon = {R.drawable.icon, R.drawable.icon,
                R.drawable.icon, R.drawable.icon, R.drawable.icon,
                R.drawable.icon, R.drawable.icon, R.drawable.icon,
                R.drawable.icon, R.drawable.icon, R.drawable.icon,
                R.drawable.icon};
        String[] iconName = {"通讯录", "日历", "照相机", "时钟", "游戏", "短信", "铃声",
                "设置", "语音", "天气", "浏览器", "视频"};
        for (int i = 0; i < icon.length; i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("image", icon[i]);
            map.put("text", iconName[i]);
            dataList.add(map);
        }

        String[] from = {"image", "text"};
        int[] to = {R.id.image, R.id.text};
        SimpleAdapter adapter = new SimpleAdapter(this, dataList, R.layout.grid_view_item, from, to);
        //配置适配器
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            }
        });
    }
}
