package com.analysys.visual.viewcrawler;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @Copyright © 2018 EGuan Inc. All rights reserved.
 * @Description: 提供调用可视化模块的接口类
 * @Version: 1.0
 * @Create: 2018/3/22 下午8:11
 * @Author: chris
 */
public class VisualManager {

    private static VisualManager instance;
    private ViewCrawler viewCrawler = null;

    private VisualManager(Context context) {
        viewCrawler = new ViewCrawler(context);
        viewCrawler.startUpdates();
    }

    public static VisualManager getInstance(Context context) {
        if (instance == null) {
            synchronized (VisualManager.class) {
                if (instance == null) {
                    instance = new VisualManager(context);
                }
            }
        }
        return instance;
    }

    public void applyEventBindingEvents(JSONArray bindingEvents) {
        viewCrawler.setEventBindings(bindingEvents);
    }


    /**
     * 预留方法，可以直接调用此方法,绕开sener触发
     */
    public void connectToSer() {
        viewCrawler.connectToEditor();
    }

    public void sendEventToSocketServer(JSONObject eventInfo) {
        viewCrawler.sendEventToSocketServer(eventInfo);
    }
}
