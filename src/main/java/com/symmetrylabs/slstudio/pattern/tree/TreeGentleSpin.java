package com.symmetrylabs.slstudio.pattern.tree;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.color.LXColor;

import com.symmetrylabs.shows.kalpa.TreeModel;
import static com.symmetrylabs.util.MathUtils.*;
import static com.symmetrylabs.util.MathConstants.*;


public class TreeGentleSpin extends TreeSpinningPattern {
    public String getAuthor() {
        return "Mark C. Slee";
    }

    public TreeGentleSpin(LX lx) {
        super(lx);
    }

    public void run(double deltaMs) {
        float azimuth = this.azimuth.getValuef();
        for (TreeModel.Twig twig : model.getTwigs()) {
            LXPoint p = twig.points[0];
            float az = (p.azimuth + azimuth + abs(p.yn - 0.5f) * QUARTER_PI) % TWO_PI;
            setColor(twig, LXColor.gray(max(0, 100 - 40 * abs(az - PI))));
        }
    }
}
