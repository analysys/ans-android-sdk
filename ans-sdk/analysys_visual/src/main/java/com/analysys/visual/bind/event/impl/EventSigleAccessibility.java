package com.analysys.visual.bind.event.impl;

import android.util.SparseArray;
import android.view.View;

import com.analysys.visual.bind.event.BindHelper;
import com.analysys.visual.bind.locate.ViewFinder;

import java.util.HashMap;
import java.util.Map;

/**
 * @Copyright © 2019 Analysys Inc. All rights reserved.
 * @Description: AccessibilityDelegate非同级事件类
 * @Create: 2019-11-28 09:56
 * @author: hcq
 */
public class EventSigleAccessibility extends EventAccessibilityBase {

    /**
     * AccessibilityDelegate事件监听器，每个root view下的每个目标元素都绑定一个
     */
    private final SparseArray<Map<View, CallOnceAccessibilityListener>> mBindMap = new SparseArray<>();

    public EventSigleAccessibility(String eventType, int accessibilityEventType) {
        super(eventType, accessibilityEventType);
    }

    @Override
    public View.AccessibilityDelegate getDelegate(int rootViewHashCode, View view) {
        Map<View, CallOnceAccessibilityListener> listeneres = mBindMap.get(rootViewHashCode);
        if (listeneres != null) {
            for (View bindView : listeneres.keySet()) {
                if (bindView == view) {
                    CallOnceAccessibilityListener listener = listeneres.get(view);
                    if (listener == null) {
                        return null;
                    }
                    Object obj = listener.getCurrent(bindView);
                    if (!(obj instanceof View.AccessibilityDelegate)) {
                        return null;
                    }
                    return (View.AccessibilityDelegate) obj;
                }
            }
        }
        return null;
    }

    @Override
    protected void doBind(ViewFinder.FindResult result) {
        if (result.listTargetView == null || result.listTargetView.isEmpty()) {
            return;
        }
        // 初始化绑定map，原则上每次绑定前都不能是绑定状态，listeneres都应该是空的
        Map<View, CallOnceAccessibilityListener> listeneres = mBindMap.get(result.rootViewHashCode);
        if (listeneres == null) {
            listeneres = new HashMap<>();
            mBindMap.put(result.rootViewHashCode, listeneres);
        }
        listeneres.clear();

        // 监听所有目标元素
        for (int i = 0; i < result.listTargetView.size(); i++) {
            View view = result.listTargetView.get(i);
            view.setTag(TAG_ROOT_HASH, result.rootViewHashCode);
            CallOnceAccessibilityListener listener;
            Object obj = CallOnceAccessibilityListener.getBindListener(view);
            if (obj instanceof CallOnceAccessibilityListener) {
                listener = (CallOnceAccessibilityListener) obj;
                listener.addCaller(this);
            } else {
                listener = new CallOnceAccessibilityListener(view, this);
                BindHelper.bind(view, this, listener);
            }
            listeneres.put(view, listener);
        }
    }

    @Override
    public void doUnbind(ViewFinder.FindResult result) {
        Map<View, CallOnceAccessibilityListener> listeneres = mBindMap.get(result.rootViewHashCode);
        if (listeneres == null) {
            return;
        }

        for (View view : listeneres.keySet()) {
            view.setTag(TAG_ROOT_HASH, null);
            BindHelper.unbind(view, this, listeneres.get(view));
        }
        listeneres.clear();
        mBindMap.remove(result.rootViewHashCode);
    }
}
