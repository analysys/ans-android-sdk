package com.analysys.basic;

/**
 * @Copyright © 2019 EGuan Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2019/1/23 11:08
 * @Author: Wang-X-C
 */
public class AnalysysCMB {
    /**
     * 应用启动来源，
     * 1. icon启动 默认值为icon启动
     * 2. msg 启动
     * 3. deepLink启动
     * 5. 其他方式启动
     */
    public static void launchSource(int source) {
        BasicUtils.source = source;
    }
}
