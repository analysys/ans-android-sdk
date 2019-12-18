package com.analysys.process;

import org.junit.Assert;
import org.junit.Test;

/**
 * @Copyright © 2019 EGuan Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2019-08-16 10:17
 * @Author: Wang-X-C
 */
public class SystemIdsTest {

    @Test
    public void getInstance() {
        Assert.assertNotNull(SystemIds.getInstance());
    }

    @Test
    public void parserId() {
        SystemIds.getInstance().parserId();
    }

    @Test
    public void knownIdName() {
//        SystemIds.getInstance(mContext).knownIdName();
    }

    @Test
    public void idFromName() {
//        SystemIds.getInstance(mContext).idFromName();
    }

    @Test
    public void nameForId() {
//        SystemIds.getInstance(mContext).nameForId();
    }

    @Test
    public void isRegister() {
        Assert.assertFalse(SystemIds.getInstance().isRegister());
    }
}