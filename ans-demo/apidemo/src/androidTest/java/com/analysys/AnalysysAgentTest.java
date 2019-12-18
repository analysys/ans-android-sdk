package com.analysys;

import android.content.Context;

import com.analysys.push.PushListener;
import com.analysys.utils.AnalysysUtil;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Copyright © 2019 EGuan Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2019-08-15 14:54
 * @Author: Wang-X-C
 */
public class AnalysysAgentTest {
    Context mContext = AnalysysUtil.getContext();

    @Test
    public void init() {
        AnalysysConfig config = new AnalysysConfig();
        config.setAppKey("test-key");
        config.setAutoInstallation(true);
        config.setAutoProfile(true);
        config.setChannel("testChannel");
        config.setBaseUrl("arksdktest.analysys.cn");
        config.setEncryptType(EncryptEnum.AES);
        AnalysysAgent.init(mContext, config);
    }

    @Test
    public void setAutoHeatMap() {
        AnalysysAgent.setAutoHeatMap(true);
    }

    @Test
    public void setDebugMode() {
        AnalysysAgent.setDebugMode(mContext, 0);
        AnalysysAgent.setDebugMode(mContext, 1);
        AnalysysAgent.setDebugMode(mContext, 2);
    }

    @Test
    public void setUploadURL() {
        String url = "https://arksdktest.analysys.cn:4069";
        String url2 = "http://arksdktest.analysys.cn:8089";
        String url3 = "wss://arksdktest.analysys.cn:4069";
        AnalysysAgent.setUploadURL(mContext, url);
        AnalysysAgent.setUploadURL(mContext, url2);
        AnalysysAgent.setUploadURL(mContext, url3);
        AnalysysAgent.setUploadURL(mContext, null);
    }

    @Test
    public void setIntervalTime() {
        AnalysysAgent.setIntervalTime(mContext, 1000);
        AnalysysAgent.setIntervalTime(mContext, 0);
    }

    @Test
    public void setMaxCacheSize() {
        AnalysysAgent.setMaxCacheSize(mContext, 200);
        AnalysysAgent.setMaxCacheSize(mContext, 0);
    }

    @Test
    public void getMaxCacheSize() {
        Assert.assertEquals(10000, AnalysysAgent.getMaxCacheSize(mContext));
    }

    @Test
    public void setMaxEventSize() {
        AnalysysAgent.setMaxEventSize(mContext, 0);
        AnalysysAgent.setMaxEventSize(mContext, 100);
    }

    @Test
    public void flush() {
        AnalysysAgent.flush(mContext);
    }

    @Test
    public void alias() {
        String alias = "aliasId";
        String original = "originalId";
        AnalysysAgent.alias(mContext, alias, original);
        AnalysysAgent.alias(mContext, null, original);
        AnalysysAgent.alias(mContext, alias, null);
        AnalysysAgent.alias(mContext, null, null);
    }

    @Test
    public void identify() {
        String distinctId = "distinctId";
        AnalysysAgent.identify(mContext, distinctId);
        AnalysysAgent.identify(mContext, null);
    }

    @Test
    public void getDistinctId() {
        Assert.assertNull(AnalysysAgent.getDistinctId(mContext));
    }

    @Test
    public void reset() {
        AnalysysAgent.reset(mContext);
    }

    @Test
    public void track() {
        AnalysysAgent.track(mContext, null);
        AnalysysAgent.track(mContext, "trackTest");
    }

