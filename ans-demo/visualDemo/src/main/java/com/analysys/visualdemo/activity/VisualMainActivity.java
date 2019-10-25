package com.analysys.visualdemo.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.analysys.visualdemo.R;

public class VisualMainActivity extends Activity {
    private static final String TAG = "VisualMainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("可视化测试主页面");
        setContentView(R.layout.activity_visual_main);
    }

    public void onClick(View v) {
        int id = v.getId();
        Intent in = null;
        if (id == R.id.btnLinear) {
            in = new Intent(this, VisualLinearActivity.class);
            startActivity(in);
        } else if (id == R.id.btnAbsoulte) {
            in = new Intent(this, ViaualAbsoulteActivity.class);
            startActivity(in);
        } else if (id == R.id.btnFrame) {
            in = new Intent(this, VisualFrameActivity.class);
            startActivity(in);
        } else if (id == R.id.btnConstraint) {
            in = new Intent(this, ViaualConstraintActivity.class);
            startActivity(in);
        } else if (id == R.id.btnRelative) {
            in = new Intent(this, VisualRelativeActivity.class);
            startActivity(in);
        } else if (id == R.id.btnTable) {
            in = new Intent(this, VisualTableActivity.class);
            startActivity(in);
        } else if (id == R.id.btnViewPager) {
            in = new Intent(this, VisualViewPagerActivity.class);
            startActivity(in);
        } else if (id == R.id.btnTestActivty) {
            in = new Intent(this, TestActivity.class);
            startActivity(in);
        } else if (id == R.id.flybutton) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("确认退出吗？");
            builder.setTitle("提示");
            builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    VisualMainActivity.this.finish();
                }
            });
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.create().show();
        } else if (id == R.id.crash) {
            "123".substring(10);

        } else if (id == R.id.back) {
            finish();
        } else if (id == R.id.ccAAAA) {
            startActivity(new Intent(this, VisualAggregationActivity.class));
        } else if (id == R.id.quit) {
            int pid = android.os.Process.myPid();
            android.os.Process.killProcess(pid);
        }

    }

    public void dialog(final View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("确认退出吗？");
        builder.setTitle("提示");
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                VisualMainActivity.this.finish();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }
}
