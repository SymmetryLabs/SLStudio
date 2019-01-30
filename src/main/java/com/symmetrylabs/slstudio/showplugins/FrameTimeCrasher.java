package com.symmetrylabs.slstudio.showplugins;

import com.symmetrylabs.slstudio.SLStudio;
import heronarts.lx.LX;
import heronarts.lx.LXLoopTask;

/**
 * Crashes SLStudio when the frame time of the engine goes over a certain threshold.
 *
 * Used to combat memory leaks by effectively restarting SLStudio when a memory
 * leak gets so bad that the program can't run any more.
 */
public class FrameTimeCrasher implements LXLoopTask {
    private final LX lx;
    private final double maxFrameMs;

    public FrameTimeCrasher(LX lx, double maxFrameMs) {
        this.lx = lx;
        this.maxFrameMs = maxFrameMs;
    }

    @Override
    public void loop(double deltaMs) {
        double ms = lx.engine.timer.runCurrentNanos / 1e6;
        if (ms > maxFrameMs) {
            System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            System.err.println("ERROR: LXEngine is taking too long to run.");
            System.err.println(String.format("Allowed frame time: %.1fms", maxFrameMs));
            System.err.println(String.format("   Last frame time: %.2fms", ms));
            System.err.println("Killing and restarting SLStudio...");
            SLStudio.applet.restart();
        }
    }

    public static void attach(LX lx, double maxFrameMs) {
        FrameTimeCrasher ftc = new FrameTimeCrasher(lx, maxFrameMs);
        lx.engine.addLoopTask(ftc);
    }
}
