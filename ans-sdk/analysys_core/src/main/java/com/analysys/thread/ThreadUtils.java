package com.analysys.thread;

import android.os.Looper;

import com.analysys.utils.ExceptionUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Copyright © 2021 Analysys Inc. All rights reserved.
 * @Description: 如果新加入high task，如果normal task没有执行完或者cachelist里有排队任务，将新任务加入cachelist，如果有返回值需求，wait
 * high task执行完后，如果有返回值需求，notify相应的callable，整个high task pool执行完后从0遍历cachelist，将同优先级的task submit到对应pool
 * 如果新加入normal task，原理同high task
 * @Create: 2021/5/6 4:49 PM
 * @author: huchangqing
 */
public class ThreadUtils {

    private static final List<PriorityCallable> sCacheList = new ArrayList<>();

    private static ThreadPoolExecutor sHighPoolExecutor = new ThreadPoolExecutor(1, 1, 100l, TimeUnit.SECONDS, new LinkedBlockingQueue()) {

        @Override
        protected void afterExecute(Runnable r, Throwable t) {
            super.afterExecute(r, t);
//            Log.i("ssss", "high after execute queue size: " + getQueue().size() + " active count: " + getActiveCount() + " thread id: " + Thread.currentThread().getId());
            boolean isExecuteFinish = getQueue().size() + getActiveCount() <= 1;
            if (isExecuteFinish) {
                submitCache("high");
            }
        }
    };

    private static void submitCache(String pri) {
        synchronized (sCacheList) {
//            Log.i("ssss", pri + " submit cache size: " + sCacheList.size() + " thread id: " + Thread.currentThread().getId());
            if (sCacheList.size() == 0) {
                return;
            }
            long priority = sCacheList.get(0).getPriority();
            ThreadPoolExecutor executor = priority == AnsLogicThread.PriorityLevel.HIGH ? sHighPoolExecutor : sNormalPoolExecutor;
            for (int i = 0; i < sCacheList.size(); i++) {
                PriorityCallable callable = sCacheList.get(i);
                if (callable.getPriority() == priority) {
//                    Log.i("ssss", "submit cache " + callable.getName());
                    Future future = executor.submit(callable);
                    if (callable.isWaitResult()) {
                        notifyWaiter(callable, future);
                    }
                    sCacheList.remove(callable);
                    i--;
                } else {
                    break;
                }
            }
        }
    }

    private static void notifyWaiter(final PriorityCallable callable, final Future future) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Object result = future.get();
                    callable.setResult(result);
                    synchronized (callable) {
//                        Log.i("ssss", "notify " + callable.getName() + " thread id: " + Thread.currentThread().getId());
                        callable.notify();
                    }
                } catch (Exception e) {
                    ExceptionUtil.exceptionPrint(e);
                }
            }
        }).start();
    }

    private static ThreadPoolExecutor sNormalPoolExecutor = new ThreadPoolExecutor(10, 100, 100l, TimeUnit.SECONDS, new LinkedBlockingQueue()) {

        @Override
        protected void afterExecute(Runnable r, Throwable t) {
            super.afterExecute(r, t);
//            Log.i("ssss", "normal after execute queue size: " + getQueue().size() + " active count: " + getActiveCount() + " thread id: " + Thread.currentThread().getId());
            boolean isExecuteFinish = getQueue().size() + getActiveCount() <= 1;
            if (isExecuteFinish) {
                submitCache("normal");
            }
        }
    };

    public static void async(PriorityCallable priorityCallable) {
        ThreadPoolExecutor poolWait;
        ThreadPoolExecutor poolSubmit;
        if (priorityCallable.getPriority() == AnsLogicThread.PriorityLevel.HIGH) {
            poolWait = sNormalPoolExecutor;
            poolSubmit = sHighPoolExecutor;
        } else {
            poolWait = sHighPoolExecutor;
            poolSubmit = sNormalPoolExecutor;
        }
        boolean addCache;
        synchronized (sCacheList) {
//            Log.i("ssss", "async " + priorityCallable.getName() + " cache size: " + sCacheList.size() + " wait pool active count: " + poolWait.getActiveCount() + " queue size: " + poolWait.getQueue().size() + " thread id: " + Thread.currentThread().getId());
            addCache = sCacheList.size() > 0 || poolWait.getActiveCount() + poolWait.getQueue().size() > 0;
        }
        if (addCache) {
            synchronized (sCacheList) {
                sCacheList.add(priorityCallable);
//                Log.i("ssss", priorityCallable.getName() + " add cache " + sCacheList.size());
            }
        } else {
//            Log.i("ssss", "submit " + priorityCallable.getName());
            poolSubmit.submit(priorityCallable);
        }
    }

    public static Object sync(PriorityCallable priorityCallable) {
        ThreadPoolExecutor poolWait;
        ThreadPoolExecutor poolSubmit;
        if (priorityCallable.getPriority() == AnsLogicThread.PriorityLevel.HIGH) {
            poolWait = sNormalPoolExecutor;
            poolSubmit = sHighPoolExecutor;
        } else {
            poolWait = sHighPoolExecutor;
            poolSubmit = sNormalPoolExecutor;
        }
        boolean addCache;
        synchronized (sCacheList) {
//            Log.i("ssss", "sync " + priorityCallable.getName() + " cache size: " + sCacheList.size() + " wait pool active count: " + poolWait.getActiveCount() + " queue size: " + poolWait.getQueue().size() + " thread id: " + Thread.currentThread().getId());
            addCache = sCacheList.size() > 0 || poolWait.getActiveCount() + poolWait.getQueue().size() > 0;
        }
        if (addCache) {
            synchronized (sCacheList) {
                sCacheList.add(priorityCallable);
//                Log.i("ssss", priorityCallable.getName() + " add cache " + sCacheList.size());
            }
            synchronized (priorityCallable) {
                try {
                    priorityCallable.setWaitResult(true);
                    // 防止主线程anr
                    if (Thread.currentThread().getId() == Looper.getMainLooper().getThread().getId()) {
                        priorityCallable.wait(1000);
                        if (priorityCallable.getResult() == null) {
                            priorityCallable.setResult(priorityCallable.call());
                        }
                    } else {
                        priorityCallable.wait();
                    }
//                    Log.i("ssss", "notified " + priorityCallable.getName() + " thread id: " + Thread.currentThread().getId());
                    return priorityCallable.getResult();
                } catch (Exception e) {
                    ExceptionUtil.exceptionPrint(e);
                }
            }
        } else {
//            Log.i("ssss", "submit " + priorityCallable.getName());
            Future future = poolSubmit.submit(priorityCallable);
            try {
                return future.get();
            } catch (Exception e) {
                ExceptionUtil.exceptionPrint(e);
            }
        }
        return null;
    }
}
