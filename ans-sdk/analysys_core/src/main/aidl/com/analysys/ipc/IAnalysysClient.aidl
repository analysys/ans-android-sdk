package com.analysys.ipc;
import com.analysys.ipc.BytesParcelable;

interface IAnalysysClient {

    BytesParcelable getVisualSnapshotData(String config, boolean force);
    void clearVisualSnapshot();
    void onVisualEditEvent(String data);
    void setVisualEditing(boolean editing);
    void reloadVisualEventLocal();
    boolean isInFront();
}
