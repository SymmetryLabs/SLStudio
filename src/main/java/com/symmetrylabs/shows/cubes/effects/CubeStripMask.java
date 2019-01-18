package com.symmetrylabs.shows.cubes.effects;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;

import com.symmetrylabs.slstudio.effect.SLEffect;
import com.symmetrylabs.shows.cubes.CubesModel;

public class CubeStripMask extends SLEffect<CubesModel> {

    public final CompoundParameter type = new CompoundParameter("type", 0, 0, 360);

    public CubeStripMask(LX lx) {
        super(lx);
        addParameter(type);
    }

    @Override
    public void run(double deltaMs, double amount) {
        for (CubesModel.Cube cube : ((CubesModel)model).getCubes()) {
            for (CubesModel.CubesStrip strip : cube.getStrips()) {
                if (strip.isHorizontal && type.getValuef() < 0.5) {
                    for (LXPoint p : strip.points) {
                        colors[p.index] = LXColor.BLACK;
                    }
                } else if (!strip.isHorizontal && type.getValuef() > 0.5) {
                    for (LXPoint p : strip.points) {
                        colors[p.index] = LXColor.BLACK;
                    }
                }
            }
        }
    }
}
