package com.analysys.utils;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @Copyright © 2019 EGuan Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2019-08-15 14:07
 * @Author: Wang-X-C
 */
public class InternalAgentTest {
    private Context mContext = AnalysysUtil.getContext();

    @Test
    public void getSourceDetail() {
        Assert.assertNotNull(InternalAgent.getSourceDetail(mContext));
    }

    @Test
    public void getChannel() {
        InternalAgent.getChannel(mContext);
    }

    @Test
    public void getAppId() {
        InternalAgent.getAppId(mContext);
    }

    @Test
    public void getUserId() {
        Assert.assertNotNull(InternalAgent.getUserId(mContext));
    }

    @Test
    public void getCurrentTime() {
        Assert.assertNotNull(InternalAgent.getCurrentTime(mContext));
    }

    @Test
    public void isCalibrated() {
        Assert.assertNotNull(InternalAgent.isCalibrated(mContext));
    }

    @Test
    public void getTimeZone() {
        Assert.assertNotNull(InternalAgent.getTimeZone(mContext));
    }

    @Test
    public void getManufacturer() {
        Assert.assertNotNull(InternalAgent.getManufacturer(mContext));
    }

    @Test
    public void getVersionName() {
//        Assert.assertNull(InternalAgent.getVersionName(mContext));
    }

    @Test
    public void getDeviceModel() {
        Assert.assertNotNull(InternalAgent.getDeviceModel(mContext));
    }

    @Test
    public void getOSVersion() {
        Assert.assertNotNull(InternalAgent.getOSVersion(mContext));
    }

    @Test
    public void getNetwork() {
        Assert.assertNotNull(InternalAgent.getNetwork(mContext));
    }

    @Test
    public void getCarrierName() {
        Assert.assertNull(InternalAgent.getCarrierName(mContext));
    }

    @Test
    public void getScreenWidth() {
        Assert.assertNotNull(InternalAgent.getScreenWidth(mContext));
    }

    @Test
    public void getScreenHeight() {
        Assert.assertNotNull(InternalAgent.getScreenHeight(mContext));
    }

    @Test
    public void getBrand() {
        Assert.assertNotNull(InternalAgent.getBrand(mContext));
    }

    @Test
    public void getDeviceLanguage() {
        Assert.assertNotNull(InternalAgent.getDeviceLanguage(mContext));
    }

    @Test
    public void isFirstDay() {
        Assert.assertNotNull(InternalAgent.isFirstDay(mContext));
    }

    @Test
    public void getSessionId() {
//        Assert.assertNull(InternalAgent.getSessionId(mContext));
    }

    @Test
    public void isFirstTime() {
        Assert.assertNotNull(InternalAgent.isFirstTime(mContext));
    }

    @Test
    public void getDebugMode() {
        Assert.assertNotNull(InternalAgent.getDebugMode(mContext));
    }

    @Test
    public void getLibVersion() {
        Assert.assertNotNull(InternalAgent.getLibVersion(mContext));
    }

    @Test
    public void getLogin() {
        Assert.assertNotNull(InternalAgent.getLogin(mContext));
    }

    @Test
    public void getIMEI() {
        Assert.assertNotNull(InternalAgent.getIMEI(mContext));
    }

    @Test
    public void getMac() {
        Assert.assertNotNull(InternalAgent.getMac(mContext));
    }

    @Test
    public void getOriginalId() {
        InternalAgent.getOriginalId(mContext);
    }

    @Test
    public void isEmpty() {
        String value = "";
        Assert.assertTrue(InternalAgent.isEmpty(value));

        String value2 = "test";
        Assert.assertFalse(InternalAgent.isEmpty(value2));

        String value3 = null;
        Assert.assertTrue(InternalAgent.isEmpty(value3));

        List<String> list = new ArrayList<>();
        Assert.assertTrue(InternalAgent.isEmpty(list));

        JSONObject job = new JSONObject();
        Assert.assertTrue(InternalAgent.isEmpty(job));

        JSONArray jar = new JSONArray();
        Assert.assertTrue(InternalAgent.isEmpty(jar));
    }

    @Test
    public void networkType() {
        Assert.assertNotNull(InternalAgent.networkType(mContext));
    }

    @Test
    public void isNetworkAvailable() {
        Assert.assertNotNull(InternalAgent.isNetworkAvailable(mContext));
    }

    @Test
    public void checkPermission() {
        Assert.assertFalse(InternalAgent.checkPermission(mContext, ""));
    }

    @Test
    public void checkClass() {
        Assert.assertTrue(InternalAgent.checkClass("com.analysys.utils", "InternalAgent"));
    }

    @Test
    public void checkUrl() {
//        Assert.assertNotNull(InternalAgent.checkUrl(""));
    }

    @Test
    public void checkEventName() {
        InternalAgent.checkEventName("track");
    }

    @Test
    public void checkKey() {
        InternalAgent.checkKey("key");
    }

    @Test
    public void checkValue() {
        InternalAgent.checkValue("value");
    }

    @Test
    public void setString() {
        InternalAgent.setString(mContext, "testKey", "testValue");
    }

    @Test
    public void getString() {
        InternalAgent.getString(mContext, "testKey", "testValue");
    }

    @Test
    public void setFloat() {
        InternalAgent.setFloat(mContext, "testKey", 0.2f);
    }

    @Test
    public void getFloat() {
        InternalAgent.getFloat(mContext, "testKey", 0.2f);
    }

    @Test
    public void i() {
    }

    @Test
    public void e() {
    }

    @Test
    public void w() {
    }

    @Test
    public void d() {
    }

    @Test
    public void v() {
    }

    @Test
    public void getNetExecutor() {
        Assert.assertNotNull(InternalAgent.getNetExecutor());
    }

    @Test
    public void createSSL() {
        Assert.assertNotNull(InternalAgent.createSSL(mContext));
    }
}