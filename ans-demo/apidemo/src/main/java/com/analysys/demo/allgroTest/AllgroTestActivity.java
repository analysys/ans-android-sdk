package com.analysys.demo.allgroTest;

import android.os.Bundle;
import android.view.View;

import com.analysys.demo.R;
import com.analysys.demo.allgroTest.fragment.FragmentOne;
import com.analysys.demo.allgroTest.fragment.FragmentThree;
import com.analysys.demo.allgroTest.fragment.FragmentTwo;
import com.analysys.demo.databinding.ActivityTestAllgroBinding;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

/**
 * @author fengzeyuan
 */
public class AllgroTestActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener, View.OnClickListener {

    private ActivityTestAllgroBinding mDataBinding;


    //参数列表需要获取一个Fragment管理器 getSupportFragmentManager
    private FragmentPagerAdapter adapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
        //通过当前的position初始化Fragment。将Fragment填充到ViewPager中
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new FragmentOne();
                case 1:
                    return new FragmentTwo();
                case 2:
                    return new FragmentThree();
                default:
                    throw new RuntimeException("数据异常");//最后这里需要抛一个运行时异常runException
            }
        }

        @Override
        public int getCount() {
            return 3;//这里返回的值是我们已知的Fragment数量
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_test_allgro);

        mDataBinding.viewPager.setAdapter(adapter);//viewPager设置上适配器
        mDataBinding.viewPager.addOnPageChangeListener(this);//给ViewPager设置滑动监听，目的是为了显示Fragment
        mDataBinding.btn1.setSelected(true);//首次进入默认选中第一个
        //下方btn设置监听，点击时切换页面
        mDataBinding.btn0.setOnClickListener(this);
        mDataBinding.btn1.setOnClickListener(this);
        mDataBinding.btn2.setOnClickListener(this);
        mDataBinding.btn3.setOnClickListener(this);

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
        //当选择Fragment时，同步设置下方按钮的旋转状态
        mDataBinding.btn1.setSelected(position == 0);
        mDataBinding.btn2.setSelected(position == 1);
        mDataBinding.btn3.setSelected(position == 2);
    }

    /**
     * 页面滑动状态变化时的监听
     */
    @Override
    public void onPageScrollStateChanged(int state) {

    }

    /**
     * 点击btn设置ViewPager当前视图
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn1:
                //参数一是ViewPager的position,参数二为是否有滑动效果
                mDataBinding.viewPager.setCurrentItem(0, true);
                break;
            case R.id.btn2:
                mDataBinding.viewPager.setCurrentItem(1, true);
                break;
            case R.id.btn3:
                mDataBinding.viewPager.setCurrentItem(2, true);
                break;
            default:
                break;
        }
    }

    public void onViewClick(View view) {
        mDataBinding.viewPager.setCurrentItem(2, true);
    }

}
