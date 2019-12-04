package com.analysys.allgro.plugin;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.analysys.AnalysysConfig;
import com.analysys.allgro.AllegroUtils;
import com.analysys.allgro.BuglyUtils;
import com.analysys.allgro.BuildConfig;
import com.analysys.allgro.R;
import com.analysys.process.AgentProcess;
import com.analysys.utils.AnalysysUtil;
import com.analysys.utils.CommonUtils;
import com.analysys.utils.Constants;

import java.lang.reflect.Method;
import java.util.Map;

import androidx.annotation.Nullable;

/**
 * @author fengzeyuan
 */
class PageViewProbe extends ASMHookAdapter {

    //  ------------------------------------ PageView Hook -------------------------------------------


    /**
     * fragmentViewCreated 探针
     */
    public void onFragmentViewCreated(Object object, View rootView, @Nullable Bundle savedInstanceState, boolean hasTrackPvAnn) {
        try {
            if (!AgentProcess.getInstance().getConfig().isAutoTrackClick()) {
                return;
            }

            if (!AllegroUtils.isFragment(object)) {
                return;
            }

            //Fragment名称
            String fragmentName = object.getClass().getName();
            rootView.setTag(R.id.analysys_tag_fragment_name, fragmentName);

            if (rootView instanceof ViewGroup) {
                traverseView(fragmentName, (ViewGroup) rootView);
            }
        } catch (Exception e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
        }
    }

    /**
     * fragmentResume 探针
     *
     * @param object fragment 实例
     */
    public void trackFragmentResume(Object object, boolean hasTrackPvAnn) {

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
                    autoTrackFragmentPageView(object, hasTrackPvAnn);
                }
            } else {
                if (fragmentIsShow(object) && fragmentGetUserVisibleHint(object) && fragmentIsShow(parentFragment) && fragmentGetUserVisibleHint(parentFragment)) {
                    autoTrackFragmentPageView(object, hasTrackPvAnn);
                }
            }
        } catch (Exception e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
        }
    }

    /**
     * SetUserVisibleHint 探针
     */
    public void trackFragmentSetUserVisibleHint(Object object, boolean isVisibleToUser, boolean hasTrackPvAnn) {

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
                            autoTrackFragmentPageView(object, hasTrackPvAnn);
                        }
                    }
                }
            } else {
                if (isVisibleToUser && fragmentGetUserVisibleHint(parentFragment)) {
                    if (fragmentIsResumed(object) && fragmentIsResumed(parentFragment)) {
                        if (fragmentIsShow(object) && fragmentIsShow(parentFragment)) {
                            autoTrackFragmentPageView(object, hasTrackPvAnn);
                        }
                    }
                }
            }
        } catch (Exception e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
        }
    }

    /**
     * Fragment OnHiddenChanged 探针
     */
    public void trackOnHiddenChanged(Object object, boolean hidden, boolean hasTrackPvAnn) {

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
                            autoTrackFragmentPageView(object, hasTrackPvAnn);
                        }
                    }
                }
            } else {
                if (!hidden && fragmentIsShow(parentFragment)) {
                    if (fragmentIsResumed(object) && fragmentIsResumed(parentFragment)) {
                        if (fragmentGetUserVisibleHint(object) && fragmentGetUserVisibleHint(parentFragment)) {
                            autoTrackFragmentPageView(object, hasTrackPvAnn);
                        }
                    }
                }
            }
        } catch (Exception e) {
            if (BuildConfig.ENABLE_BUGLY) {
                BuglyUtils.commitError(e);
            }
        }
    }

    private static boolean fragmentGetUserVisibleHint(Object fragment) {
        try {
            Method getUserVisibleHintMethod = fragment.getClass().getMethod("getUserVisibleHint");
            return (boolean) getUserVisibleHintMethod.invoke(fragment);
        } catch (Exception ignored) {
        }
        return false;
    }

    private static boolean fragmentIsShow(Object fragment) {
        try {
            Method isHiddenMethod = fragment.getClass().getMethod("isHidden");
            return !((boolean) isHiddenMethod.invoke(fragment));
        } catch (Exception ignored) {
        }
        return true;
    }


    private static boolean fragmentIsResumed(Object fragment) {
        try {
            Method isResumedMethod = fragment.getClass().getMethod("isResumed");
            return (boolean) isResumedMethod.invoke(fragment);
        } catch (Exception ignored) {
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
        try {
            fragment = Class.forName("android.app.Fragment");
        } catch (Exception ignored) {
        }
        try {
            supportFragmentClass = Class.forName("android.support.v4.app.Fragment");
        } catch (Exception ignored) {
        }

        try {
            androidXFragmentClass = Class.forName("androidx.fragment.app.Fragment");
        } catch (Exception ignored) {
        }

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


    private void autoTrackFragmentPageView(Object pageObj, boolean hasTrackPvAnn) throws Exception {
        if (!checkFragmentPVEnable(pageObj, hasTrackPvAnn)) {
            return;
        }

        // 拼接PV上报事
        // 1.初始化property
        Map<String, Object> pageInfo = AllegroUtils.getPageInfo(pageObj);
        // 2、设置PAGE_REFERRER
        String pRef = CommonUtils.getIdFile(AnalysysUtil.getContext(), Constants.SP_REFER);
        if (!TextUtils.isEmpty(pRef)) {
            pageInfo.put(Constants.PAGE_REFERRER, pRef);
        }
        CommonUtils.setIdFile(AnalysysUtil.getContext(), Constants.SP_REFER, (String) pageInfo.get(Constants.PAGE_URL));
        // 上报PV
        AgentProcess.getInstance().autoCollectPageView(pageInfo);
    }


    private void traverseView(String fragmentName, ViewGroup root) {
        if (TextUtils.isEmpty(fragmentName)) {
            return;
        }

        if (root == null) {
            return;
        }

        final int childCount = root.getChildCount();
        for (int i = 0; i < childCount; ++i) {
            final View child = root.getChildAt(i);
            child.setTag(R.id.analysys_tag_fragment_name, fragmentName);
            if (child instanceof ViewGroup && !(child instanceof ListView ||
                    child instanceof GridView ||
                    child instanceof Spinner ||
                    child instanceof RadioGroup)) {
                traverseView(fragmentName, (ViewGroup) child);
            }
        }
    }

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