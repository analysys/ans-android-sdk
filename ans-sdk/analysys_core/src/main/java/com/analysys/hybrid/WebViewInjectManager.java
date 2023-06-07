package com.analysys.hybrid;

import android.view.View;
import android.view.ViewGroup;

import com.analysys.ui.UniqueViewHelper;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Copyright © 2020 Analysys Inc. All rights reserved.
 * @Description: WebView注入辅助类
 * @Create: 2020-05-20 6:30 PM
 * @author: hcq
 */
public class WebViewInjectManager {

    private static WebViewInjectManager sInstance = new WebViewInjectManager();

    private WebViewInjectManager() {
    }

    public static WebViewInjectManager getInstance() {
        return sInstance;
    }

    private Map<Integer, HybridObject> mHybridObjects = new ConcurrentHashMap<>();

    private BaseWebViewInjector mInjector;

    /**
     * 初始化hybrid环境，通知jssdk使用hybrid协议
     */
    public synchronized HybridObject injectHybridObject(Object webView) {
        if (webView == null) {
            return null;
        }
        HybridObject obj;
        if (webView instanceof HybridObject) {
            obj = (HybridObject) webView;
        } else {
            obj = new HybridObject(webView);
        }
        int hashCode = obj.getHashCode();
        if (mHybridObjects.containsKey(hashCode)) {
            return mHybridObjects.get(hashCode);
        }
        obj.init();
        mHybridObjects.put(hashCode, obj);
        if (mInjector != null) {
            mInjector.notifyInject(hashCode);
        }
        return obj;
    }

    public void setInjector(BaseWebViewInjector injector) {
        for (HybridObject obj : mHybridObjects.values()) {
            injector.notifyInject(obj.getHashCode());
        }
        mInjector = injector;
    }

    BaseWebViewInjector getInjector() {
        return mInjector;
    }

    /**
     * 某个WebView加载url
     */
    public void loadUrl(Object webView, String url) {
        if (webView == null) {
            return;
        }
        int hashCode = webView.hashCode();
        HybridObject obj = mHybridObjects.get(hashCode);
        if (obj == null) {
            return;
        }
        obj.loadUrl(url);
    }

    /**
     * 全部WebView加载url
     */
    public void loadUrlAll(String url) {
        for (Integer key : mHybridObjects.keySet()) {
            HybridObject obj = mHybridObjects.get(key);
            obj.loadUrl(url);
        }
    }

    /**
     * 特定页面里的WebView加载url
     */
    public void loadUrlInPage(String url, int pageHashCode) {
        Iterator<Map.Entry<Integer, HybridObject>> it = mHybridObjects.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, HybridObject> entry = it.next();
            HybridObject obj = entry.getValue();
            if (pageHashCode == obj.getPageHashCode()) {
                obj.loadUrl(url);
            }
        }
    }

    /**
     * 清除hybrid
     */
    public void clearHybrid(Object webView) {
        if (webView == null) {
            return;
        }
        int hashCode;
        if (webView instanceof HybridObject) {
            hashCode = ((HybridObject) webView).getHashCode();
        } else {
            hashCode = webView.hashCode();
        }
        HybridObject obj = mHybridObjects.remove(hashCode);
        if (obj != null) {
            obj.clear();
        }
    }

    /**
     * 特定页面清除hybrid
     */
    public void clearHybridInPage(int pageHashCode) {
        Iterator<Map.Entry<Integer, HybridObject>> it = mHybridObjects.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, HybridObject> entry = it.next();
            HybridObject obj = entry.getValue();
            if (pageHashCode == obj.getPageHashCode()) {
                obj.clear();
                it.remove();
            }
        }
    }

//    public boolean isPageInjected(String pageName) {
//        Iterator<Map.Entry<Integer, HybridObject>> it = mHybridObjects.entrySet().iterator();
//        while (it.hasNext()) {
//            Map.Entry<Integer, HybridObject> entry = it.next();
//            HybridObject obj = entry.getValue();
//            if (TextUtils.equals(pageName, obj.getPageHashCode())) {
//                return true;
//            }
//        }
//        return false;
//    }

    public void injectWebViewInPage(View rootView) {
        if (rootView != null && UniqueViewHelper.isWebView(rootView.getClass())) {
            injectHybridObject(rootView);
        } else if (rootView instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) rootView;
            int childCount = vg.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = vg.getChildAt(i);
                injectWebViewInPage(child);
            }
        }
    }
}
