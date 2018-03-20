package com.symmetrylabs.slstudio.effect;

import heronarts.lx.LX;
import heronarts.lx.LXEffect;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;

import com.symmetrylabs.layouts.dollywood.DollywoodModel;

public class MaskTree extends LXEffect {

    public MaskTree(LX lx) {
        super(lx);
    }

    @Override
    public void run(double deltaMs, double amount) {
        for (LXPoint p : ((DollywoodModel)model).treeModel.points) {
            float brightness = LXColor.b(colors[p.index]);
            colors[p.index] = 0xff2a2d1d;
        }
    }
}
