package com.analysys;

/**
 * Create by Kevin on 2019-08-28
 * Describe:
 */
public interface ObserverListener {
    /**
     * @param key   用户属性的key值，是上传的时候key值，比如用户ID是xwho
     * @param value 用户属性值
     */
    void onUserProfile(final String key, final String value);

    /**
     * @param event 完整的用户上传的json数据
     */
    void onEventMessage(final String event);
}
