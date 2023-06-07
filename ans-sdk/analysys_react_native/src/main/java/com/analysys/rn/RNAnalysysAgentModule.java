package com.analysys.rn;


import android.app.Activity;
import android.text.TextUtils;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;

import com.analysys.AnalysysAgent;
import com.analysys.allgro.plugin.ASMProbeHelp;
import com.analysys.process.AgentProcess;
import com.analysys.process.HeatMap;
import com.analysys.push.PushListener;
import com.analysys.utils.ANSLog;
import com.analysys.utils.ActivityLifecycleUtils;
import com.analysys.utils.AnalysysUtil;
import com.analysys.utils.Constants;
import com.analysys.utils.ExceptionUtil;
import com.analysys.visual.bind.VisualBindManager;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * String -> String
 * ReadableMap -> Object
 * Boolean -> Bool
 * Integer -> Number
 * Double -> Number
 * Float -> Number
 * Callback -> function
 * ReadableArray -> Array
 */

public class RNAnalysysAgentModule extends ReactContextBaseJavaModule {

    public RNAnalysysAgentModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    private static final String AnalysysNetworkNONE = "networkNONE";
    private static final String AnalysysNetworkWWAN = "networkWWAN";
    private static final String AnalysysNetworkWIFI = "networkWIFI";
    private static final String AnalysysNetworkALL = "networkALL";


    private static final String MODULE_NAME = "RNAnalysysAgentModule";

    /**
     * 返回一个字符串名字，这个名字在 JavaScript (RN)端标记这个模块。
     */
    @Override
    public String getName() {
        return MODULE_NAME;
    }

    private HashMap convertToHashMap(ReadableMap map) {
        if (map == null) {
            return null;
        }
        return map.toHashMap();
    }

    @ReactMethod
    public void setDebugMode(int debugMode) {
        AnalysysAgent.setDebugMode(getReactApplicationContext(), debugMode);
    }

    @ReactMethod
    public void setUploadURL(String url) {
        AnalysysAgent.setUploadURL(getReactApplicationContext(), url);
    }

    @ReactMethod
    public void setAutomaticCollection(boolean isAuto) {
        AnalysysAgent.setAutomaticCollection(getReactApplicationContext(), isAuto);
    }

    @ReactMethod
    public void setIntervalTime(long flushInterval) {
        AnalysysAgent.setIntervalTime(getReactApplicationContext(), flushInterval);
    }

    @ReactMethod
    public void setMaxEventSize(long eventSize) {
        AnalysysAgent.setMaxEventSize(getReactApplicationContext(), eventSize);
    }

    @ReactMethod
    public void setMaxCacheSize(long cacheSize) {
        AnalysysAgent.setMaxCacheSize(getReactApplicationContext(), cacheSize);
    }

    @ReactMethod
    public void launchSource(int source) {
        AnalysysAgent.launchSource(source);
    }

    @ReactMethod
    public void setPageViewBlackListByPages(ReadableArray list) {
        ArrayList<Object> arrayList = list.toArrayList();

        List<String> listArr = new ArrayList<>();
        for (int i = 0; i < arrayList.size(); i++) {
            listArr.add((String) arrayList.get(i));
        }
        AnalysysAgent.setPageViewBlackListByPages(listArr);
    }

    @ReactMethod
    public void flush() {
        AnalysysAgent.flush(getReactApplicationContext());
    }

    @ReactMethod
    public void cleanDBCache() {
        AnalysysAgent.cleanDBCache();
    }

    @ReactMethod
    public void setPush(String provider, String pushId) {
        AnalysysAgent.setPushID(getReactApplicationContext(), provider, pushId);
    }

    @ReactMethod
    public void trackCampaign(String userInfo, Boolean isClick, final Callback callback) {
        AnalysysAgent.trackCampaign(getReactApplicationContext(), userInfo, isClick, new PushListener() {
            @Override
            public void execute(String action, String jsonParams) {
                callback.invoke(action, jsonParams);
            }
        });
    }

    @ReactMethod
    public void pageViewWithArgsAuto(String pageName, ReadableMap properties) {
        try {
            if (AgentProcess.getInstance().getConfig().isAutoTrackPageView()) {
                pageViewWithArgs(pageName, properties);
            }
        } catch (Exception e) {
            ExceptionUtil.exceptionPrint(e);
        }
    }

