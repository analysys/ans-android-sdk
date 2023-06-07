package com.analysys.visual.cmd;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.LruCache;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.AbsListView;
import android.widget.AbsoluteLayout;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.analysys.process.SystemIds;
import com.analysys.ui.UniqueViewHelper;
import com.analysys.ui.WindowUIHelper;
import com.analysys.ui.WindowUIHelper.PageRootInfo;
import com.analysys.ui.WindowUIHelper.PageViewBitmap;
import com.analysys.utils.ActivityLifecycleUtils;
import com.analysys.utils.AnalysysUtil;
import com.analysys.utils.AnsReflectUtils;
import com.analysys.utils.ExceptionUtil;
import com.analysys.visual.utils.WebViewBindHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

class PageViewCapture {

    private final RootViewCapture mRootViewCapture;
    private final List<PageViewInfo> mListPageViewInfo;
    private final ViewClassCache mViewClassCache;
    private final Handler mMainHandler;

    PageViewCapture(List<PageViewInfo> properties) {
        mListPageViewInfo = properties;
        mMainHandler = new Handler(Looper.getMainLooper());
        mRootViewCapture = new RootViewCapture();
        mViewClassCache = new ViewClassCache(300);
    }

    private String mPageSignCache;
    private boolean mContainsWebView;

    boolean checkAndUpdateChange(List<PageRootInfo> infoList) {
        StringBuilder sbSign = new StringBuilder();
        for (PageRootInfo info : infoList) {
            sbSign.append(info.getSign());
        }
        String sign = sbSign.toString();
        if (TextUtils.isEmpty(sign) || !TextUtils.equals(mPageSignCache, sign) || WebViewBindHelper.getInstance().isVisualDomListChanged()) {
            mPageSignCache = sign;
            return true;
        }
        return false;
    }

    List<PageRootInfo> getListRootViewInfo() {
        final FutureTask<List<PageRootInfo>> infoFuture =
                new FutureTask<>(mRootViewCapture);
        mMainHandler.post(infoFuture);
        try {
            List<PageRootInfo> infoList = Collections.emptyList();
            infoList = infoFuture.get(5, TimeUnit.SECONDS);
            return infoList;
        } catch (Throwable ignore) {
            ExceptionUtil.exceptionPrint(ignore);
        }
        return null;
    }

    /**
     * 替代java.io.OutputStreamWriter，提高效率
     */
    class OutputStreamWriter {

        OutputStream out;
        StringBuilder sbTxt;

        public OutputStreamWriter(OutputStream out) {
            this.out = out;
            sbTxt = new StringBuilder();
        }

        public void write(String s) {
            sbTxt.append(s);
        }

        public void flush() {
            try {
                byte[] data = sbTxt.toString().getBytes();
//                ANSLog.i(TAG, "capture send data size: " + data.length);
                out.write(data);
                sbTxt = new StringBuilder();
            } catch (Throwable ignore) {
                ExceptionUtil.exceptionThrow(ignore);
            }
        }
    }

    /**
     * 替代android.util.JsonWriter，提高效率
     */
    class JsonWriter {
        boolean needComma;
        OutputStreamWriter writer;

        public JsonWriter(OutputStreamWriter writer) {
            this.writer = writer;
        }

        public void beginObject() {
            if (needComma) {
                writer.sbTxt.append(",");
            }
            writer.sbTxt.append("{");
            needComma = false;
        }

        public JsonWriter name(String s) {
            if (needComma) {
                writer.sbTxt.append(",");
            }
            writer.sbTxt.append("\"").append(s).append("\":");
            needComma = false;
            return this;
        }

        public void value(int i) {
            if (needComma) {
                writer.sbTxt.append(",");
            }
            writer.sbTxt.append(i);
            needComma = true;
        }

        public void endObject() {
            writer.sbTxt.append("}");
            needComma = true;
        }

        public void flush() {
        }

        public void beginArray() {
            if (needComma) {
                writer.sbTxt.append(",");
            }
            writer.sbTxt.append("[");
            needComma = false;
        }

        public void endArray() {
            writer.sbTxt.append("]");
            needComma = true;
        }

        public void nullValue() {
            if (needComma) {
                writer.sbTxt.append(",");
            }
            writer.sbTxt.append("null");
            needComma = true;
        }

