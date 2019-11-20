package com.analysys.process;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @Copyright © 2019 EGuan Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2019-08-15 15:41
 * @Author: Wang-X-C
 */
public class AgentProcessTest {

    @Test
    public void getInstance() {
        Assert.assertNotNull(AgentProcess.getInstance());
    }

    @Test
    public void init() {
    }

    @Test
    public void setDebug() {
    }

    @Test
    public void appStart() {
        AgentProcess.getInstance().appStart(true, System.currentTimeMillis());
        AgentProcess.getInstance().appStart(false, System.currentTimeMillis());
    }

    @Test
    public void appEnd() {
//        AgentProcess.getInstance(mContext).appEnd(1000);
    }

    @Test
    public void pageView() {
    }

    @Test
    public void hybridPageView() {
        String pageName = "testPage";
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("aa", "AAA");
        map.put("bb", "BBB");
        AgentProcess.getInstance().hybridPageView(pageName, map);
        AgentProcess.getInstance().hybridPageView(null, map);
        AgentProcess.getInstance().hybridPageView(pageName, null);
    }

    @Test
    public void autoCollectPageView() {
        String pageUrl = "com.analysys.process.AgentProcess";
        String title = "测试title";
//        AgentProcess.getInstance(mContext).autoCollectPageView(pageUrl, title);
    }

    @Test
    public void pageTouchInfo() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("aaa", "AAA");
        map.put("bbb", "BBB");
//        AgentProcess.getInstance(mContext).pageTouchInfo(map);
    }

    @Test
    public void track() {
    }

    @Test
    public void identify() {
    }

    @Test
    public void getDistinctId() {
    }

    @Test
    public void alias() {
    }

    @Test
    public void profileSet() {
    }

    @Test
    public void profileSet1() {
    }

    @Test
    public void profileSetOnce() {
    }

    @Test
    public void profileSetOnce1() {
    }

    @Test
    public void profileIncrement() {
    }

    @Test
    public void profileIncrement1() {
    }

    @Test
    public void profileAppend() {
    }

    @Test
    public void profileAppend1() {
    }

    @Test
    public void profileAppend2() {
    }

    @Test
    public void profileUnset() {
    }

    @Test
    public void profileDelete() {
    }

    @Test
    public void registerSuperProperty() {
    }

    @Test
    public void registerSuperProperties() {
    }

    @Test
    public void getSuperProperty() {
    }

    @Test
    public void getSuperProperty1() {
    }

    @Test
    public void unregisterSuperProperty() {
    }

    @Test
    public void clearSuperProperty() {
    }

    @Test
    public void setIntervalTime() {
    }

    @Test
    public void setMaxEventSize() {
    }

    @Test
    public void getMaxCacheSize() {
    }

    @Test
    public void setMaxCacheSize() {
    }

    @Test
    public void reset() {
    }

    @Test
    public void setUploadURL() {
    }

    @Test
    public void automaticCollection() {
    }

    @Test
    public void getAutomaticCollection() {
    }

    @Test
    public void getIgnoredAutomaticCollection() {
    }

    @Test
    public void setIgnoredAutomaticCollection() {
    }

    @Test
    public void flush() {
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

    @Test
    public void setVisitorBaseURL() {
    }

    @Test
    public void setVisitorDebugURL() {
    }

    @Test
    public void setVisitorConfigURL() {
    }

    @Test
    public void enablePush() {
    }

    @Test
    public void trackCampaign() {
    }
}