package com.analysys.hybrid;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.analysys.AnalysysAgent;
import com.analysys.process.AgentProcess;
import com.analysys.utils.ANSLog;
import com.analysys.utils.ActivityLifecycleUtils;
import com.analysys.utils.AnalysysUtil;
import com.analysys.utils.AnsReflectUtils;
import com.analysys.utils.CommonUtils;
import com.analysys.utils.Constants;
import com.analysys.utils.ExceptionUtil;
import com.analysys.utils.SharedUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class HybridBridge extends ActivityLifecycleUtils.BaseLifecycleCallback {

    public static final String SCHEME = "analysysagent";
    public  String lastWebUrl = "";

    public static HybridBridge getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * process msg from JS
     *
     */
    public void execute(String info, Object webView) {
        try {

            if(!AgentProcess.getInstance().isDataCollectEnable()){
                return;
            }
            Context context = AnalysysUtil.getContext();
            JSONObject obj = new JSONObject(info);
            if (obj.length() > 0) {
                String functionName = obj.optString("functionName");
                JSONArray args = obj.optJSONArray("functionParams");
                //window.AnalysysAgent.nativeCallback(name, params)
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
                            Object.class);
                    method.invoke(Holder.INSTANCE, context, args, "window.AnalysysAgent." + callback, webView);
                }
            }
        } catch (Throwable ignore) {
            ExceptionUtil.exceptionThrow(ignore);
        }
    }

    private void getHybirdWebURL(Context context, JSONArray array) {
        if (context != null) {
            if (array.length() > 0) {
                JSONObject object = array.optJSONObject(0);
                Map urlMap = CommonUtils.jsonToMap(object);
                String webUrl = (String) urlMap.get("url");
               HybridBridge.getInstance().lastWebUrl = webUrl;

            }

        }
    }

    private void pageClose(Context context, JSONArray array) {
        if (context != null) {
            if (array.length() > 0) {
                try {
                    JSONObject object = array.optJSONObject(0);

                    AnalysysAgent.track(context,Constants.PAGE_CLOSE,CommonUtils.jsonToMap(object));
                } catch (Throwable ignore) {
                    ExceptionUtil.exceptionThrow(ignore);
                }

            }


        }
    }

    // 是否有保存page_close机制
    private boolean mSaveH5PageClose;
    private JSONObject mH5PageCloseSaveData;
    private final Object mH5PageCloseLoc = new Object();

    @SuppressWarnings("unused")
    private void saveH5PageClose(Context context, JSONArray array) {
        mSaveH5PageClose = true;
        synchronized (mH5PageCloseLoc) {
            try {
                mH5PageCloseSaveData = array.optJSONObject(1);
                mH5PageCloseSaveData.put(Constants.PV_START_TIME, System.currentTimeMillis());
                SharedUtil.setString(context, Constants.PAGE_CLOSE_H5_INFO,
                        new String(Base64.encode(String.valueOf(mH5PageCloseSaveData).getBytes(),
                                Base64.NO_WRAP)));
            } catch (Throwable e) {
                ExceptionUtil.exceptionPrint(e);
            }
        }
    }

    public void trySendSaveH5PageClose(Context context) {
        String txt = SharedUtil.getString(context, Constants.PAGE_CLOSE_H5_INFO, "");
        if (TextUtils.isEmpty(txt)) {
            return;
        }
        try {
            txt = new String(Base64.decode(txt.getBytes(), Base64.NO_WRAP));
            JSONObject jo = new JSONObject(txt);
            long startTime = jo.getLong(Constants.PV_START_TIME);
            String timeStr = SharedUtil.getString(context, Constants.LAST_OP_TIME, "");
            long time = CommonUtils.parseLong(timeStr, 0);
            long pageStayTime = time - startTime;
            if (pageStayTime > 0) {
                jo.put(Constants.PAGE_STAY_TIME, pageStayTime);
                jo.remove(Constants.PV_START_TIME);
                AgentProcess.getInstance().track(Constants.PAGE_CLOSE, CommonUtils.jsonToMap(jo), time);
            }
            SharedUtil.setString(context, Constants.PAGE_CLOSE_H5_INFO, null);
        } catch (Throwable e) {
            ExceptionUtil.exceptionPrint(e);
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

    private void loadUrl(Object webView, String url) {
        AnsReflectUtils.invokeMethod(webView, "loadUrl", new Class[]{String.class}, new Object[]{url});
    }

    @SuppressWarnings("unused")
    private void getSuperProperty(Context context,
                                  JSONArray array, String callBack, Object webView) {
        if (array != null && array.length() > 0) {
            String key = array.optString(0);
            if (!TextUtils.isEmpty(key)) {
                Object value = AnalysysAgent.getSuperProperty(context, key);
                if (value != null) {
                    loadUrl(webView, "javascript:" + callBack + "(" + "'getSuperProperty'" + "," + "'" + value.toString() + "'" + ")");
                    //loadUrl(webView, "javascript:" + callBack + "('" + value.toString() + "')");
                }
            }
        }
    }

    @SuppressWarnings("unused")
    private void getSuperProperties(Context context, JSONArray array, String callBack,
                                    Object webView) {
        Map<String, Object> res = AnalysysAgent.getSuperProperties(context);
        if (res != null && res.size() > 0) {
            loadUrl(webView, "javascript:" + callBack + "(" + "'getSuperProperties'" + "," + "'" + res.toString() + "'" + ")");
            //loadUrl(webView, "javascript:" + callBack + "('" + res.toString() + "')");
        }

    }

    private void getPresetProperties(Context context, JSONArray array, String callBack, Object webView) {
        Map<String, Object> res = AnalysysAgent.getPresetProperties(context);
        if (res != null && res.size() > 0) {
            loadUrl(webView, "javascript:" + callBack + "(" + "'getPresetProperties'" + "," + "'" + res.toString() + "'" + ")");
            //loadUrl(webView, "javascript:" + callBack + "('" + res.toString() + "')");
        }
    }

    private void getDistinctId(Context context, JSONArray array, String callBack, Object webView) {
        String res = AnalysysAgent.getDistinctId(context);
        if (!TextUtils.isEmpty(res)) {
            loadUrl(webView, "javascript:" + callBack + "(" + "'getDistinctId'" + "," + "'" + res + "'" + ")");

            //loadUrl(webView, "javascript:" + callBack + "('" + res + "')");
        }
    }

    @SuppressWarnings("unused")
    private void pageView(Context context, JSONArray array) {

        if (array.length() > 1) {
            try {
                String pageName = array.optString(0);
                JSONObject obj = array.optJSONObject(1);
                String referrer = obj.optString(Constants.PAGE_REFERRER, null);
                if (TextUtils.isEmpty(referrer)) {
                    String pRef = SharedUtil.getString(AnalysysUtil.getContext(), Constants.SP_REFER, "");
                    if (!TextUtils.isEmpty(pRef)) {
                        obj.put(Constants.PAGE_REFERRER, pRef);
                    }
                }
                Map<String, Object> map = convertToMap(obj);
                AgentProcess.getInstance().hybridPageView(pageName, map);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        if (mSaveH5PageClose) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    synchronized (mH5PageCloseLoc) {
                        if (mH5PageCloseSaveData != null) {
                            try {
                                // 被杀死也可能调onActivityDestroyed，但是发送数据是失败的，导致后面的逻辑出问题
                                Thread.sleep(200);
                                long startTime = mH5PageCloseSaveData.getLong(Constants.PV_START_TIME);
                                long time = System.currentTimeMillis();
                                long pageStayTime = time - startTime;
                                if (pageStayTime > 0) {
                                    mH5PageCloseSaveData.put(Constants.PAGE_STAY_TIME, pageStayTime);
                                    mH5PageCloseSaveData.remove(Constants.PV_START_TIME);
                                    AgentProcess.getInstance().track(Constants.PAGE_CLOSE, CommonUtils.jsonToMap(mH5PageCloseSaveData), time);
                                }
                                mH5PageCloseSaveData = null;
                                SharedUtil.setString(AnalysysUtil.getContext(), Constants.PAGE_CLOSE_H5_INFO, null);
                                Log.i("ssss", "onActivityDestroyed");
                            } catch (Throwable e) {
                                ExceptionUtil.exceptionPrint(e);
                            }
                        }
                    }
                }
            }).start();
        }
    }

    @SuppressWarnings("unused")
    private void track(Context context, JSONArray array) {
        String eventName = array.optString(0);
        if (mSaveH5PageClose && Constants.PAGE_CLOSE.equals(eventName)) {
            synchronized (mH5PageCloseLoc) {
                if (mH5PageCloseSaveData != null) {
                    SharedUtil.setString(context, Constants.PAGE_CLOSE_H5_INFO, null);
                    mH5PageCloseSaveData = null;
                } else {
                    return;
                }
            }
        }
        if (array.length() > 1) {
            JSONObject eventInfo = array.optJSONObject(1);
            Map<String, Object> map = convertToMap(eventInfo);
            AnalysysAgent.track(context, eventName, map);
        } else {
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
        if (array != null) {
            if (array.length() == 2) {
                String aliasId = array.optString(0);
                String originalId = array.optString(1);
                AnalysysAgent.alias(context, aliasId, originalId);
            } else if (array.length() == 1) {
                String aliasId = array.optString(0);
                AnalysysAgent.alias(context, aliasId);
            }
        }
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
        ActivityLifecycleUtils.addCallback(this);
    }
}
