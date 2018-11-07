package com.symmetrylabs.slstudio.pattern.tree;

import com.symmetrylabs.layouts.tree.TreeModel.*;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.LXRangeModulator;
import heronarts.lx.modulator.QuadraticEnvelope;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXParameter;
import java.util.ArrayList;
import java.util.HashMap;

public class BranchFlow extends TreePattern {
    private class Cue {
        Branch branch;
        LXParameter waitBefore;
        LXRangeModulator mod;
        float maxDist;
    }

    private class Cursor {
        int nextIndex;
        double waitingFor;
    }

    private final ArrayList<Cue> cues = new ArrayList<>();
    private final ArrayList<Cursor> cursors = new ArrayList<>();

    private final CompoundParameter period =
        new CompoundParameter("period", 5000, 1, 10000);
    private final CompoundParameter limbTrig =
        new CompoundParameter("limb", 1000, 1, 10000);
    private final CompoundParameter branchTrig =
        new CompoundParameter("branch", 300, 1, 10000);
    private final CompoundParameter fadeIn =
        new CompoundParameter("in", 0.2, 0, 0.5);
    private final CompoundParameter fadeOut =
        new CompoundParameter("out", 0.3, 0, 0.5);
    private final DiscreteParameter cursorCount =
        new DiscreteParameter("count", 2, 1, 4);

    private double t = 0;

    public BranchFlow(LX lx) {
        super(lx);
        addParameter(period);
        addParameter(fadeIn);
        addParameter(fadeOut);
        addParameter(limbTrig);
        addParameter(branchTrig);
        addParameter(cursorCount);

        for (Limb l : tree.limbs) {
            boolean first = true;
            for (Branch b : l.getBranches()) {
                Cue c = new Cue();
                c.mod = new QuadraticEnvelope(0, 1, period);
                addModulator(c.mod);
                c.branch = b;
                c.waitBefore = first ? limbTrig : branchTrig;
                c.maxDist = 0;
                for (LXPoint p : b.points) {
                    c.maxDist = Math.max(c.maxDist, dist(b, p));
                }
                first = false;
                cues.add(c);
            }
        }

        loadCursors();
    }

    @Override
    public void onParameterChanged(LXParameter p) {
        if (p == cursorCount) {
            loadCursors();
        }
    }

    private void loadCursors() {
        cursors.clear();
        int n = cursorCount.getValuei();
        for (int i = 0; i < n; i++) {
            Cursor c = new Cursor();
            c.nextIndex = i * cues.size() / n;
            c.waitingFor = 0;
            cursors.add(c);
        }
    }

    @Override
    public void run(double elapsedMs) {
        for (Cursor c : cursors) {
            c.waitingFor += elapsedMs;
            Cue cue = cues.get(c.nextIndex);
            if (c.waitingFor > cue.waitBefore.getValue()) {
                cue.mod.start();
                c.waitingFor = 0;
                c.nextIndex = (c.nextIndex + 1) % cues.size();
            }
        }

        final float in = fadeIn.getValuef();
        final float out = fadeOut.getValuef();

        for (Cue cue : cues) {
            float progress = cue.mod.getValuef();
            for (LXPoint p : cue.branch.points) {
                float d = dist(cue.branch, p) / cue.maxDist;
                float lvl;
                if (progress < 1e-3) {
                    lvl = 0;
                } else if (progress < in) {
                    float pv = progress / in;
                    lvl = d < pv ? pv : 0.f;
                } else if (progress < 1.f - out) {
                    lvl = 1.f;
                } else {
                    float pv = (progress - 1.f + out) / out;
                    lvl = d > pv ? 1.f - (float) Math.sqrt(pv) : 0.f;
                }
                colors[p.index] = LXColor.gray(100.f * lvl);
            }
        }
    }

    private static final float dist(Branch b, LXPoint p) {
        float x = p.x - b.x;
        float y = p.y - b.y;
        float z = p.z - b.z;
        return (float) Math.sqrt(x * x + y * y + z * z);
    }
}
