package com.symmetrylabs.layouts.cubes;

//package com.symmetrylabs.layouts.cubes;

import java.util.*;
import java.lang.ref.WeakReference;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.lang.Float;


import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.output.SLController;
import com.symmetrylabs.util.CubePhysicalIdMap;
import com.symmetrylabs.util.listenable.SetListener;
import heronarts.lx.LX;
import heronarts.lx.output.FadecandyOutput;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.transform.LXTransform;
import heronarts.p3lx.ui.UI2dScrollContext;

import com.symmetrylabs.layouts.Layout;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.network.NetworkMonitor;
import com.symmetrylabs.slstudio.network.NetworkDevice;
import com.symmetrylabs.util.dispatch.Dispatcher;
import com.symmetrylabs.util.listenable.ListenableSet;
import com.symmetrylabs.util.Utils;
import static com.symmetrylabs.util.DistanceUtils.*;


/**
 * This file implements the mapping functions needed to lay out the cubes.
 */
public class SummerStageLayout extends CubesLayout {

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
    static final float SP = 26f;
    static final float JUMP = TOWER_HEIGHT+TOWER_VERTICAL_SPACING;

    static final float INCHES_PER_METER = 39.3701f;

    static final ClusterConfig[] clusters = new ClusterConfig[]{

        new ClusterConfig("CLUSTER_A", SP*24, SP*14, 0, new TowerConfig[] {
            // col 1
            new TowerConfig(SP * 0, SP * 1, SP * 0, new String[][]{new String[]{"485", "484"}}),
            new TowerConfig(SP * 0, SP * 0, SP * -1, new String[][]{new String[]{"487", "486"}, new String[]{"515", "514"}, new String[]{"491", "490"}, new String[]{"477", "476"}}),
            // col 2
            new TowerConfig(SP * 1, SP * 1, SP * 0, new String[][]{new String[]{"503", "502"}, new String[]{"511", "510"}}),
            // col 3
            new TowerConfig(SP * 2, SP * 2, SP * 0, new String[][]{new String[]{"769", "768"}}),
            // col 4
            new TowerConfig(SP * 3, SP * 0, SP * 0, new String[][]{new String[]{"565", "564"}, new String[]{"473", "472"}, new String[]{"455", "454"}})
        }),

        new ClusterConfig("CLUSTER_B", SP*20, SP*13, 0, new TowerConfig[] {
            // col 1
            new TowerConfig(SP * 0, SP * 2, SP * 0, new String[][]{new String[]{"541", "540"}, new String[]{"581", "580"}, new String[]{"453", "452"}}),
            // col 2
            new TowerConfig(SP * 1, SP * 2, SP * 0, new String[][]{new String[]{"577", "576"}, new String[]{"471", "470"}, new String[]{"497", "496"}}),
            // col 3
            new TowerConfig(SP * 2, SP * 1, SP * 0, new String[][]{new String[]{"751", "750"}, new String[]{"527", "526"}, new String[]{"467", "466"}, new String[]{"507", "508"}, new String[]{"517", "516"}}),
            // col 4
            new TowerConfig(SP * 3, SP * 0, SP * 0, new String[][]{new String[]{"729", "728"}, new String[]{"749", "748"}, new String[]{"445", "444"}, new String[]{"483", "482"}})
        }),

        new ClusterConfig("CLUSTER_C", SP*16, SP*14, 0, new TowerConfig[] {
            // col 1
            new TowerConfig(SP * 0, SP * 1, SP * 0, new String[][]{new String[]{"628", "629"}, new String[]{"627", "626"}}),
            new TowerConfig(SP * 0, SP * 0, SP * -1, new String[][]{new String[]{"661", "660"}, new String[]{"659", "658"}}),
            // col 2
            new TowerConfig(SP * 1, SP * 1, SP * 0, new String[][]{new String[]{"?", "676"}, new String[]{"815", "5410ecf55605"}}), // this mac not in table
            new TowerConfig(SP * 1, SP * 0, SP * -1, new String[][]{new String[]{"663", "662"}, new String[]{"617", "616"}, new String[]{"?", "?"}}),
            // col 3
            new TowerConfig(SP * 2, SP * 1, SP * 0, new String[][]{new String[]{"639", "638"}, new String[]{"615", "614"}}),
            new TowerConfig(SP * 2, SP * 0, SP * -1, new String[][]{new String[]{"799", "798"}, new String[]{"619", "618"}, new String[]{"479", "478"}}),
            // col 4
            new TowerConfig(SP * 3, SP * 0, SP * 0, new String[][]{new String[]{"657", "656"}}),
            new TowerConfig(SP * 3, SP * 0, SP * -1, new String[][]{new String[]{"687", "688"}}),
            new TowerConfig(SP * 3, SP * 2, SP * -1, new String[][]{new String[]{"715", "714"}}),
        }),

        new ClusterConfig("CLUSTER_D", SP*13, SP*14, 0, new TowerConfig[]{
            // col 1
            new TowerConfig(SP * 0, SP * 1, SP * -1, new String[][]{new String[]{"509", "508"}, new String[]{"567", "566"}, new String[]{"583", "582"}}),
            new TowerConfig(SP * 0, SP * 1, SP * -2, new String[][]{new String[]{"?", "?"}, new String[]{"?", "?"}, new String[]{"?", "?"}}),
            // col 2
            new TowerConfig(SP * 1, SP * 0, SP * 0, new String[][]{new String[]{"765", "764"}, new String[]{"609", "608"}, new String[]{"781", "780"}, new String[]{"669", "668"}}),
            new TowerConfig(SP * 1, SP * 1, SP * -1, new String[][]{new String[]{"679", "678"}, new String[]{"643", "642"}, new String[]{"787", "786"}, new String[]{"721", "766"}}),
            // col 3
            new TowerConfig(SP * 2, SP * 0, SP * 0, new String[][]{new String[]{"523", "522"}, new String[]{"525", "524"}, new String[]{"457", "456"}, new String[]{"607", "606"}, new String[]{"571", "570"}}),
        }),

        new ClusterConfig("CLUSTER_E", SP*10, SP*11, SP*2, new TowerConfig[]{
            // col 1
//            new TowerConfig(SP * 0, SP * 2, SP * 0, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}}),
//            new TowerConfig(SP * 0, SP * 3, SP * -1, new String[][]{new String[]{"0", "0"}}),
//            new TowerConfig(SP * 0, SP * 3, SP * -2, new String[][]{new String[]{"0", "0"}}),
//            new TowerConfig(SP * 0, SP * 4, SP * -3, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}}),
            // col 2
            //new TowerConfig(SP * 1, SP * 0, SP * 0, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}}),
            new TowerConfig(SP * 1, SP * 3, SP * -3, new String[][]{new String[]{"0", "784"}, new String[]{"563", "562"}, new String[]{"777", "0"}}),
            new TowerConfig(SP * 1, SP * 4, SP * -4, new String[][]{new String[]{"717", "716"}}),
            // col 3
            new TowerConfig(SP * 2, SP * 4, SP * -4, new String[][]{new String[]{"707", "706"}}),
        }),

        // done
        new ClusterConfig("CLUSTER_F", SP*9, SP*7, 0, new TowerConfig[]{
            // col 1
            new TowerConfig(SP * 0, SP * 0, SP * 0, new String[][]{new String[]{"481", "480"}, new String[]{"441", "440"}, new String[]{"495", "494"}, new String[]{"771", "770"}, new String[]{"691", "690"}}),
            // col 2
            new TowerConfig(SP * 1, SP * 0, SP * 0, new String[][]{new String[]{"887", "882"}, new String[]{"890", "879"}, new String[]{"451", "450"}, new String[]{"803", "802"}, new String[]{"449", "448"}}),
        }),

        // done
        new ClusterConfig("CLUSTER_G", SP*9, SP*2, 0, new TowerConfig[]{
            // col 1
            new TowerConfig(SP * 0, SP * 1, SP * 0, new String[][]{new String[]{"931", "938"}, new String[]{"928", "918"}, new String[]{"906", "912"}}),
            // col 2
            new TowerConfig(SP * 1, SP * 0, SP * 0, new String[][]{new String[]{"925", "934"}, new String[]{"916", "917"}, new String[]{"924", "923"}, new String[]{"922", "932"}, new String[]{"878", "888"}}),
            // col 3
            new TowerConfig(SP * 2, SP * 0, SP * 0, new String[][]{new String[]{"936", "921"}, new String[]{"926", "998"}, new String[]{"939", "933"}, new String[]{"935", "999"}, new String[]{"904", "920"}, new String[]{"927", "915"}, new String[]{"919", "910"}}),
        }),

        // done (almost)
        new ClusterConfig("CLUSTER_H", SP*7, SP*1, 0, new TowerConfig[]{
            // col 1
            new TowerConfig(SP * 0, SP * 8, SP * 0, new String[][]{new String[]{"997", "996"}, new String[]{"957", "956"}}),
            // col 2 (529, 528, 366, 702, 817, 458), (763, 742, 601, 600, 762, 489) <- not sure what order these towers of 3 will be added
            new TowerConfig(SP * 1, SP * 0, SP * 0, new String[][]{new String[]{"366", "702"}, new String[]{"817", "458"}, new String[]{"763", "742"}, new String[]{"601", "600"}, new String[]{"762", "489"}, new String[]{"549", "574"}, new String[]{"549", "574"}, new String[]{"551", "550"}, new String[]{"945", "901"}}),
        }),

        new ClusterConfig("CLUSTER_I", SP*0, 0, 0, new TowerConfig[]{
            // col 1
            new TowerConfig(SP * 0, SP * 5, SP * 0, new String[][]{new String[]{"1032", "1022"}}),
            new TowerConfig(SP * 0, SP * 8, SP * 0, new String[][]{new String[]{"1026", "1025"}}),
            // col 2                                                                                                                                        // 1                                            2                                                3                                                4                                                    5                                                6                                                    7                                                    8                                                        9                                                    10                                                11
            new TowerConfig(SP * 1, SP * -1, SP * 0, new String[][]{new String[]{"529", "528"}, new String[]{"925", "934"}, new String[]{"1030", "1040"}, new String[]{"1001", "1000"}, new String[]{"985", "979"}, new String[]{"988", "977"}, new String[]{"991", "1020"}, new String[]{"1037", "1038"}, new String[]{"1012", "1039"}, new String[]{"1023", "1024"}, new String[]{"647", "646"}}),
            // col 3
            new TowerConfig(SP * 2, SP * 9, SP * 0, new String[][]{new String[]{"1035", "1036"}, new String[]{"1033", "1034"}}),
            // col 4
            new TowerConfig(SP * 3, SP * 9, SP * 0, new String[][]{new String[]{"980", "1006"}}),
            // col 5
            new TowerConfig(SP * 4, SP * 8, SP * 0, new String[][]{new String[]{"1009", "1008"}, new String[]{"1004", "1018"}}),
            // col 6
            new TowerConfig(SP * 5, SP * 9, SP * 0, new String[][]{new String[]{"1047", "0"}, new String[]{"960", "974"}}),
            // col 7
            new TowerConfig(SP * 6, SP * 8, SP * 0, new String[][]{new String[]{"641", "978"}, new String[]{"759", "547"}, new String[]{"654", "0"}}),
        }),

        new ClusterConfig("CLUSTER_Z", SP*28, SP*13, 0, new TowerConfig[]{
            // col 1
            new TowerConfig(SP * 0, SP * 1, SP * 0, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}}),
            new TowerConfig(SP * 0, SP * 2, SP * -1, new String[][]{new String[]{"734", "738"}, new String[]{"553", "552"}, new String[]{"852", "?"}}),
            // col 2
            new TowerConfig(SP * 1, SP * 0, SP * 0, new String[][]{new String[]{"0", "0"}}),
            new TowerConfig(SP * 1, SP * 1, SP * -1, new String[][]{new String[]{"689", "688"}, new String[]{"671", "670"}, new String[]{"465", "464"}}),
            // col 3
            new TowerConfig(SP * 2, SP * 0, SP * 0, new String[][]{new String[]{"0", "0"}}),
            new TowerConfig(SP * 2, SP * 2, SP * 0, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}}),
            new TowerConfig(SP * 2, SP * 0, SP * -1, new String[][]{new String[]{"857", "856"}, new String[]{"867", "866"}, new String[]{"585", "584"}, new String[]{"713", "712"}}),
            // col 4
            new TowerConfig(SP * 3, SP * 1, SP * 0, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}}),
            new TowerConfig(SP * 3, SP * 1, SP * -1, new String[][]{new String[]{"873", "872"}, new String[]{"847", "846"}})
        }),

        new ClusterConfig("CLUSTER_Y", SP*32, SP*13, 0, new TowerConfig[]{
            // col 1
            new TowerConfig(SP * 0, SP * 1, SP * 0, new String[][]{new String[]{"833", "832"}, new String[]{"587", "586"}, new String[]{"761", "760"}}),
            // col 2
            new TowerConfig(SP * 1, SP * 1, SP * 0, new String[][]{new String[]{"559", "558"}, new String[]{"711", "710"}, new String[]{"747", "746"}}),
            // col 3
            new TowerConfig(SP * 2, SP * 0, SP * 0, new String[][]{new String[]{"851", "850"}, new String[]{"737", "736"}, new String[]{"859", "858"}}),
            // col 4
            new TowerConfig(SP * 3, SP * 0, SP * 0, new String[][]{new String[]{"723", "722"}, new String[]{"561", "560"}, new String[]{"843", "842"}, new String[]{"591", "590"}})
        }),

        new ClusterConfig("CLUSTER_X", SP*36, SP*13, 0, new TowerConfig[]{
            // col 1
            new TowerConfig(SP * 0, SP * 1, SP * 0, new String[][]{new String[]{"", "0"}, new String[]{"0", "0"}}),
            new TowerConfig(SP * 0, SP * 1, SP * -1, new String[][]{new String[]{"0", "0"}, new String[]{"861", "860"}, new String[]{"599", "598"}}),
            // col 2
            new TowerConfig(SP * 1, SP * 1, SP * 0, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}}),
            new TowerConfig(SP * 1, SP * 1, SP * -1, new String[][]{new String[]{"469", "468"}, new String[]{"835", "834"}, new String[]{"837", "836"}, new String[]{"741", "740"}}),
            // col 3
            new TowerConfig(SP * 2, SP * 1, SP * 0, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}}),
            new TowerConfig(SP * 2, SP * 1, SP * -1, new String[][]{new String[]{"869", "868"}, new String[]{"855", "854"}, new String[]{"579", "578"}, new String[]{"791", "790"}}),
            // col 4
            new TowerConfig(SP * 3, SP * 0, SP * 0, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}}),
        }),

        new ClusterConfig("CLUSTER_W", SP*40, SP*13, 0, new TowerConfig[]{
            // col 1
            new TowerConfig(SP * 0, SP * 0, SP * 0, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}}),
            // col 2
            new TowerConfig(SP * 1, SP * 2, SP * 0, new String[][]{new String[]{"0", "0"}}),
            new TowerConfig(SP * 1, SP * 4, SP * 0, new String[][]{new String[]{"0", "0"}}),
            new TowerConfig(SP * 1, SP * 1, SP * -1, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}}),
            // col 3
            new TowerConfig(SP * 2, SP * 1, SP * -1, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}}),
            new TowerConfig(SP * 2, SP * 2, SP * -2, new String[][]{new String[]{"0", "0"}}),
            // col 4
            new TowerConfig(SP * 3, SP * 1, SP * -1, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}}),
            new TowerConfig(SP * 3, SP * 1, SP * -2, new String[][]{new String[]{"0", "0"}}),
        }),

        // done (but add last two)
        new ClusterConfig("CLUSTER_V", SP*44, SP*13, 0, new TowerConfig[]{
            // col 1
            new TowerConfig(SP * 0, SP * 0, SP * 0, new String[][]{new String[]{"?", "?"}}),
            new TowerConfig(SP * 0, SP * 0, SP * -1, new String[][]{new String[]{"733", "732"}, new String[]{"875", "874"}}),
            new TowerConfig(SP * 0, SP * 1, SP * -2, new String[][]{new String[]{"745", "744"}}),
            new TowerConfig(SP * 0, SP * 1, SP * -3, new String[][]{new String[]{"757", "756"}}),
            new TowerConfig(SP * 0, SP * 0, SP * -4, new String[][]{new String[]{"839", "838"}, new String[]{"697", "696"}}),
            // col 2
            new TowerConfig(SP * 1, SP * 0, SP * 0, new String[][]{new String[]{"903", "909"}}),
            // col 3
            new TowerConfig(SP * 2, SP * 0, SP * 0, new String[][]{new String[]{"892", "899"}}),
            // col 4
            new TowerConfig(SP * 3, SP * 0, SP * 0, new String[][]{new String[]{"713", "712"}, new String[]{"613", "612"}}),
        }),

        // done
        new ClusterConfig("CLUSTER_U", SP*44, SP*8, 0, new TowerConfig[]{
            // col 1
            new TowerConfig(SP * 0, SP * 1, SP * 0, new String[][]{new String[]{"623", "622"}, new String[]{"667", "666"}, new String[]{"645", "644"}, new String[]{"783", "782"}}),
            // col 2
            new TowerConfig(SP * 1, SP * 1, SP * 0, new String[][]{new String[]{"962", "462"}, new String[]{"475", "474"}, new String[]{"589", "588"}, new String[]{"725", "724"}}),
            // col 3
            new TowerConfig(SP * 2, SP * 1, SP * 0, new String[][]{new String[]{"673", "672"}, new String[]{"773", "772"}, new String[]{"665", "664"}, new String[]{"811", "810"}}),
            // col 4
            new TowerConfig(SP * 3, SP * 0, SP * 0, new String[][]{new String[]{"876", "520"}, new String[]{"681", "680"}, new String[]{"595", "804"}, new String[]{"675", "674"}, new String[]{"655", "463"}}),
            // col 5
            new TowerConfig(SP * 4, SP * 3, SP * 0, new String[][]{new String[]{"813", "812"}, new String[]{"649", "648"}}),
        }),

        // done
        new ClusterConfig("CLUSTER_T", SP*44, SP*1, 0, new TowerConfig[]{
            // col 1
            new TowerConfig(SP * 0, SP * 3, SP * 0, new String[][]{new String[]{"975", "958"}, new String[]{"993", "973"}, new String[]{"953", "955"}, new String[]{"703", "963"}}),
            // col 2
            new TowerConfig(SP * 1, SP * 3, SP * 0, new String[][]{new String[]{"940", "948"}, new String[]{"951", "950"}}),
            new TowerConfig(SP * 1, SP * 6, SP * 0, new String[][]{new String[]{"954", "959"}}),
            // col 3
            new TowerConfig(SP * 2, SP * 1, SP * 0, new String[][]{new String[]{"952", "943"}, new String[]{"949", "946"}, new String[]{"944", "947"}, new String[]{"929", "995"}}),
            // col 4
            new TowerConfig(SP * 3, SP * 2, SP * 0, new String[][]{new String[]{"905", "908"}, new String[]{"941", "937"}, new String[]{"942", "930"}, new String[]{"807", "830"}}),
        }),

