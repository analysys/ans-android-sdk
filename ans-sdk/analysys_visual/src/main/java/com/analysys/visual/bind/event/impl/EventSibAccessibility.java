package com.analysys.visual.bind.event.impl;

import android.text.TextUtils;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.AdapterView;

import com.analysys.ui.UniqueViewHelper;
import com.analysys.utils.AnsReflectUtils;
import com.analysys.visual.bind.event.BindHelper;
import com.analysys.visual.bind.event.EventTarget;
import com.analysys.visual.bind.locate.PathMatcher;
import com.analysys.visual.bind.locate.ViewFinder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @Copyright © 2019 Analysys Inc. All rights reserved.
 * @Description: AccessibilityDelegate同级事件类
 * @Create: 2019-11-28 09:56
 * @author: hcq
 */
public class EventSibAccessibility extends EventAccessibilityBase {

//    /**
//     * 目标元素距离同级容器的距离，用于判断是否为同级元素
//     */
//    private int mSibPathLen = 0;
//
//    /**
//     * 目标元素在其父容器中的位置，用于判断是否为同级元素
//     */
//    private int mSibChildIdx;
//
//    /**
//     * 目标元素是同级容器元素的直接子元素
//     */
//    private boolean mIsSibDirectly;

//    /**
//     * 容器的Hierarchy变化后的响应，用于绑定子元素
//     */
//    private SibHierarchyChangeCaller mSibHierarchyChangeCaller;
//
//    /**
//     * 容器的Hierarchy变化监听器，每个root view都绑定一个
//     */
//    private final SparseArray<CallOnceHierarchyChangeListener> mBindMap = new SparseArray<>();

    /**
     * 同级元素AccessibilityDelegate事件监听器，每个root view下的每个同级元素都绑定一个
     */
    private final SparseArray<LinkedHashMap<View, CallOnceAccessibilityListener>> mSibBindMap = new SparseArray<>();

    public EventSibAccessibility(String eventType, int accessibilityEventType) {
        super(eventType, accessibilityEventType);
    }

    @Override
    protected void doBind(ViewFinder.FindResult result) {
        if (!(result.sibTopView instanceof ViewGroup)) {
            return;
        }
//        // 同级事件时只用第一个
//        View targetView = result.listTargetView.get(0);
//
//        // 计算同级位置
//        mIsSibDirectly = targetView.getParent() == result.sibTopView;
//        ViewGroup viewTop = (ViewGroup) result.sibTopView;
//        mSibPathLen = ViewFinder.pathLen(viewTop, targetView);
//        mSibChildIdx = getChildIdx(targetView);

        // 初始化同级元素绑定map，原则上每次绑定前都不能是绑定状态，sibBind都应该是空的
        LinkedHashMap<View, CallOnceAccessibilityListener> sibBind = mSibBindMap.get(result.rootViewHashCode);
        if (sibBind == null) {
            sibBind = new LinkedHashMap<>();
            mSibBindMap.put(result.rootViewHashCode, sibBind);
        }
        sibBind.clear();


//        List<PathMatcher.PathElement> path = getSibPath(target.path, target.sibClassName);
//        if (path != null && !path.isEmpty()) {
//            bindChild((ViewGroup) result.sibTopView, result.rootViewHashCode, path, sibBind);
//        }
        if (target.listSibTop != null && target.listSibTop.size() > 0) {
            bindChild((ViewGroup) result.sibTopView, target.listSibTop.get(0).pathIdx + 1, target.listSibTop.get(0).row, result.rootViewHashCode,
                    target.path, target.listSibTop.subList(1, target.listSibTop.size()), sibBind);
        }

//        // 所有的root view使用同一个caller，在响应时判断root view
//        if (mSibHierarchyChangeCaller == null) {
//            mSibHierarchyChangeCaller = new SibHierarchyChangeCaller();
//        }
//
//        // 监听同级容器Hierarchy变化
//        CallOnceHierarchyChangeListener listener;
//        Object obj = CallOnceHierarchyChangeListener.getBindListener(viewTop);
//        if (obj instanceof CallOnceHierarchyChangeListener) {
//            ANSLog.i(VisualBindManager.TAG, "--bind sib hierarchy reuse, event id: " + eventId);
//            listener = (CallOnceHierarchyChangeListener) obj;
//            listener.addCaller(mSibHierarchyChangeCaller);
//        } else {
//            ANSLog.i(VisualBindManager.TAG, "--bind sib hierarchy new, event id: " + eventId);
//            listener = new CallOnceHierarchyChangeListener(viewTop, mSibHierarchyChangeCaller);
//            BindHelper.bind(viewTop, mSibHierarchyChangeCaller, listener);
//        }
//        mBindMap.put(result.rootViewHashCode, listener);
    }

