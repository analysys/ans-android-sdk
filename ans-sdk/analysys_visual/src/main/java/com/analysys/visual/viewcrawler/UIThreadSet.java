package com.analysys.visual.viewcrawler;


import android.os.Looper;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Wrapper around a set that will throw RuntimeErrors if accessed in a thread that is not the
 * main thread.
 */
class UIThreadSet<T> {
    private Set<T> mSet;

    public UIThreadSet() {
        mSet = new HashSet<T>();
    }

    public synchronized void add(T item) {
        if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
            throw new RuntimeException("Can't add an activity when not on the UI thread");
        }
        mSet.add(item);
    }

    public synchronized void remove(T item) {
        if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
            throw new RuntimeException("Can't remove an activity when not on the UI thread");
        }
        mSet.remove(item);
    }

    public synchronized Set<T> getAll() {
        if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
            throw new RuntimeException("Can't remove an activity when not on the UI thread");
        }
        return Collections.unmodifiableSet(mSet);
    }

    /**
     * 获取activity拷贝，防止多线程场景下异常
     */
    public synchronized Set<T> getAllCopy() {
        Set<T> newSet = new HashSet<>();
        newSet.addAll(mSet);
        return newSet;
    }

    public boolean isEmpty() {
        if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
            throw new RuntimeException("Can't check isEmpty() when not on the UI thread");
        }
        return mSet.isEmpty();
    }
}
