package com.symmetrylabs.shows.artbasel;

import java.util.*;

import com.symmetrylabs.shows.cubes.CubesModel;
import com.symmetrylabs.shows.cubes.CubesShow;
import com.symmetrylabs.shows.cubes.UICubesMappingPanel;
import com.symmetrylabs.shows.cubes.UICubesOutputs;
import com.symmetrylabs.slstudio.model.SLModel;

import heronarts.lx.transform.LXTransform;
import heronarts.p3lx.ui.UI2dScrollContext;

import com.symmetrylabs.slstudio.SLStudioLX;

public class ArtBaselShow extends CubesShow {
    public static final String SHOW_NAME = "artbasel";

    static final float globalOffsetX = 0;
    static final float globalOffsetY = 0;
    static final float globalOffsetZ = 0;

    static final float globalRotationX = 0;
    static final float globalRotationY = 0;
    static final float globalRotationZ = 0;

    static final float CUBE_WIDTH = 24;
    static final float CUBE_HEIGHT = 24;
    static final float TOWER_WIDTH = 24;
    static final float TOWER_HEIGHT = 24;
    static final float CUBE_SPACING = 1.5f;

    static final float TOWER_VERTICAL_SPACING = 0;
    static final float TOWER_RISER = 14;
    static final float SP = 25.5f;
    static final float JUMP = TOWER_HEIGHT+TOWER_VERTICAL_SPACING;

    static final float INCHES_PER_METER = 39.3701f;
    static final float BALCONY_RADIUS = INCHES_PER_METER * 5f;

    public SLModel buildModel() {
        // Any global transforms
        LXTransform transform = new LXTransform();

        List<CubesModel.Tower> towers = new ArrayList<>();
        List<CubesModel.Cube> allCubes = new ArrayList<>();

        for (int t = 0; allCubes.size() < 60; t++) {
            int cubeCount = t % 2 == 0 ? 4 : 3;
            transform.push();
            float theta = 9.f * t * (float) Math.PI / 180.f;
            float x = BALCONY_RADIUS * (float) -Math.cos(theta);
            float z = BALCONY_RADIUS * (float) Math.sin(theta);
            transform.translate(x, cubeCount == 3 ? CUBE_HEIGHT / 2 : 0, z);
            transform.rotateY(theta);
            List<CubesModel.Cube> towerCubes = new ArrayList<>();
            for (int i = 0; i < cubeCount; i++) {
                CubesModel.Cube c = new CubesModel.Cube("ABC", 0, SP * i, 0, 0, 0, 0, transform, CubesModel.Cube.Type.LARGE_DOUBLE);
                towerCubes.add(c);
                allCubes.add(c);
            }
            towers.add(new CubesModel.Tower(Integer.toString(t + 1), towerCubes));
            transform.pop();
        }
        System.out.println(String.format("%d total cubes in structure", allCubes.size()));

        CubesModel.Cube[] allCubesArr = new CubesModel.Cube[allCubes.size()];
        for (int i = 0; i < allCubesArr.length; i++) {
            allCubesArr[i] = allCubes.get(i);
        }

        CubesModel model = new CubesModel(towers, allCubesArr);
        model.setTopologyTolerances(6, 6, 8);
        return model;
    }

    public void setupUi(SLStudioLX lx, SLStudioLX.UI ui) {
        UI2dScrollContext utility = ui.rightPane.utility;
        new UICubesOutputs(lx, ui, this, 0, 0, utility.getContentWidth()).addToContainer(utility);
        new UICubesMappingPanel(lx, ui, 0, 0, utility.getContentWidth()).addToContainer(utility);
    }
}
