package com.analysys.utils;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;

public class ActivityLifecycleUtils {

    private static boolean sInited;
    private static WeakReference<Activity> sCurrentActivityRef;
    private static ActivityInfo sCurrentActivityInfo = new ActivityInfo();

    private static Set<BaseLifecycleCallback> sAllCallbacks = new HashSet<>();

    public static class ActivityInfo {
        public String simpleName;
        public String name;
        public String canonicalName;
        public Class<?> clz;

        void clear() {
            simpleName = null;
            name = null;
            canonicalName = null;
            clz = null;
        }
    }

    public static abstract class BaseLifecycleCallback implements Application.ActivityLifecycleCallbacks {
        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        }

        @Override
        public void onActivityStarted(Activity activity) {
        }

        @Override
        public void onActivityResumed(Activity activity) {
        }

        @Override
        public void onActivityPaused(Activity activity) {
        }

        @Override
        public void onActivityStopped(Activity activity) {
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
        }
    }

    public static void addCallback(BaseLifecycleCallback callback) {
        if (!sInited) {
            throw new RuntimeException("call ActivityLifecycleUtils.initLifecycle first");
        }
        sAllCallbacks.add(callback);
    }

    public static void removeCallback(BaseLifecycleCallback callback) {
        if (!sInited) {
            throw new RuntimeException("call ActivityLifecycleUtils.initLifecycle first");
        }
        sAllCallbacks.remove(callback);
    }

    private static Application.ActivityLifecycleCallbacks sCalback = new Application.ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            for (BaseLifecycleCallback callback : sAllCallbacks) {
                callback.onActivityCreated(activity, savedInstanceState);
            }
        }

        @Override
        public void onActivityStarted(Activity activity) {
            for (BaseLifecycleCallback callback : sAllCallbacks) {
                callback.onActivityStarted(activity);
            }
        }

        @Override
        public void onActivityResumed(Activity activity) {
            sCurrentActivityRef = new WeakReference<>(activity);

            Class<?> clz = activity.getClass();
            sCurrentActivityInfo.simpleName = clz.getSimpleName();
            sCurrentActivityInfo.name = clz.getName();
            sCurrentActivityInfo.canonicalName = clz.getCanonicalName();
            sCurrentActivityInfo.clz = clz;

            for (BaseLifecycleCallback callback : sAllCallbacks) {
                callback.onActivityResumed(activity);
            }
        }

        @Override
        public void onActivityPaused(Activity activity) {
            if (sCurrentActivityRef != null) {
                sCurrentActivityRef.clear();
            }
            sCurrentActivityInfo.clear();

            for (BaseLifecycleCallback callback : sAllCallbacks) {
                callback.onActivityPaused(activity);
            }
        }

        @Override
        public void onActivityStopped(Activity activity) {
            for (BaseLifecycleCallback callback : sAllCallbacks) {
                callback.onActivityStopped(activity);
            }
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
            for (BaseLifecycleCallback callback : sAllCallbacks) {
                callback.onActivitySaveInstanceState(activity, outState);
            }
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            for (BaseLifecycleCallback callback : sAllCallbacks) {
                callback.onActivityDestroyed(activity);
            }
        }
    };

    public static void initLifecycle() {
        sInited = true;
        Context context = AnalysysUtil.getContext();
        if(context instanceof Application){
            ((Application) context).registerActivityLifecycleCallbacks(sCalback);
        }
    }

    public static Activity getCurrentActivity() {
        if (sCurrentActivityRef != null) {
            return sCurrentActivityRef.get();
        }
        return null;
    }

}
