package com.symmetrylabs.shows.usopen;

import java.util.*;
import java.lang.ref.WeakReference;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.lang.Float;

import com.symmetrylabs.shows.cubes.CubesModel;
import com.symmetrylabs.shows.cubes.CubesShow;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.output.SLController;
import com.symmetrylabs.util.CubePhysicalIdMap;
import com.symmetrylabs.util.listenable.SetListener;
import heronarts.lx.LX;
import heronarts.lx.output.FadecandyOutput;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.transform.LXTransform;
import heronarts.p3lx.ui.UI2dScrollContext;

import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.network.NetworkMonitor;
import com.symmetrylabs.slstudio.network.NetworkDevice;
import com.symmetrylabs.util.dispatch.Dispatcher;
import com.symmetrylabs.util.listenable.ListenableSet;
import com.symmetrylabs.util.Utils;
import static com.symmetrylabs.util.DistanceUtils.*;
import static com.symmetrylabs.util.DistanceConstants.*;

/**
 * This file implements the mapping functions needed to lay out the cubes.
 */
public class USOpenShow extends CubesShow {

    static final float globalOffsetX = 0;
    static final float globalOffsetY = 0;
    static final float globalOffsetZ = 0;

    static final float globalRotationX = 0;
    static final float globalRotationY = 0;
    static final float globalRotationZ = 0;


    static final TowerConfig[] TOWER_CONFIG = {
    };

    public SLModel buildModel() {
        // Any global transforms
        LXTransform globalTransform = new LXTransform();
        globalTransform.translate(globalOffsetX, globalOffsetY, globalOffsetZ);
        globalTransform.rotateX(globalRotationX * Math.PI / 180.);
        globalTransform.rotateY(globalRotationY * Math.PI / 180.);
        globalTransform.rotateZ(globalRotationZ * Math.PI / 180.);

        List<CubesModel.Tower> towers = new ArrayList<>();
        List<CubesModel.Cube> allCubes = new ArrayList<>();

        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
                Utils.createInput("./src/main/java/com/symmetrylabs/shows/usopen/cube_coordinates.txt")));

            List<CubesModel.Cube> cubes = new ArrayList<>();

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                line = line.replaceAll(" ","").replaceAll("\"","");
                String[] vals = line.split(",");

                float x = Float.parseFloat(vals[0]) * INCHES_PER_METER * 3.5f; // why need the 3.5?
                float y = Float.parseFloat(vals[2]) * INCHES_PER_METER * 3.5f; // why need the 3.5?
                float z = Float.parseFloat(vals[1]) * INCHES_PER_METER * 3.5f; // why need the 3.5?

                CubesModel.Cube cube = new CubesModel.Cube("0", x, y, z, 0, 0, 0, globalTransform, CubesModel.Cube.Type.LARGE);
                cubes.add(cube);
                allCubes.add(cube);
            }
            towers.add(new CubesModel.Tower("", cubes));

        } catch (IOException e) {
            e.printStackTrace();
        }

        List<CubesModel.Cube> additionalCubes = new ArrayList<>();

        towers.add(new CubesModel.Tower("", additionalCubes));

        CubesModel.Cube[] allCubesArr = new CubesModel.Cube[allCubes.size()];
        for (int i = 0; i < allCubesArr.length; i++) {
            allCubesArr[i] = allCubes.get(i);
        }

        return new CubesModel(towers, allCubesArr);
    }
}
