package com.symmetrylabs.shows.treeV2.patterns;

import heronarts.lx.LX;


import com.symmetrylabs.shows.treeV2.*;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.modulator.TriangleLFO;
import heronarts.lx.LXUtils;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;

import static com.symmetrylabs.util.MathUtils.*;


public class TextureCrawl extends TexturePattern {
    public String getAuthor() {
        return "Mark C. Slee";
    }

    private static final int NUM_MASKS = 24;
    private final int[][] mask = new int[NUM_MASKS][TreeModel.Leaf.NUM_LEDS];

    private final LXModulator[] pos = new LXModulator[NUM_MASKS];
    private final LXModulator[] size = new LXModulator[NUM_MASKS];

    public TextureCrawl(LX lx) {
        super(lx);
        for (int i = 0; i < NUM_MASKS; ++i) {
            this.pos[i] = startModulator(new SawLFO(0, TreeModel.Leaf.NUM_LEDS, startModulator(new SinLFO(2000, 7000, 19000).randomBasis())));
            this.size[i] = startModulator(new TriangleLFO(-3, 2*TreeModel.Leaf.NUM_LEDS, 19000).randomBasis());
        }
    }

    public void run(double deltaMs) {
        for (int i = 0; i < NUM_MASKS; ++i) {
            float pos = this.pos[i].getValuef();
            float falloff = 100 / max(1, this.size[i].getValuef());
            for (int j = 0; j < TreeModel.Leaf.NUM_LEDS; ++j) {
                this.mask[i][j] = LXColor.gray(max(0, 100 - falloff * LXUtils.wrapdistf(j, pos, TreeModel.Leaf.NUM_LEDS)));
            }
        }
        int li = 0;
        for (TreeModel.Leaf leaf : model.leaves) {
            int[] mask = this.mask[li++ % NUM_MASKS];
            int i = 0;
            for (LXPoint p : leaf.points) {
                colors[p.index] = mask[i++];
            }
        }
    }
}
