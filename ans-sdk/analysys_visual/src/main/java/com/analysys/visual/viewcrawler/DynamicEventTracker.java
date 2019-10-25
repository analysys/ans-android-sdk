package com.analysys.visual.viewcrawler;

import android.view.View;

import com.analysys.AnalysysAgent;

/**
 * Handles translating events detected by ViewVisitors into events sent to
 * server
 * <p>
 * - Builds properties by interrogating view subtrees
 * <p>
 * - Possibly debounces events using the Handler given at construction
 */
class DynamicEventTracker implements BaseViewVisitor.OnEventListener {

    public DynamicEventTracker() {
    }

    @Override
    public void onEvent(View v, String eventName, String previousText, String matchText,
                        boolean debounce) {
        // 触发可视化埋点事件的回调
        if ((matchText != null && matchText.equals(previousText)) || matchText == null) {
            EventSender.sendEventToSocketServer(v, eventName);
            AnalysysAgent.track(v.getContext(), eventName);
        }
    }
}
