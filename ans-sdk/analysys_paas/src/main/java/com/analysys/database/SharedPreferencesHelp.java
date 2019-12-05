package com.analysys.database;

import android.content.Context;
import android.content.SharedPreferences;

class SharedPreferencesHelp {

    private static final String FILE_NAME = "fz.d";
    private static SharedPreferences.Editor mEditor = null;
    private static SharedPreferences mPreferences = null;

    public SharedPreferencesHelp() {

    }

    public SharedPreferences getPreferences(Context context) {
//        if (mPreferences == null && context != null) {
//            mPreferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
//        } else {
//            mPreferences = null;
//        }

        if (mPreferences == null) {
            if (context != null) {
                mPreferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
            }
        }



        return mPreferences;
    }

    public SharedPreferences.Editor getEditor(Context context) {

        getPreferences(context);
        if (mPreferences != null) {
            mEditor = mPreferences.edit();
        }

//        if (mEditor == null && context != null) {
//            if (mPreferences != null) {
//                mEditor = mPreferences.edit();
//            } else {
//                mPreferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
//                if (mPreferences != null) {
//                    mEditor = mPreferences.edit();
//                } else {
//                    mEditor = null;
//                }
//            }
//        } else {
//            mEditor = null;
//        }
        return mEditor;
    }

}
