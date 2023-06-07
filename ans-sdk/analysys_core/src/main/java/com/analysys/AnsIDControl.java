package com.analysys;

import android.content.Context;

import com.analysys.database.TableAllInfo;
import com.analysys.userinfo.UserInfo;
import com.analysys.utils.AnalysysUtil;
import com.analysys.utils.CommonUtils;
import com.analysys.utils.Constants;
import com.analysys.utils.SharedUtil;

public class AnsIDControl {


    /**
     * 启动的时候重置数据
     */
    public static void resetID() {
        //在主进程进行重置，避免多个进程同时重置数据
        if(CommonUtils.isMainProcess(AnalysysUtil.getContext())) {
            // 重置PV计数器值
            SharedUtil.setInt(AnalysysUtil.getContext(),Constants.PAGE_COUNT,0);

            // 重置refer
            SharedUtil.setString(AnalysysUtil.getContext(), Constants.SP_REFER, "");


            SharedUtil.setBoolean(AnalysysUtil.getContext(),Constants.SP_SEND_SUCCESS,false);

        }
    }

    /**
     * reset 接口 重置所有属性
     */
    public static void resetInfo(Context context) {
        // 重置首次访问
        SharedUtil.remove(context, Constants.SP_FIRST_START_TIME);
//        // 重置首日访问
//        SharedUtil.remove(context, Constants.DEV_IS_FIRST_DAY);
        // 重置 通用属性
        SharedUtil.remove(context, Constants.SP_SUPER_PROPERTY);
        // 重置JS通用属性
        SharedUtil.remove(context, Constants.SP_JS_SUPER_PROPERTY);


        // 重置 用户相关id
        UserInfo.resetUserInfo();

        //  清空数据库
        TableAllInfo.getInstance(context).deleteAll();
    }
}
