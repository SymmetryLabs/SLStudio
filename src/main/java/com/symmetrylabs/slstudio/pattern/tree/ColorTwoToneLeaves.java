package com.symmetrylabs.slstudio.pattern.tree;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.CompoundParameter;

import com.symmetrylabs.shows.tree.TreeModel;
import static com.symmetrylabs.util.MathUtils.*;


public class ColorTwoToneLeaves extends TreePattern {
    public String getAuthor() {
        return "Mark C. Slee";
    }

    public final CompoundParameter tone =
        new CompoundParameter("Hue", 0, 0, 360)
            .setDescription("Second hue to be mixed in with the first");

    public final CompoundParameter amount =
        new CompoundParameter("Amount", 0)
            .setDescription("Amount to mix in the second color tone");

    private final float[] bias = new float[model.leaves.size()];

    public ColorTwoToneLeaves(LX lx) {
        super(lx);
        addParameter("tone", this.tone);
        addParameter("amount", this.amount);
        for (int i = 0; i < this.bias.length; ++i) {
            this.bias[i] = random(0, 1);
        }
    }

    public void run(double deltaMs) {
        float sat = palette.getSaturationf();
        int c1 = LXColor.hsb(palette.getHuef(), sat, 100);
        int c2 = LXColor.hsb(this.tone.getValuef(), sat, 100);
        int li = 0;
        float amount = this.amount.getValuef();
        for (TreeModel.Leaf leaf : model.leaves) {
            float delta = amount - this.bias[li];
            if (delta <= 0) {
                setColor(leaf, c1);
            } else if (delta < .1) {
                setColor(leaf, LXColor.lerp(c1, c2, 10*delta));
            } else {
                setColor(leaf, c2);
            }
            ++li;
        }
    }
}
