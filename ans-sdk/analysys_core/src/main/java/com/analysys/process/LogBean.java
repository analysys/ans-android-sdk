package com.analysys.process;

/**
 * @Copyright © 2019 EGuan Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2019/5/9 16:53
 * @Author: Wang-X-C
 */
public class LogBean {

    private static String value = null;
    private static String logDetails = null;
    private static int errorCode = 200;

    public static void setDetails(int errCode, String log) {
        errorCode = errCode;
        logDetails = log;
    }

    public static int getCode() {
        return errorCode;
    }

    public static void setCode(int errCode) {
        errorCode = errCode;
    }

    public static String getValue() {
        return value;
    }

    public static void setValue(String parameterValue) {
        value = parameterValue;
    }

    public static String getLog() {
        return logDetails;
    }

    public static void setLog(String log) {
        logDetails = log;
    }

    public static void resetLogBean() {
        setValue(null);
        setLog(null);
        setCode(200);
    }
}
