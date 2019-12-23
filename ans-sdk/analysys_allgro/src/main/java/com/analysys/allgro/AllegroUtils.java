package com.analysys.allgro;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.analysys.ANSAutoPageTracker;
import com.analysys.utils.AnalysysUtil;
import com.analysys.utils.Constants;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Description:全埋点使用到的相关工具代码
 * Author: fengzeyuan
 * Date: 2019-11-06 14:05
 * Version: 1.0
 */
public class AllegroUtils {

    // -------- 控件元素相关 -----------

    public static String getViewIdResourceName(View view) {
        String idString = "";
        try {
            idString = (String) view.getTag(R.id.analysys_tag_view_id);
            if (TextUtils.isEmpty(idString)) {
                if (view.getId() != View.NO_ID) {
                    idString = getIdResourceName(view.getId());
                }
            }
        } catch (Exception ignored) {
        }
        return idString;
    }

    public static void setViewIdResourceName(View view, String id) {
        try{
            if (view != null && !TextUtils.isEmpty(id)) {
                view.setTag(R.id.analysys_tag_view_id, id);
            }

            String ids = (String) view.getTag((R.id.analysys_tag_view_id));
        }catch (Exception e){

        }
    }

    public static String getIdResourceName(int id) {
        String idString = "";
        try {
            if (id != View.NO_ID) {
                idString = AnalysysUtil.getContext().getResources().getResourceEntryName(id);
            }
        } catch (Exception ignored) {
        }
        return idString;
    }

    public static String[] getViewTypeAndText(View view) {
        String viewType = "";
        CharSequence viewText = "";
        if (view instanceof CheckBox) {
            // CheckBox
            viewType = "CheckBox";
            CheckBox checkBox = (CheckBox) view;
            viewText = checkBox.getText();
        } else if (view instanceof RadioButton) {
            // RadioButton
            viewType = "RadioButton";
            RadioButton radioButton = (RadioButton) view;
            viewText = radioButton.getText();
        } else if (view instanceof ToggleButton) {
            // ToggleButton
            viewType = "ToggleButton";
            viewText = getCompoundButtonText(view);
        } else if (view instanceof CompoundButton) {
            viewType = getViewTypeByReflect(view);
            viewText = getCompoundButtonText(view);
        } else if (view instanceof Button) {
            // Button
            viewType = "Button";
            Button button = (Button) view;
            viewText = button.getText();
        } else if (view instanceof CheckedTextView) {
            // CheckedTextView
            viewType = "CheckedTextView";
            CheckedTextView textView = (CheckedTextView) view;
            viewText = textView.getText();
        } else if (view instanceof TextView) {
            // TextView
            viewType = "TextView";
            TextView textView = (TextView) view;
            viewText = textView.getText();
        } else if (view instanceof ImageView) {
            // ImageView
            viewType = "ImageView";
            ImageView imageView = (ImageView) view;
            if (!TextUtils.isEmpty(imageView.getContentDescription())) {
                viewText = imageView.getContentDescription().toString();
            }
        } else if (view instanceof RatingBar) {
            viewType = "RatingBar";
            RatingBar ratingBar = (RatingBar) view;
            viewText = String.valueOf(ratingBar.getRating());
        } else if (view instanceof SeekBar) {
            viewType = "SeekBar";
            SeekBar seekBar = (SeekBar) view;
            viewText = String.valueOf(seekBar.getProgress());
        } else if (view instanceof ExpandableListView) {
            viewType = "ExpandableListView";
            viewText = "";
        } else if (view instanceof ListView) {
            viewType = "ListView";
            viewText = "";
        } else if (view instanceof GridView) {
            viewType = "GridView";
            viewText = "";
        } else if (view instanceof Spinner) {
            viewType = "Spinner";
            StringBuilder stringBuilder = new StringBuilder();
            viewText = traverseView(stringBuilder, (ViewGroup) view);
            if (!TextUtils.isEmpty(viewText)) {
                viewText = viewText.toString().substring(0, viewText.length() - 1);
            }
        } else if (view instanceof ViewGroup) {
            viewType = getViewGroupTypeByReflect(view);
            viewText = view.getContentDescription();
            if (TextUtils.isEmpty(viewText)) {
                try {
                    StringBuilder stringBuilder = new StringBuilder();
                    viewText = traverseView(stringBuilder, (ViewGroup) view);
                    if (!TextUtils.isEmpty(viewText)) {
                        viewText = viewText.toString().substring(0, viewText.length() - 1);
                    }
                } catch (Exception ignored) {
                }
            }
        }

        if (TextUtils.isEmpty(viewType)) {
            viewType = view.getClass().getName();
        }

        if (TextUtils.isEmpty(viewText)) {
            viewText = "";
            if (!TextUtils.isEmpty(view.getContentDescription())) {
                viewText = view.getContentDescription().toString();
            }
        }
        return new String[]{viewType, viewText.toString()};
    }

