package com.analysys.utils;

import java.io.Closeable;
import java.io.IOException;

/**
 * Description: 关闭工具
 * Author: fengzeyuan
 * Date: 2019-09-23 18:02
 * Version: 1.0
 */
public final class CloseUtils {

    private CloseUtils() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }


    /**
     * 关闭一组可关闭对象
     *
     * @param closeables 可关闭对象组
     */
    public static void closeIt(final Closeable... closeables) {
        if (closeables == null) return;
        for (Closeable closeable : closeables) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (Throwable ignore) {
                    ExceptionUtil.exceptionThrow(ignore);
                }
            }
        }
    }


    
    /**
     * 安静的关闭一组可关闭对象
     * 
     * @param closeables  可关闭对象组
     */
    public static void closeItQuietly(final Closeable... closeables) {
        if (closeables == null) return;
        for (Closeable closeable : closeables) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (Throwable ignore) {
                    ExceptionUtil.exceptionThrow(ignore);
                }
            }
        }
    }
}