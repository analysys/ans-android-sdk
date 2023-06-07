package com.analysys.demo;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TabHost;

import com.analysys.AnalysysAgent;
import com.analysys.allgro.annotations.AnalysysIgnoreTrackClick;
import com.analysys.apidemo.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProbeClickTestActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_probe_click_test);
        Button btnListener = findViewById(R.id.btn_listener);
        btnListener.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        Button btnLambda = findViewById(R.id.btn_lambda);
        btnLambda.setOnClickListener(v -> {
        });

        Button btnIgnore = findViewById(R.id.btn_ignore_annotation);
        btnIgnore.setOnClickListener(new View.OnClickListener() {
            @Override
            @AnalysysIgnoreTrackClick
            public void onClick(View v) {
            }
        });

        btnIgnore = findViewById(R.id.btn_ignore_blacklist);
        btnIgnore.setOnClickListener(v -> {
        });
        AnalysysAgent.setAutoClickBlackListByView(btnIgnore);

        CheckBox checkBox = findViewById(R.id.checkbox);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            }
        });

        RatingBar ratingBar = findViewById(R.id.rating_bar);
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
            }
        });

        SeekBar seekBar = findViewById(R.id.seek_bar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        RadioGroup radioGroup = findViewById(R.id.radio_group);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
            }
        });

        ListView listView = findViewById(R.id.list_view);
        List<String> listStr = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            listStr.add("list row " + i);
        }
        listView.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                android.R.id.text1,
                listStr));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            }
        });
        listView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        ExpandableListView expandableListView = findViewById(R.id.ex_list_view);
        List<Map<String, String>> groupData = new ArrayList<Map<String, String>>();
        List<List<Map<String, String>>> childData = new ArrayList<List<Map<String, String>>>();
        final String NAME = "NAME";
        final String IS_EVEN = "IS_EVEN";
        for (int i = 0; i < 3; i++) {
            Map<String, String> curGroupMap = new HashMap<String, String>();
            groupData.add(curGroupMap);
            curGroupMap.put(NAME, "Group " + i);
            curGroupMap.put(IS_EVEN, (i % 2 == 0) ? "This group is even" : "This group is odd");

            List<Map<String, String>> children = new ArrayList<Map<String, String>>();
            for (int j = 0; j < 5; j++) {
                Map<String, String> curChildMap = new HashMap<String, String>();
                children.add(curChildMap);
                curChildMap.put(NAME, "Child " + j);
                curChildMap.put(IS_EVEN, (j % 2 == 0) ? "This child is even" : "This child is odd");
            }
            childData.add(children);
        }
        expandableListView.setAdapter(new SimpleExpandableListAdapter(
                this,
                groupData,
                android.R.layout.simple_expandable_list_item_2,
                new String[]{NAME, IS_EVEN},
                new int[]{android.R.id.text1, android.R.id.text2},
                childData,
                android.R.layout.simple_expandable_list_item_2,
                new String[]{NAME, IS_EVEN},
                new int[]{android.R.id.text1, android.R.id.text2}
        ));
        expandableListView.expandGroup(groupData.size() - 1);
        expandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                return false;
            }
        });
        expandableListView.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> false);

        TabHost tabHost = findViewById(R.id.tab_host);
        tabHost.setup();
        tabHost.addTab(tabHost.newTabSpec("tab1").setIndicator("Tab1", null).setContent(R.id.tab1));
        tabHost.addTab(tabHost.newTabSpec("tab2").setIndicator("Tab2", null).setContent(R.id.tab2));
        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
            }
        });

        Button btnDialog = findViewById(R.id.btn_dialog);
        btnDialog.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(ProbeClickTestActivity.this);
            builder.setMessage("对话框点击测试");
            builder.setTitle("提示");
            builder.setPositiveButton("确认", (dialog, which) -> dialog.dismiss());
            builder.show();
        });

        Button btnDialogMultiChoice = findViewById(R.id.btn_dialog_multi_choice);
        btnDialogMultiChoice.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(ProbeClickTestActivity.this);
            builder.setTitle("提示");
            String[] items = {"item1", "item2", "item3"};
            boolean[] choices = {false, false, false};
            builder.setMultiChoiceItems(items, choices, (dialog, which, isChecked) -> {
            });
            builder.setPositiveButton("确认", (dialog, which) -> dialog.dismiss());
            builder.show();
        });
    }

    public void onClick(View view) {
    }
}
