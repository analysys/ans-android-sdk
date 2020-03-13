package com.analysys.allgro.plugin;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.RadioGroup;

import com.analysys.allgro.AllegroUtils;
import com.analysys.utils.ANSThreadPool;
import com.analysys.utils.ExceptionUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


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
    public void onFragmentViewCreated(final Object object, final View rootView, final Bundle savedInstanceState, final boolean hasTrackPvAnn) {
        ANSThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    for (ASMHookInterface observer : mObservers) {
                        observer.onFragmentViewCreated(object, rootView, savedInstanceState, hasTrackPvAnn);
                    }
                } catch (Throwable ignore) {
                    ExceptionUtil.exceptionThrow(ignore);
                }
            }
        });
    }

    @Override
    public void trackFragmentResume(final Object object, final boolean hasTrackPvAnn) {
        ANSThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    for (ASMHookInterface observer : mObservers) {
                        observer.trackFragmentResume(object, hasTrackPvAnn);
                    }
                } catch (Throwable ignore) {
                    ExceptionUtil.exceptionThrow(ignore);
                }
            }
        });

    }

    @Override
    public void trackFragmentSetUserVisibleHint(final Object object, final boolean isVisibleToUser, final boolean hasTrackPvAnn) {
        ANSThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    //        Log.d("javen",String.format("call trackFragmentSetUserVisibleHint in %s ",object.getClass().getSimpleName()));
                    for (ASMHookInterface observer : mObservers) {
                        observer.trackFragmentSetUserVisibleHint(object, isVisibleToUser, hasTrackPvAnn);
                    }
                } catch (Throwable ignore) {
                    ExceptionUtil.exceptionThrow(ignore);
                }

            }
        });

    }

    @Override
    public void trackOnHiddenChanged(final Object object, final boolean hidden, final boolean hasTrackPvAnn) {
        ANSThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    //        Log.d("javen",String.format("call trackOnHiddenChanged in %s ",object.getClass().getSimpleName()));
                    for (ASMHookInterface observer : mObservers) {
                        observer.trackOnHiddenChanged(object, hidden, hasTrackPvAnn);
                    }
                } catch (Throwable ignore) {
                    ExceptionUtil.exceptionThrow(ignore);
                }
            }
        });

    }


    // ------------------------ Click ---------------------------------------

    @Override
    public void trackDialog(final DialogInterface dialogInterface, final int which, final boolean hasTrackClickAnn) {
        ANSThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (isInLimitTime(dialogInterface)) {
                        return;
                    }
                    for (ASMHookInterface observer : mObservers) {
                        observer.trackDialog(dialogInterface, which, hasTrackClickAnn);
                    }
                } catch (Throwable ignore) {
                    ExceptionUtil.exceptionThrow(ignore);
                }
            }
        });

    }

    @Override
    public void trackTabLayout(final Object object, final Object tab, final boolean hasTrackClickAnn) {
        ANSThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (isInLimitTime(tab)) {
                        return;
                    }
                    for (ASMHookInterface observer : mObservers) {
                        observer.trackTabLayout(object, tab, hasTrackClickAnn);
                    }
                } catch (Throwable ignore) {
                    ExceptionUtil.exceptionThrow(ignore);
                }
            }
        });

    }

    @Override
    public void trackTabHost(final String tabName, final boolean hasTrackClickAnn) {
        ANSThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (isInLimitTime(tabName)) {
                        return;
                    }
                    for (ASMHookInterface observer : mObservers) {
                        observer.trackTabHost(tabName, hasTrackClickAnn);
                    }
                } catch (Throwable ignore) {
                    ExceptionUtil.exceptionThrow(ignore);
                }
            }
        });


    }

    @Override
    public void trackRadioGroup(final RadioGroup parent, final int checkedId, final boolean hasTrackClickAnn) {
        ANSThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
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
                } catch (Throwable ignore) {
                    ExceptionUtil.exceptionThrow(ignore);
                }
            }
        });

    }

    @Override
    public void trackExpListViewChildClick(final ExpandableListView parent, final View v, final int groupPosition, final int childPosition, final boolean hasTrackClickAnn) {
        ANSThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (isInLimitTime(v)) {
                        return;
                    }
                    for (ASMHookInterface observer : mObservers) {
                        observer.trackExpListViewChildClick(parent, v, groupPosition, childPosition, hasTrackClickAnn);
                    }
                } catch (Throwable ignore) {
                    ExceptionUtil.exceptionThrow(ignore);
                }
            }
        });

    }

    @Override
    public void trackExpListViewGroupClick(final ExpandableListView parent, final View v, final int groupPosition, final boolean hasTrackClickAnn) {
        ANSThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (isInLimitTime(v)) {
                        return;
                    }
                    for (ASMHookInterface observer : mObservers) {
                        observer.trackExpListViewGroupClick(parent, v, groupPosition, hasTrackClickAnn);
                    }
                } catch (Throwable ignore) {
                    ExceptionUtil.exceptionThrow(ignore);
                }
            }
        });

    }

    @Override
    public void trackListView(final AdapterView<?> parent, final View v, final int position, final boolean hasTrackClickAnn) {
        ANSThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (isInLimitTime(v)) {
                        return;
                    }
                    for (ASMHookInterface observer : mObservers) {
                        observer.trackListView(parent, v, position, hasTrackClickAnn);
                    }
                } catch (Throwable ignore) {
                    ExceptionUtil.exceptionThrow(ignore);
                }
            }
        });

    }

    @Override
    public void trackViewOnClick(final View v, final boolean hasTrackClickAnn) {
        ANSThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (isInLimitTime(v)) {
                        return;
                    }
                    for (ASMHookInterface observer : mObservers) {
                        observer.trackViewOnClick(v, hasTrackClickAnn);
                    }
                } catch (Throwable ignore) {
                    ExceptionUtil.exceptionThrow(ignore);
                }
            }
        });

    }

    @Override
    public void trackMenuItem(final Object obj, final MenuItem menuItem, final boolean hasTrackClickAnn) {
        ANSThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (isInLimitTime(menuItem)) {
                        return;
                    }
                    for (ASMHookInterface observer : mObservers) {
                        observer.trackMenuItem(obj, menuItem, hasTrackClickAnn);
                    }
                } catch (Throwable ignore) {
                    ExceptionUtil.exceptionThrow(ignore);
                }
            }
        });

    }

    @Override
    public void trackMenuItem(MenuItem menuItem, boolean hasTrackClickAnn) {

    }

    @Override
    public void trackDrawerSwitch(final View drawerLayout, final boolean isOpen, final boolean hasTrackClickAnn) {
        ANSThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (isInLimitTime(drawerLayout)) {
                        return;
                    }

                    for (ASMHookInterface observer : mObservers) {
                        observer.trackDrawerSwitch(drawerLayout, isOpen, hasTrackClickAnn);
                    }
                } catch (Throwable ignore) {
                    ExceptionUtil.exceptionThrow(ignore);
                }
            }
        });

    }

    @Override
    public void maybeClickInXML(final View v, final boolean methodName, final boolean hasTrackClickAnn) {
        ANSThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
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
                        observer.trackViewOnClick(v, hasTrackClickAnn);
                    }
                } catch (Throwable ignore) {
                    ExceptionUtil.exceptionThrow(ignore);
                }

            }
        });

    }

    private static HashMap<Integer, Long> eventTimestamp = new HashMap<>();
    private static int mCount;

    static boolean isInLimitTime(Object object) {
        boolean isDeBounceTrack = false;
        try {
            if (object == null) {
                return true;
            }
            mCount++;

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

                for (Iterator<Map.Entry<Integer, Long>> it = eventTimestamp.entrySet().iterator(); it.hasNext(); ) {
                    Map.Entry<Integer, Long> item = it.next();
                    if ((currentOnClickTimestamp - item.getValue()) > 500) {
                        it.remove();
                    }
                }
            }
        } catch (Throwable ignore) {
            ExceptionUtil.exceptionThrow(ignore);
        }

        return isDeBounceTrack;
    }

}
