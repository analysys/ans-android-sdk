package com.analysys.process;

import android.content.Context;
import android.text.TextUtils;

import com.analysys.utils.CommonUtils;
import com.analysys.utils.Constants;
import com.analysys.utils.ExceptionUtil;
import com.analysys.utils.LogPrompt;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * @Copyright © 2019 EGuan Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2019/2/21 15:49
 * @Author: Wang-X-C
 */
public class TemplateManage {

    private static final String MM_BASE = "base";
    private static final String MM_OUTER = "outer";
    private static final String MM_X_CONTEXT = "xcontext";
    private static final String MM_RESERVED_KEYWORDS = "ReservedKeywords";

    private static final String CONTEXT_KEY = "contextKey";
    private static final String CONTEXT_VALUE = "contextValue";
    private static final String ADDITIONAL = "additional";

    private static final String USER_KEYS_LIMIT = "userKeysLimit";

    private static final String ANS_FIELDS_MOULD = "AnsFieldsTemplate.json";
    private static final String ANS_RULE_MOULD = "AnsRuleTemplate.json";

    private static final String CUSTOMER_FIELDS_MOULD = "CustomerFieldsTemplate.json";
    private static final String CUSTOMER_RULE_MOULD = "CustomerRuleTemplate.json";

    public static JSONObject fieldsMould = null;
    public static JSONObject ruleMould = null;

    public static JSONArray contextKey = null;
    public static JSONArray contextValue = null;

    public static JSONArray reservedKeywords = null;
    public static JSONObject additional = null;

    public static int userKeysLimit = 0;

    /**
     * 初始化模板
     */
    public static void initMould(Context context) throws Throwable {
        if (ruleMould == null) {
            fieldsMould = mergeFieldsMould(context);
            ruleMould = mergeRuleMould(context);
            if (fieldsMould == null || ruleMould == null) {
                LogPrompt.showLog(LogPrompt.TEMPLATE_ERR);
                return;
            }
            JSONObject xContextRueMould = ruleMould.optJSONObject(Constants.X_CONTEXT);
            contextKey =
                    xContextRueMould.optJSONObject(CONTEXT_KEY).getJSONArray(Constants.FUNC_LIST);
            contextValue =
                    xContextRueMould.optJSONObject(CONTEXT_VALUE).getJSONArray(Constants.FUNC_LIST);
            reservedKeywords = ruleMould.optJSONArray(MM_RESERVED_KEYWORDS);
            additional = xContextRueMould.optJSONObject(ADDITIONAL);
            if (additional != null) {
                userKeysLimit = additional.optInt(USER_KEYS_LIMIT);
            }

        }
    }

    public static JSONObject getFieldsMould(Context context) {
        if (fieldsMould == null) {
            try {
                initMould(context);
            } catch (Throwable ignore) {
                ExceptionUtil.exceptionThrow(ignore);
            }
        }
        return fieldsMould;
    }

    /**
     * 合并字段模板
     */
    private static JSONObject mergeFieldsMould(Context context) throws Throwable {
        JSONObject defaultMould = null;
        String defaultJson = CommonUtils.getMould(context, ANS_FIELDS_MOULD);
        String customerJson = CommonUtils.getMould(context, CUSTOMER_FIELDS_MOULD);
        if (!CommonUtils.isEmpty(defaultJson)) {
            defaultMould = new JSONObject(defaultJson);
        }
        if (!TextUtils.isEmpty(customerJson)) {
            mergeCustomerMould(defaultMould, new JSONObject(customerJson));
        }
        mergeSharedMould(defaultMould);
        if (defaultMould == null) {
            defaultMould = new JSONObject();
        }
        return defaultMould;
    }

    /**
     * 合并基础模板到事件模板
     */
    private static void mergeSharedMould(JSONObject defaultMould) throws Throwable {
        if (!CommonUtils.isEmpty(defaultMould)) {
            Iterator<String> keys = defaultMould.keys();
            JSONObject baseMould = defaultMould.optJSONObject(MM_BASE);
            String key = null;
            while (keys.hasNext()) {
                key = keys.next();
                if (!MM_BASE.equals(key)) {
                    mergeFields(defaultMould.optJSONObject(key), baseMould);
                }
            }
            defaultMould.remove(MM_BASE);
        }
    }

