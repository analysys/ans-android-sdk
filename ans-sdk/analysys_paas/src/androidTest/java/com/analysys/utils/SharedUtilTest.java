package com.analysys.utils;

import android.content.Context;

import androidx.test.InstrumentationRegistry;

import org.junit.Assert;
import org.junit.Test;

/**
 * @Copyright © 2019 EGuan Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2019-08-13 15:42
 * @Author: Wang-X-C
 */
public class SharedUtilTest {

    @Test
    public void getBoolean() {
        String key = "TestBoolean";
        Context context = InstrumentationRegistry.getContext();
        SharedUtil.setBoolean(context, key, true);
        Assert.assertEquals(true, SharedUtil.getBoolean(context, key, false));
    }

    @Test
    public void getFloat() {
        String key = "TestFloat";
        float value =  12.3f;
        Context context = InstrumentationRegistry.getContext();
        SharedUtil.setFloat(context, key, value);
        Assert.assertEquals(value, SharedUtil.getFloat(context, key, 1.0F),0.1f);
    }

    @Test
    public void getInt() {
        String key = "TestInt";
        Context context = InstrumentationRegistry.getContext();
        SharedUtil.setInt(context, key, 12);
        Assert.assertEquals(12, SharedUtil.getInt(context, key, 10));
    }

    @Test
    public void getLong() {
        String key = "TestLong";
        Context context = InstrumentationRegistry.getContext();
        SharedUtil.setLong(context, key, 12L);
        Assert.assertEquals(12L, SharedUtil.getLong(context, key, 10L));
    }

    @Test
    public void getString() {
        String key = "TestString";
        Context context = InstrumentationRegistry.getContext();
        SharedUtil.setString(context, key, "UnitTest");
        Assert.assertEquals("UnitTest", SharedUtil.getString(context, key, "failed"));
    }

    @Test
    public void remove() {
        String key = "TestString";
        String value = "UnitTest";
        String defaultValue = "defaultValue";
        Context context = InstrumentationRegistry.getContext();
        SharedUtil.setString(context, key, value);
        Assert.assertEquals(value, SharedUtil.getString(context, key, defaultValue));
        SharedUtil.remove(context,key);
        Assert.assertEquals(defaultValue, SharedUtil.getString(context, key, defaultValue));
  }
}