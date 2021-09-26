package com.symmetrylabs.slstudio.pattern;

import java.util.List;
import java.util.Arrays;
import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.model.LXPoint;
import heronarts.lx.model.LXFixture;
import heronarts.lx.color.LXColor;
import heronarts.lx.color.ColorParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.EnumParameter;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;

// based on https://github.com/ncase/fireflies
public class FirefliesPattern extends SLPattern<SLModel> {
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
    private boolean[] fired;
    private long[] flashStarts;

    public FirefliesPattern(LX lx) {
        super(lx);

        clocks = new int[getSubmodelsSize()];
        fired = new boolean[clocks.length];
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
        addParameter(syncParam = new BooleanParameter("Sync", false));
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
            fired[i] = false;
        }
    }

    protected int getSubmodelsSize() {
        return model.getPoints().size();
    }
    protected void setSubmodelColor(int i, int c) {
        setColor(i, c);
    }
    protected double calcSubmodelEffect(int i, int j, double r) {
        LXPoint pi = model.getPoints().get(i);
        LXPoint pj = model.getPoints().get(j);
        float dx = pi.x - pj.x;
        float dy = pi.y - pj.y;
        float dz = pi.z - pj.z;
        //return (dx * dx + dy * dy + dz * dz) > (r * r) ? -1 : 1;
        return -(dx * dx + dy * dy + dz * dz) / (r * r) + 1;
    }

    @Override
    protected void run(double deltaMs) {
        long timerMillis = System.nanoTime() / 1000000;
        double flashDur = durationParam.getValue() * 1000;

        //long startTime = System.nanoTime();
        //long endTime = 0;
        int size = getSubmodelsSize();
        Arrays.fill(fired, false);
        for (int i = 0; i < size; ++i) {
            clocks[i] += noiseParam.getValue() * (Math.random() * 2 - 1) * CLOCK_MAX;
            clocks[i] += speedParam.getValue() * deltaMs;
            if (clocks[i] > CLOCK_MAX) {
                flashStarts[i] = timerMillis;
                clocks[i] = 0;
                fired[i] = true;
            }
        }

        if (syncParam.isOn()) {
            double r = radiusParam.getValue();
            int step = 100;
            int start = (int)(Math.random() * step);
            if (size < 1000) {
                step = 1;
                start = 0;
            }
            for (int i = start; i < size; i += step) {
                if (!fired[i])
                    continue;

                // hack...
                LXPoint pi = model.getPoints().get(i);
                List<LXPoint> nearbyPoints = getModel().getModelIndex()
                        .pointsWithin(pi, (float)(radiusParam.getValue() / 10));

                //endTime = System.nanoTime();
                //System.out.println((endTime - startTime) / 1000000f + ", " + nearbyPoints.size());
                for (LXPoint pj : nearbyPoints) {
                    int j = pj.index;
                    if (i == j || fired[j])
                        continue;

                    double f = calcSubmodelEffect(i, j, r);
                    if (f > 0) {
                        clocks[j] *= 1 + nudgeParam.getValue() * f;
                    }
                }
                //endTime = System.nanoTime();
                //System.out.println((endTime - startTime) / 1000000f);
            }
        }
        //endTime = System.nanoTime();
        //System.out.println((endTime - startTime) / 1000000f);

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
