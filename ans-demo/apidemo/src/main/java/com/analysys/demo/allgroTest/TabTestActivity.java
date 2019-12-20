package com.analysys.demo.allgroTest;

import android.app.TabActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TabHost;

import com.analysys.demo.R;


public class TabTestActivity extends TabActivity {//继承的是TabActivity
    /*TabHost的基本用法：
     * 1，在界面布局中定义TabHost组件，并未改组件定义该选项卡的内容
     * 2，继承TabActivity
     * 3，调用TabActivity的getTabHost()方法获取TabHost对象
     * 4，TabHost对象的addTab方法创建，添加选项卡
     * */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabhost);
        //获取该activity里面的TabHost组件
        TabHost tabhost=getTabHost();

        //创建第一个tab页对象，TabSpec代表一个选项卡页面，要设置标题和内容，内容是布局文件中FrameLayout中
        TabHost.TabSpec tab1=tabhost.newTabSpec("tab1");
        tab1.setIndicator("已接电话");//设置标题
        tab1.setContent(R.id.tab01);//设置内容
        //添加tab页
        tabhost.addTab(tab1);

        //创建第二个tab页对象
        TabHost.TabSpec tab2=tabhost.newTabSpec("tab1");
        tab2.setIndicator("已拨电话");//设置标题
        tab2.setContent(R.id.tab02);//设置内容
        //添加tab页
        tabhost.addTab(tab2);

        //创建第三个tab页对象
        TabHost.TabSpec tab3=tabhost.newTabSpec("tab1");
        tab3.setIndicator("未接电话");//设置标题
        tab3.setContent(R.id.tab03);//设置内容
        //添加tab页
        tabhost.addTab(tab3);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.menu_more) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

