package com.analysys.visual.bind.event.impl;

import android.view.View;
import android.view.accessibility.AccessibilityEvent;

import com.analysys.utils.ExceptionUtil;
import com.analysys.visual.bind.VisualBindManager;

/**
 * @Copyright © 2019 Analysys Inc. All rights reserved.
 * @Description: AccessibilityDelegate事件基类，可以满足大部分事件
 * @Create: 2019-11-28 09:55
 * @author: hcq
 */
public abstract class EventAccessibilityBase extends BaseEvent {

    /**
     * 事件类型 {@link AccessibilityEvent}
     */
    private final int mAccessibilityEventType;

    EventAccessibilityBase(String eventType, int accessibilityEventType) {
        super(eventType);
        this.mAccessibilityEventType = accessibilityEventType;
    }

    public int getAccessibilityEventType() {
        return mAccessibilityEventType;
    }

    /**
     * 获取view当前的AccessibilityDelegate
     */
    public abstract View.AccessibilityDelegate getDelegate(int rootViewHashCode, View view);

    /**
     * 触发事件响应，参数顺序 0：触发view，1：accessibility delegate type
     */
    @Override
    public void call(Object... data) {
        if (data == null || data.length < 2 || !checkTime()) {
            return;
        }
        int type = (int) data[1];
        if (type == mAccessibilityEventType) {
            final View eventView = (View) data[0];
            final boolean isMock = data.length >= 3 && (boolean) data[2];
            VisualBindManager.getInstance().postRunnableAtFrontOfQueue(new Runnable() {
                @Override
                public void run() {
                    try {
                        initFire(eventView, isMock);
                    } catch (Throwable ignore) {
                        ExceptionUtil.exceptionThrow(ignore);
                    }
                }
            });
        }
    }
}
