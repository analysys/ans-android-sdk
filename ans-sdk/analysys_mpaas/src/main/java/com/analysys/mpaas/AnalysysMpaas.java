package com.analysys.mpaas;

import com.alipay.mobile.h5container.api.H5Plugin;
import com.analysys.utils.ANSLog;
import com.mpaas.nebula.adapter.api.MPNebula;

/**
 * @Copyright © 2020 Analysys Inc. All rights reserved.
 * @Description:
 * @Create: 2020/9/11 10:53 AM
 * @author: huchangqing
 */
public class AnalysysMpaas {

    public static void init() {
        // 设置ua，相当于setHybridMode接口
//        MPNebula.setUa(new AnalysysUaProvider());
        // 注册插件，监听web加载实现hybrid事件和hybrid可视化
        MPNebula.registerH5Plugin(
                AnalysysH5Plugin.class.getName(),
                null,
                "page",
                new String[]{H5Plugin.CommonEvents.H5_PAGE_SHOULD_LOAD_URL}
        );
        ANSLog.i("mpaas init success");
    }

//    public static class AnalysysUaProvider implements H5UaProvider {
//
//        @Override
//        public String getUa(String defaultUaStr) {
//            if (defaultUaStr == null) {
//                defaultUaStr = "";
//            }
//            if (!defaultUaStr.contains(Constants.HYBRID_AGENT)) {
//                return defaultUaStr + Constants.HYBRID_AGENT;
//            }
//            return defaultUaStr;
//        }
//    }
}
