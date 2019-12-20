package com.analysys.process;

import android.content.Context;

import androidx.test.InstrumentationRegistry;

import org.junit.Test;

/**
 * @Copyright © 2019 EGuan Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2019-08-16 10:24
 * @Author: Wang-X-C
 */
public class HeatMapTest {
    Context mContext = InstrumentationRegistry.getContext();

    @Test
    public void getInstance() {
        HeatMap.getInstance(mContext);
    }

    @Test
    public void initPageInfo() {
//        HeatMap.getInstance(mContext).initPageInfo(new TestActivity());
    }

    @Test
    public void hookDecorViewClick() throws Throwable {
//        Activity activity = new TestActivity();
//        View view = activity.getWindow().getDecorView();
//        HeatMap.getInstance(mContext).hookDecorViewClick(view);
    }
}