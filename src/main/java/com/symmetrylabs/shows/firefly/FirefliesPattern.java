package com.symmetrylabs.shows.firefly;

import java.util.List;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.color.LXColor;
import heronarts.lx.color.ColorParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.EnumParameter;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import art.lookingup.KaledoscopeModel;
import art.lookingup.LUButterfly;
import art.lookingup.LUFlower;

// based on https://github.com/ncase/fireflies
public class FirefliesPattern extends SLPattern<KaledoscopeModel> {
    public static final String GROUP_NAME = FireflyShow.SHOW_NAME;

    public static enum FlashMode { FLASH, CLOCK };

    public final ColorParameter colorParam;
    public final BooleanParameter usePaletteParam;
    public final CompoundParameter speedParam;
    public final BooleanParameter syncParam;
    public final CompoundParameter radiusParam;
    public final CompoundParameter nudgeParam;
    public final EnumParameter flashModeParam;
    public final CompoundParameter durationParam;
    public final BooleanParameter resetParam;
    public final CompoundParameter noiseParam;
    public final BooleanParameter enableButterfliesParam;
    public final BooleanParameter enableFlowersParam;

    private static final int CLOCK_MAX = 1000;

    private int[] clocks;
    private long[] flashStarts;

    public FirefliesPattern(LX lx) {
        super(lx);

        double r = Math.sqrt(lx.model.xRange * lx.model.xRange
                                + lx.model.yRange * lx.model.yRange
                                + lx.model.zRange * lx.model.zRange);

        addParameter(colorParam = new ColorParameter("Color", LXColor.WHITE));
        addParameter(usePaletteParam = new BooleanParameter("UsePalette"));
        addParameter(speedParam = new CompoundParameter("Speed", 0.5, 0, 2));
        addParameter(syncParam = new BooleanParameter("EnableSync", false));
        addParameter(radiusParam = new CompoundParameter("Radius", r / 15, r / 1000, r));
        addParameter(nudgeParam = new CompoundParameter("Nudge", 0.035, 0.01, 0.1));
        addParameter(flashModeParam = new EnumParameter<FlashMode>("FlashMode", FlashMode.FLASH));
        addParameter(durationParam = new CompoundParameter("Duration", 0.75, 0, 2));
        addParameter(resetParam = new BooleanParameter("Reset").setMode(BooleanParameter.Mode.MOMENTARY));
        addParameter(noiseParam = new CompoundParameter("Noise", 0.01, 0, 0.05));

        addParameter(enableButterfliesParam = new BooleanParameter("EnableButterflies", true));
        addParameter(enableFlowersParam = new BooleanParameter("EnableFlowers", true));

        resetParam.addListener((p) -> resetClocks());

        usePaletteParam.addListener((p) -> {
            colorParam.setColor(lx.palette.color.getColor());
        });
        colorParam.addListener((p) -> {
            if (colorParam.getColor() != lx.palette.color.getColor()) {
                usePaletteParam.setValue(false);
            }
        });
        lx.palette.color.addListener((p) -> {
            if (usePaletteParam.isOn()) {
                colorParam.setColor(lx.palette.color.getColor());
            }
        });

        clocks = new int[KaledoscopeModel.allButterflies.size() + KaledoscopeModel.allFlowers.size()];
        flashStarts = new long[clocks.length];

        resetClocks();
    }

    private void resetClocks() {
        for (int i = 0; i < clocks.length; ++i) {
            clocks[i] = (int)(Math.random() * CLOCK_MAX);
            flashStarts[i] = 0;
        }
    }

    @Override
    protected void run(double deltaMs) {
        long timerMillis = System.nanoTime() / 1000000;
        double flashDur = durationParam.getValue() * 1000;

        int flowersStart = KaledoscopeModel.allButterflies.size();
        int flowersEnd = flowersStart + KaledoscopeModel.allFlowers.size();

        for (int i = 0; i < flowersEnd; ++i) {
            clocks[i] += noiseParam.getValue() * (Math.random() * 2 - 1) * CLOCK_MAX;
            clocks[i] += speedParam.getValue() * deltaMs;
            if (clocks[i] > CLOCK_MAX) {
                flashStarts[i] = timerMillis;
                clocks[i] = 0;

                if (syncParam.isOn()) {
                    LUButterfly bi = i < flowersStart ? KaledoscopeModel.allButterflies.get(i) : null;
                    LUFlower fi = i >= flowersStart ? KaledoscopeModel.allFlowers.get(i - flowersStart) : null;
                    for (int j = 0; j < flowersEnd; ++j) {
                        LUButterfly bj = j < flowersStart ? KaledoscopeModel.allButterflies.get(j) : null;
                        LUFlower fj = j >= flowersStart ? KaledoscopeModel.allFlowers.get(j - flowersStart) : null;

                        if (i == j)
                            continue;

                        float dx = (bi == null ? fi.x : bi.x) - (bj == null ? fj.x : bj.x);
                        float dy = (bi == null ? fi.y : bi.y) - (bj == null ? fj.y : bj.y);
                        float dz = (bi == null ? fi.z : bi.z) - (bj == null ? fj.z : bj.z);
                        double d = Math.sqrt(dx * dx + dy * dy + dz * dz);
                        double p = -d / radiusParam.getValue() + 1;

                        if (p > 0) {
                            clocks[j] *= 1 + nudgeParam.getValue() * p;
                        }
                    }
                }
            }
        }

        setColors(LXColor.BLACK);

        for (int i = 0; i < flowersEnd; ++i) {
            List<LXPoint> points = null;
            if (enableButterfliesParam.isOn() && i < flowersStart) {
                points = KaledoscopeModel.allButterflies.get(i).allPoints;
            }
            if (enableFlowersParam.isOn() && i >= flowersStart) {
                points = KaledoscopeModel.allFlowers.get(i - flowersStart).allPoints;
            }

            if (points == null)
                continue;

            long td = timerMillis - flashStarts[i];
            if (td > 0 && td <= flashDur) {
                for (LXPoint p : points) {
                    colors[p.index] = colorParam.getColor();
                }
            }
            else if (flashModeParam.getEnum() == FlashMode.CLOCK) {
                for (LXPoint p : points) {
                    colors[p.index] = LXColor.scaleBrightness(colorParam.getColor(), clocks[i] / (float)CLOCK_MAX);
                }
            }
        }
    }
}
