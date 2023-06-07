package com.analysys.easytouch;

import com.analysys.ObserverListener;
import com.analysys.utils.AnalysysUtil;
import com.analysys.utils.CommonUtils;

/**
 * Create by Kevin on 2020/6/3
 * Describe:
 */
public class EasytouchProcess {

    private final static String OBSERVER_XWHO = "xwho";
    private final static String CALIBRATION_TIME = "calibration_time";

    private static EasytouchProcess sInstance;

    private ObserverListener listener;

    private EasytouchProcess() {
    }

    public static EasytouchProcess getInstance() {
        if (sInstance == null) {
            synchronized (EasytouchProcess.class) {
                if (sInstance == null) {
                    sInstance = new EasytouchProcess();
                }
            }
        }
        return sInstance;
    }

    public void setObserverListener(ObserverListener listener) {
        this.listener = listener;
        if (this.listener != null) {
            this.listener.onUserProfile(OBSERVER_XWHO, CommonUtils.getUserId(AnalysysUtil.getContext()));
        }
    }

    public void setXwho(final String xwho) {
        if (this.listener != null) {
            this.listener.onUserProfile(OBSERVER_XWHO, xwho);
        }
    }

    public void setTime(long value) {
        if (this.listener != null) {
            this.listener.onUserProfile(CALIBRATION_TIME, String.valueOf(value));
        }
    }

    public void setEventMessage(final String message) {
        if (this.listener != null) {
            this.listener.onEventMessage(message);
        }
    }
}
