package com.symmetrylabs.slstudio.pattern;

import java.util.List;
import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.model.LXPoint;
import heronarts.lx.model.LXFixture;
import heronarts.lx.color.LXColor;
import heronarts.lx.color.ColorParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.EnumParameter;

// based on https://github.com/ncase/fireflies
public class FirefliesPattern extends LXPattern {
    public static enum FlashMode { FLASH, CLOCK };

    public final ColorParameter colorParam;
    public final BooleanParameter usePaletteParam;
    public final CompoundParameter speedParam;
    public final BooleanParameter syncParam;
    public final CompoundParameter radiusParam;
    public final CompoundParameter nudgeParam;
    public final EnumParameter<FlashMode> flashModeParam;
    public final CompoundParameter durationParam;
    public final BooleanParameter resetParam;
    public final CompoundParameter noiseParam;

    private static final int CLOCK_MAX = 1000;

    private int[] clocks;
    private long[] flashStarts;

    public FirefliesPattern(LX lx) {
        super(lx);

        clocks = new int[getSubmodelsSize()];
        flashStarts = new long[clocks.length];

        double r = Math.sqrt(lx.model.xRange * lx.model.xRange
                                + lx.model.yRange * lx.model.yRange
                                + lx.model.zRange * lx.model.zRange);

        addParameter(colorParam = new ColorParameter("Color", LXColor.WHITE));
        addParameter(usePaletteParam = new BooleanParameter("UsePalette"));
        colorParam.addListener((p) -> {
            if (colorParam.getColor() != lx.palette.color.getColor()) {
                usePaletteParam.setValue(false);
            }
        });
        usePaletteParam.addListener((p) -> {
            colorParam.setColor(lx.palette.color.getColor());
        });
        lx.palette.color.addListener((p) -> {
            if (usePaletteParam.isOn()) {
                colorParam.setColor(lx.palette.color.getColor());
            }
        });

        addParameter(speedParam = new CompoundParameter("Speed", 0.5, 0, 2));
        addParameter(syncParam = new BooleanParameter("Sync", true));
        addParameter(radiusParam = new CompoundParameter("Radius", r / 15, r / 1000, r));
        addParameter(nudgeParam = new CompoundParameter("Nudge", 0.035, 0.01, 0.1));
        addParameter(flashModeParam = new EnumParameter<>("FlashMode", FlashMode.FLASH));
        addParameter(durationParam = new CompoundParameter("Duration", 0.75, 0, 2));
        addParameter(noiseParam = new CompoundParameter("Noise", 0.01, 0, 0.05));

        addParameter(resetParam = new BooleanParameter("Reset").setMode(BooleanParameter.Mode.MOMENTARY));
        resetParam.addListener((p) -> resetClocks());

        resetClocks();
    }

    private void resetClocks() {
        for (int i = 0; i < clocks.length; ++i) {
            clocks[i] = (int)(Math.random() * CLOCK_MAX);
            flashStarts[i] = 0;
        }
    }

    protected int getSubmodelsSize() {
        return model.getPoints().size();
    }
    protected void setSubmodelColor(int i, int c) {
        setColor(i, c);
    }
    protected double getSubmodelsDist(int i, int j) {
        LXPoint pi = model.getPoints().get(i);
        LXPoint pj = model.getPoints().get(j);
        float dx = pi.x - pj.x;
        float dy = pi.y - pj.y;
        float dz = pi.z - pj.z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    @Override
    protected void run(double deltaMs) {
        long timerMillis = System.nanoTime() / 1000000;
        double flashDur = durationParam.getValue() * 1000;

        for (int i = 0; i < getSubmodelsSize(); ++i) {
            clocks[i] += noiseParam.getValue() * (Math.random() * 2 - 1) * CLOCK_MAX;
            clocks[i] += speedParam.getValue() * deltaMs;
            if (clocks[i] > CLOCK_MAX) {
                flashStarts[i] = timerMillis;
                clocks[i] = 0;

                if (syncParam.isOn()) {
                    for (int j = 0; j < getSubmodelsSize(); ++j) {
                        if (i == j)
                            continue;

                        double d = getSubmodelsDist(i, j);
                        double p = -d / radiusParam.getValue() + 1;

                        if (p > 0) {
                            clocks[j] *= 1 + nudgeParam.getValue() * p;
                        }
                    }
                }
            }
        }

        clear();

        for (int i = 0; i < getSubmodelsSize(); ++i) {
            long td = timerMillis - flashStarts[i];
            if (td > 0 && td <= flashDur) {
                setSubmodelColor(i, colorParam.getColor());
            }
            else if (flashModeParam.getEnum() == FlashMode.CLOCK) {
                setSubmodelColor(i, LXColor.scaleBrightness(colorParam.getColor(), clocks[i] / (float)CLOCK_MAX));
            }
        }
    }
}
