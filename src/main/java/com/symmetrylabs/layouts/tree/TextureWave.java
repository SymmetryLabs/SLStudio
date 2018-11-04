package com.symmetrylabs.slstudio.pattern.tree;

import heronarts.lx.LX;

import com.symmetrylabs.layouts.tree.TreeModel;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.modulator.TriangleLFO;
import heronarts.lx.LXUtils;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;

import static com.symmetrylabs.util.MathUtils.*;


public class TextureWave extends TexturePattern {
    public String getAuthor() {
        return "Mark C. Slee";
    }

    public final CompoundParameter speed = (CompoundParameter)
        new CompoundParameter("Speed", 1000, 4000, 250)
            .setDescription("Speed of oscillation between sides of the leaf")
            .setExponent(.5);

    private final LXModulator[] side = new LXModulator[TreeModel.Twig.NUM_LEAVES];
    private final int[] assemblageMask = new int[TreeModel.Twig.NUM_LEDS];

    public TextureWave(LX lx) {
        super(lx);
        for (int i = 0; i < this.side.length; ++i) {
            this.side[i] = startModulator(new SinLFO("Side", 0, 100, speed).setBasis(i / (float) this.side.length));
        }
        for (int i = 0; i < this.assemblageMask.length; ++i) {
            this.assemblageMask[i] = 0xff000000;
        }
        addParameter("speed", this.speed);
    }

    public void run(double deltaMs) {
        int i = 0;
        for (int ai = 0; ai < TreeModel.Twig.NUM_LEAVES; ++ai) {
            float side = this.side[ai].getValuef();
            for (int li = 0; li < TreeModel.Leaf.NUM_LEDS; ++li) {
                if (li < 3) {
                    this.assemblageMask[i] = LXColor.gray(side);
                } else if (li > 3) {
                    this.assemblageMask[i] = LXColor.gray(100 - side);
                }
                ++i;
            }
        }
        setTwigMask(this.assemblageMask);
    }
}
