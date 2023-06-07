package com.analysys.visual.bind.event.impl;

import com.analysys.visual.bind.VisualBindManager;
import com.analysys.visual.bind.event.IEventAction;

/**
 * @Copyright © 2019 Analysys Inc. All rights reserved.
 * @Description: 事件响应执行类，上报埋点
 * @Create: 2019-11-28 09:56
 * @author: hcq
 */
public class EventActionReport implements IEventAction {

    @Override
    public void doAction(BaseEvent event) {
        VisualBindManager.getInstance().report(event);
    }
}
