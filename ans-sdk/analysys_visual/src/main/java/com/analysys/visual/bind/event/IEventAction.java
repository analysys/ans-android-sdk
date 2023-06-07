package com.analysys.visual.bind.event;

import com.analysys.visual.bind.event.impl.BaseEvent;

/**
 * @Copyright © 2019 Analysys Inc. All rights reserved.
 * @Description: 事件响应执行接口
 * @Create: 2019-11-28 10:00
 * @author: hcq
 */
public interface IEventAction {

    void doAction(BaseEvent event);
}
