package com.analysys.demo.allgroTest;

import android.os.Bundle;
import android.view.View;

import com.analysys.allgro.annotations.AnalysysIgnoreTrackClick;

import androidx.appcompat.app.AppCompatActivity;

/**
 * @author fengzeyuan
 */
@AnalysysIgnoreTrackClick
public class AllgroMainActivity extends AppCompatActivity implements View.OnClickListener {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
    }

    @Override
    public void onClick(View v) {
        
    }
}
