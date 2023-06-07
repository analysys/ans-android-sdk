package com.analysys.demo;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.analysys.apidemo.R;

import java.util.HashMap;
import java.util.Map;

public class VisualSibViewPagerActivity extends BaseActivity {

    class MyPagerAdapter extends PagerAdapter {

        Map<Integer, View> views = new HashMap<>();

        private final int[] BG_COLOR = {0xffaabbcc, 0xffbbaacc, 0xffccaabb, 0xffaaccbb, 0xffbbccaa};

        @Override
        public int getCount() {
            return BG_COLOR.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View view = views.get(position);
            if (view == null) {
                view = getLayoutInflater().inflate(R.layout.view_pager_layout, null, false);
                view.setBackgroundColor(BG_COLOR[position]);
                Button btn = view.findViewById(R.id.btn_vp);
                btn.setText("ViewPager Button " + position);
                views.put(position, view);
            }
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view = views.get(position);
            views.remove(position);
            container.removeView(view);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visual_sib_view_pager);
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(new MyPagerAdapter());
    }
}
