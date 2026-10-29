package com.symmetrylabs.shows.ysiadsparty;

import java.io.File;
import java.io.FileReader;
import java.util.*;

import com.google.gson.Gson;

import com.symmetrylabs.shows.cubes.CubesModel;
import com.symmetrylabs.shows.cubes.CubesShow;
import com.symmetrylabs.shows.ysiadsparty.ui.UIShaprImportTool;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.model.SLModel;


import heronarts.lx.transform.LXTransform;

import com.symmetrylabs.shows.Show;

/**
 * This file implements the mapping functions needed to lay out the cubes.
 */
public class YsiadsPartyShow extends CubesShow implements Show {
    public static final String SHOW_NAME = "ysiadsparty";

    static final float globalOffsetX = 0;
    static final float globalOffsetY = 0;
    static final float globalOffsetZ = 0;

    static final float globalRotationX = 0;
    static final float globalRotationY = 0;
    static final float globalRotationZ = 0;
    static final float CSP = 32;


    static final TowerConfig[] TOWER_CONFIG = {
        //back middle (tallest tower)
        new TowerConfig(CSP*0, 0, 0, 0, 45, 0, new String[] { "521", "123", "422", "192", "157"}),
        //back towers going to the right
        new TowerConfig(CSP*.5f, 0, CSP*-.5f, 0, 45, 0, new String[] { "29", "141", "32", "34"}),
        new TowerConfig(CSP*1, 0, CSP*-1, 0, 45, 0, new String[] { "86", "68", "174",}),
        new TowerConfig(CSP*1.5f, 0, CSP*-1.5f, 0, 45, 0, new String[] { "43", "25"}),
        //back towers going to the left
        new TowerConfig(CSP*-.5f, 0, CSP*-.5f, 0, 45, 0, new String[] { "128", "113", "63", "51"}),
        new TowerConfig(CSP*-1, 0, CSP*-1, 0, 45, 0, new String[] { "132", "22", "1151",}),
        //two towers in the middle going to the right
        new TowerConfig(CSP*0, 0, CSP*-1, 0, 45, 0, new String[] { "1117", "172", "211",}),
        new TowerConfig(CSP*.5f, 0, CSP*-1.5f, 0, 45, 0, new String[] { "314", "408"}),
    };

    public SLModel buildModel() {
        // Any global transforms
        LXTransform globalTransform = new LXTransform();
        globalTransform.translate(globalOffsetX, globalOffsetY, globalOffsetZ);
        globalTransform.rotateX(globalRotationX * Math.PI / 180.);
        globalTransform.rotateY(globalRotationY * Math.PI / 180.);
        globalTransform.rotateZ(globalRotationZ * Math.PI / 180.);

        /* Cubes ----------------------------------------------------------*/
        List<CubesModel.Tower> towers = new ArrayList<>();
        List<CubesModel.Cube> allCubes = new ArrayList<>();

        List<TowerConfig> configs = new ArrayList<>();
        ShaprCubeImporter.Cube[] imported = loadImportedCubes();
        if (imported != null && imported.length > 0) {
            System.out.println("YsiadsPartyShow: loaded " + imported.length + " cubes from " + UIShaprImportTool.CUBES_JSON);
            for (ShaprCubeImporter.Cube c : imported) {
                configs.add(new TowerConfig(c.x, c.y, c.z, 0, c.ry, 0, new String[] {""}));
            }
        } else {
            configs.addAll(Arrays.asList(TOWER_CONFIG));
        }

        for (TowerConfig config : configs) {
            List<CubesModel.Cube> cubes = new ArrayList<>();
            float x = config.x;
            float z = config.z;
            float xRot = config.xRot;
            float yRot = config.yRot;
            float zRot = config.zRot;
            CubesModel.Cube.Type type = config.type;

            for (int i = 0; i < config.ids.length; i++) {
                float y = config.yValues[i];
                CubesModel.Cube cube = new CubesModel.Cube(config.ids[i], x, y, z, xRot, yRot, zRot, globalTransform, type);
                cubes.add(cube);
                allCubes.add(cube);
            }
            towers.add(new CubesModel.Tower("", cubes));
        }
        /*-----------------------------------------------------------------*/

        CubesModel.Cube[] allCubesArr = new CubesModel.Cube[allCubes.size()];
        for (int i = 0; i < allCubesArr.length; i++) {
            allCubesArr[i] = allCubes.get(i);
        }

        CubesModel m = new CubesModel(SHOW_NAME, towers, allCubesArr, cubeInventory, mapping);
        m.setTopologyTolerances(2, 6, 8);
        return m;
    }

    /**
     * Reads data/ysiadsparty-cubes.json written by the Shapr3D import tool,
     * or returns null when absent/unparseable.
     */
    private static ShaprCubeImporter.Cube[] loadImportedCubes() {
        File f = new File(UIShaprImportTool.CUBES_JSON);
        if (!f.exists()) return null;
        try (FileReader reader = new FileReader(f)) {
            return new Gson().fromJson(reader, ShaprCubeImporter.Cube[].class);
        } catch (Exception e) {
            System.err.println("YsiadsPartyShow: failed to load " + UIShaprImportTool.CUBES_JSON + ", using TOWER_CONFIG");
            return null;
        }
    }

    public void setupUi(final SLStudioLX lx, SLStudioLX.UI ui) {
        super.setupUi(lx, ui);
        new UIShaprImportTool(lx, ui, 0, 0, ui.rightPane.utility.getContentWidth())
            .addToContainer(ui.rightPane.utility);
    }

    @Override
    public String getShowName() {
        return SHOW_NAME;
    }
}
