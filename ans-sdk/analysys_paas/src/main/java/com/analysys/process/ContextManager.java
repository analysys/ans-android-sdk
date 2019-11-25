package com.analysys.process;

import android.content.Context;

import com.analysys.utils.CommonUtils;

import java.lang.ref.WeakReference;

/**
 * @Copyright © 2019 EGuan Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2019-07-02 14:30
 * @Author: Wang-X-C
 */
public class ContextManager {

    private static Context sAppContext;

    public static Context getContext() {
        return sAppContext;
    }

    public static void setContext(Context context) {
        if (sAppContext == null && context != null) {
            sAppContext = context.getApplicationContext();
        }
    }
}
