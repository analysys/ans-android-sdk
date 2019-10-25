package com.analysys.compatibilitydemo.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Copyright © 2018 EGuan Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2018/7/11 17:50
 * @Author: WXC
 */
public class ThreadPool {
    private static ExecutorService executor = Executors.newSingleThreadExecutor();

    public static void execute(Runnable command) {
        if (executor.isShutdown()) {
            executor = Executors.newSingleThreadExecutor();
        }

        executor.execute(command);
    }
}
