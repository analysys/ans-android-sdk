package com.analysys.process;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.analysys.ui.UniqueViewHelper;
import com.analysys.utils.ANSLog;
import com.analysys.utils.ExceptionUtil;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @Copyright © 2019 analysys Inc. All rights reserved.
 * @Description: path生成
 * @Version: 1.0
 * @Create: Apr 1, 2019 2:50:17 PM
 * @Author: sanbo
 */
public class PathGeneral {
    private boolean isDebug = false;
    private int mContentID = -1;
    private ArrayList<JSONObject> mTempPath;
    /**
     * 结束标志
     */
    private String[] mNosupportEntryName = new String[]{"action_bar", "content", "decor_content_parent"};
    private String[] mNoSupportList = new String[]{
            // 页面悬浮
            "com.android.internal.widget.ActionBarOverlayLayout", "com.android.internal.policy.DecorView",
            // 页面的actionbar
            "com.android.internal.widget.ActionBarView", "com.android.internal.widget.ActionBarContainer"
    };

    private PathGeneral() {
    }

    public static PathGeneral getInstance() {
        return Holder.Instance;
    }

    /**
     * <pre>
     * 需要参数:
     * 1. id: 来源 view.getId()
     * 2. hashCode: 来源 view.hashCode()
     * 3. contentDescription: 来源 view.getContentDescription()
     * 4. tag: 来源 view.getTag()
     * 5. mp_id_name: 来源 当view.getId不为-1时，mResourceIds.nameForId(viewId)
     * 6. view_class: 来源 view.getClass().getCanonicalName() 自己和父节点的类名字
     * </pre>
     */
    public String general(View view) {
        try {
            if (isFinalPoint(view)) {
                return null;
            }
            // 备用ID清空
            mContentID = -1;
            // 清理遍历
            if (mTempPath != null) {
                mTempPath.clear();
            } else {
                mTempPath = new ArrayList<>();
            }
            // 获取可变性path
            getDynamicPath(view);
            if (mTempPath.size() > 0 && mContentID != -1) {
                // 加上固定的头
                getIntrinsicPath();
                if (mTempPath.size() > 0) {
                    // 反转
                    Collections.reverse(mTempPath);
                }
            } else {
                mTempPath.clear();
            }
            mContentID = -1;
        } catch (Throwable ignore) {
            ExceptionUtil.exceptionThrow(ignore);
            //ANSLog.e(e);
        }
        return String.valueOf(mTempPath);
    }

    /**
     * 增加固定有的头
     */
    private void getIntrinsicPath() {
        try {
            JSONObject o = new JSONObject();
            o.put("prefix", "shortest");
            o.put("index", 0);
            o.put("id", mContentID);
            mTempPath.add(o);
        } catch (Throwable ignore) {
            ExceptionUtil.exceptionThrow(ignore);
            //ANSLog.e(e);
        }
    }

    /**
     * 获取组件可变部分的path
     */
    private void getDynamicPath(View view) {
        try {
            JSONObject obj = new JSONObject();
            // 是否到结束节点
            if (!isFinalPoint(view)) {
                // 获取唯一id和index
                getUniquePath(obj, view);
                // 该组件标志获取成功则赋予内存遍历
                if (obj.length() > 0) {
                    mTempPath.add(obj);
                }
                ViewParent vvp = view.getParent();
                if (vvp instanceof View) {
                    // 取父节点
                    getDynamicPath((View) vvp);
                }
            }
        } catch (Throwable ignore) {
            ExceptionUtil.exceptionThrow(ignore);
            //ANSLog.e(e);
        }
    }