    @ReactMethod
    public void pageViewWithArgs(String pageName, ReadableMap properties) {
        try {
            HashMap<String, Object> hashMap = convertToHashMap(properties);
            if (hashMap == null) {
                AnalysysUtil.setRNUrl(pageName);
                AnalysysAgent.pageView(this.getReactApplicationContext(), pageName);
            } else {
                String url = (String) hashMap.get(Constants.PAGE_URL);
                if (!TextUtils.isEmpty(url)) {
                    AnalysysUtil.setRNUrl(url);
                } else {
                    AnalysysUtil.setRNUrl(pageName);
                }
                AnalysysAgent.pageView(this.getReactApplicationContext(), pageName, hashMap);
            }
        } catch (Exception e) {
            ExceptionUtil.exceptionPrint(e);
        }
    }

    @ReactMethod
    public void pageView(String pageName) {
        try {
            AnalysysUtil.setRNUrl(pageName);
            AnalysysAgent.pageView(this.getReactApplicationContext(), pageName);
        } catch (Exception e) {
            ExceptionUtil.exceptionPrint(e);
        }
    }

    @ReactMethod
    public void track(String eventName) {
        try {
            AnalysysAgent.track(this.getReactApplicationContext(), eventName);
        } catch (Exception e) {
            ExceptionUtil.exceptionPrint(e);
        }
    }

    @ReactMethod
    public void trackWithArgs(String eventName, ReadableMap properties) {
        try {
            HashMap<String, Object> hashMap = convertToHashMap(properties);
            if (hashMap == null) {
                AnalysysAgent.track(this.getReactApplicationContext(), eventName);
            } else {
                AnalysysAgent.track(this.getReactApplicationContext(), eventName, hashMap);
            }
        } catch (Exception e) {
            ExceptionUtil.exceptionPrint(e);
        }
    }

    @ReactMethod
    public void identify(String distinctId) {
        try {
            AnalysysAgent.identify(this.getReactApplicationContext(), distinctId);
        } catch (Exception e) {
            ExceptionUtil.exceptionPrint(e);
        }
    }

    @ReactMethod
    public void alias(String aliasId) {
        try {
            AnalysysAgent.alias(this.getReactApplicationContext(), aliasId);
        } catch (Exception e) {
            ExceptionUtil.exceptionPrint(e);
        }
    }

    @ReactMethod
    public void getDistinctId(Callback callbackFn) {
        try {
            String id = AnalysysAgent.getDistinctId(this.getReactApplicationContext());
            if (id == null) {
                callbackFn.invoke("");
            } else {
                callbackFn.invoke(id);
            }
        } catch (Exception e) {
            ExceptionUtil.exceptionPrint(e);
        }
    }

    @ReactMethod
    public void profileSet(ReadableMap properties) {
        try {
            HashMap<String, Object> hashMap = convertToHashMap(properties);
            AnalysysAgent.profileSet(this.getReactApplicationContext(), hashMap);
        } catch (Exception e) {
            ExceptionUtil.exceptionPrint(e);
        }
    }

    @ReactMethod
    public void profileSetOnce(ReadableMap properties) {
        try {
            HashMap<String, Object> hashMap = convertToHashMap(properties);
            AnalysysAgent.profileSetOnce(this.getReactApplicationContext(), hashMap);
        } catch (Exception e) {
            ExceptionUtil.exceptionPrint(e);
        }
    }

    @ReactMethod
    public void profileIncrement(ReadableMap properties) {
        try {
            Map<String, Number> hashMap = convertToHashMap(properties);
            AnalysysAgent.profileIncrement(this.getReactApplicationContext(), hashMap);
        } catch (Exception e) {
            ExceptionUtil.exceptionPrint(e);
        }
    }

    @ReactMethod
    public void profileAppend(ReadableMap properties) {
        try {
            HashMap<String, Object> hashMap = convertToHashMap(properties);
            AnalysysAgent.profileAppend(this.getReactApplicationContext(), hashMap);
        } catch (Exception e) {
            ExceptionUtil.exceptionPrint(e);
        }
    }

    @ReactMethod
    public void profileUnset(String profileKey) {
        try {
            AnalysysAgent.profileUnset(this.getReactApplicationContext(), profileKey);
        } catch (Exception e) {
            ExceptionUtil.exceptionPrint(e);
        }
    }

    @ReactMethod
    public void profileDelete() {
        try {
            AnalysysAgent.profileDelete(this.getReactApplicationContext());
        } catch (Exception e) {
            ExceptionUtil.exceptionPrint(e);
        }
    }

