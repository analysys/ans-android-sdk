package com.analysys.allgro;

import android.util.Log;

/**
 * Description:
 * Author: fengzeyuan
 * Date: 2019-11-27 15:06
 * Version: 1.0
 */
public class AllgroLog {

    private static final String TAG = "AllgroLog";

    private static final boolean logSwitch = BuildConfig.DEBUG;

    /**
     * Verbose日志
     *
     * @param msg 消息
     */
    public static void v(Object msg) {
        log(TAG, msg.toString(), null, 'i');
    }

    /**
     * Verbose日志
     *
     * @param tag 标签
     * @param msg 消息
     */
    public static void v(String tag, Object msg) {
        log(tag, msg.toString(), null, 'i');
    }

    /**
     * Verbose日志
     *
     * @param tag 标签
     * @param msg 消息
     * @param tr  异常
     */
    public static void v(String tag, Object msg, Throwable tr) {
        log(tag, msg.toString(), tr, 'v');
    }

    /**
     * Debug日志
     *
     * @param msg 消息
     */
    public static void d(Object msg) {
        log(TAG, msg.toString(), null, 'd');
    }

    /**
     * Debug日志
     *
     * @param tag 标签
     * @param msg 消息
     */
    public static void d(String tag, Object msg) {
        log(tag, msg.toString(), null, 'd');
    }

    /**
     * Debug日志
     *
     * @param tag 标签
     * @param msg 消息
     * @param tr  异常
     */
    public static void d(String tag, Object msg, Throwable tr) {
        log(tag, msg.toString(), tr, 'd');
    }

    /**
     * Info日志
     *
     * @param msg 消息
     */
    public static void i(Object msg) {
        log(TAG, msg.toString(), null, 'i');
    }

    /**
     * Info日志
     *
     * @param tag 标签
     * @param msg 消息
     */
    public static void i(String tag, Object msg) {
        log(tag, msg.toString(), null, 'i');
    }

    /**
     * Info日志
     *
     * @param tag 标签
     * @param msg 消息
     * @param tr  异常
     */
    public static void i(String tag, Object msg, Throwable tr) {
        log(tag, msg.toString(), tr, 'i');
    }

    /**
     * Warn日志
     *
     * @param msg 消息
     */
    public static void w(Object msg) {
        log(TAG, msg.toString(), null, 'w');
    }

    /**
     * Warn日志
     *
     * @param tag 标签
     * @param msg 消息
     */
    public static void w(String tag, Object msg) {
        log(tag, msg.toString(), null, 'w');
    }

    /**
     * Warn日志
     *
     * @param tag 标签
     * @param msg 消息
     * @param tr  异常
     */
    public static void w(String tag, Object msg, Throwable tr) {
        log(tag, msg.toString(), tr, 'w');
    }

    /**
     * Error日志
     *
     * @param msg 消息
     */
    public static void e(Object msg) {
        log(TAG, msg.toString(), null, 'e');
    }

    /**
     * Error日志
     *
     * @param tag 标签
     * @param msg 消息
     */
    public static void e(String tag, Object msg) {
        log(tag, msg.toString(), null, 'e');
    }

    /**
     * Error日志
     *
     * @param tag 标签
     * @param msg 消息
     * @param tr  异常
     */
    public static void e(String tag, Object msg, Throwable tr) {
        log(tag, msg.toString(), tr, 'e');
    }

    /**
     * 根据tag, msg和等级，输出日志
     *
     * @param tag  标签
     * @param msg  消息
     * @param tr   异常
     * @param type 日志类型
     */
    private static void log(String tag, String msg, Throwable tr, char type) {
        if (logSwitch) {
            if ('e' == type) {
                Log.e(generateTag(tag), msg, tr);
            } else if ('w' == type) {
                Log.w(generateTag(tag), msg, tr);
            } else if ('d' == type) {
                Log.d(generateTag(tag), msg, tr);
            } else if ('i' == type) {
                Log.i(generateTag(tag), msg, tr);
            }
        }
    }


    /**
     * 产生tag
     *
     * @return tag
     */
    private static String generateTag(String tag) {
        StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
        StackTraceElement caller = stacks[4];
        String format = "Tag[" + tag + "] %s[%s, %d]";
        String callerClazzName = caller.getClassName();
        callerClazzName = callerClazzName.substring(callerClazzName.lastIndexOf(".") + 1);
        return String.format(format, callerClazzName, caller.getMethodName(), caller.getLineNumber());
    }
}
