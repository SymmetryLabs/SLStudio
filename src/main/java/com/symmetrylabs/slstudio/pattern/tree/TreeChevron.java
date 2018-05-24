package com.symmetrylabs.slstudio.pattern.tree;

import com.symmetrylabs.layouts.oslo.TreeModel;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.DampedParameter;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.parameter.CompoundParameter;

import static com.jogamp.opengl.math.FloatUtil.QUARTER_PI;
import static com.symmetrylabs.util.MathConstants.HALF_PI;
import static com.symmetrylabs.util.MathConstants.PI;
import static heronarts.lx.LX.TWO_PI;
import static java.lang.Math.max;
import static java.lang.StrictMath.abs;

public class TreeChevron extends TreeSpinningPattern {
    public String getAuthor() {
        return "Mark C. Slee";
    }

    public final CompoundParameter slope =
        new CompoundParameter("Slope", 0, -HALF_PI, HALF_PI)
            .setDescription("Slope of the chevron shape");

    public final CompoundParameter sharp =
        new CompoundParameter("Sharp", 200, 100, 800)
            .setDescription("Sharpness of the lines");

    private final LXModulator slopeDamped = startModulator(new DampedParameter(this.slope, PI, TWO_PI, PI));
    private final LXModulator sharpDamped = startModulator(new DampedParameter(this.sharp, 300, 400, 200));

    public TreeChevron(LX lx) {
        super(lx);
        addParameter("slope", this.slope);
        addParameter("sharp", this.sharp);
    }

    public void run(double deltaMs) {
        float azimuth = this.azimuth.getValuef();
        float slope = this.slopeDamped.getValuef();
        float sharp = this.sharpDamped.getValuef();
        for (TreeModel.LeafAssemblage assemblage : model.assemblages) {
            LXPoint p = assemblage.points[0];
            float az = (float) (TWO_PI + p.azimuth + azimuth + abs(p.yn - .5) * slope) % QUARTER_PI;
            setColor(assemblage, LXColor.gray(max(0, 100 - sharp * abs(az - PI/8.))));
        }
    }
}