//        new ClusterConfig("CLUSTER_S", 3500, 0, 0, new TowerConfig[]{
//            // col 1
//            new TowerConfig(SP * 0, SP * 1, SP * 0, new String[][]{new String[]{"0", "0"}}),
//            // col 2
//            new TowerConfig(SP * 1, SP * 0, SP * 0, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}}),
//            new TowerConfig(SP * 1, SP * 4, SP * 0, new String[][]{new String[]{"0", "0"}}),
//            // col 3
//            new TowerConfig(SP * 2, SP * 0, SP * 0, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}, new String[]{"0", "0"}}),
//            // col 4
//            new TowerConfig(SP * 3, SP * 0, SP * 0, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}}),
//            new TowerConfig(SP * 3, SP * 3, SP * 0, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}}),
//        }),

        new ClusterConfig("CLUSTER_R", SP*48, SP*9, 0, new TowerConfig[]{
            // col 1
            new TowerConfig(SP * 0, SP * 0, SP * 0, new String[][]{new String[]{"0", "0"}}),
            // col 2
            new TowerConfig(SP * 1, SP * 0, SP * 0, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}}),
            // col 3
            new TowerConfig(SP * 2, SP * 0, SP * 0, new String[][]{new String[]{"446", "596"}, new String[]{"865", "864"}}),
        }),

        // done
        new ClusterConfig("CLUSTER_Q", SP*51, SP*0, 0, new TowerConfig[]{
            // col 1
            new TowerConfig(SP * 0, SP * 9, SP * 0, new String[][]{new String[]{"967", "964"}, new String[]{"968", "976"}}),
            // col 2
            new TowerConfig(SP * 1, SP * 9, SP * 0, new String[][]{new String[]{"778", "970"}, new String[]{"971", "994"}}),
            // col 3
            new TowerConfig(SP * 2, SP * 9, SP * 0, new String[][]{new String[]{"986", "972"}}),
            // col 4
            new TowerConfig(SP * 3, SP * 0, SP * 0, new String[][]{new String[]{"911", "1063"}, new String[]{"700", "792"}, new String[]{"438", "488"}, new String[]{"531", "530"}, new String[]{"498", "831"}, new String[]{"794", "795"}, new String[]{"546", "913"}, new String[]{"518", "532"}, new String[]{"849", "848"}, new String[]{"914", "847"}, new String[]{"818", "464"}}),
            // col     5
            new TowerConfig(SP * 4, SP * 0, SP * 0, new String[][]{new String[]{"1072", "1073"}}),//new String[]{"573", "572"}});
            new TowerConfig(SP * 4, SP * 2, SP * 0, new String[][]{new String[]{"637", "636"}, new String[]{"779", "796"}}),
            new TowerConfig(SP * 4, SP * 7, SP * 0, new String[][]{new String[]{"827", "826"}}),
            new TowerConfig(SP * 4, SP * 9, SP * 0, new String[][]{new String[]{"992", "819"}}),
        }),
    };

    static class ClusterConfig {
        final String id;
        final float x;
        final float y;
        final float z;
        final TowerConfig[] configs;

        ClusterConfig(String id, float x, float y, float z, TowerConfig[] configs) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.z = z;
            this.configs = configs;
        }
    }

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

        List<CubesModel.Tower> towers = new ArrayList<>();
        List<CubesModel.Cube> allCubes = new ArrayList<>();

