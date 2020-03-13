package com.analysys.utils;

import android.text.TextUtils;

import com.analysys.process.LogBean;
import com.analysys.process.TemplateManage;

import org.json.JSONArray;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Copyright © 2019 EGuan Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2019/5/10 17:40
 * @Author: Wang-X-C
 */
public class ParameterCheck {
    /**
     * 校验事件名称（配置）
     */
    public static void checkEventName(Object eventInfo) {
        try {
            String eventName = String.valueOf(eventInfo);
            if (!checkNamingSpecification(eventName)) {
                LogBean.setDetails(Constants.CODE_FAILED,
                        LogPrompt.FRONT + getSubString(eventName) + LogPrompt.NAMING_ERR);
                return;
            }
            if (Constants.MAX_EVENT_NAME_LENGTH < eventName.length()) {
                LogBean.setDetails(Constants.CODE_FAILED,
                        LogPrompt.ERR_HEAD_VALUE + getSubString(eventName) + LogPrompt.WHAT_LENGTH_ERR);
                return;
            }
            LogBean.setCode(Constants.CODE_SUCCESS);
        } catch (Throwable ignore) {
            ExceptionUtil.exceptionThrow(ignore);
        }
    }

    /**
     * 校验xContext内key字符串长度（配置）
     */
    public static void checkKey(Object objKey) {
        try {
            if (CommonUtils.isEmpty(objKey)) {
                LogBean.setDetails(Constants.CODE_FAILED, LogPrompt.KEY_EMPTY);
                return;
            }
            String key = String.valueOf(objKey);
            if (Constants.MAX_KEY_LENGTH < key.length()) {
                LogBean.setDetails(Constants.CODE_DELETE,
                        LogPrompt.ERR_HEAD_KEY + getSubString(key) + LogPrompt.KEY_LENGTH_ERR);
                return;
            }
            if (!checkNamingSpecification(key)) {
                LogBean.setDetails(Constants.CODE_CUT_OFF,
                        LogPrompt.FRONT + getSubString(key) + LogPrompt.NAMING_ERR);
                return;
            }
            if (!isReservedKeywords(key)) {
                LogBean.setDetails(Constants.CODE_DELETE,
                        LogPrompt.FRONT + getSubString(key) + LogPrompt.RESERVED_ERR);
                return;
            }
        } catch (Throwable ignore) {
            ExceptionUtil.exceptionThrow(ignore);
        }
    }

    /**
     * 校验value数据类型（配置）
     */
    public static void checkValue(Object value) {
        try {
            if (CommonUtils.isEmpty(value)) {
                LogBean.setDetails(Constants.CODE_FAILED, LogPrompt.VALUE_EMPTY);
            } else {
                if (value instanceof Number || value instanceof Boolean) {
                } else if (value instanceof String) {
                    String sValue = String.valueOf(value);
                    if (!checkValueLength(sValue)) {
                        setLengthErr(sValue);
                        LogBean.setValue(getSubValue(sValue));
                        return;
                    }
                } else if (value.getClass().isArray()) {
                    checkArray(value);
                } else if (value instanceof List<?>) {
                    checkList((List<Object>) value);
                } else if (value instanceof JSONArray) {
                    checkJsonArray((JSONArray) value);
                } else {
                    LogBean.setDetails(Constants.CODE_FAILED, LogPrompt.TYPE_ERROR);
                }
            }

        } catch (Throwable ignore) {
            ExceptionUtil.exceptionThrow(ignore);
        }
    }

    /**
     * 判断是否可以覆盖
     */
    private static boolean isReservedKeywords(String key) {
        if (TemplateManage.reservedKeywords != null) {
            JSONArray reservedKeywords = TemplateManage.reservedKeywords;
            for (int i = 0; i < reservedKeywords.length(); i++) {
                if (key.equals(reservedKeywords.optString(i))) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 校验key命名是否规范
     */
    private static boolean checkNamingSpecification(String key) {
        if (key != null) {
            Pattern p = Pattern.compile(Constants.KEY_RULE);
            Matcher m = p.matcher(key);
            return m.matches();
        }
        return false;
    }

    /**
     * Array 类型校验
     */
    private static void checkArray(Object object) {
        if (object instanceof String[] ? false : true) {
            LogBean.setDetails(Constants.CODE_FAILED, LogPrompt.TYPE_ERROR);
            return;
        }
        if (!checkArraySize(((String[]) object).length)) {
            LogBean.setDetails(Constants.CODE_FAILED, LogPrompt.ARRAY_SIZE_ERROR);
            return;
        }
        String[] values = (String[]) object;
        String value = null;
        for (int i = 0; i < values.length; i++) {
            value = values[i];
            if (!checkValueLength(value)) {
                setLengthErr(value);
                values[i] = getSubValue(value);
            }
        }
    }

    /**
     * list类型校验
     */
    private static void checkList(List<Object> list) {
        if (list.size() > 100) {
            LogBean.setDetails(Constants.CODE_FAILED, LogPrompt.ARRAY_SIZE_ERR);
            return;
        }
        Object object = null;
        for (int i = 0; i < list.size(); i++) {
            object = list.get(i);
            if (object instanceof String) {
                String value = String.valueOf(object);
                if (!checkValueLength(value)) {
                    setLengthErr(value);
                    list.set(i, getSubValue(value));
                }
            } else {
                LogBean.setDetails(Constants.CODE_DELETE, LogPrompt.TYPE_ERROR);

            }

        }

    }

    /**
     * value 字符串 超长 Log 日志 设置
     */
    private static void setLengthErr(String value) {
        LogBean.setDetails(Constants.CODE_CUT_OFF,
                LogPrompt.ERR_HEAD_VALUE
                        + getSubString(value)
                        + LogPrompt.VALUE_LENGTH_ERR);
    }

    /**
     * json array 类型校验
     *
     * @throws
     */
    private static void checkJsonArray(JSONArray jar) {
        for (int i = 0; i < jar.length(); i++) {
            if (jar.opt(i) instanceof Number) {
                LogBean.setDetails(Constants.CODE_FAILED, LogPrompt.TYPE_ERROR);
                return;
            }
        }
    }

    /**
     * 校验array大小
     */
    private static boolean checkArraySize(int size) {
        if (Constants.MAX_ARRAY_SIZE < size) {
            return false;
        }
        return true;
    }

    /**
     * 校验xContext内value字符串长度
     */
    private static boolean checkValueLength(String value) {
        if (!TextUtils.isEmpty(value) && 255 < value.length()) {
            return false;
        }
        return true;
    }

    /**
     * 超长字段截取
     */
    private static String getSubString(String value) {
        if (!CommonUtils.isEmpty(value)) {
            int length = 30;
            if (length < value.length()) {
                value = value.substring(0, length) + "....";
            }
        }
        return value;
    }

    private static String getSubValue(String value) {
        if (value != null && 8192 < value.length()) {
            return value.substring(0, 8191) + "$";
        }
        return value;
    }
}
