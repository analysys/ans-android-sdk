package com.analysys.allgro.plugin;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.RadioGroup;

import com.analysys.thread.AnsLogicThread;
import com.analysys.thread.PriorityCallable;
import com.analysys.utils.AnsReflectUtils;
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
public class ASMProbeHelp{

    private static class Holder {
        public static final ASMProbeHelp instance = new ASMProbeHelp();
    }

    private ASMProbeHelp() {
    }

    public static ASMProbeHelp getInstance() {
        return Holder.instance;
    }

    private Set<ASMHookAdapter> mObservers = new HashSet<>();

    public void registerHookObserver(ASMHookAdapter observer) {
        mObservers.add(observer);
    }


    // ------------------------ Fragment ---------------------------------------

    public void onFragmentViewCreated(final Object object, final View rootView, final Bundle savedInstanceState, final boolean hasTrackPvAnn) {
        AnsLogicThread.async(new PriorityCallable(AnsLogicThread.PriorityLevel.MIDDLE) {
            @Override
            public Object call() throws Exception {
                try {
                    for (ASMHookAdapter observer : mObservers) {
                        observer.onFragmentViewCreated(object, rootView, savedInstanceState, hasTrackPvAnn);
                    }
                } catch (Throwable ignore) {
                    ExceptionUtil.exceptionThrow(ignore);
                }
                return null;
            }
        });
    }

    public void trackFragmentResume(final Object object, final boolean hasTrackPvAnn) {
        final long currentTime = System.currentTimeMillis();
        AnsLogicThread.async(new PriorityCallable(AnsLogicThread.PriorityLevel.MIDDLE) {
            @Override
            public Object call() throws Exception {
                try {
                    for (ASMHookAdapter observer : mObservers) {
                        observer.trackFragmentResume(object, hasTrackPvAnn,currentTime);
                    }
                } catch (Throwable ignore) {
                    ExceptionUtil.exceptionThrow(ignore);
                }
                return null;
            }
        });
    }

    public void trackFragmentPause(final Object object, final boolean hasTrackPvAnn) {
        final long currentTime = System.currentTimeMillis();
        AnsLogicThread.async(new PriorityCallable(AnsLogicThread.PriorityLevel.MIDDLE) {
            @Override
            public Object call() throws Exception {
                try {
                    for (ASMHookAdapter observer : mObservers) {
                        observer.trackFragmentPause(object, hasTrackPvAnn,currentTime);
                    }
                } catch (Throwable ignore) {
                    ExceptionUtil.exceptionThrow(ignore);
                }
                return null;
            }
        });
    }

    public void trackFragmentSetUserVisibleHint(final Object object, final boolean isVisibleToUser, final boolean hasTrackPvAnn) {
        final long currentTime = System.currentTimeMillis();
        AnsLogicThread.async(new PriorityCallable(AnsLogicThread.PriorityLevel.MIDDLE) {
            @Override
            public Object call() throws Exception {
                try {
                    //        Log.d("javen",String.format("call trackFragmentSetUserVisibleHint in %s ",object.getClass().getSimpleName()));
                    for (ASMHookAdapter observer : mObservers) {
                        observer.trackFragmentSetUserVisibleHint(object, isVisibleToUser, hasTrackPvAnn,currentTime);
                    }
                } catch (Throwable ignore) {
                    ExceptionUtil.exceptionThrow(ignore);
                }
                return null;
            }
        });
    }

    public void trackOnHiddenChanged(final Object object, final boolean hidden, final boolean hasTrackPvAnn) {
        final long currentTime = System.currentTimeMillis();
        AnsLogicThread.async(new PriorityCallable(AnsLogicThread.PriorityLevel.MIDDLE) {
            @Override
            public Object call() throws Exception {
                try {
                    //        Log.d("javen",String.format("call trackOnHiddenChanged in %s ",object.getClass().getSimpleName()));
                    for (ASMHookAdapter observer : mObservers) {
                        observer.trackOnHiddenChanged(object, hidden, hasTrackPvAnn,currentTime);
                    }
                } catch (Throwable ignore) {
                    ExceptionUtil.exceptionThrow(ignore);
                }
                return null;
            }
        });
    }


    // ------------------------ Click ---------------------------------------

    public void trackDialog(final DialogInterface dialogInterface, final int which, final boolean hasTrackClickAnn) {
        final long currentTime = System.currentTimeMillis();
        AnsLogicThread.async(new PriorityCallable(AnsLogicThread.PriorityLevel.LOW) {
            @Override
            public Object call() throws Exception {
                try {
                    if (isInLimitTime(dialogInterface)) {
                        return null;
                    }
                    for (ASMHookAdapter observer : mObservers) {
                        observer.trackDialog(dialogInterface, which, hasTrackClickAnn,currentTime);
                    }
                } catch (Throwable ignore) {
                    ExceptionUtil.exceptionThrow(ignore);
                }
                return null;
            }
        });
    }

