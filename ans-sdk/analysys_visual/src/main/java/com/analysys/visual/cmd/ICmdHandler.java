package com.analysys.visual.cmd;

import java.io.OutputStream;

public interface ICmdHandler {

    void handleCmd(Object cmd, OutputStream out);
}
