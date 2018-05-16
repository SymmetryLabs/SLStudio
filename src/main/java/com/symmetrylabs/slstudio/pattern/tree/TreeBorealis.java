package com.symmetrylabs.slstudio.pattern.tree;

import com.symmetrylabs.layouts.oslo.TreeModel;
import heronarts.lx.LX;
import heronarts.lx.LXUtils;
import heronarts.lx.color.LXColor;
import heronarts.lx.LXPattern;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.parameter.CompoundParameter;

import static com.symmetrylabs.util.NoiseUtils.noise;
import static heronarts.lx.LXUtils.constrain;

public class TreeBorealis extends TreePattern {

    public String getAuthor() {
        return "Mark C. Slee";
    }

    public final CompoundParameter speed =
        new CompoundParameter("Speed", .5, .01, 1)
            .setDescription("Speed of motion");

    public final CompoundParameter scale =
        new CompoundParameter("Scale", .5, .1, 1)
            .setDescription("Scale of lights");

    public final CompoundParameter spread =
        new CompoundParameter("Spread", 6, .1, 10)
            .setDescription("Spreading of the motion");

    public final CompoundParameter base =
        new CompoundParameter("Base", .5, .2, 1)
            .setDescription("Base brightness level");

    public final CompoundParameter contrast =
        new CompoundParameter("Contrast", 1, .5, 2)
            .setDescription("Contrast of the lights");

    public TreeBorealis(LX lx) {
        super(lx);
        addParameter("speed", this.speed);
        addParameter("scale", this.scale);
        addParameter("spread", this.spread);
        addParameter("base", this.base);
        addParameter("contrast", this.contrast);
    }

    private float yBasis = 0;

    public void run(double deltaMs) {
        this.yBasis -= deltaMs * .0005 * this.speed.getValuef();
        float scale = this.scale.getValuef();
        float spread = this.spread.getValuef();
        float base = .01 * this.base.getValuef();
        float contrast = this.contrast.getValuef();
        for (TreeModel.Leaf leaf : tree.leaves) {
            float nv = noise(
                scale * (base * leaf.point.rxz - spread * leaf.point.yn),
                leaf.point.yn + this.yBasis
            );
            setColor(leaf, LXColor.gray(constrain(contrast * (-50 + 180 * nv), 0, 100)));
        }
    }
}
