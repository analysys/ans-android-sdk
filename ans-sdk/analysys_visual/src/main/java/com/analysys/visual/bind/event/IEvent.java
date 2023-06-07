package com.analysys.visual.bind.event;

import com.analysys.ui.RootView;

/**
 * @Copyright © 2019 Analysys Inc. All rights reserved.
 * @Description: 事件接口
 * @Create: 2019-11-28 10:00
 * @author: hcq
 */
public interface IEvent {

    /**
     * 从根元素开始查找，绑定符合条件的元素
     *
     * @param rootView 根元素
     */
    void bind(RootView rootView);

    /**
     * 解绑根元素下的所有元素
     *
     * @param rootView 根元素
     */
    void unbind(RootView rootView);

    /**
     * 解绑所有根元素
     */
    void unbindAll();
}
