package com.analysys.database;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * @Copyright © 2019 EGuan Inc. All rights reserved.
 * @Description: SharedPreference操作
 * @Version: 1.0
 * @Create: 2019/11/25 17:31
 * @Author: WP
 */
class SharedPreferencesHelp {

    private static final String FILE_NAME = "fz.d";
    private static SharedPreferences.Editor mEditor = null;
    private static SharedPreferences mPreferences = null;

    SharedPreferencesHelp() {

    }

    SharedPreferences getPreferences(Context context) {

        if (mPreferences == null) {
            if (context != null) {
                mPreferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
            }
        }


        return mPreferences;
    }

    SharedPreferences.Editor getEditor(Context context) {

        getPreferences(context);
        if (mPreferences != null) {
            mEditor = mPreferences.edit();
        }

        return mEditor;
    }

}
