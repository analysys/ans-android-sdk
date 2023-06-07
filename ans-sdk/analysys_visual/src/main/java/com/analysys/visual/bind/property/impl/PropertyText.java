package com.analysys.visual.bind.property.impl;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.analysys.visual.utils.ReflectUnit;

/**
 * @Copyright © 2019 Analysys Inc. All rights reserved.
 * @Description: 文本属性类 TODO 需要解决容器类匹配问题，效率问题
 * @Create: 2019-11-28 10:26
 * @author: hcq
 */
public class PropertyText extends BaseProperty {

    private static final int MAX_PROPERTY_LENGTH = 128;

    public PropertyText(String name, String key, String propType, ReflectUnit reflectUnit, Object matchValue, String regex) {
        super(name, key, propType, reflectUnit, matchValue, regex);
    }

    @Override
    protected Object getProperty(View view) {
        if (view instanceof TextView) {
            return getTextFromTextView((TextView) view);
        } else if (view instanceof ImageView) {
            return getTextFromImageView(view);
        } else {
//            return getTextFromView(view); // TODO 容器文本处理
            return null;
        }
    }

    private Object getTextFromImageView(View view) {
        CharSequence retSequence = view.getContentDescription();
        if (null != retSequence) {
            return retSequence.toString();
        }
        return null;
    }

    private String getTextFromTextView(TextView textView) {
        CharSequence retSequence
                = textView.getText();
        if (null != retSequence) {
            return retSequence.toString();
        }
        return null;
    }

    /**
     * 获取view的字符串，如果是容器类，遍历子类，找到所有的TextView元素，将这些元素的字符合并
     */
    private String getTextFromView(View v) {
        String ret = null;

        if (v instanceof TextView) {
            ret = getTextFromTextView((TextView) v);
        } else if (v instanceof ViewGroup) {
            final StringBuilder builder = new StringBuilder();
            final ViewGroup vGroup = (ViewGroup) v;
            final int childCount = vGroup.getChildCount();
            boolean textSeen = false;
            for (int i = 0; i < childCount && builder.length() < MAX_PROPERTY_LENGTH; i++) {
                final View child = vGroup.getChildAt(i);
                if (child == null) {
                    break;
                }
                final String childText = getTextFromView(child);
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
