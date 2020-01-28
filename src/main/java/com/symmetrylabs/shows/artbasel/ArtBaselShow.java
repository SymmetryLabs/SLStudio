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

            new TowerConfig(SP*0, 0, SP*0, new String[][]{new String[] {"204", "1084"}}),
            new TowerConfig(SP*1, 0, SP*.75f, new String[][]{new String[] {"5410ecf48d2f", "1305"}}),
            new TowerConfig(SP*1.75f, 0, SP*1.75f, new String[][]{new String[] {"571", "570"}, new String[] {"1067", "1066"}}),
            // //Gap between cubes

            // //Cubes zig zag back to front
            // //1
            new TowerConfig(SP*(1.75f+1.5f), 0, SP*(1.75f+1f), new String[][]{new String[] {"873", "872"}}),
            // //Next cube left to right closest to downstage
            // //2
            new TowerConfig(SP*(1.75f+1.5f+.75f), 0, SP*(1.75f+1f-1.25f), new String[][]{new String[] {"1100", "1102"}}),
            // //Next cube to the right behind
            // //3
            new TowerConfig(SP*(1.75f+1.5f+.75f+.25f), 0, SP*(1.75f+1f-1.25f+1f), new String[][]{new String[] {"475", "474"}}),
            // //4
            new TowerConfig(SP*(1.75f+1.5f+.75f+.25f+.8f), 0, SP*(1.75f+1f-1.25f+1f-1f), new String[][]{new String[] {"1221", "1220"}}),
            // //5
            new TowerConfig(SP*(1.75f+1.5f+.75f+.25f+.8f+.5f), 0, SP*(1.75f+1f-1.25f+1f-1f+1f), new String[][]{new String[] {"953", "1110"}}),
            // //6
            new TowerConfig(SP*(1.75f+1.5f+.75f+.25f+.8f+.5f+.9f), 0, SP*(1.75f+1f-1.25f+1f-1f+1f-1.25f), new String[][]{new String[] {"781", "780"}}),
            // //GAP between cubes(small)

            // //7
            new TowerConfig(SP*(1.75f+1.5f+.75f+.25f+.8f+.5f+.9f+.9f), 0, SP*(1.75f+1f-1.25f+1f-1f+1f-1.25f+1.25f), new String[][]{new String[] {"775", "774"}}),
            // //8
            new TowerConfig(SP*(1.75f+1.5f+.75f+.25f+.8f+.5f+.9f+.9f+1.1f), 0, SP*(1.75f+1f-1.25f+1f-1f+1f-1.25f+1.25f-.5f), new String[][]{new String[] {"773", "772"}}),

            // //GAP between cubes(large)

            // //Cubes zig zag back to front
            // //1
            new TowerConfig(SP*(1.75f+1.5f+.75f+.25f+.8f+.5f+.9f+.9f+1.1f+1.75f), 0, SP*(1.75f+1f-1.25f+1f-1f+1f-1.25f+1.25f-.5f+.1f), new String[][]{new String[] {"1192", "1317"}, new String[] {"575", "1189"}}),
            // //2
            new TowerConfig(SP*(1.75f+1.5f+.75f+.25f+.8f+.5f+.9f+.9f+1.1f+1.75f+.5f), 0,     SP*(1.75f+1f-1.25f+1f-1f+1f-1.25f+1.25f-.5f+.1f-1.25f), new String[][]{new String[] {"1216", "1219"}, new String[] {"1363", "1228"}}),
            //3
            new TowerConfig(SP*(1.75f+1.5f+.75f+.25f+.8f+.5f+.9f+.9f+1.1f+1.75f+.5f+.5f), 0, SP*(1.75f+1f-1.25f+1f-1f+1f-1.25f+1.25f-.5f+.1f-1.25f+1.1f), new String[][]{new String[] {"765", "764"}}),
            //4
            new TowerConfig(SP*(1.75f+1.5f+.75f+.25f+.8f+.5f+.9f+.9f+1.1f+1.75f+.5f+.5f+1f), 0, SP*(1.75f+1f-1.25f+1f-1f+1f-1.25f+1.25f-.5f+.1f-1.25f+1.1f-1.5f), new String[][]{new String[] {"771", "770"}}),
            //5
            new TowerConfig(SP*(1.75f+1.5f+.75f+.25f+.8f+.5f+.9f+.9f+1.1f+1.75f+.5f+.5f+1f+.25f), 0, SP*(1.75f+1f-1.25f+1f-1f+1f-1.25f+1.25f-.5f+.1f-1.25f+1.1f-1.5f+2), new String[][]{new String[] {"1053", "1062"}}),
            //6
            new TowerConfig(SP*(1.75f+1.5f+.75f+.25f+.8f+.5f+.9f+.9f+1.1f+1.75f+.5f+.5f+1f+.25f+.9f), 0, SP*(1.75f+1f-1.25f+1f-1f+1f-1.25f+1.25f-.5f+.1f-1.25f+1.1f-1.5f+2f-1.5f), new String[][]{new String[] {"975", "489"}}),

            //FAR RIGHT 3 BACK TO FRONT
            //7
            new TowerConfig(SP*(1.75f+1.5f+.75f+.25f+.8f+.5f+.9f+.9f+1.1f+1.75f+.5f+.5f+1f+.25f+.9f+.6f), 0, SP*(1.75f+1f-1.25f+1f-1f+1f-1.25f+1.25f-.5f+.1f-1.25f+1.1f-1.5f+2f-1.5f+1.5f), new String[][]{new String[] {"415", "1050"}}),
            new TowerConfig(SP*(1.75f+1.5f+.75f+.25f+.8f+.5f+.9f+.9f+1.1f+1.75f+.5f+.5f+1f+.25f+.9f+.6f+.5f), 0, SP*(1.75f+1f-1.25f+1f-1f+1f-1.25f+1.25f-.5f+.1f-1.25f+1.1f-1.5f+2f-1.5f+1.5f-1f), new String[][]{new String[] {"928", "918"}}),
            new TowerConfig(SP*(1.75f+1.5f+.75f+.25f+.8f+.5f+.9f+.9f+1.1f+1.75f+.5f+.5f+1f+.25f+.9f+.6f+.5f+.1f), 0, SP*(1.75f+1f-1.25f+1f-1f+1f-1.25f+1.25f-.5f+.1f-1.25f+1.1f-1.5f+2f-1.5f+1.5f-1f-1.5f), new String[][]{new String[] {"591", "1056"}, new String[] {"957", "1226"}}),

