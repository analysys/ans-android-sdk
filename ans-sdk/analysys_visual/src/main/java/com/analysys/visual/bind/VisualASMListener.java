package com.analysys.visual.bind;

import android.view.View;

import com.analysys.allgro.plugin.ASMHookAdapter;
import com.analysys.utils.ExceptionUtil;

/**
 * @Copyright © 2019 Analysys Inc. All rights reserved.
 * @Description: asm回调监听类，为了防止绑定事件时注册监听器失败（可能被其它业务覆盖），使用asm在编译时插入检测代码
 * @Create: 2019-11-28 11:56
 * @author: hcq
 */
public class VisualASMListener extends ASMHookAdapter {

    @Override
    public void trackSendAccessibilityEvent(final View view, final int eventType, boolean hasTrackClickAnn) {
        VisualBindManager.getInstance().postRunnableAtFrontOfQueue(new Runnable() {
            @Override
            public void run() {
                try {
                    VisualBindManager.getInstance().newAccessibilityEvent(view, eventType, false);
                } catch (Throwable ignore) {
                    ExceptionUtil.exceptionThrow(ignore);
                }
            }
        });
    }
}
