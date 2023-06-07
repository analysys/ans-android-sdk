package com.analysys.visual.bind.locate;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.analysys.utils.ANSLog;
import com.analysys.utils.AnalysysUtil;
import com.analysys.utils.AnsReflectUtils;
import com.analysys.utils.ExceptionUtil;
import com.analysys.visual.VisualManager;

import org.json.JSONObject;

import java.util.List;

public class PathMatcher {

    private static final String TAG = VisualManager.TAG;
    private final IndexStack mIndexStack;

    PathMatcher() {
        mIndexStack = new IndexStack();
    }

    private static boolean hasClassName(Object o, String className) {
        if (o == null) {
            return false;
        }
        Class<?> klass = o.getClass();
        while (true) {
            if (TextUtils.equals(klass.getName(), className)) {
                return true;
            }
            if (klass == Object.class) {
                return false;
            }
            klass = klass.getSuperclass();
        }
    }

    View findViewInRoot(View givenRootView, List<PathElement> path, boolean indexClassOnly, boolean ignoreFirstElement) {
        if (path.isEmpty()) {
            return givenRootView;
        }

        if (mIndexStack.full()) {
            ANSLog.w(TAG, "index stack full1");
            return null;
        }

        final PathElement rootPathElement = path.get(0);
        final List<PathElement> childPath = path.subList(1, path.size());

        final int indexKey = mIndexStack.alloc();
        final View rootView = ignoreFirstElement ? givenRootView : findViewMatch(rootPathElement, givenRootView, indexKey, indexClassOnly);
        mIndexStack.free();
        if (null != rootView) {
            return findViewInMatchedRoot(rootView, rootPathElement, childPath, indexClassOnly);
        }
        return null;
    }

    private View findViewInMatchedRoot(View alreadyMatched, PathElement rootElement, List<PathElement> remainingPath, boolean indexClassOnly) {

        if (remainingPath.isEmpty()) {
            return alreadyMatched;
        }

        if (!(alreadyMatched instanceof ViewGroup)) {
            return null;
        }

        if (mIndexStack.full()) {
            ANSLog.w(TAG, "index stack full2");
            return null;
        }

        final ViewGroup parent = (ViewGroup) alreadyMatched;
        final PathElement matchElement = remainingPath.get(0);
        final List<PathElement> nextPath = remainingPath.subList(1, remainingPath.size());

        final int childCount = parent.getChildCount();
        final int indexKey = mIndexStack.alloc();
        View find = null;
        for (int i = 0; i < childCount; i++) {
            final View givenChild = parent.getChildAt(i);
            if (AnalysysUtil.isEmptyRNGroup(givenChild)) {
                continue;
            }
            boolean ignoreChild = false;
            // ViewPager子View会销毁重建，index不可靠，只搜索当前展示的
            if(rootElement.prefix == PathElement.VIEW_PAGER_PREFIX) {
                Object itemInfo = AnsReflectUtils.invokeMethod(parent, "infoForChild", new Class[]{View.class}, new Object[]{givenChild});
                Object currentItem = AnsReflectUtils.invokeMethod(parent, "getCurrentItem");
                if (itemInfo != null && currentItem != null) {
                    Object position = AnsReflectUtils.getField(itemInfo, "position");
                    if (position != currentItem) {
                        ignoreChild = true;
                    }
                }
            }
            if (ignoreChild) {
                continue;
            }
            final View child = findViewMatch(matchElement, givenChild, indexKey, indexClassOnly);
            if (null != child) {
                find = findViewInMatchedRoot(child, matchElement, nextPath, indexClassOnly);
//                if (find == null) {
//                    ANSLog.v(VisualBindManager.TAG, "find is null: " + i + ", " + child + ", " + matchElement + ", " + nextPath + ", " + indexClassOnly);
//                }
            }
            if (matchElement.index >= 0 && mIndexStack.read(indexKey) > matchElement.index) {
                break;
            }
        }
        mIndexStack.free();
        return find;
    }

    private View findViewMatch(PathElement findElement, View subject, int indexKey, boolean indexClassOnly) {
        if (subject == null) {
            return null;
        }
        final int currentIndex = mIndexStack.read(indexKey);

        // 老path view class可能为空
//        boolean isViewClassEmpty = TextUtils.isEmpty(findElement.uniqueViewClassName) && TextUtils.isEmpty(findElement.viewClassName);
        boolean isViewClassEmpty = TextUtils.isEmpty(findElement.viewClassName);
        boolean match = false;
        if (isViewClassEmpty) {
            match = matches(findElement, subject);
            if (match) {
                mIndexStack.increment(indexKey);
            }
        } else {
            // 比较class
            boolean classMatch = false;
            // TODO 是否删除该功能？
//            if (!TextUtils.isEmpty(findElement.uniqueViewClassName)) {
//                if (UniqueViewHelper.isExtendsFromUniqueClass(subject.getClass().getName(), findElement.uniqueViewClassName)) {
//                    classMatch = true;
//                }
//            } else {
            if (hasClassName(subject, findElement.viewClassName)) {
                classMatch = true;
            }
//            }
            if (classMatch) {
                mIndexStack.increment(indexKey);
                if (indexClassOnly) {
                    match = true;
                } else {
                    match = matches(findElement, subject);
                }
            }
        }

        if (match) {
            if (findElement.index == -1 || findElement.index == currentIndex) {
                return subject;
            }
        }

        if (findElement.prefix == PathElement.SHORTEST_PREFIX && subject instanceof ViewGroup) {
            final ViewGroup group = (ViewGroup) subject;
            final int childCount = group.getChildCount();
            for (int i = 0; i < childCount; i++) {
                final View child = group.getChildAt(i);
                if (AnalysysUtil.isEmptyRNGroup(child)) {
                    continue;
                }
                final View result = findViewMatch(findElement, child, indexKey, indexClassOnly);
                if (null != result) {
                    return result;
                }
            }
        }

        return null;
    }

