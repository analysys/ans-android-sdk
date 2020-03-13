package com.analysys.network;

import android.Manifest;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.analysys.AnalysysAgent;
import com.analysys.AnalysysConfig;
import com.analysys.utils.CommonUtils;

/**
 * @Copyright © 2020 EGuan Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2020/1/9 18:05
 * @Author: WP
 */
public class NetworkUtils {

    /**
     * 检测当的网络状态
     *
     * @param context Context
     * @return true 表示网络可用
     */
    public static boolean isNetworkAvailable(Context context) {
        if (!CommonUtils.checkPermission(context, Manifest.permission.ACCESS_NETWORK_STATE)) {
            return false;
        }
        ConnectivityManager connectivity = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo networkInfo = connectivity.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                // 当前网络是连接的
                return networkInfo.getState() == NetworkInfo.State.CONNECTED;
            }
        }
        return false;
    }


    /**
     *  获取网络类型
      * @param context
     * @param flag  true获取详细的网络运营商类型，false只获取运营商
     * @return
     */
    public static String networkType(Context context,boolean flag) {
        String netType = "";
        // 检测权限
        if (!CommonUtils.checkPermission(context, Manifest.permission.ACCESS_NETWORK_STATE)) {
            return "Unknown";
        }
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context
                .CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (connMgr != null) {
            networkInfo = connMgr.getActiveNetworkInfo();
        }

        if (networkInfo == null) {
            return netType;
        }
        int nType = networkInfo.getType();
        if (nType == ConnectivityManager.TYPE_WIFI) {
            netType = "WIFI";
        } else if (nType == ConnectivityManager.TYPE_MOBILE) {
            if(flag){
                int nSubType = networkInfo.getSubtype();
                return String.valueOf(nSubType);
            } else {
                //返回移动网络
                return "WWAN";
            }
        }
        return netType;
    }


    /**
     * 网络上传策略
     * @param networkType
     * @param uploadPolicy
     * @return
     */
    public static boolean isUploadPolicy(String networkType,int uploadPolicy) {
        int netType = AnalysysAgent.AnalysysNetworkType.AnalysysNetworkALL;
        if(networkType.equals("WWAN")){
            netType = AnalysysAgent.AnalysysNetworkType.AnalysysNetworkWWAN;
        } else if(networkType.equals("WIFI")){
            netType = AnalysysAgent.AnalysysNetworkType.AnalysysNetworkWIFI;
        }
        return (netType & uploadPolicy) != 0;
    }
}
