package com.analysys.visualdemo.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

import com.analysys.visualdemo.R;
import com.analysys.visualdemo.pupupwindow.CommonPopupWindow;


public class GravityActivity extends Activity implements View.OnClickListener {
    // anchor for popup window
    private Button gravityBt;
    // horizontal radio buttons
    private RadioButton alignLeftRb;
    private RadioButton alignRightRb;
    private RadioButton centerHoriRb;
    private RadioButton toRightRb;
    private RadioButton toLeftRb;
    // vertical radio buttons
    private RadioButton alignAboveRb;
    private RadioButton alignBottomRb;
    private RadioButton centerVertRb;
    private RadioButton toBottomRb;
    private RadioButton toAboveRb;
    // popup window
    private CommonPopupWindow window;
    private CommonPopupWindow.LayoutGravity layoutGravity;
    private TextView horiText;
    private TextView vertText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pupup_gravity);

        window = new CommonPopupWindow(this, R.layout.popup_gravity, 80,
                ViewGroup.LayoutParams.WRAP_CONTENT) {
            @Override
            protected void initView() {
                View view = getContentView();
                horiText = (TextView) view.findViewById(R.id.hori_text);
                vertText = (TextView) view.findViewById(R.id.vert_text);
            }

            @Override
            protected void initEvent() {
            }
        };
        layoutGravity =
                new CommonPopupWindow.LayoutGravity(CommonPopupWindow.LayoutGravity.ALIGN_LEFT | CommonPopupWindow.LayoutGravity.TO_BOTTOM);

        initView();
        initEvent();
    }

    private void initView() {
        gravityBt = (Button) findViewById(R.id.gravity_bt);

        alignLeftRb = (RadioButton) findViewById(R.id.align_left_rb);
        alignRightRb = (RadioButton) findViewById(R.id.align_right_rb);
        centerHoriRb = (RadioButton) findViewById(R.id.center_hori_rb);
        toRightRb = (RadioButton) findViewById(R.id.to_right_rb);
        toLeftRb = (RadioButton) findViewById(R.id.to_left_rb);

        alignAboveRb = (RadioButton) findViewById(R.id.align_above_rb);
        alignBottomRb = (RadioButton) findViewById(R.id.align_bottom_rb);
        centerVertRb = (RadioButton) findViewById(R.id.center_vert_rb);
        toBottomRb = (RadioButton) findViewById(R.id.to_bottom_rb);
        toAboveRb = (RadioButton) findViewById(R.id.to_above_rb);
    }

    private void initEvent() {
        gravityBt.setOnClickListener(this);

        alignLeftRb.setOnClickListener(this);
        alignRightRb.setOnClickListener(this);
        centerHoriRb.setOnClickListener(this);
        toRightRb.setOnClickListener(this);
        toLeftRb.setOnClickListener(this);

        alignAboveRb.setOnClickListener(this);
        alignBottomRb.setOnClickListener(this);
        centerVertRb.setOnClickListener(this);
        toBottomRb.setOnClickListener(this);
        toAboveRb.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.gravity_bt) {
            window.showBashOfAnchor(gravityBt, layoutGravity, 0, 0);
        } else if (id == R.id.align_left_rb) {
            layoutGravity.setHoriGravity(CommonPopupWindow.LayoutGravity.ALIGN_LEFT);
            horiText.setText("AL");
        } else if (id == R.id.align_right_rb) {
            layoutGravity.setHoriGravity(CommonPopupWindow.LayoutGravity.ALIGN_RIGHT);
            horiText.setText("AR");
        } else if (id == R.id.center_hori_rb) {
            layoutGravity.setHoriGravity(CommonPopupWindow.LayoutGravity.CENTER_HORI);
            horiText.setText("CH");
        } else if (id == R.id.to_right_rb) {
            layoutGravity.setHoriGravity(CommonPopupWindow.LayoutGravity.TO_RIGHT);
            horiText.setText("TR");
        } else if (id == R.id.to_left_rb) {
            layoutGravity.setHoriGravity(CommonPopupWindow.LayoutGravity.TO_LEFT);
            horiText.setText("TL");
        } else if (id == R.id.align_above_rb) {
            layoutGravity.setVertGravity(CommonPopupWindow.LayoutGravity.ALIGN_ABOVE);
            vertText.setText("AA");
        } else if (id == R.id.align_bottom_rb) {
            layoutGravity.setVertGravity(CommonPopupWindow.LayoutGravity.ALIGN_BOTTOM);
            vertText.setText("AB");
        } else if (id == R.id.center_vert_rb) {
            layoutGravity.setVertGravity(CommonPopupWindow.LayoutGravity.CENTER_VERT);
            vertText.setText("CV");
        } else if (id == R.id.to_bottom_rb) {
            layoutGravity.setVertGravity(CommonPopupWindow.LayoutGravity.TO_BOTTOM);
            vertText.setText("TB");
        } else if (id == R.id.to_above_rb) {
            layoutGravity.setVertGravity(CommonPopupWindow.LayoutGravity.TO_ABOVE);
            vertText.setText("TA");
        }
    }
}
