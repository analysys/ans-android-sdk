package com.analysys.visual.utils;

import android.text.TextUtils;

import com.analysys.utils.AnsReflectUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @Copyright © 2019 Analysys Inc. All rights reserved.
 * @Description: 反射单元类
 * @Create: 2019-11-28 10:35
 * @author: hcq
 */
public class ReflectUnit {

    private final String mReflectName;
    private Class<?>[] mParamsClz;
    private Object[] mParamsValue;

    /**
     * 将json配置转化为反射单元
     */
    public ReflectUnit(JSONObject jo) throws JSONException, ClassNotFoundException {
        mReflectName = jo.optString("reflect_name", null);
        if (!TextUtils.isEmpty(mReflectName)) {
            JSONArray jaParamsClz = jo.optJSONArray("params_type");
            JSONArray jaParamsValue = jo.optJSONArray("params");
            if (jaParamsClz != null && jaParamsValue != null) {
                mParamsClz = new Class[jaParamsClz.length()];
                mParamsValue = new Object[mParamsClz.length];
                for (int i = 0; i < mParamsClz.length; i++) {
                    mParamsClz[i] = Class.forName(jaParamsClz.getString(i));
                    mParamsValue[i] = jaParamsValue.get(i);
                }
            }
        }
    }

    /**
     * 获取反射值，约定json中无params为反射字段，否则为反射函数
     */
    public Object call(Object obj) {
        if (mParamsClz == null) {
            return AnsReflectUtils.getField(obj, mReflectName);
        } else {
            return AnsReflectUtils.invokeMethod(obj, mReflectName, mParamsClz, mParamsValue);
        }
    }
}
