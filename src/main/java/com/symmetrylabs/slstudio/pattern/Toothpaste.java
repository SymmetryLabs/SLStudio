package com.symmetrylabs.slstudio.pattern;

import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.StripsModel;
import com.symmetrylabs.slstudio.model.StripsTopology;
import com.symmetrylabs.slstudio.model.StripsTopology.Dir;
import com.symmetrylabs.slstudio.model.StripsTopology.Bundle;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import com.symmetrylabs.util.MathUtils;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.transform.LXVector;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Toothpaste<T extends Strip> extends SLPattern<StripsModel<T>> {
    private static final int MAX_BANDS = 16;

    private static final double[] frequencies = new double[MAX_BANDS];
    static {
        for (int i = 0; i < MAX_BANDS; i++) {
            frequencies[i] = Math.pow(2, i - 2);
        }
    }

    private final DiscreteParameter bands = new DiscreteParameter("bands", 1, 1, MAX_BANDS);
    private final CompoundParameter rate = new CompoundParameter("rate", 0.01, 0.05);
    private final CompoundParameter flip = new CompoundParameter("flip", 1000, 5000);
    private Random random = new Random();

    private double[] ampx = new double[MAX_BANDS];
    private double[] ampy = new double[MAX_BANDS];
    private double[] ampz = new double[MAX_BANDS];

    private Dir[] stripDirs;

    private int changex;
    private int changey;
    private int changez;
    private float dir;
    private double accumT;

    public Toothpaste(LX lx) {
        super(lx);
        addParameter(rate);
        addParameter(flip);
        addParameter(bands);

        Arrays.fill(ampx, 0);
        Arrays.fill(ampy, 0);
        Arrays.fill(ampz, 0);

        ampx[0] = 1;
        ampy[1] = 1;
        ampz[2] = 1;
        accumT = 0;

        int nStrips = model.getStrips().size();
        stripDirs = new Dir[nStrips];
        /* if we don't have topology for the model, we pick "random" directions
         * for each strip to break them up a little. */
        if (model.getTopology() == null) {
            List<Dir> dirs = Arrays.asList(Dir.values());
            Collections.shuffle(dirs);
            for (int i = 0; i < nStrips; i++) {
                stripDirs[i] = dirs.get(i % dirs.size());
            }
        } else {
            StripsTopology topo = model.getTopology();
            for (Bundle b : topo.bundles) {
                for (int stripIndex : b.strips) {
                    stripDirs[stripIndex] = b.dir;
                }
            }
        }

        flip();
    }

    private void flip() {
        changex = random.nextInt(bands.getValuei());
        changey = random.nextInt(bands.getValuei());
        changez = random.nextInt(bands.getValuei());
        dir = random.nextInt(2) == 0 ? -1 : 1;
    }

    @Override
    public void onParameterChanged(LXParameter p) {
        if (p == bands) {
            for (int i = bands.getValuei(); i < MAX_BANDS; i++) {
                ampx[i] = 0;
                ampy[i] = 0;
                ampz[i] = 0;
            }
        }
    }

    @Override
    public void run(double elapsedMs) {
        int bn = bands.getValuei();

        accumT += elapsedMs;
        if (accumT > flip.getValue()) {
            flip();
        }

        double r = rate.getValue();
        ampx[changex] = MathUtils.constrain(
            ampx[changex] + dir * elapsedMs * r * random.nextGaussian(), -1, 1);
        ampy[changey] = MathUtils.constrain(
            ampy[changey] + dir * elapsedMs * r * random.nextGaussian(), -1, 1);
        ampz[changez] = MathUtils.constrain(
            ampz[changez] + dir * elapsedMs * r * random.nextGaussian(), -1, 1);

        double scalex = 0;
        double scaley = 0;
        double scalez = 0;
        for (double a : ampx) {
            scalex += a;
        }
        for (double a : ampy) {
            scaley += a;
        }
        for (double a : ampz) {
            scalez += a;
        }

        final int nStrips = model.getStrips().size();
        for (int stripIndex = 0; stripIndex < nStrips; stripIndex++) {
            Strip s = model.getStripByIndex(stripIndex);
            double[] amps = ampx;
            double scale = scalex;
            /*
            switch (stripDirs[stripIndex]) {
                case X:
                    amps = ampx;
                    scale = scalex;
                    break;
                case Y:
                    amps = ampy;
                    scale = scaley;
                    break;
                case Z:
                default:
                    amps = ampz;
                    scale = scalez;
                    break;
            }
            */
            int i = 0;
            int n = s.size;
            for (LXPoint v : s.points) {
                double t = 2 * (i / (n - 1.0) - 0.5);
                double sig = 0;
                for (int b = 0; b < bn; b++) {
                    sig += amps[b] * Math.cos(t * frequencies[b]);
                }
                sig = (sig / scale + 0.5) / 2.0;
                colors[v.index] = LXColor.rgba(255, 255, 255, (int) Math.ceil(255 * sig));
                i++;
            }
        }
    }
}
