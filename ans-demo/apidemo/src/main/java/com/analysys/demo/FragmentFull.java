package com.analysys.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import com.analysys.apidemo.R;
import com.analysys.utils.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * @Copyright © 2020 Analysys Inc. All rights reserved.
 * @Description:
 * @Create: 2020/7/21 5:22 PM
 * @author: huchangqing
 */
public class FragmentFull extends Fragment {

    class ClickAction {
        Class activityClz;
        String itemName;
        String eventName;

        ClickAction(Class clz, String name, String eName) {
            activityClz = clz;
            itemName = name;
            eventName = eName;
        }

        void doAction() {
            Activity activity = getActivity();
            if (activity == null) {
                return;
            }
            Intent intent = new Intent(activity, activityClz);
            intent.putExtra(BaseActivity.EXTRA_ACTIVITY_TITLE, itemName);
            intent.putExtra(BaseActivity.EXTRA_EVENT_NAME, eventName);
            activity.startActivity(intent);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_full, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ListView listView = view.findViewById(R.id.list_view);
        List<ClickAction> list = new ArrayList<>();
        list.add(new ClickAction(PropertyActivity.class, "通用属性", null));
        list.add(new ClickAction(PageViewActivity.class, "页面采集", Constants.PAGE_VIEW));
        list.add(new ClickAction(ProbeActivity.class, "全埋点", Constants.USER_CLICK));
        list.add(new ClickAction(HeatMapActivity.class, "热图", Constants.APP_CLICK));
        list.add(new ClickAction(VisualActivity.class, "可视化", null));
        list.add(new ClickAction(TrackActivity.class, "统计事件", null));
        list.add(new ClickAction(UserInfoActivity.class, "用户ID 用户属性", null));
        list.add(new ClickAction(HybridActivity.class, "Hybrid", null));
        list.add(new ClickAction(OtherActivity.class, "其它", null));
        List<String> listStr = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            listStr.add(list.get(i).itemName);
        }
        listView.setAdapter(new ArrayAdapter<>(getActivity(),
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
