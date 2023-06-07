package com.analysys.ui;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Copyright © 2020 Analysys Inc. All rights reserved.
 * @Description:
 * @Create: 2020/5/20 6:54 PM
 * @author: huchangqing
 */
public class UniqueViewHelper {

    /**
     * 由于androidx，support库的存在导致view类名不一致，使用unique类名替代
     *
     * @param clzName       查找类名
     * @param uniqueClzName unique类名
     */
    public static boolean isExtendsFromUniqueClass(String clzName, String uniqueClzName) {
        if (TextUtils.isEmpty(clzName) || TextUtils.isEmpty(uniqueClzName)) {
            return false;
        }
        boolean eq = isUniqueClassExactly(clzName, uniqueClzName);
        if (!eq) {
            try {
                Class clz = Class.forName(clzName);
                while (clz != Object.class) {
                    clz = clz.getSuperclass();
                    if (clz != null && isUniqueClassExactly(clz.getName(), uniqueClzName)) {
                        return true;
                    }
                }
            } catch (Throwable ignore) {
            }
        }
        return eq;
    }

    public static final String UNIQUE_CLZ_VIEW_PAGER = "unique.ViewPager";
    public static final String UNIQUE_CLZ_RECYCLER_VIEW = "unique.RecyclerView";
    public static final String UNIQUE_CLZ_WEB_VIEW = "unique.WebView";
    public static final String UNIQUE_CLZ_CONTEXT_THEME_WRAPPER = "unique.ContextThemeWrapper";
    private static Map<String, List<String>> sUniqueClzMap = new HashMap<>();

    static {
        List<String> list = new ArrayList<>();
        list.add("com.android.internal.widget.ViewPager");
        list.add("android.support.v4.view.ViewPager");
        list.add("androidx.viewpager.widget.ViewPager");
        sUniqueClzMap.put(UNIQUE_CLZ_VIEW_PAGER, list);

        list = new ArrayList<>();
        list.add("com.android.internal.widget.RecyclerView");
        list.add("android.support.v7.widget.RecyclerView");
        list.add("androidx.recyclerview.widget.RecyclerView");
        sUniqueClzMap.put(UNIQUE_CLZ_RECYCLER_VIEW, list);

        list = new ArrayList<>();
        list.add("android.webkit.WebView");
        list.add("com.tencent.smtt.sdk.WebView");
        list.add("com.alipay.mobile.nebulauc.impl.UCWebView$WebViewEx");
        sUniqueClzMap.put(UNIQUE_CLZ_WEB_VIEW, list);

        list = new ArrayList<>();
        list.add("android.view.ContextThemeWrapper");
        list.add("androidx.appcompat.view.ContextThemeWrapper");
        list.add("android.support.v7.view.ContextThemeWrapper");
        list.add("com.facebook.react.uimanager.ThemedReactContext");
        sUniqueClzMap.put(UNIQUE_CLZ_CONTEXT_THEME_WRAPPER, list);
    }

    public static String getUniqueClzName(String clzName) {
        if (TextUtils.isEmpty(clzName)) {
            return null;
        }
        Set<String> clzSet = sUniqueClzMap.keySet();
        String objClzName = Object.class.getName();
        while (!TextUtils.equals(clzName, objClzName)) {
            for (String key : clzSet) {
                if (sUniqueClzMap.get(key).contains(clzName)) {
                    return key;
                }
            }
            try {
                clzName = Class.forName(clzName).getSuperclass().getName();
            } catch (Throwable ignore) {
            }
        }
        return null;
    }

    private static boolean isUniqueClassExactly(String clzName, String uniqueClzName) {
        List<String> list = sUniqueClzMap.get(uniqueClzName);
        if (list == null) {
            return false;
        }
        return list.contains(clzName);
    }

    public static boolean isWebView(Class<?> clz) {
        if (clz == null) {
            return false;
        }
        return isWebView(clz.getName());
    }

    public static boolean isWebView(String clzName) {
        return isExtendsFromUniqueClass(clzName, UNIQUE_CLZ_WEB_VIEW);
    }

    public static boolean isContextThemeWrapper(String clzName) {
        return isExtendsFromUniqueClass(clzName, UNIQUE_CLZ_CONTEXT_THEME_WRAPPER);
    }
}
