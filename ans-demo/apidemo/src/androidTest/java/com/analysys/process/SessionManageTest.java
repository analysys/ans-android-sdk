package com.analysys.process;

import android.content.Context;

import com.analysys.utils.AnalysysUtil;

import org.junit.Assert;
import org.junit.Test;

/**
 * @Copyright © 2019 EGuan Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2019-08-16 10:35
 * @Author: Wang-X-C
 */
public class SessionManageTest {
    Context mContext = AnalysysUtil.getContext();

    @Test
    public void getInstance() {
        SessionManage.getInstance(mContext);
    }

    @Test
    public void resetSession() {
        SessionManage.getInstance(mContext).resetSession(true);
        SessionManage.getInstance(mContext).resetSession(false);
    }

    @Test
    public void getSessionId() {
        Assert.assertNotNull(SessionManage.getInstance(mContext).getSessionId());
    }

//    @Test
//    public void setPageEnd() {
//        SessionManage.getInstance(mContext).setPageEnd();
//    }
}