//        try {
//            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
//                Utils.createInput("data/SummerStageCoordinates.txt")));
//
//            List<CubesModel.Cube> cubes = new ArrayList<>();
//
//            String line;
//            while ((line = bufferedReader.readLine()) != null) {
//                line = line.replaceAll(" ","").replaceAll("\"","");
//                String[] vals = line.split(",");
//
//                float x = metersToInches(Float.parseFloat(vals[0]));
//                float y = metersToInches(Float.parseFloat(vals[2]));
//                float z = metersToInches(Float.parseFloat(vals[1]));
//
//                CubesModel.DoubleControllerCube cube = new CubesModel.DoubleControllerCube("0", "0", x, y, z, 0, 0, 0, globalTransform);
//                cubes.add(cube);
//                allCubes.add(cube);
//            }
//            towers.add(new CubesModel.Tower("", cubes));
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }


        for (ClusterConfig cluster : clusters) {
            List<CubesModel.Cube> cubes = new ArrayList<>();

            globalTransform.push();
            globalTransform.translate(cluster.x, cluster.y, cluster.z);

            for (TowerConfig config : cluster.configs) {
                float x = config.x;
                float z = config.z;

                for (int i = 0; i < config.ids.length; i++) {
                    String idA = config.ids[i][0];
                    String idB = config.ids[i][1];
                    float y = config.yValues[i];
                    CubesModel.DoubleControllerCube cube = new CubesModel.DoubleControllerCube(idA, idB, x, y, z, 0, 0, 0, globalTransform);
                    cubes.add(cube);
                    allCubes.add(cube);
                }
            }
            globalTransform.pop();
            towers.add(new CubesModel.Tower(cluster.id, cubes));
        }

        CubesModel.Cube[] allCubesArr = new CubesModel.Cube[allCubes.size()];
        for (int i = 0; i < allCubesArr.length; i++) {
            allCubesArr[i] = allCubes.get(i);
        }

        return new CubesModel(towers, allCubesArr);
    }

    public void setupUi(SLStudioLX lx, SLStudioLX.UI ui) {
        UI2dScrollContext utility = ui.rightPane.utility;
        new UIOutputs(lx, ui, this, 0, 0, utility.getContentWidth()).addToContainer(utility);
        new UIMappingPanel(lx, ui, 0, 0, utility.getContentWidth()).addToContainer(utility);
    }
}
