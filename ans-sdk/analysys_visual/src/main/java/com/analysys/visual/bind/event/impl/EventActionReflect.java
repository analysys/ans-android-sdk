package com.analysys.visual.bind.event.impl;

import com.analysys.visual.bind.event.IEventAction;
import com.analysys.visual.utils.ReflectUnit;

/**
 * @Copyright © 2019 Analysys Inc. All rights reserved.
 * @Description: 事件响应执行类，反射调用目标元素的方法
 * @Create: 2019-11-28 09:55
 * @author: hcq
 */
public class EventActionReflect implements IEventAction {

    private final ReflectUnit mReflect;

    public EventActionReflect(ReflectUnit reflectUnit) {
        mReflect = reflectUnit;
    }

    @Override
    public void doAction(BaseEvent event) {
        if (mReflect != null) {
            mReflect.call(event.getEventView());
        }
    }
}
