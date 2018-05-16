package com.symmetrylabs.slstudio.pattern.tree;

import com.symmetrylabs.layouts.oslo.TreeModel;
import heronarts.lx.LX;
import heronarts.lx.LXUtils;
import heronarts.lx.color.LXColor;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.FunctionalParameter;

import static com.symmetrylabs.util.MathUtils.max;

public class TreeSnakes extends TreePattern {
    public String getAuthor() {
        return "Mark C. Slee";
    }

    private static final int NUM_SNAKES = 24;
    private final LXModulator snakes[] = new LXModulator[NUM_SNAKES];
    private final LXModulator sizes[] = new LXModulator[NUM_SNAKES];

    private final int[][] mask = new int[NUM_SNAKES][TreeModel.Branch.NUM_LEAVES];

    public final CompoundParameter speed = (CompoundParameter)
        new CompoundParameter("Speed", 7000, 19000, 2000)
            .setExponent(.5)
            .setDescription("Speed of snakes moving");

    public final CompoundParameter modSpeed = (CompoundParameter)
        new CompoundParameter("ModSpeed", 7000, 19000, 2000)
            .setExponent(.5)
            .setDescription("Speed of snake length modulation");

    public final CompoundParameter size =
        new CompoundParameter("Size", 15, 10, 100)
            .setDescription("Size of longest snake");

    public TreeSnakes(LX lx) {
        super(lx);
        addParameter("speed", this.speed);
        addParameter("modSpeed", this.modSpeed);
        addParameter("size", this.size);
        for (int i = 0; i < NUM_SNAKES; ++i) {
            final int ii = i;
            this.snakes[i] = startModulator(new SawLFO(0, TreeModel.Branch.NUM_LEAVES, speed).randomBasis());
            this.sizes[i] = startModulator(new SinLFO(4, this.size, new FunctionalParameter() {
                public double getValue() {
                    return modSpeed.getValue() + ii*100;
                }
            }).randomBasis());
        }
    }

    public void run(double deltaMs) {
        for (int i = 0; i < NUM_SNAKES; ++i) {
            float snake = this.snakes[i].getValuef();
            float falloff = 100 / this.sizes[i].getValuef();
            for (int j = 0; j < TreeModel.Branch.NUM_LEAVES; ++j) {
                this.mask[i][j] = LXColor.gray(max(0, 100 - falloff * LXUtils.wrapdistf(j, snake, TreeModel.Branch.NUM_LEAVES)));
            }
        }
        int bi = 0;
        for (TreeModel.Branch branch : model.branches) {
            int[] mask = this.mask[bi++ % NUM_SNAKES];
            int li = 0;
            for (TreeModel.Leaf leaf : branch.leaves) {
                setColor(leaf, mask[li++]);
            }
        }
    }
}
