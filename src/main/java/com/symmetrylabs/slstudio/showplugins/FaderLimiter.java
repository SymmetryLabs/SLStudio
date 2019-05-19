package com.symmetrylabs.slstudio.showplugins;

import heronarts.lx.LX;
import heronarts.lx.LXLoopTask;

public class FaderLimiter implements LXLoopTask {
    private final LX lx;
    private final float max;

    public FaderLimiter(LX lx) {
        this(lx, 0.60f);
    }

    public FaderLimiter(LX lx, float max) {
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
        FaderLimiter afl = new FaderLimiter(lx);
        lx.engine.addLoopTask(afl);
    }

    public static void attach(LX lx, float max) {
        FaderLimiter afl = new FaderLimiter(lx, max);
        lx.engine.addLoopTask(afl);
    }
}
