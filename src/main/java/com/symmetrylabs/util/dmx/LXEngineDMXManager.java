package com.symmetrylabs.util.dmx;

import heronarts.lx.LXEngine;

public final class LXEngineDMXManager {

    static final int MASTER_FADER_PORT = 0;

    public static void configure(LXEngine engine) {
        engine.dmx.addHandler(new LXParameterChangeDMXHandler(MASTER_FADER_PORT, engine.output.brightness));
    }

}