//LAYER 1.5
            //CUBE SPLITTIGN ON THE FAR LEFT
            new TowerConfig(SP*(1.75f+1f+.75f-1f), SP*1.5f, SP*(1.75f+.25f-1.1f-.2f), new String[][]{new String[] {"723", "196"}}),
            //CUBE SPLITTING NEAR THE MIDDLE GAP
            new TowerConfig(SP*(1.75f+1.5f+.75f+.25f+.8f+.5f+.9f+.9f+1.1f+1.75f+.5f-1f), SP*1.5f,     SP*(1.75f+1f-1.25f+1f-1f+1f-1.25f+1.25f-.5f+.1f-1.25f-.5f), new String[][]{new String[] {"498", "831"}}),


//LAYER 2
            //1
            new TowerConfig(SP*(.75f), SP*1, SP*(.75f), new String[][]{new String[] {"572", "806"}}),
            //2
            //STRAIGHT TOWER (ALREADY MAPPED ABOVE)
            // new TowerConfig(SP*1.75f, 0, SP*1.75f, new String[][]{new String[] {"919", "910"}, new String[] {"919", "910"}}),
            //3
            new TowerConfig(SP*(1.75f+1f), SP*1, SP*(1.75f+.25f), new String[][]{new String[] {"971", "974"}}),
            //4
            new TowerConfig(SP*(1.75f+1f+.75f), SP*1, SP*(1.75f+.25f-1.1f), new String[][]{new String[] {"741", "740"}}),
            //5
            new TowerConfig(SP*(1.75f+1f+.75f+.25f), SP*1, SP*(1.75f+.25f-1.1f+1.1f), new String[][]{new String[] {"827", "826"}}),
            //6
            new TowerConfig(SP*(1.75f+1f+.75f+.25f+1f), SP*1, SP*(1.75f+.25f-1.1f+1.1f-1f), new String[][]{new String[] {"143", "150"}}),
            //7
            new TowerConfig(SP*(1.75f+1f+.75f+.25f+1f+.1f), SP*1, SP*(1.75f+.25f-1.1f+1.1f-1f+1f), new String[][]{new String[] {"583", "582"}}),
            //8
            new TowerConfig(SP*(1.75f+1f+.75f+.25f+1f+.1f+1f), SP*1, SP*(1.75f+.25f-1.1f+1.1f-1f+1f-2f), new String[][]{new String[] {"361", "1007"}}),
            //9
            new TowerConfig(SP*(1.75f+1f+.75f+.25f+1f+.1f+1f+.5f), SP*1, SP*(1.75f+.25f-1.1f+1.1f-1f+1f-2f+2f), new String[][]{new String[] {"609", "608"}}),
            //10
            new TowerConfig(SP*(1.75f+1f+.75f+.25f+1f+.1f+1f+.5f+.7f), SP*1, SP*(1.75f+.25f-1.1f+1.1f-1f+1f-2f+2f-1f), new String[][]{new String[] {"1012", "1039"}}),
            //11
            new TowerConfig(SP*(1.75f+1f+.75f+.25f+1f+.1f+1f+.5f+.7f+.5f), SP*1, SP*(1.75f+.25f-1.1f+1.1f-1f+1f-1f+2f-2f+1f), new String[][]{new String[] {"1354", "1355"}}),



            //BIG GAP

            //1
            //STRAIGHT TOWER (ALREADY MAPPED ABOVE)
            //new TowerConfig(SP*(1.75f+1.5f+.75f+.25f+.8f+.5f+.9f+.9f+1.1f+1.75f), 0, SP*(1.75f+1f-1.25f+1f-1f+1f-1.25f+1.25f-.5f+.1f), new String[][]{new String[] {"919", "910"}}),
            //2
            //STRAIGHT TOWER (ALREADY MAPPED ABOVE)
            //new TowerConfig(SP*(1.75f+1.5f+.75f+.25f+.8f+.5f+.9f+.9f+1.1f+1.75f+.5f), 0,     SP*(1.75f+1f-1.25f+1f-1f+1f-1.25f+1.25f-.5f+.1f-1.25f), new String[][]{new String[] {"919", "910"}, new String[] {"919", "910"}}),
            //3
            new TowerConfig  (SP*(1.75f+1.5f+.75f+.25f+.8f+.5f+.9f+.9f+1.1f+1.75f+.5f+1f),SP*1,SP*(1.75f+1f-1.25f+1f-1f+1f-1.25f+1.25f-.5f+.1f-1.25f+1.1f), new String[][]{new String[] {"1339", "1340"}}),
            //4
            new TowerConfig  (SP*(1.75f+1.5f+.75f+.25f+.8f+.5f+.9f+.9f+1.1f+1.75f+.5f+1f+.75f),SP*1,      SP*(1.75f+1f-1.25f+1f-1f+1f-1.25f+1.25f-.5f+.1f-1.25f+1.1f-1.25f), new String[][]{new String[] {"654", "1083"}}),
            //5
            new TowerConfig  (SP*(1.75f+1.5f+.75f+.25f+.8f+.5f+.9f+.9f+1.1f+1.75f+.5f+1f+.75f+.5f) ,SP*1, SP*(1.75f+1f-1.25f+1f-1f+1f-1.25f+1.25f-.5f+.1f-1.25f+1.1f-1.25f+2f), new String[][]{new String[] {"1302", "1303"}}),
            //6
            new TowerConfig  (SP*(1.75f+1.5f+.75f+.25f+.8f+.5f+.9f+.9f+1.1f+1.75f+.5f+1f+.75f+.5f+.75f) ,SP*1, SP*(1.75f+1f-1.25f+1f-1f+1f-1.25f+1.25f-.5f+.1f-1.25f+1.1f-1.25f+2f-1.25f), new String[][]{new String[] {"531", "983"}}),
            //7
            //STRAIGHT TOWERALREADY MAPPED ABOVE
            //new TowerConfig(SP*(1.75f+1.5f+.75f+.25f+.8f+.5f+.9f+.9f+1.1f+1.75f+.5f+.5f+1f+.25f+.9f+.6f+.5f+.1f), 0, SP*(1.75f+1f-1.25f+1f-1f+1f-1.25f+1.25f-.5f+.1f-1.25f+1.1f-1.5f+2f-1.5f+1.5f-1f-1.5f), new String[][]{new String[] {"919", "910"}, new String[] {"919", "910"}}),


