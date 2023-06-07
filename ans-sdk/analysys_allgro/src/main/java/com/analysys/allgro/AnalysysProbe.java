package com.analysys.allgro;

import com.analysys.allgro.plugin.ASMProbeHelp;
import com.analysys.allgro.plugin.PageViewProbe;
import com.analysys.allgro.plugin.ViewClickProbe;

/**
 * @Copyright © 2020 Analysys Inc. All rights reserved.
 * @Description: 全埋点初始化入口类
 * @Create: 2020/5/6 11:32 AM
 * @author: huchangqing
 */
public class AnalysysProbe {

    public static void init() {
        ASMProbeHelp.getInstance().registerHookObserver(new PageViewProbe());
        ASMProbeHelp.getInstance().registerHookObserver(new ViewClickProbe());
    }

}
