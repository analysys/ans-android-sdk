package com.analysys.utils;

import org.junit.Test;

/**
 * @Copyright © 2019 EGuan Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2019-08-14 19:57
 * @Author: Wang-X-C
 */
public class AnsLogTest {
    String value = "测试 Log ";

    @Test
    public void init() {
        ANSLog.init(true, true,
                true, true, true, value);
    }

    @Test
    public void v() {
        ANSLog.init(true, true,
                true, true, true, value);
        ANSLog.v(value);
    }

    @Test
    public void d() {
        ANSLog.init(true, true,
                true, true, true, value);
        ANSLog.d(value);
    }

    @Test
    public void i() {
        ANSLog.init(true, true,
                true, true, true, value);
        ANSLog.i(value);
    }

    @Test
    public void w() {
        ANSLog.init(true, true,
                true, true, true, value);
        ANSLog.w(value);
    }

    @Test
    public void e() {
        ANSLog.init(true, true,
                true, true, true, value);
        ANSLog.e(value);
    }

    @Test
    public void wtf() {
        ANSLog.init(true, true,
                true, true, true, value);
        ANSLog.wtf(value);
    }
}