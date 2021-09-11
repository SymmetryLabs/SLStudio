package com.symmetrylabs.shows.firefly;

import art.lookingup.KaledoscopeModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.DiscreteParameter;

public class BFStrand extends SLPattern {
    public static final String GROUP_NAME = FireflyShow.SHOW_NAME;

    DiscreteParameter strandNum = new DiscreteParameter("strand", 0, 0, 10);
    public BFStrand(LX lx) {
        super(lx);
        addParameter("strand", strandNum);
    }

    @Override
    protected void run(double deltaMs) {
        for (LXPoint p : model.points) {
            colors[p.index] = LXColor.rgb(0, 0, 0);
        }
        for (LXPoint p : KaledoscopeModel.allStrands.get(strandNum.getValuei()).allPoints) {
            colors[p.index] = LXColor.rgb(255, 255, 255);
        }
    }
}
