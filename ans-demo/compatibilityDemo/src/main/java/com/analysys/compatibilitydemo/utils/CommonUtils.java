package com.analysys.compatibilitydemo.utils;

import android.content.Context;

/**
 * @Copyright © 2019 EGuan Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2019-07-11 11:39
 * @Author: Wang-X-C
 */
public class CommonUtils {

    public static int getDebug(Context context){

        return SharedUtil.getInt(context, Content.DEBUG_MODE, 2);

    }

}
