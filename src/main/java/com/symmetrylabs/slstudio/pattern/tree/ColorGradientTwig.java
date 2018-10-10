package com.symmetrylabs.slstudio.pattern.tree;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.modulator.DampedParameter;

import com.symmetrylabs.shows.tree.TreeModel;


public class ColorGradientTwig extends TreePattern {
    public String getAuthor() {
        return "Mark C. Slee";
    }

    public final CompoundParameter speed = (CompoundParameter)
        new CompoundParameter("Speed", 8000, 15000, 1000)
            .setExponent(.5)
            .setDescription("Speed of hue motion thru the assemblage");

    private final LXModulator spread = startModulator(new DampedParameter(palette.spread, 360, 540));

    private final int[] rawGradient = new int[TreeModel.Twig.NUM_LEAVES];
    private final int[] twigGradient = new int[TreeModel.Twig.NUM_LEAVES];

    public final LXModulator offset = startModulator(new SawLFO(0, TreeModel.Twig.NUM_LEAVES, speed));

    public ColorGradientTwig(LX lx) {
        super(lx);
        addParameter("speed", this.speed);
    }

    public void run(double deltaMs) {
        float hue = palette.getHuef();
        float sat = palette.getSaturationf();
        float spread = this.spread.getValuef() / TreeModel.Twig.NUM_LEAVES;
        float offset = this.offset.getValuef();
        float offsetLerp = offset % 1;
        int offsetFloor = (int) offset;
        for (int i = 0; i < TreeModel.Twig.NUM_LEAVES; ++i) {
            this.rawGradient[i] = LXColor.hsb(hue + spread * i, sat, 100);
        }
        for (int i = 0; i < TreeModel.Twig.NUM_LEAVES; ++i) {
            int i1 = (i + offsetFloor) % TreeModel.Twig.NUM_LEAVES;
            int i2 = (i + offsetFloor + 1) % TreeModel.Twig.NUM_LEAVES;
            this.twigGradient[i] = LXColor.lerp(this.rawGradient[i1], this.rawGradient[i2], offsetLerp);
        }
        for (TreeModel.Twig twig :tree.getTwigs()) {
            int li = 0;
            for (TreeModel.Leaf leaf : twig.getLeaves()) {
                setColor(leaf, this.twigGradient[li++]);
            }
        }
    }
}
