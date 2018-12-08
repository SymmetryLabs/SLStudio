package com.symmetrylabs.shows.arlo;

import heronarts.lx.LX;
import heronarts.lx.LXLoopTask;

public class ArloFaderLimiter implements LXLoopTask {
    private final LX lx;
    private final float max;

    public ArloFaderLimiter(LX lx) {
        this(lx, 0.65f);
    }

    public ArloFaderLimiter(LX lx, float max) {
        this.lx = lx;
        this.max = max;
    }

    @Override
    public void loop(double deltaMs) {
        if (lx.engine.output.brightness.getValue() > max) {
            lx.engine.output.brightness.setValue(max);
        }
    }

    public static void attach(LX lx) {
        ArloFaderLimiter afl = new ArloFaderLimiter(lx);
        lx.engine.addLoopTask(afl);
    }

    public static void attach(LX lx, float max) {
        ArloFaderLimiter afl = new ArloFaderLimiter(lx, max);
        lx.engine.addLoopTask(afl);
    }
}
