package com.analysys.utils;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @Copyright © 2019 EGuan Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2019-08-15 10:39
 * @Author: Wang-X-C
 */
public class CheckUtilsTest {

//    @Test
//    public void checkEvent() throws Throwable {
//        String value = "[{\"appid\":\"heatmappushtest\"," +
//                "\"xwho\":\"9545f294-3b6d-47d7-9b59-4a50ccb86b4a\",\"xwhat\":\"$end\"," +
//                "\"xwhen\":1565837528090,\"xcontext\":{\"$network\":\"WIFI\"," +
//                "\"$os_version\":\"Android 9\",\"$is_first_day\":false,\"$model\":\"MIX 2S\"," +
//                "\"$os\":\"Android\",\"$screen_width\":1080,\"$channel\":\"wandoujia\"," +
//                "\"$platform\":\"Android\",\"$brand\":\"Xiaomi\",\"$screen_height\":2030," +
//                "\"$language\":\"zh\",\"$app_version\":\"1.0\",\"$lib\":\"Android\",\"$debug\":1," +
//                "\"$is_time_calibrated\":true,\"$is_login\":false,\"$lib_version\":\"4.3.5.3\"," +
//                "\"$duration\":22279,\"$time_zone\":\"GMT+08:00\",\"$manufacturer\":\"Xiaomi\"," +
//                "\"$session_id\":\"9e084fc170696bb7\"}}]";
//        JSONArray jar = new JSONArray(value);
//        CheckUtils.checkEvent(jar);
//    }

    @Test
    public void checkIdLength() {
        String value = null;
        for (int i = 0; i < 300; i++) {
            value += i;
        }
        String value2 = "idTest";
        Assert.assertFalse(CheckUtils.checkIdLength(value));
        Assert.assertTrue(CheckUtils.checkIdLength(value2));
    }

    @Test
    public void checkOriginalIdLength() {
        String value = null;
        for (int i = 0; i < 300; i++) {
            value += i;
        }
        Assert.assertFalse(CheckUtils.checkOriginalIdLength(value));

        String value2 = "idTest";
        Assert.assertTrue(CheckUtils.checkOriginalIdLength(value2));
    }

    @Test
    public void checkToMap() {

        Map<String, Object> map = new HashMap<String, Object>();
        String key = "testKey";
        String value = "testValue";
        CheckUtils.checkToMap(map, key, value);
    }

    @Test
    public void checkParameter() {
        String apiName = "track";
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("aaa", "AAA");
        map.put(null, "BBB");
        Assert.assertTrue(CheckUtils.checkParameter(apiName, map));
    }

    @Test
    public void checkTrackEventName() {
        String apiName = "track";
        String eventInfo = "trackEventInfo";
        Assert.assertTrue(CheckUtils.checkTrackEventName(apiName,eventInfo));
    }
}