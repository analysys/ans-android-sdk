package com.analysys.allgro.plugin;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.analysys.AnalysysConfig;
import com.analysys.allgro.AllegroUtils;
import com.analysys.process.AgentProcess;
import com.analysys.utils.AnalysysUtil;
import com.analysys.utils.AnsReflectUtils;
import com.analysys.utils.Constants;
import com.analysys.utils.ExceptionUtil;
import com.analysys.utils.SharedUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * @author fengzeyuan
 */
public class PageViewProbe extends ASMHookAdapter {

    //  ------------------------------------ PageView Hook -------------------------------------------

    private final Map<Integer, Long> mShowFragment = new HashMap<>();

    /**
     * fragmentViewCreated 探针
     */
    @Override
    public void onFragmentViewCreated(Object object, View rootView, Bundle savedInstanceState, boolean hasTrackPvAnn) {
//        try {
//            if (!AgentProcess.getInstance().getConfig().isAutoTrackClick()) {
//                return;
//            }
//
//            if (!AllegroUtils.isFragment(object)) {
//                return;
//            }
//
//            //Fragment名称
//            String fragmentName = object.getClass().getName();
//            rootView.setTag(R.id.analysys_tag_fragment_name, fragmentName);
//
//            if (rootView instanceof ViewGroup) {
//                traverseView(fragmentName, (ViewGroup) rootView);
//            }
//        } catch (Throwable ignore) {
//            ExceptionUtil.exceptionThrow(ignore);
//        }
    }

    /**
     * fragmentResume 探针
     *
     * @param object fragment 实例
     */
    @Override
    public void trackFragmentResume(Object object, boolean hasTrackPvAnn, long currentTime) {

        try {
            if (!checkFragmentPVEnable(object, hasTrackPvAnn)) {
                return;
            }

            // 判断是否是Fragment
            if (isNotFragment(object)) {
                return;
            }
            Object parentFragment = AllegroUtils.getParentFragment(object);
            if (parentFragment == null) {
                if (fragmentIsShow(object) && fragmentGetUserVisibleHint(object)) {
                    autoTrackFragmentPageView(object, hasTrackPvAnn, currentTime);
                }
            } else {
                if (fragmentIsShow(object) && fragmentGetUserVisibleHint(object) && fragmentIsShow(parentFragment) && fragmentGetUserVisibleHint(parentFragment)) {
                    autoTrackFragmentPageView(object, hasTrackPvAnn, currentTime);
                }
            }
        } catch (Throwable ignore) {
            ExceptionUtil.exceptionThrow(ignore);
        }
    }

    @Override
    public void trackFragmentPause(Object object, boolean hasTrackPvAnn, long currentTime) {
        try {
            if (!checkFragmentPCEnable(object, hasTrackPvAnn)) {
                return;
            }

            // 判断是否是Fragment
            if (isNotFragment(object)) {
                return;
            }
            autoTrackFragmentPageClose(object, hasTrackPvAnn, currentTime);
        } catch (Throwable ignore) {
            ExceptionUtil.exceptionThrow(ignore);
        }
    }

    /**
     * SetUserVisibleHint 探针
     */
    @Override
    public void trackFragmentSetUserVisibleHint(Object object, boolean isVisibleToUser, boolean hasTrackPvAnn, long currentTime) {
        if (isVisibleToUser) {
            trackFragmentSetUserVisibleHintTrue(object, isVisibleToUser, hasTrackPvAnn, currentTime);
        } else {
            trackFragmentSetUserVisibleHintFalse(object, isVisibleToUser, hasTrackPvAnn, currentTime);
        }
    }

    private void trackFragmentSetUserVisibleHintTrue(Object object, boolean isVisibleToUser, boolean hasTrackPvAnn, long currentTime){
        try {
            if (!checkFragmentPVEnable(object, hasTrackPvAnn)) {
                return;
            }

            if (isNotFragment(object)) {
                return;
            }

            Object parentFragment = AllegroUtils.getParentFragment(object);

            if (parentFragment == null) {
                if (isVisibleToUser) {
                    if (fragmentIsResumed(object)) {
                        if (fragmentIsShow(object)) {
                            autoTrackFragmentPageView(object, hasTrackPvAnn, currentTime);
                        }
                    }
                }
            } else {
                if (isVisibleToUser && fragmentGetUserVisibleHint(parentFragment)) {
                    if (fragmentIsResumed(object) && fragmentIsResumed(parentFragment)) {
                        if (fragmentIsShow(object) && fragmentIsShow(parentFragment)) {
                            autoTrackFragmentPageView(object, hasTrackPvAnn, currentTime);
                        }
                    }
                }
            }
        } catch (Throwable ignore) {
            ExceptionUtil.exceptionThrow(ignore);
        }
    }

