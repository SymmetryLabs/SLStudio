package com.symmetrylabs.layouts.cubes.patterns;

import com.symmetrylabs.layouts.cubes.CubesModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.PolyBuffer;
import heronarts.lx.LXPattern;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.DiscreteParameter;

import java.util.List;

import static heronarts.lx.PolyBuffer.Space.SRGB8;


public class StripSelector extends LXPattern {

    public final CubesModel model;

    public final DiscreteParameter selectedStrip = new DiscreteParameter("strip", 1, 1, 13);

    public StripSelector(LX lx) {
        super(lx);
        this.model = (CubesModel) lx.model;
        addParameter(selectedStrip);
    }

    public void run(double deltaMs) {
        setColors(0);

        for (CubesModel.Cube cube : model.getCubes()) {
            CubesModel.CubesStrip strip = cube.getStrips().get(selectedStrip.getValuei()-1);

            int i = 0;
            for (LXPoint p : strip.getPoints()) {
                colors[p.index] = i++ > 4 ? LXColor.RED : LXColor.GREEN;
            }
        }
    }

    }



