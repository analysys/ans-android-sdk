package com.analysys.visual.cmd;

import com.analysys.visual.utils.VisualIpc;

import java.io.OutputStream;

public class CmdEventBindImpl implements ICmdHandler {

    @Override
    public void handleCmd(Object cmd, OutputStream out) {
        VisualIpc.getInstance().onVisualEditEvent(cmd.toString());
    }
}
