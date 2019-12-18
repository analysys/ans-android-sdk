package com.analysys.utils;

import android.Manifest;
import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Copyright © 2019 EGuan Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2019-08-14 10:51
 * @Author: Wang-X-C
 */
public class CommonUtilsTest {
    Context mContext = AnalysysUtil.getContext();

    @Test
    public void getManifestData() {
        Assert.assertNull(CommonUtils.getManifestData(mContext, "ANALYSYS_APPKEY"));
    }

    @Test
    public void getTime() {
        Assert.assertNotNull(CommonUtils.getTime());
    }

    @Test
    public void stringToSet() {
        String value1 = "111$$222$$333";
        Set<String> set = new HashSet<String>();
        set.add("111");
        set.add("222");
        set.add("333");
        Assert.assertEquals(set, CommonUtils.toSet(value1));

        String value2 = "111";
        Set<String> set2 = new HashSet<>();
        set2.add(value2);
        Assert.assertEquals(set2, CommonUtils.toSet(value2));
    }

    @Test
    public void setToString() {
        String value = "333$$222";
        Set<String> set = new HashSet<String>();
        set.add("222");
        set.add("333");
        CommonUtils.toString(set);
    }

    @Test
    public void stringToList() {
        String value = "111$$222$$333";
        List<String> list = new ArrayList<String>();
        list.add("111");
        list.add("222");
        list.add("333");
        Assert.assertEquals(list, CommonUtils.toList(value));

        String value2 = "111";
        List<String> list2 = new ArrayList<>();
        list2.add(value2);
        Assert.assertEquals(list2, CommonUtils.toList(value2));
    }

    @Test
    public void listToString() {
        String value = "111$$222$$333";
        List<String> list = new ArrayList<String>();
        list.add("111");
        list.add("222");
        list.add("333");
        Assert.assertEquals(value, CommonUtils.toString(list));
    }

    @Test
    public void jsonToMap() throws Throwable {
        JSONObject job = new JSONObject();
        job.put("11", "1111");
        job.put("22", "2222");
        job.put("33", "3333");

        Map<String, Object> map = new HashMap<>();
        map.put("11", "1111");
        map.put("22", "2222");
        map.put("33", "3333");

        Assert.assertEquals(map, CommonUtils.jsonToMap(job));
    }

    @Test
    public void messageZip() throws Throwable {
        String value = "测试压缩";
        String zipValue = CommonUtils.messageZip(value);
        Assert.assertEquals(value, CommonUtils.messageUnzip(zipValue));

        CommonUtils.messageZip("");
        Assert.assertNull(CommonUtils.messageUnzip(null));
    }

    @Test
    public void getSpvInfo() {
        Assert.assertNotNull(CommonUtils.getSpvInfo(mContext));
    }

    @Test
    public void isFirstStart() {
        Assert.assertNotNull(CommonUtils.isFirstStart(mContext));
        Assert.assertFalse(CommonUtils.isFirstStart(null));
    }

    @Test
    public void getFirstStartTime() {
        Assert.assertNotNull(CommonUtils.getFirstStartTime(mContext));
    }

    @Test
    public void getDay() {
        Assert.assertNotNull(CommonUtils.getDay());
    }

    @Test
    public void checkUrl() {
        String url = "https://arkpaastest.analysys.cn:4089/";
        String baseUrl = "https://arkpaastest.analysys.cn:4089";
        Assert.assertEquals(baseUrl, CommonUtils.checkUrl(url));
    }

    @Test
    public void isEmpty() {
        Assert.assertTrue(CommonUtils.isEmpty(null));
        Assert.assertTrue(CommonUtils.isEmpty(""));
        Assert.assertTrue(CommonUtils.isEmpty("    "));
        Assert.assertTrue(CommonUtils.isEmpty(new JSONObject()));
        Assert.assertTrue(CommonUtils.isEmpty(new JSONArray()));
        Assert.assertTrue(CommonUtils.isEmpty(new HashMap<>()));
        Assert.assertTrue(CommonUtils.isEmpty(new ArrayList<>()));
        Assert.assertTrue(CommonUtils.isEmpty(new HashSet<>()));
    }

    @Test
    public void isMainProcess() {
        Assert.assertTrue(CommonUtils.isMainProcess(mContext));
        Assert.assertFalse(CommonUtils.isMainProcess(null));
    }

    @Test
    public void networkType() {
        Assert.assertNotNull(CommonUtils.networkType(mContext));
    }

    @Test
    public void checkPermission() {
        Assert.assertTrue(CommonUtils.checkPermission(mContext,
                Manifest.permission.ACCESS_WIFI_STATE));
        Assert.assertTrue(CommonUtils.checkPermission(mContext,
                Manifest.permission.ACCESS_NETWORK_STATE));
    }

    @Test
    public void getAppKey() {
        CommonUtils.getAppKey(mContext);
    }

