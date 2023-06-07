package com.analysys.ipc;

import java.util.Map;

/**
 * @Copyright © 2020 Analysys Inc. All rights reserved.
 * @Description:
 * @Create: 2020/8/5 7:00 PM
 * @author: huchangqing
 */
public interface IIpcProxy {
    BytesParcelable getVisualSnapshotData(String config, boolean force);

    void clearVisualSnapshot();

    void onVisualEditEvent(String data);

    void reportVisualEvent(String eventId, String eventPageName, Map properties);

    void setVisualEditing(boolean editing);

    void sendVisualEditEvent2Client(String processName);

    void reloadVisualEventLocal();
}
