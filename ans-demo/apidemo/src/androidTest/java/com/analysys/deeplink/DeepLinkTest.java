package com.analysys.deeplink;

import android.content.Intent;
import android.net.Uri;

import org.junit.Assert;
import org.junit.Test;

/**
 * @Copyright © 2019 EGuan Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2019-08-16 10:38
 * @Author: Wang-X-C
 */
public class DeepLinkTest {

    @Test
    public void getUtmValue() {
        Intent intent = new Intent();
        intent.setData(Uri.parse("http://images.google.com/images?utm_source=关键字&utm_medium" +
                "=测试&utm_campaign=utm_campaign&utm_campaign_id=utm_campaign_id&utm_content" +
                "=utm_content&utm_term=utm_term"));
        Assert.assertNotNull(DeepLink.getUtmValue(intent));

        Intent intent2 = new Intent();
        intent2.setData(Uri.parse("http://images.google.com/images?hmsr=关键字&hmpl" +
                "=测试&hmcu=hmcu&hmkw=hmkw&hmci=hmci"));
        Assert.assertNotNull(DeepLink.getUtmValue(intent2));
    }
}