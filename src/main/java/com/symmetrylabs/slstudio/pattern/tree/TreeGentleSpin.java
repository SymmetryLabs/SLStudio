package com.symmetrylabs.slstudio.pattern.tree;

import com.symmetrylabs.layouts.oslo.TreeModel;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;

public class TreeGentleSpin {
    public String getAuthor() {
        return "Mark C. Slee";
    }

    public TreeGentleSpin(LX lx) {
        super(lx);
    }

    public void run(double deltaMs) {
        float azimuth = this.azimuth.getValuef();
        for (TreeModel.LeafAssemblage assemblage : model.assemblages) {
            LXPoint p = assemblage.points[0];
            float az = (p.azimuth + azimuth + abs(p.yn - .5) * QUARTER_PI) % TWO_PI;
            setColor(assemblage, LXColor.gray(max(0, 100 - 40 * abs(az - PI))));
        }
    }
}
