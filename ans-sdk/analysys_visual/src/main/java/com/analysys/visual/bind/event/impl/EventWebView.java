package com.analysys.visual.bind.event.impl;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewParent;

import com.analysys.utils.ExceptionUtil;
import com.analysys.visual.bind.VisualBindManager;
import com.analysys.visual.bind.event.EventFactory;
import com.analysys.visual.bind.locate.ViewFinder;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @Copyright © 2019 Analysys Inc. All rights reserved.
 * @Description: WebView事件类
 * @Create: 2019-12-02 11:37
 * @author: hcq
 */
public class EventWebView extends BaseEvent {

    private View mWebView;

    public EventWebView(String eventType) {
        super(eventType);
    }

    @Override
    protected void doBind(ViewFinder.FindResult result) {
    }

    @Override
    protected void doUnbind(ViewFinder.FindResult result) {
    }

    @Override
    public void call(Object... data) {
    }

    public void setAllProperties(String properties, String extraEditProperties) {
        mAllProperties = new HashMap<>();
        try {
            if (!TextUtils.isEmpty(properties)) {
                JSONObject joProperties = new JSONObject(properties);
                for (Iterator<String> it = joProperties.keys(); it.hasNext(); ) {
                    String key = it.next();
                    Object value = joProperties.get(key);
                    mAllProperties.put(key, value);
                }
            }
            if (!TextUtils.isEmpty(extraEditProperties)) {
                if (VisualBindManager.getInstance().isEditing()) {
                    JSONObject joExtraProperties = new JSONObject(extraEditProperties);
                    for (Iterator<String> it = joExtraProperties.keys(); it.hasNext(); ) {
                        String key = it.next();
                        Object value = joExtraProperties.get(key);
                        mAllProperties.put(key, value);
                    }
                }
            }
        } catch (Throwable ignore) {
            mAllProperties = null;
            ExceptionUtil.exceptionPrint(ignore);
        }
    }

    @Override
    protected View getEventRootView(Relate relate) {
        if (mWebView == null) {
            return null;
        }
        // 回溯到关联根元素
        View view = mWebView;
        for (int i = 0; i < relate.target.sibTopDistance; i++) {
            ViewParent vp = view.getParent();
            if (vp instanceof View) {
                view = (View) vp;
            } else {
                return null;
            }
        }
        return view;
    }

    public String getNativeProperty(Object view, String strRelate) {
        if (!(view instanceof View) || TextUtils.isEmpty(strRelate)) {
            return null;
        }

        mWebView = (View) view;
        JSONObject joResult = new JSONObject();
        try {
            JSONArray jaRelate = new JSONArray(strRelate);
            for (int i = 0; i < jaRelate.length(); i++) {
                JSONObject joRelated = jaRelate.getJSONObject(i);
                Relate relate = EventFactory.json2Relate(joRelated);
                Map<String, Object> map = getRelateProperties(relate);
                if (map == null) {
                    continue;
                }
                for (String key : map.keySet()) {
                    joResult.put(key, map.get(key));
                }
            }
        } catch (Throwable ignore) {
            ExceptionUtil.exceptionThrow(ignore);
        }
        mWebView = null;
        return joResult.toString();
    }
}
