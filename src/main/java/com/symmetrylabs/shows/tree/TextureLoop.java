package com.symmetrylabs.slstudio.pattern.tree;

import heronarts.lx.LX;

import com.symmetrylabs.shows.tree.TreeModel;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.modulator.LXModulator;
import heronarts.lx.modulator.SawLFO;
import heronarts.lx.LXUtils;
import heronarts.lx.color.LXColor;

import static com.symmetrylabs.util.MathUtils.*;


public class TextureLoop extends TexturePattern {
    public String getAuthor() {
        return "Mark C. Slee";
    }
    
    public final CompoundParameter speed = (CompoundParameter)
        new CompoundParameter("Speed", 500, 2000, 200)
        .setExponent(.5)
        .setDescription("Speed of the loop motion");    
    
    public final CompoundParameter size =
        new CompoundParameter("Size", 3, 1, TreeModel.Leaf.NUM_LEDS)
        .setDescription("Size of the thread");
    
    public LXModulator pos = startModulator(new SawLFO(0, TreeModel.Leaf.NUM_LEDS, speed)); 
    
    private final int[] leafMask = new int[TreeModel.Leaf.NUM_LEDS];
    
    public TextureLoop(LX lx) {
        super(lx);
        addParameter("rate", this.speed);
        addParameter("size", this.size);
    }
    
    public void run(double deltaMs) {
        float pos = this.pos.getValuef();
        float falloff = 100 / this.size.getValuef();
        for (int i = 0; i < this.leafMask.length; ++i) {
            this.leafMask[i] = LXColor.gray(max(0, 100 - falloff * LXUtils.wrapdistf(i, pos, TreeModel.Leaf.NUM_LEDS)));
        }
        setLeafMask(this.leafMask);
    }
}