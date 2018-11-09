package com.symmetrylabs.slstudio.pattern.tree;

import com.symmetrylabs.shows.tree.TreeModel.*;
import heronarts.lx.LX;
import heronarts.lx.audio.GraphicMeter;
import heronarts.lx.audio.LXAudioEngine;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.EnumParameter;
import heronarts.lx.parameter.LXParameter;
import java.util.HashMap;
import java.util.List;

public class TreeEq extends TreePattern {
    private double t = 0;
    private final GraphicMeter eq =
        new GraphicMeter(lx.engine.audio.input.mix, model.getBranches().size());

    private final CompoundParameter attack = new CompoundParameter("Attk", 0.73);
    private final CompoundParameter gain = new CompoundParameter("Gain", 0.65);
    private final CompoundParameter range = new CompoundParameter("Range", 0.2);
    private final CompoundParameter release = new CompoundParameter("Rls", 0.33);
    private final CompoundParameter slope = new CompoundParameter("Slope", 0.5);
    private final CompoundParameter spin = new CompoundParameter("Spin", 5, 0, 20);
    public final EnumParameter<LXAudioEngine.Mode> mode = new EnumParameter<LXAudioEngine.Mode>("Mode", LXAudioEngine.Mode.INPUT);

    private final HashMap<Branch, Double> maxDists = new HashMap<>();

    private double offset;

    public TreeEq(LX lx) {
        super(lx);
        eq.start();
        addParameter(gain);
        addParameter(range);
        addParameter(attack);
        addParameter(release);
        addParameter(slope);
        addParameter(spin);
        addParameter(mode);
        startModulator(eq);

        for (Branch b : model.getBranches()) {
            double maxDist = 0;
            for (LXPoint p : b.points) {
                maxDist = Math.max(maxDist, dist(b, p));
            }
            maxDists.put(b, maxDist);
        }
    }

    @Override
    public void onParameterChanged(LXParameter p) {
        if (p == mode) {
            if (mode.getEnum() == LXAudioEngine.Mode.INPUT) {
                eq.setBuffer(lx.engine.audio.input.mix);
            } else {
                eq.setBuffer(lx.engine.audio.output.mix);
            }
        }
    }

    @Override
    public void run(double elapsedMs) {
        eq.gain.setNormalized(gain.getValuef());
        eq.range.setNormalized(range.getValuef());
        eq.attack.setNormalized(attack.getValuef());
        eq.release.setNormalized(release.getValuef());
        eq.slope.setNormalized(release.getValuef());

        List<Branch> branches = model.getBranches();
        offset = (offset + spin.getValue() * elapsedMs / 1000) % branches.size();

        final double band2w = offset % 1.;
        for (int i = 0; i < branches.size(); i++) {
            final Branch b = branches.get(i);
            final int band1 = (i + (int) offset) % branches.size();
            final int band2 = (band1 + 1) % branches.size();
            final double lvl = eq.getBand(band1) * (1 - band2w) + eq.getBand(band2) * band2w;

            final int c = LXColor.gray(100.f * lvl);
            final double maxDist = maxDists.get(b);
            for (LXPoint p : b.points) {
                colors[p.index] = dist(b, p) / maxDist <= lvl ? c : 0;
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
