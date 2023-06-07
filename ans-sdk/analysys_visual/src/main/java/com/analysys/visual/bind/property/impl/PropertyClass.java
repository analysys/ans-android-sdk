package com.analysys.visual.bind.property.impl;

import android.view.View;

import com.analysys.utils.ExceptionUtil;
import com.analysys.visual.utils.ReflectUnit;

/**
 * @Copyright © 2019 Analysys Inc. All rights reserved.
 * @Description: 类名属性
 * @Create: 2019-11-28 10:23
 * @author: hcq
 */
public class PropertyClass extends BaseProperty {

    public PropertyClass(String name, String key, String propType, ReflectUnit reflectUnit, Object matchValue, String regex) {
        super(name, key, propType, reflectUnit, matchValue, regex);
    }

    @Override
    protected Object getProperty(View view) {
        return view.getClass().getName();
    }

    @Override
    public boolean isMatch(Object obj) {
        Object bindV = getMatchValue();
        if (!(bindV instanceof String)) {
            return false;
        }
        String matchClsName = (String) bindV;

        // 前缀为>表示匹配所有子类
        if (!matchClsName.startsWith(">")) {
            return super.isMatch(obj);
        }
        if (obj == null) {
            return false;
        }
        Class<?> objCls = obj.getClass();
        try {
            Class<?> matchCls = Class.forName(matchClsName.substring(1));
            return matchCls.isAssignableFrom(objCls);
        } catch (Throwable ignore) {
            ExceptionUtil.exceptionThrow(ignore);
        }
        return false;
    }
}
