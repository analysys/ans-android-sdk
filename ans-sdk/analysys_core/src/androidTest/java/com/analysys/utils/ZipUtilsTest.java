package com.analysys.utils;

import org.junit.Assert;
import org.junit.Test;

/**
 * @Copyright © 2019 EGuan Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2019-08-14 10:16
 * @Author: Wang-X-C
 */
public class ZipUtilsTest {

    @Test
    public void compressForGzip() {
        try {
            Assert.assertNotNull(ZipUtils.compressForGzip("测试字符串"));
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    @Test
    public void decompressForGzip() {
        try {
            byte[] byteValue = ZipUtils.compressForGzip("测试字符串");
            Assert.assertNotNull(ZipUtils.decompressForGzip(byteValue));
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
}