        public void value(String value) {
            if (needComma) {
                writer.sbTxt.append(",");
            }
            if (value == null) {
                writer.sbTxt.append("null");
            } else {
                writer.sbTxt.append("\"");
                for (int i = 0, length = value.length(); i < length; i++) {
                    char c = value.charAt(i);
                    switch (c) {
                        case '"':
                        case '\\':
                            writer.sbTxt.append('\\');
                            writer.sbTxt.append(c);
                            break;

                        case '\t':
                            writer.sbTxt.append("\\t");
                            break;

                        case '\b':
                            writer.sbTxt.append("\\b");
                            break;

                        case '\n':
                            writer.sbTxt.append("\\n");
                            break;

                        case '\r':
                            writer.sbTxt.append("\\r");
                            break;

                        case '\f':
                            writer.sbTxt.append("\\f");
                            break;

                        case '\u2028':
                        case '\u2029':
                            writer.sbTxt.append(String.format("\\u%04x", (int) c));
                            break;

                        default:
                            if (c <= 0x1F) {
                                writer.sbTxt.append(String.format("\\u%04x", (int) c));
                            } else {
                                writer.sbTxt.append(c);
                            }
                            break;
                    }

                }
                writer.sbTxt.append("\"");
            }
            needComma = true;
        }

        public void value(Object value) {
            if (needComma) {
                writer.sbTxt.append(",");
            }
            writer.sbTxt.append(value == null ? "null" : value);
            needComma = true;
        }
    }

    void capture(OutputStream out, List<PageRootInfo> infoList) throws IOException, JSONException {
        mContainsWebView = false;
        final OutputStreamWriter writer = new OutputStreamWriter(out);
        writer.write("[");

        final int infoCount = infoList.size();
        boolean isRNPage = AnalysysUtil.isRNPage();
        String rnUrl = AnalysysUtil.getRNUrl();
        for (int i = 0; i < infoCount; i++) {
            if (i > 0) {
                writer.write(",");
            }
            final PageRootInfo info = infoList.get(i);
            writer.write("{");
            writer.write("\"activity\":");
            writer.write(JSONObject.quote(isRNPage && !TextUtils.isEmpty(rnUrl) ? rnUrl : info.activityName));
            writer.write(",");
            writer.write("\"scale\":");
            writer.write(String.format("%s", info.scale));
            writer.write(",");
            writer.write("\"serialized_objects\":");
            {
                final JsonWriter j = new JsonWriter(writer);
                j.beginObject();
                j.name("rootObject").value(info.rootView.hashCode());
                j.name("objects");
                captureViewHierarchy(writer, j, info.rootView);
                j.endObject();
                j.flush();
            }
            writer.write(",");
            writer.write("\"screenshot\":");
            writer.flush();
            info.screenshot.writeBitmapJSON(Bitmap.CompressFormat.PNG, 100, out);
            writer.write("}");
        }

        writer.write("]");
        writer.flush();
    }

    private void captureViewHierarchy(OutputStreamWriter writer, JsonWriter j, View rootView) throws IOException, JSONException {
        j.beginArray();
        captureView(writer, j, rootView);
        j.endArray();
    }

