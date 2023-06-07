package com.analysys.visual.bind.property;

import com.analysys.visual.bind.property.impl.BaseProperty;
import com.analysys.visual.bind.property.impl.PropertyClass;
import com.analysys.visual.bind.property.impl.PropertyText;
import com.analysys.visual.utils.ReflectUnit;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @Copyright © 2019 Analysys Inc. All rights reserved.
 * @Description: 将json配置转化为属性对象
 * @Create: 2019-11-28 10:35
 * @author: hcq
 */
public class PropertyFactory {

    public static final String PROPERTY_TEXT = "text";
    private static final String PROPERTY_CLASS_NAME = "class";
    public static final String PROPERTY_SIB_POSITION = "sib_position";
//    private static final String PROPERTY_ID_NAME = "id_name";
//    private static final String PROPERTY_BG_COLOR = "bg_color";
//    /**
//     * 同级事件容器，如ListView的viewItemType
//     */
//    private static final String PROPERTY_SIB_ITEM_TYPE = "sib_item_type";

    public static BaseProperty createProperty(JSONObject joProperty) throws JSONException, ClassNotFoundException {
        String name = joProperty.optString("prop_name");
        String key = joProperty.optString("key", null);
        String propType = joProperty.optString("prop_type", null);
        ReflectUnit reflectUnit = null;
        if (joProperty.has("reflect_name")) {
            reflectUnit = new ReflectUnit(joProperty);
        }
        Object value = joProperty.opt("value");
        String regex = joProperty.optString("regex", null);
        switch (name) {
            case PROPERTY_TEXT:
                return new PropertyText(name, key, propType, reflectUnit, value, regex);
            case PROPERTY_CLASS_NAME:
                return new PropertyClass(name, key, propType, reflectUnit, value, regex);
//            case PROPERTY_SIB_POSITION:
//                return new PropertySibPosition(name, key, propType, reflectUnit, value, regex);
//            case PROPERTY_SIB_ITEM_TYPE:
//                return new PropertySibItemType(name, key, propType, reflectUnit, value, regex);
//            case PROPERTY_ID_NAME:
//                return new PropertyIdName(name, key, propType, reflectUnit, value, regex);
//            case PROPERTY_BG_COLOR:
//                try {
//                    long lValue = Long.valueOf(value.toString(), 16);
//                    return new PropertyBgColor(name, key, propType, reflectUnit, (int) lValue, regex);
//                } catch (Exception e) {
//                    return new BaseProperty(name, key, propType, reflectUnit, value, regex);
//                }
            default:
                if(reflectUnit != null) {
                    return new BaseProperty(name, key, propType, reflectUnit, value, regex);
                }
        }
        return null;
    }
}
