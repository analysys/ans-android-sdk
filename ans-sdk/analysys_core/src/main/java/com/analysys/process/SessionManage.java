package com.analysys.process;

import android.content.Context;
import android.text.TextUtils;

import com.analysys.utils.CommonUtils;
import com.analysys.utils.Constants;
import com.analysys.utils.ExceptionUtil;
import com.analysys.utils.SharedUtil;

import java.security.MessageDigest;
import java.util.Random;

/**
 * @Copyright © 2018 EGuan Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2018/12/5 16:07
 * @Author: Wang-X-C
 */
public class SessionManage {

    private Context mContext = null;
    private String startDay = "";

    public static SessionManage getInstance(Context context) {
        if (Holder.INSTANCE.mContext == null && context != null) {
            Holder.INSTANCE.mContext = context.getApplicationContext();
        }
        return Holder.INSTANCE;
    }

    public synchronized void resetSession(boolean deepLink) {
        // 是否跨天
        if (isSpanDay()) {
            setSessionId();
            return;
        }
        // 判断此次启动是否为deepLink启动
        if (deepLink) {
            setSessionId();
            return;
        }
        // 判断session是否为空
        if (CommonUtils.isEmpty(getSessionId())) {
            setSessionId();
            return;
        }
        // 判断是否超时 重置Session
        if (isSessionTimeOut(mContext)) {
            setSessionId();
        }
    }

    /**
     * 获取 session Id
     */
    public synchronized String getSessionId() {
        return SharedUtil.getString(mContext, Constants.SP_SESSION_ID,"");
    }

    /**
     * 判断 Session 是否超时
     */
    private boolean isSessionTimeOut(Context context) {
        if (context != null) {
            String pageEndTime = SharedUtil.getString(context, Constants.SP_LAST_PAGE_CHANGE,"");
            if (!TextUtils.isEmpty(pageEndTime)) {
                long endTime = CommonUtils.parseLong(pageEndTime, 0);
                // 上次变动时间到现在是否超过30s
                return System.currentTimeMillis() - endTime >= Constants.SESSION_INVALID;
            }
        }
        return true;
    }


    /**
     * 由是否跨天判断是否需要重置session
     */
    private boolean isSpanDay() {
        if (CommonUtils.isEmpty(startDay)) {
            startDay = SharedUtil.getString(mContext, Constants.SP_START_DAY,"");
            if (CommonUtils.isEmpty(startDay)) {
                setStartDay();
                return false;
            }
        }
        // 其次判断是不是同一天，要不要更新session
        if (!CommonUtils.getDay().equals(startDay)) {
            setStartDay();
            return true;
        }
        return false;
    }

    /**
     * 存储页面的开始日期
     */
    private void setStartDay() {
        startDay = CommonUtils.getDay();
        SharedUtil.setString(mContext, Constants.SP_START_DAY, startDay);
    }

//    /**
//     * 由上个页面的结束时间，和当前页面的开启时间判断要不要更新session
//     */
//    private boolean isTimeOut() {
//        if (pageEndTime < 1) {
//            pageEndTime = SharedUtil.getLong(mContext, Constants.SP_LAST_PAGE_CHANGE, 0L);
//        }
//        if (pageEndTime > 1) {
//            long time = System.currentTimeMillis();
//            if ((time - pageEndTime) > Constants.BG_INTERVAL_TIME) {
//                return true;
//            }
//        } else {
//            return true;
//        }
//        return false;
//    }

    private void setSessionId() {
        SharedUtil.setString(mContext, Constants.SP_SESSION_ID, getSession());
    }

    /**
     * 存储SessionId
     */
    private String getSession() {
        Random random = new Random();
        String randomNumber = String.valueOf(random.nextInt(1000000));
        return getMD5(System.currentTimeMillis()
                + Constants.DEV_SYSTEM + randomNumber);
    }

    /**
     * MD5加密
     */
    private String getMD5(String val) {
        byte[] m = null;
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(val.getBytes());
            m = md5.digest();
        } catch (Throwable ignore) {
            ExceptionUtil.exceptionThrow(ignore);
        }
        return getString(m);
    }

    private String getString(byte[] b) {
        if (b != null) {
            StringBuilder buf = new StringBuilder();
            for (int value : b) {
                int a = value;
                if (a < 0) {
                    a += 256;
                }
                if (a < 16) {
                    buf.append("0");
                }
                buf.append(Integer.toHexString(a));
            }
            return buf.toString().substring(8, 24);
        }
        return "";
    }

    private static class Holder {
        public static final SessionManage INSTANCE = new SessionManage();
    }
}
