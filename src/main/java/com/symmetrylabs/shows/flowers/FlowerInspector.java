package com.symmetrylabs.shows.flowers;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.DiscreteParameter;
import java.util.Arrays;

public class FlowerInspector extends FlowerPattern {
    public static final String GROUP_NAME = FlowersShow.SHOW_NAME;

    private final DiscreteParameter flowerParam;

    public FlowerInspector(LX lx) {
        super(lx);
        flowerParam = new DiscreteParameter("flower", 0, 0, model.getFlowers().size());
        addParameter(flowerParam);
    }

    private FlowerModel get() {
        return model.getFlowers().get(flowerParam.getValuei());
    }

    @Override
    public void run(double elapsedMs) {
        Arrays.fill(colors, 0);
        for (LXPoint p : get().points) {
            colors[p.index] = 0xFFFFFFFF;
        }
    }

    @Override
    public String getCaption() {
        return get().getFlowerData().toString();
    }
}
