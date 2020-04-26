package com.analysys.process;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.analysys.utils.ANSThreadPool;
import com.analysys.utils.CommonUtils;
import com.analysys.utils.Constants;
import com.analysys.utils.ExceptionUtil;
import com.analysys.utils.ReflectUtils;

import org.json.JSONArray;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Copyright © 2019 EGuan Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2019/3/27 10:40
 * @Author: Wang-X-C
 */
public class HeatMap {

    public ConcurrentHashMap<String, Object> pageInfo = null;
    private ConcurrentHashMap<String, Object> clickInfo = null;
    private float rx = 0, ry = 0, x = 0, y = 0;

    public static HeatMap getInstance() {
        return HeatMap.Holder.INSTANCE;
    }

    private HeatMap() {
//        SystemIds.getInstance().parserId();
    }

    /**
     * 初始化页面宽高分辨率等信息
     */
    public void initPageInfo(Activity activity) {
        pageInfo = new ConcurrentHashMap<>();
        if (activity != null) {
            pageInfo.put(Constants.PAGE_URL, activity.getClass().getName());
            DisplayMetrics metrics = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
            pageInfo.put(Constants.PAGE_WIDTH, metrics.widthPixels);
            pageInfo.put(Constants.PAGE_HEIGHT, metrics.heightPixels);
            pageInfo.put(Constants.TOUCH_SCREEN_DPI, metrics.densityDpi);
            float scale = 1.0f;
            View rootView = activity.getWindow().getDecorView().getRootView();
            rootView.setDrawingCacheEnabled(true);
            rootView.buildDrawingCache(true);
            Bitmap rawBitmap = rootView.getDrawingCache();
            if (rawBitmap != null && !rawBitmap.isRecycled()) {
                final int rawDensity = rawBitmap.getDensity();
                scale = ((float) DisplayMetrics.DENSITY_DEFAULT) / rawDensity;
            }
            pageInfo.put(Constants.TOUCH_SCREEN_SCALE, scale);
        }
    }

    /***
     * 递归调用解析view
     * @param decorView 根节点view
     */
    public void hookDecorViewClick(View decorView) throws Exception {
        if (decorView instanceof ViewGroup) {
            hookViewClick(decorView);
            int count = ((ViewGroup) decorView).getChildCount();
            for (int i = 0; i < count; i++) {
                if (((ViewGroup) decorView).getChildAt(i) instanceof ViewGroup) {
                    hookDecorViewClick(((ViewGroup) decorView).getChildAt(i));
                } else {
                    hookViewClick(((ViewGroup) decorView).getChildAt(i));
                }
            }
        } else {
            hookViewClick(decorView);
        }
    }

    /**
     * 反射给View注册监听
     */
    private void hookViewClick(View view) throws Exception {
        int visibility = view.getVisibility();
        if (visibility == 4 || visibility == 8) {
            return;
        }
        if (!view.getGlobalVisibleRect(new Rect())) {
            return;
        }
        Class viewClass = Class.forName("android.view.View");
        Method getListenerInfoMethod = viewClass.getDeclaredMethod("getListenerInfo");
        if (!getListenerInfoMethod.isAccessible()) {
            getListenerInfoMethod.setAccessible(true);
        }
        Object listenerInfoObject = getListenerInfoMethod.invoke(view);
        Class mListenerInfoClass = Class.forName("android.view.View$ListenerInfo");
        Field mOnClickListenerField = mListenerInfoClass.getDeclaredField("mOnTouchListener");

//        Log.d("sanbo", view.hashCode() + "-----" + HeatMap.HookTouchListener.class.getName() +
//        " <-------> " + mOnClickListenerField.getType().getName());

        mOnClickListenerField.setAccessible(true);
        Object touchListenerObj = mOnClickListenerField.get(listenerInfoObject);
        if (!(touchListenerObj instanceof HookTouchListener)) {
//            printLog(view, touchListenerObj);
            HookTouchListener touchListenerProxy =
                    new HookTouchListener((View.OnTouchListener) touchListenerObj);
            mOnClickListenerField.set(listenerInfoObject, touchListenerProxy);
        }

    }

