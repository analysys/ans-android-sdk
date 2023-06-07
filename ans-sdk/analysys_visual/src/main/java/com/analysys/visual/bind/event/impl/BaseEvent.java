package com.analysys.visual.bind.event.impl;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.analysys.ui.RootView;
import com.analysys.ui.UniqueViewHelper;
import com.analysys.ui.WindowUIHelper;
import com.analysys.utils.AnalysysUtil;
import com.analysys.visual.bind.VisualBindManager;
import com.analysys.visual.bind.event.EventTarget;
import com.analysys.visual.bind.event.ICaller;
import com.analysys.visual.bind.event.IEvent;
import com.analysys.visual.bind.event.IEventAction;
import com.analysys.visual.bind.locate.PathMatcher;
import com.analysys.visual.bind.locate.ViewFinder;
import com.analysys.visual.bind.property.IProperty;
import com.analysys.visual.bind.property.impl.BaseProperty;
import com.analysys.visual.utils.WebViewBindHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Copyright © 2019 Analysys Inc. All rights reserved.
 * @Description: 事件基类，事件会绑定监听器，用于响应
 * @Create: 2019-11-28 09:53
 * @author: hcq
 */
public abstract class BaseEvent implements IEvent, ICaller {

    /**
     * 事件唯一标识，埋点标识
     */
    public int id;

    /**
     * 埋点标识
     */
    public String eventId;

    /**
     * 事件操作类型，增删改
     */
    public String recordType;

    /**
     * 事件类型，点击、长按等
     */
    public final String eventType;

    /**
     * 事件触发所在page名称，可能是activity、dialog、float window、popup window等
     */
    public String eventPageName;

    /**
     * 事件生效范围，在哪些page生效
     */
    public BindingRange bindingRange;

    /**
     * 事件触发目标元素，可能是多个目标
     */
    public EventTarget target;

    /**
     * 同级事件目标容器
     */
    public EventTarget sibTopTarget;

    /**
     * 事件触发后的响应，埋点上报或者其它动作
     */
    public List<IEventAction> listEventAction;

    /**
     * 事件绑定的属性列表
     */
    public List<IProperty> listProperties;

    /**
     * 与该事件关联的目标元素
     */
    public List<Relate> listRelate;

    /**
     * 事件触发条件列表，满足所有条件才能触发
     */
    public List<EventCondition> listCondition;

    /**
     * 事件触发事件
     */
    private long mEventTime;

    /**
     * 事件触发最短间隔
     */
    private static final int EVENT_MIN_INTERVAL = 500;

    /**
     * 事件json格式
     */
    public String strEvent;

    BaseEvent(String eventType) {
        this.eventType = eventType;
    }

    public static final int TAG_ROOT_HASH = 3 << 24;

    protected Map<String, Object> mAllProperties;

    /**
     * 是否可以视为hybrid埋点：有hybrid标识或者为属性绑定
     */
    public boolean availableForHybrid() {
        return (this instanceof EventWebView) || (target.path == null && target.propsBindingList != null);
    }

    /**
     * 绑定范围类
     */
    public class BindingRange {

        /**
         * 是否所有activity生效
         */
        public boolean allActivity;

        /**
         * 哪些activity生效
         */
        public final List<String> activities = new ArrayList<>();

        /**
         * 是否所有dialog生效
         */
        public boolean allDialog;

        /**
         * 哪些activity内的dialog生效
         */
        public final List<String> dialogPages = new ArrayList<>();

        /**
         * 是否所有float window生效
         */
        public boolean allFloatWin;

        /**
         * 哪些activity内的float window生效
         */
        public final List<String> floatWinPages = new ArrayList<>();

        /**
         * 是否所有popup window生效
         */
        public boolean allPopWin;

        /**
         * 哪些activity内的popup window生效
         */
        public final List<String> popWinPages = new ArrayList<>();
    }

    /**
     * 关联元素类
     */
    public static class Relate {

        /**
         * 关联元素位置
         */
        public EventTarget target;

        /**
         * 关联元素属性
         */
        public List<IProperty> listProperty;

        /**
         * relate字符串，供hybrid使用
         */
        public String strRelate;
    }

    /**
     * 触发条件类，与关联类逻辑类似
     */
    public class EventCondition extends Relate {

