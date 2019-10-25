package com.analysys.aesencrypt;

import java.util.Map;

/**
 * @Copyright © 2019 EGuan Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2019/3/8 15:29
 * @Author: Wang-X-C
 */
public class EncryptAgent {
    /**
     * 数据AES加密
     */
    public static String dataEncrypt(String appId, String version, String data, int type) {
        return EncryptManager.getInstance().dataEncrypt(appId, version, data, type);
    }

    /**
     * 获取上传头信息
     */
    public static Map<String, String> getHeadInfo() {
        return EncryptManager.getInstance().getHeadInfo();
    }
}
