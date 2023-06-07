package com.analysys.visual.bind.event;

import android.view.View;

import java.util.Set;

/**
 * @Copyright © 2019 Analysys Inc. All rights reserved.
 * @Description: * View监听器封装接口
 * 事件触发拦截主要是通过给view添加监听器实现的，为了不对原有逻辑造成影响还需要保留原有监听器，如果多个监听器存在，可能会造成监听器被覆盖，或者循环触发
 * 要解决的两个问题：
 * * 1、监听器被覆盖，解决方法：对所有目标元素添加监听器，使用asm拦截所有同类监听器
 * * 2、事件触发重复，解决方法：触发事件后判断处理是否结束，如果未处理结束对情况下再次响应则可以确定是循环触发，放弃调用原有监听器即可打破循环
 * @Create: 2019-11-28 09:57
 * @author: hcq
 */
public interface ICallOnceListener {

    /**
     * 获取原有监听器
     */
    Object getCurrent(View view);

    /**
     * 添加监听
     */
    void listen(View view);

    /**
     * 恢复原有监听器，解绑时调用
     */
    void reset(View view);

    /**
     * 每一个view对同类监听器只添加一次，需要维持一个响应列表，事件触发后遍历通知
     */
    void addCaller(ICaller caller);

    /**
     * 移除一个响应者
     */
    void removeCaller(ICaller caller);

    /**
     * 获取所有响应者
     */
    Set<ICaller> getCaller();
}
