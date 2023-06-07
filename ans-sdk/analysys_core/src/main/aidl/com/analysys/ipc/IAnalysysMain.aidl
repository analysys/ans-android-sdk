package com.analysys.ipc;

interface IAnalysysMain {
    void setClientBinder(String processName, IBinder client);
    void reportVisualEvent(String eventId, String eventPageName, in Map properties);
}
