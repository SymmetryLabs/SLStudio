package com.symmetrylabs.slstudio.pattern.tree;

import com.symmetrylabs.layouts.oslo.TreeModel;
import com.symmetrylabs.slstudio.pattern.base.TreePattern;
import heronarts.lx.LX;
import heronarts.lx.LXUtils;
import heronarts.lx.color.LXColor;
import heronarts.lx.modulator.DampedParameter;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;

import static com.symmetrylabs.util.MathUtils.abs;
import static com.symmetrylabs.util.MathUtils.max;

public class TreeEmanation extends TreePattern {
    public String getAuthor() {
        return "Mark C. Slee";
    }

    public final CompoundParameter speed = (CompoundParameter)
        new CompoundParameter("Speed", 5000, 11000, 500)
            .setExponent(.5)
            .setDescription("Speed of emanation");

    public final CompoundParameter size =
        new CompoundParameter("Size", 2, 1, 4)
            .setDescription("Size of emanation");

    public final BooleanParameter inward =
        new BooleanParameter("Inward", false)
            .setDescription("Direction of emanation");

    private final LXModulator sizeDamped = startModulator(new DampedParameter(this.size, 2));

    private final float maxPos = TreeModel.Branch.NUM_ASSEMBLAGES-1;
    private final float midBranch = maxPos / 2;

    private static final int NUM_POSITIONS = 15;

    private final LXModulator[] pos = new LXModulator[NUM_POSITIONS];

    public TreeEmanation(LX lx) {
        super(lx);
        addParameter("speed", this.speed);
        addParameter("size", this.size);
        addParameter("inward", this.inward);
        for (int i = 0; i < NUM_POSITIONS; ++i) {
            this.pos[i] = startModulator(new SawLFO(maxPos, 0, this.speed).randomBasis());
        }
    }

    public void run(double deltaMs) {
        float falloff = 100 / this.sizeDamped.getValuef();
        boolean inward = this.inward.isOn();
        int bi = 0;
        for (TreeModel.Branch branch : model.branches) {
            float pos = this.pos[bi++ % this.pos.length].getValuef();
            if (inward) {
                pos = maxPos - pos;
            }
            float ai = 0;
            for (TreeModel.LeafAssemblage assemblage : branch.assemblages) {
                float d = LXUtils.wrapdistf(abs(ai - midBranch), pos, maxPos);
                setColor(assemblage, LXColor.gray(max(0, 100 - falloff * d)));
                ++ai;
            }
        }
    }
}
