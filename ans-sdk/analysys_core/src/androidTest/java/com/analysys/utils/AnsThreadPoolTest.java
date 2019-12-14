package com.analysys.utils;

import org.junit.Assert;
import org.junit.Test;

/**
 * @Copyright © 2019 EGuan Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2019-08-14 20:05
 * @Author: Wang-X-C
 */
public class AnsThreadPoolTest {

    @Test
    public void getNetExecutor() {
        Assert.assertNotNull(ANSThreadPool.getNetExecutor());
    }

    @Test
    public void getDBExecutor() {
        Assert.assertNotNull(ANSThreadPool.getDBExecutor());
    }

    @Test
    public void shoutdown() {
        ANSThreadPool.shoutdown();
    }

    @Test
    public void pushDB() {
        ANSThreadPool.pushDB(null);
        ANSThreadPool.pushDB(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    @Test
    public void pushNet() {
        ANSThreadPool.pushNet(null);
        ANSThreadPool.pushNet(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    @Test
    public void execute() {
        ANSThreadPool.execute(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    @Test
    public void waitForAsyncTask() {
        ANSThreadPool.waitForAsyncTask();
    }

    @Test
    public void heatMapExecute() {
        ANSThreadPool.heatMapExecute(new Runnable() {
            @Override
            public void run() {

            }
        });
    }
}