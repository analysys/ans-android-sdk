package com.analysys.visual.bind.event;

import android.view.View;

/**
 * @Copyright © 2019 Analysys Inc. All rights reserved.
 * @Description: 事件监听器绑定辅助类
 * @Create: 2019-11-28 09:57
 * @author: hcq
 */
public class BindHelper {

    /**
     * 对view绑定listener，响应列表中添加caller
     */
    public static void bind(View view, ICaller caller, ICallOnceListener listener) {
        if (view == null || caller == null || listener == null) {
            return;
        }
        listener.listen(view);
        listener.addCaller(caller);
    }

    /**
     * 对view解绑listener，响应列表为空则重置监听器
     */
    public static void unbind(View view, ICaller caller, ICallOnceListener listener) {
        if (view == null || caller == null || listener == null) {
            return;
        }
        listener.removeCaller(caller);
        if (listener.getCaller().isEmpty()) {
            listener.reset(view);
        }
    }
}