        boolean isMatch(View view) {
            if (listProperty == null) {
                return true;
            }
            for (IProperty property : listProperty) {
                if (!property.isMatch(view)) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * 触发事件的view所属根view的hashCode
     */
    int mEventRootHashCode;

    /**
     * key：根View hashcode
     * value：绑定目标
     */
    final Map<Integer, ViewFinder.FindResult> mBindView = new ConcurrentHashMap<>();

    @Override
    public final void bind(RootView rootView) {
        // 判断是否在绑定范围内
        if (!inRange(rootView)) {
            return;
        }

//        ANSLog.i(VisualBindManager.TAG, "try bind event, page: " + rootView.pageName
//                + ", event id: " + eventId
//                + ", path: " + target.path
//                + (sibTopTarget == null ? "" : ", sibTopPath: " + sibTopTarget.path));

        // 查找绑定元素
        ViewFinder.FindResult result = ViewFinder.find(rootView.view, this);
        if ((result.listTargetView == null || result.listTargetView.isEmpty()) && result.sibTopView == null) {
            return;
        }
//        ANSLog.i(VisualBindManager.TAG, "--bind hit, page: " + rootView.pageName + ", event id: " + eventId);
        // 保存查找结果
        mBindView.put(rootView.hashCode, result);

        doBind(result);
    }

    protected abstract void doBind(ViewFinder.FindResult result);

    protected abstract void doUnbind(ViewFinder.FindResult result);

    @Override
    public final void unbind(RootView rootView) {
        ViewFinder.FindResult result = mBindView.get(rootView.hashCode);
        if (result != null) {
//            ANSLog.i(VisualBindManager.TAG, "--unbind, event id: " + eventId + " root view: " + rootView.pageName);
            doUnbind(result);
            result.clear();
            mBindView.remove(rootView.hashCode);
        }
    }

    @Override
    public void unbindAll() {
        for (int hashcode : mBindView.keySet()) {
            ViewFinder.FindResult result = mBindView.get(hashcode);
            doUnbind(result);
            result.clear();
        }
        mBindView.clear();
    }

    /**
     * 防止多监听器链式调用造成短时间内多次触发
     */
    boolean checkTime() {
        long time = System.currentTimeMillis();
        return Math.abs(mEventTime - time) > EVENT_MIN_INTERVAL;
    }

    void setEventTime() {
        mEventTime = System.currentTimeMillis();
    }

    /**
     * 获取绑定的view列表
     */
    public List<View> getBindViews(int rootViewHashCode) {
        ViewFinder.FindResult result = mBindView.get(rootViewHashCode);
        if (result != null) {
            return result.listTargetView;
        }
        return null;
    }

//    /**
//     * 获取所有关联元素，供调试使用
//     */
//    public List<View> getRelateViews(int viewRootHashCode) {
//        return getRelateViews(viewRootHashCode, listRelate);
//    }
//
//    /**
//     * 获取所有条件元素，供调试使用
//     */
//    public List<View> getConditionViews(int viewRootHashCode) {
//        return getRelateViews(viewRootHashCode, listCondition);
//    }
//
//    private List<View> getRelateViews(int viewRootHashCode, List<? extends Relate> listRelate) {
//        ViewFinder.FindResult result = mBindView.get(viewRootHashCode);
//        if (result == null) {
//            return null;
//        }
//        if (listRelate != null) {
//            List<View> listView = new ArrayList<>();
//            for (Relate relate : listRelate) {
//                if (relate.target == target) {
//                    listView.add(result.eventView);
//                } else {
//                    List<View> list = ViewFinder.findViewByEventTarget(getEventRootView(relate), relate.target, true, true);
//                    View view = (list == null || list.isEmpty()) ? null : list.get(0);
//                    listView.add(view);
//                }
//            }
//            return listView;
//        }
//        return null;
//    }

    /**
     * 获取触发事件的元素
     */
    public View getEventView() {
        ViewFinder.FindResult result = mBindView.get(mEventRootHashCode);
        if (result != null) {
            return result.eventView;
        }
        return null;
    }

    /**
     * 判断是否在生效范围
     */
    public boolean inRange(RootView rootView) {
        boolean isRNPage = AnalysysUtil.isRNPage();
        String rnUrl = AnalysysUtil.getRNUrl();
        if (isRNPage && !TextUtils.isEmpty(rnUrl)) {
            if (bindingRange.allActivity) {
                return true;
            }
            return bindingRange.activities.contains(rnUrl);
        }

        // activity
        if (!rootView.pageName.contains("(")) {
            if (bindingRange.allActivity) {
                return true;
            }
            return bindingRange.activities.contains(rootView.pageName);
        }

        // dialog
        int idx = rootView.pageName.indexOf(WindowUIHelper.getDialogName(null));
        if (idx >= 0) {
            if (bindingRange.allDialog) {
                return true;
            }
            return bindingRange.dialogPages.contains(rootView.pageName);
        }

        // float window
        idx = rootView.pageName.indexOf(WindowUIHelper.getFloatWindowName(null));
        if (idx >= 0) {
            if (bindingRange.allFloatWin) {
                return true;
            }
            return bindingRange.floatWinPages.contains(rootView.pageName);
        }

        // popup window
        idx = rootView.pageName.indexOf(WindowUIHelper.getPopupWindowName(null));
        if (idx >= 0) {
            if (bindingRange.allPopWin) {
                return true;
            }
            return bindingRange.popWinPages.contains(rootView.pageName);
        }

        return false;
    }

    /**
     * 获取事件属性
     */
    public Map<String, Object> getProperties() {
        return mAllProperties;
    }

    /**
     * 计算与该事件相关的属性，包括触发元素属性和关联元素属性
     */
    private void calculateAllProperties() {
        mAllProperties = null;
        ViewFinder.FindResult result = mBindView.get(mEventRootHashCode);
        if (result == null || result.eventView == null) {
            return;
        }
        // 触发元素属性
        Map<String, Object> allProperties = new HashMap<>();
        if (listProperties != null) {
            for (IProperty p : listProperties) {
                BaseProperty property = (BaseProperty) p;
                Object value = property.getValue(result.eventView);
                allProperties.put(property.key, value);
            }
        }

        // 关联元素属性
        if (listRelate != null) {
            for (Relate relate : listRelate) {
                Map<String, Object> relateProperties = getRelateProperties(relate);
                if (relateProperties != null) {
                    allProperties.putAll(relateProperties);
                }
            }
        }

        if (VisualBindManager.getInstance().isEditing()) {
            View view = result.eventView;
            int[] outLocale = new int[2];
            //相对整个屏幕的绝对坐标(包含状态栏)
            view.getLocationOnScreen(outLocale);
            allProperties.put("$pos_left", outLocale[0] + "");
            allProperties.put("$pos_top", outLocale[1] + "");
            allProperties.put("$pos_width", view.getWidth() + "");
            allProperties.put("$pos_height", view.getHeight() + "");
        }

        mAllProperties = allProperties;
    }

    /**
     * 获取触发元素所属根元素
     */
    protected View getEventRootView(Relate relate) {
        if (relate == null || relate.target == null || relate.target.sibTopDistance < 0) {
            ViewFinder.FindResult result = mBindView.get(mEventRootHashCode);
            if (result == null || result.eventView == null) {
                return null;
            }
            return result.eventView.getRootView();
        } else {
            ViewFinder.FindResult result = mBindView.get(mEventRootHashCode);
            if (result == null) {
                return null;
            }
            View view = result.eventView;
            if (view == null) {
                return null;
            }
            // 回溯到关联根元素
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
    }

    /**
     * 获取关联元素的属性
     */
    protected Map<String, Object> getRelateProperties(Relate relate) {
        // 关联hybrid元素
        if (relate.target.h5Path != null) {
            List<View> list = ViewFinder.findViewByEventTarget(getEventRootView(relate), relate.target, true, true);
            View relateView = (list == null || list.isEmpty()) ? null : list.get(0);
            if (relateView != null && UniqueViewHelper.isWebView(relateView.getClass())) {
                return WebViewBindHelper.getInstance().getHybridProperty(relateView, relate.strRelate);
            }
            return null;
        }
        View relateView;
        // 关联列表中的固定一行
        if (relate.target.isFixSibRow && relate.target.listSibTop != null && relate.target.listSibTop.size() > 0) {
            ViewGroup sibParent = null;
            EventTarget.SibTop sibTop = relate.target.listSibTop.get(0);
            if (sibTop.pathIdx < relate.target.path.size() - 1) {
                EventTarget sibTarget = new EventTarget(relate.target.path, sibTop.pathIdx + 1);
                List<View> list = ViewFinder.findViewByEventTarget(getEventRootView(relate), sibTarget, true, true);
                View sibView = (list == null || list.isEmpty()) ? null : list.get(0);
                if (sibView instanceof ViewGroup) {
                    sibParent = (ViewGroup) sibView;
                }
            }
            if (sibParent == null) {
                return null;
            }
            relateView = getFixRelateView(sibParent, sibTop.pathIdx + 1, sibTop.row, relate.target.path, relate.target.listSibTop.subList(1, relate.target.listSibTop.size()));
        } else {
            // 查找关联目标，只用第一个元素
            List<View> list = ViewFinder.findViewByEventTarget(getEventRootView(relate), relate.target, true, true);
            relateView = (list == null || list.isEmpty()) ? null : list.get(0);
        }
        if (relateView != null && relate.listProperty != null) {
            Map<String, Object> properties = new HashMap<>();
            for (IProperty p : relate.listProperty) {
                BaseProperty property = (BaseProperty) p;
                Object value = property.getValue(relateView);
                properties.put(property.key, value);
            }
            return properties;
        }
        return null;
    }

    private View getFixRelateView(ViewGroup topView, int pathStartIdx, int sibRow, List<PathMatcher.PathElement> fullPath, List<EventTarget.SibTop> listSibTop) {
        EventTarget.SibTop sibTop = null;
        List<PathMatcher.PathElement> path;
        if (listSibTop.size() > 0) {
            sibTop = listSibTop.get(0);
            if (sibTop.pathIdx < fullPath.size() - 1) {
                path = fullPath.subList(pathStartIdx, sibTop.pathIdx + 1);
            } else {
                return null;
            }
        } else {
            path = fullPath.subList(pathStartIdx, fullPath.size());
        }

        int childCount = topView.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = topView.getChildAt(i);
            if (child == null) {
                continue;
            }
            if (sibRow != -1) {
                if (EventSibAccessibility.getSibRow(child) != sibRow) {
                    continue;
                }
            }
            View view = ViewFinder.findByPath(child, path, true, true);
            if (view != null) {
                if (sibTop != null) {
                    View v = getFixRelateView((ViewGroup) view, sibTop.pathIdx + 1, sibTop.row, fullPath, listSibTop.subList(1, listSibTop.size()));
                    if (v != null) {
                        return v;
                    }
                } else {
                    return view;
                }
            }
        }
        return null;
    }

    /**
     * 是否符合触发条件，所有条件都满足才能触发事件响应
     */
    private boolean isMatchCondition() {
        if (listCondition == null) {
            return true;
        }
        ViewFinder.FindResult result = mBindView.get(mEventRootHashCode);
        if (result == null) {
            return false;
        }
        for (int i = 0; i < listCondition.size(); i++) {
            BaseEvent.EventCondition condition = listCondition.get(i);
            if (condition != null) {
                // 没有指定条件元素则使用事件触发元素
                View conditionView;
                if (condition.target == target) {
                    conditionView = result.eventView;
                } else {
                    // 查找条件元素，只用第一个元素
                    List<View> list = ViewFinder.findViewByEventTarget(getEventRootView(condition), condition.target, true, true);
                    conditionView = (list == null || list.isEmpty()) ? null : list.get(0);
                }
                if (conditionView == null || !condition.isMatch(conditionView)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * 初始化触发事件前的环境
     */
    void initFire(View eventView, boolean isMock) {
        setEventTime();
        if (eventView != null) {
            int hashCode = getRootViewHashCode(eventView);
            ViewFinder.FindResult result = mBindView.get(hashCode);
            if (result != null) {
                eventPageName = WindowUIHelper.getPageName(eventView);
                mEventRootHashCode = hashCode;
                result.eventView = eventView;
                fire(isMock);
            }
        }
    }

    /**
     * 获取view所在的根view的hashCode
     */
    protected int getRootViewHashCode(View view) {
        if (view == null) {
            return -1;
        }
        Object tag = view.getTag(BaseEvent.TAG_ROOT_HASH);
        if (tag instanceof Integer) {
            return (int) tag;
        }
        View root = view.getRootView();
        if (root == null) {
            return -1;
        }
        return root.hashCode();
    }

    /**
     * 触发事件响应
     *
     * @param isMock 是否为Debug模式下的模拟点击
     */
    void fire(boolean isMock) {
        if (!isMock && isMatchCondition()) {
            calculateAllProperties();
            for (IEventAction action : listEventAction) {
                action.doAction(BaseEvent.this);
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof BaseEvent)) {
            return false;
        }
        BaseEvent event = (BaseEvent) obj;

        if (id != -1 && event.id != -1) {
            return id == event.id;
        }
        if (TextUtils.equals(eventId, event.eventId)) {
            return true;
        }
        // 老版本比较path
        if (target.isOldPath || event.target.isOldPath) {
            return TextUtils.equals(target.oldPathStr, event.target.oldPathStr);
        }
        return false;
    }
}
