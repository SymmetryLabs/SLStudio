package com.symmetrylabs.shows.arlo;

import com.symmetrylabs.util.CaptionSource;
import com.symmetrylabs.slstudio.SLStudioLX;
import heronarts.lx.LX;
import heronarts.lx.LXLoopTask;
import heronarts.lx.modulator.LinearEnvelope;

public class ArloXfadeTimer implements LXLoopTask, CaptionSource {
    private static final double LIGHT_SHOW_MS = 1000 * 60 * 10; // 10min
    private static final double SOLID_COLOR_MS = 1000 * 60 * 30; // 30min
    private static final double CROSSFADE_MS = 5_000;

    private final LX lx;
    private boolean inLightShow = true;
    private double timeSinceLastXfade = 0;
    private long lastRunNanos;
    private double lastXfadeValue = 0;
    private long xfadeStartNanos;

    public ArloXfadeTimer(LX lx, SLStudioLX.UI ui) {
        this.lx = lx;
        lastXfadeValue = lx.engine.crossfader.getValue();
    }

    @Override
    public void loop(double deltaMs) {
        /* we don't use deltaMs, because we want this to be independent
         of engine speed. */
        long runNanos = System.nanoTime();
        timeSinceLastXfade += 1e-6 * (runNanos - lastRunNanos);
        lastRunNanos = runNanos;

        double xfadeValue = lx.engine.crossfader.getValue();
        if (Math.abs(xfadeValue - lastXfadeValue) > 0.1) {
            timeSinceLastXfade = 0;
            inLightShow = xfadeValue < 0.5;
        }
        lastXfadeValue = xfadeValue;

        if (timeSinceLastXfade > (inLightShow ? LIGHT_SHOW_MS : SOLID_COLOR_MS)) {
            inLightShow = !inLightShow;
            xfadeStartNanos = runNanos;
            timeSinceLastXfade = 0;
        }

        double xfade = 1e-6 * (runNanos - xfadeStartNanos) / CROSSFADE_MS;
        if (xfade > 1) {
            xfade = 1;
        }
        if (inLightShow) {
            xfade = 1 - xfade;
        }
        lx.engine.crossfader.setValue(xfade);
    }

    @Override
    public String getCaption() {
        double deadline = inLightShow ? LIGHT_SHOW_MS : SOLID_COLOR_MS;
        deadline -= timeSinceLastXfade;
        return String.format(
            "in %s, %.2f seconds until crossfade",
            inLightShow ? "light show" : "solid color mode", deadline / 1000);
    }

    public static void attach(LX lx, SLStudioLX.UI ui) {
        ArloXfadeTimer axt = new ArloXfadeTimer(lx, ui);
        lx.engine.addLoopTask(axt);
        ui.captionText.addSource(axt);
    }
}
