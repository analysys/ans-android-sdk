package com.analysys.visual.viewcrawler;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewTreeObserver;

import com.analysys.visual.utils.UIHelper;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Handles applying and managing the life cycle of edits in an application. Clients
 * can replace all of the edits in an app with {@link EditState#setEdits(Map)}.
 * <p>
 * Some client is responsible for informing the EditState about the presence or absence
 * of Activities, by calling {@link EditState#add(Activity)} and {@link EditState#remove(Activity)}
 */
class EditState extends UIThreadSet<Activity> {

    private final Handler mUiThreadHandler;
    private final Map<String, List<BaseViewVisitor>> mIntendedEdits;
    private final Set<EditBinding> mCurrentEdits;

    public EditState() {
        mUiThreadHandler = new Handler(Looper.getMainLooper());
        mIntendedEdits = new HashMap<String, List<BaseViewVisitor>>();
        mCurrentEdits = new HashSet<EditBinding>();
    }

    /**
     * Should be called whenever a new Activity appears in the application.
     */
    @Override
    public void add(Activity newOne) {
        super.add(newOne);
        applyEditsOnUiThread();
    }

    /**
     * Should be called whenever an activity leaves the application, or is otherwise no longer
     * relevant to our edits.
     */
    @Override
    public void remove(Activity oldOne) {
        super.remove(oldOne);
    }

    /**
     * Sets the entire set of edits to be applied to the application.
     * <p>
     * Edits are represented by ViewVisitors, batched in a map by the String name of the activity
     * they should be applied to. Edits to apply to all views should be in a list associated with
     * the key {@code null} (Not the string "null", the actual null value!)
     * <p>
     * The given edits will completely replace any existing edits.
     * <p>
     * setEdits can be called from any thread, although the changes will occur (eventually) on the
     * UI thread of the application, and may not appear immediately.
     *
     * @param newEdits A Map from activity name to a list of edits to apply
     */
    // Must be thread-safe
    public void setEdits(Map<String, List<BaseViewVisitor>> newEdits) {

        synchronized (mCurrentEdits) {
            for (final EditBinding stale : mCurrentEdits) {
                stale.kill();
            }
            mCurrentEdits.clear();
        }

        synchronized (mIntendedEdits) {
            mIntendedEdits.clear();
            mIntendedEdits.putAll(newEdits);
        }

        applyEditsOnUiThread();
    }

    private void applyEditsOnUiThread() {
        if (Thread.currentThread() == mUiThreadHandler.getLooper().getThread()) {
            applyIntendedEdits();
        } else {
            mUiThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    applyIntendedEdits();
                }
            });
        }
    }

    // Must be called on UI Thread
    private void applyIntendedEdits() {
        for (final Activity activity : getAll()) {
            final String activityName = activity.getClass().getCanonicalName();
            Map<String, View> rootViews = new HashMap<>();

            List<ViewSnapshot.RootViewInfo> listDlgView = UIHelper.getActivityDialogs(activity);
            if (listDlgView == null || listDlgView.isEmpty()) {
                final View rootView = activity.getWindow().getDecorView().getRootView();
                rootViews.put(activityName, rootView);
            } else {
                for (ViewSnapshot.RootViewInfo info : listDlgView) {
                    rootViews.put(info.activityName, info.rootView);
                }
            }

            for (String pageName : rootViews.keySet()) {
                final List<BaseViewVisitor> specificChanges;
                final List<BaseViewVisitor> wildcardChanges;
                synchronized (mIntendedEdits) {
                    specificChanges = mIntendedEdits.get(pageName);
                    wildcardChanges = mIntendedEdits.get(null);
                }

                View rootView = rootViews.get(pageName);
                if (null != specificChanges) {
                    applyChangesFromList(rootView, specificChanges);
                }

                if (null != wildcardChanges) {
                    applyChangesFromList(rootView, wildcardChanges);
                }
            }
        }
    }

    // Must be called on UI Thread
    private void applyChangesFromList(View rootView, List<BaseViewVisitor> changes) {
        synchronized (mCurrentEdits) {
            final int size = changes.size();
            for (int i = 0; i < size; i++) {
                final BaseViewVisitor visitor = changes.get(i);
                final EditBinding binding = new EditBinding(rootView, visitor, mUiThreadHandler);
                mCurrentEdits.add(binding);
            }
        }
    }

    /* The binding between a bunch of edits and a view. Should be instantiated and live on the UI
     thread */
    private static class EditBinding implements ViewTreeObserver.OnGlobalLayoutListener, Runnable {
        private final WeakReference<View> mViewRoot;
        private final BaseViewVisitor mEdit;
        private final Handler mHandler;
        private volatile boolean mDying;
        private boolean mAlive;

        public EditBinding(View viewRoot, BaseViewVisitor edit, Handler uiThreadHandler) {
            mEdit = edit;
            mViewRoot = new WeakReference<View>(viewRoot);
            mHandler = uiThreadHandler;
            mAlive = true;
            mDying = false;

            final ViewTreeObserver observer = viewRoot.getViewTreeObserver();
            if (observer.isAlive()) {
                observer.addOnGlobalLayoutListener(this);
            }
            run();
        }

        @Override
        public void onGlobalLayout() {
            run();
        }

        @Override
        public void run() {
            if (!mAlive) {
                return;
            }

            final View viewRoot = mViewRoot.get();
            if (null == viewRoot || mDying) {
                cleanUp();
                return;
            }
            // ELSE View is alive and we are alive

            mEdit.visit(viewRoot);              //此处理回最终的事件回调
            mHandler.removeCallbacks(this);
            mHandler.postDelayed(this, 1000);
        }

        public void kill() {
            mDying = true;
            mHandler.post(this);
        }

        private void cleanUp() {
            if (mAlive) {
                final View viewRoot = mViewRoot.get();
                if (null != viewRoot) {
                    final ViewTreeObserver observer = viewRoot.getViewTreeObserver();
                    if (observer.isAlive()) {
                        observer.removeGlobalOnLayoutListener(this); // Deprecated Name
                    }
                }
                mEdit.cleanup();
            }
            mAlive = false;
        }
    }

}
