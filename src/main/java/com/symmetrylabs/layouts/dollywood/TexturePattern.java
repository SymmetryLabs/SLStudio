package com.symmetrylabs.layouts.dollywood;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.color.LXColor;

public abstract class TexturePattern extends LXPattern {
    private final DollywoodModel model;

    public TexturePattern(LX lx) {
        super(lx);
        this.model = ((DollywoodModel)lx.model);
    }
            
    protected void setWingMask(int[] wingMask) { 
        for (DollywoodModel.Wing wing : model.wings) {
            for (int i = 0; i < wing.points.length; ++i) {
                colors[wing.points[0].index + i] = wingMask[i];
            }
        }
    }
}