package com.analysys.demo;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.analysys.apidemo.R;

import java.lang.reflect.Constructor;

public class VisualClassBindActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visual_class_bind);
    }

    private int buttonIdx;

    public void onClick(View view) {
        try {
            ViewGroup vg = (ViewGroup) view.getParent();
            Constructor constructor = view.getClass().getDeclaredConstructor(Context.class);
            Button button = (Button) constructor.newInstance(this);
            button.setText("Button " + (buttonIdx++));
            ViewGroup.MarginLayoutParams lp = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.topMargin = (int) (20 * getResources().getDisplayMetrics().density + 0.5f);
            button.setLayoutParams(lp);
            vg.addView(button);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