    @ReactMethod
    public void registerSuperProperty(String superPropertyName, String superPropertyValue) {
        try {
            AnalysysAgent.registerSuperProperty(this.getReactApplicationContext(), superPropertyName, superPropertyValue);
        } catch (Exception e) {
            ExceptionUtil.exceptionPrint(e);
        }
    }

    @ReactMethod
    public void registerSuperProperties(ReadableMap properties) {
        try {
            HashMap<String, Object> hashMap = convertToHashMap(properties);
            AnalysysAgent.registerSuperProperties(this.getReactApplicationContext(), hashMap);
        } catch (Exception e) {
            ExceptionUtil.exceptionPrint(e);
        }
    }

    @ReactMethod
    public void unRegisterSuperProperty(String superPropertyName) {
        try {
            AnalysysAgent.unRegisterSuperProperty(this.getReactApplicationContext(), superPropertyName);
        } catch (Exception e) {
            ExceptionUtil.exceptionPrint(e);
        }
    }

    @ReactMethod
    public void clearSuperProperties() {
        try {
            AnalysysAgent.clearSuperProperties(this.getReactApplicationContext());
        } catch (Exception e) {
            ExceptionUtil.exceptionPrint(e);
        }
    }

    @ReactMethod
    public void getSuperProperty(String superPropertyName, Callback callbackFn) {
        try {
            Object propertyValue = AnalysysAgent.getSuperProperty(this.getReactApplicationContext(), superPropertyName);
            if (propertyValue == null) {
                callbackFn.invoke("");
            } else {
                callbackFn.invoke(propertyValue);
            }
        } catch (Exception e) {
            ExceptionUtil.exceptionPrint(e);
        }
    }

    @ReactMethod
    public void getSuperProperties(Callback callbackFn) {
        try {
            Map<String, Object> properties = AnalysysAgent.getSuperProperties(this.getReactApplicationContext());
            if (properties == null) {
                callbackFn.invoke("");
            } else {
                callbackFn.invoke(properties.toString());
            }
        } catch (Exception e) {
            ExceptionUtil.exceptionPrint(e);
        }
    }

    @ReactMethod
    public void getPresetProperties(Callback callbackFn) {
        try {
            Map<String, Object> properties = AnalysysAgent.getPresetProperties(this.getReactApplicationContext());
            if (properties == null) {
                callbackFn.invoke("");
            } else {
                callbackFn.invoke(properties.toString());
            }
        } catch (Exception e) {
            ExceptionUtil.exceptionPrint(e);
        }
    }

    @ReactMethod
    public void reset() {
        try {
            AnalysysAgent.reset(this.getReactApplicationContext());
        } catch (Exception e) {
            ExceptionUtil.exceptionPrint(e);
        }
    }

    @ReactMethod
    public void setUploadNetworkType(int networkType) {
        try {
            AnalysysAgent.setUploadNetworkType(networkType);
        } catch (Exception e) {
            ExceptionUtil.exceptionPrint(e);
        }
    }

    @Override
    public Map<String, Object> getConstants() {
        final Map<String, Object> constants = new HashMap<>();
        constants.put(AnalysysNetworkNONE, 0);
        constants.put(AnalysysNetworkWWAN, 1 << 1);
        constants.put(AnalysysNetworkWIFI, 1 << 2);
        constants.put(AnalysysNetworkALL, 0xFF);
        return constants;
    }

    @ReactMethod
    public void setViewClickableMap(ReadableMap map) {
        HashMap<String, Object> hashMap = convertToHashMap(map);
        ANSLog.i("RN setViewClickableMap map = " + (hashMap == null ? "null" : hashMap));
        AnalysysUtil.setRNViewClickableMap(hashMap);
        VisualBindManager.getInstance().rebindCurrentActivityView();
        HeatMap.tryHookClick();
    }

    @ReactMethod
    public void viewClicked(int viewId) {
        try {
            ANSLog.i("RN viewClicked " + viewId);
            Activity activity = ActivityLifecycleUtils.getCurrentActivity();
            if (activity != null) {
                final View view = activity.findViewById(viewId);
                ANSLog.i("RN viewClicked view = " + view);
                if (view != null) {
                    ASMProbeHelp.getInstance().trackViewOnClick(view, false);
                    VisualBindManager.getInstance().newAccessibilityEvent(view, AccessibilityEvent.TYPE_VIEW_CLICKED, true);
                }
            }
        } catch (Throwable ignore) {
            ExceptionUtil.exceptionPrint(ignore);
        }
    }
}

