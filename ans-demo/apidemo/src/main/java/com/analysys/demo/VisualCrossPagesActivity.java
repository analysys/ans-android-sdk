package com.analysys.demo;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;

import com.analysys.apidemo.R;

public class VisualCrossPagesActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visual_cross_pages);
    }

    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btn_open_page) {
            Intent intent = new Intent(this, VisualTextBindActivity.class);
            intent.putExtra(BaseActivity.EXTRA_ACTIVITY_TITLE, "按文字内容定位");
            startActivity(intent);
        } else if (id == R.id.btn_open_dialog) {
            showDialog();
        } else if (id == R.id.btn_open_pop) {
            showPop(view);
        }
    }

    private void showPop(View view) {
        View root = LayoutInflater.from(this).inflate(R.layout.pop_layout, null);
        PopupWindow popupWindow = new PopupWindow(root, 400, 600);
        popupWindow.setFocusable(true);
        popupWindow.showAsDropDown(view);
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        };
        root.findViewById(R.id.btn_ok).setOnClickListener(listener);
        root.findViewById(R.id.btn_cancel).setOnClickListener(listener);
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