    /**
     * 获取 CompoundButton text
     *
     * @param view view
     * @return CompoundButton 显示的内容
     */
    private static String getCompoundButtonText(View view) {
        try {
            CompoundButton switchButton = (CompoundButton) view;
            Method method;
            if (switchButton.isChecked()) {
                method = view.getClass().getMethod("getTextOn");
            } else {
                method = view.getClass().getMethod("getTextOff");
            }
            return (String) method.invoke(view);
        } catch (Exception ex) {
            return "UNKNOWN";
        }
    }

    /**
     * 通过反射判断类的类型
     *
     * @param view 判断类型的 view
     * @return viewType
     */
    private static String getViewTypeByReflect(View view) {
        Class<?> compatClass;
        compatClass = getClassByName("android.widget.Switch");
        if (compatClass != null && compatClass.isInstance(view)) {
            return "Switch";
        }
        compatClass = getClassByName("android.support.v7.widget.SwitchCompat");
        if (compatClass != null && compatClass.isInstance(view)) {
            return "SwitchCompat";
        }
        compatClass = getClassByName("android.support.design.widget.NavigationView");
        if (compatClass != null && compatClass.isInstance(view)) {
            return "SwitchCompat";
        }
        return view.getClass().getName();
    }

    private static String traverseView(StringBuilder stringBuilder, ViewGroup root) {
        try {
            if (stringBuilder == null) {
                stringBuilder = new StringBuilder();
            }

            if (root == null) {
                return stringBuilder.toString();
            }

            final int childCount = root.getChildCount();
            for (int i = 0; i < childCount; ++i) {
                final View child = root.getChildAt(i);

                if (child.getVisibility() != View.VISIBLE) {
                    continue;
                }

                if (child instanceof ViewGroup) {
                    traverseView(stringBuilder, (ViewGroup) child);
                } else {
                    String viewText = getViewText(child);
                    if (!TextUtils.isEmpty(viewText)) {
                        stringBuilder.append(viewText);
                        stringBuilder.append("-");
                    }
                }
            }
            return stringBuilder.toString();
        } catch (Exception e) {
            return stringBuilder != null ? stringBuilder.toString() : "";
        }
    }

