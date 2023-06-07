package com.analysys.ui;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewTreeObserver;

/**
 * @Copyright © 2019 Analysys Inc. All rights reserved.
 * @Description: 根view封装类
 * @Create: 2019-11-28 10:01
 * @author: hcq
 */
public class RootView {
    /**
     * 根view
     */
    public final View view;

    /**
     * 根view hashCode
     */
    public final int hashCode;

    /**
     * 根view所在页面名字，可能为activity、dialog、float window、popup window等，参考
     */
    public final String pageName;

    /**
     * 根view绑定的GlobalLayoutListener
     */
    public ViewTreeObserver.OnGlobalLayoutListener globalLayoutListener;

    /**
     * 根view绑定的ScrollChangedListener
     */
    public ViewTreeObserver.OnScrollChangedListener scrollChangedListener;

    public RootView(View view, String pageName) {
        this.view = view;
        this.hashCode = view.hashCode();
        this.pageName = pageName;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof RootView) {
            RootView brv = (RootView) obj;
            return view == brv.view && TextUtils.equals(pageName, brv.pageName);
        }
        return false;
    }
}
