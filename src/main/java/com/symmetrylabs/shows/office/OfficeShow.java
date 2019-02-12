package com.symmetrylabs.shows.office;

import com.symmetrylabs.shows.HasWorkspace;
import com.symmetrylabs.shows.cubes.CubesModel;
import com.symmetrylabs.shows.cubes.CubesShow;
import com.symmetrylabs.shows.cubes.UICubesMappingPanel;
import com.symmetrylabs.shows.cubes.UICubesOutputs;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.showplugins.FrameTimeCrasher;
import com.symmetrylabs.slstudio.workspaces.Workspace;
import heronarts.lx.transform.LXTransform;
import heronarts.p3lx.ui.UI2dScrollContext;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
 
public class OfficeShow extends CubesShow implements HasWorkspace {
    public static final String SHOW_NAME = "office";

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

    private Workspace workspace;

    static final TowerConfig[] TOWER_CONFIG = {


    //Back Row

//TOWER 1

            new TowerConfig(SP * 0, SP * 0, SP * 0, new String[][]{
                new String[] {"967", "964"},
                new String[] {"968", "976"},
                new String[] {"446", "596"},
                new String[] {"669", "668"},
            }),    

            new TowerConfig(SP * 1, SP * 0, SP * 0, new String[][]{
                new String[] {"778", "970"},
                new String[] {"971", "994"},
                new String[] {"1097", "1070"},
                new String[] {"603", "602"},
                new String[] {"449", "448"},
            }),

            new TowerConfig(SP * 2, SP * 0, SP * 0, new String[][]{
                new String[] {"366", "702"},
                new String[] {"809", "808"},
                new String[] {"583", "582"},
            }),

            new TowerConfig(SP * 3, SP * 0, SP * 0, new String[][]{
                new String[] {"1012", "1039"},
                new String[] {"977", "1024"},
                new String[] {"647", "646"},
            }),    


////////////////////////// ROW 2
                new TowerConfig(0, SP * 0, -SP * 1, new String[][]{
                new String[] {"475", "474"},
                new String[] {"1107", "1106"},
                new String[] {"1078", "902"},
                new String[] {"546", "913"},
            }),

                new TowerConfig(SP * 1, SP * 0, -SP * 1, new String[][]{
                new String[] {"893", "894"},
                new String[] {"897", "885"},
                new String[] {"443", "821"},
            }),

                new TowerConfig(SP * 2, SP * 0, -SP * 1, new String[][]{
                new String[] {"567", "566"},
                new String[] {"873", "872"},
                new String[] {"849", "848"},
                new String[] {"911", "1063"},
            }),

                new TowerConfig(SP * 3, SP * 0, -SP * 1, new String[][]{
                new String[] {"1046", "1045"},
                new String[] {"954", "959"},
                new String[] {"865", "864"},    
            }),



// //////////////////// ROW 3 ////////////////////////////////

                new TowerConfig(-SP * 1, SP * 0, -SP * 2, new String[][]{
                new String[] {"589", "588"},
                new String[] {"725", "724"},
                new String[] {"1017", "1016"},
            }),
            
                new TowerConfig(0, SP * 0, -SP * 2, new String[][]{
                new String[] {"767", "766"},
                new String[] {"775", "774"},
                new String[] {"833", "832"},
                new String[] {"1081", "972"},

            }),

                new TowerConfig(SP * 1, SP * 0, -SP * 2, new String[][]{
                new String[] {"931", "938"},
                new String[] {"437", "1094"},
                new String[] {"442", "722"},
            }),

                new TowerConfig(SP * 2, SP * 0, -SP * 2, new String[][]{
                new String[] {"451", "450"},
                new String[] {"1011", "802"}, 
                new String[] {"587", "586"},

            }),

            new TowerConfig(SP * 3, SP * 0, -SP * 2, new String[][]{
                new String[] {"739", "738"},
                new String[] {"553", "552"}, 
  
            }),

    };

    static class TowerConfig {

        final CubesModel.Cube.Type type;
        final float x;
        final float y;
        final float z;
        final float xRot;
        final float yRot;
        final float zRot;
        final String[][] ids;
        final float[] yValues;

        TowerConfig(float x, float y, float z, String[][] ids) {
            this(CubesModel.Cube.Type.LARGE, x, y, z, ids);
        }

        TowerConfig(float x, float y, float z, float yRot, String[][] ids) {
            this(x, y, z, 0, yRot, 0, ids);
        }

        TowerConfig(CubesModel.Cube.Type type, float x, float y, float z, String[][] ids) {
            this(type, x, y, z, 0, 0, 0, ids);
        }

        TowerConfig(CubesModel.Cube.Type type, float x, float y, float z, float yRot, String[][] ids) {
            this(type, x, y, z, 0, yRot, 0, ids);
        }

        TowerConfig(float x, float y, float z, float xRot, float yRot, float zRot, String[][] ids) {
            this(CubesModel.Cube.Type.LARGE, x, y, z, xRot, yRot, zRot, ids);
        }

        TowerConfig(CubesModel.Cube.Type type, float x, float y, float z, float xRot, float yRot, float zRot, String[][] ids) {
            this.type = type;
            this.x = x;
            this.y = y;
            this.z = z;
            this.xRot = xRot;
            this.yRot = yRot;
            this.zRot = zRot;
            this.ids = ids;

            this.yValues = new float[ids.length];
            for (int i = 0; i < ids.length; i++) {
                yValues[i] = y + i * (CUBE_HEIGHT + CUBE_SPACING);
            }
        }
    }

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

        int stripId = 0;
        for (TowerConfig config : TOWER_CONFIG) {
            List<CubesModel.Cube> cubes = new ArrayList<>();
            float x = config.x;
            float z = config.z;
            float xRot = config.xRot;
            float yRot = config.yRot;
            float zRot = config.zRot;
            CubesModel.Cube.Type type = config.type;

            for (int i = 0; i < config.ids.length; i++) {
                float y = config.yValues[i];
                CubesModel.DoubleControllerCube cube =
                    new CubesModel.DoubleControllerCube(
                        config.ids[i][0], config.ids[i][1],
                        x, y, z, xRot, yRot, zRot, globalTransform);
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

        return new CubesModel(towers, allCubesArr);
    }

    @Override
    public void setupUi(SLStudioLX lx, SLStudioLX.UI ui) {
        super.setupUi(lx, ui);
        workspace = new Workspace(lx, ui, "shows/office");
    }

    @Override
    public Workspace getWorkspace() {
        return workspace;
    }
}
