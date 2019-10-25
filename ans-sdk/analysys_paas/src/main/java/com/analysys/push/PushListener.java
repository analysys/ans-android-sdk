package com.analysys.push;

/**
 * @Copyright © 2018 EGuan Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2018/12/25 19:14
 * @Author: Wang-X-C
 */
public interface PushListener {
    /**
     * push推送的回调方法
     *
     * @param action Push消息中的action
     * @param jsonParams 通过服务器下发透传过来的消息
     */
    void execute(String action, String jsonParams);
}