    @Test
    public void isNetworkAvailable() {
        Assert.assertTrue(CommonUtils.isNetworkAvailable(mContext));
    }

    @Test
    public void mergeJson() throws Throwable {
        JSONObject job = new JSONObject();
        job.put("aa", "AAA");

        JSONObject job2 = new JSONObject();
        job2.put("bb", "BBB");

        JSONObject job3 = new JSONObject();
        job3.put("aa", "AAA");
        job3.put("bb", "BBB");

        CommonUtils.mergeJson(job, job2);
    }

    @Test
    public void readStream() {
    }

    @Test
    public void reflexUtils() {
    }

    @Test
    public void reflexUtils1() {
    }

    @Test
    public void getDistinctId() {
//        Assert.assertNull(CommonUtils.getDistinctId(mContext));
    }

    @Test
    public void pushToJSON() throws Throwable {
        JSONObject job = new JSONObject();
        job.put("aa", "AAA");

        String key = "aa";
        String value = "AAA";
        CommonUtils.pushToJSON(job, key, value);

        CommonUtils.pushToJSON(job, "bb", null);

        String key2 = "cc";
        Object value2 = 1;
        CommonUtils.pushToJSON(job, key2, value2);

        String key3 = "dd";
        String value3 = "DDD";
        CommonUtils.pushToJSON(job, key3, value3);

    }

    @Test
    public void getUrl() {
        Assert.assertNotNull(CommonUtils.getUrl(mContext));
    }

    @Test
    public void timeCalibration() {
    }

    @Test
    public void getMould() {
        String fileName = "LifeCycleConfig.json";
        Assert.assertNotNull(CommonUtils.getMould(mContext, fileName));
    }

    @Test
    public void checkClass() {
        String pkgName = "com.analysys.utils";
        String className = "CommonUtils";
        Assert.assertTrue(CommonUtils.checkClass(pkgName, className));
    }

    @Test
    public void setIdFile() {
        String key = "testKey";
        String value = "testValue";
        CommonUtils.setIdFile(mContext, key, value);
    }

    @Test
    public void getIdFile() {
        String key = "testKey";
        Assert.assertNotNull(CommonUtils.getIdFile(mContext, key));
    }

    @Test
    public void writeCount() {
    }

    @Test
    public void readCount() {
    }

    @Test
    public void resetCount() {
    }

    @Test
    public void getClassPath() {
    }

    @Test
    public void getMethod() {
    }

    @Test
    public void getChannel() {
        Assert.assertNotNull(CommonUtils.getChannel(mContext));
    }

    @Test
    public void getUserId() {
        Assert.assertNotNull(CommonUtils.getUserId(mContext));
    }

    @Test
    public void getVersionName() {
//        Assert.assertNull(CommonUtils.getVersionName(mContext));
    }

    @Test
    public void getCarrierName() {
        Assert.assertNull(CommonUtils.getCarrierName(mContext));
    }

    @Test
    public void getScreenWidth() {
        Assert.assertNotNull(CommonUtils.getScreenWidth(mContext));
    }

    @Test
    public void getScreenHeight() {
        Assert.assertNotNull(CommonUtils.getScreenHeight(mContext));
    }

    @Test
    public void isFirstDay() {
        CommonUtils.isFirstDay(mContext);
    }

    @Test
    public void getDebugMode() {
        Assert.assertEquals(2, CommonUtils.getDebugMode(mContext));
    }

    @Test
    public void getLogin() {
        Assert.assertFalse(CommonUtils.getLogin(mContext));
    }

    @Test
    public void getIMEI() {
        Assert.assertNotNull(CommonUtils.getIMEI(mContext));
    }

    @Test
    public void getMac() {
        Assert.assertNotNull(CommonUtils.getMac(mContext));
    }

    @Test
    public void getOriginalId() {
        Assert.assertNotNull(CommonUtils.getOriginalId(mContext));
    }

    @Test
    public void dbEncrypt() {
        String value = "测试本地数据加密";
        String encrypt = CommonUtils.dbEncrypt(value);
        Assert.assertEquals(value, CommonUtils.dbDecrypt(encrypt));
    }

    @Test
    public void getApplication() {
        Assert.assertNotNull(CommonUtils.getApplication());
    }

    @Test
    public void clearEmptyValue() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("aa", "AA");
        map.put("", "BB");
        map.put(null, "CC");
        map.put("dd", "");
        map.put("ee", null);
        CommonUtils.clearEmptyValue(map);
    }

    @Test
    public void deepCopy() {
        Map<String, Object> map = new HashMap<>();
        map.put("aa", "AA");
        map.put("bb", "BB");
        Assert.assertEquals(map, CommonUtils.deepCopy(map));
    }

    @Test
    public void getSSLSocketFactory() {
        Assert.assertNotNull(CommonUtils.getSSLSocketFactory(mContext));
    }
}