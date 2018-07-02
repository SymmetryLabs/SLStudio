package com.symmetrylabs.slstudio.pattern.tree;

import heronarts.lx.LX;
import heronarts.lx.LXUtils;
import heronarts.lx.color.LXColor;
import heronarts.lx.LXPattern;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.parameter.CompoundParameter;

import com.symmetrylabs.layouts.tree.TreeModel;
import static com.symmetrylabs.util.MathConstants.*;
import static com.symmetrylabs.util.MathUtils.*;


public class TreeTumbler extends TreePattern {

    public String getAuthor() {
        return "Mark C. Slee";
    }

    private LXModulator azimuthRotation = startModulator(new SawLFO(0, 1, 15000).randomBasis());
    private LXModulator thetaRotation = startModulator(new SawLFO(0, 1, 13000).randomBasis());

    public TreeTumbler(LX lx) {
        super(lx);
    }

    public void run(double deltaMs) {
        float azimuthRotation = this.azimuthRotation.getValuef();
        float thetaRotation = this.thetaRotation.getValuef();
        for (TreeModel.Leaf leaf : model.getLeaves()) {
            float tri1 = LXUtils.trif(azimuthRotation + leaf.point.azimuth / PI);
            float tri2 = LXUtils.trif(thetaRotation + (PI + leaf.point.theta) / PI);
            float tri = max(tri1, tri2);
            setColor(leaf, LXColor.gray(100 * tri * tri));
        }
    }
}
