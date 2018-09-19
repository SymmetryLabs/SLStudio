package com.symmetrylabs.slstudio.pattern;

import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.StripsModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import com.symmetrylabs.util.MathUtils;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.transform.LXVector;

import java.util.ArrayList;
import java.util.List;

public class Fourier<T extends Strip> extends SLPattern<StripsModel<T>> {
    private static final int BANDS = 6;

    private static final double[] frequencies = new double[BANDS];

    static {
        for (int i = 0; i < BANDS; i++) {
            frequencies[i] = Math.pow(3, i - 1);
        }
    }

    private final List<CompoundParameter> amplitudes;
    private final List<CompoundParameter> phaseShifts;

    private final CompoundParameter xDisplacementAmpl = new CompoundParameter("XDA", 0, 0, 50);
    private final CompoundParameter xDisplacementFreq = new CompoundParameter("XDF", 0, 0, 200);
    private final CompoundParameter xDisplacementRate = new CompoundParameter("XDR", 0, 0, 500);
    private double xdo = 0;

    public Fourier(LX lx) {
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
        addParameter(xDisplacementAmpl);
        addParameter(xDisplacementFreq);
        addParameter(xDisplacementRate);
    }

    @Override
    public void run(double elapsedMs) {
        xdo += elapsedMs * xDisplacementRate.getValue() / 1000f;

        double[] amp = new double[BANDS];
        double[] ps = new double[BANDS];
        for (int i = 0; i < BANDS; i++) {
            amp[i] = amplitudes.get(i).getValue();
            ps[i] = phaseShifts.get(i).getValue();
        }

        double xda = xDisplacementAmpl.getValue();
        double xdf = xDisplacementFreq.getValue();

        final int nStrips = model.getStrips().size();
        for (int stripIndex = 0; stripIndex < nStrips; stripIndex++) {
            Strip s = model.getStripByIndex(stripIndex);
            for (LXVector v : getVectors(s.points)) {
                double sig = 0;
                double xd = xda * Math.cos(v.x / xdf + xdo) + v.x;
                for (int b = 0; b < BANDS; b++) {
                    sig += amp[b] * (
                        Math.cos((xd + ps[b]) * frequencies[b])
                        + Math.cos((v.y + ps[b]) * frequencies[b])
                        + Math.cos((v.z + ps[b]) * frequencies[b]));
                }
                sig = MathUtils.constrain((sig + 0.5) / 6.0, 0, 1);
                colors[v.index] = LXColor.gray(100 * sig);
            }
        }
    }
}