    public void trackTabLayout(final Object object, final Object tab, final boolean hasTrackClickAnn) {
        final long currentTime = System.currentTimeMillis();
        AnsLogicThread.async(new PriorityCallable(AnsLogicThread.PriorityLevel.LOW) {
            @Override
            public Object call() throws Exception {
                try {
                    if (isInLimitTime(tab)) {
                        return null;
                    }
                    for (ASMHookAdapter observer : mObservers) {
                        observer.trackTabLayout(object, tab, hasTrackClickAnn,currentTime);
                    }
                } catch (Throwable ignore) {
                    ExceptionUtil.exceptionThrow(ignore);
                }
                return null;
            }
        });
    }

    public void trackTabHost(final String tabName, final boolean hasTrackClickAnn) {
        final long currentTime = System.currentTimeMillis();
        AnsLogicThread.async(new PriorityCallable(AnsLogicThread.PriorityLevel.LOW) {
            @Override
            public Object call() throws Exception {
                try {
                    if (isInLimitTime(tabName)) {
                        return null;
                    }
                    for (ASMHookAdapter observer : mObservers) {
                        observer.trackTabHost(tabName, hasTrackClickAnn,currentTime);
                    }
                } catch (Throwable ignore) {
                    ExceptionUtil.exceptionThrow(ignore);
                }
                return null;
            }
        });
    }

    public void trackRadioGroup(final RadioGroup parent, final int checkedId, final boolean hasTrackClickAnn) {
        final long currentTime = System.currentTimeMillis();
        AnsLogicThread.async(new PriorityCallable(AnsLogicThread.PriorityLevel.LOW) {
            @Override
            public Object call() throws Exception {
                try {
                    View childView = null;
                    if (parent != null) {
                        childView = parent.findViewById(checkedId);
                    }
                    if (isInLimitTime(childView)) {
                        return null;
                    }
                    for (ASMHookAdapter observer : mObservers) {
                        observer.trackRadioGroup(parent, checkedId, hasTrackClickAnn,currentTime);
                    }
                } catch (Throwable ignore) {
                    ExceptionUtil.exceptionThrow(ignore);
                }
                return null;
            }
        });
    }

    public void trackExpListViewChildClick(final ExpandableListView parent, final View v, final int groupPosition, final int childPosition, final boolean hasTrackClickAnn) {
        final long currentTime = System.currentTimeMillis();
        AnsLogicThread.async(new PriorityCallable(AnsLogicThread.PriorityLevel.LOW) {
            @Override
            public Object call() throws Exception {
                try {
                    if (isInLimitTime(v)) {
                        return null;
                    }
                    for (ASMHookAdapter observer : mObservers) {
                        observer.trackExpListViewChildClick(parent, v, groupPosition, childPosition, hasTrackClickAnn,currentTime);
                    }
                } catch (Throwable ignore) {
                    ExceptionUtil.exceptionThrow(ignore);
                }
                return null;
            }
        });
    }

    public void trackExpListViewGroupClick(final ExpandableListView parent, final View v, final int groupPosition, final boolean hasTrackClickAnn) {
        final long currentTime = System.currentTimeMillis();
        AnsLogicThread.async(new PriorityCallable(AnsLogicThread.PriorityLevel.LOW) {
            @Override
            public Object call() throws Exception {
                try {
                    if (isInLimitTime(v)) {
                        return null;
                    }
                    for (ASMHookAdapter observer : mObservers) {
                        observer.trackExpListViewGroupClick(parent, v, groupPosition, hasTrackClickAnn,currentTime);
                    }
                } catch (Throwable ignore) {
                    ExceptionUtil.exceptionThrow(ignore);
                }
                return null;
            }
        });
    }

    public void trackListView(final AdapterView<?> parent, final View v, final int position, final boolean hasTrackClickAnn) {
        final long currentTime = System.currentTimeMillis();
        AnsLogicThread.async(new PriorityCallable(AnsLogicThread.PriorityLevel.LOW) {
            @Override
            public Object call() throws Exception {
                try {
                    if (isInLimitTime(v)) {
                        return null;
                    }
                    for (ASMHookAdapter observer : mObservers) {
                        observer.trackListView(parent, v, position, hasTrackClickAnn,currentTime);
                    }
                } catch (Throwable ignore) {
                    ExceptionUtil.exceptionThrow(ignore);
                }
                return null;
            }
        });
    }

