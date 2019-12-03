package com.analysys.allgro.plugin;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.RadioGroup;

import com.analysys.allgro.AllegroUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import androidx.annotation.Nullable;

/**
 * Description:
 * Author: fengzeyuan
 * Date: 2019-11-21 15:56
 * Version: 1.0
 */
public class ASMProbeHelp implements ASMHookInterface {

    private static class Holder {
        public static final ASMProbeHelp instance = new ASMProbeHelp();
    }

    private ASMProbeHelp() {
        registerHookObserver(new PageViewProbe());
        registerHookObserver(new ViewClickProbe());
    }

    public static ASMProbeHelp getInstance() {
        return Holder.instance;
    }

    private Set<ASMHookInterface> mObservers = new HashSet<>();

    public void registerHookObserver(ASMHookAdapter observer) {
        mObservers.add(observer);
    }



    // ------------------------ Fragment ---------------------------------------

    @Override
    public void onFragmentViewCreated(Object object, View rootView, @Nullable Bundle savedInstanceState, boolean hasTrackPvAnn) {
//        Log.d("javen",String.format("call onFragmentViewCreated in %s ",object.getClass().getSimpleName()));
        for (ASMHookInterface observer : mObservers) {
            observer.onFragmentViewCreated(object, rootView, savedInstanceState, hasTrackPvAnn);
        }
    }

    @Override
    public void trackFragmentResume(Object object, boolean hasTrackPvAnn) {
//        Log.d("javen",String.format("call trackFragmentResume in %s ",object.getClass().getSimpleName()));
        for (ASMHookInterface observer : mObservers) {
            observer.trackFragmentResume(object, hasTrackPvAnn);
        }
    }

    @Override
    public void trackFragmentSetUserVisibleHint(Object object, boolean isVisibleToUser, boolean hasTrackPvAnn) {
//        Log.d("javen",String.format("call trackFragmentSetUserVisibleHint in %s ",object.getClass().getSimpleName()));
        for (ASMHookInterface observer : mObservers) {
            observer.trackFragmentSetUserVisibleHint(object, isVisibleToUser, hasTrackPvAnn);
        }
    }

    @Override
    public void trackOnHiddenChanged(Object object, boolean hidden, boolean hasTrackPvAnn) {
//        Log.d("javen",String.format("call trackOnHiddenChanged in %s ",object.getClass().getSimpleName()));
        for (ASMHookInterface observer : mObservers) {
            observer.trackOnHiddenChanged(object, hidden, hasTrackPvAnn);
        }
    }


    // ------------------------ Click ---------------------------------------

    @Override
    public void trackDialog(DialogInterface dialogInterface, int which, boolean hasTrackClickAnn) {
        if (isInLimitTime(dialogInterface)) {
            return;
        }
        for (ASMHookInterface observer : mObservers) {
            observer.trackDialog(dialogInterface, which, hasTrackClickAnn);
        }
    }

    @Override
    public void trackTabLayout(Object object, Object tab, boolean hasTrackClickAnn) {
        if (isInLimitTime(tab)) {
            return;
        }
        for (ASMHookInterface observer : mObservers) {
            observer.trackTabLayout(object, tab, hasTrackClickAnn);
        }
    }

    @Override
    public void trackTabHost(String tabName, boolean hasTrackClickAnn) {
        if (isInLimitTime(tabName)) {
            return;
        }
        for (ASMHookInterface observer : mObservers) {
            observer.trackTabHost(tabName, hasTrackClickAnn);
        }
    }

    @Override
    public void trackRadioGroup(RadioGroup parent, int checkedId, boolean hasTrackClickAnn) {
        View childView = null;
        if (parent != null) {
            childView = parent.findViewById(checkedId);
        }       
        if (isInLimitTime(childView)) {
            return;
        }
        for (ASMHookInterface observer : mObservers) {
            observer.trackRadioGroup(parent, checkedId, hasTrackClickAnn);
        }
    }