    @Test
    public void track1() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("aa", "AAA");
        map.put("bb", "BBB");
        map.put("cc", "CCC");
        AnalysysAgent.track(mContext, "trackTest", map);
    }

    @Test
    public void pageView() {
        AnalysysAgent.pageView(mContext, null);
        AnalysysAgent.pageView(mContext, "pageViewTest");
    }

    @Test
    public void pageView1() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("aa", "AAA");
        map.put("bb", "BBB");
        map.put("cc", "CCC");
        AnalysysAgent.pageView(mContext, "pageViewTest", map);
    }

    @Test
    public void registerSuperProperty() {
        String key = "testKey";
        String value = "testValue";
        AnalysysAgent.registerSuperProperty(mContext, key, value);
        AnalysysAgent.registerSuperProperty(mContext, null, value);
        AnalysysAgent.registerSuperProperty(mContext, key, null);
    }

    @Test
    public void registerSuperProperties() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("aa", "AAA");
        map.put("bb", "BBB");
        map.put("cc", "CCC");
        AnalysysAgent.registerSuperProperties(mContext, map);
        AnalysysAgent.registerSuperProperties(mContext, null);
    }

    @Test
    public void unRegisterSuperProperty() {
        String value = "testSuperProperty";
        AnalysysAgent.unRegisterSuperProperty(mContext, value);
        AnalysysAgent.unRegisterSuperProperty(mContext, null);
    }

    @Test
    public void clearSuperProperties() {
        AnalysysAgent.clearSuperProperties(mContext);
    }

    @Test
    public void getSuperProperty() {
        Assert.assertNull(AnalysysAgent.getSuperProperty(mContext, "aa"));
    }

    @Test
    public void getSuperProperties() {
        Assert.assertNotNull(AnalysysAgent.getSuperProperties(mContext));
    }

    @Test
    public void profileSet() {
        String key = "testKey";
        String value = "testValue";
        AnalysysAgent.profileSet(mContext, key, value);
        AnalysysAgent.profileSet(mContext, null, value);
        AnalysysAgent.profileSet(mContext, key, null);
    }

    @Test
    public void profileSet1() {
        Map<String, Object> map = new HashMap<>();
        map.put("aaa", "AAA");
        map.put("", "BBB");
        map.put("   ", "CCC");
        map.put(null, "CCC");
        map.put("eee", "");
        map.put("fff", "  ");
        map.put("ggg", null);
        AnalysysAgent.profileSet(mContext, map);
    }

    @Test
    public void profileSetOnce() {
        String key = "testKey";
        String value = "testValue";
        AnalysysAgent.profileSetOnce(mContext, key, value);
        AnalysysAgent.profileSetOnce(mContext, null, value);
        AnalysysAgent.profileSetOnce(mContext, key, null);
    }

    @Test
    public void profileSetOnce1() {
        Map<String, Object> map = new HashMap<>();
        map.put("aaa", "AAA");
        map.put("", "BBB");
        map.put("   ", "CCC");
        map.put(null, "CCC");
        map.put("eee", "");
        map.put("fff", "  ");
        map.put("ggg", null);
        AnalysysAgent.profileSetOnce(mContext, map);
    }

    @Test
    public void profileIncrement() {
        String key = "testKey";
        int value = 12;
        AnalysysAgent.profileIncrement(mContext, key, value);
        AnalysysAgent.profileIncrement(mContext, null, value);
    }

    @Test
    public void profileIncrement1() {
        Map<String, Number> map = new HashMap<String, Number>();
        map.put("aaa", 12);
        map.put("aaa", 12.0f);
        map.put("aaa", 12.0D);
        AnalysysAgent.profileIncrement(mContext, map);
    }

    @Test
    public void profileAppend() {
        String key = "testKey";
        String value = "testValue1";
        AnalysysAgent.profileAppend(mContext, key, value);
        AnalysysAgent.profileAppend(mContext, null, value);
        AnalysysAgent.profileAppend(mContext, key, null);
    }

    @Test
    public void profileAppend1() {
        String key = "testKey";
        List<String> list = new ArrayList<>();
        list.add("testValue1");
        list.add("testValue2");
        AnalysysAgent.profileAppend(mContext, key, list);
    }

    @Test
    public void profileUnset() {
        AnalysysAgent.profileUnset(mContext, "test1");
    }

    @Test
    public void profileDelete() {
        AnalysysAgent.profileDelete(mContext);
    }

    @Test
    public void setAutomaticCollection() {
        AnalysysAgent.setAutomaticCollection(mContext, true);
        AnalysysAgent.setAutomaticCollection(mContext, false);
    }

    @Test
    public void getAutomaticCollection() {
        AnalysysAgent.getAutomaticCollection(mContext);
    }

    @Test
    public void setIgnoredAutomaticCollectionActivities() {
        List<String> list = new ArrayList<>();
        list.add("com.analysys.AnalysysAgent");
        AnalysysAgent.setIgnoredAutomaticCollectionActivities(mContext, list);
    }

    @Test
    public void getIgnoredAutomaticCollection() {
        Assert.assertNotNull(AnalysysAgent.getIgnoredAutomaticCollection(mContext));
    }

    @Test
    public void setVisitorDebugURL() {
        String url = "wss://arksdktest.analysys.cn:4091";
        AnalysysAgent.setVisitorDebugURL(mContext, url);
        AnalysysAgent.setVisitorDebugURL(mContext, null);
    }

    @Test
    public void setVisitorConfigURL() {
        String url = "https://arksdktest.analysys.cn:4089";
        AnalysysAgent.setVisitorConfigURL(mContext, url);
        AnalysysAgent.setVisitorConfigURL(mContext, null);
    }

    @Test
    public void setPushID() {
        String provider = "provider";
        String pushId = "pushId";
        AnalysysAgent.setPushID(mContext, provider, pushId);
        AnalysysAgent.setPushID(mContext, null, pushId);
        AnalysysAgent.setPushID(mContext, provider, null);
    }

    @Test
    public void trackCampaign() {
        String campaign = "campaign";
        AnalysysAgent.trackCampaign(mContext,campaign,true);
        AnalysysAgent.trackCampaign(mContext,campaign,false);
        AnalysysAgent.trackCampaign(mContext,null,true);
        AnalysysAgent.trackCampaign(mContext,null,false);
    }

    @Test
    public void trackCampaign1() {
        String campaign = "campaign";
        PushListener pl = new PushListener() {
            @Override
            public void execute(String action, String jsonParams) {

            }
        };
        AnalysysAgent.trackCampaign(mContext,campaign,true, pl);
        AnalysysAgent.trackCampaign(mContext,campaign,false, pl);
        AnalysysAgent.trackCampaign(mContext,null,true, pl);
        AnalysysAgent.trackCampaign(mContext,null,false, pl);
    }

    @Test
    public void interceptUrl() {
    }

    @Test
    public void setHybridModel() {
    }

    @Test
    public void resetHybridModel() {
    }
}