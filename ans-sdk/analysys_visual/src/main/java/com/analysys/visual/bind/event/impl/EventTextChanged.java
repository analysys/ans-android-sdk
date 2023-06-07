package com.analysys.visual.bind.event.impl;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseArray;
import android.view.View;
import android.widget.TextView;

import com.analysys.utils.ExceptionUtil;
import com.analysys.visual.bind.VisualBindManager;
import com.analysys.visual.bind.locate.ViewFinder;

import java.util.HashMap;
import java.util.Map;

/**
 * @Copyright © 2019 Analysys Inc. All rights reserved.
 * @Description: TextView及其子类文本改变事件类
 * @Create: 2019-12-02 11:37
 * @author: hcq
 */
public class EventTextChanged extends BaseEvent {

    /**
     * 记录每个root view下每个绑定
     */
    private final SparseArray<Map<TextView, ViewTextWatcher>> mBindMap = new SparseArray<>();

    public EventTextChanged(String eventType) {
        super(eventType);
    }

    private class ViewTextWatcher implements TextWatcher {
        View textView;

        public ViewTextWatcher(TextView tv) {
            textView = tv;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            call(textView);
        }
    }

    @Override
    protected void doBind(ViewFinder.FindResult result) {
        Map<TextView, ViewTextWatcher> watcheres = mBindMap.get(result.rootViewHashCode);
        if (watcheres == null) {
            watcheres = new HashMap<>();
            mBindMap.put(result.rootViewHashCode, watcheres);
        }
        watcheres.clear();
        for (int i = 0; i < result.listTargetView.size(); i++) {
            View view = result.listTargetView.get(i);
            if (view instanceof TextView) {
                view.setTag(TAG_ROOT_HASH, result.rootViewHashCode);
                TextView tv = (TextView) view;
                ViewTextWatcher watcher = new ViewTextWatcher(tv);
                tv.addTextChangedListener(watcher);
                watcheres.put(tv, watcher);
            }
        }
    }

    @Override
    protected void doUnbind(ViewFinder.FindResult result) {
        Map<TextView, ViewTextWatcher> watcheres = mBindMap.get(result.rootViewHashCode);
        if (watcheres == null) {
            return;
        }

//        ANSLog.i(VisualBindManager.TAG, "--unbind, event id: " + eventId);

        for (TextView tv : watcheres.keySet()) {
            ViewTextWatcher watcher = watcheres.get(tv);
            watcher.textView = null;
            tv.removeTextChangedListener(watcher);
            tv.setTag(TAG_ROOT_HASH, null);
        }
        watcheres.clear();
        mBindMap.remove(result.rootViewHashCode);
    }

    @Override
    public void call(Object... data) {
        if (data == null || data.length < 1 || !checkTime()) {
            return;
        }
        final View eventView = (View) data[0];
        final boolean isMock = data.length >= 2 && (boolean) data[1];
        VisualBindManager.getInstance().postRunnableAtFrontOfQueue(new Runnable() {
            @Override
            public void run() {
                try {
                    initFire(eventView, isMock);
                } catch (Throwable ignore) {
                    ExceptionUtil.exceptionThrow(ignore);
                }
            }
        });
    }
}