    private void trackFragmentSetUserVisibleHintFalse(Object object, boolean isVisibleToUser, boolean hasTrackPvAnn, long currentTime) {
        try {
            if (!checkFragmentPCEnable(object, hasTrackPvAnn)) {
                return;
            }

            if (isNotFragment(object)) {
                return;
            }
            autoTrackFragmentPageClose(object, hasTrackPvAnn, currentTime);
        } catch (Throwable ignore) {
            ExceptionUtil.exceptionThrow(ignore);
        }
    }

    /**
     * Fragment OnHiddenChanged 探针
     */
    @Override
    public void trackOnHiddenChanged(Object object, boolean hidden, boolean hasTrackPvAnn, long currentTime) {
        if (hidden) {
            trackOnHiddenChangedFalse(object, hidden, hasTrackPvAnn, currentTime);
        } else {
            trackOnHiddenChangedTrue(object, hidden, hasTrackPvAnn, currentTime);
        }
    }

    private void trackOnHiddenChangedTrue(Object object, boolean hidden, boolean hasTrackPvAnn, long currentTime) {
        try {
            if (!checkFragmentPVEnable(object, hasTrackPvAnn)) {
                return;
            }

            if (isNotFragment(object)) {
                return;
            }

            Object parentFragment = AllegroUtils.getParentFragment(object);

            if (parentFragment == null) {
                if (!hidden) {
                    if (fragmentIsResumed(object)) {
                        if (fragmentGetUserVisibleHint(object)) {
                            autoTrackFragmentPageView(object, hasTrackPvAnn, currentTime);
                        }
                    }
                }
            } else {
                if (!hidden && fragmentIsShow(parentFragment)) {
                    if (fragmentIsResumed(object) && fragmentIsResumed(parentFragment)) {
                        if (fragmentGetUserVisibleHint(object) && fragmentGetUserVisibleHint(parentFragment)) {
                            autoTrackFragmentPageView(object, hasTrackPvAnn, currentTime);
                        }
                    }
                }
            }
        } catch (Throwable ignore) {
            ExceptionUtil.exceptionThrow(ignore);
        }
    }

    private void trackOnHiddenChangedFalse(Object object, boolean hidden, boolean hasTrackPvAnn, long currentTime) {
        try {
            if (!checkFragmentPCEnable(object, hasTrackPvAnn)) {
                return;
            }

            if (isNotFragment(object)) {
                return;
            }
            autoTrackFragmentPageClose(object, hasTrackPvAnn, currentTime);
        } catch (Throwable ignore) {
            ExceptionUtil.exceptionThrow(ignore);
        }
    }

    private static boolean fragmentGetUserVisibleHint(Object fragment) {
        Object obj = AnsReflectUtils.invokeMethod(fragment, "getUserVisibleHint");
        if (obj != null) {
            return (boolean) obj;
        }
        return false;
    }

    private static boolean fragmentIsShow(Object fragment) {
        Object obj = AnsReflectUtils.invokeMethod(fragment, "isHidden");
        if (obj != null) {
            boolean flag = (boolean) obj;
            return !flag;
        }
        return true;
    }


    private static boolean fragmentIsResumed(Object fragment) {
        Object obj = AnsReflectUtils.invokeMethod(fragment, "isResumed");
        if (obj != null) {
            return (boolean) obj;
        }
        return false;
    }

    /**
     * 判断当前示例是不是Fragment
     */
    private static boolean isNotFragment(Object object) {
        Class<?> supportFragmentClass = null;
        Class<?> androidXFragmentClass = null;
        Class<?> fragment = null;
        fragment = AnsReflectUtils.getClassByName("android.app.Fragment");
        supportFragmentClass = AnsReflectUtils.getClassByName("android.support.v4.app.Fragment");
        androidXFragmentClass = AnsReflectUtils.getClassByName("androidx.fragment.app.Fragment");

        if (supportFragmentClass == null && androidXFragmentClass == null && fragment == null) {
            return true;
        }

        if ((supportFragmentClass != null && supportFragmentClass.isInstance(object)) ||
                (androidXFragmentClass != null && androidXFragmentClass.isInstance(object)) ||
                (fragment != null && fragment.isInstance(object))) {
            return false;
        }
        return true;
    }


    //  ------------------------------------ PageView Hook -------------------------------------------

    private void autoTrackFragmentPageClose(Object pageObj, boolean hasTrackPvAnn, long currentTime) throws Throwable {
        if (!checkFragmentPCEnable(pageObj, hasTrackPvAnn) || !checkPageCloseTime(pageObj, currentTime)) {
            return;
        }
        Long startTime = null;
        int pageHash = pageObj.hashCode();
        synchronized (mShowFragment) {
            startTime = mShowFragment.get(pageHash);
            if (startTime != null) {
                mShowFragment.remove(pageHash);
            }
        }

        if (startTime != null && currentTime > startTime) {
            Map<String, Object> pageInfo = AllegroUtils.getPageInfo(pageObj, false);
            pageInfo.put(Constants.PAGE_STAY_TIME, currentTime - startTime);
            AgentProcess.getInstance().autoCollectPageClose(pageHash, pageInfo, currentTime);
        }
    }

