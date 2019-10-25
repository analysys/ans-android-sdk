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

    private static WeakReference<Context> weakReference = null;

    public static Context getContext() {
        if (weakReference == null) {
            weakReference = new WeakReference<Context>(CommonUtils.getApplication());
        }
        return (weakReference == null) ? null : weakReference.get();
    }

    public static void setContext(Context context) {
        if (weakReference == null && context != null) {
            weakReference = new WeakReference<Context>(context.getApplicationContext());
        }
    }
}
