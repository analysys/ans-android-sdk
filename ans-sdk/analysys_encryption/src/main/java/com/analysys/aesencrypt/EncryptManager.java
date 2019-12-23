package com.analysys.aesencrypt;

import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

/**
 * @Copyright © 2019 EGuan Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2019/3/8 15:30
 * @Author: Wang-X-C
 */
public class EncryptManager {

    static String requestTime = null;
    static int type = 0;

    public static EncryptManager getInstance() {

        return Holder.INSTANCE;
    }

    /**
     * 获取加密key
     */
    private static String getEncryptKey(String platform, String appId, String sdkVersionName,
                                        String time) {
        try {
            String originalKey = platform + appId + sdkVersionName + time;
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            String md5Code = AESEncrypt.toHex(md5.digest(originalKey.getBytes("utf-8")));
            String encodeKey = Base64Utils.encode(md5Code.getBytes());
            String[] versionCode = sdkVersionName.split("\\.");
            int length = versionCode.length;
            String key = "";
            if (length > 2) {
                int valuePosition = Integer.parseInt(versionCode[length - 1]);
                int startPosition = Integer.parseInt(versionCode[length - 2]);
                key = getEncryptCode(encodeKey, startPosition, valuePosition);
            }
            return key;
        } catch (Throwable e) {
        }
        return null;
    }

    /**
     * 加密秘钥 生成
     *
     * @param startPosition 取数的起始位置 偶数从前往后，奇数从后往前
     * @param valuePosition 取数位置，取奇数位还是偶数位 奇数取奇数位值，偶数取值偶数位值
     */
    private static String getEncryptCode(String rowKey, int startPosition, int valuePosition) {
        try {
            if (rowKey != null) {
                if (startPosition % 2 != 0) {
                    rowKey = new StringBuilder(rowKey).reverse().toString();
                }
                StringBuffer sbKey = new StringBuffer();
                if (valuePosition % 2 != 0) {
                    for (int i = 0; i < rowKey.length(); i++) {
                        sbKey.append(rowKey.charAt(i));
                        i++;
                    }
                } else {
                    for (int i = 0; i < rowKey.length(); i++) {
                        i++;
                        sbKey.append(rowKey.charAt(i));
                    }
                }
                if (sbKey.length() < 16) {
                    for (int i = rowKey.length() - 1; 0 < i; i--) {
                        sbKey.append(rowKey.charAt(i));
                        if (sbKey.length() == 16) {
                            return sbKey.toString();
                        }
                    }
                } else {
                    return sbKey.substring(0, 16);
                }
                return String.valueOf(sbKey);
            }
        } catch (Exception e) {
            
        }
        return null;
    }

    /**
     * 获取上传头信息
     */
    public Map<String, String> getHeadInfo() {
        Map<String, String> headInfo = new HashMap<String, String>();
        headInfo.put("reqt", requestTime());
        headInfo.put("reqv", String.valueOf(type));
        return headInfo;
    }

    private String requestTime() {
        if (requestTime == null) {
            requestTime = String.valueOf(System.currentTimeMillis());
        }
        return requestTime;
    }

    /**
     * aes加密
     */
    public String dataEncrypt(String appId, String version, String data, int encryptType) {
        String encrypt = null;
        try {
            String pwd = getEncryptKey("Android", appId, version, requestTime());
            if (pwd != null) {
                type = encryptType;
                if (encryptType == 1) {
                    encrypt = AESEncrypt.ECBEncrypt(data, pwd);
                }
                if (encryptType == 2) {
                    encrypt = AESEncrypt.CBCEncrypt(pwd, data);
                }
            }
        } catch (Throwable e) {
        }
        return encrypt;
    }

    private static class Holder {
        public static final EncryptManager INSTANCE = new EncryptManager();
    }
}
