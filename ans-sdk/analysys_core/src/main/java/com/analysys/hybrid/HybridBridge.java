package com.analysys.hybrid;

import android.content.Context;
import android.text.TextUtils;
import android.webkit.WebView;

import com.analysys.AnalysysAgent;
import com.analysys.process.AgentProcess;
import com.analysys.utils.AnalysysUtil;
import com.analysys.utils.CommonUtils;
import com.analysys.utils.Constants;
import com.analysys.utils.ExceptionUtil;
import com.analysys.utils.SharedUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class HybridBridge {

    private static final String SCHEME = "analysysagent";

    public static HybridBridge getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * process msg from JS
     *
     */
    public void execute(String url, Object webView) {
        try {
            Context context = AnalysysUtil.getContext();
            if (context != null && url.startsWith(SCHEME)) {
                String info = url.substring((SCHEME.length() + 1));
                JSONObject obj = new JSONObject(info);
                if (obj.length() > 0) {
                    String functionName = obj.optString("functionName");
                    JSONArray args = obj.optJSONArray("functionParams");
                    String callback = obj.optString("callbackFunName");
                    //call the method
                    Class<HybridBridge> classType = HybridBridge.class;
                    if (TextUtils.isEmpty(callback)) {
                        Method method = classType.getDeclaredMethod(
                                functionName,
                                Context.class,
                                JSONArray.class);
                        method.invoke(Holder.INSTANCE, context, args);
                    } else {
                        Method method = classType.getDeclaredMethod(
                                functionName,
                                Context.class,
                                JSONArray.class,
                                String.class,
                                WebView.class);
                        method.invoke(Holder.INSTANCE, context, args, callback, webView);
                    }
                }
            }
        } catch (Throwable ignored) {
            ExceptionUtil.exceptionThrow(ignored);
        }
    }

    @SuppressWarnings("unused")
    private void registerSuperProperty(Context context, JSONArray array) {
        if (context != null) {
            if (array != null && array.length() > 0) {
                String key = array.optString(0);
                Object value = array.opt(1);
                AgentProcess.getInstance().registerJsSuperProperty(key,value);
            }
        }
    }

    @SuppressWarnings("unused")
    private void registerSuperProperties(Context context, JSONArray array) {
        if (context != null) {
            if (array != null && array.length() > 0) {
                JSONObject obj = array.optJSONObject(0);
                if (obj != null && obj.length() > 0) {
                    Map<String, Object> map = convertToMap(obj);
                    AgentProcess.getInstance().registerJsSuperProperties(map);
                }
            }
        }
    }

    @SuppressWarnings("unused")
    private void unRegisterSuperProperty(Context context, JSONArray array) {
        if (array != null && array.length() > 0) {
            String key = array.optString(0);
            if (!TextUtils.isEmpty(key)) {
                AgentProcess.getInstance().unregisterJsSuperProperty(key);
            }
        }
    }


    @SuppressWarnings("unused")
    private void profileAppend(Context context, JSONArray array) {
        if (array != null && array.length() > 0) {
            if (array.length() == 2) {
                String key = array.optString(0);
                Object value = array.opt(1);
                AnalysysAgent.profileAppend(context, key, value);
            } else {
                JSONObject obj = array.optJSONObject(0);
                if (obj != null && obj.length() > 0) {
                    Map<String, Object> map = convertToMap(obj);
                    AnalysysAgent.profileAppend(context, map);
                }
            }
        }
    }

    @SuppressWarnings("unused")
    private void profileSet(Context context, JSONArray array) {
        if (array != null && array.length() > 0) {
            if (array.length() == 2) {
                String key = array.optString(0);
                Object value = array.opt(1);
                AnalysysAgent.profileSet(context, key, value);
            } else {
                JSONObject obj = array.optJSONObject(0);
                if (obj != null && obj.length() > 0) {
                    Map<String, Object> map = convertToMap(obj);
                    AnalysysAgent.profileSet(context, map);
                }
            }
        }
    }

    @SuppressWarnings("unused")
    private void profileIncrement(Context context, JSONArray array) {
        if (array != null && array.length() > 0) {
            if (array.length() == 2) {
                String key = array.optString(0);
                Number value = (Number) array.opt(1);
                AnalysysAgent.profileIncrement(context, key, value);
            } else {
                JSONObject obj = array.optJSONObject(0);
                if (obj != null && obj.length() > 0) {
                    Map<String, Number> map = convertToNumberMap(obj);
                    AnalysysAgent.profileIncrement(context, map);
                }
            }
        }
    }

    @SuppressWarnings("unused")
    private void profileSetOnce(Context context, JSONArray array) {
        if (array != null && array.length() > 0) {
            if (array.length() == 2) {
                String key = array.optString(0);
                Object value = array.opt(1);
                AnalysysAgent.profileSetOnce(context, key, value);
            } else {
                JSONObject obj = array.optJSONObject(0);
                if (obj != null && obj.length() > 0) {
                    Map<String, Object> map = convertToMap(obj);
                    AnalysysAgent.profileSetOnce(context, map);
                }
            }
        }
    }

    @SuppressWarnings("unused")
    private void clearSuperProperties(Context context, JSONArray array) {
        AgentProcess.getInstance().clearJsSuperProperty();
    }

    @SuppressWarnings("unused")
    private void reset(Context context, JSONArray array) {
        AnalysysAgent.reset(context);
    }

    @SuppressWarnings("unused")
    private void profileDelete(Context context, JSONArray array) {
        AnalysysAgent.profileDelete(context);
    }

    @SuppressWarnings("unused")
    private void profileUnset(Context context, JSONArray array) {
        if (array != null && array.length() > 0) {
            String key = array.optString(0);
            if (!TextUtils.isEmpty(key)) {
                AnalysysAgent.profileUnset(context, key);
            }
        }
    }

    @SuppressWarnings("unused")
    private void getSuperProperty(Context context,
                                  JSONArray array, String callBack, WebView webView) {
        if (array != null && array.length() > 0) {
            String key = array.optString(0);
            if (!TextUtils.isEmpty(key)) {
                Object value = AnalysysAgent.getSuperProperty(context, key);
                if (value != null) {
                    webView.loadUrl("javascript:" + callBack + "('" + value.toString() + "')");
                }
            }
        }
    }

    @SuppressWarnings("unused")
    private void getSuperProperties(Context context, JSONArray array, String callBack,
                                    WebView webView) {
        Map<String, Object> res = AnalysysAgent.getSuperProperties(context);
        if (res != null && res.size() > 0) {
            webView.loadUrl("javascript:" + callBack + "('" + res.toString() + "')");
        }
    }

    private void getDistinctId(Context context, JSONArray array, String callBack, WebView webView) {
        String res = AnalysysAgent.getDistinctId(context);
        if (!TextUtils.isEmpty(res)) {
            webView.loadUrl("javascript:" + callBack + "('" + res + "')");
        }
    }

    @SuppressWarnings("unused")
    private void pageView(Context context, JSONArray array) {
        if (array.length() > 1) {
            String pageName = array.optString(0);
            JSONObject obj = array.optJSONObject(1);
            Map<String, Object> map = convertToMap(obj);
            AgentProcess.getInstance().hybridPageView(pageName, map);
        } else {
            String pageName = array.optString(0);
            if (!TextUtils.isEmpty(pageName)) {
                AnalysysAgent.pageView(context, pageName);
            }
        }
    }

    @SuppressWarnings("unused")
    private void track(Context context, JSONArray array) {
        if (array.length() > 1) {
            String eventName = array.optString(0);
            JSONObject eventInfo = array.optJSONObject(1);
            Map<String, Object> map = convertToMap(eventInfo);
            AnalysysAgent.track(context, eventName, map);
        } else {
            String eventName = array.optString(0);
            if (!TextUtils.isEmpty(eventName)) {
                AnalysysAgent.track(context, eventName);
            }
        }
    }

    @SuppressWarnings("unused")
    private void identify(Context context, JSONArray array) {
        String distinctId = array.optString(0);
        if (!TextUtils.isEmpty(distinctId)) {
            AnalysysAgent.identify(context, distinctId);
        }
    }

    @SuppressWarnings("unused")
    private void alias(Context context, JSONArray array) {
        String aliasId = array.optString(0);
        String originalId = array.optString(1);
        AnalysysAgent.alias(context, aliasId, originalId);
    }

    /**
     * convert JSONObject to Map
     */
    private Map<String, Object> convertToMap(JSONObject obj) {
        Map<String, Object> res = new HashMap<>();
        if (obj != null && obj.length() > 0) {
            Iterator<String> it = obj.keys();
            while (it.hasNext()) {
                final String key = it.next();
                final Object o = obj.opt(key);
                res.put(key, o);
            }
        }
        return res;
    }

    private Map<String, Number> convertToNumberMap(JSONObject obj) {
        Map<String, Number> res = new HashMap<>();
        if (obj != null && obj.length() > 0) {
            Iterator<String> it = obj.keys();
            while (it.hasNext()) {
                final String key = it.next();
                final Number o = (Number) obj.opt(key);
                res.put(key, o);
            }
        }
        return res;
    }

    private static class Holder {
        private static final HybridBridge INSTANCE = new HybridBridge();
    }

    private HybridBridge() {
    }
}
