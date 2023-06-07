package com.analysys.visual.cmd;

import android.text.TextUtils;

import com.analysys.visual.utils.ViewMethodReflector;

public class PageViewInfo {
    public final String name;
    public final Class<?> targetClass;
    public final ViewMethodReflector accessor;
    public PageViewInfo(String name, Class<?> targetClass, ViewMethodReflector accessor) {
        this.name = name;
        this.targetClass = targetClass;
        this.accessor = accessor;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PageViewInfo)) {
            return false;
        }
        PageViewInfo pro = (PageViewInfo) obj;
        return TextUtils.equals(name, pro.name);
    }

    @Override
    public String toString() {
        return "[PageViewInfo " + name + "," + targetClass + ", " + accessor + "]";
    }
}