    private List<PathMatcher.PathElement> getSibPath(List<PathMatcher.PathElement> path, String sibClassName) {
        for (int i = path.size() - 1; i >= 0; i--) {
            if (TextUtils.equals(path.get(i).viewClassName, sibClassName)) {
                if (i < path.size() - 1) {
                    return path.subList(i + 1, path.size());
                }
                return null;
            }
        }
        return null;
    }

    /**
     * 同级事件返回所有绑定的同级元素
     */
    @Override
    public List<View> getBindViews(int rootViewHashCode) {
        LinkedHashMap<View, CallOnceAccessibilityListener> sibBind = mSibBindMap.get(rootViewHashCode);
        if (sibBind != null) {
            return new ArrayList<>(sibBind.keySet());
        }
        return null;
    }

    /**
     * 查找特定root view下的同级元素中是否存在该view，返回其AccessibilityDelegate
     */
    @Override
    public View.AccessibilityDelegate getDelegate(int rootViewHashCode, View view) {
        LinkedHashMap<View, CallOnceAccessibilityListener> sibBind = mSibBindMap.get(rootViewHashCode);
        if (sibBind != null) {
            for (View bindView : sibBind.keySet()) {
                if (bindView == view) {
                    CallOnceAccessibilityListener listener = sibBind.get(view);
                    if (listener == null) {
                        return null;
                    }
                    Object obj = listener.getCurrent(bindView);
                    if (!(obj instanceof View.AccessibilityDelegate)) {
                        return null;
                    }
                    return (View.AccessibilityDelegate) obj;
                }
            }
        }
        return null;
    }
//
//    private int getChildIdx(View view) {
//        ViewGroup viewParent = (ViewGroup) view.getParent();
//        int childCount = viewParent.getChildCount();
//        for (int i = 0; i < childCount; i++) {
//            if (viewParent.getChildAt(i) == view) {
//                return i;
//            }
//        }
//        return -1;
//    }

    private void bindChild(ViewGroup topView, int pathStartIdx, int sibRow, int rootViewHashCode, List<PathMatcher.PathElement> fullPath,
                           List<EventTarget.SibTop> listSibTop, LinkedHashMap<View, CallOnceAccessibilityListener> sibBind) {
        EventTarget.SibTop sibTop = null;
        List<PathMatcher.PathElement> path;
        if (listSibTop.size() > 0) {
            sibTop = listSibTop.get(0);
            if (sibTop.pathIdx < fullPath.size() - 1) {
                path = fullPath.subList(pathStartIdx, sibTop.pathIdx + 1);
            } else {
                return;
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
                if (getSibRow(child) != sibRow) {
                    continue;
                }
            }
            View view = ViewFinder.findByPath(child, path, true, true);
            if (view != null) {
                if (sibTop != null) {
                    bindChild((ViewGroup) view, sibTop.pathIdx + 1, sibTop.row, rootViewHashCode, fullPath,
                            listSibTop.subList(1, listSibTop.size()), sibBind);
                } else {
                    CallOnceAccessibilityListener listener;
                    Object obj = CallOnceAccessibilityListener.getBindListener(view);
                    if (obj instanceof CallOnceAccessibilityListener) {
//                    ANSLog.i(VisualBindManager.TAG, "--bind sib child reuse, size: " + sibBind.size());
                        listener = (CallOnceAccessibilityListener) obj;
                        listener.addCaller(this);
                    } else {
//                    ANSLog.i(VisualBindManager.TAG, "--bind sib child new, size: " + sibBind.size());
                        listener = new CallOnceAccessibilityListener(view, this);
                        BindHelper.bind(view, this, listener);
                    }
                    sibBind.put(view, listener);
                    view.setTag(TAG_ROOT_HASH, rootViewHashCode);
                }
            }
        }
    }

