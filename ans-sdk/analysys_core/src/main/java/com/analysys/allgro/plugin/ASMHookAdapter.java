package com.analysys.allgro.plugin;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.RadioGroup;

/**
 * Description:
 * Author: fengzeyuan
 * Date: 2019-11-21 16:33
 * Version: 1.0
 */
public class ASMHookAdapter {
    // ------------------------ Fragment ---------------------------------------

    /**
     * fragmentViewCreated 探针
     */
    void onFragmentViewCreated(Object object, View rootView, Bundle savedInstanceState, boolean hasTrackPvAnn){}

    /**
     * fragmentResume 探针
     *
     * @param object fragment 实例
     */
    void trackFragmentResume(Object object, boolean hasTrackPvAnn,long currentTime){}

    /**
     * fragmentPause 探针
     *
     * @param object fragment 实例
     */
    void trackFragmentPause(Object object, boolean hasTrackPvAnn,long currentTime){}

    /**
     * SetUserVisibleHint 探针
     */
    void trackFragmentSetUserVisibleHint(Object object, boolean isVisibleToUser, boolean hasTrackPvAnn,long currentTime){}

    /**
     * Fragment OnHiddenChanged 探针
     */
    void trackOnHiddenChanged(Object object, boolean hidden, boolean hasTrackPvAnn,long currentTime){}


    // ------------------------ Click ---------------------------------------

    /**
     * Dialog 点击
     */
    void trackDialog(DialogInterface dialogInterface, int which, boolean hasTrackClickAnn,long currentTime){}

    /**
     * TabLayout 点击
     */
    void trackTabLayout(Object object, Object tab, boolean hasTrackClickAnn,long currentTime){}

    /**
     * TabHost 点击
     */
    void trackTabHost(String tabName, boolean hasTrackClickAnn,long currentTime){}

    /**
     * RadioGroup 点击
     */
    void trackRadioGroup(RadioGroup parent, int checkedId, boolean hasTrackClickAnn,long currentTime){}

    /**
     * 扩展ListView Child Item点击
     */
    void trackExpListViewChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, boolean hasTrackClickAnn,long currentTime){}

    /**
     * 扩展列表控件GroupItem点击
     */
    void trackExpListViewGroupClick(ExpandableListView parent, View v, int groupPosition, boolean hasTrackClickAnn,long currentTime){}

    /**
     * 列表控件点击
     */
    void trackListView(AdapterView<?> parent, View v, int position, boolean hasTrackClickAnn,long currentTime){}

    /**
     * 独立控件点击
     */
    void trackViewOnClick(View v, boolean hasTrackClickAnn,long currentTime){}

    /**
     * Accessibility事件
     */
    public void trackSendAccessibilityEvent(View v, int eventType, final boolean hasTrackClickAnn) {}

    /**
     * 菜单控件点击
     */
    void trackMenuItem(Object obj,MenuItem menuItem, boolean hasTrackClickAnn,long currentTime){}

    /**
     * 菜单控件点击
     */
    void trackMenuItem(MenuItem menuItem, boolean hasTrackClickAnn,long currentTime){}

    /**
     * DrawerLayout控件点击
     */
    void trackDrawerSwitch(View drawerLayout,boolean isOpen, boolean hasTrackClickAnn,long currentTime){}

    /**
     * 可能的XML控件点击绑定
     */
    void maybeClickInXML(View v, boolean methodName, boolean hasTrackClickAnn){}

}