    private int mFragmentPageCloseHashcode;
    private long mFragmentPageCloseTime;

    private boolean checkPageCloseTime(Object page, long time) {
        // 避免重复调用
        int hashcode = page.hashCode();
        if (mFragmentPageCloseHashcode == hashcode) {
            if (Math.abs(mFragmentPageCloseTime - time) < 500) {
                return false;
            } else {
                mFragmentPageCloseTime = time;
            }
        } else {
            mFragmentPageCloseHashcode = hashcode;
            mFragmentPageCloseTime = time;
        }
        return true;
    }

    private void autoTrackFragmentPageView(Object pageObj, boolean hasTrackPvAnn, long currentTime) throws Throwable {
        if (!checkFragmentPVEnable(pageObj, hasTrackPvAnn) || !checkPageViewTime(pageObj, currentTime)) {
            return;
        }
        int pageHash = pageObj.hashCode();
        synchronized (mShowFragment) {
            if (!mShowFragment.containsKey(pageHash)) {
                mShowFragment.put(pageHash, currentTime);
            } else {
                return;
            }
        }

        // 拼接PV上报事
        // 1.初始化property
        Map<String, Object> pageInfo = AllegroUtils.getPageInfo(pageObj, false);
        // 2、设置PAGE_REFERRER
        String pRef = SharedUtil.getString(AnalysysUtil.getContext(), Constants.SP_REFER, "");
        if (!TextUtils.isEmpty(pRef)) {
            pageInfo.put(Constants.PAGE_REFERRER, pRef);
        }
        SharedUtil.setString(AnalysysUtil.getContext(), Constants.SP_REFER, (String) pageInfo.get(Constants.PAGE_URL));
        // 上报PV
        AgentProcess.getInstance().autoCollectPageView(pageInfo, currentTime);
    }

    private int mFragmentPageViewHashcode;
    private long mFragmentPageViewTime;

    private boolean checkPageViewTime(Object page, long time) {
        // 避免重复调用
        int hashcode = page.hashCode();
        if (mFragmentPageViewHashcode == hashcode) {
            if (Math.abs(mFragmentPageViewTime - time) < 500) {
                return false;
            } else {
                mFragmentPageViewTime = time;
            }
        } else {
            mFragmentPageViewHashcode = hashcode;
            mFragmentPageViewTime = time;
        }
        return true;
    }

//    private void traverseView(String fragmentName, ViewGroup root) {
//        if (TextUtils.isEmpty(fragmentName)) {
//            return;
//        }
//
//        if (root == null) {
//            return;
//        }
//
//        final int childCount = root.getChildCount();
//        for (int i = 0; i < childCount; ++i) {
//            final View child = root.getChildAt(i);
//            if (child != null) {
//                child.setTag(R.id.analysys_tag_fragment_name, fragmentName);
//                if (child instanceof ViewGroup && !(child instanceof ListView ||
//                        child instanceof GridView ||
//                        child instanceof Spinner ||
//                        child instanceof RadioGroup)) {
//                    traverseView(fragmentName, (ViewGroup) child);
//                }
//            }
//        }
//    }

    //  ----------------- 黑白名单策略 ---------------------------------

    /**
     * 判断是否触发PV上报
     *
     * @param page           页面对象
     * @param hasAutoPageAnn 是否有AutoPage 注解
     */
    private boolean checkFragmentPVEnable(Object page, boolean hasAutoPageAnn) {
        AgentProcess instance = AgentProcess.getInstance();
        AnalysysConfig config = instance.getConfig();
        if (config.isAutoTrackFragmentPageView()) {
            String pageName = page.getClass().getName();
            if (instance.isThisPageInPageViewBlackList(pageName)) {
                return false;
            } else if (instance.hasAutoPageViewWhiteList()) {
                return instance.isThisPageInPageViewWhiteList(pageName);
            }
            return true;
        }
        return false;
    }

    /**
     * 判断是否触发pageclose上报
     *
     * @param page           页面对象
     * @param hasAutoPageAnn 是否有AutoPage 注解
     */
    private boolean checkFragmentPCEnable(Object page, boolean hasAutoPageAnn) {
        AgentProcess instance = AgentProcess.getInstance();
        AnalysysConfig config = instance.getConfig();
//        return checkFragmentPVEnable(page, hasAutoPageAnn) && config.isAutoPageViewDuration();
        return config.isAutoPageViewDuration();
    }
}