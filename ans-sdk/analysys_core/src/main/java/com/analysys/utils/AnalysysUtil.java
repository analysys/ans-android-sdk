package com.analysys.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Description:静态工具箱门面类
 * Author: fengzeyuan
 * Date: 2019-10-29 14:30
 * Version: 1.0
 */
public class AnalysysUtil {

    @SuppressLint("StaticFieldLeak")
    private static Context sContext;

    public static void init(Context context) {
        if (context != null && sContext == null) {
            sContext = context;
            Context appContext = context.getApplicationContext();
            if (appContext != null) {
                sContext = appContext;
            }
        }
        if (sContext == null) {
            sContext = getApplication();
        }
    }

    public static Context getContext() {
        if (sContext == null) {
            sContext = getApplication();
        }
        return sContext;
    }

    private static Application getApplication() {
        try {
            Class<?> aClass = Class.forName(Class.class.getName());
            Method method = aClass.getMethod("forName", String.class);
            Class<?> activityThread = (Class<?>) method.invoke(null, "android.app.ActivityThread");
            if (activityThread != null) {
                Object at = activityThread.getMethod("currentActivityThread").invoke(null);
                Object app = activityThread.getMethod("getApplication").invoke(at);
                if (app != null) {
                    return (Application) app;
                }
            }
        } catch (Exception ignore) {
            ExceptionUtil.exceptionThrow(ignore);
        }
        return null;
    }

    // -------- 弱引用保存当前AC ---------------------
    private static WeakReference<Activity> mActivity;

    public static void onActivityCreated(Activity activity) {
        mActivity = new WeakReference<>(activity);
    }

    public static synchronized Activity getCurActivity() {
        if (mActivity != null) {
            return mActivity.get();
        }
        return null;
    }

    // RN clickable view map
    private static HashMap<String, Object> sRNClickabeMap;

    // RN current page
    private static String sRNUrl;

    public static void setRNUrl(String url) {
        sRNUrl = url;
    }

    public static String getRNUrl() {
        return sRNUrl;
    }

    public static boolean isRNPage() {
        Activity activity = ActivityLifecycleUtils.getCurrentActivity();
        if (activity == null) {
            return false;
        }
        try {
            Class<?> ReactActivityClz = Class.forName("com.facebook.react.ReactActivity");
            if (ReactActivityClz.isAssignableFrom(activity.getClass())) {
                return true;
            }
        } catch (Throwable e) {
        }
        return false;
    }

    public static void setRNViewClickableMap(HashMap<String, Object> map) {
        sRNClickabeMap = map;
    }

    public static Boolean getRNViewClickable(View v) {
        if (sRNClickabeMap == null) {
            return null;
        }
        try {
            Map<String, Boolean> map = (Map<String, Boolean>) sRNClickabeMap.get(v.getId() + "");
            if (map != null) {
                return map.get("isClick");
            }
        } catch (Exception ignore) {
            ExceptionUtil.exceptionPrint(ignore);
        }
        return null;
    }

    private static Class sRNGroupClz;
    static {
        try {
            sRNGroupClz = Class.forName("com.facebook.react.views.view.ReactViewGroup");
        } catch (Throwable e) {
        }
    }

    public static boolean isEmptyRNGroup(View child) {
        if (sRNGroupClz == null) {
            return false;
        }
        try {
            if (sRNGroupClz.isAssignableFrom(child.getClass())) {
                ViewGroup vg = (ViewGroup) child;
                if (vg.getChildCount() == 0) {
                    return true;
                }
            }
        } catch (Throwable e) {
        }
        return false;
    }

}
