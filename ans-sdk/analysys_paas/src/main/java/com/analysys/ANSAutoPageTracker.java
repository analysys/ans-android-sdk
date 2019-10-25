package com.analysys;

import java.util.Map;

/**
 * @Copyright © 2019 EGuan Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2019-08-20 10:34
 * @Author: Wang-X-C
 */
public interface ANSAutoPageTracker {
    /**
     * 获取对应页面的自定义参数
     * @return
     */
    Map<String, Object> registerPageProperties();

    /**
     * 手动设置页面的url
     * @return
     */
    String registerPageUrl();

}
