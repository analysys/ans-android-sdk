package com.analysys.visual.cmd;

import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;

import com.analysys.network.NetworkUtils;
import com.analysys.utils.ANSLog;
import com.analysys.utils.AnalysysUtil;
import com.analysys.utils.Constants;
import com.analysys.visual.VisualManager;

import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;

public class CmdSendToServerImpl implements ICmdHandler {

    private static final String TAG = VisualManager.TAG;

//    private static final String ID = "$id";
    private static final String EVENT_ID = "$event_id";
    private static final String MANUFACTURER = "$manufacturer";
    private static final String MODEL = "$model";
    private static final String OS_VERSION = "$os_version";
    private static final String LIB_VERSION = "$lib_version";
    private static final String NETWORK = "$network";
    private static final String SCREEN_WIDTH = "$screen_width";
    private static final String SCREEN_HEIGHT = "$screen_height";
    private static final String EVENT_INFO = "event_info";
    private static final String TARGET_PAGE = "target_page";
    private static final String TYPE = "type";
    private static final String EVENTINFO_REQUEST = "eventinfo_request";

    @Override
    public void handleCmd(Object cmd, OutputStream out) {
        Map<String, Object> info = (Map<String, Object>) cmd;
        final OutputStreamWriter writer = new OutputStreamWriter(out);
        try {
            JSONObject event = new JSONObject();
            JSONObject eventInfo = new JSONObject();
//            eventInfo.put(ID, info.id);
            eventInfo.put(EVENT_ID, info.get(VisualManager.KEY_EVENT_ID));
            eventInfo.put(MANUFACTURER, Build.MANUFACTURER);
            eventInfo.put(MODEL, Build.MODEL);
            eventInfo.put(OS_VERSION, Build.VERSION.RELEASE);
            eventInfo.put(LIB_VERSION, Constants.DEV_SDK_VERSION);
            eventInfo.put(NETWORK, NetworkUtils.networkType(AnalysysUtil.getContext(), true));
            DisplayMetrics dm = Resources.getSystem().getDisplayMetrics();
            eventInfo.put(SCREEN_WIDTH, dm.widthPixels);
            eventInfo.put(SCREEN_HEIGHT, dm.heightPixels);
            Map<String, Object> properties = (Map<String, Object>) info.get(VisualManager.KEY_EVENT_PROPERTIES);
            if (properties != null) {
                for (String key : properties.keySet()) {
                    eventInfo.put(key, properties.get(key));
                }
            }
            event.put(EVENT_INFO, eventInfo);
            event.put(TARGET_PAGE, info.get(VisualManager.KEY_EVENT_PAGE_NAME));
            event.put(TYPE, EVENTINFO_REQUEST);

            writer.write(event.toString());
            writer.flush();
            ANSLog.i(TAG, "Send ws command: eventinfo_request");
        } catch (final IOException e) {
            ANSLog.e(TAG, "send event_info to server fail", e);
        } catch (Throwable e) {
            ANSLog.e(e);
        } finally {
            try {
                writer.close();
            } catch (final IOException e) {
                ANSLog.e(TAG, "close websocket writer fail", e);
            }
        }
    }
}