//LAYER 3   
            //1
            new TowerConfig(SP*(1.75f+.6f), SP*2, SP*(1.75f+.1f), new String[][]{new String[] {"977", "1024"}}),
            //2
            new TowerConfig(SP*(1.75f+.6f+1.1f), SP*2, SP*(1.75f+.1f+1f), new String[][]{new String[] {"567", "566"}}),
            //3
            new TowerConfig(SP*(1.75f+.6f+1.1f+1.25f), SP*2,    SP*(1.75f+.1f+1f-.5f), new String[][]{new String[] {"1325", "1326"}}),
            //4
            new TowerConfig(SP*(1.75f+.6f+1.1f+1.25f+1f), SP*2, SP*(1.75f+.1f+1f-.5f-.5f), new String[][]{new String[] {"517", "1314"}}),
            //5
            new TowerConfig(SP*(1.75f+.6f+1.1f+1.25f+1f+.5f), SP*2, SP*(1.75f+.1f+1f-.5f-.5f+1f), new String[][]{new String[] {"813", "812"}}),
            //6
            new TowerConfig(SP*(1.75f+.6f+1.1f+1.25f+1f+.5f+1.1f), SP*2, SP*(1.75f+.1f+1f-.5f-.5f+1f-.5f-.1F), new String[][]{new String[] {"1367", "1368"}}),


            //BIG GAP

            //1
            //TOWER OF 3
            new TowerConfig(SP*(1.75f+1.5f+.75f+.25f+.8f+.5f+.9f+.9f+1.1f+1.75f-.1f), SP*2,        SP*(1.75f+1f-1.25f+1f-1f+1f-1.25f+1.25f-.5f+.1f-.75f+.1f), new String[][]{new String[] {"553", "552"}, new String[] {"849", "919"}, new String[] {"1249", "1123"}}),
            //2
            //SLIGHTLY LOWER TO THE LEFT OF THE TOWER OF 3
            new TowerConfig(SP*(1.75f+1.5f+.75f+.25f+.8f+.5f+.9f+.9f+1.1f+1.75f-.1f-1f), SP*1.85f, SP*(1.75f+1f-1.25f+1f-1f+1f-1.25f+1.25f-.5f+.1f-.75f+.1f+.1f), new String[][]{new String[] {"942", "930"}}),
            //3
            //2ND CUBE ON THE DOUBLED UP TOWER TO THE RIGHT OF THE TOWER OF 3

            new TowerConfig  (SP*(1.75f+1.5f+.75f+.25f+.8f+.5f+.9f+.9f+1.1f+1.75f+.5f+1f),SP*2,SP*(1.75f+1f-1.25f+1f-1f+1f-1.25f+1.25f-.5f+.1f-1.25f+1.1f), new String[][]{new String[] {"1238", "1237"}}),

            //4
            //TOWER OF TWO TO THE RIGHT
            new TowerConfig  (SP*(1.75f+1.5f+.75f+.25f+.8f+.5f+.9f+.9f+1.1f+1.75f+.5f+1f+1f),SP*2,SP*(1.75f+1f-1.25f+1f-1f+1f-1.25f+1.25f-.5f+.1f-1.25f+1.1f-.1f), new String[][]{new String[] {"906", "913"}, new String[] {"751", "750"}}),

            //5
            new TowerConfig  (SP*(1.75f+1.5f+.75f+.25f+.8f+.5f+.9f+.9f+1.1f+1.75f+.5f+1f+1f+.1f),SP*2,SP*(1.75f+1f-1.25f+1f-1f+1f-1.25f+1.25f-.5f+.1f-1.25f+1.1f-.1f-1.5f), new String[][]{new String[] {"1030", "1040"}}),


