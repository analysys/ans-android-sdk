package com.analysys.demo;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.analysys.apidemo.R;

public class VisualPositionBindActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visual_position_bind);
    }

    public void onClick(View view) {
        ViewGroup ll1 = findViewById(R.id.ll1);
        ViewGroup ll2 = findViewById(R.id.ll2);
        View view1 = ll1.getChildAt(0);
        View view2 = ll2.getChildAt(0);
        ll1.removeView(view1);
        ll2.removeView(view2);
        ll1.addView(view2);
        ll2.addView(view1);
    }
}
