package com.analysys.demo;

import android.text.TextUtils;

import com.analysys.process.AgentProcess;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * @Copyright © 2020 Analysys Inc. All rights reserved.
 * @Description:
 * @Create: 2020/7/22 5:41 PM
 * @author: huchangqing
 */
public class TrackEventObserver implements AgentProcess.IEventObserver {

    private static TrackEventObserver sInstance = new TrackEventObserver();

    public static class EventData {
        String eventName;
        String time;
        JSONObject sendData;

        public EventData(String eventName, String time, JSONObject sendData) {
            this.eventName = eventName;
            this.time = time;
            this.sendData = sendData;
        }

        public String getDetail() {
            StringBuilder sb = new StringBuilder();
            sb.append("xwhat: ").append(sendData.optString("xwhat"));
            sb.append("\nxwho: ").append(sendData.optString("xwho"));
            sb.append("\nxwhen: ").append(sendData.optString("xwhen"));
            sb.append("\nappid: ").append(sendData.optString("appid"));
            JSONObject xcontext = sendData.optJSONObject("xcontext");
            sb.append("\nxcontext: ");
            Iterator<String> keys = xcontext.keys();
            try {
                while (keys.hasNext()) {
                    String key = keys.next();
                    Object value = xcontext.get(key);
                    sb.append("\n  ").append(key).append(": ").append(value);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return sb.toString();
        }
    }

    public void init() {
        AgentProcess.getInstance().setEventObserver(this);
    }

    private List<EventData> mListData = new ArrayList<>();

    private TrackEventObserver() {
    }

    public static TrackEventObserver getInstance() {
        return sInstance;
    }

    public synchronized List<EventData> getData(String eventNameFilter) {
        List<EventData> listResult = new ArrayList<>();
        if (TextUtils.isEmpty(eventNameFilter)) {
            listResult.addAll(mListData);
        } else {
            String[] names = eventNameFilter.split(" ");
            for (int i = 0; i < mListData.size(); i++) {
                EventData data = mListData.get(i);
                for (int j = 0; j < names.length; j++) {
                    if (data.eventName.equals(names[j])) {
                        listResult.add(data);
                        break;
                    }
                }
            }
        }
        return listResult;
    }

    private SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private List<String> mObseverEventName = new ArrayList<>();
    private AgentProcess.IEventObserver mObserver;

    public synchronized void setObserver(String eventName, AgentProcess.IEventObserver observer) {
        mObseverEventName.clear();
        if (!TextUtils.isEmpty(eventName)) {
            mObseverEventName.addAll(Arrays.asList(eventName.split(" ")));
        }
        mObserver = observer;
    }

    @Override
    public synchronized void onEvent(String eventName, JSONObject sendData) {
        try {
            long xwhen = sendData.getLong("xwhen");
            String xwhat = sendData.getString("xwhat");
            JSONObject xcontext = sendData.getJSONObject("xcontext");
            String url = xcontext.optString("$url", null);
            if (!TextUtils.isEmpty(url) && (url.equals(FragmentEvent.class.getName())
//                    || url.equals(FragmentUser.class.getName())
//                    || url.equals(FragmentProperty.class.getName())
                    || url.equals(InfoDialog.class.getName()))) {
                return;
            }
            String elementType = xcontext.optString("$element_type", null);
            if (!TextUtils.isEmpty(elementType) && elementType.equals("MenuItem")) {
                return;
            }
            Date d1 = new Date(xwhen);
            String time = mFormat.format(d1);
            mListData.add(0, new EventData(eventName, time, sendData));
            if (mObserver != null && (mObseverEventName.isEmpty() || mObseverEventName.contains(xwhat))) {
                mObserver.onEvent(eventName, sendData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
