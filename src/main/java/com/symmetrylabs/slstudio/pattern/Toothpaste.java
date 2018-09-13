package com.symmetrylabs.slstudio.pattern;

import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.StripsModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import com.symmetrylabs.util.MathUtils;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;

import java.util.ArrayList;
import java.util.List;

public class Toothpaste<T extends Strip> extends SLPattern<StripsModel<T>> {
    private static final int BANDS = 6;

    private static final double[] frequencies = new double[BANDS];

    static {
        for (int i = 0; i < BANDS; i++) {
            frequencies[i] = Math.pow(3, i - 2);
        }
    }

    private final List<CompoundParameter> amplitudes;
    private final List<CompoundParameter> phaseShifts;

    public Toothpaste(LX lx) {
        super(lx);

        amplitudes = new ArrayList<>(BANDS);
        phaseShifts = new ArrayList<>(BANDS);
        for (int i = 0; i < BANDS; i++) {
            CompoundParameter a = new CompoundParameter(String.format("A%d", i), 0, -1, 1);
            CompoundParameter ps = new CompoundParameter(String.format("P%d", i), 0, -1, 1);
            addParameter(a);
            addParameter(ps);
            amplitudes.add(a);
            phaseShifts.add(ps);
        }
    }

    @Override
    public void run(double elapsedMs) {
        double[] amp = new double[BANDS];
        double[] ps = new double[BANDS];
        for (int i = 0; i < BANDS; i++) {
            amp[i] = amplitudes.get(i).getValue();
            ps[i] = phaseShifts.get(i).getValue();
        }

        final int nStrips = model.getStrips().size();
        for (int stripIndex = 0; stripIndex < nStrips; stripIndex++) {
            Strip s = model.getStripByIndex(stripIndex);
            int i = 0;
            int n = s.size;
            for (LXPoint v : s.points) {
                double t = Math.abs(2 * (i / (n - 1.0) - 0.5));
                double sig = 0;
                for (int b = 0; b < BANDS; b++) {
                    sig += amp[b] * Math.cos((t + ps[b]) * frequencies[b]);
                }
                sig = MathUtils.constrain((sig + 0.5) / 2.0, 0, 1);
                colors[v.index] = LXColor.gray(100 * sig);
                i++;
            }
        }
    }
}
