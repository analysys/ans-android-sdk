package com.analysys.hybrid;
import com.analysys.userinfo.UserInfo;
import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewTreeObserver;
import android.webkit.JavascriptInterface;

import com.analysys.ui.WindowUIHelper;
import com.analysys.utils.ActivityLifecycleUtils;
import com.analysys.utils.AnsReflectUtils;
import com.analysys.utils.ExceptionUtil;

import org.json.JSONObject;
import org.json.JSONStringer;
import com.analysys.utils.CommonUtils;

import com.analysys.utils.InternalAgent;
import com.analysys.utils.SharedUtil;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import com.analysys.utils.Constants;
import com.analysys.utils.AnalysysUtil;

public class HybridObject {

    private static final String WEB_OBJ = "AnalysysAgentHybrid";

    private volatile boolean mInited;

    private Handler mHandler;

    private Object mWebView;

    private int mPageHashCode;

    private String mCurrentUrl;

    public HybridObject(Object webView) {
        mWebView = webView;
    }

    public int getHashCode() {
        return mWebView.hashCode();
    }

    void init() {
        Object looper = AnsReflectUtils.invokeMethod(mWebView, "getWebViewLooper");
        if (looper == null) {
            looper = Looper.getMainLooper();
        }
        mHandler = new Handler((Looper) looper);
        Object view = getView();
        if (view instanceof View) {
            mPageHashCode = ((View) view).getRootView().hashCode();
        }
        Runnable injectRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    Object settings = AnsReflectUtils.invokeMethod(mWebView, "getSettings");
                    if (settings != null) {
                        AnsReflectUtils.invokeMethod(settings, "setJavaScriptEnabled", new Class[]{boolean.class}, new Object[]{true});
                    }
                    addJavascriptInterface(HybridObject.this, WEB_OBJ);
                } catch (Throwable ignore) {
                    ExceptionUtil.exceptionThrow(ignore);
                }
                mInited = true;
            }
        };
        if (Thread.currentThread().getId() == mHandler.getLooper().getThread().getId()) {
            injectRunnable.run();
        } else {
            mHandler.post(injectRunnable);
        }
        if (!TextUtils.isEmpty(sInjectJsSdk)) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        String url = (String) AnsReflectUtils.invokeMethod(mWebView, "getUrl");
//                        if (TextUtils.isEmpty(url) || !TextUtils.equals(url, mCurrentUrl)) {
//                            mCurrentUrl = url;
                            if (!TextUtils.isEmpty(url)) {
                                loadUrl(sInjectJsSdk);
                            }
