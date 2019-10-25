package com.analysys.visualdemo.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.analysys.visualdemo.R;

public class DynamicAddActivity extends AppCompatActivity {

    LinearLayout linearLayout = null;
    private int l_row = 100;
    private int l_colum = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dynamic_add);
        linearLayout = (LinearLayout) findViewById(R.id.line1);
    }

    public void onClick(View view) {

        if (view.getId() == R.id.add) {
            l_row += 100;
            l_colum += 100;
            addButton(l_row, l_colum);
        }
    }

    public void addButton(int width, int height) {
        Button button = new Button(this);
        button.setText(l_row + "*" + l_colum);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width, height);
        layoutParams.width = width;
        layoutParams.height = height;
        button.setLayoutParams(layoutParams);
        linearLayout.addView(button);
    }
}