    public static int getSibRow(View view) {
        ViewParent vp = view.getParent();
        if (vp instanceof AdapterView) {
            return ((AdapterView) vp).getPositionForView(view);
        } else if (UniqueViewHelper.isExtendsFromUniqueClass(vp.getClass().getName(), UniqueViewHelper.UNIQUE_CLZ_RECYCLER_VIEW)) {
            Object position = AnsReflectUtils.invokeMethod(vp, "getChildAdapterPosition",
                    new Class[]{View.class}, new Object[]{view});
            return position == null ? -1 : (int) position;
        } else if (UniqueViewHelper.isExtendsFromUniqueClass(vp.getClass().getName(), UniqueViewHelper.UNIQUE_CLZ_VIEW_PAGER)) {
            Object itemInfo = AnsReflectUtils.invokeMethod(vp, "infoForChild", new Class[]{View.class}, new Object[]{view});
            if (itemInfo != null) {
                Object position = AnsReflectUtils.getField(itemInfo, "position");
                return position == null ? -1 : (int) position;
            } else {
                return -1;
            }
        }
        return -1;
    }

//    private void bindChild(ViewGroup topView, int rootViewHashCode, List<PathMatcher.PathElement> path, LinkedHashMap<View, CallOnceAccessibilityListener> sibBind) {
//        int childCount = topView.getChildCount();
//        for (int i = 0; i < childCount; i++) {
//            View child = topView.getChildAt(i);
//            if (child == null) {
//                continue;
//            }
//            View view = ViewFinder.findByPath(child, path, true, true);
//            if (view != null) {
//                CallOnceAccessibilityListener listener;
//                Object obj = CallOnceAccessibilityListener.getBindListener(view);
//                if (obj instanceof CallOnceAccessibilityListener) {
////                    ANSLog.i(VisualBindManager.TAG, "--bind sib child reuse, size: " + sibBind.size());
//                    listener = (CallOnceAccessibilityListener) obj;
//                    listener.addCaller(this);
//                } else {
////                    ANSLog.i(VisualBindManager.TAG, "--bind sib child new, size: " + sibBind.size());
//                    listener = new CallOnceAccessibilityListener(view, this);
//                    BindHelper.bind(view, this, listener);
//                }
//                sibBind.put(view, listener);
//                view.setTag(TAG_ROOT_HASH, rootViewHashCode);
//            }
//        }
//    }

//    /**
//     * 递归查找所有同级元素，绑定监听器
//     */
//    private void bindChild(View view, ViewFinder.FindResult result, LinkedHashMap<View, CallOnceAccessibilityListener> sibBind) {
//        if (isSib(view, result)) {
//            CallOnceAccessibilityListener listener;
//            Object obj = CallOnceAccessibilityListener.getBindListener(view);
//            if (obj instanceof CallOnceAccessibilityListener) {
//                ANSLog.i(VisualBindManager.TAG, "--bind sib child reuse, size: " + sibBind.size());
//                listener = (CallOnceAccessibilityListener) obj;
//                listener.addCaller(this);
//            } else {
//                ANSLog.i(VisualBindManager.TAG, "--bind sib child new, size: " + sibBind.size());
//                listener = new CallOnceAccessibilityListener(view, this);
//                BindHelper.bind(view, this, listener);
//            }
//            sibBind.put(view, listener);
//            view.setTag(TAG_ROOT_HASH, result.rootViewHashCode);
//        } else if (view instanceof ViewGroup) {
//            ViewGroup viewGroup = (ViewGroup) view;
//            int childCount = viewGroup.getChildCount();
//            for (int i = 0; i < childCount; i++) {
//                bindChild(viewGroup.getChildAt(i), result, sibBind);
//            }
//        }
//    }

//    /**
//     * 递归查找所有同级元素，解绑监听器 TODO TreeObserver？
//     */
//    private void unbindChild(View view, LinkedHashMap<View, CallOnceAccessibilityListener> sibBind) {
//        if (view == null) {
//            return;
//        }
//        CallOnceAccessibilityListener listener = sibBind.get(view);
//        if (listener != null) {
//            BindHelper.unbind(view, this, listener);
//            sibBind.remove(view);
//            ANSLog.i(VisualBindManager.TAG, "--unbind sib child, size: " + sibBind.size());
//            view.setTag(TAG_ROOT_HASH, null);
//        }
//
//        if (view instanceof ViewGroup) {
//            ViewGroup viewGroup = (ViewGroup) view;
//            int childCount = viewGroup.getChildCount();
//            for (int i = 0; i < childCount; i++) {
//                unbindChild(viewGroup.getChildAt(i), sibBind);
//            }
//        }
//    }

