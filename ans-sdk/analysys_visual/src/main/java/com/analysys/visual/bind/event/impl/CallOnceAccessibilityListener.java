package com.analysys.visual.bind.event.impl;

import android.view.View;

import com.analysys.utils.AnsReflectUtils;
import com.analysys.visual.bind.event.ICallOnceListener;
import com.analysys.visual.bind.event.ICaller;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @Copyright © 2019 Analysys Inc. All rights reserved.
 * @Description: AccessibilityDelegate的封装，详细介绍见 {@link ICallOnceListener}
 * @Create: 2019-11-28 09:54
 * @author: hcq
 */
public class CallOnceAccessibilityListener extends View.AccessibilityDelegate implements ICallOnceListener {

    private final View.AccessibilityDelegate mOri;
    private boolean mIsCalling;
    private final Set<ICaller> mCaller = new CopyOnWriteArraySet<>();

    public CallOnceAccessibilityListener(View view, ICaller caller) {
        mOri = (View.AccessibilityDelegate) getCurrent(view);
        mCaller.add(caller);
    }

    @Override
    public void addCaller(ICaller caller) {
        mCaller.add(caller);
    }

    @Override
    public void removeCaller(ICaller caller) {
        mCaller.remove(caller);
    }

    @Override
    public Set<ICaller> getCaller() {
        return mCaller;
    }

    public static Object getBindListener(View view) {
        return AnsReflectUtils.invokeMethod(view, "getAccessibilityDelegate");
    }

    @Override
    public Object getCurrent(View view) {
        return AnsReflectUtils.invokeMethod(view, "getAccessibilityDelegate");
    }

    @Override
    public void reset(View view) {
        view.setAccessibilityDelegate(mOri);
    }

    @Override
    public void listen(View view) {
        view.setAccessibilityDelegate(this);
    }

    @Override
    public void sendAccessibilityEvent(View host, int eventType) {
        if (mIsCalling) {
            return;
        }
        mIsCalling = true;
        for (ICaller caller : mCaller) {
            caller.call(host, eventType);
        }
        if (mOri != null) {
            mOri.sendAccessibilityEvent(host, eventType);
        }
        mIsCalling = false;
    }
}