    private static String getViewText(View child) {
        if (child instanceof EditText) {
            return "";
        }
        try {
            Class<?> switchCompatClass = null;
            try {
                switchCompatClass = Class.forName("android.support.v7.widget.SwitchCompat");
            } catch (Exception e) {
                //ignored
            }

            if (switchCompatClass == null) {
                try {
                    switchCompatClass = Class.forName("androidx.appcompat.widget.SwitchCompat");
                } catch (Exception e) {
                    //ignored
                }
            }

            CharSequence viewText = null;

            if (child instanceof CheckBox) {
                CheckBox checkBox = (CheckBox) child;
                viewText = checkBox.getText();
            } else if (switchCompatClass != null && switchCompatClass.isInstance(child)) {
                CompoundButton switchCompat = (CompoundButton) child;
                Method method;
                if (switchCompat.isChecked()) {
                    method = child.getClass().getMethod("getTextOn");
                } else {
                    method = child.getClass().getMethod("getTextOff");
                }
                viewText = (String) method.invoke(child);
            } else if (child instanceof RadioButton) {
                RadioButton radioButton = (RadioButton) child;
                viewText = radioButton.getText();
            } else if (child instanceof ToggleButton) {
                ToggleButton toggleButton = (ToggleButton) child;
                boolean isChecked = toggleButton.isChecked();
                if (isChecked) {
                    viewText = toggleButton.getTextOn();
                } else {
                    viewText = toggleButton.getTextOff();
                }
            } else if (child instanceof Button) {
                Button button = (Button) child;
                viewText = button.getText();
            } else if (child instanceof CheckedTextView) {
                CheckedTextView textView = (CheckedTextView) child;
                viewText = textView.getText();
            } else if (child instanceof TextView) {
                TextView textView = (TextView) child;
                viewText = textView.getText();
            } else if (child instanceof ImageView) {
                ImageView imageView = (ImageView) child;
                if (!TextUtils.isEmpty(imageView.getContentDescription())) {
                    viewText = imageView.getContentDescription().toString();
                }
            } else {
                viewText = child.getContentDescription();
            }
            if (TextUtils.isEmpty(viewText) && child instanceof TextView) {
                viewText = ((TextView) child).getHint();
            }
            if (!TextUtils.isEmpty(viewText)) {
                return viewText.toString();
            }
        } catch (Exception ignored) {
        }
        return "";
    }

    /**
     * 通过反射判断类的类型
     *
     * @param view 判断类型的 viewGroup
     * @return viewType
     */
    private static String getViewGroupTypeByReflect(View view) {
        Class<?> compatClass;
        compatClass = getClassByName("android.support.v7.widget.CardView");
        if (compatClass != null && compatClass.isInstance(view)) {
            return "CardView";
        }
        compatClass = getClassByName("androidx.cardview.widget.CardView");
        if (compatClass != null && compatClass.isInstance(view)) {
            return "CardView";
        }
        compatClass = getClassByName("android.support.design.widget.NavigationView");
        if (compatClass != null && compatClass.isInstance(view)) {
            return "NavigationView";
        }
        compatClass = getClassByName("com.google.android.material.navigation.NavigationView");
        if (compatClass != null && compatClass.isInstance(view)) {
            return "NavigationView";
        }
        return view.getClass().getName();
    }

    // --------- 页面相关 -------------------

    public static boolean isPage(Object checkObj) {
        return checkObj != null
                && (checkObj instanceof Activity || checkObj instanceof Dialog || isFragment(checkObj));
    }

    /**
     * 判断当前示例是不是Fragment
     */
    public static boolean isFragment(Object object) {
        try {
            Class<?> supportFragmentClass = null;
            Class<?> androidXFragmentClass = null;
            Class<?> fragment = null;
            try {
                fragment = Class.forName("android.app.Fragment");
            } catch (Exception e) {
                //ignored
            }
            try {
                supportFragmentClass = Class.forName("android.support.v4.app.Fragment");
            } catch (Exception e) {
                //ignored
            }

            try {
                androidXFragmentClass = Class.forName("androidx.fragment.app.Fragment");
            } catch (Exception e) {
                //ignored
            }

            if (supportFragmentClass == null && androidXFragmentClass == null && fragment == null) {
                return false;
            }

            if ((supportFragmentClass != null && supportFragmentClass.isInstance(object)) ||
                    (androidXFragmentClass != null && androidXFragmentClass.isInstance(object)) ||
                    (fragment != null && fragment.isInstance(object))) {
                return true;
            }
        } catch (Exception e) {
            //ignored
        }
        return false;
    }