    private void unbindAllChild(LinkedHashMap<View, CallOnceAccessibilityListener> sibBind) {
        for (View view : sibBind.keySet()) {
            if (view != null) {
                CallOnceAccessibilityListener listener = sibBind.get(view);
                if (listener != null) {
                    BindHelper.unbind(view, this, listener);
                }
            }
        }
        sibBind.clear();
    }

//    @Override
//    public void fire(boolean isMock) {
//        View view = getEventView();
//        refreshSibProperties(view, listProperties);
//
//        if (listRelate != null) {
//            for (Relate relate : listRelate) {
//                // 关联中含有列表item相关属性的，只能指定为event view
//                refreshSibProperties(view, relate.listProperty);
//            }
//        }
//        if (listCondition != null) {
//            for (EventCondition condition : listCondition) {
//                // 条件中含有列表item相关属性的，只能指定为event view
//                refreshSibProperties(view, condition.listProperty);
//            }
//        }
//        super.fire(isMock);
//    }

//    /**
//     * 事件触发后，设置触发元素位于同级容器中的位置，viewType等
//     */
//    private void refreshSibProperties(View view, List<IProperty> listProperties) {
//        if (listProperties != null && view != null) {
//            for (IProperty property : listProperties) {
//                if (property instanceof PropertySibPosition) {
//                    ((PropertySibPosition) property).setEventPosition(getEventPosition(view));
//                } else if (property instanceof PropertySibItemType) {
//                    ((PropertySibItemType) property).setSibItemType(getSibItemType(view));
//                }
//            }
//        }
//    }

//    /**
//     * 获取触发元素位于同级容器中的位置，listview类容器为item index，自定义同级容器为绑定顺序
//     */
//    private int getEventPosition(View view) {
//        ViewFinder.FindResult result = mBindView.get(mEventRootHashCode);
//        if (result.sibTopView instanceof AdapterView) {
//            while (true) {
//                ViewParent vp = view.getParent();
//                if (vp == result.sibTopView) {
//                    return ((AdapterView) result.sibTopView).getPositionForView(view);
//                }
//                if (vp instanceof View) {
//                    view = (View) vp;
//                } else {
//                    break;
//                }
//            }
//        } else if (UniqueViewHelper.isExtendsFromUniqueClass(result.sibTopView.getClass().getName(), UniqueViewHelper.UNIQUE_CLZ_RECYCLER_VIEW)) {
//            while (true) {
//                ViewParent vp = view.getParent();
//                if (vp == result.sibTopView) {
//                    Object position = AnsReflectUtils.invokeMethod(result.sibTopView, "getChildAdapterPosition",
//                            new Class[]{View.class}, new Object[]{view});
//                    return position == null ? -1 : (int) position;
//                }
//                if (vp instanceof View) {
//                    view = (View) vp;
//                } else {
//                    break;
//                }
//            }
//        } else if (UniqueViewHelper.isExtendsFromUniqueClass(result.sibTopView.getClass().getName(), UniqueViewHelper.UNIQUE_CLZ_VIEW_PAGER)) {
//            while (true) {
//                ViewParent vp = view.getParent();
//                if (vp == result.sibTopView) {
//                    Object itemInfo = AnsReflectUtils.invokeMethod(vp, "infoForChild", new Class[]{View.class}, new Object[]{view});
//                    if (itemInfo != null) {
//                        Object position = AnsReflectUtils.getField(itemInfo, "position");
//                        return position == null ? -1 : (int) position;
//                    } else {
//                        return -1;
//                    }
//                }
//                if (vp instanceof View) {
//                    view = (View) vp;
//                } else {
//                    break;
//                }
//            }
//        } else {
//            int pos = -1;
//            LinkedHashMap<View, CallOnceAccessibilityListener> sibBind = mSibBindMap.get(mEventRootHashCode);
//            if (sibBind != null) {
//                for (View bindView : sibBind.keySet()) {
//                    pos++;
//                    if (bindView == view) {
//                        return pos;
//                    }
//                }
//            }
//        }
//        return -1;
//    }
//
//    /**
//     * 获取触发元素item type
//     */
//    private int getSibItemType(View view) {
//        ViewFinder.FindResult result = mBindView.get(mEventRootHashCode);
//        if (result.sibTopView instanceof AdapterView) {
//            while (true) {
//                ViewParent vp = view.getParent();
//                if (vp == result.sibTopView) {
//                    ViewGroup.LayoutParams lp = view.getLayoutParams();
//                    if (lp instanceof AbsListView.LayoutParams) {
//                        Object itemType = AnsReflectUtils.getField(lp, "viewType");
//                        return itemType == null ? -1 : (int) itemType;
//                    } else {
//                        return -1;
//                    }
//                }
//                if (vp instanceof View) {
//                    view = (View) vp;
//                } else {
//                    break;
//                }
//            }
//        } else if (UniqueViewHelper.isExtendsFromUniqueClass(result.sibTopView.getClass().getName(), UniqueViewHelper.UNIQUE_CLZ_RECYCLER_VIEW)) {
//            Object layoutManager = AnsReflectUtils.invokeMethod(result.sibTopView, "getLayoutManager");
//            if (layoutManager == null) {
//                return -1;
//            }
//            while (true) {
//                ViewParent vp = view.getParent();
//                if (vp == result.sibTopView) {
//                    Object itemType = AnsReflectUtils.invokeMethod(layoutManager, "getItemViewType",
//                            new Class[]{View.class}, new Object[]{view});
//                    return itemType == null ? -1 : (int) itemType;
//                }
//                if (vp instanceof View) {
//                    view = (View) vp;
//                } else {
//                    break;
//                }
//            }
//        }
//        // TODO 自定义同级容器？
//        return -1;
//    }

//    /**
//     * 判断是否为同级元素父控件，类名，在父控件中的位置，到top的路径长度
//     */
//    private boolean isSib(View view, ViewFinder.FindResult result) {
//        if (view == null || result == null) {
//            return false;
//        }
//        // 直接子元素
//        if (mIsSibDirectly && view.getParent() == result.sibTopView) {
//            return true;
//        }
//
//        // 类名不一样不满足
//        if (view.getClass() != result.listTargetView.get(0).getClass()) {
//            return false;
//        }
//
//        // 在父容器中的位置不一样不满足
//        if (getChildIdx(view) != mSibChildIdx) {
//            return false;
//        }
//
//        // 与同级容器的距离不一样不满足
//        ViewGroup viewTop = (ViewGroup) result.sibTopView;
//        int len = ViewFinder.pathLen(viewTop, view);
//        return len != -1 && len == mSibPathLen;
//    }

