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
import com.symmetrylabs.slstudio.showplugins.FaderLimiter;
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

            //LEG1
            //STARTING FROM BOTTOM FRONT LEG FLOOR CUBES WORKING UP THE STRUCTURE
            new TowerConfig(SP*0, SP*0, SP*0, new String[][]{new String[] {"5410ecf5d87b", ""}}), 
            new TowerConfig(SP*0, SP*0, SP*2, new String[][]{new String[] {"726", ""}}),
            //ONE CUBE UP FROM THE GROUND
            new TowerConfig(SP*0, SP*1, SP*1, new String[][]{new String[] {"", "879"}}),
            new TowerConfig(SP*0, SP*1, SP*3, new String[][]{new String[] {"1037", ""}}), 

            //TOP ACROSS SECTION
            new TowerConfig(SP*0, SP*4, SP*3, new String[][]{new String[] {"", "5410ecfdb2d4"}}), 
            new TowerConfig(SP*-2, SP*4, SP*3, new String[][]{new String[] {"1043", ""}}), 
            new TowerConfig(SP*-4, SP*4, SP*3, new String[][]{new String[] {"5410ecf583c6", ""}}),

            //THREE CUBES UP FROM GROUND ACROSS SECTION
            new TowerConfig(SP*0, SP*3, SP*3, new String[][]{new String[] {"5410ecf4a602", ""}}), 
            new TowerConfig(SP*-1.5f, SP*3, SP*3, new String[][]{new String[] {"", "626"}}), 
            new TowerConfig(SP*-2.5f, SP*3, SP*3, new String[][]{new String[] {"511", ""}}),
            new TowerConfig(SP*-4, SP*3, SP*3, new String[][]{new String[] {"5410ecfd3c72", "1129"}}),

            //TWO CUBES UP FROM GROUND ACROSS SECTION
            new TowerConfig(SP*0, SP*2, SP*2, new String[][]{new String[] {"", "930"}}), 
            new TowerConfig(SP*-1, SP*2, SP*2, new String[][]{new String[] {"778", "970"}}), 
            new TowerConfig(SP*-2, SP*2, SP*2, new String[][]{new String[] {"447", "5410ecf4c257"}}),
            new TowerConfig(SP*-3, SP*2, SP*2, new String[][]{new String[] {"1081", "972"}}), 
            new TowerConfig(SP*-4, SP*2, SP*2, new String[][]{new String[] {"649", ""}}),

            //ONE CUBE FROM GROUND ACROSS SECTION
            // new TowerConfig(SP*0, SP*1, SP*3, new String[][]{new String[] {"", ""}}), 
            new TowerConfig(SP*-1.5f, SP*1, SP*3, new String[][]{new String[] {"470", ""}}), 
            new TowerConfig(SP*-2.5f, SP*1, SP*3, new String[][]{new String[] {"595", ""}}),
            // new TowerConfig(SP*-4, SP*1, SP*3, new String[][]{new String[] {"5410ecf5e2e2", ""}}),

            //LEG2
            //STARTING FROM BOTTOM FRONT LEG FLOOR CUBES WORKING UP THE STRUCTURE
            new TowerConfig(SP*-4, SP*0, SP*0, new String[][]{new String[] {"815", ""}}), 
            new TowerConfig(SP*-4, SP*0, SP*2, new String[][]{new String[] {"789", ""}}),
            //ONE CUBE UP FROM THE GROUND
            new TowerConfig(SP*-4, SP*1, SP*1, new String[][]{new String[] {"980", ""}}),
            new TowerConfig(SP*-4, SP*1, SP*3, new String[][]{new String[] {"5410ecf5e2e2", ""}}), 

            //ONE CUBE UP FROM THE GROUND
            new TowerConfig(SP*(0-4), SP*1, SP*1, new String[][]{new String[] {"", ""}}),
            new TowerConfig(SP*(0-4), SP*1, SP*3, new String[][]{new String[] {"", ""}}), 

            //TOP ACROSS SECTION
            new TowerConfig(SP*(0-4), SP*4, SP*3, new String[][]{new String[] {"", ""}}), 
            new TowerConfig(SP*(-2-4), SP*4, SP*3, new String[][]{new String[] {"", ""}}), 
            new TowerConfig(SP*(-4-4), SP*4, SP*3, new String[][]{new String[] {"", ""}}),

            //THREE CUBES UP FROM GROUND ACROSS SECTION
            new TowerConfig(SP*(0-4), SP*3, SP*3, new String[][]{new String[] {"", ""}}), 
            new TowerConfig(SP*(-1.5f-4), SP*3, SP*3, new String[][]{new String[] {"", ""}}), 
            new TowerConfig(SP*(-2.5f-4), SP*3, SP*3, new String[][]{new String[] {"", ""}}),
            new TowerConfig(SP*(-4-4), SP*3, SP*3, new String[][]{new String[] {"", ""}}),

            //TWO CUBES UP FROM GROUND ACROSS SECTION
            new TowerConfig(SP*(0-4), SP*2, SP*2, new String[][]{new String[] {"", ""}}), 
            new TowerConfig(SP*(-1-4), SP*2, SP*2, new String[][]{new String[] {"", ""}}), 
            new TowerConfig(SP*(-2-4), SP*2, SP*2, new String[][]{new String[] {"", ""}}),
            new TowerConfig(SP*(-3-4), SP*2, SP*2, new String[][]{new String[] {"", ""}}), 
            new TowerConfig(SP*(-4-4), SP*2, SP*2, new String[][]{new String[] {"", ""}}),

            //LEG3
            //STARTING FROM BOTTOM FRONT LEG FLOOR CUBES WORKING UP THE STRUCTURE
            new TowerConfig(SP*-8, SP*0, SP*0, new String[][]{new String[] {"", ""}}), 
            new TowerConfig(SP*-8, SP*0, SP*2, new String[][]{new String[] {"", ""}}),
            //ONE CUBE UP FROM THE GROUND
            new TowerConfig(SP*-8, SP*1, SP*1, new String[][]{new String[] {"", ""}}),
            new TowerConfig(SP*-8, SP*1, SP*3, new String[][]{new String[] {"", ""}}),

            //TOP ACROSS SECTION
            new TowerConfig(SP*(0-8), SP*4, SP*3, new String[][]{new String[] {"", ""}}), 
            new TowerConfig(SP*(-2-8), SP*4, SP*3, new String[][]{new String[] {"", ""}}), 
            new TowerConfig(SP*(-4-8), SP*4, SP*3, new String[][]{new String[] {"", ""}}),

            //THREE CUBES UP FROM GROUND ACROSS SECTION
            new TowerConfig(SP*(0-8), SP*3, SP*3, new String[][]{new String[] {"", ""}}), 
            new TowerConfig(SP*(-1.5f-8), SP*3, SP*3, new String[][]{new String[] {"", ""}}), 
            new TowerConfig(SP*(-2.5f-8), SP*3, SP*3, new String[][]{new String[] {"", ""}}),
            new TowerConfig(SP*(-4-8), SP*3, SP*3, new String[][]{new String[] {"", ""}}),

            //TWO CUBES UP FROM GROUND ACROSS SECTION
            new TowerConfig(SP*(0-8), SP*2, SP*2, new String[][]{new String[] {"", ""}}), 
            new TowerConfig(SP*(-1-8), SP*2, SP*2, new String[][]{new String[] {"", ""}}), 
            new TowerConfig(SP*(-2-8), SP*2, SP*2, new String[][]{new String[] {"", ""}}),
            new TowerConfig(SP*(-3-8), SP*2, SP*2, new String[][]{new String[] {"", ""}}), 
            new TowerConfig(SP*(-4-8), SP*2, SP*2, new String[][]{new String[] {"", ""}}), 

            //LEG4
            //STARTING FROM BOTTOM FRONT LEG FLOOR CUBES WORKING UP THE STRUCTURE
            new TowerConfig(SP*-12, SP*0, SP*0, new String[][]{new String[] {"", ""}}), 
            new TowerConfig(SP*-12, SP*0, SP*2, new String[][]{new String[] {"", ""}}),
            //ONE CUBE UP FROM THE GROUND
            new TowerConfig(SP*-12, SP*1, SP*1, new String[][]{new String[] {"", ""}}),
            new TowerConfig(SP*-12, SP*1, SP*3, new String[][]{new String[] {"", ""}}),

            //TOP ACROSS SECTION
            new TowerConfig(SP*(0-12), SP*4, SP*3, new String[][]{new String[] {"", ""}}), 
            new TowerConfig(SP*(-2-12), SP*4, SP*3, new String[][]{new String[] {"", ""}}), 
            new TowerConfig(SP*(-4-12), SP*4, SP*3, new String[][]{new String[] {"", ""}}),

            //THREE CUBES UP FROM GROUND ACROSS SECTION
            new TowerConfig(SP*(0-12), SP*3, SP*3, new String[][]{new String[] {"", ""}}), 
            new TowerConfig(SP*(-1.5f-12), SP*3, SP*3, new String[][]{new String[] {"", ""}}), 
            new TowerConfig(SP*(-2.5f-12), SP*3, SP*3, new String[][]{new String[] {"", ""}}),
            new TowerConfig(SP*(-4-12), SP*3, SP*3, new String[][]{new String[] {"", ""}}),

            //TWO CUBES UP FROM GROUND ACROSS SECTION
            new TowerConfig(SP*(0-12), SP*2, SP*2, new String[][]{new String[] {"", ""}}), 
            new TowerConfig(SP*(-1-12), SP*2, SP*2, new String[][]{new String[] {"", ""}}), 
            new TowerConfig(SP*(-2-12), SP*2, SP*2, new String[][]{new String[] {"", ""}}),
            new TowerConfig(SP*(-3-12), SP*2, SP*2, new String[][]{new String[] {"", ""}}), 
            new TowerConfig(SP*(-4-12), SP*2, SP*2, new String[][]{new String[] {"", ""}}),

            //LEG5
            //STARTING FROM BOTTOM FRONT LEG FLOOR CUBES WORKING UP THE STRUCTURE
            new TowerConfig(SP*-16, SP*0, SP*0, new String[][]{new String[] {"", ""}}), 
            new TowerConfig(SP*-16, SP*0, SP*2, new String[][]{new String[] {"", ""}}),
            //ONE CUBE UP FROM THE GROUND
            new TowerConfig(SP*-16, SP*1, SP*1, new String[][]{new String[] {"", ""}}),
            new TowerConfig(SP*-16, SP*1, SP*3, new String[][]{new String[] {"", ""}}),

            //TOP ACROSS SECTION
            new TowerConfig(SP*(0-16), SP*4, SP*3, new String[][]{new String[] {"", ""}}), 
            new TowerConfig(SP*(-2-16), SP*4, SP*3, new String[][]{new String[] {"", ""}}), 
            new TowerConfig(SP*(-4-16), SP*4, SP*3, new String[][]{new String[] {"", ""}}),

            //THREE CUBES UP FROM GROUND ACROSS SECTION
            new TowerConfig(SP*(0-16), SP*3, SP*3, new String[][]{new String[] {"", ""}}), 
            new TowerConfig(SP*(-1.5f-16), SP*3, SP*3, new String[][]{new String[] {"", ""}}), 
            new TowerConfig(SP*(-2.5f-16), SP*3, SP*3, new String[][]{new String[] {"", ""}}),
            new TowerConfig(SP*(-4-16), SP*3, SP*3, new String[][]{new String[] {"", ""}}),

            //TWO CUBES UP FROM GROUND ACROSS SECTION
            new TowerConfig(SP*(0-16), SP*2, SP*2, new String[][]{new String[] {"", ""}}), 
            new TowerConfig(SP*(-1-16), SP*2, SP*2, new String[][]{new String[] {"", ""}}), 
            new TowerConfig(SP*(-2-16), SP*2, SP*2, new String[][]{new String[] {"", ""}}),
            new TowerConfig(SP*(-3-16), SP*2, SP*2, new String[][]{new String[] {"", ""}}), 
            new TowerConfig(SP*(-4-16), SP*2, SP*2, new String[][]{new String[] {"", ""}}),







            // new TowerConfig(SP*1, SP*2, SP*-1, new String[][]{new String[] {"751", "750"}}),

            // new TowerConfig(SP*0, SP*3, SP*-1.5f, new String[][]{new String[] {"815", "814"}}), 
            // new TowerConfig(SP*1, SP*3, SP*-1.5f, new String[][]{new String[] {"789", "788"}}), 


            // new TowerConfig(SP*-.5f, SP*4, SP*-2, new String[][]{new String[] {"503", "502"}}), 
            // new TowerConfig(SP*.5f, SP*4, SP*-2, new String[][]{new String[] {"644", "5410ecfd5a45"}}),
            // new TowerConfig(SP*1.5f, SP*4, SP*-2, new String[][]{new String[] {"649", "648"}}),

            // new TowerConfig(SP*-.5f, SP*5, SP*-3, new String[][]{new String[] {"511", "510"}}), 
            // new TowerConfig(SP*.5f, SP*5, SP*-3, new String[][]{new String[] {"5410ecf5317f", "5410ecf68363"}}),
            // new TowerConfig(SP*1.5f, SP*5, SP*-3, new String[][]{new String[] {"523", "522"}}),

            // new TowerConfig(SP*-.5f, SP*6, SP*-3.5f, new String[][]{new String[] {"5410ecf4a602", "5410ecf4c5f7"}}), 
            // new TowerConfig(SP*.5f, SP*6, SP*-3.5f, new String[][]{new String[] {"665", "836"}}),
            // new TowerConfig(SP*1.5f, SP*6, SP*-3.5f, new String[][]{new String[] {"481", "480"}}),

            // // Hanging loose
            // new TowerConfig(SP*-.5f, SP*3.5f, SP*-3, new String[][]{new String[] {"483", "482"}}), 
            // new TowerConfig(SP*1.5f, SP*4.5f, SP*-4, new String[][]{new String[] {"708", "709"}}),
            // new TowerConfig(SP*-.5f, SP*5.5f, SP*-4.5f, new String[][]{new String[] {"1122", "1120"}}),

            // // Right side back going back
            // new TowerConfig(SP*2.5f, SP*6.5f, SP*-4, new String[][]{new String[] {"1035", "1036"}}),
            // new TowerConfig(SP*2.5f, SP*8, SP*-4, new String[][]{new String[] {"203", "156"}}),
            // new TowerConfig(SP*2.5f, SP*7.5f, SP*-3, new String[][]{new String[] {"581", "580"}}),
            // new TowerConfig(SP*2.5f, SP*7.5f, SP*-2, new String[][]{new String[] {"1043", "758"}}),
            // new TowerConfig(SP*2.5f, SP*7.5f, SP*-1, new String[][]{new String[] {"988", "768"}}),
            // new TowerConfig(SP*2.5f, SP*8.5f, SP*0, new String[][]{new String[] {"922", "1059"}}),
            // new TowerConfig(SP*2.5f, SP*5, SP*3, new String[][]{new String[] {"5410ecf5e2e2", "5410ecf6b63e"}}),

            // // Right side 
            // new TowerConfig(SP*2.5f, SP*6, SP*-5, new String[][]{new String[] {"611", "610"}}),
            // new TowerConfig(SP*2.5f, SP*6, SP*-6, new String[][]{new String[] {"910", "1124"}}),
            // new TowerConfig(SP*2.5f, SP*7, SP*-6.5f, new String[][]{new String[] {"1003", "989"}}),
            // new TowerConfig(SP*2.5f, SP*6, SP*-7, new String[][]{new String[] {"729", "728"}}),
            
            // new TowerConfig(SP*2.5f, SP*6, SP*-8, new String[][]{new String[] {"1080", "1061"}}),

            // new TowerConfig(SP*2.5f, SP*6, SP*-9, new String[][]{new String[] {"1128", "5410ecfdb2d4"}}),
            // new TowerConfig(SP*2.5f, SP*7, SP*-9.5f, new String[][]{new String[] {"905", "955"}}),
            // new TowerConfig(SP*2.5f, SP*6, SP*-10, new String[][]{new String[] {"726", "865"}}),
            // new TowerConfig(SP*2.5f, SP*6, SP*-11, new String[][]{new String[] {"795", "5410ecf4bf7e"}}),
            // new TowerConfig(SP*2.5f, SP*7, SP*-11.5f, new String[][]{new String[] {"595", "804"}}),
            // new TowerConfig(SP*2.5f, SP*6, SP*-12, new String[][]{new String[] {"448", "449"}}),
            // new TowerConfig(SP*2.5f, SP*6, SP*-13, new String[][]{new String[] {"398", "1151"}}),
            // new TowerConfig(SP*2.5f, SP*7, SP*-13, new String[][]{new String[] {"5410ecf5d87b", "5410ecf4c8aa"}}),

            // // Right side front
            // new TowerConfig(SP*2.5f, SP*6.5f, SP*-14.5f, new String[][]{new String[] {"437", "1094"}}),
            // new TowerConfig(SP*2.5f, SP*7.5f, SP*-14, new String[][]{new String[] {"700", "792"}, new String[] {"623", "1141"}}),
            // new TowerConfig(SP*2.5f, SP*7.5f, SP*-15, new String[][]{new String[] {"579", "578"}, new String[] {"1125", "677"}}),
            // new TowerConfig(SP*2.5f, SP*8.5f, SP*-16, new String[][]{new String[] {"679", "678"}}),

            // // Front curve
            // new TowerConfig(SP*2, SP*7.5f, SP*-16.5f, new String[][]{new String[] {"721", "720"}}),
            // new TowerConfig(SP*2, SP*8.5f, SP*-17, new String[][]{new String[] {"982", "981"}}),
           
            // new TowerConfig(SP*1, SP*8.5f, SP*-18, new String[][]{new String[] {"942", "930"}}),
            // new TowerConfig(SP*1, SP*7.5f, SP*-18, new String[][]{new String[] {"5410ecf53668", "5410ecf5205e"}}),
            // new TowerConfig(SP*0, SP*8.5f, SP*-18, new String[][]{new String[] {"1051", "1058"}}),
            // new TowerConfig(SP*0, SP*7.5f, SP*-18, new String[][]{new String[] {"687", "686"}}),
           
            // new TowerConfig(SP*-1, SP*8.5f, SP*-17, new String[][]{new String[] {"607", "606"}}),
            // new TowerConfig(SP*-1, SP*7.5f, SP*-16.5f, new String[][]{new String[] {"531", "143"}}),

            // // Left side front
            // new TowerConfig(SP*-1.5f, SP*8.5f, SP*-16, new String[][]{new String[] {"5410ecfd7b9c", "1005"}}),
            // new TowerConfig(SP*-1.5f, SP*7.5f, SP*-14, new String[][]{new String[] {"?14", "?13"}, new String[] {"1092", "951"}}),
            // new TowerConfig(SP*-1.5f, SP*7.5f, SP*-15, new String[][]{new String[] {"1142", "1131"}, new String[] {"1030", "1040"}}),
            // new TowerConfig(SP*-2.5f, SP*8, SP*-15, new String[][]{new String[] {"1089", "5410ecfd752a"}}),
            // new TowerConfig(SP*-1.5f, SP*6.5f, SP*-14.5f, new String[][]{new String[] {"5410ecf51b63", "1150"}}),
            // new TowerConfig(SP*-2.5f, SP*8.5f, SP*-13, new String[][]{new String[] {"667", "666"}}),

            // // Left side
            
            // new TowerConfig(SP*-1.5f, SP*6.5f, SP*-11.5f, new String[][]{new String[] {"1133", "1139"}}),
            // new TowerConfig(SP*-1.5f, SP*6, SP*-10.5f, new String[][]{new String[] {"749", "748"}}),

            // new TowerConfig(SP*-.5f, SP*6.5f, SP*-10, new String[][]{new String[] {"890", "879"}}),

            
            // new TowerConfig(SP*-1.5f, SP*5.5f, SP*-9.5f, new String[][]{new String[] {"5410ecfd3c72", "1129"}}),
            // new TowerConfig(SP*-.5f, SP*6.5f, SP*-9, new String[][]{new String[] {"775", "774"}}),
            // new TowerConfig(SP*-1.5f, SP*6, SP*-8.5f, new String[][]{new String[] {"659", "658"}}),
            // new TowerConfig(SP*-1.5f, SP*7, SP*-8, new String[][]{new String[] {"", "869"}}),
            // new TowerConfig(SP*-1.5f, SP*5.5f, SP*-7.5f, new String[][]{new String[] {"5410ecfd56ff", "5410ecf6a91e"}}),

            // new TowerConfig(SP*-1.5f, SP*6, SP*-6.5f, new String[][]{new String[] {"977", "1024"}}),
            // new TowerConfig(SP*-.5f, SP*6.5f, SP*-6, new String[][]{new String[] {"635", "634"}}),
            // new TowerConfig(SP*-1.5f, SP*6, SP*-5.5f, new String[][]{new String[] {"1125", "1141"}}),
            // new TowerConfig(SP*-1.5f, SP*6, SP*-4.5f, new String[][]{new String[] {"517", "5410ecf583c6"}}),
            // new TowerConfig(SP*-1.5f, SP*6.5f, SP*-3.5f, new String[][]{new String[] {"1081", "972"}}),

            // // 3 back cubes
            // new TowerConfig(SP*-1.5f, SP*8.5f, SP*-3, new String[][]{new String[] {"470", "660"}}),
            // new TowerConfig(SP*-1.5f, SP*6.5f, SP*4, new String[][]{new String[] {"1144", "1023"}}),
            // new TowerConfig(SP*-1.5f, SP*8.5f, SP*7, new String[][]{new String[] {"621", "952"}}),

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
        workspace = new Workspace(lx, ui, "shows/artbasel");
        FaderLimiter.attach(lx);
    }

    @Override
    public Workspace getWorkspace() {
        return workspace;
    }
}