    public void trackViewOnClick(final View v, final boolean hasTrackClickAnn) {
        try {
//            final int viewIdx = PathGeneral.getInstance().getIndex(v);
//        final String path = PathGeneral.getInstance().general(v, viewIdx);

            final long currentTime = System.currentTimeMillis();
            AnsLogicThread.async(new PriorityCallable(AnsLogicThread.PriorityLevel.LOW) {
                @Override
                public Object call() throws Exception {
                    try {
                        if (isInLimitTime(v)) {
                            return null;
                        }
                        for (ASMHookAdapter observer : mObservers) {
                            observer.trackViewOnClick(v, hasTrackClickAnn,currentTime);
                        }
                    } catch (Throwable ignore) {
                        ExceptionUtil.exceptionThrow(ignore);
                    }
                    return null;
                }
            });
        }catch (Throwable ignore){
            ExceptionUtil.exceptionThrow(ignore);
        }
    }

    public void trackSendAccessibilityEvent(final View v, final int eventType, final boolean hasTrackClickAnn) {
        try {
//            final int viewIdx = PathGeneral.getInstance().getIndex(v);
//        final String path = PathGeneral.getInstance().general(v, viewIdx);

            AnsLogicThread.async(new PriorityCallable(AnsLogicThread.PriorityLevel.LOW) {
                @Override
                public Object call() throws Exception {
                    try {
                        if (isInLimitTime(v)) {
                            return null;
                        }
                        for (ASMHookAdapter observer : mObservers) {
                            observer.trackSendAccessibilityEvent(v, eventType, hasTrackClickAnn);
                        }
                    } catch (Throwable ignore) {
                        ExceptionUtil.exceptionThrow(ignore);
                    }
                    return null;
                }
            });
        }catch (Throwable ignore){
            ExceptionUtil.exceptionThrow(ignore);
        }
    }

    public void trackMenuItem(final Object obj, final MenuItem menuItem, final boolean hasTrackClickAnn) {
        final long currentTime = System.currentTimeMillis();
        AnsLogicThread.async(new PriorityCallable(AnsLogicThread.PriorityLevel.LOW) {
            @Override
            public Object call() throws Exception {
                try {
                    if (isInLimitTime(menuItem)) {
                        return null;
                    }
                    for (ASMHookAdapter observer : mObservers) {
                        observer.trackMenuItem(obj, menuItem, hasTrackClickAnn,currentTime);
                    }
                } catch (Throwable ignore) {
                    ExceptionUtil.exceptionThrow(ignore);
                }
                return null;
            }
        });
    }

    public void trackMenuItem(MenuItem menuItem, boolean hasTrackClickAnn) {

    }

    public void trackDrawerSwitch(final View drawerLayout, final boolean isOpen, final boolean hasTrackClickAnn) {
        final long currentTime = System.currentTimeMillis();
        AnsLogicThread.async(new PriorityCallable(AnsLogicThread.PriorityLevel.LOW) {
            @Override
            public Object call() throws Exception {
                try {
                    if (isInLimitTime(drawerLayout)) {
                        return null;
                    }

                    for (ASMHookAdapter observer : mObservers) {
                        observer.trackDrawerSwitch(drawerLayout, isOpen, hasTrackClickAnn,currentTime);
                    }
                } catch (Throwable ignore) {
                    ExceptionUtil.exceptionThrow(ignore);
                }
                return null;
            }
        });
    }

    public void maybeClickInXML(final View v, final boolean methodName, final boolean hasTrackClickAnn) {
        try {
            final long currentTime = System.currentTimeMillis();
            AnsLogicThread.async(new PriorityCallable(AnsLogicThread.PriorityLevel.LOW) {
                @Override
                public Object call() throws Exception {
                    try {
                        if (isInLimitTime(v)) {
                            return null;
                        }
                        Object listenerInfo = AnsReflectUtils.getField(v, "mListenerInfo");
                        if (listenerInfo == null) {
                            return null;
                        }
                        Object onClickListener = AnsReflectUtils.getField(listenerInfo, "mOnClickListener");
                        if (onClickListener == null) {
                            return null;
                        }
                        Object mtdName = AnsReflectUtils.getField(onClickListener, "mMethodName");
                        if (mtdName == null || !mtdName.equals(methodName)) {
                            return null;
                        }
                        for (ASMHookAdapter observer : mObservers) {
                            observer.trackViewOnClick(v, hasTrackClickAnn,currentTime);
                        }
                    } catch (Throwable ignore) {
                        ExceptionUtil.exceptionThrow(ignore);
                    }
                    return null;
                }
            });
        }catch (Throwable ignore){
            ExceptionUtil.exceptionThrow(ignore);
        }

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