    /**
     * 合并基础事件模板和定制事件模板
     */
    private static void mergeCustomerMould(JSONObject defaultEvent,
                                           JSONObject customerEvent) throws Throwable {
        if (customerEvent != null) {
            Iterator<String> keys = customerEvent.keys();
            String key = null;
            JSONObject defaultMould = null;
            while (keys.hasNext()) {
                key = keys.next();
                if (TextUtils.isEmpty(key)) {
                    continue;
                }
                defaultMould = defaultEvent.optJSONObject(key);
                if (defaultMould != null) {
                    mergeFields(defaultMould, customerEvent.optJSONObject(key));
                } else {
                    defaultEvent.put(key, customerEvent.optJSONObject(key));
                }
            }
        }
    }

    /**
     * 合并基础公共模板和定制公共模板
     */
    private static void mergeFields(JSONObject defaultMould,
                                    JSONObject customerMould) throws Throwable {
        JSONArray defaultOuter = defaultMould.optJSONArray(MM_OUTER);
        JSONArray customerOuter = customerMould.optJSONArray(MM_OUTER);
        if (defaultOuter == null) {
            defaultMould.put(MM_OUTER, customerOuter);
        } else {
            mergeJsonArray(defaultOuter, customerOuter);
        }
        JSONArray defaultXContext = defaultMould.optJSONArray(MM_X_CONTEXT);
        JSONArray customerXContext = customerMould.optJSONArray(MM_X_CONTEXT);
        if (defaultXContext == null) {
            defaultMould.put(MM_X_CONTEXT, customerXContext);
        } else {
            mergeJsonArray(defaultXContext, customerXContext);
        }
    }

    /**
     * 合并规则模板
     */
    private static JSONObject mergeRuleMould(Context context) throws Throwable {
        JSONObject defaultRule = null;
        String defaultJson = CommonUtils.getMould(context, ANS_RULE_MOULD);
        String customerJson = CommonUtils.getMould(context, CUSTOMER_RULE_MOULD);
        if (!CommonUtils.isEmpty(defaultJson)) {
            defaultRule = new JSONObject(defaultJson);
            JSONArray defaultKeywords = defaultRule.optJSONArray(MM_RESERVED_KEYWORDS);
            JSONObject defaultXContext = defaultRule.optJSONObject(MM_X_CONTEXT);
            if (!CommonUtils.isEmpty(customerJson)) {
                JSONObject customerRule = null;
                customerRule = new JSONObject(customerJson);
                JSONArray customerKeywords = customerRule.optJSONArray(MM_RESERVED_KEYWORDS);
                JSONObject customerXContext = customerRule.optJSONObject(MM_X_CONTEXT);
                if (defaultKeywords != null && customerKeywords != null) {
                    mergeJsonArray(defaultKeywords, customerKeywords);
                    customerRule.remove(MM_RESERVED_KEYWORDS);
                }
                if (defaultXContext != null && customerXContext != null) {
                    mergeEventRule(defaultXContext, customerXContext);
                    customerRule.remove(MM_X_CONTEXT);
                }
                mergeEventRule(defaultRule, customerRule);
            }
        }
        return defaultRule;
    }

    /**
     * 合并默认、定制两个模板
     */
    private static void mergeEventRule(JSONObject defaultRule,
                                       JSONObject customerRule) throws Throwable {
        if (customerRule != null && defaultRule != null) {
            Iterator<String> keys = customerRule.keys();
            String key = null;
            while (keys.hasNext()) {
                key = keys.next();
                defaultRule.put(key, customerRule.optJSONObject(key));
            }
        }
    }

    /**
     * 合并两个jsonArray
     */
    private static void mergeJsonArray(JSONArray base, JSONArray other) {
        if (base != null && other != null) {
            for (int i = 0; i < other.length(); i++) {
                base.put(other.optString(i));
            }
        }
    }
}
