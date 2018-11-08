package com.symmetrylabs.shows.arlo;

import heronarts.lx.LX;
import heronarts.lx.LXLoopTask;

public class ArloFaderLimiter implements LXLoopTask {
    private final LX lx;

    public ArloFaderLimiter(LX lx) {
        this.lx = lx;
    }

    @Override
    public void loop(double deltaMs) {
        if (lx.engine.output.brightness.getValue() > 0.65) {
            lx.engine.output.brightness.setValue(0.65);
        }
    }

    public static void attach(LX lx) {
        ArloFaderLimiter afl = new ArloFaderLimiter(lx);
        lx.engine.addLoopTask(afl);
    }
}