    /**
     * 获取示页面相关信息
     *
     * @return String[0]=url  String[1]=title
     */
    public static Map<String, Object> getPageInfo(Object pageObj) {
        Map<String, Object> pageInfo = new HashMap<>();
        try {
            if (pageObj instanceof ANSAutoPageTracker) {
                ANSAutoPageTracker autoPageTracker = (ANSAutoPageTracker) pageObj;
                Map<String, Object> map = autoPageTracker.registerPageProperties();
                String url = autoPageTracker.registerPageUrl();
                if (!TextUtils.isEmpty(url)) {
                    map.put(Constants.PAGE_URL, url);
                }
                pageInfo.putAll(map);
            }

            if (pageObj instanceof Activity) {
                if (!pageInfo.containsKey(Constants.PAGE_TITLE)) {
                    String title = getActivityTitle((Activity) pageObj);
                    if (TextUtils.isEmpty(title)) {
                        pageInfo.put(Constants.PAGE_TITLE, title);
                    }
                }
                if (!pageInfo.containsKey(Constants.PAGE_URL)) {
                    pageInfo.put(Constants.PAGE_URL, pageObj.getClass().getCanonicalName());
                }
            } else if (pageObj instanceof Dialog) {
                Activity parentAc = getActivityFromDialog((Dialog) pageObj);
                if (!pageInfo.containsKey(Constants.PAGE_TITLE)) {
                    String title = pageObj.getClass().getName();
                    if (parentAc != null) {
                        String inText = getActivityTitle(parentAc);
                        if (!TextUtils.isEmpty(inText)) {
                            title += " in " + inText;
                        }
                    }
                    pageInfo.put(Constants.PAGE_TITLE, title);
                }
                if (!pageInfo.containsKey(Constants.PAGE_URL)) {
                    pageInfo.put(Constants.PAGE_URL, pageObj.getClass().getName());
                }
                if (parentAc != null) {
                    pageInfo.put(Constants.PARENT_URL, getPageUrl(parentAc));
                }
            } else if (isFragment(pageObj)) {
                Activity parentAc = getActivityFromFragment(pageObj);
                if (!pageInfo.containsKey(Constants.PAGE_TITLE)) {
                    String title = pageObj.getClass().getName();
                    if (parentAc != null) {
                        String inText = getActivityTitle(parentAc);
                        if (!TextUtils.isEmpty(inText)) {
                            title += " in " + inText;
                        }
                    }
                    pageInfo.put(Constants.PAGE_TITLE, title);
                }
                if (!pageInfo.containsKey(Constants.PAGE_URL)) {
                    pageInfo.put(Constants.PAGE_URL, pageObj.getClass().getName());
                }
                if (parentAc != null) {
                    pageInfo.put(Constants.PARENT_URL, getPageUrl(parentAc));
                }
            }

            // 页面宽高
            int[] pageHeightAndWidth = getPageHeightAndWidth(pageObj);
            pageInfo.put(Constants.PAGE_HEIGHT, pageHeightAndWidth[0]);
            pageInfo.put(Constants.PAGE_WIDTH, pageHeightAndWidth[1]);

        } catch (Exception e) {
        }
        return pageInfo;
    }

    public static Activity getCurAc() {
        return AnalysysUtil.getCurActivity();
    }

    /**
     * 通过View获取Activity
     */
    public static Object getPageObjFromView(View v) {
        try {
            if (v != null) {
                // 尝试获取Dialog
                Dialog dialog = getDialogFromView(v);
                if (dialog != null) {
                    return dialog;
                }
                // 尝试获取Fragment
                try {
                    String pageName = getFragmentPageName(v);
                    if (!TextUtils.isEmpty(pageName)) {
                        return Class.forName(pageName).newInstance();
                    }
                } catch (Exception e) {
                }
                // 尝试获取activity
                Activity activity = getActivityFromView(v);
                if (activity != null) {
                    return activity;
                }
            }
        } catch (Exception e) {
        }
        // 最后的尝试
        return AnalysysUtil.getCurActivity();
    }


    /**
     * 获取当前Fragment的父Fragment
     */
    public static Object getParentFragment(Object fragmentObj) {
        try {
            Method getParentFragmentMethod = fragmentObj.getClass().getMethod("getParentFragment");
            return getParentFragmentMethod.invoke(fragmentObj);
        } catch (Exception e) {
            //ignored
        }
        return null;
    }


