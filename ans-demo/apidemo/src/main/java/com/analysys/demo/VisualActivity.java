package com.analysys.demo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.analysys.apidemo.R;

import java.util.ArrayList;
import java.util.List;

public class VisualActivity extends BaseActivity {

    class ClickAction {
        Class activityClz;
        String itemName;

        ClickAction(Class clz, String name) {
            activityClz = clz;
            itemName = name;
        }

        void doAction() {
            Intent intent = new Intent(VisualActivity.this, activityClz);
            intent.putExtra(BaseActivity.EXTRA_ACTIVITY_TITLE, itemName);
            startActivity(intent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visual);
        ListView listView = findViewById(R.id.list_view);
        List<ClickAction> list = new ArrayList<>();
        list.add(new ClickAction(VisualBaseControlActivity.class, "基础控件"));
        list.add(new ClickAction(VisualPositionBindActivity.class, "按位置定位"));
        list.add(new ClickAction(VisualTextBindActivity.class, "按文字内容定位"));
        list.add(new ClickAction(VisualClassBindActivity.class, "按类型定位"));
        list.add(new ClickAction(VisualCrossPagesActivity.class, "跨页面定位"));
        list.add(new ClickAction(VisualRelatedActivity.class, "上报关联属性"));
        list.add(new ClickAction(VisualSibListViewActivity.class, "同级-ListView"));
        list.add(new ClickAction(VisualSibRecyclerViewActivity.class, "同级-RecyclerView"));
        list.add(new ClickAction(VisualSibViewPagerActivity.class, "同级-ViewPager"));
        list.add(new ClickAction(VisualSibGridViewActivity.class, "同级-GridView"));
        list.add(new ClickAction(VisualHybridActivity.class, "Hybrid可视化"));
        list.add(new ClickAction(Process1Activity.class, "多进程支持"));
        List<String> listStr = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            listStr.add(list.get(i).itemName);
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
