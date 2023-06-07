package com.analysys.visual.bind.property.impl;

import android.text.TextUtils;
import android.view.View;

import com.analysys.utils.ExceptionUtil;
import com.analysys.visual.bind.property.IProperty;
import com.analysys.visual.utils.ReflectUnit;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Copyright © 2019 Analysys Inc. All rights reserved.
 * @Description: 属性基类
 * @Create: 2019-11-28 10:10
 * @author: hcq
 */
public class BaseProperty implements IProperty {

    private final String name;

    /**
     * 上报埋点时的key
     */
    public final String key;

    /**
     * 值类型
     */
    private final String mPropType;

    /**
     * 某些属性使用预定方法可能拿不到值，加入放射方法增加灵活性
     */
    private final ReflectUnit mReflect;

    /**
     * 属性期望值，用于定位或者条件判断
     */
    private final Object mMatchValue;

    private final String mRegex;

    public BaseProperty(String name, String key, String propType, ReflectUnit reflectUnit, Object matchValue, String regex) {
        this.name = name;
        this.key = key;
        mPropType = propType;
        mReflect = reflectUnit;
        mMatchValue = matchValue;
        mRegex = regex;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object getValue(Object obj) {
        if (!TextUtils.isEmpty(key) && mMatchValue != null) {
            return getTypeValue(mMatchValue);
        }
        if (!(obj instanceof View) || TextUtils.isEmpty(name)) {
            return null;
        }
        View view = (View) obj;

        Object value;
        if (mReflect == null) {
            value = getTypeValue(getProperty(view));
        } else {
            value = getTypeValue(mReflect.call(view));
        }
        return value;
    }

    public Object getTypeValue(Object value) {
        if (value == null) {
            return null;
        }
        if (!TextUtils.isEmpty(mRegex) && value instanceof String) {
            try {
                Pattern p = Pattern.compile(mRegex);
                Matcher m = p.matcher(value.toString());
                if (m.find()) {
                    value = m.group();
                }
            } catch (Throwable ignore) {
                ExceptionUtil.exceptionPrint(ignore);
            }
        }
        String str = value.toString();
        switch (mPropType) {
            case "string":
                if (str.length() > 1000) {
                    return str.substring(0, 1000);
                } else {
                    return str;
                }
            case "number":
                try {
                    return Double.parseDouble(str);
                } catch (Throwable ignore) {
                    ExceptionUtil.exceptionPrint(ignore);
                }
                break;
            case "bool":
                try {
                    return Boolean.parseBoolean(str);
                } catch (Throwable ignore) {
                    ExceptionUtil.exceptionPrint(ignore);
                }
                break;
            default:
                break;
        }
        return str;
    }

    @Override
    public Object getMatchValue() {
        return mMatchValue;
    }

    @Override
    public boolean isMatch(Object obj) {
        Object v = getValue(obj);
        Object bindV = getMatchValue();
        if (v == null || bindV == null) {
            return false;
        }
        try {
            // 字符串类型使用通配符 TODO 第一期暂不实现
            if (v instanceof String && bindV instanceof CharSequence) {
//                return Pattern.matches((String) bindV, (CharSequence) v);
                return v.equals(bindV);
            } else if ("number".equals(mPropType)) {
                return (double)v == Double.parseDouble(bindV.toString());
            }
        } catch (Throwable ignore) {
            ExceptionUtil.exceptionPrint(ignore);
            return false;
        }
        return v == bindV;
    }

    Object getProperty(View view) {
        return null;
    }
}
