package com.analysys.visualdemo.activity;

import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.analysys.visualdemo.R;
import com.analysys.visualdemo.VisualViewPagerAdapter;
import com.analysys.visualdemo.VisualViewPagerOneFragment;
import com.analysys.visualdemo.VisualViewPagerThreeFragment;
import com.analysys.visualdemo.VisualViewPagerTwoFragment;

import java.util.ArrayList;
import java.util.List;

public class VisualViewPagerActivity extends FragmentActivity {
    VisualViewPagerOneFragment oneFragment = new VisualViewPagerOneFragment();
    VisualViewPagerTwoFragment twoFragment = new VisualViewPagerTwoFragment();
    VisualViewPagerThreeFragment thrFragment = new VisualViewPagerThreeFragment();
    private ViewPager mPager;//页卡内容
    private ArrayList<Fragment> listViews; //Tab页面列表
    private ImageView cursor;//动画图片
    private TextView t1, t2, t3;//页卡头标
    private int offset = 0;//动画图片偏移量
    private int currIndex = 0;//当前页卡编号
    private int bmpW;//动画图片宽度

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("可视化ViewPager");
        setContentView(R.layout.activity_visual_view_pager);
        InitTextView();
        InitViewPager();
        InitImageView();
    }

    private void InitTextView() {
        t1 = (TextView) findViewById(R.id.text1);
        t2 = (TextView) findViewById(R.id.text2);
        t3 = (TextView) findViewById(R.id.text3);
        t1.setOnClickListener(new MyOnClickListener(0));
        t2.setOnClickListener(new MyOnClickListener(1));
        t3.setOnClickListener(new MyOnClickListener(2));
    }

    /**
     * 初始化ViewPager
     */
    private void InitViewPager() {
        mPager = (ViewPager) findViewById(R.id.vPager);
        listViews = new ArrayList<Fragment>();
/*	LayoutInflater mInflater = getLayoutInflater();
    listViews.add(mInflater.inflate(R.layout.activity_visual_viewpager_one, null));
	listViews.add(mInflater.inflate(R.layout.activity_visual_viewpager_two, null));
	listViews.add(mInflater.inflate(R.layout.activity_visual_viewpager_three, null));*/
        listViews.add(oneFragment);
        listViews.add(twoFragment);
        listViews.add(thrFragment);
        //用support包，只能用getSupportFragmentManager();
        FragmentManager fragmentManager = this.getSupportFragmentManager();
        //通过fragment适配器把fragment添加入viewpager中
        mPager.setAdapter(new VisualViewPagerAdapter(fragmentManager, listViews));
        mPager.setCurrentItem(0);
        mPager.setOnPageChangeListener(new MyOnPageChangeListener());
    }

    ;

    /**
     * 初始化动画
     */
    private void InitImageView() {
        cursor = (ImageView) findViewById(R.id.cursor);
        bmpW = BitmapFactory.decodeResource(getResources(), R.drawable.background_visual).getWidth();//获取图片宽度
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenW = dm.widthPixels;//获取分辨率宽度
        offset = (screenW / 3 - bmpW) / 2;//计算偏移量
        Matrix matrix = new Matrix();
        matrix.postTranslate(offset, 0);
        cursor.setImageMatrix(matrix);//设置动画初始位置
    }

    /**
     * 头标点击监听
     */
    public class MyOnClickListener implements View.OnClickListener {
        private int index = 0;

        public MyOnClickListener(int i) {
            index = i;
        }

        @Override
        public void onClick(View v) {
            mPager.setCurrentItem(index);
        }
    }

    /**
     * ViewPager适配器
     */
    public class MyPagerAdapter extends PagerAdapter {
        public List<View> mListViews;

        public MyPagerAdapter(List<View> mListViews) {
            this.mListViews = mListViews;
        }

        @Override
        public void destroyItem(View arg0, int arg1, Object arg2) {
            ((ViewPager) arg0).removeView(mListViews.get(arg1));
        }

        @Override
        public void finishUpdate(View arg0) {
        }

        @Override
        public int getCount() {
            return mListViews.size();
        }

        @Override
        public Object instantiateItem(View arg0, int arg1) {
            ((ViewPager) arg0).addView(mListViews.get(arg1), 0);
            return mListViews.get(arg1);
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == (arg1);
        }

        @Override
        public void restoreState(Parcelable arg0, ClassLoader arg1) {
        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        @Override
        public void startUpdate(View arg0) {
        }
    }

    /**
     * 页卡切换监听,改变动画位置
     */
    public class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {
        int one, two;

        @Override
        public void onPageSelected(int arg0) {
            one = offset * 2 + bmpW;//页卡1 -> 页卡2 偏移量
            two = one * 2;//页卡1 -> 页卡3 偏移量
            Animation animation = null;
            switch (arg0) {
                case 0:
                    if (currIndex == 1) {
                        animation = new TranslateAnimation(one, 0, 0, 0);
                    } else if (currIndex == 2) {
                        animation = new TranslateAnimation(two, 0, 0, 0);
                    }
                    break;
                case 1:
                    if (currIndex == 0) {
                        animation = new TranslateAnimation(offset, one, 0, 0);
                    } else if (currIndex == 2) {
                        animation = new TranslateAnimation(two, one, 0, 0);
                    }
                    break;
                case 2:
                    if (currIndex == 0) {
                        animation = new TranslateAnimation(offset, two, 0, 0);
                    } else if (currIndex == 1) {
                        animation = new TranslateAnimation(one, two, 0, 0);
                    }
                    break;
            }
            if (animation != null) {
                currIndex = arg0;
                animation.setFillAfter(true);//True:图片停在动画结束位置
                animation.setDuration(300);
                cursor.startAnimation(animation);
            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    }
}