    /**
     * 这里会处理matchElement的逻辑:
     * 1.如果属性viewClassName存在且不与subject的Class类相同或者subject是viewClassName的子类,反回false
     * 2.如果属性viewId存在且不与subject的ID相同,返回false
     * 3.如果属性contentDescription存在且不与subject的描述信息相同,返回false
     * 4.如果属性tag存在且不与subject的tag相同,返回false
     * 5.以上4个条件都不满足,则表示matchElement所代表的view与subject的以上四种相同,返回true,
     * 后续再判断前4种情况不能确定View后,通过父控件的index值确定
     */
    private boolean matches(PathElement matchElement, View subject) {
        if (-1 != matchElement.viewId && subject.getId() != matchElement.viewId) {
            return false;
        }

        CharSequence cs = subject.getContentDescription();
        if (null != matchElement.contentDescription && cs != null
                && !matchElement.contentDescription.contentEquals(cs)) {
            return false;
        }

        final String matchTag = matchElement.tag;
        if (null != matchElement.tag) {
            final Object subjectTag = subject.getTag();
            return null != subjectTag && matchTag.equals(subject.getTag().toString());
        }

        return true;
    }

    public static class PathElement {
        public static final int ZERO_LENGTH_PREFIX = 0;
        public static final int SHORTEST_PREFIX = 1;
        public static final int VIEW_PAGER_PREFIX = 2;
        public int prefix;
        public final String viewClassName;
//        public final String uniqueViewClassName;
        public int index;
        final int viewId;
        final String viewIdName;
        final String contentDescription;
        final String tag;
        public int row;

        public PathElement(int usePrefix, String vClass, int ix, int vId, String vIdName, String cDesc,
                           String vTag, int vRow) {
            prefix = usePrefix;
            viewClassName = vClass;
//            uniqueViewClassName = vUniqueClass;
            index = ix;
            viewId = vId;
            viewIdName = vIdName;
            contentDescription = cDesc;
            tag = vTag;
            row = vRow;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof PathElement)) {
                return false;
            }
            PathElement element = (PathElement) obj;
            return prefix == element.prefix
                    && TextUtils.equals(viewClassName, element.viewClassName)
//                    && TextUtils.equals(uniqueViewClassName, element.uniqueViewClassName)
                    && index == element.index
                    && viewId == element.viewId
                    && TextUtils.equals(viewIdName, element.viewIdName)
                    && TextUtils.equals(contentDescription, element.contentDescription)
                    && TextUtils.equals(tag, element.tag);
        }

        public JSONObject toJsonObject() {
            try {
                final JSONObject ret = new JSONObject();
                if (prefix == SHORTEST_PREFIX) {
                    ret.put("prefix", "shortest");
                }
                if (null != viewClassName) {
                    ret.put("view_class", viewClassName);
                }
//                if (null != uniqueViewClassName) {
//                    ret.put("view_class_unique", uniqueViewClassName);
//                }
                if (index > -1) {
                    ret.put("index", index);
                }
                if (viewId > -1) {
                    ret.put("id", viewId);
                }
                if (null != viewIdName) {
                    ret.put("idName", viewIdName);
                }
                if (null != contentDescription) {
                    ret.put("contentDescription", contentDescription);
                }
                if (null != tag) {
                    ret.put("tag", tag);
                }
                return ret;
            } catch (Throwable ignore) {
                ExceptionUtil.exceptionThrow(ignore);
            }
            return null;
        }

        @Override
        public String toString() {
            JSONObject jo = toJsonObject();
            if (jo != null) {
                return jo.toString();
            }
            return "";
        }
    }

    private static class IndexStack {
        private final int[] mStack;
        private int mStackSize;

        IndexStack() {
            mStack = new int[256];
            mStackSize = 0;
        }

        boolean full() {
            return mStack.length == mStackSize;
        }

        int alloc() {
            final int index = mStackSize;
            mStackSize++;
            mStack[index] = 0;
            return index;
        }

        int read(int index) {
            return mStack[index];
        }

        void increment(int index) {
            mStack[index]++;
        }

        void free() {
            mStackSize--;
            if (mStackSize < 0) {
                throw new ArrayIndexOutOfBoundsException(mStackSize);
            }
        }
    }
}
