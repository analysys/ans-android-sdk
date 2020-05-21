package com.analysys.utils;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

/**
 * @Copyright © 2019 EGuan Inc. All rights reserved.
 * @Description: 线程操作以及数据同步
 * @Version: 1.0
 * @Create: 2020/4/12 16:43
 * @Author: WP
 */
public class AThreadPool {


//    保证时序
    //高优先级队列：一个标记位置
    //中、低优先级队列:等待高优先级队列标记位完成之后再执行

    private static LinkedList<Runnable> middleLinked = new LinkedList<>();
    private static final byte[] middleByte = new byte[1];
    private static LinkedList<Runnable> loWLinked = new LinkedList<>();
    private static final byte[] lowByte = new byte[1];
    private static final int MAXSIZE = 100;


    private static ExecutorService highService = Executors.newSingleThreadExecutor();

    private static ExecutorService middleService = Executors.newSingleThreadExecutor();

    private static ExecutorService lowService = Executors.newSingleThreadExecutor();

    private static volatile boolean initSuccess = false;

    /**
     * 初始化接口调用使用
     *
     * @param callable
     */
    public static void initHighPriorityExecutor(Callable callable) {
        FutureTask<Boolean> future = new FutureTask<Boolean>(callable) {
            // 异步任务执行完成，回调
            @Override
            protected void done() {
                try {
                    boolean flag = get();
                    initSuccess = true;
                } catch (Throwable e) {
                    ExceptionUtil.exceptionThrow(e);
                }
            }
        };

        if (!initSuccess) {
            highService.execute(future);
        }
    }

    /**
     * 高优先级执行：set类对外操作接口
     *
     * @param runnable
     */
    public static void asyncHighPriorityExecutor(Runnable runnable) {
        highService.execute(runnable);
    }

    /**
     * 高优先级同步执行：主要时get类对外操作接口
     *
     * @param callable
     * @return
     */
    public static Object syncHighPriorityExecutor(Callable callable) {
        Object object = null;
        FutureTask<Object> futureTask = new FutureTask<Object>(callable);
        highService.execute(futureTask);

        while (!futureTask.isDone() && !futureTask.isCancelled()) {
            try {
                object = futureTask.get();
            } catch (Throwable ignore) {
                ExceptionUtil.exceptionThrow(ignore);
            }
        }
        return object;
    }


    /**
     * 执行start、pageveiw、end必须保证时序
     *
     * @param runnable
     */
    public static void asyncMiddlePriorityExecutor(final Runnable runnable) {
        if (initSuccess) {
            try {
                if (middleLinked.size() > 0) {
                    synchronized (middleByte) {
                        if (middleLinked.size() > 0) {
                            Iterator<Runnable> iterator = middleLinked.iterator();
                            while (iterator.hasNext()) {
                                Runnable tmp = iterator.next();
                                middleService.execute(tmp);
                            }
                            middleLinked.clear();
                        }
                    }
                }
            } catch (Throwable ignore) {
                ExceptionUtil.exceptionThrow(ignore);
            }

            middleService.execute(runnable);
        } else {
            if (middleLinked.size() < MAXSIZE) {
                middleLinked.add(runnable);
            }
        }
    }

    /**
     * 普通事件
     *
     * @param runnable
     */
    public static void asyncLowPriorityExecutor(Runnable runnable) {
        if (initSuccess) {
            try{
                if(loWLinked.size()>0){
                    synchronized (lowByte){
                        if(loWLinked.size()>0){
                            Iterator<Runnable> iterator = loWLinked.iterator();
                            while(iterator.hasNext()){
                                Runnable tmp = iterator.next();
                                lowService.execute(tmp);
                            }
                            loWLinked.clear();
                        }
                    }
                }
            }catch (Throwable ignore){
                ExceptionUtil.exceptionThrow(ignore);
            }

            lowService.execute(runnable);
        } else {
            if(loWLinked.size()<MAXSIZE){
                loWLinked.add(runnable);
            }
        }
    }
}
