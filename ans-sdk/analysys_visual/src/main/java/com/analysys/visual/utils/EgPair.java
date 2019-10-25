package com.analysys.visual.utils;

import android.os.Build;
import android.util.Pair;


/**
 * Extends {@link Pair} class to be backwards compatible with old Android versions.
 * Before Jelly Bean, {@link Pair#hashCode()} and {@link Pair#equals(Object)} methods assume the
 * first and second objects are never null.
 */
public class EgPair<F, S> extends Pair<F, S> {

    public EgPair(F first, S second) {
        super(first, second);
    }

    @Override
    public boolean equals(Object o) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
            return super.equals(o);
        }

        if (!(o instanceof Pair)) {
            return false;
        }
        Pair<?, ?> p = (Pair<?, ?>) o;
        return ((p.first == first) || (p.first != null && p.first.equals(first))) && ((p.second == second) || (p.second != null && p.second.equals(second)));
    }

    @Override
    public int hashCode() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
            return super.hashCode();
        }
        return (first == null ? 0 : first.hashCode()) ^ (second == null ? 0 : second.hashCode());
    }
}
