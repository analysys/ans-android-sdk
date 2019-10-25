package com.analysys.visual.viewcrawler;

import android.view.View;
import android.view.ViewGroup;

import com.analysys.utils.InternalAgent;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;


class Pathfinder {


    private static final String LOGTAG = "PathFinder";
    private final IntStack mIndexStack;

    public Pathfinder() {
        mIndexStack = new IntStack();
    }

    private static boolean hasClassName(Object o, String className) {
        Class<?> klass = o.getClass();
        while (true) {
            if (klass.getCanonicalName().equals(className)) {
                return true;
            }
            if (klass == Object.class) {
                return false;
            }
            klass = klass.getSuperclass();
        }
    }

    public void findTargetsInRoot(View givenRootView, List<PathElement> path,
                                  Accumulator accumulator) {
        if (path.isEmpty()) {
            return;
        }

        if (mIndexStack.full()) {
            InternalAgent.w(LOGTAG, "There appears to be a concurrency issue in the pathfinding " +
                    "code. Path will not be matched.");
            return; // No memory to perform the find.
        }

        final PathElement rootPathElement = path.get(0);
        final List<PathElement> childPath = path.subList(1, path.size());

        final int indexKey = mIndexStack.alloc();
        final View rootView = findPrefixedMatch(rootPathElement, givenRootView, indexKey);
        mIndexStack.free();

        if (null != rootView) {
            findTargetsInMatchedView(rootView, childPath, accumulator);
        }
    }

    private void findTargetsInMatchedView(View alreadyMatched, List<PathElement> remainingPath,
                                          Accumulator accumulator) {
        // When this is run, alreadyMatched has already been matched to a path prefix.
        // path is a possibly empty "remaining path" suffix left over after the match

        if (remainingPath.isEmpty()) {
            // Nothing left to match- we're found!
            accumulator.accumulate(alreadyMatched);
            return;
        }

        if (!(alreadyMatched instanceof ViewGroup)) {
            // Matching a non-empty path suffix is impossible, because we have no children
            return;
        }

        if (mIndexStack.full()) {
            InternalAgent.v(LOGTAG, "Path is too deep, will not match");
            // Can't match anyhow, stack is too deep
            return;
        }

        final ViewGroup parent = (ViewGroup) alreadyMatched;
        final PathElement matchElement = remainingPath.get(0);
        final List<PathElement> nextPath = remainingPath.subList(1, remainingPath.size());

        final int childCount = parent.getChildCount();
        final int indexKey = mIndexStack.alloc();
        for (int i = 0; i < childCount; i++) {
            final View givenChild = parent.getChildAt(i);
            final View child = findPrefixedMatch(matchElement, givenChild, indexKey);
            if (null != child) {
                findTargetsInMatchedView(child, nextPath, accumulator);
            }
            if (matchElement.index >= 0 && mIndexStack.read(indexKey) > matchElement.index) {
                break;
            }
        }
        mIndexStack.free();
    }

    // Finds the first matching view of the path element in the given subject's view hierarchy.
    // If the path is indexed, it needs a start index, and will consume some indexes
    private View findPrefixedMatch(PathElement findElement, View subject, int indexKey) {
        final int currentIndex = mIndexStack.read(indexKey);
        if (matches(findElement, subject)) {
            mIndexStack.increment(indexKey);
            if (findElement.index == -1 || findElement.index == currentIndex) {
                return subject;
            }
        }

        //TODO 需要满足ListView中Web端只埋一点，使ListView所有item都生效的需求应该在这里
        if (findElement.prefix == PathElement.SHORTEST_PREFIX && subject instanceof ViewGroup) {
            final ViewGroup group = (ViewGroup) subject;
            final int childCount = group.getChildCount();
            for (int i = 0; i < childCount; i++) {
                final View child = group.getChildAt(i);
                final View result = findPrefixedMatch(findElement, child, indexKey);
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
     *
     * @param matchElement
     * @param subject
     * @return
     */
    private boolean matches(PathElement matchElement, View subject) {
        if (null != matchElement.viewClassName &&
                !hasClassName(subject, matchElement.viewClassName)) {
            return false;
        }

        if (-1 != matchElement.viewId && subject.getId() != matchElement.viewId) {
            return false;
        }

        if (null != matchElement.contentDescription &&
                !matchElement.contentDescription.equals(subject.getContentDescription())) {
            return false;
        }

        final String matchTag = matchElement.tag;
        if (null != matchElement.tag) {
            final Object subjectTag = subject.getTag();
            if (null == subjectTag || !matchTag.equals(subject.getTag().toString())) {
                return false;
            }
        }

        return true;
    }

    public interface Accumulator {
        public void accumulate(View v);
    }

    public static class PathElement {
        public static final int ZERO_LENGTH_PREFIX = 0;
        public static final int SHORTEST_PREFIX = 1;
        public final int prefix;
        public final String viewClassName;
        public final int index;
        public final int viewId;
        public final String contentDescription;
        public final String tag;

        public PathElement(int usePrefix, String vClass, int ix, int vId, String cDesc,
                           String vTag) {
            prefix = usePrefix;
            viewClassName = vClass;
            index = ix;
            viewId = vId;
            contentDescription = cDesc;
            tag = vTag;
        }

        @Override
        public String toString() {
            try {
                final JSONObject ret = new JSONObject();
                if (prefix == SHORTEST_PREFIX) {
                    ret.put("prefix", "shortest");
                }
                if (null != viewClassName) {
                    ret.put("view_class", viewClassName);
                }
                if (index > -1) {
                    ret.put("index", index);
                }
                if (viewId > -1) {
                    ret.put("id", viewId);
                }
                if (null != contentDescription) {
                    ret.put("contentDescription", contentDescription);
                }
                if (null != tag) {
                    ret.put("tag", tag);
                }
                return ret.toString();
            } catch (final JSONException e) {
                throw new RuntimeException("Can't serialize PathElement to String", e);
            }
        }
    }

    /**
     * Bargain-bin pool of integers, for use in avoiding allocations during path crawl
     */
    private static class IntStack {
        private static final int MAX_INDEX_STACK_SIZE = 256;
        private final int[] mStack;
        private int mStackSize;

        public IntStack() {
            mStack = new int[MAX_INDEX_STACK_SIZE];
            mStackSize = 0;
        }

        public boolean full() {
            return mStack.length == mStackSize;
        }

        /**
         * Pushes a new value, and returns the index you can use to increment and read that value
         * later.
         */
        public int alloc() {
            final int index = mStackSize;
            mStackSize++;
            mStack[index] = 0;
            return index;
        }

        /**
         * Gets the value associated with index. index should be the result of a previous call to
         * alloc()
         */
        public int read(int index) {
            return mStack[index];
        }

        public void increment(int index) {
            mStack[index]++;
        }

        /**
         * Should be matched to each call to alloc. Once free has been called, the key associated
         * with the
         * matching alloc should be considered invalid.
         */
        public void free() {
            mStackSize--;
            if (mStackSize < 0) {
                throw new ArrayIndexOutOfBoundsException(mStackSize);
            }
        }
    }
}
