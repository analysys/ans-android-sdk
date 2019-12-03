package com.analysys.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;

import androidx.annotation.Nullable;

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
        if (context != null) {
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
        } catch (Exception ignored) {
        }
        return null;
    }

    // -------- 弱引用保存当前AC ---------------------
    private static WeakReference<Activity> mActivity;

    public static void onActivityCreated(Activity activity) {
        mActivity = new WeakReference<>(activity);
    }

    public static synchronized @Nullable Activity getCurActivity() {
        if (mActivity != null) {
            return mActivity.get();
        }
        return null;
    }

}