    private void setCoordinate(final View v, final MotionEvent event) {
        final float rawX = event.getRawX();
        final float rawY = event.getRawY();
        if (isTouch(rawX, rawY)) {
            x = event.getX();
            y = event.getY();

            ANSThreadPool.heatMapExecute(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (clickInfo == null) {
                            clickInfo = new ConcurrentHashMap<>();
                        } else {
                            clickInfo.clear();
                        }
                        setClickCoordinate();
                        final String path = PathGeneral.getInstance().general(v);
                        boolean isAddPath = setPath(path);
                        if (isAddPath) {
                            rx = rawX;
                            ry = rawY;
                            setClickContent(v);
                            clickInfo.putAll(pageInfo);
                            AgentProcess.getInstance().pageTouchInfo(clickInfo);
                        }
                    } catch (Throwable ignore) {
                        ExceptionUtil.exceptionThrow(ignore);
                    }
                }
            });
        }
    }

    /**
     * 过滤多层touch事件
     */
    private boolean isTouch(float rawX, float rawY) {
        if ((rx == 0 || ry == 0) || (rx != rawX || ry != rawY)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 添加点击控件坐标
     */
    private void setClickCoordinate() {
        // 基于屏幕
        clickInfo.put(Constants.TOUCH_CLICK_X, rx);
        clickInfo.put(Constants.TOUCH_CLICK_Y, ry);
        // 基于控件
        clickInfo.put(Constants.TOUCH_ELEMENT_X, x);
        clickInfo.put(Constants.TOUCH_ELEMENT_Y, y);
    }

    /**
     * 添加点击控件类型及内容
     */
    private void setClickContent(View v) throws Exception {
        int click = 0;
        if (v instanceof ImageButton || v instanceof Button) {
            click = 1;
        }
        clickInfo.put(Constants.TOUCH_ELEMENT_CLICKABLE, click);
        clickInfo.put(Constants.ELEMENT_TYPE, v.getClass().getName());
        // 控件内容
        String content = getContent(v);
        if (!TextUtils.isEmpty(content)) {
            clickInfo.put(Constants.ELEMENT_CONTENT, content);
        }
    }

    private boolean setPath(String path) throws Throwable {
        if (!TextUtils.isEmpty(path) && !CommonUtils.isEmpty(new JSONArray(path))) {
            clickInfo.put(Constants.ELEMENT_PATH, path.replaceAll(" ", ""));
            return true;
        }
        return false;
    }

    private String getContent(View view) throws Exception {
        Class<?> compatClass = null;
        compatClass = ReflectUtils.getClassByName("android.support.v7.widget.SwitchCompat");
        if (compatClass == null) {
            compatClass = ReflectUtils.getClassByName("androidx.appcompat.widget.SwitchCompat");
        }
        CharSequence charSequence = null;
        if (compatClass != null
                && compatClass.isInstance(view)) {
            CompoundButton compoundButton = (CompoundButton) view;
            Method method;
            if (compoundButton.isChecked()) {
                method = view.getClass().getMethod("getTextOn");
            } else {
                method = view.getClass().getMethod("getTextOff");
            }
            charSequence = (String) method.invoke(view);
        } else if (view instanceof CheckBox) {
            CheckBox checkBox = (CheckBox) view;
            charSequence = checkBox.getText();
        } else if (view instanceof RadioButton) {
            RadioButton radioButton = (RadioButton) view;
            charSequence = radioButton.getText();
        } else if (view instanceof ToggleButton) {
            ToggleButton toggleButton = (ToggleButton) view;
            boolean isChecked = toggleButton.isChecked();
            if (isChecked) {
                charSequence = toggleButton.getTextOn();
            } else {
                charSequence = toggleButton.getTextOff();
            }
        } else if (view instanceof Button) {
            Button button = (Button) view;
            charSequence = button.getText();
        } else if (view instanceof CheckedTextView) {
            CheckedTextView textView = (CheckedTextView) view;
            charSequence = textView.getText();
        } else if (view instanceof TextView) {
            TextView textView = (TextView) view;
            charSequence = textView.getText();
        } else if (view instanceof ImageView) {
            ImageView imageView = (ImageView) view;
            if (!TextUtils.isEmpty(imageView.getContentDescription())) {
                charSequence = String.valueOf(imageView.getContentDescription());
            }
        }
        if (!TextUtils.isEmpty(charSequence)) {
            return String.valueOf(charSequence);
        }
        return "";
    }

    private static class Holder {
        public static final HeatMap INSTANCE = new HeatMap();
    }

    private class HookTouchListener implements View.OnTouchListener {
        private View.OnTouchListener onTouchListener;

        private HookTouchListener(View.OnTouchListener onTouchListener) {
            this.onTouchListener = onTouchListener;
        }

        @Override
        public boolean onTouch(final View v, final MotionEvent event) {
//            Log.v("sanbo", Log.getStackTraceString(new Exception(v.hashCode() + "")));
            try {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    try {
                        // 黑白名单判断
                        if (isTackHeatMap(v)) {
                            setCoordinate(v, event);
                        }
                    } catch (Throwable ignore) {
                        ExceptionUtil.exceptionThrow(ignore);
                    }
                }
//            boolean x1 = patch(v, event);
                // 获取是否递归调用
                boolean isLoop = isLoop(Thread.currentThread().getStackTrace());
                // 无回调或者循环递归了，均不消费
                if (onTouchListener == null || isLoop) {
                    return false;
                } else {
                    return onTouchListener.onTouch(v, event);
                }
            } catch (Throwable ignore) {
                ExceptionUtil.exceptionThrow(ignore);
            }
            return false;
        }
    }


    /**
     * 根据堆栈查看是否循环递归
     *
     * @param ste 方法调用栈
     * @return true 递归
     */
    private boolean isLoop(StackTraceElement[] ste) {
        if (ste.length > 4) {
            String methodPath = ste[2].getClassName() + "." + ste[2].getMethodName();
            String methodPath2 = ste[3].getClassName() + "." + ste[3].getMethodName();
//            Log.i("sanbo", methodPath + "<----->" + methodPath2);
            return TextUtils.equals(methodPath, methodPath2);
        }
        return false;
    }


    // --------------------- 黑白名单 ---------------------------------

    // 黑白名单
    private HashSet<String> mIgnoreByPages = new HashSet<>();
    private HashSet<String> mAutoByPages = new HashSet<>();

    /**
     * 热图黑名单
     */
    void setHeatMapBlackListByPages(List<String> pages) {
        try {
            mIgnoreByPages.clear();
            if (pages != null) {
                mIgnoreByPages.addAll(pages);
            }
        }catch (Throwable ignore) {
            ExceptionUtil.exceptionThrow(ignore);
        }

    }

    /**
     * 热图白名单
     */
    void setHeatMapWhiteListByPages(List<String> pages) {
        try {
            mAutoByPages.clear();
            if (pages != null) {
                mAutoByPages.addAll(pages);
            }
        } catch (Throwable ignore) {
            ExceptionUtil.exceptionThrow(ignore);
        }

    }

    /**
     * 判断是否上报热图信息
     */
    private boolean isTackHeatMap(View v) {

        try {
            if (isInIgnoreList(v)) {
                // 命中黑名单
                return false;
            } else if (hasAutoList()) {
                // 存在白名单
                return isInAutoList(v); // 命中白名单
            }
        }catch (Throwable ignore) {
            ExceptionUtil.exceptionThrow(ignore);
        }

        return true;
    }

    /**
     * 判断是否存在黑名单
     *
     * @return 是否存在黑名单
     */
    private boolean hasAutoList() {
        return !mAutoByPages.isEmpty();
    }

    /**
     * 判断是否命中黑名单
     *
     * @return 是否命中白名单
     */
    private boolean isInIgnoreList(View v) {
        try {
            Context context = v.getContext();
            if (context instanceof Activity) {
                String pageName = context.getClass().getName();
                return !TextUtils.isEmpty(pageName) && mIgnoreByPages.contains(pageName);
            }
        } catch (Throwable ignore) {
            ExceptionUtil.exceptionThrow(ignore);
        }

        return false;
    }

    /**
     * 判断是否命中白名单
     *
     * @return 是否命中白名单
     */
    private boolean isInAutoList(View v) {
        try {
            Context context = v.getContext();
            if (context instanceof Activity) {
                String pageName = context.getClass().getName();
                return !TextUtils.isEmpty(pageName) && mAutoByPages.contains(pageName);
            }
        } catch (Throwable ignore) {
            ExceptionUtil.exceptionThrow(ignore);
        }

        return false;
    }

    // --------------------- 黑白名单 ---------------------------------

}