//                        }
                    } catch (Throwable ignore) {
                        ExceptionUtil.exceptionThrow(ignore);
                    }
                    if (mHandler != null) {
                        mHandler.postDelayed(this, 1000);
                    }
                }
            }, 1000);
        }
    }

    int getPageHashCode() {
        return mPageHashCode;
    }

    protected Object getView() {
        return mWebView;
    }

    void clear() {
        BaseWebViewInjector injector = WebViewInjectManager.getInstance().getInjector();
        if (injector != null) {
            injector.clearHybrid(getHashCode());
        }
        removeJavascriptInterface(WEB_OBJ);
        mHandler = null;
    }

    protected void clearWebView() {
        mWebView = null;
    }

    private boolean addJavascriptInterface(Object jsObj, String objName) {
        return callWebViewMethod(mWebView, mHandler, "addJavascriptInterface",
                new Class[]{Object.class, String.class}, new Object[]{jsObj, objName});
    }

    private boolean removeJavascriptInterface(String objName) {
        return callWebViewMethod(mWebView, mHandler, "removeJavascriptInterface",
                new Class[]{String.class}, new Object[]{objName});
    }

    boolean loadUrl(String url) {
        return callWebViewMethod(mWebView, mHandler, "loadUrl", new Class[]{String.class}, new Object[]{url});
    }

    private boolean callWebViewMethod(final Object webView, Handler handler, String methodName, Class[] paramTypes, final Object[] paramValues) {
        if (handler == null || webView == null) {
            return false;
        }
        final Method method = AnsReflectUtils.findMethod(webView.getClass(), methodName, paramTypes);
        if (method == null) {
            return false;
        }
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    method.invoke(webView, paramValues);
                } catch (Throwable ignore) {
                    ExceptionUtil.exceptionThrow(ignore);
                }
            }
        };
        if (Thread.currentThread().getId() == handler.getLooper().getThread().getId()) {
            runnable.run();
        } else {
            handler.post(runnable);
        }
        return true;
    }

    @JavascriptInterface
    public boolean isHybrid() {
        BaseWebViewInjector injector = WebViewInjectManager.getInstance().getInjector();
        if (injector != null) {
            return injector.isHybrid(getHashCode());
        }
        return true;
    }

    @JavascriptInterface
    public String getAppStartInfo() {
//        Map map = new HashMap();
//        map.put("is_appstart",true);
//        map.put("userId",UserInfo.getXho());
//        String json = map.toString();
        String sessionid = InternalAgent.getSessionId(AnalysysUtil.getContext());
        String res = "{\"is_appstart\":true,\"userId\":" + "\"" + UserInfo.getXho() + "\",\"sessionid\":" + "\"" + sessionid + "\"}";
        //String res = "{\"is_appstart\":true,\"userId\":" + "\"" + UserInfo.getXho() + "\"}";
        return res;
//        BaseWebViewInjector injector = WebViewInjectManager.getInstance().getInjector();
//        if (injector != null) {
//            return injector.getAppStartInfo(getHashCode());
//
//        }
//        return null;
    }

    @JavascriptInterface
    public void onVisualDomList(String info) {
        BaseWebViewInjector injector = WebViewInjectManager.getInstance().getInjector();
        if (injector != null) {
            injector.onVisualDomList(getHashCode(), info);
        }
    }

    @JavascriptInterface
    public void onProperty(String info) {
        BaseWebViewInjector injector = WebViewInjectManager.getInstance().getInjector();
        if (injector != null) {
            injector.onProperty(getHashCode(), info);
        }
    }

    @JavascriptInterface
    public void AnalysysAgentTrack(String eventId, String eventInfo, String extraEditInfo) {
        BaseWebViewInjector injector = WebViewInjectManager.getInstance().getInjector();
        if (injector != null) {
            injector.AnalysysAgentTrack(getHashCode(), eventId, eventInfo, extraEditInfo);
        }
    }

    @JavascriptInterface
    public String getEventList() {
        BaseWebViewInjector injector = WebViewInjectManager.getInstance().getInjector();
        if (injector != null) {
            return injector.getEventList(getHashCode());
        }
        return null;
    }

    @JavascriptInterface
    public String getProperty(String info) {
        BaseWebViewInjector injector = WebViewInjectManager.getInstance().getInjector();
        if (injector != null) {
            return injector.getProperty(getView(), info);
        }
        return null;
    }

    @JavascriptInterface
    public void analysysHybridCallNative(final String msg) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                HybridBridge.getInstance().execute(msg, mWebView);
            }
        };
        if (mHandler != null) {
            if (Thread.currentThread().getId() == mHandler.getLooper().getThread().getId()) {
                runnable.run();
            } else {
                mHandler.post(runnable);
            }
        } else {
            runnable.run();
        }
    }

    private static String sInjectJsSdk;

    /**
     * 注入测试
     */
    public static void setInjectJsSdk(String jsSdk) {
        sInjectJsSdk = jsSdk;
        if (!TextUtils.isEmpty(sInjectJsSdk)) {
            ActivityLifecycleUtils.addCallback(new ActivityLifecycleUtils.BaseLifecycleCallback() {

                private Set<Integer> mInjectPages = new HashSet<>();

                private void forceInject(final Activity activity) {
                    try {
                        final View contentView = activity.findViewById(android.R.id.content);
                        if (contentView == null) {
                            return;
                        }
                        final View rootView = contentView.getRootView();
                        ViewTreeObserver observer = rootView.getViewTreeObserver();
                        if (observer.isAlive()) {
                            observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                                @Override
                                public void onGlobalLayout() {
                                    boolean isAlive = WindowUIHelper.isRootViewAlive(rootView);
                                    if (isAlive) {
                                        WebViewInjectManager.getInstance().injectWebViewInPage(rootView);
                                    }
                                }
                            });
                        }
                    } catch (Throwable ignore) {
                        ExceptionUtil.exceptionThrow(ignore);
                    }
                }

                @Override
                public void onActivityResumed(Activity activity) {
                    try {
                        int pageHashCode = activity.getWindow().getDecorView().hashCode();
                        if (!mInjectPages.contains(pageHashCode)) {
                            mInjectPages.add(pageHashCode);
                            forceInject(activity);
                        }
                    } catch (Throwable ignore) {
                        ExceptionUtil.exceptionThrow(ignore);
                    }
                }

                @Override
                public void onActivityDestroyed(Activity activity) {
                    try {
                        int pageHashCode = activity.getWindow().getDecorView().hashCode();
                        if (mInjectPages.contains(pageHashCode)) {
                            mInjectPages.remove(pageHashCode);
                            WebViewInjectManager.getInstance().clearHybridInPage(pageHashCode);
                        }
                    } catch (Throwable ignore) {
                        ExceptionUtil.exceptionThrow(ignore);
                    }
                }
            });
        }
    }

}