package com.analysys.visual.viewcrawler;

import android.annotation.TargetApi;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;

import com.analysys.visual.utils.UIHelper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

@TargetApi(16)
abstract class BaseViewVisitor implements Pathfinder.Accumulator {

    private static final String TAG = "BaseViewVisitor";
    private final List<Pathfinder.PathElement> mPath;
    private final Pathfinder mPathfinder;

    protected BaseViewVisitor(List<Pathfinder.PathElement> path) {
        mPath = path;
        mPathfinder = new Pathfinder();
    }

    /**
     * Scans the View hierarchy below rootView, applying it's operation to each matching child view.
     */
    public void visit(View rootView) {
        mPathfinder.findTargetsInRoot(rootView, mPath, this);
//        for (Pathfinder.PathElement element: mPath) {
//            Log.e(TAG,"viewClassName=" + element.viewClassName + "\t" + "index=" + element
//            .index + "\t" + "viewId=" + element.viewId + "\t" + "prefix=" + element.prefix);
//        }
    }

    /**
     * Removes listeners and frees resources associated with the visitor. Once cleanup is called,
     * the BaseViewVisitor should not be used again.
     */
    public abstract void cleanup();

    protected List<Pathfinder.PathElement> getPath() {
        return mPath;
    }

    protected Pathfinder getPathfinder() {
        return mPathfinder;
    }

    protected abstract String name();

    /**
     * onEvent will be fired when whatever the BaseViewVisitor installed fires
     * (For example, if the BaseViewVisitor installs watches for clicks, then onEvent will be called
     * on click)
     */
    public interface OnEventListener {
        void onEvent(View host, String eventName, String previousText, String match_text,
                     boolean deBounce);
    }

    /**
     * Adds an accessibility event, which will fire onEvent, to every matching view.
     */
    public static class AddAccessibilityEventVisitor extends BaseEventTriggeringVisitor {
        private final int mEventType;
        private final WeakHashMap<View, TrackingAccessibilityDelegate> mWatching;

        public AddAccessibilityEventVisitor(List<Pathfinder.PathElement> path,
                                            int accessibilityEventType, String eventName,
                                            String matchText, OnEventListener listener) {
            super(path, eventName, matchText, listener, false);
            mEventType = accessibilityEventType;
            mWatching = new WeakHashMap<View, TrackingAccessibilityDelegate>();
        }

        @Override
        public void cleanup() {
            for (final Map.Entry<View, TrackingAccessibilityDelegate> entry :
                    mWatching.entrySet()) {
                final View v = entry.getKey();
                final TrackingAccessibilityDelegate toCleanup = entry.getValue();
                final View.AccessibilityDelegate currentViewDelegate = getOldDelegate(v);
                if (currentViewDelegate == toCleanup) {
                    v.setAccessibilityDelegate(toCleanup.getRealDelegate());
                } else if (currentViewDelegate instanceof TrackingAccessibilityDelegate) {
                    final TrackingAccessibilityDelegate newChain =
                            (TrackingAccessibilityDelegate) currentViewDelegate;
                    newChain.removeFromDelegateChain(toCleanup);
                } else {
                    // Assume we've been replaced, zeroed out, or for some other reason we're
                    // already gone.
                    // (This isn't too weird, for example, it's expected when views get recycled)
                }
            }
            mWatching.clear();
        }

        @Override
        public void accumulate(View found) {
            final View.AccessibilityDelegate realDelegate = getOldDelegate(found);
            if (realDelegate instanceof TrackingAccessibilityDelegate) {
                final TrackingAccessibilityDelegate currentTracker =
                        (TrackingAccessibilityDelegate) realDelegate;
                if (currentTracker.willFireEvent(UIHelper.textPropertyFromView(found),
                        getEventName())) {
                    return; // Don't double track
                }
            }

            // We aren't already in the tracking call chain of the view
            final TrackingAccessibilityDelegate newDelegate =
                    new TrackingAccessibilityDelegate(realDelegate);
            found.setAccessibilityDelegate(newDelegate);
            mWatching.put(found, newDelegate);
        }

        @Override
        protected String name() {
            return getEventName() + " event when (" + mEventType + ")";
        }

        private View.AccessibilityDelegate getOldDelegate(View v) {
            View.AccessibilityDelegate ret = null;
            try {
                Class<?> klass = v.getClass();
                Method m = klass.getMethod("getAccessibilityDelegate");
                ret = (View.AccessibilityDelegate) m.invoke(v);
            } catch (NoSuchMethodException e) {
                // In this case, we just overwrite the original.
            } catch (IllegalAccessException e) {
                // In this case, we just overwrite the original.
            } catch (InvocationTargetException e) {
                //InternalAgent.w(TAG, "getAccessibilityDelegate threw an exception when called.", e);
            }

            return ret;
        }

        private class TrackingAccessibilityDelegate extends View.AccessibilityDelegate {
            private View.AccessibilityDelegate mRealDelegate;
            private String mPreviousText;

