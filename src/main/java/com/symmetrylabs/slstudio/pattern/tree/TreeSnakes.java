package com.symmetrylabs.slstudio.pattern.tree;

import heronarts.lx.LX;
import heronarts.lx.LXUtils;
import heronarts.lx.color.LXColor;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.FunctionalParameter;

import com.symmetrylabs.shows.tree.TreeModel;
import static com.symmetrylabs.util.MathUtils.*;


public class TreeSnakes extends TreePattern {
    public String getAuthor() {
        return "Mark C. Slee";
    }

    private final int NUM_LEAVES = TreeModel.Twig.NUM_LEAVES*8;

    private static final int NUM_SNAKES = 24;
    private final LXModulator snakes[] = new LXModulator[NUM_SNAKES];
    private final LXModulator sizes[] = new LXModulator[NUM_SNAKES];

    private final int[][] mask = new int[NUM_SNAKES][NUM_LEAVES];

    public final CompoundParameter speed = (CompoundParameter)
        new CompoundParameter("Speed", 7000, 19000, 2000)
            .setExponent(0.5f)
            .setDescription("Speed of snakes moving");

    public final CompoundParameter modSpeed = (CompoundParameter)
        new CompoundParameter("ModSpeed", 7000, 19000, 2000)
            .setExponent(0.5f)
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
            this.snakes[i] = startModulator(new SawLFO(0, NUM_LEAVES, speed).randomBasis());
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
            for (int j = 0; j < NUM_LEAVES; ++j) {
                this.mask[i][j] = LXColor.gray(max(0, 100 - falloff * LXUtils.wrapdistf(j, snake, NUM_LEAVES)));
            }
        }
        int bi = 0;
        for (TreeModel.Branch branch : model.getBranches()) {
            int[] mask = this.mask[bi++ % NUM_SNAKES];
            int li = 0;
            for (TreeModel.Leaf leaf : branch.getLeaves()) {
                setColor(leaf, mask[li++]);
            }
        }
    }
}
