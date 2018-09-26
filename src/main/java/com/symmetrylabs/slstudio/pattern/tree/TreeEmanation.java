package com.symmetrylabs.slstudio.pattern.tree;

import heronarts.lx.LX;
import heronarts.lx.LXUtils;
import heronarts.lx.color.LXColor;
import heronarts.lx.modulator.DampedParameter;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;

import com.symmetrylabs.shows.tree.TreeModel;
import static com.symmetrylabs.util.MathUtils.*;


public class TreeEmanation extends TreePattern {
    public String getAuthor() {
        return "Mark C. Slee";
    }

    public final CompoundParameter speed = (CompoundParameter)
        new CompoundParameter("Speed", 5000, 11000, 500)
            .setExponent(0.5f)
            .setDescription("Speed of emanation");

    public final CompoundParameter size =
        new CompoundParameter("Size", 2, 1, 4)
            .setDescription("Size of emanation");

    public final BooleanParameter inward =
        new BooleanParameter("Inward", false)
            .setDescription("Direction of emanation");

    private final LXModulator sizeDamped = startModulator(new DampedParameter(this.size, 2));

    private final float maxPos = TreeModel.Branch.NUM_TWIGS-1;
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
        for (TreeModel.Branch branch : model.getBranches()) {
            float pos = this.pos[bi++ % this.pos.length].getValuef();
            if (inward) {
                pos = maxPos - pos;
            }
            float ai = 0;
            for (TreeModel.Twig twig : branch.getTwigs()) {
                float d = LXUtils.wrapdistf(abs(ai - midBranch), pos, maxPos);
                setColor(twig, LXColor.gray(max(0, 100 - falloff * d)));
                ++ai;
            }
        }
    }
}
