package com.analysys.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Base64;
import android.util.Base64OutputStream;
import android.view.View;
import android.view.WindowManager;

import com.analysys.utils.AnalysysUtil;
import com.analysys.utils.AnsReflectUtils;
import com.analysys.utils.ExceptionUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Copyright © 2019 Analysys Inc. All rights reserved.
 * @Description: root view帮助类，反射获取WindowManagerGlobal下的mViews，解析页面名字
 * * 1、activity：""：所有activity，"a.b.c"：activity类名
 * * 2、dialog："(dialog)":独立对话框，"a.b.c(dialog)":某个activity下的dialog
 * * 3、float window："(float)":独立悬浮窗，"a.b.c(float)":某个activity下的悬浮窗
 * * 4、popup window："(popup)":独立popup window，"a.b.c(popup)":某个activity下的popup window
 * @Create: 2019-11-28 11:46
 * @author: hcq
 */
public class WindowUIHelper {

    private static Object sWMGlobal;
    private static List sGlobalViews;

    public static Object getWindowManagerGlobal() {
        if (sWMGlobal == null) {
            WindowManager wm = (WindowManager) AnalysysUtil.getContext().getSystemService(Context.WINDOW_SERVICE);
            if (wm != null) {
                sWMGlobal = AnsReflectUtils.getField(wm, "mGlobal");
            }
        }
        return sWMGlobal;
    }

    public static List getGlobalViews(boolean forceList) {
        if (sGlobalViews == null) {
            Object global = getWindowManagerGlobal();
            if (global != null) {
                Object obj = AnsReflectUtils.getField(global, "mViews");
                if (obj instanceof List) {
                    sGlobalViews = (List) obj;
                } else if (obj instanceof View[]) {
                    if (forceList) {
                        View[] arrViews = (View[]) obj;
                        return new ArrayList<>(Arrays.asList(arrViews));
                    }
                }
            }
        }
        return sGlobalViews;
    }

    public static boolean setGlobalViews(Object global, List views) {
        if (AnsReflectUtils.setField(global, "mViews", views)) {
            sGlobalViews = views;
            return true;
        }
        return false;
    }

    private static boolean isActivityContext(Context context) {
        if (context == null) {
            return false;
        }
        if (context instanceof Activity) {
            return true;
        }
        return context.getClass().getName().endsWith("DecorContext");
    }

    /**
     * 获取view所在的根节点信息，判断页面类型
     */
    public static RootView getRootView(View view) {
        if (view == null) {
            return null;
        }
        view = view.getRootView();
        if (view == null) {
            return null;
        }
        Context viewContext = view.getContext();
        View contentView = view.findViewById(android.R.id.content);
        if (contentView != null) {
            Context contentViewContext = contentView.getContext();
            Activity activity = getActivityBase(contentViewContext);
            if (activity != null) {
                if (isActivityContext(viewContext)) {
                    return new RootView(view, getActivityName(activity));
                } else {
                    return new RootView(view, getDialogName(activity));
                }
            }
        } else {
            // 检查popup window和悬浮窗
            if (view.getClass().getName().contains("Popup")) {
                return new RootView(view, getPopupWindowName(viewContext));
            } else {
                // TODO 悬浮窗暂时不支持
                return null;
//                // 排除Toast
//                boolean isToast = false;
//                ViewGroup.LayoutParams lp = view.getLayoutParams();
//                if (lp instanceof WindowManager.LayoutParams) {
//                    CharSequence title = ((WindowManager.LayoutParams) lp).getTitle();
//                    if (title != null) {
//                        if (title.toString().toLowerCase().equals("toast")) {
//                            isToast = true;
//                        }
//                    }
//                }
//                if (!isToast) {
//                    return new RootView(view, getFloatWindowName(viewContext));
//                }
            }
        }
        return null;
    }

    /**
     * 获取当前进程所有window的根节点信息
     */
    public static List<RootView> getAllWindowViews() {
        List views = getGlobalViews(true);
        if (views == null) {
            return null;
        }
        List<RootView> listView = new ArrayList<>();
        for (int i = 0; i < views.size(); i++) {
            View view = (View) views.get(i);
            RootView rootView = getRootView(view);
            if (rootView != null) {
                listView.add(rootView);
            }
        }
        return listView;
    }

    public static String getActivityName(Object activity) {
        return activity == null ? "" : activity.getClass().getName();
    }

    public static final String DIALOG_SUFFIX = "(dialog)";
    public static final String FLOATWIN_SUFFIX = "(float)";
    public static final String POPWIN_SUFFIX = "(popup)";

    public static String getDialogName(Object activity) {
        String activityName = "";
        if (activity instanceof String) {
            activityName = (String) activity;
        } else if (activity instanceof Activity) {
            activityName = getActivityName(activity);
        }
        return activityName + DIALOG_SUFFIX;
    }

    public static String getPopupWindowName(Object activity) {
        String activityName = "";
        if (activity instanceof String) {
            activityName = (String) activity;
        } else if (activity instanceof Context) {
            Activity act = getActivityBase((Context) activity);
            if (act != null) {
                activityName = getActivityName(act);
            }
        }
        return activityName + POPWIN_SUFFIX;
    }

    public static String getFloatWindowName(Object activity) {
        String activityName = "";
        if (activity instanceof String) {
            activityName = (String) activity;
        } else if (activity instanceof Context) {
            Activity act = getActivityBase((Context) activity);
            if (act != null) {
                activityName = getActivityName(act);
            }
        }
        return activityName + FLOATWIN_SUFFIX;
    }

    /**
     * 获取view所在的页面名字
     */
    public static String getPageName(View view) {
        RootView rView = getRootView(view);
        if (rView == null || rView.pageName == null) {
            return "";
        }
        return rView.pageName;
    }