//LAYER 3.5
            //FAR RIGHT TOWER THAT SPLITS THE TOWER OF 2
            new TowerConfig  (SP*(1.75f+1.5f+.75f+.25f+.8f+.5f+.9f+.9f+1.1f+1.75f+.5f+1f+1f+.1f+1f),SP*2.5F,SP*(1.75f+1f-1.25f+1f-1f+1f-1.25f+1.25f-.5f+.1f-1.25f+1.1f-.1f-1.5f-.6f), new String[][]{new String[] {"1126", "111353"}}),
            //SPLITTER CUBETO THE RIGHT AND SLIGHTLY IN FRONT OF THE TOWER OF 3
            new TowerConfig  (SP*(1.75f+1.5f+.75f+.25f+.8f+.5f+.9f+.9f+1.1f+1.75f+.5f+1f+1f+.1f+1f-1.5f),SP*2.5F,SP*(1.75f+1f-1.25f+1f-1f+1f-1.25f+1.25f-.5f+.1f-1.25f+1.1f-.1f-1.5f-.6f-.4f), new String[][]{new String[] {"529", "528"}}),

//LAYER 4

            //FAR LEFT CUBE
            new TowerConfig(SP*(1.75f+.6f+.5f), SP*3, SP*(1.75f+.1f+.1f), new String[][]{new String[] {"140", "1134"}}),
            //MIDDLE CUBE 2ND OF THE TOWER OF 2
            new TowerConfig(SP*(1.75f+.6f+1.1f+1.25f), SP*3,    SP*(1.75f+.1f+1f-.5f), new String[][]{new String[] {"1037", "1038"}}),
            //CUBE TO THE RIGHT OF THE SIGN
            new TowerConfig(SP*(1.75f+.6f+1.1f+1.25f+2f), SP*3,    SP*(1.75f+.1f+1f-.5f+.1f), new String[][]{new String[] {"1072", "1073"}}),
            //CUBE TO THE RIGHT OF THE MIDDLE OF THE TOWER OF 3
            new TowerConfig(SP*(1.75f+1.5f+.75f+.25f+.8f+.5f+.9f+.9f+1.1f+1.75f-.1f+1.6f), SP*3, SP*(1.75f+1f-1.25f+1f-1f+1f-1.25f+1.25f-.5f+.1f-.75f+.1f+.2f), new String[][]{new String[] {"449", "448"}}),

            //CUBE TO THE RIGHT DOUBLE STACKED
            //ALREADY MAPPED

