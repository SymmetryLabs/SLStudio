package com.symmetrylabs.slstudio.pattern;

import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.transform.LXVector;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Crystalline extends SLPattern<SLModel> {
    private static final int CANDIDATE_COUNT = 500;

    private static class DirS2 {
        final LXVector n;
        final float lat;
        final float lon;
        float d;
        /* ranges from 0 to 1, identifies a point in the interval [vmin, vmax] */
        float v;

        DirS2(float lat, float lon, float v) {
            this.lat = lat;
            this.lon = lon;
            this.d = 0;
            this.v = v;
            this.n = new LXVector(
                (float) (Math.sin(lat) * Math.cos(lon)),
                (float) (Math.sin(lat) * Math.sin(lon)),
                (float) Math.cos(lat));
        }

        float angDist(DirS2 p) {
            /* this is the formula for great-circle distance */
            return (float) Math.acos(
                Math.sin(lat) * Math.sin(p.lat) + Math.cos(lon) * Math.cos(p.lon) * Math.cos(Math.abs(lon - p.lon)));
        }
    }

    private final DiscreteParameter count = new DiscreteParameter("count", 3, 0, 40);
    private final CompoundParameter width = new CompoundParameter("width", 24, 300);
    private final CompoundParameter cx = new CompoundParameter("cx", model.cx, model.xMin, model.xMax);
    private final CompoundParameter cy = new CompoundParameter("cy", model.cy, model.yMin, model.yMax);
    private final CompoundParameter cz = new CompoundParameter("cz", model.cz, model.zMin, model.zMax);
    private final CompoundParameter vmin = new CompoundParameter("vmin", 0.01, 0, 0.5);
    private final CompoundParameter vmax = new CompoundParameter("vmax", 0.05, 0, 0.5);
    private final CompoundParameter cutWhite = new CompoundParameter("cutwhite", 0.1, 0, 1);
    private final CompoundParameter cutBlack = new CompoundParameter("cutblack", 0.4, 0, 1);
    private final BooleanParameter reset = new BooleanParameter("reset", false).setMode(BooleanParameter.Mode.MOMENTARY);
    private final Random random = new Random();
    private final List<DirS2> dirs = new ArrayList<>();

    public Crystalline(LX lx) {
        super(lx);
        addParameter(count);
        addParameter(width);
        addParameter(cx);
        addParameter(cy);
        addParameter(cz);
        addParameter(vmin);
        addParameter(vmax);
        addParameter(cutWhite);
        addParameter(cutBlack);
        addParameter(reset);
        reset.setShouldSerialize(false);
        refillDirs();
    }

    @Override
    public void onParameterChanged(LXParameter p) {
        if (p == count) {
            refillDirs();
        } else if (p == reset) {
            dirs.clear();
            refillDirs();
        }
    }

    private void refillDirs() {
        int c = count.getValuei();
        if (dirs.size() > c) {
            dirs.subList(c, dirs.size()).clear();
        } else {
            while (dirs.size() < c) {
                dirs.add(sampleDir());
            }
        }
    }

    private DirS2 sampleDir() {
        List<DirS2> candidates = new ArrayList<>();
        for (int i = 0; i < CANDIDATE_COUNT; i++) {
            candidates.add(new DirS2(180f * random.nextFloat(), 360f * random.nextFloat(), random.nextFloat()));
        }
        float maxDist = 0;
        DirS2 best = candidates.get(0);
        for (DirS2 candidate : candidates) {
            for (DirS2 dir : dirs) {
                float dist = candidate.angDist(dir);
                if (dist > maxDist) {
                    best = candidate;
                    maxDist = dist;
                }
            }
        }
        return best;
    }

    @Override
    public void run(double elapsedMs) {
        final float velmin = vmin.getValuef();
        final float velmax = vmax.getValuef();
        for (DirS2 dir : dirs) {
            float v = dir.v * (velmax - velmin) + velmin;
            dir.d = (float) (dir.d + v * elapsedMs / 1000f);
        }

        final float w = width.getValuef();
        final int on = LXColor.WHITE;
        final int off = 0;
        Arrays.fill(colors, off);

        final float cw = cutWhite.getValuef();
        final float cb = cutBlack.getValuef();

        LXVector negCenter = new LXVector(-cx.getValuef(), -cy.getValuef(), -cz.getValuef());
        for (LXVector v : getVectors()) {
            v = v.copy().add(negCenter);
            boolean flip = false;
            float min = 1;
            for (DirS2 d : dirs) {
                float proj = v.dot(d.n) + w * d.d;
                proj = (proj % w) / w + (proj < 0 ? 1 : 0);
                min = Float.min(min, proj);
                if (proj < 0.5) {
                    flip = !flip;
                }
            }
            if (!flip) {
                colors[v.index] = off;
            } else {
                float x = 2 * min;
                if (x < cw) {
                    colors[v.index] = on;
                } else {
                    float g = 1f - (x - cw) / (1 - cw);
                    colors[v.index] = LXColor.gray(100 * g);
                }
            }
        }
    }
}
