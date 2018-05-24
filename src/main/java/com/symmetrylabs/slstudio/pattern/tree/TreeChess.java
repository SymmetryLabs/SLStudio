package com.symmetrylabs.slstudio.pattern.tree;

import com.symmetrylabs.layouts.oslo.TreeModel;
import heronarts.lx.LX;
import heronarts.lx.LXUtils;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.DampedParameter;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.parameter.CompoundParameter;

import static com.symmetrylabs.util.MathConstants.PI;
import static heronarts.lx.LX.TWO_PI;
import static java.lang.StrictMath.abs;

public class TreeChess extends TreeSpinningPattern {
    public String getAuthor() {
        return "Mark C. Slee";
    }

    public final CompoundParameter numSpots = (CompoundParameter)
        new CompoundParameter("Spots", 4, 2, 8)
            .setDescription("Number of spots");

    private final LXModulator numSpotsDamped = startModulator(new DampedParameter(numSpots, 12, 20, 6));

    public TreeChess(LX lx) {
        super(lx);
        addParameter("numSpots", this.numSpots);
    }

    public void run(double deltaMs) {
        float azimuth = this.azimuth.getValuef();
        float numSpots = this.numSpotsDamped.getValuef();
        for (TreeModel.LeafAssemblage assemblage : model.assemblages) {
            LXPoint p = assemblage.points[0];
            float az = p.azimuth + azimuth;
            if (az > TWO_PI) {
                az -= TWO_PI;
            }
            float d = LXUtils.wrapdistf(az, 0, (float) TWO_PI);
            d = abs(d - PI) / PI;
            int add = ((int) (numSpots * p.yn)) % 2;
            float basis = (numSpots * d + .5f * add) % 1f;
            float d2 = 2*abs(.5f - basis);
            setColor(assemblage, LXColor.gray(100 * (1-d2)*(1-d2)));
        }
    }
}
