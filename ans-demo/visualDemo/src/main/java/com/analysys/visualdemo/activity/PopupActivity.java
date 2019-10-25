package com.analysys.visualdemo.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.analysys.visualdemo.R;
import com.analysys.visualdemo.pupupwindow.CommonPopupWindow;

import java.util.ArrayList;
import java.util.List;

public class PopupActivity extends Activity implements View.OnClickListener {
    private View activityPopup;
    private Button translateBt;
    private Button alphaBt;
    private Button scaleBt;
    private Button rotateBt;

    private CommonPopupWindow window;
    private ListView dataList;
    private List<String> datas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popup);

        initData();
        initPopupWindow();
        initView();
    }

    private void initData() {
        datas = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            datas.add("Item " + i);
        }
    }

    private void initPopupWindow() {
        // get the height of screen
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int screenHeight = metrics.heightPixels;
        // create popup window
        window = new CommonPopupWindow(this, R.layout.popup_list,
                ViewGroup.LayoutParams.MATCH_PARENT,
                (int) (screenHeight * 0.7)) {
            @Override
            protected void initView() {
                View view = getContentView();
                dataList = (ListView) view.findViewById(R.id.data_list);
                dataList.setAdapter(new ArrayAdapter<String>(PopupActivity.this,
                        R.layout.popup_list_item, datas));
            }

            @Override
            protected void initEvent() {
                dataList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position,
                                            long id) {
                        window.getPopupWindow().dismiss();
                    }
                });
            }

            @Override
            protected void initWindow() {
                super.initWindow();
                PopupWindow instance = getPopupWindow();
                instance.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        WindowManager.LayoutParams lp = getWindow().getAttributes();
                        lp.alpha = 1.0f;
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                        getWindow().setAttributes(lp);
                    }
                });
            }
        };
    }

    private void initView() {
        activityPopup = findViewById(R.id.activity_popup);
        translateBt = (Button) findViewById(R.id.translate_bt);
        translateBt.setOnClickListener(this);
        alphaBt = (Button) findViewById(R.id.alpha_bt);
        alphaBt.setOnClickListener(this);
        scaleBt = (Button) findViewById(R.id.scale_bt);
        scaleBt.setOnClickListener(this);
        rotateBt = (Button) findViewById(R.id.rotate_bt);
        rotateBt.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        PopupWindow win = window.getPopupWindow();
        int id = v.getId();
        if (id == R.id.translate_bt) {
            win.setAnimationStyle(R.style.animTranslate);
        } else if (id == R.id.alpha_bt) {
            win.setAnimationStyle(R.style.animAlpha);
        } else if (id == R.id.scale_bt) {
            win.setAnimationStyle(R.style.animScale);
        } else if (id == R.id.rotate_bt) {
            win.setAnimationStyle(R.style.animRotate);
        }
        window.showAtLocation(activityPopup, Gravity.BOTTOM, 0, 0);
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = 0.3f;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        getWindow().setAttributes(lp);
    }
}
