package com.analysys.demo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.analysys.apidemo.R;

public class InfoDialog extends DialogFragment implements ViewPager.OnPageChangeListener, View.OnClickListener {

    private FragmentPagerAdapter adapter;

    private ViewPager mViewPager;
    private TextView mTVEvent;
    //    private TextView mTVUser;
//    private TextView mTVProperty;
    private String mEventName;

    public InfoDialog(String eventName) {
        mEventName = eventName;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        adapter = new FragmentPagerAdapter(getChildFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                switch (position) {
                    case 0:
                        return new FragmentEvent(mEventName);
//                    case 1:
//                        return new FragmentUser();
//                    case 2:
//                        return new FragmentProperty();
                    default:
                        return null;
                }
            }

            @Override
            public int getCount() {
                return 1;
            }
        };

        return inflater.inflate(R.layout.dialog_info, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mTVEvent = view.findViewById(R.id.tv_event);
        mTVEvent.setOnClickListener(this);
//        mTVUser = view.findViewById(R.id.tv_user);
//        mTVUser.setOnClickListener(this);
//        mTVProperty = view.findViewById(R.id.tv_property);
//        mTVProperty.setOnClickListener(this);
        mViewPager = view.findViewById(R.id.view_pager);
        mViewPager.setAdapter(adapter);
        mViewPager.addOnPageChangeListener(this);
        mTVEvent.setSelected(true);
//        mTVUser.setSelected(false);
//        mTVProperty.setSelected(false);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        mTVEvent.setSelected(position == 0);
//        mTVUser.setSelected(position == 1);
//        mTVProperty.setSelected(position == 2);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.tv_event) {
            mViewPager.setCurrentItem(0, true);
        }
//        else if (id == R.id.tv_user) {
//            mViewPager.setCurrentItem(1, true);
//        } else if (id == R.id.tv_property) {
//            mViewPager.setCurrentItem(2, true);
//        }
    }
}