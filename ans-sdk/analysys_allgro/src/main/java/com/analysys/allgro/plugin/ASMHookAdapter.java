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
 * Date: 2019-11-21 16:37
 * Version: 1.0
 */
public class ASMHookAdapter implements ASMHookInterface {

    ASMHookAdapter() {
//        SystemIds.getInstance().parserId();
    }

    @Override
    public void onFragmentViewCreated(Object object, View rootView, Bundle savedInstanceState, boolean hasTrackPvAnn) {
        
    }

    @Override
    public void trackFragmentResume(Object object, boolean hasTrackPvAnn,long currentTime) {

    }

    @Override
    public void trackFragmentSetUserVisibleHint(Object object, boolean isVisibleToUser, boolean hasTrackPvAnn,long currentTime) {

    }

    @Override
    public void trackOnHiddenChanged(Object object, boolean hidden, boolean hasTrackPvAnn,long currentTime) {

    }

    @Override
    public void trackDialog(DialogInterface dialogInterface, int which, boolean hasTrackClickAnn,long currentTime) {

    }

    @Override
    public void trackTabLayout(Object object, Object tab, boolean hasTrackClickAnn,long currentTime) {

    }

    @Override
    public void trackTabHost(String tabName, boolean hasTrackClickAnn,long currentTime) {

    }

    @Override
    public void trackRadioGroup(RadioGroup parent, int checkedId, boolean hasTrackClickAnn,long currentTime) {

    }

    @Override
    public void trackExpListViewChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, boolean hasTrackClickAnn,long currentTime) {

    }

    @Override
    public void trackExpListViewGroupClick(ExpandableListView parent, View v, int groupPosition, boolean hasTrackClickAnn,long currentTime) {

    }

    @Override
    public void trackListView(AdapterView<?> parent, View v, int position, boolean hasTrackClickAnn,long currentTime) {

    }

    @Override
    public void trackViewOnClick(View v, boolean hasTrackClickAnn,long currentTime) {

    }

    @Override
    public void trackMenuItem(Object obj,MenuItem menuItem, boolean hasTrackClickAnn,long currentTime) {

    }

    @Override
    public void trackMenuItem(MenuItem menuItem, boolean hasTrackClickAnn,long currentTime) {
        
    }

    @Override
    public void trackDrawerSwitch(View drawerLayout, boolean isOpen, boolean hasTrackClickAnn,long currentTime) {
        
    }

    @Override
    public void maybeClickInXML(View v, boolean methodName, boolean hasTrackClickAnn) {

    }
}