//LAYER 5

            //LEFT OF THE TOWER OF 2
            new TowerConfig(SP*(1.75f+.6f+1.1f+1.25f+2f-1f), SP*4,    SP*(1.75f+.1f+1f-.5f+.1f), new String[][]{new String[] {"637", "1057"}}),
            //2nd CUBE OF TOWER OF 2
            new TowerConfig(SP*(1.75f+.6f+1.1f+1.25f+2f), SP*4,    SP*(1.75f+.1f+1f-.5f+.1f), new String[][]{new String[] {"1343", "1344"}}),
            //CUBE TO THE RIGHT OF THE TOWER OF 3
            new TowerConfig(SP*(1.75f+1.5f+.75f+.25f+.8f+.5f+.9f+.9f+1.1f+1.75f-.1f+1F), SP*4, SP*(1.75f+1f-1.25f+1f-1f+1f-1.25f+1.25f-.5f+.1f-.75f+.1f+.2f), new String[][]{new String[] {"1081", "972"}}),
















            //Next cube left to right closest to downstage
            // new TowerConfig(SP*0, 0, SP*0, new String[][]{new String[] {"919", "910"}}),
            // new TowerConfig(SP*0, 0, SP*0, new String[][]{new String[] {"919", "910"}}),
            // new TowerConfig(SP*0, 0, SP*0, new String[][]{new String[] {"919", "910"}}),
            // new TowerConfig(SP*0, 0, SP*0, new String[][]{new String[] {"919", "910"}}),
            // new TowerConfig(SP*0, 0, SP*0, new String[][]{new String[] {"919", "910"}}),
            // new TowerConfig(SP*0, 0, SP*0, new String[][]{new String[] {"919", "910"}}),
            // new TowerConfig(SP*0, 0, SP*0, new String[][]{new String[] {"919", "910"}}),
            // new TowerConfig(SP*0, 0, SP*0, new String[][]{new String[] {"919", "910"}}),
            // new TowerConfig(SP*0, 0, SP*0, new String[][]{new String[] {"919", "910"}}),
            // new TowerConfig(SP*0, 0, SP*0, new String[][]{new String[] {"919", "910"}}),
            // new TowerConfig(SP*0, 0, SP*0, new String[][]{new String[] {"919", "910"}}),
            // new TowerConfig(SP*0, 0, SP*0, new String[][]{new String[] {"919", "910"}}),
            // new TowerConfig(SP*0, 0, SP*0, new String[][]{new String[] {"919", "910"}}),




        

 
};