    private void captureView(OutputStreamWriter writer, JsonWriter j, View view) throws IOException, JSONException {
        int viewId = view.getId();
        String viewIdName = null;
        if (-1 != viewId) {
            viewIdName = SystemIds.getInstance().nameFromId(view.getResources(), viewId);
        }

        // 特殊处理
        if (viewIdName != null) {
            ViewParent parent = view.getParent();
            if (parent != null) {
                if (UniqueViewHelper.isExtendsFromUniqueClass(parent.getClass().getName(), UniqueViewHelper.UNIQUE_CLZ_RECYCLER_VIEW)) {
                    viewId = -1;
                    viewIdName = null;
                }
            }
        }

        j.beginObject();
        int hashCode = view.hashCode();
        j.name("hashCode").value(hashCode);
        j.name("invisible").value(isInvisible(view));
        j.name("id").value(viewId);
        if (null == viewIdName) {
            j.name("mp_id_name").nullValue();
        } else {
            j.name("mp_id_name").value(viewIdName);
        }

        final CharSequence description = view.getContentDescription();
        if (null == description) {
            j.name("contentDescription").nullValue();
        } else {
            j.name("contentDescription").value(description.toString());
        }

        final Object tag = view.getTag();
        if (null == tag) {
            j.name("tag").nullValue();
        } else if (tag instanceof CharSequence) {
            j.name("tag").value(tag.toString());
        }

        int rowIdx = getRowIdx(view);
        if (rowIdx != -1) {
            j.name("row").value(rowIdx);
        }

        j.name("top").value(view.getTop());
        j.name("left").value(view.getLeft());
        j.name("width").value(view.getWidth());
        j.name("height").value(view.getHeight());
        j.name("scrollX").value(view.getScrollX());
        j.name("scrollY").value(view.getScrollY());
        j.name("visibility").value(view.getVisibility());

        int[] outLocale = new int[2];
        // 相对整个屏幕的绝对坐标(包含状态栏)
        view.getLocationOnScreen(outLocale);
        // 相对window的绝对坐标.(不包含任务栏)
        // view.getLocationInWindow(outLocale);
        j.name("atop").value(outLocale[1]);
        j.name("aleft").value(outLocale[0]);

        float translationX = 0;
        float translationY = 0;
        translationX = view.getTranslationX();
        translationY = view.getTranslationY();

        j.name("translationX").value(translationX);
        j.name("translationY").value(translationY);

//        String uniqueClzName = null;
        j.name("classes");
        j.beginArray();
        Class<?> klass = view.getClass();
        do {
            j.value(mViewClassCache.get(klass));
//            if (TextUtils.isEmpty(uniqueClzName)) {
//                uniqueClzName = UniqueViewHelper.getUniqueClzName(klass.getName());
//            }
            klass = klass.getSuperclass();
        } while (klass != Object.class && klass != null);
        j.endArray();

//        // TODO 需要服务端和前端支持
//        if (!TextUtils.isEmpty(uniqueClzName)) {
//            j.name("view_class_unique").value(uniqueClzName);
//        }
        addProperties(j, view);

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (layoutParams instanceof RelativeLayout.LayoutParams) {
            RelativeLayout.LayoutParams relativeLayoutParams =
                    (RelativeLayout.LayoutParams) layoutParams;
            int[] rules = relativeLayoutParams.getRules();
            j.name("layoutRules");
            j.beginArray();
            for (int rule : rules) {
                j.value(rule);
            }
            j.endArray();
        }

        if (UniqueViewHelper.isWebView(view.getClass())) {
            mContainsWebView = true;
            String nodeInfo = WebViewBindHelper.getInstance().getVisualDomList(view);
            if (nodeInfo != null) {
                j.name("h5_view").value((Object) nodeInfo);
            }
            // 服务器未做判空处理，设置为空即可
            j.name("subviews");
            j.beginArray();
            j.endArray();
            j.endObject();
        } else {
            j.name("subviews");
            j.beginArray();
            if (view instanceof ViewGroup) {
                final ViewGroup group = (ViewGroup) view;
                final int childCount = group.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    final View child = group.getChildAt(i);
                    // child can be null when views are getting disposed.
                    if (null != child && !AnalysysUtil.isEmptyRNGroup(child)) {
                        j.value(child.hashCode());
                    }
                }
            }
            j.endArray();
            j.endObject();
            // 所有节点都上传，防止path解析混乱
            if (view instanceof ViewGroup) {
                final ViewGroup group = (ViewGroup) view;
                final int childCount = group.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    final View child = group.getChildAt(i);
                    // child can be null when views are getting disposed.
                    if (null != child && !AnalysysUtil.isEmptyRNGroup(child)) {
                        captureView(writer, j, child);
                    }
                }
            }
        }
    }

    private int getRowIdx(View view) {
        if (view == null) {
            return -1;
        }
        ViewParent vp = view.getParent();
        if (!(vp instanceof ViewGroup)) {
            return -1;
        }
        if (UniqueViewHelper.isExtendsFromUniqueClass(vp.getClass().getName(), UniqueViewHelper.UNIQUE_CLZ_RECYCLER_VIEW)) {
            Object position = AnsReflectUtils.invokeMethod(vp, "getChildAdapterPosition",
                    new Class[]{View.class}, new Object[]{view});
            return position == null ? -1 : (int) position;
        } else if (vp instanceof AdapterView) {
            return ((AdapterView) vp).getPositionForView(view);
        } else if (UniqueViewHelper.isExtendsFromUniqueClass(vp.getClass().getName(), UniqueViewHelper.UNIQUE_CLZ_VIEW_PAGER)) {
            Object itemInfo = AnsReflectUtils.invokeMethod(vp, "infoForChild", new Class[]{View.class}, new Object[]{view});
            if (itemInfo != null) {
                Object position = AnsReflectUtils.getField(itemInfo, "position");
                return position == null ? -1 : (int) position;
            }
        }
        return -1;
    }

    /**
     * 判断 View 是否隐藏
     */
    private boolean isInvisible(View view) {
        int visibility = view.getVisibility();
        return visibility == View.INVISIBLE || visibility == View.GONE
                || !view.getGlobalVisibleRect(new Rect())
                || !view.getLocalVisibleRect(new Rect());
    }

    @SuppressWarnings("deprecation")
    private void addProperties(JsonWriter j, View v) throws IOException {
        final Class<?> viewClass = v.getClass();
        boolean processable = true;
        for (final PageViewInfo pageViewInfo : mListPageViewInfo) {
            if (pageViewInfo.targetClass.isAssignableFrom(viewClass) && null != pageViewInfo.accessor) {
                Object value = pageViewInfo.accessor.applyMethod(v);

                if (processable) {
                    if (!TextUtils.isEmpty(pageViewInfo.name)) {
                        switch (pageViewInfo.name) {
                            case "clickable":
                                boolean isClickable = false;
                                Boolean rnClickable = AnalysysUtil.getRNViewClickable(v);
                                if (rnClickable != null) {
                                    value = isClickable = rnClickable;
                                } else {
                                    isClickable = (Boolean) value;
                                }
                                if (!isClickable || v instanceof AbsListView || v instanceof AbsoluteLayout) {
                                    processable = false;
                                }
                                if (!isClickable) {
                                    ViewParent vp = v.getParent();
                                    if (vp instanceof AdapterView) {
                                        value = processable = ((AdapterView) vp).getOnItemClickListener() != null;
                                    }
                                }
                                break;
                            case "alpha":
                                float alpha = (Float) value;
                                // 透明度是0则不可见
                                if (alpha == 0) {
                                    processable = false;
                                }
                                break;
                            case "hidden":
                                int visibility = (Integer) value;
                                // visibility是0则显示
                                if (visibility != 0) {
                                    processable = false;
                                    // 多做一次判断
                                } else if (isInvisible(v)) {
                                    processable = false;
                                }
                                break;
                        }
                    }
                }
                if (null == value) {
                    // Don't produce anything in this case
                } else if (value instanceof Number) {
                    j.name(pageViewInfo.name).value((Number) value);
                } else if (value instanceof Boolean) {
                    j.name(pageViewInfo.name).value((Boolean) value);
                } else if (value instanceof ColorStateList) {
                    j.name(pageViewInfo.name).value((Integer) ((ColorStateList) value).getDefaultColor());
                } else if (value instanceof Drawable) {
                    final Drawable drawable = (Drawable) value;
                    final Rect bounds = drawable.getBounds();
                    j.name(pageViewInfo.name);
                    j.beginObject();
                    j.name("classes");
                    j.beginArray();
                    Class<?> klass = drawable.getClass();
                    while (klass != Object.class) {
                        j.value(klass.getName());
                        klass = klass.getSuperclass();
                    }
                    j.endArray();
                    j.name("dimensions");
                    j.beginObject();
                    j.name("left").value(bounds.left);
                    j.name("right").value(bounds.right);
                    j.name("top").value(bounds.top);
                    j.name("bottom").value(bounds.bottom);
                    j.endObject();
                    if (drawable instanceof ColorDrawable) {
                        final ColorDrawable colorDrawable = (ColorDrawable) drawable;
                        j.name("color").value(colorDrawable.getColor());
                    }
                    j.endObject();
                } else {
                    j.name(pageViewInfo.name).value(value.toString());
                }
            } else if ("text".equals(pageViewInfo.name) && ImageView.class.isAssignableFrom(viewClass)) {
                CharSequence cd = v.getContentDescription();
                if (cd != null && cd.length() > 0) {
                    j.name(pageViewInfo.name).value(cd.toString());
                }
            }
        }
        j.name("processable").value(processable ? "1" : "0");
    }

    private static class ViewClassCache extends LruCache<Class<?>, String> {
        ViewClassCache(int maxSize) {
            super(maxSize);
        }

        @Override
        protected String create(Class<?> klass) {
            return klass.getName();
        }
    }

    private class RootViewCapture implements Callable<List<PageRootInfo>> {
        private final Map<String, PageViewBitmap> mBitmapMap;

        RootViewCapture() {
            mBitmapMap = new HashMap<>();
        }

        @Override
        public List<PageRootInfo> call() {
            List<PageRootInfo> rootViews = new ArrayList<>();

            Activity activity = ActivityLifecycleUtils.getCurrentActivity();
            if (activity != null) {
                String activityName = activity.getClass().getName();
                List<PageRootInfo> listView;
                if (WindowUIHelper.isTranslucentActivity(activity)) {
                    listView = WindowUIHelper.getTopOpaqueActivityViews();
                } else {
                    listView = WindowUIHelper.getCurrentWindowViews(activity, activityName);
                }
                if (listView == null || listView.isEmpty()) {
                    final View rootView = activity.getWindow().getDecorView().getRootView();
                    final PageRootInfo info = new PageRootInfo(activityName, rootView);
                    rootViews.add(info);
                } else {
                    rootViews.addAll(listView);
                }

                final int viewCount = rootViews.size();
                for (int i = 0; i < viewCount; i++) {
                    final PageRootInfo info = rootViews.get(i);
                    screenshot(info);
                }
            }
            return rootViews;
        }

        class WebViewLayerCache {
            View webView;
            int oriLayerType;
            Paint oriLayerPaint;

            WebViewLayerCache(View view) {
                webView = view;
                oriLayerType = webView.getLayerType();
                oriLayerPaint = (Paint) AnsReflectUtils.getField(webView, "mLayerPaint");
            }

            void checkAndDisableHardwareAccelerated() {
                if (oriLayerType != View.LAYER_TYPE_SOFTWARE) {
                    webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                }
            }

            void reset() {
                if (oriLayerType != webView.getLayerType()) {
                    webView.setLayerType(oriLayerType, oriLayerPaint);
                }
            }
        }

        private void screenshot(PageRootInfo info) {
            final View rootView = info.rootView;
            Bitmap viewBitmap = null;

            try {
                viewBitmap = (Bitmap) AnsReflectUtils.invokeMethod(rootView, "createSnapshot",
                        new Class[]{Bitmap.Config.class, Integer.TYPE, Boolean.TYPE},
                        new Object[]{Bitmap.Config.RGB_565, Color.WHITE, false});
            } catch (Throwable ignore) {
//                ExceptionUtil.exceptionThrow(ignore);
            }

            Boolean originalCacheState = null;
            List<WebViewLayerCache> listWebView = null;
            try {
                if (null == viewBitmap) {
                    // 含有WebView的界面有可能截图不能刷新，关闭硬件加速可以解决此问题
                    if (PageViewCapture.this.mContainsWebView) {
                        listWebView = new ArrayList<>();
                        findWebView((ViewGroup) rootView, listWebView);
                        for (WebViewLayerCache cache : listWebView) {
                            cache.checkAndDisableHardwareAccelerated();
                        }
                    }
                    originalCacheState = rootView.isDrawingCacheEnabled();
                    rootView.setDrawingCacheEnabled(true);
                    rootView.buildDrawingCache(true);
                    viewBitmap = rootView.getDrawingCache();
                }
            } catch (Throwable ignore) {
                ExceptionUtil.exceptionThrow(ignore);
            }

            PageViewBitmap pageViewBitmap = mBitmapMap.get(info.activityName);
            if (pageViewBitmap == null) {
                pageViewBitmap = new PageViewBitmap();
                mBitmapMap.put(info.activityName, pageViewBitmap);
            }

            float scale = 1.0f;
            if (null != viewBitmap) {
                final int rawDensity = viewBitmap.getDensity();

                if (rawDensity != Bitmap.DENSITY_NONE) {
                    scale = ((float) DisplayMetrics.DENSITY_DEFAULT) / rawDensity;
                }

                final int rawWidth = viewBitmap.getWidth();
                final int rawHeight = viewBitmap.getHeight();
                final int destWidth = (int) ((viewBitmap.getWidth() * scale) + 0.5);
                final int destHeight = (int) ((viewBitmap.getHeight() * scale) + 0.5);

                if (rawWidth > 0 && rawHeight > 0 && destWidth > 0 && destHeight > 0) {
                    pageViewBitmap.recreate(destWidth, destHeight, DisplayMetrics.DENSITY_DEFAULT, viewBitmap);
                }
            }

            if (listWebView != null) {
                for (WebViewLayerCache cache : listWebView) {
                    cache.reset();
                }
            }

            if (null != originalCacheState && !originalCacheState) {
                rootView.setDrawingCacheEnabled(false);
            }
            info.scale = scale;
            info.screenshot = pageViewBitmap;
        }

        private void findWebView(ViewGroup rootView, List<WebViewLayerCache> listWebView) {
            for (int i = 0; i < rootView.getChildCount(); i++) {
                View view = rootView.getChildAt(i);
                if (UniqueViewHelper.isWebView(view.getClass())) {
                    listWebView.add(new WebViewLayerCache(view));
                } else if (view instanceof ViewGroup) {
                    findWebView((ViewGroup) view, listWebView);
                }
            }
        }
    }
}
