package com.analysys.visual.utils;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * @Copyright © 2018 Eguan Inc. All rights reserved.
 * @Description: 界面工具类。取字的工具类
 * @Version: 1.0
 * @Create: 2018年7月23日 下午7:33:28
 * @Author: sanbo
 */
public class UIHelper {
    private static final int MAX_PROPERTY_LENGTH = 128;

    /**
     * Recursively scans a view and it's children, looking for user-visible text to
     * provide as an event property.
     */
    public static String textPropertyFromView(View v) {
        String ret = null;

        if (v instanceof TextView) {
            final TextView textV = (TextView) v;
            final CharSequence retSequence = textV.getText();
            if (null != retSequence) {
                ret = retSequence.toString();
            }
        } else if (v instanceof ViewGroup) {
            final StringBuilder builder = new StringBuilder();
            final ViewGroup vGroup = (ViewGroup) v;
            final int childCount = vGroup.getChildCount();
            boolean textSeen = false;
            for (int i = 0; i < childCount && builder.length() < MAX_PROPERTY_LENGTH; i++) {
                final View child = vGroup.getChildAt(i);
                final String childText = textPropertyFromView(child);
                if (null != childText && childText.length() > 0) {
                    if (textSeen) {
                        builder.append(", ");
                    }
                    builder.append(childText);
                    textSeen = true;
                }
            }

            if (builder.length() > MAX_PROPERTY_LENGTH) {
                ret = builder.substring(0, MAX_PROPERTY_LENGTH);
            } else if (textSeen) {
                ret = builder.toString();
            }
        }

        return ret;
    }
}
