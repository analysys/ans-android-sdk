package com.analysys;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.analysys.database.EventTableMetaData;
import com.analysys.utils.AnalysysUtil;
import com.analysys.utils.CommonUtils;
import com.analysys.utils.Constants;
import com.analysys.utils.ExceptionUtil;
import com.analysys.utils.SharedUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * @Copyright 2020 analysys Inc. All rights reserved.
 * @Description: 内存变量控制器，主要为了解决内存变量多进程同步的问题
 * @Version: 1.0
 * @Create: 2020-04-26 17:38:25
 * @author: miqt
 * @mail: miqingtang@analysys.com.cn
 */
public class AnsRamControl {
    private static volatile AnsRamControl instance = null;

    private AnsRamControl() {
        Context context = AnalysysUtil.getContext();
        if (context != null && CommonUtils.isMainProcess(context)) {
            setDataCollectEnable(SharedUtil.getBoolean(context, Constants.SP_ENABLE_DATA_COLLECT, true));
        }
    }

    public static AnsRamControl getInstance() {
        if (instance == null) {
            synchronized (AnsRamControl.class) {
                if (instance == null) {
                    instance = new AnsRamControl();
                }
            }
        }
        return instance;
    }


    private void set(String key, String value) {
        try {
            Uri uri = EventTableMetaData.getTABLE_RAM(AnalysysUtil.getContext());
            ContentValues contentValues = new ContentValues();
            contentValues.put("key", key);
            contentValues.put("values", value);
            AnalysysUtil.getContext().getContentResolver().insert(uri, contentValues);
        } catch (Throwable ignore) {
            ExceptionUtil.exceptionThrow(ignore);
        }
    }

    private String get(String key, String defValue) {
        String values = null;
        Cursor cursor = null;
        try {
            Uri uri = EventTableMetaData.getTABLE_RAM(AnalysysUtil.getContext());
            cursor = AnalysysUtil.getContext().getContentResolver().query(uri, new String[]{key, defValue}, defValue, null, null);
            if (cursor != null && cursor.getCount() >= 1) {
                if (cursor.moveToPosition(0)) {
                    values = cursor.getString(0);
                }
            }
        } catch (Throwable ignore) {
            ExceptionUtil.exceptionThrow(ignore);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        if (values == null) {
            values = defValue;
        }
        return values;
    }

    /**
     * 设置上传策略
     *
     * @param networkType 上传策略值
     * @see AnalysysAgent.AnalysysNetworkType#AnalysysNetworkALL
     * @see AnalysysAgent.AnalysysNetworkType#AnalysysNetworkNONE
     * @see AnalysysAgent.AnalysysNetworkType#AnalysysNetworkWWAN
     * @see AnalysysAgent.AnalysysNetworkType#AnalysysNetworkNONE
     */
    public void setUploadNetworkType(int networkType) {
        set(Constants.RAM_NET_WORK_TYPE, String.valueOf(networkType));
    }

    /**
     * 读取网络上传策略
     *
     * @return 上传策略值
     */
    public int getUploadNetworkType() {
        try {
            String value = get(Constants.RAM_NET_WORK_TYPE,
                    String.valueOf(AnalysysAgent.AnalysysNetworkType.AnalysysNetworkALL));
            return Integer.valueOf(value);
        } catch (Throwable e) {
            ExceptionUtil.exceptionThrow(e);
        }
        return AnalysysAgent.AnalysysNetworkType.AnalysysNetworkALL;
    }


    /**
     * 获取最大缓存数量
     *
     * @return 最大缓存数量, 默认值：{@link  Constants#MAX_CACHE_COUNT}
     */
    public long getCacheMaxCount() {
        try {
            String value = get(Constants.RAM_CACHE_MAX_COUNT,
                    String.valueOf(Constants.MAX_CACHE_COUNT));
            return Long.valueOf(value);
        } catch (Throwable e) {
            ExceptionUtil.exceptionThrow(e);
        }
        return Constants.MAX_CACHE_COUNT;
    }

    /**
     * 设置最大缓存数量
     *
     * @param cacheMaxCount 最大缓存数量 默认值：{@link  Constants#MAX_CACHE_COUNT}
     */
    public void setCacheMaxCount(long cacheMaxCount) {
        set(Constants.RAM_CACHE_MAX_COUNT, String.valueOf(cacheMaxCount));
    }

    /**
     * 获取数据采集开关
     */
    public Boolean getDataCollectEnable() {
        try {
            String value = get(Constants.RAM_DATA_COLLECT_ENABLE, null);
            if (value != null) {
                return Boolean.valueOf(value);
            }
        } catch (Throwable e) {
            ExceptionUtil.exceptionThrow(e);
        }
        return null;
    }

    /**
     * 设置数据采集开关
     */
    public void setDataCollectEnable(boolean enable) {
        set(Constants.RAM_DATA_COLLECT_ENABLE, String.valueOf(enable));
    }

    public static final class RamData {
        private static volatile RamData instance = null;
        //基本类型的内存参数
        private Map<String, String> mRamCacheMap;


        private RamData() {
            mRamCacheMap = new HashMap<>();
            initDef();
        }

        /**
         * 设置默认值
         */
        private void initDef() {
            mRamCacheMap.put(Constants.RAM_NET_WORK_TYPE, String.valueOf(AnalysysAgent.AnalysysNetworkType.AnalysysNetworkALL));
            mRamCacheMap.put(Constants.RAM_CACHE_MAX_COUNT, String.valueOf(Constants.MAX_CACHE_COUNT));
        }

        public static RamData getInstance() {
            if (instance == null) {
                synchronized (RamData.class) {
                    if (instance == null) {
                        instance = new RamData();
                    }
                }
            }
            return instance;
        }

        /**
         * 请继续保持仅在AnsContentProvider进行读写该map，保证进程间同步。
         *
         * @return
         */
        public Map<String, String> getMap() {
            return mRamCacheMap;
        }
    }

}
