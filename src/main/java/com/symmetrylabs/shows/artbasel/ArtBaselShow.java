package com.symmetrylabs.shows.artbasel;

import com.symmetrylabs.shows.HasWorkspace;
import com.symmetrylabs.shows.cubes.CubesModel;
import com.symmetrylabs.shows.cubes.CubesShow;
import com.symmetrylabs.shows.cubes.UICubesMappingPanel;
import com.symmetrylabs.shows.cubes.UICubesOutputs;
import com.symmetrylabs.slstudio.workspaces.Workspace;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.model.SLModel;
import heronarts.lx.transform.LXTransform;
import heronarts.p3lx.ui.UI2dScrollContext;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class ArtBaselShow extends CubesShow implements HasWorkspace {
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

    private static final File ID_FILE = new File("shows/artbasel/cube-ids.txt");

    private Workspace workspace;

    private LinkedList<String[]> loadIds() {
        List<String> lines;
        try {
            lines = Files.readAllLines(ID_FILE.toPath());
        } catch (IOException e) {
            System.err.println(String.format("couldn't read ID file: %s", e.getMessage()));
            return null;
        }
        LinkedList<String[]> res = new LinkedList<>();
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("#") || line.length() == 0) {
                continue;
            }
            String[] lineBits = line.split(" ");
            if (lineBits.length != 2) {
                System.err.println(String.format("bad format on line: '%s'", line));
                continue;
            }
            res.addLast(lineBits);
        }
        return res;
    }

    public SLModel buildModel() {
        // Any global transforms
        LXTransform transform = new LXTransform();

        List<CubesModel.Tower> towers = new ArrayList<>();
        List<CubesModel.Cube> allCubes = new ArrayList<>();
        LinkedList<String[]> ids = loadIds();

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
                String[] idPair;
                if (ids.isEmpty()) {
                    idPair = new String[] {
                        String.format("UNMAPPED-%02d-A", allCubes.size() + 1),
                        String.format("UNMAPPED-%02d-B", allCubes.size() + 1),
                    };
                } else {
                    idPair = ids.removeFirst();
                }
                CubesModel.Cube c = new CubesModel.DoubleControllerCube(
                    idPair[0], idPair[1], 0, SP * i, 0, 0, 0, 0, transform);
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

    @Override
    public void setupUi(SLStudioLX lx, SLStudioLX.UI ui) {
        super.setupUi(lx, ui);
        workspace = new Workspace(lx, ui, "shows/artbasel");
    }

    @Override
    public Workspace getWorkspace() {
        return workspace;
    }
}
