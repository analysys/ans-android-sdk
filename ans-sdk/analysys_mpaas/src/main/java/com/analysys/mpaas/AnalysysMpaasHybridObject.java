package com.analysys.mpaas;

import android.webkit.JavascriptInterface;

import com.alipay.mobile.nebula.webview.APWebView;
import com.analysys.hybrid.HybridObject;

/**
 * @Copyright © 2020 Analysys Inc. All rights reserved.
 * @Description:
 * @Create: 2020/9/11 11:22 AM
 * @author: huchangqing
 */
class AnalysysMpaasHybridObject extends HybridObject {

    private Object mUCWebView;

    public AnalysysMpaasHybridObject(APWebView webView) {
        super(webView);
        mUCWebView = webView.getView();
    }

    @Override
    protected void clearWebView() {
        super.clearWebView();
        mUCWebView = null;
    }

    @Override
    public int getHashCode() {
        return mUCWebView.hashCode();
    }

    @Override
    protected Object getView() {
        return mUCWebView;
    }

    @Override
    @JavascriptInterface
    @com.uc.webview.export.JavascriptInterface
    public boolean isHybrid() {
        return super.isHybrid();
    }

    @Override
    @JavascriptInterface
    @com.uc.webview.export.JavascriptInterface
    public void onVisualDomList(String info) {
        super.onVisualDomList(info);
    }

    @Override
    @JavascriptInterface
    @com.uc.webview.export.JavascriptInterface
    public void onProperty(String info) {
        super.onProperty(info);
    }

    @Override
    @JavascriptInterface
    @com.uc.webview.export.JavascriptInterface
    public void AnalysysAgentTrack(String eventId, String eventInfo, String extraEditInfo) {
        super.AnalysysAgentTrack(eventId, eventInfo, extraEditInfo);
    }

    @Override
    @JavascriptInterface
    @com.uc.webview.export.JavascriptInterface
    public void analysysHybridCallNative(String msg) {
        super.analysysHybridCallNative(msg);
    }

    @Override
    @JavascriptInterface
    @com.uc.webview.export.JavascriptInterface
    public String getEventList() {
        return super.getEventList();
    }

    @Override
    @JavascriptInterface
    @com.uc.webview.export.JavascriptInterface
    public String getProperty(String info) {
        return super.getProperty(info);
    }
}
