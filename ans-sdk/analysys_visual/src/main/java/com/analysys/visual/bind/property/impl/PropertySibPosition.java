package com.analysys.visual.bind.property.impl;

import android.view.View;

import com.analysys.visual.utils.ReflectUnit;

/**
 * @Copyright © 2019 Analysys Inc. All rights reserved.
 * @Description: 同级事件中用到，表示事件触发元素在同级容器中的位置，在事件触发时设置，TODO 不能在绑定时指定
 * @Create: 2019-11-28 10:25
 * @author: hcq
 */
public class PropertySibPosition extends BaseProperty {

    private int mEventPosition = -1;

    public PropertySibPosition(String name, String key, String propType, ReflectUnit reflectUnit, Object matchValue, String regex) {
        super(name, key, propType, reflectUnit, matchValue, regex);
    }

    @Override
    protected Object getProperty(View view) {
        return mEventPosition;
    }

    public void setEventPosition(int position) {
        mEventPosition = position;
    }
}
