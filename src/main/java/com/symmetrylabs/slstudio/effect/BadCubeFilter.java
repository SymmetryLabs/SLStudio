package com.symmetrylabs.slstudio.effect;

import com.symmetrylabs.shows.cubes.CubesModel;

import java.util.HashSet;

import heronarts.lx.LX;
import heronarts.lx.color.LXColor;

public class BadCubeFilter extends ModelSpecificEffect<CubesModel> {
    @Override protected CubesModel createEmptyModel() {
        return new CubesModel();
    }

    static String[] BAD_CUBES = {
        "852", "853",
        "841", "840",
        "513", "512",
        "641", "978",
    };

    HashSet<String> badCubeSet = new HashSet<>();

    public BadCubeFilter(LX lx) {
        super(lx);

        for (String id : BAD_CUBES) {
            badCubeSet.add(id);
        }
    }



    @Override protected void run(double deltaMs, double enabledAmount) {
        for (CubesModel.Cube c : model.getCubes()) {
            boolean bad;
            if (c instanceof CubesModel.DoubleControllerCube) {
                CubesModel.DoubleControllerCube dc = (CubesModel.DoubleControllerCube)c;
                bad = badCubeSet.contains(dc.idA) || badCubeSet.contains(dc.idB);
            } else {
                bad = badCubeSet.contains(c.id);
            }

            if (bad) {
                setColor(c, LXColor.BLACK);
            }
        }
    }
}