            public TrackingAccessibilityDelegate(View.AccessibilityDelegate realDelegate) {
                mRealDelegate = realDelegate;
            }

            public View.AccessibilityDelegate getRealDelegate() {
                return mRealDelegate;
            }

            public boolean willFireEvent(final String text, final String eventName) {
                mPreviousText = text;
                if (getEventName() == eventName) {
                    return true;
                } else if (mRealDelegate instanceof TrackingAccessibilityDelegate) {
                    return ((TrackingAccessibilityDelegate) mRealDelegate).willFireEvent(text,
                            eventName);
                } else {
                    return false;
                }
            }

            public void removeFromDelegateChain(final TrackingAccessibilityDelegate other) {
                if (mRealDelegate == other) {
                    mRealDelegate = other.getRealDelegate();
                } else if (mRealDelegate instanceof TrackingAccessibilityDelegate) {
                    final TrackingAccessibilityDelegate child =
                            (TrackingAccessibilityDelegate) mRealDelegate;
                    child.removeFromDelegateChain(other);
                } else {
                    // We can't see any further down the chain, just return.
                }
            }

            @Override
            public void sendAccessibilityEvent(View host, int eventType) {

                if (eventType == mEventType) {
                    fireEvent(mPreviousText, host);
                }

                if (null != mRealDelegate) {
                    mRealDelegate.sendAccessibilityEvent(host, eventType);
                }
            }
        }
    }

    /**
     * Installs a TextWatcher in each matching view. Does nothing if matching views are not
     * TextViews.
     */
    public static class AddTextChangeListener extends BaseEventTriggeringVisitor {
        private final Map<TextView, TextWatcher> mWatching;

        public AddTextChangeListener(List<Pathfinder.PathElement> path, String eventName,
                                     String matchText, OnEventListener listener) {
            super(path, eventName, matchText, listener, true);
            mWatching = new HashMap<TextView, TextWatcher>();
        }

        @Override
        public void cleanup() {
            for (final Map.Entry<TextView, TextWatcher> entry : mWatching.entrySet()) {
                final TextView v = entry.getKey();
                final TextWatcher watcher = entry.getValue();
                v.removeTextChangedListener(watcher);
            }

            mWatching.clear();
        }

        @Override
        public void accumulate(View found) {
            if (found instanceof TextView) {
                final TextView foundTextView = (TextView) found;
                final TextWatcher watcher = new TrackingTextWatcher(foundTextView);
                final TextWatcher oldWatcher = mWatching.get(foundTextView);
                if (null != oldWatcher) {
                    foundTextView.removeTextChangedListener(oldWatcher);
                }
                foundTextView.addTextChangedListener(watcher);
                mWatching.put(foundTextView, watcher);
            }
        }

        @Override
        protected String name() {
            return getEventName() + " on Text Change";
        }

        private class TrackingTextWatcher implements TextWatcher {
            private final View mBoundTo;
            private final String mPreviousText;

            public TrackingTextWatcher(View boundTo) {
                mBoundTo = boundTo;
                mPreviousText = UIHelper.textPropertyFromView(boundTo);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                ; // Nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ; // Nothing
            }

            @Override
            public void afterTextChanged(Editable s) {
                fireEvent(mPreviousText, mBoundTo);
            }
        }
    }

    /**
     * Monitors the view tree for the appearance of matching views where there were not
     * matching views before. Fires only once per traversal.
     */
    public static class ViewDetectorVisitor extends BaseEventTriggeringVisitor {
        private boolean mSeen;

        public ViewDetectorVisitor(List<Pathfinder.PathElement> path, String eventName,
                                   String matchText, OnEventListener listener) {
            super(path, eventName, matchText, listener, false);
            mSeen = false;
        }

        @Override
        public void cleanup() {
            ; // Do nothing, we don't have anything to leak :)
        }

        @Override
        public void accumulate(View found) {

            if (found != null && !mSeen) {
//                fireEvent(found);
                fireEvent("", found);
            }
            mSeen = (found != null);
        }

        @Override
        protected String name() {
            return getEventName() + " when Detected";
        }
    }

    private static abstract class BaseEventTriggeringVisitor extends BaseViewVisitor {
        private final OnEventListener mListener;

//        protected void fireEvent(View found) {
//            fireEvent("", found);
//        }
        private final String mEventName;
        private final boolean mDebounce;
        private final String mMatchText;
        public BaseEventTriggeringVisitor(List<Pathfinder.PathElement> path, String eventName,
                                          String matchText, OnEventListener listener,
                                          boolean debounce) {
            super(path);
            mMatchText = matchText;
            mListener = listener;
            mEventName = eventName;
            mDebounce = debounce;
        }

        protected void fireEvent(String previousText, View found) {
            mListener.onEvent(found, mEventName, previousText, mMatchText, mDebounce);
        }

        protected String getEventName() {
            return mEventName;
        }
    }
}
