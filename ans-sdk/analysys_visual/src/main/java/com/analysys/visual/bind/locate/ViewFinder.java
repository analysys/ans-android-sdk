package com.analysys.visual.bind.locate;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.analysys.visual.bind.event.EventTarget;
import com.analysys.visual.bind.event.impl.BaseEvent;
import com.analysys.visual.bind.property.IProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * @Copyright © 2019 Analysys Inc. All rights reserved.
 * @Description: 目标元素查找辅助类
 * @Create: 2019-11-28 10:01
 * @author: hcq
 */
public class ViewFinder {

    public static class FindResult {

        FindResult(int hashCode, List<View> listTargetView, View sibTopView) {
            this.rootViewHashCode = hashCode;
            this.listTargetView = listTargetView;
            this.sibTopView = sibTopView;
        }

        /**
         * 根view的hashCode
         */
        public final int rootViewHashCode;

        /**
         * 同级情况下为同级容器元素
         */
        public View sibTopView;

        /**
         * 非同级情况下为所有绑定元素，
         * 同级情况下为单个目标元素，其它同级元素参照该元素查找
         */
        public List<View> listTargetView;

        /**
         * 触发事件的元素
         */
        public View eventView;

        public void clear() {
            sibTopView = null;
            eventView = null;
            if (listTargetView != null) {
                listTargetView.clear();
                listTargetView = null;
            }
        }
    }

    /**
     * 在rootView中查找符合event绑定条件的元素
     */
    public static FindResult find(View rootView, BaseEvent event) {
        // 查找同级容器元素
        View sibTopView = null;
        if (event.sibTopTarget != null) {
            List<View> listSib = findViewByEventTarget(rootView, event.sibTopTarget, false, false);
            if (listSib != null && !listSib.isEmpty()) {
                sibTopView = listSib.get(0);
            }
        }

        // 查找目标元素
        List<View> target = findViewByEventTarget(rootView, event.target, false, false);
        return new FindResult(rootView.hashCode(), target, sibTopView);
    }

    /**
     * 计算rootView到view到距离
     */
    public static int pathLen(ViewGroup rootView, View view) {
        int len = 0;
        while (view != rootView) {
            len++;

            ViewParent vp = view.getParent();
            if (vp instanceof View) {
                view = (View) vp;
            } else {
                return -1;
            }
        }
        return len;
    }

    public static List<View> findViewByEventTarget(View rootView, EventTarget target, boolean indexClassOnly, boolean ignoreFirstElementIndex) {
        List<View> listTargetView = null;
        // 路径定位
        View pathView = null;
        if (target.path != null) {
            if (target.sibTopDistance == 0 && target.path.isEmpty()) {
                pathView = rootView;
            } else {
                pathView = findByPath(rootView, target.path, indexClassOnly, ignoreFirstElementIndex);
            }
            if (pathView == null) {
                return null;
            }
        }
        if (pathView != null) {
            if (target.propsBindingList != null && !target.propsBindingList.isEmpty()) {
                for (IProperty p : target.propsBindingList) {
                    if (!p.isMatch(pathView)) {
                        return null;
                    }
                }
            }
            List<View> list = new ArrayList<>();
            list.add(pathView);
            return list;
        }

        // 属性定位
        if (target.propsBindingList != null && !target.propsBindingList.isEmpty()) {
            listTargetView = new ArrayList<>();
            findByProperties(rootView, target.propsBindingList, listTargetView);
        }
        return listTargetView;
    }

    /**
     * 按路径查找
     */
    public static View findByPath(View view, List<PathMatcher.PathElement> path, boolean indexClassOnly, boolean ignoreFirstElementIndex) {
        View target = new PathMatcher().findViewInRoot(view, path, indexClassOnly, ignoreFirstElementIndex);
        return target;
    }

    /**
     * 按属性查找，所有属性满足预期才能定位
     */
    private static void findByProperties(View view, List<IProperty> propsBindingList, List<View> listResult) {
        if (view == null) {
            return;
        }
        boolean find = true;
        for (IProperty p : propsBindingList) {
            if (!p.isMatch(view)) {
                find = false;
                break;
            }
        }
        if (find) {
            listResult.add(view);
        }
        if (view instanceof ViewGroup) {
            ViewGroup root = (ViewGroup) view;
            int childCount = root.getChildCount();
            for (int i = 0; i < childCount; i++) {
                findByProperties(root.getChildAt(i), propsBindingList, listResult);
            }
        }
    }

}
