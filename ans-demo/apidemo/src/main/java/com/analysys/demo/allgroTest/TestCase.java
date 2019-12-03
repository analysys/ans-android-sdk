package com.analysys.demo.allgroTest;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.TabHost;

import com.analysys.demo.AnsApplication;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

/**
 * Description:
 * Author: fengzeyuan
 * Date: 2019-12-03 13:46
 * Version: 1.0
 */
public class TestCase {
    // 所有Hook代码，请移步：../build/intermediates/transforms/Analysys_ASMTransform

    public void testTrackViewClick() {
        View view = new View(AnsApplication.getInstance());
        view.setOnClickListener(v -> {

        });

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        SwitchCompat buttonCompat = new SwitchCompat(AnsApplication.getInstance());
        buttonCompat.setOnCheckedChangeListener((buttonView, isChecked) -> {

        });

        buttonCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            }
        });

        RatingBar bar = new RatingBar(AnsApplication.getInstance());
        bar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {

        });

        bar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {

            }
        });
        SeekBar seekBar = new SeekBar(AnsApplication.getInstance());
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        RadioGroup radioGroup =new RadioGroup(AnsApplication.getInstance());
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            
        });

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

            }
        });
    }

    /**
     * ListView 系列
     */
    public void testTrackListView() {
        ListView listView = new ListView(AnsApplication.getInstance());
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });

        ExpandableListView expListView = new ExpandableListView(AnsApplication.getInstance());
        expListView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        expListView.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> false);

        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                return false;
            }
        });

        expListView.setOnGroupClickListener((parent, v, groupPosition, id) -> false);

        expListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                return false;
            }
        });
    }

    /**
     * dialog 上报
     */
    public void testTrackDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(AnsApplication.getInstance());
        builder.setMultiChoiceItems(null, null, (dialog, which, isChecked) -> {

        });

        builder.setMultiChoiceItems(null, null, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {

            }
        });

        builder.setPositiveButton("确定", (dialog, which) -> {
        });

        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
    }

    /**
     * TabLayout
     */
    public void testTrackTabLayout() {
        TabHost tabHost = new TabHost(AnsApplication.getInstance());

        tabHost.setOnTabChangedListener(tabId -> {

        });

        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {

            }
        });
    }

    /**
     * TabHost
     */
    public void testTrackTabHost() {
        TabLayout tabLayout = new TabLayout(AnsApplication.getInstance());

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    /**
     * Menu
     */
    public void testTrackMenu() {
        Toolbar toolbar = new Toolbar(AnsApplication.getInstance());
        toolbar.setOnMenuItemClickListener(item -> false);

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return false;
            }
        });

        BottomNavigationView bottomNavView = new BottomNavigationView(AnsApplication.getInstance());
        bottomNavView.setOnNavigationItemSelectedListener(menuItem -> false);

        bottomNavView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                return false;
            }
        });
    }


}
