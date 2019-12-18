package com.analysys.utils;

import org.json.JSONArray;
import org.junit.Test;

/**
 * @Copyright © 2019 EGuan Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2019-08-15 11:48
 * @Author: Wang-X-C
 */
public class LogPromptTest {

    @Test
    public void showLog() {
        String value = "testLog";
        LogPrompt.showLog(value);
    }

    @Test
    public void showLog1() {
        String value = "testLog";
        String detailed = "detailedLog";
        LogPrompt.showLog(value, detailed);
    }

    @Test
    public void encryptLog() {
        String value = "testLog";
        LogPrompt.showLog(value, true);
        LogPrompt.showLog(value, false);
    }

    @Test
    public void showLog2() {
        String value = "testLog";
        LogPrompt.showLog(value, true);
        LogPrompt.showLog(value, false);
    }

    @Test
    public void showInitLog() {
        LogPrompt.showInitLog(true);
        LogPrompt.showInitLog(false);
    }

    @Test
    public void showChannelLog() {
        String channel = "channelLogTest";
        LogPrompt.showChannelLog(true, channel);
        LogPrompt.showChannelLog(false, channel);
    }

    @Test
    public void showKeyLog() {
        String key = "keyLogTest";
        LogPrompt.showKeyLog(false, key);
        LogPrompt.showKeyLog(true, key);
    }

    @Test
    public void processFailed() {
        LogPrompt.processFailed();
    }

    @Test
    public void keyFailed() {
        LogPrompt.keyFailed();
    }

    @Test
    public void networkErr() {
        LogPrompt.networkErr();
    }

    @Test
    public void showSendMessage() {
        String url = "https://arksdktest.analysys.com:4089";
        JSONArray jar = new JSONArray();
        jar.put("json 数组测试");
        LogPrompt.showSendMessage(url, jar);
    }

    @Test
    public void showReturnCode() {
        String code = "200";
        LogPrompt.showReturnCode(code);
    }

    @Test
    public void showSendResults() {
        LogPrompt.showSendResults(true);
        LogPrompt.showSendResults(false);
    }
}