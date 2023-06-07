package com.analysys.visual.bind.event;

import android.text.TextUtils;
import android.widget.AdapterView;

import com.analysys.process.SystemIds;
import com.analysys.ui.UniqueViewHelper;
import com.analysys.utils.ANSLog;
import com.analysys.utils.AnsReflectUtils;
import com.analysys.visual.bind.locate.PathMatcher;
import com.analysys.visual.bind.property.IProperty;
import com.analysys.visual.bind.property.PropertyFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @Copyright © 2019 Analysys Inc. All rights reserved.
 * @Description: 事件目标类
 * @Create: 2019-11-28 09:57
 * @author: hcq
 */
public class EventTarget {

    private static final List<PathMatcher.PathElement> NEVER_MATCH_PATH = Collections.emptyList();

    /**
     * 按路径定位
     */
    public List<PathMatcher.PathElement> path;

    /**
     * 老版本path的字符串，用来比较埋点
     */
    public String oldPathStr;

    /**
     * 是否为老版本埋点，只有path字段
     */
    public boolean isOldPath;

    // bind target by properties, ignore path
    /**
     * 按属性定位
     */
    public List<IProperty> propsBindingList;
//    private String forceBindingStr;

    public class SibTop {
        /**
         * 同级元素指定位置
         */
        public int row = -1;

        /**
         * 同级容器类名
         */
        public String sibClassName;

        public int pathIdx;

        SibTop(String sibClassName, int pathIdx, int row) {
            this.sibClassName = sibClassName;
            this.pathIdx = pathIdx;
            this.row = row;
        }

        @Override
        public String toString() {
            return sibClassName + " " + row + " " + pathIdx;
        }
    }

    public boolean isFixSibRow;

    public List<SibTop> listSibTop;

    /**
     * 标识原生关联h5
     */
    public String h5Path;

    /**
     * 查找关联元素时会先以当前触发元素为起点上溯该距离查找根元素，再以根元素查找关联元素
     */
    public int sibTopDistance = -1;

    EventTarget(JSONObject joTarget, boolean isHybrid) throws JSONException, ClassNotFoundException {
        JSONArray jaNewPath = joTarget.optJSONArray("new_path");
        JSONArray jaPB = joTarget.optJSONArray("props_binding");
        h5Path = joTarget.optString("h5_path", null);
        oldPathStr = joTarget.optString("path", null);

        if (jaNewPath != null && jaNewPath.length() != 0 && !isHybrid) {
            path = readPath(jaNewPath);
        }
        if (jaPB != null && jaPB.length() != 0) {
            propsBindingList = new ArrayList<>();
            for (int i = 0; i < jaPB.length(); i++) {
                IProperty pro = PropertyFactory.createProperty(jaPB.getJSONObject(i));
                if (pro != null) {
                    propsBindingList.add(pro);
                }
            }
        }

        if (path == null && propsBindingList == null && !isHybrid) {
            if (!TextUtils.isEmpty(oldPathStr)) {
                path = readPath(new JSONArray(oldPathStr));
                isOldPath = true;
            }
        }

        sibTopDistance = joTarget.optInt("step", -1);
    }

//    public EventTarget(List<PathMatcher.PathElement> vPath, String clzName) {
//        for (int i = vPath.size() - 1; i >= 0; i--) {
//            if (TextUtils.equals(vPath.get(i).viewClassName, clzName)) {
//                path = vPath.subList(0, i + 1);
//                break;
//            }
//        }
//    }

    public EventTarget(List<PathMatcher.PathElement> vPath, int idx) {
        path = vPath.subList(0, idx);
    }

//    static String path2Str(List<PathMatcher.PathElement> listElement) {
//        if (listElement == null || listElement.isEmpty()) {
//            return "";
//        }
//        JSONArray jaElement = new JSONArray();
//        for (PathMatcher.PathElement element : listElement) {
//            jaElement.put(element.toJsonObject());
//        }
//        return jaElement.toString();
//    }

