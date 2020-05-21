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

import java.util.Map;

/**
 * @author fengzeyuan
 */
class PageViewProbe extends ASMHookAdapter {

    //  ------------------------------------ PageView Hook -------------------------------------------


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
    public void trackFragmentResume(Object object, boolean hasTrackPvAnn,long currentTime) {

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
                    autoTrackFragmentPageView(object, hasTrackPvAnn,currentTime);
                }
            } else {
                if (fragmentIsShow(object) && fragmentGetUserVisibleHint(object) && fragmentIsShow(parentFragment) && fragmentGetUserVisibleHint(parentFragment)) {
                    autoTrackFragmentPageView(object, hasTrackPvAnn,currentTime);
                }
            }
        } catch (Throwable ignore) {
            ExceptionUtil.exceptionThrow(ignore);
        }
    }

    /**
     * SetUserVisibleHint 探针
     */
    @Override
    public void trackFragmentSetUserVisibleHint(Object object, boolean isVisibleToUser, boolean hasTrackPvAnn,long currentTime) {

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
                            autoTrackFragmentPageView(object, hasTrackPvAnn,currentTime);
                        }
                    }
                }
            } else {
                if (isVisibleToUser && fragmentGetUserVisibleHint(parentFragment)) {
                    if (fragmentIsResumed(object) && fragmentIsResumed(parentFragment)) {
                        if (fragmentIsShow(object) && fragmentIsShow(parentFragment)) {
                            autoTrackFragmentPageView(object, hasTrackPvAnn,currentTime);
                        }
                    }
                }
            }
        } catch (Throwable ignore) {
            ExceptionUtil.exceptionThrow(ignore);
        }
    }

    /**
     * Fragment OnHiddenChanged 探针
     */
    @Override
    public void trackOnHiddenChanged(Object object, boolean hidden, boolean hasTrackPvAnn,long currentTime) {

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
                            autoTrackFragmentPageView(object, hasTrackPvAnn,currentTime);
                        }
                    }
                }
            } else {
                if (!hidden && fragmentIsShow(parentFragment)) {
                    if (fragmentIsResumed(object) && fragmentIsResumed(parentFragment)) {
                        if (fragmentGetUserVisibleHint(object) && fragmentGetUserVisibleHint(parentFragment)) {
                            autoTrackFragmentPageView(object, hasTrackPvAnn,currentTime);
                        }
                    }
                }
            }
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


    private void autoTrackFragmentPageView(Object pageObj, boolean hasTrackPvAnn,long currentTime) throws Throwable {
        if (!checkFragmentPVEnable(pageObj, hasTrackPvAnn)) {
            return;
        }

        // 拼接PV上报事
        // 1.初始化property
        Map<String, Object> pageInfo = AllegroUtils.getPageInfo(pageObj,false);
        // 2、设置PAGE_REFERRER
        String pRef = SharedUtil.getString(AnalysysUtil.getContext(), Constants.SP_REFER,"");
        if (!TextUtils.isEmpty(pRef)) {
            pageInfo.put(Constants.PAGE_REFERRER, pRef);
        }
        SharedUtil.setString(AnalysysUtil.getContext(), Constants.SP_REFER, (String) pageInfo.get(Constants.PAGE_URL));
        // 上报PV
        AgentProcess.getInstance().autoCollectPageView(pageInfo,currentTime);
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
}