    /**
     * <pre>
     * 获取唯一id.ID获取原则:
     *  1. 多个id共存时，mpid不参与唯一性id
     *  2. 单个id存在，三个有哪个是哪个
     * </pre>
     */
    private void getUniquePath(JSONObject obj, View view) {
        try {
            if (obj == null) {
                obj = new JSONObject();
            }
            // 基于cd和tag可并存原则，只要非空都获取
            CharSequence contentDescription = view.getContentDescription();
            if (!TextUtils.isEmpty(contentDescription)) {
                obj.put("contentDescription", contentDescription);
                obj.put("index", 0);
            }
            Object tagObj = view.getTag();
            if (tagObj != null) {
                if (tagObj instanceof CharSequence) {
                    obj.put("tag", tagObj.toString());
                    obj.put("index", 0);
                }
            }
            // contentDescription和MPID互斥
            if (TextUtils.isEmpty(contentDescription)) {
                int viewId = view.getId();
                if (viewId != -1) {
                    String mpid = SystemIds.getInstance().nameFromId(view.getResources(), viewId);
                    if (!TextUtils.isEmpty(mpid)) {
                        // 特殊处理
                        ViewParent parent = view.getParent();
                        if (parent != null) {
                            if (!UniqueViewHelper.isExtendsFromUniqueClass(parent.getClass().getName(), UniqueViewHelper.UNIQUE_CLZ_RECYCLER_VIEW)) {
                                obj.put("mp_id_name", mpid);
                                obj.put("index", 0);
                            }
                        }
                    }
                }
            }
            if (obj.length() == 0) {
                updateIndexWhenUseViewClass(obj, view);
            }
        } catch (Throwable ignore) {
            ExceptionUtil.exceptionThrow(ignore);
            //ANSLog.e(e);
        }
    }

    /**
     * 根据view_class定位的时候回去index
     */
    private void updateIndexWhenUseViewClass(JSONObject obj, View view) throws Throwable {
        // 当三个id都没有的时候，去类型
        String clazz = view.getClass().getCanonicalName();
        if (!TextUtils.isEmpty(clazz)) {
            obj.put("view_class", clazz);
            // 只有使用view_class时,才获取兄弟排序
            final int viewIdx = PathGeneral.getInstance().getIndex(view);
            obj.put("index", viewIdx);
        }
    }

    /**
     * 获取兄弟排序。默认规则:
     * 其他人包含自己类型 排序index+1
     * 涉及api
     * 获取类名称: getCanonicalName()
     * 获取父类父类: getSuperclass()
     */
    public int getIndex(View view) {
        int index = 0;
        // 获取自己类型名称
        String selfClass = view.getClass().getCanonicalName();
        if (isDebug) {
            ANSLog.w("getIndex selfClass:" + selfClass);
        }
        // 获取父节点
        ViewParent vp = view.getParent();
        if (vp instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) vp;
            int count = vg.getChildCount();
            // 大于1个元素才会遍历
            if (count > 1) {
                for (int i = 0; i < count; i++) {
                    View temp = vg.getChildAt(i);
                    if (temp == null) {
                        return -1;
                    }
                    if (isDebug) {
                        ANSLog.w("getIndex 兄弟节点:" + selfClass);
                    }
                    // 到自己停止
                    if (temp.equals(view)) {
                        return index;
                    }
                    // 对比类型
                    if (isContextType(temp, selfClass)) {
                        index += 1;
                    }
                }
            }
        }
        return index;
    }

    /**
     * 结束节点:
     * 1. mpid是 android:content
     * 2. 达到边界支持类型
     * 3. actionbar
     */
    protected boolean isFinalPoint(View view) {
        int viewId = view.getId();
        if (viewId != -1) {
            String mpid = SystemIds.getInstance().nameFromId(view.getResources(), viewId);
            if (!TextUtils.isEmpty(mpid) && "android:content".equals(mpid)) {
                mContentID = viewId;
                return true;
            }
        }
        // 通过 EntryName来识别 action_bar/content/decor_content_parent
        try {
            String entryname = view.getResources().getResourceEntryName(viewId);
            if (!TextUtils.isEmpty(entryname) && Arrays.asList(mNosupportEntryName).contains(entryname)) {
                return true;
            } else {
                return checkClass(view);
            }
        } catch (Throwable ignore) {
//            ExceptionUtil.exceptionThrow(ignore);
            return checkClass(view);
        }
    }

    private boolean checkClass(View view) {
        String viewClass = view.getClass().getCanonicalName();
        return Arrays.asList(mNoSupportList).contains(viewClass);
    }

    /**
     * 判断类型是否包含
     */
    private boolean isContextType(View temp, String selfClass) {
        List<String> types = new ArrayList<>();
        Class<?> klass = temp.getClass();
        while (klass != null && klass != Object.class) {
            types.add(klass.getCanonicalName());
            klass = klass.getSuperclass();
        }
        return types.contains(selfClass);
    }

    private static class Holder {
        private static PathGeneral Instance = new PathGeneral();
    }
}
