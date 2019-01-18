package com.symmetrylabs.shows.hhgarden;

import heronarts.lx.LX;
import heronarts.lx.parameter.BooleanParameter;

public abstract class FlowerTexturePattern extends FlowerPattern {
    public final BooleanParameter rotate =
        new BooleanParameter("rotate", false)
        .setDescription("Rotates texture masks between elements");

    public FlowerTexturePattern(LX lx) {
        super(lx);
        addParameter(rotate);
    }

    protected void setFlowerMask(int[] flowerMask) {
        int offset = 0;
        boolean rot = rotate.getValueb();
        for (FlowerModel fm : model.getFlowers()) {
            for (int i = 0; i < fm.points.length; i++) {
                colors[fm.points[i].index] = flowerMask[(i + offset) % flowerMask.length];
            }
            if (rot) {
                offset++;
            }
        }
    }
}
