package com.analysys.aesencrypt;

import android.text.TextUtils;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESEncrypt {
    public final static byte[] ivBytes = "Analysys_315$CBC".getBytes();

    /**
     * 加密
     */
    public static String ECBEncrypt(String content, String password) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(password.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            byte[] result = cipher.doFinal(content.getBytes());
            return toHex(result);
        } catch (Throwable e) {
        }
        return null;
    }

    /**
     * 解密
     */
    public static byte[] ECBDecrypt(String content, String password) {
        try {
            byte[] contents = toBytes(content);
            SecretKeySpec secretKeySpec = new SecretKeySpec(password.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            byte[] result = cipher.doFinal(contents);
            return result;
        } catch (Throwable e) {
        }
        return null;
    }

    /**
     * 加密
     */
    /**
     * 加密
     */
    public static String CBCEncrypt(String rawPassword, String content) {
        try {
            byte[] key = rawPassword.getBytes();
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivSpec);
            byte[] result = cipher.doFinal(content.getBytes());
            return toHex(result);
        } catch (Throwable e) {
        }
        return null;
    }

    /**
     * 解密
     */
    public static byte[] CBCDecrypt(String rawPassword, String content) {
        try {
            byte[] key = rawPassword.getBytes();
            byte[] contents = toBytes(content);
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivSpec);
            byte[] result = cipher.doFinal(contents);
            return result;
        } catch (Throwable e) {
        }
        return null;
    }

    /**
     * to bytes
     */
    public static byte[] toBytes(String cipherString) {
        if (TextUtils.isEmpty(cipherString)) {
            return null;
        }
        int len = cipherString.length() / 2;
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++) {
            result[i] = Integer.valueOf(cipherString.substring(2 * i, 2 * i + 2), 16).byteValue();
        }
        return result;
    }

    /**
     * to String
     */
    public static String toHex(byte[] bytes) {
        final char[] hexDigits = "0123456789ABCDEF".toCharArray();
        StringBuilder ret = new StringBuilder(bytes.length * 2);
        for (int i = 0; i < bytes.length; i++) {
            ret.append(hexDigits[(bytes[i] >> 4) & 0x0f]);
            ret.append(hexDigits[bytes[i] & 0x0f]);
        }
        return ret.toString();
    }

    private static byte[] getKey(byte[] rawpassword) {
        try {
            // 创建AES的Key生产者
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            // 利用用户密码作为随机数初始化出128位的key生产者
            // 加密没关系，SecureRandom是生成安全随机数序列，rawpassword是种子，只要种子相同，序列就一样，所以解密只要有password就行
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            random.setSeed(rawpassword);
            kgen.init(128, random);
            // 根据用户密码，生成一个密钥
            SecretKey secretKey = kgen.generateKey();
            // 返回基本编码格式的密钥，如果此密钥不支持编码，则返回null
            byte[] enCodeFormat = secretKey.getEncoded();
            return enCodeFormat;
        } catch (Throwable e) {
        }
        return rawpassword;
    }
}
