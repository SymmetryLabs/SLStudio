package com.symmetrylabs.slstudio.pattern.tree;

import heronarts.lx.LX;
import heronarts.lx.LXUtils;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.modulator.DampedParameter;

import com.symmetrylabs.layouts.tree.TreeModel;
import static com.symmetrylabs.util.MathConstants.*;
import static com.symmetrylabs.util.MathUtils.*;

public class ColorLighthouse extends TreePattern {
    public String getAuthor() {
        return "Mark C. Slee";
    }

    public final CompoundParameter speed = (CompoundParameter)
        new CompoundParameter("Speed", 17000, 19000, 5000)
            .setExponent(2)
            .setDescription("Speed of lighthouse motion");

    public final CompoundParameter spread = new CompoundParameter("Spread", 80, 360)
        .setDescription("Spread of lighthouse gradient");

    public final CompoundParameter slope = new CompoundParameter("Slope", 0, -1, 1)
        .setDescription("Slope of gradient");

    private final LXModulator spreadDamped = startModulator(new DampedParameter(this.spread, 360, 540, 270));
    private final LXModulator slopeDamped = startModulator(new DampedParameter(this.slope, 2, 4, 2));

    private final LXModulator azimuth = startModulator(new SawLFO(0, TWO_PI, speed));

    public ColorLighthouse(LX lx) {
        super(lx);
        addParameter("speed", this.speed);
        addParameter("spread", this.spread);
        addParameter("slope", this.slope);
    }

    public void run(double deltaMs) {
        float hue = palette.getHuef();
        float sat = palette.getSaturationf();
        float azimuth = this.azimuth.getValuef();
        float spread = this.spreadDamped.getValuef() / PI;
        float slope = PI * this.slopeDamped.getValuef();
        for (TreeModel.Leaf leaf : tree.getLeaves()) {
            float az = (TWO_PI + leaf.point.azimuth + abs(leaf.point.yn - 0.5f) * slope) % TWO_PI;
            float d = LXUtils.wrapdistf(az, azimuth, TWO_PI);
            setColor(leaf, LXColor.hsb(
                hue + spread * d,
                sat,
                100
            ));
        }
    }
}
