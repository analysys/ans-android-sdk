package com.analysys.visual.viewcrawler;

import android.content.res.Resources;

/**
 * This interface is for internal use in the library, and should not be included in
 * client code.
 */
public interface ResourceIds {
//    public boolean knownIdName(String name);

    int idFromName(Resources res, String name);

    String nameForId(Resources res, int id);
}
