package com.tagsgroup.simulators.fix;

import com.tagsgroup.simulators.SimulatorMain;

public class ContextManager {
    SimulatorMain context = null;

    public SimulatorMain getContext() {
        return context;
    }

    public void setContext(SimulatorMain context) {
        this.context = context;
    }
}