    @Override
    public void trackExpListViewChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, boolean hasTrackClickAnn) {
        if (isInLimitTime(v)) {
            return;
        }
        for (ASMHookInterface observer : mObservers) {
            observer.trackExpListViewChildClick(parent, v, groupPosition, childPosition, hasTrackClickAnn);
        }
    }

    @Override
    public void trackExpListViewGroupClick(ExpandableListView parent, View v, int groupPosition, boolean hasTrackClickAnn) {
        if (isInLimitTime(v)) {
            return;
        }
        for (ASMHookInterface observer : mObservers) {
            observer.trackExpListViewGroupClick(parent, v, groupPosition, hasTrackClickAnn);
        }
    }

    @Override
    public void trackListView(AdapterView<?> parent, View v, int position, boolean hasTrackClickAnn) {
        if (isInLimitTime(v)) {
            return;
        }
        for (ASMHookInterface observer : mObservers) {
            observer.trackListView(parent, v, position, hasTrackClickAnn);
        }
    }

    @Override
    public void trackViewOnClick(View v, boolean hasTrackClickAnn) {
        if (isInLimitTime(v)) {
            return;
        }
        for (ASMHookInterface observer : mObservers) {
            observer.trackViewOnClick(v, hasTrackClickAnn);
        }
    }

    @Override
    public void trackMenuItem(Object obj, MenuItem menuItem, boolean hasTrackClickAnn) {
        if (isInLimitTime(menuItem)) {
            return;
        }
        for (ASMHookInterface observer : mObservers) {
            observer.trackMenuItem(obj,menuItem, hasTrackClickAnn);
        }
    }

    @Override
    public void trackMenuItem(MenuItem menuItem, boolean hasTrackClickAnn) {
        
    }

    @Override
    public void trackDrawerSwitch(View drawerLayout, boolean isOpen, boolean hasTrackClickAnn) {
        if (isInLimitTime(drawerLayout)) {
            return;
        }

        for (ASMHookInterface observer : mObservers) {
            observer.trackDrawerSwitch(drawerLayout,isOpen, hasTrackClickAnn);
        }
    }

    @Override
    public void maybeClickInXML(View v, boolean methodName, boolean hasTrackClickAnn) {
        if (isInLimitTime(v)) {
            return;
        }
        Object listenerInfo = AllegroUtils.getFieldValue(v, "mListenerInfo");
        if (listenerInfo == null) {
            return;
        }
        Object onClickListener = AllegroUtils.getFieldValue(listenerInfo, "mOnClickListener");
        if (onClickListener == null) {
            return;
        }
        Object mtdName = AllegroUtils.getFieldValue(onClickListener, "mMethodName");
        if (mtdName == null || !mtdName.equals(methodName)) {
            return;
        }
        for (ASMHookInterface observer : mObservers) {
            observer.trackViewOnClick(v,hasTrackClickAnn);
        }
    }

    private static HashMap<Integer, Long> eventTimestamp = new HashMap<>();
    private static int mCount;

    static boolean isInLimitTime(Object object) {
        if (object == null) {
            return true;
        }
        mCount++;
        boolean isDeBounceTrack = false;
        long currentOnClickTimestamp = System.currentTimeMillis();
        Object targetObject = eventTimestamp.get(object.hashCode());
        if (targetObject != null) {
            long lastOnClickTimestamp = (long) targetObject;
            if ((currentOnClickTimestamp - lastOnClickTimestamp) < 500) {
                isDeBounceTrack = true;
            }
        }
        eventTimestamp.put(object.hashCode(), currentOnClickTimestamp);

        // 计数器检测，定时清
        if (mCount > 1000) {
            mCount = 0;
            for (Map.Entry<Integer, Long> entry : eventTimestamp.entrySet()) {
                if ((currentOnClickTimestamp - entry.getValue()) > 500) {
                    eventTimestamp.remove(entry.getKey());
                }
            }
        }
        return isDeBounceTrack;
    }

}
