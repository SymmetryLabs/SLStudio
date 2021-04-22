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

    private Workspace workspace;

    static final TowerConfig[] TOWER_CONFIG = {



//LAYER 1 (FLOOR CUBES)
//wrong in inventory

//1132, 1135

            new TowerConfig(SP*0, SP*0, SP*0, new String[][]{
                new String[] {"781", "780"},
                new String[] {"1067", "1066"},
                new String[] {"361", "1007"},

            }), 
            //FRONT MIDDLE TOWER OF 3
            new TowerConfig(SP*1.5f, SP*0, SP*0, new String[][]{
                new String[] {"741", "740"},
                new String[] {"5410ecf48d2f", "5410ecf4c520"},
                new String[] {"415hp", "1050"},

            }), 
            //FRONT RIGHT TOWER OF 3
            new TowerConfig(SP*2.5f, SP*0, SP*0, new String[][]{
                new String[] {"475", "474"},
                new String[] {"5410ecf4fec0", "5410ecf4c0ab"},
                new String[] {"5410ecf5aa8e", "5410ecf68db3"},

            }), 
            //FRONT TOWER OF 2
            new TowerConfig(SP*3.5f, SP*0, SP*0, new String[][]{
                new String[] {"813", "812"},
                new String[] {"583", "582"},

            }), 
    //--------------------BACK ROW---------------------------------------
            //BACK LEFT TOWER OF 4
            new TowerConfig(SP*.5f, SP*0, SP*1, new String[][]{
                new String[] {"5410ecfd8d32", "5410ecf520a5"},
                new String[] {"575", "1146"},
                new String[] {"949", "946"},
                new String[] {"723", "196"},

            }), 
            //BACK MIDDLE TOWER OF 4
            new TowerConfig(SP*1.5f, SP*0, SP*1, new String[][]{
                new String[] {"5410ecf4feb5", "1056"},
                new String[] {"957", "1348"},
                new String[] {"1053", "1062"},
                new String[] {"5410ecf4eb5", "5410ecf4fd47"},

            }), 
            //BACK RIGHT TOWER OF 4
            new TowerConfig(SP*2.5f, SP*0, SP*1, new String[][]{
                new String[] {"953", "1110"},
                new String[] {"971", "994"},
                new String[] {"637", "1057"},
                new String[] {"765", "764"},

            }), 
            //BACK RIGHT TOWER OF 2
            new TowerConfig(SP*3.5f, SP*0, SP*1, new String[][]{
                new String[] {"1073", "1072"},
                new String[] {"873", "872"},

            }), 

};



            // //Piano Cubes
            // new TowerConfig(SP*0, SP*1, SP*0, new String[][]{new String[] {"5410ecf53264", "5410ecf50358"}}), 
            // new TowerConfig(SP*1, SP*1.5f, SP*.5f, new String[][]{new String[] {"824", "825"}}), 
            // new TowerConfig(SP*1.75f, SP*2.5f, 0, new String[][]{new String[] {"140", "5410ecfdb7c6"}}), 
            // new TowerConfig(SP*2f, SP*1.5f, 0, new String[][]{new String[] {"928", "5410ecf53264"}}), 
            // new TowerConfig(SP*3f, SP*2f, SP*-.5f, new String[][]{new String[] {"5410ecf50358", "489"}}), 
            // new TowerConfig(SP*4f, SP*1.5f, SP*-1, new String[][]{new String[] {"5410ecf50358", "5410ecf53264"}}), 
            // new TowerConfig(SP*3.5f, SP*1.5f,SP* -2, new String[][]{new String[] {"826", "827"}}), 
            // new TowerConfig(SP*3.5f, SP*.5f,SP* -2, new String[][]{new String[] {"567", "566"}}), 
            // new TowerConfig(SP*3.5f, SP*-.5f,SP* -2, new String[][]{new String[] {"806", "5410ecf53264"}}), 
            // new TowerConfig(SP*3.5f, SP*.5f, SP* -3, new String[][]{new String[] {"0", "614"}}), 

            // //Tower of 2
            // new TowerConfig(SP*-2.25f, 0, SP*-3.25f, new String[][]{new String[] {"5410ecf53639", "5410ecf53185"}, new String[] {"1125", "1141"}}), 
            // //Tower of 3
            // new TowerConfig(SP*-1.25f, 0, SP*-3, new String[][]{new String[] {"942", "786"}, new String[] {"1077", "1076"}, new String[] {"587", "586"}}), 
            // // Tower of 4
            // new TowerConfig(SP*-1.5f, 0, SP*-2, new String[][]{new String[] {"787", "786"}, new String[] {"595", "804"}, new String[] {"1012", "1039"},  new String[] {"", "1084"}}), 
            // // Tower of 3
            // new TowerConfig(SP*-1, SP*.5f, SP*-1, new String[][]{new String[] {"1128", "5410ecfdb2d4"},  new String[] {"5410ecf51b63", "1150"}, new String[] {"5410ecfdcb12", "5410ecfd7450"}}),
            // // Tower of 4 
            // new TowerConfig(SP*0, 0, SP*0, new String[][]{new String[] {"553", "552"}, new String[] {"849", "919"}, new String[] {"1132", "1135"},  new String[] {"773", "772"}}),
            // // Tower of 3 
            // new TowerConfig(SP*1, SP*.5f, SP*-.5f, new String[][]{new String[] {"667", "666"},  new String[] {"906", "913"}, new String[] {"751", "750"}}), 
            // // Tower of 4 
            // new TowerConfig(SP*2, 0, SP*0, new String[][]{new String[] {"498", "831"}, new String[] {"5410ecf58c7a", "5410ecf57cb7"}, new String[] {"1037", "1038"},  new String[] {"529", "528"}}), 
            // // Tower of 3 
            // new TowerConfig(SP*3, SP*.5f, SP*-.5f, new String[][]{new String[] {"1122", "1120"},  new String[] {"649", "648"}, new String[] {"1081", "972"}}), 
            // // Tower of 4 
            // new TowerConfig(SP*4, 0, SP*0, new String[][]{new String[] {"437", "1094"}, new String[] {"495", "494"}, new String[] {"1134", "546"},  new String[] {"517", "5410ecf583c6"}}), 
            // // Tower of 3 
            // new TowerConfig(SP*5, SP*.5f, SP*-.5f, new String[][]{new String[] {"571", "570"},  new String[] {"775", "774"}, new String[] {"531", "983"}}), 
            // // Tower of 4 
            // new TowerConfig(SP*6, 0, SP*0, new String[][]{new String[] {"1140", "5410ecfdb2dd"}, new String[] {"778", "970"}, new String[] {"747", "746"},  new String[] {"398", "1151"}}),
            // // Tower of 3 
            // new TowerConfig(SP*5.5f, SP*.5f, SP*-1, new String[][]{new String[] {"1030", "1040"},  new String[] {"5410ecf53668", "5410ecf5205e"}, new String[] {"449", "448"}}),
            // // Tower of 4 
            // new TowerConfig(SP*6, 0, SP*-2, new String[][]{new String[] {"977", ""}, new String[] {"5410ecfd56ff", "5410ecf6a91e"}, new String[] {"795", "5410ecf4bf7e"},  new String[] {"654", ""}}), 




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
        workspace = new Workspace(lx, ui, "shows/artbasel");
    }

    @Override
    public Workspace getWorkspace() {
        return workspace;
    }
}
