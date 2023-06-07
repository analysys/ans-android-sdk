package com.analysys.utils;

import static android.content.Context.SENSOR_SERVICE;

import android.content.Context;
import android.hardware.SensorManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class AnalysysDeduplicationUtil {

    private static volatile AnalysysDeduplicationUtil instance;

    private Map<String,Object> judgeMap;

    private Context mContext;
    private int duplicationCount = 0;


    public static AnalysysDeduplicationUtil getInstance(Context context) {
        if (context == null) {
            return null;
        }
        if (instance == null) {
            synchronized (AnalysysSSManager.class) {
                if (instance == null) {
                    instance = new AnalysysDeduplicationUtil(context);
                }
            }
        }
        return instance;
    }

    public AnalysysDeduplicationUtil (Context context) {
        mContext = context;
        judgeMap = new ConcurrentHashMap<>();

    }

    public boolean judgeDeduplication(JSONObject object) {

        try {
            String key = object.getString("xwhen");
            String jsonString = object.toString();
            int contentHashCode = jsonString.hashCode();

            if (key == null) {
                return true;
            }

            if (key.isEmpty() || contentHashCode == 0) {
                return true;
            }

            if (judgeMap.isEmpty()) {
                judgeMap.put(key,jsonString.hashCode());
                return true;
            }
            if (judgeMap.get(key) != null) {
                if (judgeMap.get(key).equals(jsonString.hashCode())) {
                    duplicationCount ++;
                    ANSLog.d("duplication count = ",duplicationCount);

                    return false;
                }
            }


            if (judgeMap.size() >= 10) {
                Set<String> keySet = judgeMap.keySet();
                Iterator<String> iterator = keySet.iterator();
                String firstKey = "";
                if (iterator.hasNext()) {
                    firstKey = iterator.next();
                }
                if (firstKey.length() > 0) {
                    judgeMap.remove(firstKey);
                }
            }

            judgeMap.put(key,jsonString.hashCode());


        } catch (JSONException e) {
            e.printStackTrace();
        }

        return true;
    }
}
