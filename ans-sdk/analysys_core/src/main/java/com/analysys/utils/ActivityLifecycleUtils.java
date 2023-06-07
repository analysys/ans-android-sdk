package com.analysys.utils;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;

/**
 * @Copyright © 2019 Analysys Inc. All rights reserved.
 * @Description: activity生命周期utils类
 * @Create: 2019-12-05 17:43
 * @author: hcq
 */
public class ActivityLifecycleUtils {

    private static WeakReference<Activity> sCurrentActivityRef;

    private static final Set<BaseLifecycleCallback> sAllCallbacks = new HashSet<>();

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
        synchronized (sAllCallbacks) {
            sAllCallbacks.add(callback);
        }
    }

    public static void removeCallback(BaseLifecycleCallback callback) {
        synchronized (sAllCallbacks) {
            sAllCallbacks.remove(callback);
        }
    }

    private static BaseLifecycleCallback[] getCallbacks() {
        synchronized (sAllCallbacks) {
            BaseLifecycleCallback[] callbacks = new BaseLifecycleCallback[sAllCallbacks.size()];
            return sAllCallbacks.toArray(callbacks);
        }
    }

    private static Application.ActivityLifecycleCallbacks sCalback = new Application.ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            BaseLifecycleCallback[] callbacks = getCallbacks();
            for (BaseLifecycleCallback callback : callbacks) {
                callback.onActivityCreated(activity, savedInstanceState);
            }
        }

        @Override
        public void onActivityStarted(Activity activity) {
            BaseLifecycleCallback[] callbacks = getCallbacks();
            for (BaseLifecycleCallback callback : callbacks) {
                callback.onActivityStarted(activity);
            }
        }

        @Override
        public void onActivityResumed(Activity activity) {
            sCurrentActivityRef = new WeakReference<>(activity);

            BaseLifecycleCallback[] callbacks = getCallbacks();
            for (BaseLifecycleCallback callback : callbacks) {
                callback.onActivityResumed(activity);
            }
        }

        @Override
        public void onActivityPaused(Activity activity) {
            if (sCurrentActivityRef != null) {
                sCurrentActivityRef.clear();
                sCurrentActivityRef = null;
            }

            BaseLifecycleCallback[] callbacks = getCallbacks();
            for (BaseLifecycleCallback callback : callbacks) {
                callback.onActivityPaused(activity);
            }
        }

        @Override
        public void onActivityStopped(Activity activity) {
            BaseLifecycleCallback[] callbacks = getCallbacks();
            for (BaseLifecycleCallback callback : callbacks) {
                callback.onActivityStopped(activity);
            }
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
            BaseLifecycleCallback[] callbacks = getCallbacks();
            for (BaseLifecycleCallback callback : callbacks) {
                callback.onActivitySaveInstanceState(activity, outState);
            }
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            BaseLifecycleCallback[] callbacks = getCallbacks();
            for (BaseLifecycleCallback callback : callbacks) {
                callback.onActivityDestroyed(activity);
            }
        }
    };

    public static void initLifecycle() {
        Context context = AnalysysUtil.getContext();
        if (context instanceof Application) {
            ((Application) context).registerActivityLifecycleCallbacks(sCalback);
        }
    }

    public static void releaseLifecycle() {
        Context context = AnalysysUtil.getContext();
        if (context instanceof Application) {
            ((Application) context).unregisterActivityLifecycleCallbacks(sCalback);
        }
    }

    public static Activity getCurrentActivity() {
        if (sCurrentActivityRef != null) {
            return sCurrentActivityRef.get();
        }
        return null;
    }

    public static boolean isActivityResumed() {
        return sCurrentActivityRef != null;
    }

}
