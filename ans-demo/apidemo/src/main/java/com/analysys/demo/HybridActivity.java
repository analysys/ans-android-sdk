package com.analysys.demo;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.analysys.AnalysysAgent;
import com.analysys.apidemo.R;

public class HybridActivity extends BaseActivity {
    private WebView mWebView;
    private Context mContext = null;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentLayoutId());
        mContext = this.getApplicationContext();
        initWebView();

        dialog = new ProgressDialog(this);
        dialog.setTitle("提示");
        dialog.setMessage("WebView加载中...");
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setCancelable(true);
        dialog.setIndeterminate(true);
        dialog.setMax(100);
        dialog.show();
    }

    protected int getContentLayoutId() {
        return R.layout.activity_hybrid;
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

            mWebView.clearCache(true);

            // 退出时调用此方法，移除绑定的服务，否则某些特定系统会报错
            mWebView.getSettings().setJavaScriptEnabled(false);
            mWebView.clearHistory();
            mWebView.clearView();
            mWebView.removeAllViews();
            mWebView.destroy();
//            AnalysysAgent.resetHybridModel(mContext, mWebView);
            AnalysysAgent.resetAnalysysAgentHybrid(mWebView);
            mWebView = null;
        }
        super.onDestroy();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView() {
        mWebView = findViewById(R.id.web_view);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        mWebView.clearCache(true);
        mWebView.clearHistory();
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        //mWebView.loadUrl("http://192.168.31.48:5173");
        mWebView.loadUrl("http://192.168.50.3:8080");
        //mWebView.loadUrl("http://10.9.24.114:9292?v=" + System.currentTimeMillis());
//        mWebView.setWebViewClient(new MyWebviewClient());
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                dialog.setProgress(newProgress);
                if (newProgress == 100) {
                    dialog.dismiss();
                }
            }
        });
//        AnalysysAgent.setHybridModel(mContext, mWebView);
        AnalysysAgent.setAnalysysAgentHybrid(mWebView);
    }

    class MyWebviewClient extends WebViewClient {

        @Override
        public void onPageFinished(WebView view, String url) {
            Log.d("analysys.hybrid", "onPageFinished url:" + url);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.d("analysys.hybrid", "shouldOverrideUrlLoading url:" + url);
            AnalysysAgent.interceptUrl(mContext, url, view);
            return false;
        }
    }
}
