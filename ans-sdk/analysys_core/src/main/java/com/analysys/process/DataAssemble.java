package com.analysys.process;

import android.content.Context;
import android.text.TextUtils;

import com.analysys.utils.AnalysysUtil;
import com.analysys.utils.CheckUtils;
import com.analysys.utils.CommonUtils;
import com.analysys.utils.Constants;
import com.analysys.utils.ExceptionUtil;
import com.analysys.utils.LogPrompt;
import com.analysys.utils.SharedUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @Copyright © 2019 EGuan Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2019/2/22 17:46
 * @Author: Wang-X-C
 */
public class DataAssemble {

    private final String OUTER = "outer";
    private final String VALUE = "value";
    private final String VALUE_TYPE = "valueType";
    Context mContext;

    public static DataAssemble getInstance(Context context) {
        if (Holder.INSTANCE.mContext == null && context != null) {
            Holder.INSTANCE.mContext = context;
        }
        return Holder.INSTANCE;
    }

    /**
     * 获取完整上传Json数据，务必保证是在线程中调用，否则会导致阻塞
     * 参数1.API名称
     * 参数2.eventName
     * 参数3.用户传参
     * 参数4.默认采集
     * 参数5.eventName track事件使用
     */
    public JSONObject getEventData(Object... values) throws Throwable {
        if (values != null && mContext != null) {
            long currentTime = (long) values[0];

            String apiName = String.valueOf(values[1]);
            String eventName = String.valueOf(values[2]);
            Map<String, Object> data = toMap(values[3]);
            JSONObject eventMould = getEventMould(eventName);
            if (eventMould == null) {
                return null;
            }
            if (Constants.TRACK.equals(eventName)) {
                String eventInfo = String.valueOf(values[5]);
                if (!CheckUtils.checkTrackEventName(eventName, eventInfo)) {
                    LogPrompt.showLog(apiName, LogBean.getLog());

                    //当校验不合格的时候数据还是发送，只是打印日志
//                    return null;
                }
                eventName = eventInfo;
            }
            // 校验用户传参
            CheckUtils.checkParameter(apiName, data);
            if (data == null) {
                data = new HashMap<String, Object>();
            }
            mergeParameter(data, values[4]);
            mergeSuperProperty(eventName, data);
            //合并预置事件自定义事件
            mergePreEventUserProperty(eventName,data);

            return fillData(eventName, eventMould, data,currentTime);
        }
        return null;
    }

    private void mergeParameter(Map<String, Object> userParameter, Object autoParameter) {
        Map<String, Object> autoMap = toMap(autoParameter);
        CommonUtils.clearEmptyValue(autoMap);
        if (autoMap != null) {
            userParameter.putAll(autoMap);
            autoMap.clear();
        }
    }

    /**
     * 获取事件字段模板
     */
    private JSONObject getEventMould(String eventName) {
        JSONObject jo = TemplateManage.getFieldsMould(AnalysysUtil.getContext());
        if (jo == null) {
            return null;
        }
        if (eventName.startsWith(Constants.PROFILE)) {
            return jo.optJSONObject(Constants.PROFILE);
        }
        return jo.optJSONObject(eventName);
    }

    /**
     * 转 map
     */
    private Map<String, Object> toMap(Object data) {
        if (data != null) {
            return (Map<String, Object>) data;
        }
        return null;
    }

    /**
     * 添加通用属性
     */
    private void mergeSuperProperty(String eventName,
                                    Map<String, Object> xContextMap) throws Throwable {
        if (!Constants.ALIAS.equals(eventName) && !eventName.startsWith(Constants.PROFILE)) {

            Map mapSuper = AgentProcess.getInstance().getSuperProperty();
            if (mapSuper != null && mapSuper.size() > 0) {
                Iterator iterator = mapSuper.keySet().iterator();
                while (iterator.hasNext()) {
                    String key = (String) iterator.next();
                    if (key != null) {
                        if (!xContextMap.containsKey(key)) {
                            xContextMap.put(key, mapSuper.get(key));
                        }
                    }
                }
            }

        }
    }

    /**
     * 添加预置事件用户自定义属性
     */

    private void mergePreEventUserProperty(String eventName,
                                           Map<String, Object> xContextMap) throws Throwable {
        if (Constants.END.equals(eventName) || Constants.STARTUP.equals(eventName) || Constants.APP_CRASH_DATA.equals(eventName) || Constants.ALIAS.equals(eventName) || Constants.PAGE_VIEW.equals(eventName)) {
            Map mapSuper = AgentProcess.getInstance().getPreEventUserProperties();
            if (mapSuper != null && mapSuper.size() > 0) {
                Iterator iterator = mapSuper.keySet().iterator();
                while (iterator.hasNext()) {
                    String key = (String) iterator.next();
                    if (key != null) {
                        if (!xContextMap.containsKey(key)) {
                            xContextMap.put(key, mapSuper.get(key));
                        }
                    }
                }
            }
        }
    }

    /**
     * 通过遍历字段模板填充数据
     */
    private JSONObject fillData(String eventName, JSONObject eventMould,
                                Map<String, Object> xContextMap,long currentTime) throws Throwable {
        JSONObject allJob = null;
        JSONArray outerKeysArray = eventMould.optJSONArray(OUTER);
        String outFields = null;
        JSONObject fieldsRuleMould = null;
        JSONArray xContextFieldsArray = null;
        Map<String, Object> map = new HashMap<String, Object>();
        if (outerKeysArray != null) {
            allJob = new JSONObject();
            for (int i = 0; i < outerKeysArray.length(); i++) {
                outFields = outerKeysArray.optString(i);
                fieldsRuleMould = TemplateManage.ruleMould.optJSONObject(outFields);
                xContextFieldsArray = eventMould.optJSONArray(outFields);
                if (xContextFieldsArray != null) {
                    int length = xContextFieldsArray.length();
                    String xContextFields = null;
                    for (int j = 0; j < length; j++) {
                        xContextFields = xContextFieldsArray.optString(j);
                        map.put(xContextFields,
                                getValue(fieldsRuleMould.optJSONObject(xContextFields), null));
                    }
                    if (!CommonUtils.isEmpty(map)) {
                        CommonUtils.clearEmptyValue(map);
                        xContextMap.putAll(map);
                        map.clear();
                    }
                    allJob.put(outFields, new JSONObject(xContextMap));
                } else {
                    //  3.获取value并校验
                    Object outerValue = getValue(fieldsRuleMould, eventName);
                    CommonUtils.pushToJSON(allJob, outFields, outerValue);
                    allJob.put("xwhen", currentTime);
                }
            }
        }
        return allJob;
    }

    /**
     * 通过ruleMould模板规则获取value值
     * 0：方法获取
     * 1. 默认值
     * 2. 值传入
     */
    private Object getValue(JSONObject ruleMould, String what) {
        if (ruleMould != null) {
            int type = ruleMould.optInt(VALUE_TYPE);
            String value = ruleMould.optString(VALUE);
            if (type == 0) {
                return reflexGetData(value);
            } else if (type == 1) {
                return value;
            } else if (type == 2) {
                return what;
            }
        }
        return null;
    }

    /**
     * 通过反射获取需要填充的数据
     */
    private Object reflexGetData(String path) {
        if (!TextUtils.isEmpty(path)) {
            return CommonUtils.reflexUtils(
                    CommonUtils.getClassPath(path),
                    CommonUtils.getMethod(path),
                    new Class[]{Context.class}, mContext);
        }
        return null;
    }

    private static class Holder {
        public static DataAssemble INSTANCE = new DataAssemble();
    }
}
