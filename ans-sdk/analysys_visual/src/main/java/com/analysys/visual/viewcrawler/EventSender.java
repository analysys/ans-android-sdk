package com.analysys.visual.viewcrawler;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.View;

import com.analysys.utils.InternalAgent;
import com.analysys.visual.utils.Constants;

import org.json.JSONObject;


/**
 * @Description: 在调试阶段，当埋点事件被触发时，通过该类执行埋点信息上报至WS的操作
 * @Version: 1.0
 * @Create: 2018/6/16 14:03
 * @Author: chris
 */
public class EventSender {

    private static final String EVENT_ID = "$event_id";
    private static final String MANUFACTURER = "$manufacturer";
    private static final String MODEL = "$model";
    private static final String OS_VERSION = "$os_version";
    private static final String LIB_VERSION = "$lib_version";
    private static final String NETWORK = "$network";
    private static final String SCREEN_WIDTH = "$screen_width";
    private static final String SCREEN_HEIGHT = "$screen_height";
    private static final String POS_LEFT = "$pos_left";
    private static final String POS_TOP = "$pos_top";
    private static final String POS_WIDTH = "$pos_width";
    private static final String POS_HEIGHT = "$pos_height";
    private static final String EVENT_INFO = "event_info";
    private static final String TARGET_PAGE = "target_page";
    private static final String TYPE = "type";
    private static final String EVENTINFO_REQUEST = "eventinfo_request";

    /**
     * 此方法在子线程执行
     *
     * @param view
     * @param eventName
     */
    public static synchronized void sendEventToSocketServer(View view, String eventName) {
        try {
            JSONObject event = new JSONObject();
            JSONObject eventInfo = new JSONObject();
            eventInfo.put(EVENT_ID, eventName);
            eventInfo.put(MANUFACTURER, Build.MANUFACTURER);
            eventInfo.put(MODEL, Build.MODEL);
            eventInfo.put(OS_VERSION, Build.VERSION.RELEASE);
            eventInfo.put(LIB_VERSION, Constants.VERSION);
            eventInfo.put(NETWORK, InternalAgent.networkType(view.getContext()));
            DisplayMetrics dm = Resources.getSystem().getDisplayMetrics();
            eventInfo.put(SCREEN_WIDTH, dm.widthPixels);
            eventInfo.put(SCREEN_HEIGHT, dm.heightPixels);
            int[] outLocale = new int[2];
            //相对整个屏幕的绝对坐标(包含状态栏)
            view.getLocationOnScreen(outLocale);
            eventInfo.put(POS_LEFT, outLocale[0] + "");
            eventInfo.put(POS_TOP, outLocale[1] + "");
            eventInfo.put(POS_WIDTH, view.getWidth() + "");
            eventInfo.put(POS_HEIGHT, view.getHeight() + "");
            event.put(EVENT_INFO, eventInfo);
            event.put(TARGET_PAGE, getTargetPage(view));
            event.put(TYPE, EVENTINFO_REQUEST);
            VisualManager.getInstance(view.getContext()).sendEventToSocketServer(event);
        } catch (Throwable e) {
            InternalAgent.e(e);
        }
    }

    private static String getTargetPage(View view) {
        try {
            Context context = view.getContext();
            if (context instanceof Activity) {
                Activity activity = (Activity) context;
                return activity.getClass().getName();
            }
        } catch (Throwable e) {
            InternalAgent.e(e);
        }
        return "";
    }
}
