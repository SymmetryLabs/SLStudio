package com.symmetrylabs.slstudio.pattern.tree;

import com.symmetrylabs.layouts.oslo.TreeModel;
import com.symmetrylabs.slstudio.pattern.base.TreePattern;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;

import static com.jogamp.opengl.math.FloatUtil.QUARTER_PI;
import static com.symmetrylabs.util.MathConstants.PI;
import static com.symmetrylabs.util.MathUtils.abs;
import static com.symmetrylabs.util.MathUtils.max;
import static heronarts.lx.LX.TWO_PI;

public class TreeGentleSpin extends TreeSpinningPattern {
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
            float az = (p.azimuth + azimuth + abs(p.yn - .5f) * QUARTER_PI) % (float) TWO_PI;
            setColor(assemblage, LXColor.gray(max(0, 100 - 40 * abs(az - PI))));
        }
    }
}
