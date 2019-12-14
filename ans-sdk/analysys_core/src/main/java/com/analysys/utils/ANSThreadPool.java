package com.analysys.utils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @Copyright © 2019 EGuan Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2019/2/22 16:43
 * @Author: Wang-X-C
 */
public class ANSThreadPool {
    private static final boolean DEBUG = true;
    /** 任务队列,为了最后的清理数据 */
    private static List<WeakReference<ScheduledFuture<?>>> queue =
            new ArrayList<WeakReference<ScheduledFuture<?>>>();
    private static ExecutorService executor = Executors.newSingleThreadExecutor();
    private static long MAX_WAIT_SECONDS = 5;
    private static ExecutorService heatMapExecutor = Executors.newSingleThreadExecutor();

    public ANSThreadPool() {
    }

    public static ExecutorService getNetExecutor() {
        return NetHodler.NET_EXECUTORS;
    }

    public static ExecutorService getDBExecutor() {
        return DatabaseHodler.DB_EXECUTORS;
    }

    public static void shoutdown() {
        shoutDownNetExecutor();
        shoutDownDBExecutor();
    }

    public static void pushDB(Runnable task) {
        if (task == null) {
            return;
        }
        try {
            if (!DatabaseHodler.DB_EXECUTORS.isShutdown()) {
                getDBExecutor().execute(task);
            }
        } catch (Throwable e) {
            if (DEBUG) {
                ANSLog.e(e);
            }
        }
    }

    public static void pushNet(Runnable task) {
        if (task == null) {
            return;
        }
        try {
            if (!NetHodler.NET_EXECUTORS.isShutdown()) {
                getNetExecutor().execute(task);
            }
        } catch (Throwable e) {
            if (DEBUG) {
                ANSLog.e(e);
            }
        }
    }

    public static void execute(Runnable command) {
        if (executor.isShutdown()) {
            executor = Executors.newSingleThreadExecutor();
        }

        executor.execute(command);
    }

    public static void waitForAsyncTask() {
        try {
            for (WeakReference<ScheduledFuture<?>> reference : queue) {
                ScheduledFuture<?> f = reference.get();
                if (f != null) {
                    f.cancel(false);
                }
            }
            queue.clear();
            if (!executor.isShutdown()) {
                executor.shutdown();
            }
            executor.awaitTermination(MAX_WAIT_SECONDS, TimeUnit.SECONDS);
        } catch (Throwable ignore) {
        }
    }

    /**
     * 热图
     */
    public static void heatMapExecute(Runnable command) {
        if (heatMapExecutor.isShutdown()) {
            heatMapExecutor = Executors.newSingleThreadExecutor();
        }

        heatMapExecutor.execute(command);
    }

    private static void shoutDownNetExecutor() {
        if (!NetHodler.NET_EXECUTORS.isShutdown()) {
            NetHodler.NET_EXECUTORS.shutdown();
            try {
                NetHodler.NET_EXECUTORS.awaitTermination(10, TimeUnit.SECONDS);
            } catch (Throwable e) {
                if (DEBUG) {
                    //ANSLog.e(e);
                }
            }
        }
    }

    private static void shoutDownDBExecutor() {
        if (!DatabaseHodler.DB_EXECUTORS.isShutdown()) {
            DatabaseHodler.DB_EXECUTORS.shutdown();
            try {
                DatabaseHodler.DB_EXECUTORS.awaitTermination(10, TimeUnit.SECONDS);
            } catch (Throwable e) {
                if (DEBUG) {
                    //ANSLog.e(e);
                }
            }
        }
    }

    private static class NetHodler {
        public static final ExecutorService NET_EXECUTORS = Executors.newSingleThreadExecutor();
    }

    private static class DatabaseHodler {
        public static final ExecutorService DB_EXECUTORS = Executors.newFixedThreadPool(5);
    }
}