    /**
     * 获取 Activity 的 title
     *
     * @param activity Activity
     * @return Activity 的 title
     */
    private static String getActivityTitle(Activity activity) {
        if (activity != null) {
            try {
                String activityTitle = null;

                if (Build.VERSION.SDK_INT >= 11) {
                    String toolbarTitle = getToolbarTitle(activity);
                    if (!TextUtils.isEmpty(toolbarTitle)) {
                        activityTitle = toolbarTitle;
                    }
                }

                if (!TextUtils.isEmpty(activityTitle)) {
                    activityTitle = activity.getTitle().toString();
                }

                if (TextUtils.isEmpty(activityTitle)) {
                    PackageManager packageManager = activity.getPackageManager();
                    if (packageManager != null) {
                        ActivityInfo activityInfo = packageManager.getActivityInfo(activity.getComponentName(), 0);
                        if (!TextUtils.isEmpty(activityInfo.loadLabel(packageManager))) {
                            activityTitle = activityInfo.loadLabel(packageManager).toString();
                        }
                    }
                }

                return activityTitle;
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    @TargetApi(11)
    private static String getToolbarTitle(Activity activity) {
        try {
            if ("com.tencent.connect.common.AssistActivity".equals(activity.getClass().getCanonicalName())) {
                if (!TextUtils.isEmpty(activity.getTitle())) {
                    return activity.getTitle().toString();
                }
                return null;
            }
            ActionBar actionBar = activity.getActionBar();
            if (actionBar != null) {
                if (!TextUtils.isEmpty(actionBar.getTitle())) {
                    return actionBar.getTitle().toString();
                }
            } else {
                try {
                    Class<?> appCompatActivityClass = compatActivity();
                    if (appCompatActivityClass != null && appCompatActivityClass.isInstance(activity)) {
                        Method method = activity.getClass().getMethod("getSupportActionBar");
                        Object supportActionBar = method.invoke(activity);
                        if (supportActionBar != null) {
                            method = supportActionBar.getClass().getMethod("getTitle");
                            CharSequence charSequence = (CharSequence) method.invoke(supportActionBar);
                            if (charSequence != null) {
                                return charSequence.toString();
                            }
                        }
                    }
                } catch (Exception e) {
                    //ignored
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static Class<?> compatActivity() {
        Class<?> appCompatActivityClass = null;
        try {
            appCompatActivityClass = Class.forName("android.support.v7.app.AppCompatActivity");
        } catch (Exception e) {
            //ignored
        }
        if (appCompatActivityClass == null) {
            try {
                appCompatActivityClass = Class.forName("androidx.appcompat.app.AppCompatActivity");
            } catch (Exception e) {
                //ignored
            }
        }
        return appCompatActivityClass;
    }

    /**
     * 获取示例的url 和 title
     *
     * @param pageObj
     * @return String[0]=Height  String[1]=Width
     */
    private static int[] getPageHeightAndWidth(Object pageObj) {
        int[] pageInfo = new int[2];
        if (pageObj instanceof Activity) {
            Activity ac = (Activity) pageObj;
            View decorView = ac.getWindow().getDecorView();
            pageInfo[0] = decorView.getHeight();
            pageInfo[1] = decorView.getHeight();
        } else if (isFragment(pageObj)) {
            try {
                Class pageClass = pageObj.getClass();
                Method getViewMethod = pageClass.getMethod("getView");
                if (!getViewMethod.isAccessible()) {
                    getViewMethod.setAccessible(true);
                }
                View view = (View) getViewMethod.invoke(pageObj);
                if (view != null) {
                    pageInfo[0] = view.getHeight();
                    pageInfo[1] = view.getWidth();
                }
            } catch (Exception e) {
            }
        }
        return pageInfo;
    }


    /**
     * 获取页面Url
     *
     * @param pageObj 页面对象
     * @return Url
     */
    public static String getPageUrl(Object pageObj) {
        String url = "";
        if (pageObj instanceof ANSAutoPageTracker) {
            ANSAutoPageTracker autoPageTracker = (ANSAutoPageTracker) pageObj;
            url = autoPageTracker.registerPageUrl();
            if (TextUtils.isEmpty(url)) {
                Map<String, Object> map = autoPageTracker.registerPageProperties();
                url = (String) map.get(Constants.PAGE_URL);
            }
            if (TextUtils.isEmpty(url)) {
                return url;
            }
        }

        if (pageObj instanceof Activity) {
            url = pageObj.getClass().getCanonicalName();
        } else if (pageObj != null) {
            url = pageObj.getClass().getName();
        }
        return url;
    }

    /**
     * 根据 Fragment 获取对应的 Activity
     *
     * @param fragment，Fragment
     * @return Activity or null
     */
    public static Activity getActivityFromFragment(Object fragment) {
        try {
            Method getActivityMethod = fragment.getClass().getMethod("getActivity");
            return (Activity) getActivityMethod.invoke(fragment);
        } catch (Exception e) {
            //ignored
        }
        return getCurAc();
    }

    public static Activity getActivityFromDialog(Dialog dialog) {
        Activity ac = AllegroUtils.getActivityFromContext(dialog.getContext());
        if (ac == null) {
            ac = dialog.getOwnerActivity();
        }

        if (ac == null) {
            ac = getCurAc();
        }
        return ac;
    }

    /**
     * 通过View获取Activity
     */
    public static Activity getActivityFromView(View view) {
        if (view == null) {
            return getCurAc();
        }
        return getActivityFromContext(view.getContext());
    }

    /**
     * 通过上下问获取activity
     */
    private static Activity getActivityFromContext(Context context) {
        if (context != null) {
            if (context instanceof Activity) {
                return (Activity) context;
            } else if (context instanceof ContextWrapper) {
                while (!(context instanceof Activity) && context instanceof ContextWrapper) {
                    context = ((ContextWrapper) context).getBaseContext();
                }
                if (context instanceof Activity) {
                    return (Activity) context;
                }
            }
        }
        return getCurAc();
    }


    /**
     * 通过View获取Dialog
     */
    private static Dialog getDialogFromView(View v) {
        Object result = callMethod(v, "getWindow");
        if (result instanceof Window) {
            Window window = (Window) result;
            Window.Callback callback = window.getCallback();
            if (callback instanceof Dialog) {
                return (Dialog) callback;
            }
        }
        return null;
    }

    private static String getFragmentPageName(View v) {
        Object tag = v.getTag(R.id.analysys_tag_fragment_name);
        if (tag instanceof String) {
            return tag.toString();
        }
        ViewParent parent = v.getParent();
        while (parent instanceof View) {
            View curView = (View) parent;
            Object fragmentName = curView.getTag(R.id.analysys_tag_fragment_name);
            if (fragmentName instanceof String) {
                String pageName = fragmentName.toString();
                v.setTag(R.id.analysys_tag_fragment_name, pageName);
                return pageName;
            }
            parent = curView.getParent();
        }
        return "";
    }


    // --------- 反射 -------------------

    private static Class<?> getClassByName(String name) {
        Class<?> compatClass;
        try {
            compatClass = Class.forName(name);
        } catch (ClassNotFoundException e) {
            return null;
        }
        return compatClass;
    }

    /**
     * 获取对象字段值
     */
    public static Object getFieldValue(Object obj, String fieldName) {
        try {
            Field field = obj.getClass().getField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * 调用对象方法
     */
    public static Object callMethod(Object obj, String methodName, Object... params) {
        try {
            Class<?>[] parameterTypes = null;
            if (params != null) {
                parameterTypes = new Class[params.length];
                for (int i = 0; i < params.length; i++) {
                    parameterTypes[i] = params[i].getClass();
                }
            }
            Method method = obj.getClass().getMethod(methodName, parameterTypes);
            method.setAccessible(true);
            return method.invoke(obj, params);
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * 上报捕获的异常
     * @param throwable 
     */
    public static void reportCatchException(Exception throwable) {
        if (BuildConfig.ENABLE_BUGLY) {
            try {
                Class clazz = Class.forName("com.tencent.bugly.crashreport.CrashReport");
                Method postCatchedException = clazz.getMethod("postCatchedException", Throwable.class);
                postCatchedException.invoke(null, throwable);
            } catch (Throwable e) {

            }
        }
    }
}

