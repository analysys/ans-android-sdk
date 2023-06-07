package com.analysys;

/**
 * 日志监听
 */
public interface LogObserverListener {

    /**
     * 完整的上传日志
     *
     * @param event
     */
    void onLogMessage(final String event);
}
