package com.analysys.visual.utils;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.analysys.hybrid.BaseWebViewInjector;
import com.analysys.hybrid.WebViewInjectManager;
import com.analysys.ui.UniqueViewHelper;
import com.analysys.utils.ExceptionUtil;
import com.analysys.visual.bind.VisualBindManager;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Copyright © 2019 Analysys Inc. All rights reserved.
 * @Description: WebView绑定辅助类，所有webview函数在同initHybrid函数调用线程执行
 * @Create: 2019-11-29 11:35
 * @author: hcq
 */
public class WebViewBindHelper extends BaseWebViewInjector {

    private static WebViewBindHelper sInstance = new WebViewBindHelper();
    private Map<Integer, HybridEventObject> mHybridObjects = new ConcurrentHashMap<>();

    private WebViewBindHelper() {
        WebViewInjectManager.getInstance().setInjector(this);
    }

    public static WebViewBindHelper getInstance() {
        return sInstance;
    }

    public boolean isVisualDomListChanged() {
        for (HybridEventObject obj : mHybridObjects.values()) {
            if (obj.domListChanged) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取webview dom节点信息
     */
    public String getVisualDomList(Object webView) {
        if (webView == null) {
            return null;
        }
        int hashCode = webView.hashCode();
        HybridEventObject obj = mHybridObjects.get(hashCode);
        if (obj == null) {
            return null;
        }
        WebViewInjectManager.getInstance().loadUrl(webView, "javascript:try{window.AnalysysAgent.getVisualDomList(" + webView.hashCode() + ")}catch(err){console.log(err.message)}");
        // 等待js返回，如果没有返回，上报上一次的结构
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
        }
        return obj.hybridDomList;
    }

    /**
     * 绑定WebView事件
     *
     * @param webView WebView对象
     * @param event   事件json
     */
    private void bindWebView(Object webView, String event) {
        WebViewInjectManager.getInstance().loadUrl(webView, getEventStr(event));
    }

    public void unBindAll() {
        WebViewInjectManager.getInstance().loadUrlAll(getEventStr(""));
    }

    private String getEventStr(String event) {
        return "javascript:try{window.AnalysysAgent.onEventList(" + event + ")}catch(err){console.log(err.message)}";
    }

    public void clearHybridInPage(int pageHashCode) {
        WebViewInjectManager.getInstance().loadUrlInPage(getEventStr(""), pageHashCode);
        WebViewInjectManager.getInstance().clearHybridInPage(pageHashCode);
    }

    public void bindWebViewInPage(View rootView, String event) {
        if (rootView != null && UniqueViewHelper.isWebView(rootView.getClass())) {
            bindWebView(rootView, event);
        } else if (rootView instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) rootView;
            int childCount = vg.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = vg.getChildAt(i);
                bindWebViewInPage(child, event);
            }
        }
    }

    public Map<String, Object> getHybridProperty(View webView, String strRelate) {
        if (webView == null) {
            return null;
        }
        int hashCode = webView.hashCode();
        HybridEventObject obj = mHybridObjects.get(hashCode);
        if (obj == null) {
            return null;
        }
        obj.hybridProperty = null;
        WebViewInjectManager.getInstance().loadUrl(webView, "javascript:try{window.AnalysysAgent.getProperty(" + strRelate + ")}catch(err){console.log(err.message)}");
        int count = 0;
        while (count++ < 40) {
            if (obj.hybridProperty == null) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                }
            } else {
                break;
            }
        }
        String property = obj.hybridProperty;
        if (!TextUtils.isEmpty(property)) {
            Map<String, Object> result = new HashMap<>();
            try {
                JSONObject jo = new JSONObject(property);
                Iterator<String> it = jo.keys();
                while (it.hasNext()) {
                    String key = it.next();
                    Object value = jo.get(key);
                    result.put(key, value);
                }
                return result;
            } catch (Throwable ignore) {
                ExceptionUtil.exceptionThrow(ignore);
            }
        }
        return null;
    }

    @Override
    public boolean isHybrid(int hashCode) {
        return true;
    }

    @Override
    public void onVisualDomList(int hashCode, String info) {
        HybridEventObject obj = mHybridObjects.get(hashCode);
        if (obj == null) {
            return;
        }
        if (TextUtils.isEmpty(info)) {
            obj.domListChanged = true;
            obj.hybridDomList = "";
        } else {
            obj.domListChanged = !info.equals(obj.hybridDomList);
            obj.hybridDomList = info;
        }
    }

    @Override
    public void onProperty(int hashCode, String info) {
        HybridEventObject obj = mHybridObjects.get(hashCode);
        if (obj == null) {
            return;
        }
        if (TextUtils.isEmpty(info)) {
            obj.hybridProperty = "";
        } else {
            obj.hybridProperty = info;
        }
    }

    @Override
    public void AnalysysAgentTrack(int hashCode, String eventId, String eventInfo, String extraEditInfo) {
        VisualBindManager.getInstance().reportHybrid(eventId, eventInfo, extraEditInfo);
    }

    @Override
    public String getEventList(int hashCode) {
        String events = VisualBindManager.getInstance().getHybridEventList();
        return events;
    }

    @Override
    public String getProperty(Object webView, String info) {
        String ret = null;
        if (webView != null) {
            ret = VisualBindManager.getInstance().getNativeProperty(webView, info);
        }
        if (TextUtils.isEmpty(ret)) {
            ret = "{}";
        }
        return ret;
    }

    @Override
    public void notifyInject(int hashCode) {
        HybridEventObject obj = new HybridEventObject();
        mHybridObjects.put(hashCode, obj);
    }

    @Override
    public void clearHybrid(int hashCode) {
        HybridEventObject obj = mHybridObjects.remove(hashCode);
        if (obj != null) {
            obj.clear();
        }
    }

    private class HybridEventObject {

        private volatile String hybridDomList;
        private volatile boolean domListChanged = true;

        private volatile String hybridProperty;

        void clear() {
            hybridDomList = null;
            hybridProperty = null;
        }
    }
}
