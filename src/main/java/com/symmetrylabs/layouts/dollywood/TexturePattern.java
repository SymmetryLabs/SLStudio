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

    protected void setButterflyMask(int[] butterflyMask) {
        for (DollywoodModel.Butterfly butterfly : model.getButterflies()) {
            for (int i = 0; i < butterfly.points.length; ++i) {
                colors[butterfly.points[i].index] = butterflyMask[i];
            }
        }
    }
            
    protected void setWingMask(int[] wingMask) { 
        for (DollywoodModel.Wing wing : model.wings) {
            for (int i = 0; i < wing.points.length; ++i) {
                if (i > wingMask.length-1) break;
                colors[wing.points[0].index + i] = wingMask[i];
            }
        }
    }
}