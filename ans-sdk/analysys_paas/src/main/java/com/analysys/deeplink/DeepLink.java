package com.analysys.deeplink;

import android.content.Intent;
import android.net.Uri;

import com.analysys.utils.CheckUtils;
import com.analysys.utils.CommonUtils;
import com.analysys.utils.Constants;
import com.analysys.utils.InternalAgent;

import java.util.HashMap;
import java.util.Map;

/**
 * @Copyright © 2018 EGuan Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2018/12/5 16:09
 * @Author: Wang-X-C
 */
public class DeepLink {

    private static final String UTM_SOURCE = "utm_source";
    private static final String UTM_MEDIUM = "utm_medium";
    private static final String UTM_CAMPAIGN = "utm_campaign";
    private static final String UTM_CAMPAIGN_ID = "utm_campaign_id";
    private static final String UTM_CONTENT = "utm_content";
    private static final String UTM_TERM = "utm_term";
    private static final String HM_SR = "hmsr";
    private static final String HM_PL = "hmpl";
    private static final String HM_CU = "hmcu";
    private static final String HM_KW = "hmkw";
    private static final String HM_CI = "hmci";

    private static String uriAddress = null;

    public static Map<String, Object> getUtmValue(Intent intent) {
        Map<String, Object> utmMap = new HashMap<String, Object>();
        if (!CommonUtils.isEmpty(intent)) {
            Uri uri = intent.getData();
            InternalAgent.uri = String.valueOf(uri);
            if (!CommonUtils.isEmpty(uri)) {
                // 判断两次是否为同一次deepLink
                if (isUriEquals(uri)) {
                    return utmMap;
                }
                if (!CommonUtils.isEmpty(uri.getQueryParameter(UTM_SOURCE))
                        && !CommonUtils.isEmpty(uri.getQueryParameter(UTM_MEDIUM))
                        && !CommonUtils.isEmpty(uri.getQueryParameter(UTM_CAMPAIGN))) {
                    getUtmInfo(utmMap, uri);
                } else if (!CommonUtils.isEmpty(uri.getQueryParameter(HM_SR))
                        && !CommonUtils.isEmpty(uri.getQueryParameter(HM_PL))
                        && !CommonUtils.isEmpty(uri.getQueryParameter(HM_CU))) {
                    getHmInfo(utmMap, uri);
                }
            }
        }
        return utmMap;
    }

    private static void getUtmInfo(Map<String, Object> map, Uri uri) {
        CheckUtils.checkToMap(map, Constants.UTM_SOURCE,
                uri.getQueryParameter(UTM_SOURCE));
        CheckUtils.checkToMap(map, Constants.UTM_MEDIUM,
                uri.getQueryParameter(UTM_MEDIUM));
        CheckUtils.checkToMap(map, Constants.UTM_CAMPAIGN,
                uri.getQueryParameter(UTM_CAMPAIGN));
        CheckUtils.checkToMap(map, Constants.UTM_CAMPAIGN_ID,
                uri.getQueryParameter(UTM_CAMPAIGN_ID));
        CheckUtils.checkToMap(map, Constants.UTM_CONTENT,
                uri.getQueryParameter(UTM_CONTENT));
        CheckUtils.checkToMap(map, Constants.UTM_TERM,
                uri.getQueryParameter(UTM_TERM));
    }

    private static void getHmInfo(Map<String, Object> map, Uri uri) {
        CheckUtils.checkToMap(map,
                Constants.UTM_SOURCE, uri.getQueryParameter(HM_SR));
        CheckUtils.checkToMap(map,
                Constants.UTM_MEDIUM, uri.getQueryParameter(HM_PL));
        CheckUtils.checkToMap(map,
                Constants.UTM_CAMPAIGN, uri.getQueryParameter(HM_CU));
        CheckUtils.checkToMap(map,
                Constants.UTM_CAMPAIGN, uri.getQueryParameter(HM_KW));
        CheckUtils.checkToMap(map,
                Constants.UTM_CONTENT, uri.getQueryParameter(HM_CI));
    }

    private static boolean isUriEquals(Uri uri) {
        if (!CommonUtils.isEmpty(uriAddress)) {
            if (uriAddress.equals(uri.toString())) {
                return true;
            }
        }
        uriAddress = uri.toString();
        return false;
    }
}
