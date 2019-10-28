package com.analysys.demo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.analysys.ANSAutoPageTracker;
import com.analysys.AnalysysAgent;

import java.util.Map;

/**
 * @Copyright © 2019 EGuan Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2019-07-01 00:48
 * @Author: Wang-X-C
 */
public class WebViewActivity extends AppCompatActivity implements ANSAutoPageTracker {
    private WebView mWebView;
    private Context mContext = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        initWebView();
        mContext = this.getApplicationContext();
    }

    @Override
    protected void onDestroy() {
        if (mWebView != null) {
            // 如果先调用destroy()方法，则会命中if (isDestroyed()) return;这一行代码，需要先onDetachedFromWindow()，再
            // destory()
            ViewParent parent = mWebView.getParent();
            if (parent != null) {
                ((ViewGroup) parent).removeView(mWebView);
            }
            mWebView.stopLoading();
            // 退出时调用此方法，移除绑定的服务，否则某些特定系统会报错
            mWebView.getSettings().setJavaScriptEnabled(false);
            mWebView.clearHistory();
            mWebView.clearView();
            mWebView.removeAllViews();
            mWebView.destroy();

        }
        super.onDestroy();
        AnalysysAgent.resetHybridModel(mContext, mWebView);
        System.exit(0);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView() {
        mWebView = new WebView(getApplicationContext());
        LinearLayout linearLayout = findViewById(R.id.webViewLayout);
        linearLayout.addView(mWebView);
        mWebView.loadUrl("http://uc.analysys.cn/huaxiang/hybrid-4.3.0.10/");
        mWebView.setWebViewClient(new MyWebviewClient());
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        AnalysysAgent.setHybridModel(mContext, mWebView);
    }

    @Override
    public Map<String, Object> registerPageProperties() {
        return null;
    }

    @Override
    public String registerPageUrl() {
        return "WebView 页";
    }

    class MyWebviewClient extends WebViewClient {
        @Override
        public void onPageFinished(WebView view, String url) {
            Log.d("analysys.hybrid", "onPageFinished url:" + url);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            AnalysysAgent.interceptUrl(mContext, url, view);
            return false;
        }
    }
}
