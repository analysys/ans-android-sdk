package com.analysys.mpaas;

import com.alipay.mobile.h5container.api.H5BridgeContext;
import com.alipay.mobile.h5container.api.H5Event;
import com.alipay.mobile.h5container.api.H5EventFilter;
import com.alipay.mobile.h5container.api.H5Param;
import com.alipay.mobile.h5container.api.H5SimplePlugin;
import com.alipay.mobile.nebula.util.H5Utils;
import com.alipay.mobile.nebula.webview.APWebView;
import com.analysys.hybrid.WebViewInjectManager;

/**
 * @Copyright © 2020 Analysys Inc. All rights reserved.
 * @Description:
 * @Create: 2020/9/11 11:06 AM
 * @author: huchangqing
 */
public class AnalysysH5Plugin extends H5SimplePlugin {
    @Override
    public void onPrepare(H5EventFilter filter) {
        super.onPrepare(filter);
        filter.addAction(CommonEvents.H5_PAGE_SHOULD_LOAD_URL);
    }

    private AnalysysMpaasHybridObject mHybridObject;

    @Override
    public void onRelease() {
        super.onRelease();
        WebViewInjectManager.getInstance().clearHybrid(mHybridObject);
        mHybridObject = null;
    }

    @Override
    public boolean interceptEvent(H5Event event, H5BridgeContext context) {
        String action = event.getAction();
        if (CommonEvents.H5_PAGE_SHOULD_LOAD_URL.equals(action)) {
            APWebView webView = event.getH5page().getWebView();
            if (mHybridObject == null) {
                mHybridObject = new AnalysysMpaasHybridObject(webView);
                // 注入webview
                WebViewInjectManager.getInstance().injectHybridObject(mHybridObject);
            }
        }
        return false;
    }
}