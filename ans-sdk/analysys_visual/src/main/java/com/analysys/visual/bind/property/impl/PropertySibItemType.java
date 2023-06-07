package com.analysys.visual.bind.property.impl;

import android.view.View;

import com.analysys.visual.utils.ReflectUnit;

/**
 * @Copyright © 2019 Analysys Inc. All rights reserved.
 * @Description: 同级事件容器，如ListView的ItemViewType，在事件触发时设置，TODO 不能在绑定时指定
 * @Create: 2019-11-28 10:26
 * @author: hcq
 */
public class PropertySibItemType extends BaseProperty {

    private int mSibItemType = -1;

    public PropertySibItemType(String name, String key, String propType, ReflectUnit reflectUnit, Object matchValue, String regex) {
        super(name, key, propType, reflectUnit, matchValue, regex);
    }

    @Override
    protected Object getProperty(View view) {
        return mSibItemType;
    }

    public void setSibItemType(int sibItemType) {
        mSibItemType = sibItemType;
    }

}