    /**
     * 判断根节点是否在Window Manager中
     */
    public static boolean isRootViewAlive(View rootView) {
        List list = getGlobalViews(true);
        return list != null && list.contains(rootView);
    }

    /**
     * 判断是否为透明activity 如dialog样式的activity
     */
    public static boolean isTranslucentActivity(Activity activity) {
        try {
            return activity.getWindow().getWindowStyle().getBoolean(
                    (Integer) AnsReflectUtils.getStaticField(Class.forName("com.android.internal.R$styleable"),
                            "Window_windowIsTranslucent"), false);
        } catch (Throwable ignore) {
            ExceptionUtil.exceptionPrint(ignore);
        }
        return false;
    }

    /**
     * 获取透明activity和底下非透明activity
     */
    public static List<PageRootInfo> getTopOpaqueActivityViews() {
        List views = getGlobalViews(true);
        if (views == null || views.size() < 2) { // TODO 只有一个透明activity的情况？
            return null;
        }

        List<PageRootInfo> listView = new ArrayList<>();
        for (int i = views.size() - 1; i >= 0; i--) {
            View view = (View) views.get(i);
            // 检查activity
            Context viewContext = view.getContext();
            View contentView = view.findViewById(android.R.id.content);
            if (contentView != null) {
                if (isActivityContext(viewContext)) {
                    Context contentViewContext = contentView.getContext();
                    Activity activity = getActivityBase(contentViewContext);
                    if (activity != null) {
                        List<PageRootInfo> list = getCurrentWindowViews(activity, activity.getClass().getName());
                        if (list != null && list.size() > 0) {
                            listView.addAll(0, list);
                        }
                        if (i != views.size() - 1 && !isTranslucentActivity(activity)) {
                            break;
                        }
                    }
                }
            }
        }
        return listView;
    }


    public static List<PageRootInfo> getCurrentWindowViews(Activity activity, String activityName) {
        List views = getGlobalViews(true);
        if (views == null) {
            return null;
        }

        List<PageRootInfo> listView = new ArrayList<>();
        boolean findActivity = false;
        for (int i = 0; i < views.size(); i++) {
            View view = (View) views.get(i);

            // 检查activity
            View contentView = view.findViewById(android.R.id.content);
            Context viewContext = view.getContext();
            if (contentView != null) {
                Context contentViewContext = contentView.getContext();
                Activity act = getActivityBase(contentViewContext);
                if (act == activity) {
                    if (isActivityContext(viewContext)) {
                        findActivity = true;
                        listView.add(0, new PageRootInfo(activityName, view));
                    } else {
                        listView.add(new PageRootInfo(getDialogName(activity), view));
                    }
                } else if (act != null && findActivity) {
                    break;
                }
            }
            // 检查popup window
            else if (view.getClass().getName().contains("Popup")) {
                if (viewContext == activity) {
                    listView.add(new PageRootInfo(getPopupWindowName(activity), view));
                }
            }
            // TODO 检测悬浮窗
        }
        return listView;
    }

    private static Activity getActivityBase(Context context) {
        int limit = 0;
        while (true) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            if (context == null || !UniqueViewHelper.isContextThemeWrapper(context.getClass().getName())) {
                return null;
            }
            //防止极端情况
            if (limit++ > 10) {
                return null;
            }
            context = (Context) AnsReflectUtils.invokeMethod(context, "getBaseContext");
        }
    }

    public static class PageRootInfo {
        public String activityName;
        public final View rootView;
        public PageViewBitmap screenshot;
        public float scale;

        public PageRootInfo(String activityName, View rootView) {
            this.activityName = activityName;
            this.rootView = rootView;
            this.screenshot = null;
            this.scale = 1.0f;
        }

        public String getSign() {
            if (screenshot == null || screenshot.mCached == null) {
                return null;
            }
            Bitmap bitmap = screenshot.mCached;
            int w = bitmap.getWidth();
            int h = bitmap.getHeight();
            int[] pixels = new int[w * h];
            bitmap.getPixels(pixels, 0, w, 0, 0, w, h);
            long sign = 0;
            for (int v : pixels) {
                sign += v;
            }
            return String.valueOf(sign);
        }
    }

    public static class PageViewBitmap {
        private final Paint mPaint;
        private Bitmap mCached;

        public PageViewBitmap() {
            mPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
            mCached = null;
        }

        public synchronized void recreate(int width, int height, int destDensity, Bitmap source) {
            if (null == mCached || mCached.getWidth() != width || mCached.getHeight() != height) {
                try {
                    mCached = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);
                } catch (final OutOfMemoryError e) {
                    mCached = null;
                }

                if (null != mCached) {
                    mCached.setDensity(destDensity);
                }
            } else {
                mCached.eraseColor(0x00000000);
            }

            if (null != mCached) {
                final Canvas scaledCanvas = new Canvas(mCached);
                scaledCanvas.drawBitmap(source, 0, 0, mPaint);
            }
        }

        // Writes a QUOTED base64 string (or the string null) to the output stream
        public synchronized void writeBitmapJSON(Bitmap.CompressFormat format, int quality, OutputStream out)
                throws IOException {
            if (null == mCached || mCached.getWidth() == 0 || mCached.getHeight() == 0) {
                out.write("null".getBytes());
            } else {
                out.write('"');
                final Base64OutputStream imageOut = new Base64OutputStream(out, Base64.NO_WRAP);
                mCached.compress(Bitmap.CompressFormat.PNG, quality, imageOut);
                imageOut.flush();
                out.write('"');
            }
        }
    }
}