//////////////////////////////////////////////////////////////////////////////
///////////////////////// FAENA POP UP MAPPINGS //////////////////////////////
//////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////


        //stick the tower configs here, like this:
        //BACK ROW
// //HOUSE LEFT TOWER 1 ROW 1
//             new TowerConfig(SP * -1.5f, SP * 0, SP * 0, new String[][]{
//                 new String[] {"1041", "1025"},
//                 new String[] {"1027", "612"},
//                 new String[] {"773", "772"},
//                 new String[] {"916", "917"},
//                 new String[] {"905", "955"},
//             }),
// //TOWER 2 ROW 1
//                 new TowerConfig(-SP* 3.0f, SP * 0, SP * 0, new String[][]{
//                 new String[] {"919", "910"},
//                 new String[] {"637", "1057"},
//                 new String[] {"529", "528"},
//                 new String[] {"516", "1101"},                
//             }),

// //TOWER 3 ROW 1
//             //TOWER OF 6
//             new TowerConfig(SP * 0, SP * 0, SP * 0, new String[][]{
//                 new String[] {"1026", "1025"},
//                 new String[] {"791", "790"},
//                 new String[] {"487", "486"},
//                 new String[] {"515", "514"},
//                 new String[] {"491", "490"},
//                 new String[] {"477", "476"},
//             }),
// //TOWER 4 ROW 1
//             //tower of five 
//             new TowerConfig(SP * 1.5f,  0, SP * 0, new String[][]{
//                 new String[] {"727", "726"},
//                 new String[] {"945", "901"},
//                 new String[] {"645", "978"},
//                 new String[] {"604", "1087"},
//                 new String[] {"960", "974"},
//             }),
// //TOWER 5 ROW 1
//             //tower of four
//                         new TowerConfig(SP * 3.0f, SP * 0, SP * 0, new String[][]{
//                 new String[] {"1091", "1090"},
//                 new String[] {"771", "770"},
//                 new String[] {"1111", "1112"},
//                 new String[] {"949", "946"},                
//             }),


