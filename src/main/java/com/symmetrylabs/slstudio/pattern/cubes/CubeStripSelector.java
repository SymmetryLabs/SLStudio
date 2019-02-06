package com.symmetrylabs.slstudio.pattern.cubes;

import com.symmetrylabs.shows.cubes.CubesModel;
import com.symmetrylabs.shows.cubes.CubesShow;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.DiscreteParameter;


public class CubeStripSelector extends SLPattern<CubesModel> {
    public static final String GROUP_NAME = CubesShow.SHOW_NAME;

    public final DiscreteParameter selectedStrip = new DiscreteParameter("strip", 1, 1, 13);

    public CubeStripSelector(LX lx) {
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
