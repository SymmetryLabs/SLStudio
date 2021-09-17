package com.symmetrylabs.shows.firefly;

import art.lookingup.KaledoscopeModel;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;

public class Collision extends AnimPattern {
    public static final String GROUP_NAME = FireflyShow.SHOW_NAME;

    CompoundParameter slope = new CompoundParameter("slope", 20f, 0.01f, 60.0f);
    CompoundParameter maxValue = new CompoundParameter("maxv", 1.0, 0.0, 1.0);

    public Collision(LX lx) {
        super(lx);
        registerPhase("Move", 3.0f, 60.0f, "Start duration");
        registerPhase("Explode", 1.0f, 60.0f, "Explosion duration");
        addParameter(maxValue);
        addParameter(slope);
    }

    protected float blobPos1 = 0.0f;
    protected float blobPos2 = 1.0f;

    public void runPhase(int phaseNum, double deltaMs) {
        // Start at t = 0 and t = 1.  Move towards t=0.5.  At t=0.5, change animation phase to
        // explosion.
        for (LXPoint pt : model.points)
            colors[pt.index] = LXColor.rgba(0, 0, 0, 255);

        if (phaseNum == 0) {
            blobPos1 = (time / curPhaseDuration) / 2f - 0.01f;
            blobPos2 = 1f - (time / curPhaseDuration) / 2f + 0.01f;
            RunRender1D.renderTriangle(colors, KaledoscopeModel.allRuns.get(0), blobPos1, slope.getValuef(), maxValue.getValuef(), LXColor.Blend.ADD);
            RunRender1D.renderTriangle(colors, KaledoscopeModel.allRuns.get(0), blobPos2, slope.getValuef(), maxValue.getValuef(), LXColor.Blend.ADD);
            RunRender1D.renderTriangle(colors, KaledoscopeModel.allRuns.get(1), blobPos1, slope.getValuef(), maxValue.getValuef(), LXColor.Blend.ADD);
            RunRender1D.renderTriangle(colors, KaledoscopeModel.allRuns.get(1), blobPos2, slope.getValuef(), maxValue.getValuef(), LXColor.Blend.ADD);
        } else if (phaseNum == 1) {
            float explosionSlope = slope.getValuef() - (time/curPhaseDuration) * slope.getValuef();
            RunRender1D.renderTriangle(colors, KaledoscopeModel.allRuns.get(0), blobPos1, explosionSlope, maxValue.getValuef(), LXColor.Blend.ADD);
            RunRender1D.renderTriangle(colors, KaledoscopeModel.allRuns.get(0), blobPos2, explosionSlope, maxValue.getValuef(), LXColor.Blend.ADD);
            RunRender1D.renderTriangle(colors, KaledoscopeModel.allRuns.get(1), blobPos1, explosionSlope, maxValue.getValuef(), LXColor.Blend.ADD);
            RunRender1D.renderTriangle(colors, KaledoscopeModel.allRuns.get(1), blobPos2, explosionSlope, maxValue.getValuef(), LXColor.Blend.ADD);
        }
    }
}
