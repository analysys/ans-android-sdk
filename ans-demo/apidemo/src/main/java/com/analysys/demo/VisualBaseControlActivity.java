package com.analysys.demo;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.analysys.apidemo.R;

import java.util.ArrayList;
import java.util.List;

public class VisualBaseControlActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visual_base_control);
    }

    public void onClick(View view) {
        if (view.getId() == R.id.btn_dialog) {
            showDialog();
        } else if (view.getId() == R.id.btn_pop) {
            showPop(view);
        }
    }

    private void showPop(View view) {
        ListView listView = new ListView(this);
        listView.setBackgroundColor(0xffdddddd);
        PopupWindow popupWindow = new PopupWindow(listView, 400, 600);
        List<String> listData = new ArrayList<>();
        listData.add("row 0");
        listData.add("row 1");
        listData.add("row 2");
        listData.add("row 3");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, listData);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                popupWindow.dismiss();
            }
        });
        popupWindow.setFocusable(true);
        popupWindow.showAsDropDown(view);
    }

    private void showDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_visual);
        dialog.findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}