    private List<PathMatcher.PathElement> readPath(JSONArray pathDesc) throws JSONException {
        final List<PathMatcher.PathElement> path = new ArrayList<>();

        for (int i = pathDesc.length() - 1; i >= 0; i--) {
            final JSONObject targetView = pathDesc.getJSONObject(i);

            final String prefixCode = targetView.optString("prefix", null);
            final String targetViewClass = targetView.optString("view_class", null);
//            final String targetUniqueViewClass = targetView.optString("view_class_unique", null);
            final int targetIndex = targetView.optInt("index", -1);
            final String targetDescription = targetView.optString("contentDescription", null);
            final int targetExplicitId = targetView.optInt("id", -1);
            final String targetIdName = targetView.optString("mp_id_name", null);
            final String targetTag = targetView.optString("tag", null);
            final int row = targetView.optInt("row", -1);

            final int prefix;
            if ("shortest".equals(prefixCode)) {
                prefix = PathMatcher.PathElement.SHORTEST_PREFIX;
            } else if (null == prefixCode) {
                prefix = PathMatcher.PathElement.ZERO_LENGTH_PREFIX;
            } else {
                return NEVER_MATCH_PATH;
            }

            final int targetId;

            final Integer targetIdOrNull = reconcileIds(targetExplicitId, targetIdName);
            if (null == targetIdOrNull) {
                return NEVER_MATCH_PATH;
            } else {
                targetId = targetIdOrNull;
            }

            path.add(0, new PathMatcher.PathElement(prefix, targetViewClass, targetIndex, targetId, targetIdName,
                    targetDescription, targetTag, row));

            if (row != -1) {
                isFixSibRow = true;
            }

            if (targetViewClass != null) {
                boolean isViewPager = UniqueViewHelper.isExtendsFromUniqueClass(targetViewClass, UniqueViewHelper.UNIQUE_CLZ_VIEW_PAGER);
                if (AnsReflectUtils.isSubClass(AdapterView.class, targetViewClass)
                        || UniqueViewHelper.isExtendsFromUniqueClass(targetViewClass, UniqueViewHelper.UNIQUE_CLZ_RECYCLER_VIEW)
                        || isViewPager) {
                    if (path.size() > 1) {
                        path.get(1).index = 0;
                        if (listSibTop == null) {
                            listSibTop = new ArrayList<>();
                        }
                        listSibTop.add(0, new SibTop(targetViewClass, i, path.get(1).row));
                        if (isViewPager) {
                            path.get(0).prefix = PathMatcher.PathElement.VIEW_PAGER_PREFIX;
                        }
                    }
                }
            }
        }

        return path;
    }

//    if (target == null) {
//        // 部分容器如ViewPager子View首次展示不会显示全，导致index超出的情况下定位失败
//        if (sibTopView != null) {
//            List<Pathfinder.PathElement> path = event.target.path;
//            for (int i = 0; i < path.size(); i++) {
//                Pathfinder.PathElement element = path.get(i);
//                if (TextUtils.equals(element.viewClassName, event.target.sibClassName)) {
//                    if (i + 1 < path.size()) {
//                        path.get(i + 1).index = 0;
//                        target = findViewByEventTarget(rootView, event.target, false, false);
//                        break;
//                    }
//                }
//            }
//        }
//    }

    // May return null (and log a warning) if arguments cannot be reconciled
    private Integer reconcileIds(int explicitId, String idName) {
        final int idFromName;
        if (null != idName) {
            idFromName = SystemIds.getInstance().idFromName(null, idName);
            if (idFromName == -1) {
                ANSLog.w("idFromName == -1 idName =" + idName);
                return null;
            }
        } else {
            idFromName = -1;
        }

        if (-1 != idFromName && -1 != explicitId && idFromName != explicitId) {
            ANSLog.w("Path contains both a named and an explicit id, and they don't match. No" +
                    " views will be matched.");
            return null;
        }

        if (-1 != idFromName) {
            return idFromName;
        }

        return explicitId;
    }

//    @Override
//    public boolean equals(@Nullable Object obj) {
//        if (!(obj instanceof EventTarget)) {
//            return false;
//        }
//        EventTarget eventTarget = (EventTarget) obj;
//        if (!TextUtils.isEmpty(eventTarget.forceBindingStr)
//                && !TextUtils.isEmpty(forceBindingStr)
//                && eventTarget.forceBindingStr.equals(forceBindingStr)) {
//            return true;
//        }
//        return !TextUtils.isEmpty(eventTarget.pathStr)
//                && !TextUtils.isEmpty(pathStr)
//                && eventTarget.pathStr.equals(pathStr);
//    }
}