    @Override
    public void doUnbind(ViewFinder.FindResult result) {
        if (result.sibTopView == null) {
            return;
        }
        LinkedHashMap<View, CallOnceAccessibilityListener> sibBind = mSibBindMap.get(result.rootViewHashCode);
        if (sibBind != null) {
//            ANSLog.i(VisualBindManager.TAG, "-doUnbind sib, unbind all child, event id: " + eventId);
            unbindAllChild(sibBind);
            mSibBindMap.remove(result.rootViewHashCode);
        }

//        CallOnceHierarchyChangeListener listener = mBindMap.get(result.rootViewHashCode);
//        if (listener != null) {
//            ANSLog.i(VisualBindManager.TAG, "-doUnbind sib, unbind HierarchyChangeListener, event id: " + eventId);
//            ViewGroup viewTop = (ViewGroup) result.sibTopView;
//            BindHelper.unbind(viewTop, mSibHierarchyChangeCaller, listener);
//            mBindMap.remove(result.rootViewHashCode);
//        }
    }

//    /**
//     * 同级容器Hierarchy变化响应
//     */
//    private class SibHierarchyChangeCaller implements ICaller {
//
////        private EventSibAccessibility getEvent() {
////            return EventSibAccessibility.this;
////        }
//
//        /**
//         * 0：view parent
//         * 1：view
//         * 2：is add
//         */
//        @Override
//        public void call(Object... data) {
//            View sibTopView = (View) data[0];
//            final View view = (View) data[1];
//            final boolean isAdd = (boolean) data[2];
//            // remove的情况下，必须使用容器计算hashCode
//            final int hashCode = getRootViewHashCode(sibTopView);
//            VisualBindManager.getInstance().postRunnable(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        ViewFinder.FindResult result = mBindView.get(hashCode);
//                        LinkedHashMap<View, CallOnceAccessibilityListener> sibBind = mSibBindMap.get(hashCode);
//                        if (result != null && sibBind != null) {
//                            if (isAdd) {
//                                bindChild(view, result, sibBind);
//                            } else {
//                                unbindChild(view, sibBind);
//                            }
//                        }
//                    } catch (Throwable ignore) {
//                        ExceptionUtil.exceptionThrow(ignore);
//                    }
//                }
//            });
//        }
//
//        @Override
//        public boolean equals(Object obj) {
//            if (!(obj instanceof SibHierarchyChangeCaller)) {
//                return false;
//            }
//            SibHierarchyChangeCaller caller = (SibHierarchyChangeCaller) obj;
//            return getEvent().equals(caller.getEvent());
//        }
//    }
}
