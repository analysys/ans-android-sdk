package com.analysys.demo;



import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;


import com.analysys.ANSAutoPageTracker;
import com.analysys.AnalysysAgent;
import com.analysys.apidemo.R;

import java.util.HashMap;
import java.util.Map;

/**
 * @Copyright © 2020 Analysys Inc. All rights reserved.
 * @Description:
 * @Create: 2020/7/20 7:00 PM
 * @author: huchangqing
 */
public class MainActivity extends BaseActivity implements ViewPager.OnPageChangeListener {

    private FragmentMini mFragmentMini = new FragmentMini();
    private FragmentFull mFragmentFull = new FragmentFull();


    private FragmentPagerAdapter adapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return mFragmentMini;
                case 1:
                    return mFragmentFull;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    };

    private ViewPager mViewPager;
    private TextView mTVMini;
    private TextView mTVFull;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mViewPager = findViewById(R.id.view_pager);
        mTVMini = findViewById(R.id.tv_mini);
        mTVFull = findViewById(R.id.tv_full);
        mViewPager.setAdapter(adapter);
        mViewPager.addOnPageChangeListener(this);
        mTVMini.setSelected(true);
        mTVFull.setSelected(false);
//        ActionBar actionBar = getSupportActionBar();
//        if (actionBar != null) {
//            actionBar.setDisplayShowTitleEnabled(true);
//            actionBar.setTitle("易观方舟Demo");
//        }
//        AnalysysAgent.track(this,"homepage");
    }



    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        mTVMini.setSelected(position == 0);
        mTVFull.setSelected(position == 1);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem item = menu.findItem(R.id.action_modify);
        SpannableString spannableString = new SpannableString(item.getTitle());
        spannableString.setSpan(new ForegroundColorSpan(0xff2222ee), 0, spannableString.length(), 0);
        item.setTitle(spannableString);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_modify) {
            Intent intent = new Intent(this, ModifyConfigActivity.class);
            intent.putExtra(BaseActivity.EXTRA_ACTIVITY_TITLE, "修改配置");
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    public void onTabClick(View view) {
        int id = view.getId();
        if (id == R.id.tv_mini) {
            mViewPager.setCurrentItem(0, true);
        } else if (id == R.id.tv_full) {
            mViewPager.setCurrentItem(1, true);
        }
    }
}
