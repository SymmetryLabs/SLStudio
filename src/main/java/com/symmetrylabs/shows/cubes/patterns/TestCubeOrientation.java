package com.symmetrylabs.shows.cubes;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.color.LXColor;

import com.symmetrylabs.shows.cubes.CubesModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.transform.LXVector;

public class TestCubeOrientation extends SLPattern<CubesModel> {
    public static final String GROUP_NAME = CubesShow.SHOW_NAME;

    private final int[] cols = new int[] {
        LXColor.RED, LXColor.GREEN, LXColor.BLUE, LXColor.WHITE, lx.hsb(50, 100, 100)
    };

    public TestCubeOrientation(LX lx) {
        super(lx);
    }

    public void run(double delta) {
        for (CubesModel.Cube cube : model.getCubes()) {
            int i = 0;

            for (CubesModel.Face face : cube.getFaces()) {
                int col = cols[i++];
                for (LXVector v : getVectors(face.points)) {
                    colors[v.index] = col;
                }

                // make bottom of cube yellow
                for (LXVector v : getVectors(face.getStripByIndex(2).points)) {
                    colors[v.index] = cols[4];
                }
            }
        }
    }
}
