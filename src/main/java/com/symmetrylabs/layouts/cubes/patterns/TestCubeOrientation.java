package com.symmetrylabs.layouts.cubes.patterns;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.color.LXColor;

import com.symmetrylabs.layouts.cubes.CubesModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;

public class TestCubeOrientation extends SLPattern<CubesModel> {

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
                for (LXPoint p : face.points) {
                    colors[p.index] = col;
                }

                // make bottom of cube yellow
                for (LXPoint p : face.getStripByIndex(2).points) {
                    colors[p.index] = cols[4];
                }
            }
        }
    }
}
