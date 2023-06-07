package com.analysys.visual.bind.property.impl;

import android.view.View;

import com.analysys.process.SystemIds;
import com.analysys.visual.utils.ReflectUnit;

/**
 * @Copyright © 2019 Analysys Inc. All rights reserved.
 * @Description: id名称属性类
 * @Create: 2019-11-28 10:26
 * @author: hcq
 */
public class PropertyIdName extends BaseProperty {


    public PropertyIdName(String name, String key, String propType, ReflectUnit reflectUnit, Object matchValue, String regex) {
        super(name, key, propType, reflectUnit, matchValue, regex);
    }

    @Override
    protected Object getProperty(View view) {
        int id = view.getId();
        return SystemIds.getInstance().nameFromId(view.getResources(), id);
    }

}
