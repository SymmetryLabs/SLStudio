package com.symmetrylabs.shows.hhgarden;

import com.symmetrylabs.shows.hhgarden.FlowerModel.FlowerPoint;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import java.util.stream.Stream;

public abstract class FlowerPattern extends SLPattern<FlowersModel> {
    public static final String GROUP_NAME = HHGardenShow.SHOW_NAME;

    protected FlowerPoint[] flowerPoints;

    public FlowerPattern(LX lx) {
        super(lx);
        flowerPoints = new FlowerPoint[model.size];
        for (int i = 0; i < model.size; i++) {
            flowerPoints[i] = (FlowerPoint) model.points[i];
        }
    }

    /* FlowersModel can't be default-constructed, so we override this
         method to avoid needing the default constructor. */
    @Override
    public Class getModelClass() {
        return FlowersModel.class;
    }
}