// //ROW 2
//                 //tower of four
//             new TowerConfig(-SP*3.7f, 0, -SP * 1, new String[][]{
//                 new String[] {"741", "740"},
//                 new String[] {"1099", "1098"},
//                 new String[] {"924", "923"},
//                 new String[] {"595", "804"},                
//             }),

//                 //tower of four
//             new TowerConfig(-SP*2.25f, SP * 0, -SP * 1, new String[][]{
//                 new String[] {"757", "756"},
//                 new String[] {"1096", "1095"},
//                 new String[] {"867", "866"},
//                 new String[] {"936", "921"},                
//             }),
    
//             //tower of four
//             new TowerConfig(-SP*.7f, SP * 0, -SP * 1, new String[][]{
//                 new String[] {"1075", "593"},
//                 new String[] {"783", "782"},
//                 new String[] {"495", "494"},
//                 new String[] {"441", "440"},                
//             }),

//             new TowerConfig(SP * .7f, 0, -SP * 1,  new String[][] {
//                 new String[] {"1109", "1108"},
//                 new String[] {"654", "1003"},
//                 new String[] {"929", "995"},
//                 new String[] {"928", "918"},
//                 new String[] {"1004", "1018"},
//             }),

// //ROW 3

//                 //tower of three 
//             new TowerConfig(SP * -4, 0, -SP * 2, new String[][]{
//                 new String[] {"957", "998"},
//                 new String[] {"1067", "1066"},
//                 new String[] {"481", "480"},
//             }),

//                 //tower of three 
//             new TowerConfig(SP * -1.5f, SP * 0, -SP * 2, new String[][]{
//                 new String[] {"1009", "1008"},
//                 new String[] {"935", "999"},
//                 new String[] {"997", "996"},
//             }),

//             //tower of four
//             new TowerConfig(SP*1.5f, SP * 0, -SP * 2, new String[][]{
//                 new String[] {"876", "520"},
//                 new String[] {"944", "947"},
//                 new String[] {"914", "847"},
//                 new String[] {"818", "464"},                
//             }),



//                 //tower of three 
//             new TowerConfig(SP * 3.5f, 0, -SP * 2, new String[][]{
//                 new String[] {"1028", "1029"},//UNLABLED CUBES
//                 new String[] {"926", "998"},
//                 new String[] {"906", "912"},
//             }),

//                 //tower of two 
//             new TowerConfig(SP * 5f, SP * 0, -SP * 2, new String[][]{
//                 new String[] {"896", "898"},
//                 new String[] {"1104", "1103"},
//             }),

// //ROW 4


//                     //tower of two 
//             new TowerConfig(SP * -4.5f, SP * 0, -SP * 3, new String[][]{
//                 new String[] {"719", "718"},
//                 new String[] {"817", "682"},
//             }),

//                     //tower of two 
//             new TowerConfig(-SP * 2, SP * 0, -SP * 3, new String[][]{
//                 new String[] {"679", "678"},
//                 new String[] {"707", "706"},
//             }),

//             new TowerConfig(SP * 3.0f, SP * 0, -SP * 3, new String[][]{
//                 new String[] {"1052", "830"},
//                 new String[] {"1085", "920"},
//                 new String[] {"473", "472"},
//                 new String[] {"455", "454"},                
//             }),

//                         //tower of two 
//             new TowerConfig(SP * 4, SP * 0, -SP * 3, new String[][]{
//                 new String[] {"713", "712"},
//                 new String[] {"940", "948"},
//             }),

//    };

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
