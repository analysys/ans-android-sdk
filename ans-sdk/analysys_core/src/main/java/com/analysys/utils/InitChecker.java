package com.analysys.utils;

import com.analysys.process.AgentProcess;

/**
 * @Copyright © 2020 Analysys Inc. All rights reserved.
 * @Description:
 * @Create: 2020/6/12 15:31 PM
 * @author: huchangqing
 */
public class InitChecker {

    public static boolean check(String apiName) {
        if (!AgentProcess.getInstance().isInited()) {
            ANSLog.e("The SDK is not initialized, please call " + apiName + " after initialization");
            return false;
        }
        return true;
    }
}
