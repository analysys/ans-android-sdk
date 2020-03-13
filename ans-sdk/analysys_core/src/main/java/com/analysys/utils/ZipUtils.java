package com.analysys.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @Copyright © 2018 EGuan Inc. All rights reserved.
 * @Description: Gzip 压缩 base64编码
 * @Version: 1.0
 * @Create: 2018/3/13
 * @Author: Wang-X-C
 */
public class ZipUtils {
    /**
     * Gzip 压缩数据
     */
    public static byte[] compressForGzip(String unGzipStr) throws IOException {
        if (CommonUtils.isEmpty(unGzipStr)) {
            return null;
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(baos);
        gzip.write(unGzipStr.getBytes());
        gzip.close();
        byte[] encode = baos.toByteArray();
        baos.flush();
        baos.close();
        return encode;
    }

    /**
     * Gzip解压数据
     */
    public static String decompressForGzip(byte[] gzipStr) {
        ByteArrayOutputStream out = null;
        ByteArrayInputStream in = null;
        GZIPInputStream gzip = null;
        try {
            if (gzipStr == null || gzipStr.length <= 0) {
                return null;
            }
            out = new ByteArrayOutputStream();
            in = new ByteArrayInputStream(gzipStr);
            // android 对GZIPInputStream有调整，严格模式下会报错
            gzip = new GZIPInputStream(in);
            byte[] buffer = new byte[256];
            int n = 0;
            while ((n = gzip.read(buffer, 0, buffer.length)) > 0) {
                out.write(buffer, 0, n);
            }
            return out.toString();
        } catch (Throwable ignore) {
            ExceptionUtil.exceptionThrow(ignore);
        } finally {
            if (gzip != null) {
                try {
                    gzip.close();
                } catch (Throwable ignore) {
                    ExceptionUtil.exceptionThrow(ignore);
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (Throwable ignore) {
                    ExceptionUtil.exceptionThrow(ignore);
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (Throwable ignore) {
                    ExceptionUtil.exceptionThrow(ignore);
                }
            }
        }
        return null;
    }
}
