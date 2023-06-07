package com.analysys.utils;

import android.content.Context;
import android.util.Log;

import com.analysys.process.AgentProcess;

import java.util.HashMap;

/**
 * @Copyright 2019 sanbo Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2019-08-04 13:54:41
 * @author: sanbo
 * @mail: xueyongfu@analysys.com.cn
 */
public class CrashHandler implements Thread.UncaughtExceptionHandler {
    private Thread.UncaughtExceptionHandler mDefaultHandler;
    private boolean isEnableCatchThrowable = false;
    private CrashCallBack handler = null;

    public class CrashType {
        public static final int crash_auto = 0;
        public static final int crash_report = 1;
    }

    private CrashHandler() {
        if (Thread.getDefaultUncaughtExceptionHandler() == this) {
            return;
        }
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    public static CrashHandler getInstance() {
        return Holder.Instance;
    }

    /**
     * 设置回调
     *
     * @param calback
     * @return
     */
    public CrashHandler setCallback(CrashCallBack calback) {
        if (calback != null) {
            handler = calback;
        }
        return Holder.Instance;
    }

    /**
     * 是否错误采集
     *
     * @param isEnable
     * @return
     */
    public CrashHandler setEnableCatch(boolean isEnable) {
        isEnableCatchThrowable = isEnable;
        return Holder.Instance;
    }

    public boolean isEnableCatch() {
        return isEnableCatchThrowable;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        // 增加内部处理
        handleException(ex);
        // 系统处理
        if (mDefaultHandler != null && (mDefaultHandler != Thread.getDefaultUncaughtExceptionHandler())) {
            mDefaultHandler.uncaughtException(thread, ex);
        }
    }

    private void handleException(Throwable ex) {

        if (handler == null) {
            // 没有注册回调
            return;
        }
        if (isEnableCatchThrowable) {
            handler.onAppCrash(ex);
        } else {
            handler.onAppCrash(null);
        }
    }

    /**
     * 回调函数
     */
    public interface CrashCallBack {
        public abstract void onAppCrash(Throwable e);
    }

    private static class Holder {
        public static CrashHandler Instance = new CrashHandler();
    }


    /**
     * 异常上报
     *
     * @param context
     * @param ex
     */
    public void reportException(Context context, Throwable ex, int type) {
        if (ex != null) {
//            Writer writer = new StringWriter();
//            PrintWriter printWriter = new PrintWriter(writer);
//            ex.printStackTrace(printWriter);
//            Throwable cause = ex.getCause();
//            while (cause != null) {
//                cause.printStackTrace(printWriter);
//                cause = cause.getCause();
//            }
//            printWriter.close();
//            String result = writer.toString();


            HashMap<String, Object> crashMap = new HashMap<>(2);
            crashMap.put(Constants.CRASH_DATA, Log.getStackTraceString(ex));
//            crashMap.put(Constants.CRASH_TYPE, type);

            AgentProcess.getInstance().trackSync(Constants.APP_CRASH_DATA, crashMap);

        }
    }

}