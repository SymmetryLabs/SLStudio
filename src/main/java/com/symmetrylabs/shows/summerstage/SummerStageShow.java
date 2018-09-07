package com.symmetrylabs.shows.summerstage;

import java.util.*;


import com.symmetrylabs.shows.cubes.CubesModel;
import com.symmetrylabs.shows.cubes.CubesShow;
import com.symmetrylabs.shows.cubes.UICubesMappingPanel;
import com.symmetrylabs.shows.cubes.UICubesOutputs;
import com.symmetrylabs.slstudio.model.SLModel;

import heronarts.lx.transform.LXTransform;
import heronarts.p3lx.ui.UI2dScrollContext;

import com.symmetrylabs.slstudio.SLStudioLX;

/**
 * This file implements the mapping functions needed to lay out the cubes.
 */
public class SummerStageShow extends CubesShow {
    public static final String SHOW_NAME = "ss";

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

    static final ClusterConfig[] clusters = new ClusterConfig[] {


        /**--------------------------------------------------------------------------------------------------------------------------
         * LEFT FACE
        */
        new ClusterConfig("LEFT_FACE",SP*0, SP*1, SP*2, new TowerConfig[]{
            // col 1
            new TowerConfig(SP * 0, SP * 6, SP * 0, new String[][]{
                new String[]{"1032", "1022"}
            }),
            new TowerConfig(SP * 0, SP * 9, SP * 0, new String[][]{
                new String[]{"1026", "1025"}
            }),
            // col 1 (-1)
            new TowerConfig(SP * 0, SP * 6, SP * -1, new String[][]{
                new String[]{"1107", "1106"}, new String[]{"1104", "1103"}
            }),
            // col 2
            new TowerConfig(SP * 1, SP * 0, SP * 0, new String[][]{
                new String[]{"529", "528"}, new String[]{"925", "934"}, new String[]{"1030", "1040"},
                new String[]{"592", "1000"}, new String[]{"985", "979"}, new String[]{"988", "977"}, // 1001 -> 592 8/3/18
                new String[]{"991", "1020"}, new String[]{"1037", "1038"}, new String[]{"1012", "1039"},
                new String[]{"1023", "1024"}, new String[]{"647", "646"}
            }),
            // col 3
            new TowerConfig(SP * 2, SP * 10, SP * 0, new String[][]{
                new String[]{"1035", "1036"}, new String[]{"1033", "1034"}
            }),
            // col 4
            new TowerConfig(SP * 3, SP * 10, SP * 0, new String[][]{
                new String[]{"980", "1006"}
            }),
            // col 5
            new TowerConfig(SP * 4, SP * 9, SP * 0, new String[][]{
                new String[]{"1009", "1008"}, new String[]{"1004", "1018"}
            }),
            // col 6
            new TowerConfig(SP * 5, SP * 10, SP * 0, new String[][]{
                new String[]{"1047", "883"}, new String[]{"960", "974"}
            }),
            // col 7
            new TowerConfig(SP * 6, SP * 9, SP * 0, new String[][]{
                new String[]{"641", "978"}, new String[]{"759", "547"}, new String[]{"654", "1083"}
            }),
            // col 7 (-1)
            new TowerConfig(SP * 6, SP * 11, SP * -1, new String[][]{
                new String[]{"1067", "1066"}, new String[]{"1041", "1042"} // NEW!!!
            }),
            // col 8
            new TowerConfig(SP * 7, SP * 10, SP * 0, new String[][]{
                new String[]{"997", "996"}, new String[]{"957", "956"}
            }),
            // col 9
            new TowerConfig(SP * 8, SP * 3, SP * 0, new String[][]{
                new String[]{"366", "702"}, new String[]{"896", "898"}, new String[]{"763", "742"},
                new String[]{"601", "600"}, new String[]{"975", "489"}, new String[]{"549", "574"},
                new String[]{"551", "550"}, new String[]{"945", "901"}
            }),
            // col 10
            new TowerConfig(SP * 9, SP * 3, SP * 0, new String[][]{
                new String[]{"931", "938"}, new String[]{"928", "918"}, new String[]{"906", "912"}
            }),
            new TowerConfig(SP * 9, SP * 4, SP * -1, new String[][]{
                new String[]{"924", "923"}
            }),
            new TowerConfig(SP * 9, SP * 7, SP * 0, new String[][]{
                new String[]{"481", "480"}, new String[]{"441", "440"}, new String[]{"495", "494"},
                new String[]{"771", "770"}, new String[]{"691", "690"}
            }),
            // col 11
            new TowerConfig(SP * 10, SP * 3, SP * 0, new String[][]{
                new String[]{"916", "917"}
            }),
            new TowerConfig(SP * 10, SP * 5, SP * 0, new String[][]{
                new String[]{"922", "1059"}, new String[]{"1013", "1015"},
                new String[]{"887", "882"}, new String[]{"890", "879"}, new String[]{"451", "450"}, new String[]{"1011", "802"},
                new String[]{"449", "448"}
            }),
            // col 11 (-1)
            new TowerConfig(SP * 10, SP * 7, SP * -1, new String[][]{
                new String[]{"1029", "1028"}, new String[]{"936", "921"},
            }),
            // col 12
            new TowerConfig(SP * 11, SP * 3, SP * 0, new String[][]{
                new String[]{"926", "998"}, new String[]{"939", "933"}, new String[]{"935", "999"}
            }),
            new TowerConfig(SP * 11, SP * 7, SP * 0, new String[][]{
                new String[]{"927", "915"}, new String[]{"919", "910"}
            }),
            new TowerConfig(SP * 11, SP * 10, SP * 0, new String[][]{
                new String[]{"981", "982"}, new String[]{"1003", "989"}, new String[]{"685", "1071"}, new String[]{"817", "682"}
            }),
            // col 12 (-1)
            new TowerConfig(SP * 11, SP * 13, SP * -1, new String[][]{
                new String[]{"719", "718"}
            }),


            new TowerConfig(SP * 8, SP * 12, SP * -1, new String[][]{
                new String[]{"1111", "1112"}
            }),
            new TowerConfig(SP * 9, SP * 11, SP * -1, new String[][]{
                new String[]{"1109", "1108"}, new String[] {"1065", "1074"}
            }),
            new TowerConfig(SP * 10, SP * 11, SP * -1, new String[][]{
                new String[]{"1091", "1090"} // 1096 was flickering
            }),
        }),

        /**--------------------------------------------------------------------------------------------------------------------------
         * RIGHT FACE
        */
        new ClusterConfig("RIGHT_FACE",SP*44, SP*1, 0, new TowerConfig[]{
            // col 1
            new TowerConfig(SP * 0, SP * 5, SP * 0, new String[][]{
                new String[]{"993", "973"}, new String[]{"717", "716"}, new String[]{"703", "963"},
                new String[]{"623", "622"}, new String[]{"667", "666"}, new String[]{"645", "644"}, new String[]{"783", "782"}
            }),
            // col 2
            new TowerConfig(SP * 1, SP * 4, SP * 0, new String[][]{
                new String[]{"940", "948"}, new String[]{"1075", "593"}//new String[]{"951", "950"}
            }),
            new TowerConfig(SP * 1, SP * 7, SP * 0, new String[][]{
                new String[]{"954", "959"}, new String[]{"962", "462"}, new String[]{"475", "474"},
                new String[]{"589", "588"}, new String[]{"725", "724"}, new String[]{"1017", "1016"}
            }),
            // col 3
            new TowerConfig(SP * 2, SP * 3, SP * 0, new String[][]{
                new String[]{"949", "946"}, new String[]{"944", "947"}, new String[]{"929", "995"}
            }),
            new TowerConfig(SP * 2, SP * 8, SP * 0, new String[][]{
                new String[]{"673", "672"}, new String[]{"773", "772"}, new String[]{"665", "664"}, new String[]{"811", "810"},
                new String[]{"903", "909"}
            }),
            // col 4
            new TowerConfig(SP * 3, SP * 3, SP * 0, new String[][]{
                new String[]{"905", "955"}, new String[]{"941", "937"}, new String[]{"942", "930"}, new String[]{"1052", "830"}
            }),
            new TowerConfig(SP * 3, SP * 8, SP * 0, new String[][]{
                new String[]{"681", "680"}, new String[]{"595", "804"},
                new String[]{"675", "674"}, new String[]{"655", "463"}, new String[]{"892", "899"}
            }),
            // col 4 (-1)
            new TowerConfig(SP * 3, SP * 4, SP * -1, new String[][]{
                new String[]{"604", "1087"}
            }),
            // col 5
            new TowerConfig(SP * 4, SP * 9, SP * 0, new String[][]{
                new String[]{"953", "1110"}, new String[]{"813", "812"}, new String[]{"649", "648"}, // i think 727 is wired wrong
                new String[]{"713", "712"}, new String[]{"1027", "612"}
            }),
            // col 6
            new TowerConfig(SP * 5, SP * 9, SP * 0, new String[][]{
                new String[]{"543", "542"}, new String[]{"1043", "758"}
            }),
            // col 7
            new TowerConfig(SP * 6, SP * 9, SP * 0, new String[][]{
                new String[]{"446", "596"}, new String[]{"865", "864"}
            }),
        }),


        new ClusterConfig("CLUSTER_A",SP*24, SP*14, 0, new TowerConfig[] {
            // col 1
            new TowerConfig(SP * 0, SP * 1, SP * 0, new String[][]{new String[]{"485", "484"}, new String[]{"699", "698"}}),
            new TowerConfig(SP * 0, SP * 0, SP * -1, new String[][]{new String[]{"487", "486"}, new String[]{"515", "514"}, new String[]{"491", "490"}, new String[]{"477", "476"}}),
            // col 2
            new TowerConfig(SP * 1, SP * 1, SP * 0, new String[][]{new String[]{"503", "502"}, new String[]{"511", "510"}}),
            // col 3
            new TowerConfig(SP * 2, SP * 2, SP * 0, new String[][]{new String[]{"878", "768"}}),
            // col 4
            new TowerConfig(SP * 3, SP * 0, SP * 0, new String[][]{new String[]{"565", "564"}, new String[]{"473", "472"}, new String[]{"455", "454"}}) // 565n was flickering
        }),

        new ClusterConfig("CLUSTER_B", SP*20, SP*13, 0, new TowerConfig[] {
            // col 1
            new TowerConfig(SP * 0, SP * 2, SP * 0, new String[][]{new String[]{"541", "540"}, new String[]{"581", "580"}, new String[]{"453", "452"}}),
            // col 2
            new TowerConfig(SP * 1, SP * 2, SP * 0, new String[][]{new String[]{"577", "576"}, new String[]{"471", "470"}, new String[]{"497", "496"}}),
            // col 3
            new TowerConfig(SP * 2, SP * 1, SP * 0, new String[][]{new String[]{"751", "750"}, new String[]{"527", "526"}, new String[]{"467", "466"}, new String[]{"507", "506"}}),
            // col 4
            new TowerConfig(SP * 3, SP * 0, SP * 0, new String[][]{new String[]{"729", "728"}, new String[]{"749", "748"}, new String[]{"445", "444"}, new String[]{"483", "482"}})
        }),

        new ClusterConfig("CLUSTER_C", SP*16, SP*14, 0, new TowerConfig[] {
            // col 1
            new TowerConfig(SP * 0, SP * 1, SP * 0, new String[][]{new String[]{"629", "628"}, new String[]{"627", "626"}}),
            new TowerConfig(SP * 0, SP * 0, SP * -1, new String[][]{new String[]{"661", "660"}, new String[]{"659", "658"}}),
            // col 2
            new TowerConfig(SP * 1, SP * 1, SP * 0, new String[][]{new String[]{"677", "676"}, new String[]{"815", "814"}}),
            new TowerConfig(SP * 1, SP * 0, SP * -1, new String[][]{new String[]{"663", "662"}, new String[]{"617", "616"}, new String[]{"?13", "?14"}}),
            // col 3
            new TowerConfig(SP * 2, SP * 1, SP * 0, new String[][]{new String[]{"639", "638"}}),
            new TowerConfig(SP * 2, SP * 0, SP * -1, new String[][]{new String[]{"799", "798"}, new String[]{"619", "618"}, new String[]{"479", "478"}}),
            // col 4
            new TowerConfig(SP * 3, SP * 0, SP * 0, new String[][]{new String[]{"657", "656"}}),
            new TowerConfig(SP * 3, SP * 0, SP * -1, new String[][]{new String[]{"687", "686"}}),
            new TowerConfig(SP * 3, SP * 2, SP * -1, new String[][]{new String[]{"715", "714"}}),
        }),

        new ClusterConfig("CLUSTER_D", SP*13, SP*14, 0, new TowerConfig[]{
            // col 1
            new TowerConfig(SP * 0, SP * 1, SP * -1, new String[][]{new String[]{"509", "508"}, new String[]{"567", "566"}, new String[]{"583", "582"}}),
            new TowerConfig(SP * 0, SP * 1, SP * -2, new String[][]{new String[]{"679", "678"}, new String[]{"1046", "1045"}, new String[]{"787", "786"}}),  //643. 642 ->> 1046 , 1045 - AS OF 8/3/18
            // col 2
            new TowerConfig(SP * 1, SP * 0, SP * 0, new String[][]{new String[]{"765", "764"}, new String[]{"609", "608"}, new String[]{"781", "780"}, new String[]{"669", "668"}}),
            new TowerConfig(SP * 1, SP * 1, SP * -1, new String[][]{new String[]{"603", "602"}, new String[]{"821", "820"}, new String[]{"809", "808"}, new String[]{"721", "720"}}),
            // col 3
            new TowerConfig(SP * 2, SP * 0, SP * 0, new String[][]{new String[]{"523", "522"}, new String[]{"525", "524"}, new String[]{"457", "456"}, new String[]{"607", "606"}}),
        }),

        new ClusterConfig("CLUSTER_E", SP*10, SP*11, SP*2, new TowerConfig[]{
            // col 1
//            new TowerConfig(SP * 0, SP * 2, SP * 0, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}}),
//            new TowerConfig(SP * 0, SP * 3, SP * -1, new String[][]{new String[]{"0", "0"}}),
//            new TowerConfig(SP * 0, SP * 3, SP * -2, new String[][]{new String[]{"0", "0"}}),
//            new TowerConfig(SP * 0, SP * 4, SP * -3, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}}),
            // col 2
            //new TowerConfig(SP * 1, SP * 0, SP * 0, new String[][]{new String[]{"0", "0"}, new String[]{"0", "0"}}),
            new TowerConfig(SP * 1, SP * 3, SP * -3, new String[][]{new String[]{"1082", "784"}, new String[]{"563", "562"}, new String[]{"777", "776"}}),
            // col 3
            new TowerConfig(SP * 2, SP * 4, SP * -4, new String[][]{new String[]{"707", "706"}}),
        }),

        new ClusterConfig("CLUSTER_Z", SP*28, SP*13, 0, new TowerConfig[]{
            // col 1
            new TowerConfig(SP * 0, SP * 1, SP * 0, new String[][]{new String[]{"873", "872"}, new String[]{"767", "766"}, new String[]{"775", "774"}}),
            new TowerConfig(SP * 0, SP * 2, SP * -1, new String[][]{new String[]{"739", "738"}, new String[]{"553", "552"}, new String[]{"852", "853"}}),
            // col 2
            new TowerConfig(SP * 1, SP * 1, SP * -1, new String[][]{new String[]{"1092", "694"}, new String[]{"611", "610"}, new String[]{"653", "652"}}),
            // col 3
            new TowerConfig(SP * 2, SP * 0, SP * 0, new String[][]{new String[]{"857", "?12"}}),
            new TowerConfig(SP * 2, SP * 2, SP * 0, new String[][]{new String[]{"651", "650"}, new String[]{"801", "800"}}),
            new TowerConfig(SP * 2, SP * 0, SP * -1, new String[][]{new String[]{"621", "952"}, new String[]{"789", "788"}, new String[]{"635", "634"}}),
            // col 4
            new TowerConfig(SP * 3, SP * 1, SP * 0, new String[][]{new String[]{"671", "670"}, new String[]{"689", "688"}}),
            new TowerConfig(SP * 3, SP * 1, SP * -1, new String[][]{new String[]{"867", "866"}, new String[]{"585", "584"}}) // 867 was flickering
        }),

        new ClusterConfig("CLUSTER_Y", SP*32, SP*13, 0, new TowerConfig[]{
            // col 1
            new TowerConfig(SP * 0, SP * 1, SP * 0, new String[][]{new String[]{"833", "832"}, new String[]{"587", "586"}, new String[]{"761", "760"}}),
            // col 2
            new TowerConfig(SP * 1, SP * 1, SP * 0, new String[][]{new String[]{"559", "558"}, new String[]{"711", "710"}, new String[]{"747", "746"}}),
            // col 3
            new TowerConfig(SP * 2, SP * 1, SP * 0, new String[][]{new String[]{"737", "736"}, new String[]{"859", "858"}}), //new String[]{"851", "850"}
            new TowerConfig(SP * 2, SP * 2 - (2 * CUBE_SPACING), SP * 0, 90, 180, 90, new String[][]{new String[]{"722", "1060"}}), //new String[]{"851", "850"}
            // col 4
            new TowerConfig(SP * 3, SP * 1, SP * 0, new String[][]{new String[]{"561", "560"}, new String[]{"843", "842"}, new String[]{"465", "458"}}), //new String[]{"723", "722"}
            new TowerConfig(SP * 3, SP * 2 - (2 * CUBE_SPACING), SP * 0, 180, 180, 0, new String[][]{new String[]{"851", "850"}})
        }),

        new ClusterConfig("CLUSTER_X", SP*36, SP*13, 0, new TowerConfig[]{
            // col 1
            new TowerConfig(SP * 0, SP * 1, SP * 0, new String[][]{new String[]{"493", "984"}, new String[]{"704", "705"}}),
            new TowerConfig(SP * 0, SP * 1, SP * -1, new String[][]{new String[]{"545", "439"}, new String[]{"871", "870"}, new String[]{"555", "554"}}),
            // col 2
            new TowerConfig(SP * 1, SP * 1, SP * 0, new String[][]{new String[]{"731", "730"}, new String[]{"461", "460"}}),
            new TowerConfig(SP * 1, SP * 1, SP * -1, new String[][]{new String[]{"469", "468"}, new String[]{"835", "834"}, new String[]{"837", "836"}, new String[]{"741", "740"}}),
            // col 3
            new TowerConfig(SP * 2, SP * 1, SP * 0, new String[][]{new String[]{"735", "734"}, new String[]{"755", "754"}, new String[]{"753", "752"}}),
            new TowerConfig(SP * 2, SP * 1, SP * -1, new String[][]{new String[]{"869", "868"}, new String[]{"855", "854"}, new String[]{"579", "578"}, new String[]{"791", "790"}}),
            // col 4
            new TowerConfig(SP * 3, SP * 0, SP * 0, new String[][]{new String[]{"513", "512"}, new String[]{"861", "860"}, new String[]{"599", "598"}, new String[]{"625", "624"}}), //5410ecf67b05
        }),

        new ClusterConfig("CLUSTER_W", SP*40, SP*13, 0, new TowerConfig[]{
            // col 1
            new TowerConfig(SP * 0, SP * 0, SP * 0, new String[][]{new String[]{"841", "840"}, new String[]{"863", "862"}, new String[]{"805", "556"}, new String[]{"884", "880"}, new String[]{"823", "701"}}),
            // col 2
            new TowerConfig(SP * 1, SP * 2, SP * 0, new String[][]{new String[]{"829", "828"}}),
            new TowerConfig(SP * 1, SP * 4, SP * 0, new String[][]{new String[]{"569", "568"}}),
            new TowerConfig(SP * 1, SP * 2, SP * -1, new String[][]{new String[]{"708", "709"}}),
            // col 3
            new TowerConfig(SP * 2, SP * 1, SP * -1, new String[][]{new String[]{"893", "894"}, new String[]{"897", "885"}, new String[]{"443", "442"}}),
            new TowerConfig(SP * 2, SP * 2, SP * -2, new String[][]{new String[]{"825", "824"}}),
            // col 4
            new TowerConfig(SP * 3, SP * 1, SP * -1, new String[][]{new String[]{"844", "845"}, new String[]{"499", "816"}}),
        }),

        // done (but add last two)
        new ClusterConfig("CLUSTER_V", SP*44, SP*13, 1, new TowerConfig[]{
            // col 1
            new TowerConfig(SP * 0, SP * 0, SP * 0, new String[][]{new String[]{"733", "732"}, new String[]{"875", "874"}}),
            new TowerConfig(SP * 0, SP * 1, SP * -1, new String[][]{new String[]{"745", "744"}}),
            new TowerConfig(SP * 0, SP * 1, SP * -2, new String[][]{new String[]{"757", "756"}}),
            new TowerConfig(SP * 0, SP * 0, SP * -3, new String[][]{new String[]{"839", "838"}, new String[]{"697", "696"}}),
            // col 2
            //new TowerConfig(SP * 1, SP * 0, SP * 0, new String[][]{new String[]{"1017", "1016"}}),
            // col 3
            //new TowerConfig(SP * 2, SP * 0, SP * 0, new String[][]{new String[]{"892", "899"}}),
            // col 4
            //new TowerConfig(SP * 3, SP * 0, SP * 0, new String[][]{new String[]{"713", "712"}, new String[]{"613", "5410ecf5f70e"}}),
        }),

        // done
        new ClusterConfig("CLUSTER_U", SP*44, SP*8, 0, new TowerConfig[]{
            // col 1
            //new TowerConfig(SP * 0, SP * 1, SP * 0, new String[][]{new String[]{"623", "622"}, new String[]{"667", "666"}, new String[]{"645", "644"}, new String[]{"783", "782"}}),
            // col 2
            //new TowerConfig(SP * 1, SP * 1, SP * 0, new String[][]{new String[]{"962", "462"}, new String[]{"475", "474"}, new String[]{"589", "588"}, new String[]{"725", "724"}}),
            // col 3
            //new TowerConfig(SP * 2, SP * 1, SP * 0, new String[][]{new String[]{"673", "672"}, new String[]{"773", "772"}, new String[]{"665", "664"}, new String[]{"811", "810"}}),
            // col 4
            //new TowerConfig(SP * 3, SP * 0, SP * 0, new String[][]{new String[]{"876", "520"}, new String[]{"681", "680"}, new String[]{"595", "804"}, new String[]{"675", "674"}, new String[]{"655", "463"}}),
            // col 5
            //new TowerConfig(SP * 4, SP * 3, SP * 0, new String[][]{new String[]{"813", "812"}, new String[]{"649", "648"}}),
        }),

        // done
        new ClusterConfig("CLUSTER_Q", SP*51, SP*1, 0, new TowerConfig[]{
            // col 1
            new TowerConfig(SP * 0, SP * 9, SP * 0, new String[][]{new String[]{"967", "964"}, new String[]{"968", "976"}}),
            // col 2
            new TowerConfig(SP * 1, SP * 9, SP * 0, new String[][]{new String[]{"778", "970"}, new String[]{"971", "994"}}),
            // col 3
            new TowerConfig(SP * 2, SP * 9, SP * 0, new String[][]{new String[]{"986", "972"}}),
            // col 4
            new TowerConfig(SP * 3, SP * 0, SP * 0, new String[][]{new String[]{"911", "1063"}, new String[]{"700", "792"}, new String[]{"437", "1094"}, new String[]{"531", "530"}, new String[]{"498", "831"}, new String[]{"794", "795"}, new String[]{"546", "913"}, new String[]{"518", "532"}, new String[]{"849", "848"}, new String[]{"914", "847"}, new String[]{"818", "464"}}),
            new TowerConfig(SP * 3, SP * 2, SP * -1, new String[][]{new String[]{"1077", "1076"}}),
            // col     5
            new TowerConfig(SP * 4, SP * 0, SP * 0, new String[][]{new String[]{"1072", "1073"}}),//new String[]{"573", "572"}});
            new TowerConfig(SP * 4, SP * 2, SP * 0, new String[][]{new String[]{"637", "1057"}, new String[]{"779", "796"}}),
            new TowerConfig(SP * 4, SP * 7, SP * 0, new String[][]{new String[]{"827", "826"}}),
            new TowerConfig(SP * 4, SP * 9, SP * 0, new String[][]{new String[]{"992", "819"}}),

            new TowerConfig(SP * 4, SP * 3, SP * -1, new String[][]{new String[]{"1080", "1061"}}),
            new TowerConfig(SP * 4, SP * 7, SP * -1, new String[][]{new String[]{"1078", "902"}}),

            // col 6
            new TowerConfig(SP * 4, SP * 6, SP * -1, new String[][]{new String[]{"1097", "1070"}}), // 1101?
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

                float rX = config.xRot;
                float rY = config.yRot;
                float rZ = config.zRot;

                for (int i = 0; i < config.ids.length; i++) {
                    String idA = config.ids[i][0];
                    String idB = config.ids[i][1];
                    float y = config.yValues[i];
                    CubesModel.DoubleControllerCube cube = new CubesModel.DoubleControllerCube(idA, idB, x, y, z, rX, rY, rZ, globalTransform);
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

        CubesModel model = new CubesModel(towers, allCubesArr);
        model.setTopologyTolerances(6, 6, 6);
        return model;
    }

    public void setupUi(SLStudioLX lx, SLStudioLX.UI ui) {
        UI2dScrollContext utility = ui.rightPane.utility;
        new UICubesOutputs(lx, ui, this, 0, 0, utility.getContentWidth()).addToContainer(utility);
        new UICubesMappingPanel(lx, ui, 0, 0, utility.getContentWidth()).addToContainer(utility);
    }
}
