package com.analysys.demo;

import android.os.Bundle;
import android.view.View;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.analysys.apidemo.R;
import com.analysys.apidemo.databinding.ActivityProbeFragmentTestBinding;

public class ProbeFragmentTestActivity extends BaseActivity implements ViewPager.OnPageChangeListener {

    private ActivityProbeFragmentTestBinding mDataBinding;

    private FragmentPagerAdapter adapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new FragmentProbeOne();
                case 1:
                    return new FragmentProbeTwo();
                default:
                    throw new RuntimeException("数据异常");
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_probe_fragment_test);
        if (mDataBinding == null) {
            finish();
            return;
        }

        mDataBinding.viewPager.setAdapter(adapter);
        mDataBinding.viewPager.addOnPageChangeListener(this);
        mDataBinding.btn1.setSelected(true);
        mDataBinding.setPageInfo(this);
    }


    /**
     * 页面滑动时的监听
     */
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    /**
     * 页面选择时的监听
     */
    @Override
    public void onPageSelected(int position) {
        mDataBinding.btn1.setSelected(position == 0);
        mDataBinding.btn2.setSelected(position == 1);
    }

    /**
     * 页面滑动状态变化时的监听
     */
    @Override
    public void onPageScrollStateChanged(int state) {
    }

    public void onViewClick(View view) {
        int id = view.getId();
        if (id == R.id.btn1) {
            mDataBinding.viewPager.setCurrentItem(0, true);
        } else if (id == R.id.btn2) {
            mDataBinding.viewPager.setCurrentItem(1, true);
        }
    }
}
