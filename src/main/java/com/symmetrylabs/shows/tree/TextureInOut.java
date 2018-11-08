package com.symmetrylabs.shows.tree;

import heronarts.lx.LX;

import com.symmetrylabs.slstudio.pattern.tree.TexturePattern;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.FunctionalParameter;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.LXUtils;
import heronarts.lx.color.LXColor;

import static com.symmetrylabs.util.MathUtils.*;


public class TextureInOut extends TexturePattern {
    public String getAuthor() {
        return "Mark C. Slee";
    }

    public final CompoundParameter speed = (CompoundParameter)
        new CompoundParameter("Speed", 1000, 5000, 200)
        .setExponent(.5)
        .setDescription("Speed of the motion");

    public final CompoundParameter size = (CompoundParameter)
        new CompoundParameter("Size", 2, 1, 4)
        .setDescription("Size of the streak");

    private final LXModulator[] leaves = new LXModulator[TreeModel.Twig.NUM_LEAVES];
    private final int[] twigMask = new int[TreeModel.Twig.NUM_LEDS];

    public TextureInOut(LX lx) {
        super(lx);
        addParameter("speed", this.speed);
        addParameter("size", this.size);
        for (int i = 0; i < this.leaves.length; ++i) {
            final int ii = i;
            this.leaves[i] = startModulator(new SinLFO(0, (TreeModel.Leaf.NUM_LEDS-1)/2., new FunctionalParameter() {
                public double getValue() {
                    return speed.getValue() * (1 + .05 * ii);
                }
            }).randomBasis());
        }
    }
}
