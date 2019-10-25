package com.analysys.push;


/**
 * @Copyright © 2018 EGuan Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2018/12/25 19:26
 * @Author: Wang-X-C
 */
public class Constants {
    /**
     * push
     */
    public static final String PUSH_KEY_INFO = "EGPUSH_CINFO";
    public static final String PUSH_KEY_CAMPID = "campaign_id";
    public static final String PUSH_KEY_CAMPID_NAME = "utm_campaign";
    public static final String PUSH_KEY_CAMPID_MEDIUM = "utm_medium";
    public static final String PUSH_KEY_CAMPID_SOURCE = "utm_source";
    public static final String PUSH_KEY_CAMPID_CONTENT = "utm_content";
    public static final String PUSH_KEY_CAMPID_TERM = "utm_term";
    public static final String PUSH_KEY_ACTION = "ACTION";
    public static final String PUSH_KEY_ACTIONTYPE = "ACTIONTYPE";
    public static final String PUSH_KEY_CPD = "CPD";

    /** PUSH event */
    public static final String PUSH_EVENT_ACTION = "$action";
    public static final String PUSH_EVENT_ACTIONTYPE = "$action_type";
    public static final String PUSH_EVENT_COMPAIGNID = "$utm_campaign_id";
    public static final String PUSH_EVENT_CPD = "$cpd";


    public static final String PUSH_EVENT_CAMPID_NAME = "$utm_campaign";
    public static final String PUSH_EVENT_CAMPID_MEDIUM = "$utm_medium";
    public static final String PUSH_EVENT_CAMPID_SOURCE = "$utm_source";
    public static final String PUSH_EVENT_CAMPID_CONTENT = "$utm_content";
    public static final String PUSH_EVENT_CAMPID_TERM = "$utm_term";

    public static final String PUSH_PACKAGE = "com.analysys.push";
    public static final String PUSH_CLASS = "PushProvider";
}
