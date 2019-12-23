package com.analysys.visual.utils;

import android.app.Activity;
import android.content.Context;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.analysys.visual.viewcrawler.ViewSnapshot;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * @Copyright © 2018 Eguan Inc. All rights reserved.
 * @Description: 界面工具类。取字的工具类
 * @Version: 1.0
 * @Create: 2018年7月23日 下午7:33:28
 * @Author: sanbo
 */
public class UIHelper {
    private static final int MAX_PROPERTY_LENGTH = 128;

    public static final String DIALOG_SUFFIX = "(dialog)";

    /**
     * Recursively scans a view and it's children, looking for user-visible text to
     * provide as an event property.
     */
    public static String textPropertyFromView(View v) {
        String ret = null;

        if (v instanceof TextView) {
            final TextView textV = (TextView) v;
            final CharSequence retSequence = textV.getText();
            if (null != retSequence) {
                ret = retSequence.toString();
            }
        } else if (v instanceof ViewGroup) {
            final StringBuilder builder = new StringBuilder();
            final ViewGroup vGroup = (ViewGroup) v;
            final int childCount = vGroup.getChildCount();
            boolean textSeen = false;
            for (int i = 0; i < childCount && builder.length() < MAX_PROPERTY_LENGTH; i++) {
                final View child = vGroup.getChildAt(i);
                final String childText = textPropertyFromView(child);
                if (null != childText && childText.length() > 0) {
                    if (textSeen) {
                        builder.append(", ");
                    }
                    builder.append(childText);
                    textSeen = true;
                }
            }

            if (builder.length() > MAX_PROPERTY_LENGTH) {
                ret = builder.substring(0, MAX_PROPERTY_LENGTH);
            } else if (textSeen) {
                ret = builder.toString();
            }
        }

        return ret;
    }

    private static String getDialogName(Activity activity) {
        return activity == null ? "" : activity.getClass().getCanonicalName() + DIALOG_SUFFIX;
    }

    public static List<ViewSnapshot.RootViewInfo> getCurrentWindowViews(Activity activity, String activityName) {
        WindowManager wm = activity.getWindowManager();
        Object global = getReflectField(wm, "mGlobal");
        if (global == null) {
            return null;
        }
        Object objViews = getReflectField(global, "mViews");
        List views;
        if (objViews instanceof List) {
            views = (List)objViews;
        } else if (objViews instanceof View[]) {
            View[] arrViews=(View[]) objViews;
            views = new ArrayList(Arrays.asList(arrViews));
        } else {
            return null;
        }
        List<ViewSnapshot.RootViewInfo> listView = new ArrayList<>();
        for (int i = 0; i < views.size(); i++) {
            View view = (View) views.get(i);

//                // 检查popup window和悬浮窗
//                if (view.getContext() == activity) {
//                    if (view.getClass().getName().equals("android.widget.PopupWindow$PopupDecorView")) {
//                        listView.add(new RootViewInfo(activityName + "[popup_win]", view));
//                    } else {
//                        listView.add(new RootViewInfo(activityName + "[float_win]", view));
//                    }
//                    continue;
//                }

            // 检查activity
            View contentView = view.findViewById(android.R.id.content);
            if (contentView == null) {
                continue;
            }
            Context context = contentView.getContext();
            if (context instanceof Activity) {
                if (context == activity) {
                    listView.add(new ViewSnapshot.RootViewInfo(activityName, view));
                }
                continue;
            }

            if (isDialogBelongActivity(context, activity)) {
                listView.add(new ViewSnapshot.RootViewInfo(getDialogName(activity), view));
            }
        }
        return listView;
    }

    public static List<ViewSnapshot.RootViewInfo> getActivityDialogs(Activity activity) {
        if (activity == null) {
            return null;
        }
        WindowManager wm = activity.getWindowManager();
        Object global = getReflectField(wm, "mGlobal");
        if (global == null) {
            return null;
        }
        Object objViews = getReflectField(global, "mViews");
        List views;
        if (objViews instanceof List) {
            views = (List)objViews;
        } else if (objViews instanceof View[]) {
            View[] arrViews=(View[]) objViews;
            views = new ArrayList(Arrays.asList(arrViews));
        } else {
            return null;
        }
        List<ViewSnapshot.RootViewInfo> listView = null;
        for (int i = 0; i < views.size(); i++) {
            View view = (View) views.get(i);

            // 检查activity
            View contentView = view.findViewById(android.R.id.content);
            if (contentView == null) {
                continue;
            }
            Context context = contentView.getContext();
            if (context instanceof Activity) {
                continue;
            }

            if (isDialogBelongActivity(context, activity)) {
                if (listView == null) {
                    listView = new ArrayList<>();
                }
                listView.add(new ViewSnapshot.RootViewInfo(getDialogName(activity), view));
            }
        }
        return listView;
    }

    private static boolean isDialogBelongActivity(Context dialogContext, Context activityContext) {
        int limit = 0;
        while (true) {
            if (dialogContext == activityContext) {
                return true;
            }
            if (!(dialogContext instanceof ContextThemeWrapper)) {
                return false;
            }

            //防止极端情况
            if (limit++ > 10) {
                return false;
            }

            dialogContext = ((ContextThemeWrapper) dialogContext).getBaseContext();
        }

    }

    private static Object getReflectField(Object obj, String fieldName) {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (Throwable ignore) {
        }
        return null;
    }
}
