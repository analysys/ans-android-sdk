package com.analysys.demo.allgroTest;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.Toast;

import com.analysys.allgro.annotations.AnalysysIgnoreTrackClick;
import com.analysys.demo.AnsApplication;

import androidx.appcompat.app.AppCompatActivity;

/**
 * @author fengzeyuan
 */
@AnalysysIgnoreTrackClick
public class AllgroMainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));




        testbtn(layout);

        setContentView(layout);

    }

    private void testbtn(LinearLayout layout) {
        Button view = new Button(AnsApplication.getInstance());
        view.setText("按钮");

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(AnsApplication.getInstance(), "按钮点击", Toast.LENGTH_LONG).show();
                showDialog();
            }
        });

        layout.addView(view);
    }

    private void testListView(LinearLayout layout) {
        final String[] COUNTRIES = new String[]{"中国", "俄罗斯", "英国", "法国"};

        ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, COUNTRIES);
        ListView listView = new ListView(AnsApplication.getInstance());
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
        layout.addView(listView);
    }

    private void showDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(AllgroMainActivity.this);
//        builder.setMultiChoiceItems(null, null, (dialog, which, isChecked) -> {
//
//        });

        builder.setMultiChoiceItems(null, null, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {

            }
        });

//        builder.setPositiveButton("确定", (dialog, which) -> {
//        });

        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setTitle("Test");
        builder.setMessage("你要点击哪一个按钮");
        builder.show();
    }

    private void test() {
        TabHost tabHost = new TabHost(AllgroMainActivity.this);

        tabHost.setOnTabChangedListener(tabId -> {

        });

        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {

            }
        });
    }
}
