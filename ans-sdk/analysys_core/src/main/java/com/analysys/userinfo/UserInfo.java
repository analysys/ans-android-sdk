package com.analysys.userinfo;

import android.content.SharedPreferences;
import android.provider.Settings;
import android.text.TextUtils;

import com.analysys.AnalysysConfig;
import com.analysys.process.AgentProcess;
import com.analysys.utils.AnalysysUtil;
import com.analysys.utils.CommonUtils;
import com.analysys.utils.Constants;
import com.analysys.utils.SharedUtil;

public class UserInfo {

    // xwho,alias,distinct_id,original_id,is_login

   public static void setAliasID(String aliasID) {
       if (!TextUtils.isEmpty(aliasID)) {
           SharedUtil.setString(AnalysysUtil.getContext(), Constants.SP_ALIAS_ID, aliasID);
       } else {
           SharedUtil.remove(AnalysysUtil.getContext(),Constants.SP_ALIAS_ID);
       }
       CommonUtils.setIdFile(AnalysysUtil.getContext(), Constants.SP_ALIAS_ID, "");
   }

   public static String getAliasID() {
       //考虑兼容4.1.0之前版本通过sp的方式存储，也考虑兼容4.1.0~4.4.7之间版本通过setIDFile方式，还得考虑4.1.0和4.1.0~4.4.7之间版本都设置的alias先后顺序

       String aliasId = CommonUtils.getIdFile(AnalysysUtil.getContext(),Constants.SP_ALIAS_ID);
       if(TextUtils.isEmpty(aliasId)){
           aliasId = SharedUtil.getString(AnalysysUtil.getContext(),Constants.SP_ALIAS_ID,"");
       } else {
            SharedUtil.setString(AnalysysUtil.getContext(),Constants.SP_ALIAS_ID,aliasId);
            CommonUtils.setIdFile(AnalysysUtil.getContext(),Constants.SP_ALIAS_ID,"");
       }
       return aliasId;
   }

   public static void setDistinctID(String distinctID) {
       if(!TextUtils.isEmpty(distinctID)) {
           SharedUtil.setString(AnalysysUtil.getContext(),Constants.SP_DISTINCT_ID,distinctID);
       } else {
           SharedUtil.remove(AnalysysUtil.getContext(), Constants.SP_DISTINCT_ID);
       }
       CommonUtils.setIdFile(AnalysysUtil.getContext(), Constants.SP_DISTINCT_ID, "");
   }

    /**
     * 获取用户需要的distinctID：distinctID>uuid
     * @return
     */
   public static String getDistinctID() {
        String id = getOriginalDistinctID();
        if(TextUtils.isEmpty(id)) {
            id = getUUID();
        }
        return id;
   }

    /**
     * 获取原始的DistinctID，是通过identify接口设置的
     * @return
     */
   private static String getOriginalDistinctID() {
       // 通过identify设置的distinctID,原始的通过identify设置的
        String distinctId = CommonUtils.getIdFile(AnalysysUtil.getContext(),Constants.SP_DISTINCT_ID);
        if(TextUtils.isEmpty(distinctId)){
            distinctId = SharedUtil.getString(AnalysysUtil.getContext(),Constants.SP_DISTINCT_ID,"");
        } else {
            SharedUtil.setString(AnalysysUtil.getContext(),Constants.SP_DISTINCT_ID,distinctId);
            CommonUtils.setIdFile(AnalysysUtil.getContext(),Constants.SP_DISTINCT_ID,"");
        }
        return distinctId;
   }

   private static String getUUID() {
        String uuid = CommonUtils.getIdFile(AnalysysUtil.getContext(),Constants.SP_UUID);
        if(TextUtils.isEmpty(uuid)) {
            uuid = SharedUtil.getString(AnalysysUtil.getContext(),Constants.SP_UUID,"");

            if(TextUtils.isEmpty(uuid)) {
                uuid = String.valueOf(java.util.UUID.randomUUID());
                SharedUtil.setString(AnalysysUtil.getContext(),Constants.SP_UUID,uuid);
            }

        } else {
            SharedUtil.setString(AnalysysUtil.getContext(),Constants.SP_UUID,uuid);
            CommonUtils.setIdFile(AnalysysUtil.getContext(),Constants.SP_UUID,"");
        }
        return uuid;
   }

   public static String getXho() {
       // aliasID>distinctID>uuid
        String xwho = getAliasID();
        if(TextUtils.isEmpty(xwho)) {
            xwho = getOriginalDistinctID();

            if(TextUtils.isEmpty(xwho)) {
                xwho = getUUID();
            }
        }
        return xwho;
   }

   public static boolean getLoginState() {
       boolean flag = false;
       String aliasId = getAliasID();
       if(!TextUtils.isEmpty(aliasId)){
           flag = true;
       }
       return flag;
   }

   public static void resetUserInfo() {
       SharedUtil.remove(AnalysysUtil.getContext(), Constants.SP_ALIAS_ID);
       SharedUtil.remove(AnalysysUtil.getContext(), Constants.SP_DISTINCT_ID);
       SharedUtil.remove(AnalysysUtil.getContext(), Constants.SP_UUID);
       CommonUtils.setIdFile(AnalysysUtil.getContext(), Constants.SP_ALIAS_ID, "");
       CommonUtils.setIdFile(AnalysysUtil.getContext(), Constants.SP_DISTINCT_ID, "");
       CommonUtils.setIdFile(AnalysysUtil.getContext(), Constants.SP_UUID, "");

       SharedUtil.remove(AnalysysUtil.getContext(),Constants.SP_ORIGINAL_ID);
   }


    /**
     * 设置广告ID
     * @param adid
     */
   public static void setADID(String adid) {
       if(!TextUtils.isEmpty(adid)) {
           SharedUtil.setString(AnalysysUtil.getContext(), Constants.SP_ADID, adid);
       } else {
           SharedUtil.remove(AnalysysUtil.getContext(),Constants.SP_ADID);
       }
   }

    /**
     * 获取广告ID
     * @return
     */
   public static String getADID() {

       String id = SharedUtil.getString(AnalysysUtil.getContext(), Constants.SP_ADID, null);
       return id;
   }

    /**
     * device id advertisingId>androidid>uuid
     */
    public static String getDeviceId() {
        if (AgentProcess.getInstance().getConfig().isAutoTrackDeviceId()) {

            String id = getADID();
            if(TextUtils.isEmpty(id)){
                id = CommonUtils.getAndroidID(AnalysysUtil.getContext());

                if(TextUtils.isEmpty(id)){
                    id = getUUID();
                }
            }

            return id;
        }
        return "";
    }

    /**
     * 一直都没用
     * @param originalId
     */
   @Deprecated
   public static void setOriginalID(String originalId) {
       if(!TextUtils.isEmpty(originalId)) {
           SharedUtil.setString(AnalysysUtil.getContext(), Constants.SP_ORIGINAL_ID, originalId);
       } else {
           SharedUtil.remove(AnalysysUtil.getContext(),Constants.SP_ORIGINAL_ID);
       }
   }

    /**
     * 一直都没用
     * @return
     */
   @Deprecated
   public static String getOriginalID(){
       return SharedUtil.getString(AnalysysUtil.getContext(),Constants.SP_ORIGINAL_ID,"");
   }
}
