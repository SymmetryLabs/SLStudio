  /**
 *     DOUBLE BLACK DIAMOND        DOUBLE BLACK DIAMOND
 *
 *         //\\   //\\                 //\\   //\\  
 *        ///\\\ ///\\\               ///\\\ ///\\\
 *        \\\/// \\\///               \\\/// \\\///
 *         \\//   \\//                 \\//   \\//
 *
 *        EXPERTS ONLY!!              EXPERTS ONLY!!
 *
 * This file implements the mapping functions needed to lay out the physical
 * cubes and the output ports on the panda board. It should only be modified
 * when physical changes or tuning is being done to the structure.
 */


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
static final float CUBE_SPACING = 2.5;

static final float TOWER_VERTICAL_SPACING = 2.5;
static final float TOWER_RISER = 14;
static final float SP = 24;
static final float JUMP = TOWER_HEIGHT+TOWER_VERTICAL_SPACING;

// static final BulbConfig[] BULB_CONFIG = {
//     // new BulbConfig("lifx-1", -50, 50, -30),
//     // new BulbConfig("lifx-2", 0, 50, 0),
//     // new BulbConfig("lifx-3", -65, 20, -100),
//     // new BulbConfig("lifx-4", 0, 0, 0),
//     // new BulbConfig("lifx-5", 0, 0, 0),
// };

static final float PIXEL_PITCH = 1.25; // double check measurement

static final float CONTOUR_WIDTH = 5; // needs measurement;
static final float DISTANCE_TO_FLOOR = 100;

static final TowerConfig[] TOWER_CONFIG = {};


/**
 * Vip Lounge
 *---------------------------------------------------------------------------------------------*/
static final float vip_lounge_offset_x = 0;
static final float vip_lounge_offset_y = 0;
static final float vip_lounge_offset_z = 0;
static final float vip_lounge_rotation_x = 0;
static final float vip_lounge_rotation_y = 0;
static final float vip_lounge_rotation_z = 0;

static final StripConfig[] VIP_LOUGNE_STRIP_CONFIG = {
  //strip id, {x, y, z}, {xRot, yRot, zRot}, num leds, length

  // // horizontals - bottom
  // new StripConfig("vip-lounge-strip1", new float[] { 0, 0, 0}, new float[] {0, 0, 0}, 44),
  // new StripConfig("vip-lounge-strip2", new float[] {64, 0, 0}, new float[] {0, 0, 0}, 141),

  // // horizontals - middle bottom
  // new StripConfig("vip-lounge-strip3", new float[] {  0, 62, 0}, new float[] {0, 0, 0}, 92),
  // new StripConfig("vip-lounge-strip4", new float[] {123, 62, 0}, new float[] {0, 0, 0}, 98),

  // // horizontals - middle top
  // new StripConfig("vip-lounge-strip5", new float[] {0,   68, 0}, new float[] {0, 0, 0}, 121),
  // new StripConfig("vip-lounge-strip6", new float[] {163, 68, 0}, new float[] {0, 0, 0}, 66),

  // // // horizontals - top
  // new StripConfig("vip-lounge-strip7", new float[] {0, 130, 0}, new float[] {0, 0, 0}, 71),
  // new StripConfig("vip-lounge-strip8", new float[] {97, 130, 0}, new float[] {0, 0, 0}, 119+7), 

  // // // verticals - left
  // new StripConfig("vip-lounge-strip9",  new float[] {0, 0, 0}, new float[] {0, 0, 90}, 50), 
  // new StripConfig("vip-lounge-strip10", new float[] {0, 68, 0}, new float[] {0, 0, 90}, 49), 

  // // // verticals - right 
  // new StripConfig("vip-lounge-strip11", new float[] {238, 0, 0}, new float[] {0, 0, 90}, 39), 
  // new StripConfig("vip-lounge-strip12", new float[] {240, 49, 0}, new float[] {0, 0, 0}, 3),
  // new StripConfig("vip-lounge-strip13", new float[] {244, 48.5, 0}, new float[] {0, 0, 90}, 10), 
  // new StripConfig("vip-lounge-strip14", new float[] {244, 68  , 0}, new float[] {0, 0, 90}, 49), 

  // // // bottom angle (left, right)
  // new StripConfig("vip-lounge-strip15", new float[] {55, 1, 0}, new float[] {0, 0, 45}, 69), 
  // new StripConfig("vip-lounge-strip16", new float[] {63, 1, 0}, new float[] {0, 0, 45}, 67), 

  // // // top angle (left, right)
  // new StripConfig("vip-lounge-strip17", new float[] {152, 68, 0}, new float[] {0, 0, 137.7}, 72), 
  // new StripConfig("vip-lounge-strip18", new float[] {162, 67, 0}, new float[] {0, 0, 137.7}, 74), 

};

static final StripConfig[] VJ_BOOTH_STRIP_CONFIG = {

};

/**
 * Long Skinny
 *---------------------------------------------------------------------------------------------*/
static final float long_skinny_offset_x = 0;
static final float long_skinny_offset_y = 136;
static final float long_skinny_offset_z = 0;
static final float long_skinny_rotation_x = 0;
static final float long_skinny_rotation_y = 0;
static final float long_skinny_rotation_z = 0;

static final StripConfig[] LONG_SKINNY_RUN_STRIP_CONFIG = {

  // // horizontals - bottom 
  // new StripConfig("long-skinny-strip1", new float[] {  0, 0, 0}, new float[] {0, 0, 0}, 98),
  // new StripConfig("long-skinny-strip2", new float[] {132, 0, 0}, new float[] {0, 0, 0}, 121),
  // new StripConfig("long-skinny-strip3", new float[] {294, 0, 0}, new float[] {0, 0, 0}, 84),
  // new StripConfig("long-skinny-strip4", new float[] {407, 0, 0}, new float[] {0, 0, 0}, 79),

  // // horizontals - top
  // new StripConfig("long-skinny-strip5", new float[] { -1, 24, 0}, new float[] {0, 0, 0}, 129),
  // new StripConfig("long-skinny-strip6", new float[] {170, 24, 0}, new float[] {0, 0, 0}, 68),
  // new StripConfig("long-skinny-strip7", new float[] {260, 24, 0}, new float[] {0, 0, 0}, 144),
  // new StripConfig("long-skinny-strip8", new float[] {448, 24, 0}, new float[] {0, 0, 0}, 76),

  // // verticals & angles
  // new StripConfig("long-skinny-strip9", new float[] {0, 1, 0}, new float[] {0, 0, 90}, 18), 

  // new StripConfig("long-skinny-strip10", new float[] {124, 1, 0}, new float[] {0, 0, 33}, 32), 
  // new StripConfig("long-skinny-strip11", new float[] {134, 1, 0}, new float[] {0, 0, 33}, 32), 

  // new StripConfig("long-skinny-strip12", new float[] {284, -1, 0}, new float[] {0, 0, 144.5}, 33), 
  // new StripConfig("long-skinny-strip13", new float[] {293, -1.5, 0}, new float[] {0, 0, 144}, 35), 

  // new StripConfig("long-skinny-strip14", new float[] {399, 2, 0}, new float[] {0, 0, 28}, 33), 
  // new StripConfig("long-skinny-strip15", new float[] {407, 2, 0}, new float[] {0, 0, 28}, 33), 

  // new StripConfig("long-skinny-strip16", new float[] {505, 2, 0}, new float[] {0, 0, 28}, 32)
};

/**
 * Columns (and ceiling above)
 *---------------------------------------------------------------------------------------------*/

static final float columns_offset_x = -32;
static final float columns_offset_y = 203;
static final float columns_offset_z = 0;
static final float columns_rotation_x = 0;
static final float columns_rotation_y = 0;
static final float columns_rotation_z = 0;

static final StripConfig[] COLUMNS_STRIP_CONFIG = {
  // // ceiling
  // new StripConfig("columns-strip1", new float[] {40+28, 0, 12}, new float[] {0, 0, 0}, 48), // 448 total
  // new StripConfig("columns-strip2", new float[] {40+28+60, 0, 12}, new float[] {0, 0, 0}, 147),
  // new StripConfig("columns-strip3", new float[] {40+28+60+183, 0, 12}, new float[] {0, 0, 0}, 147),
  // new StripConfig("columns-strip4", new float[] {40+28+60+183+182, 0, 12}, new float[] {0, 0, 0}, 145),

  // new StripConfig("columns-strip5", new float[] {40+28, 19, 12}, new float[] {0, 0, 0}, 49),
  // new StripConfig("columns-strip6", new float[] {40+28+61, 19, 12}, new float[] {0, 0, 0}, 146),
  // new StripConfig("columns-strip7", new float[] {40+28+61+182, 19, 12}, new float[] {0, 0, 0}, 143),
  // new StripConfig("columns-strip8", new float[] {40+28+61+182+179, 19, 12}, new float[] {0, 0, 0}, 151),

  // // first square (TRIM POSITION!!!)
  // new StripConfig("columns-strip9", new float[] {150, 22, DISTANCE_TO_FLOOR}, new float[] {0, 0, 0}, 26), // top
  // new StripConfig("columns-strip10", new float[] {150+32, 22, DISTANCE_TO_FLOOR}, new float[] {0, 0, -90}, 25), // right
  // new StripConfig("columns-strip11", new float[] {150+32, -10, DISTANCE_TO_FLOOR}, new float[] {0, 0, -180}, 26), // bottom
  // new StripConfig("columns-strip12", new float[] {150, -10, DISTANCE_TO_FLOOR}, new float[] {0, 0, -270}, 26), // left

  // // second square (TRIM POSITION!!!)
  // new StripConfig("columns-strip13", new float[] {321, 22, DISTANCE_TO_FLOOR}, new float[] {0, 0, 0}, 27), // top
  // new StripConfig("columns-strip14", new float[] {321+32, 22, DISTANCE_TO_FLOOR}, new float[] {0, 0, -90}, 25), // right
  // new StripConfig("columns-strip15", new float[] {321+32, -10, DISTANCE_TO_FLOOR}, new float[] {0, 0, -180}, 26), // bottom
  // new StripConfig("columns-strip16", new float[] {321, -10, DISTANCE_TO_FLOOR}, new float[] {0, 0, -270}, 26), // left

  // // third square (TRIM POSITION!!!)
  // new StripConfig("columns-strip17", new float[] {491, 22, DISTANCE_TO_FLOOR}, new float[] {0, 0, 0}, 28), // top
  // new StripConfig("columns-strip18", new float[] {491+32, 22, DISTANCE_TO_FLOOR}, new float[] {0, 0, -90}, 27), // right
  // new StripConfig("columns-strip19", new float[] {491+32, -10, DISTANCE_TO_FLOOR}, new float[] {0, 0, -180}, 26), // bottom
  // new StripConfig("columns-strip20", new float[] {491, -10, DISTANCE_TO_FLOOR}, new float[] {0, 0, -270}, 26), // left

};

/**
 * Stairs
 *---------------------------------------------------------------------------------------------*/

static final float stairs_offset_x = 280;
static final float stairs_offset_y = 30;
static final float stairs_offset_z = 40;
static final float stairs_rotation_x = 0;
static final float stairs_rotation_y = 0;
static final float stairs_rotation_z = 175;

static final StripConfig[] STAIRS_STRIP_CONFIG = {
  // PROBLEMS TO FIX!!!
  // //top to bottom (just straight strips for now)
  // new StripConfig("stairs-strip1", new float[] {0, 0, 0}, new float[] {0, 0, 0}, 34), // top
  // new StripConfig("stairs-strip2", new float[] {-3.5, -12, 8}, new float[] {0, 0, 0}, 46),
  // new StripConfig("stairs-strip3", new float[] {-8.5, -24, 16}, new float[] {0, 0, 0}, 51),
  // new StripConfig("stairs-strip4", new float[] {-8.5, -36, 24}, new float[] {0, 0, 0}, 51),
  // new StripConfig("stairs-strip5", new float[] {-8.5, -48, 32}, new float[] {0, 0, 0}, 51),
  // new StripConfig("stairs-strip6", new float[] {-8.5, -60, 40}, new float[] {0, 0, 0}, 51),
  // new StripConfig("stairs-strip7", new float[] {-8.5, -72, 48}, new float[] {0, 0, 0}, 51),
  // new StripConfig("stairs-strip8", new float[] {-8.5, -84, 56}, new float[] {0, 0, 0}, 51),
  // new StripConfig("stairs-strip9", new float[] {-20, -96, 64}, new float[] {0, 0, 0}, 73), // bottom

};

/**
 * Big Long Section
 *---------------------------------------------------------------------------------------------*/

static final float big_long_section_offset_x = 108; // TRIM THIS !!!
static final float big_long_section_offset_y = 203+25;
static final float big_long_section_offset_z = 0;
static final float big_long_section_rotation_x = 0;
static final float big_long_section_rotation_y = 0;
static final float big_long_section_rotation_z = 0;

static final StripConfig[] BIG_LONG_SECTION_STRIP_CONFIG = {

  // // horizontal bottom
  // new StripConfig("big-long-section-strip1", new float[] {0, 0, 0}, new float[] {0, 0, 0}, 199), // contains corner pixel
  // new StripConfig("big-long-section-strip2", new float[] {255, 0, 0}, new float[] {0, 0, 0}, 73),
  // new StripConfig("big-long-section-strip3", new float[] {255+100, 0, 0}, new float[] {0, 0, 0}, 153),

  // // horizontal top
  // new StripConfig("big-long-section-strip4", new float[] {83, 54, 0}, new float[] {0, 0, 0}, 84),
  // new StripConfig("big-long-section-strip5", new float[] {83+112, 54, 0}, new float[] {0, 0, 0}, 185),
  // new StripConfig("big-long-section-strip6", new float[] {83+112+237, 54, 0}, new float[] {0, 0, 0}, 93),

  // // angles
  // new StripConfig("big-long-section-strip7", new float[] {1, 1, 0}, new float[] {0, 0, 35}, 73),

  // new StripConfig("big-long-section-strip8", new float[] {250, -2, 0}, new float[] {0, 0, 138}, 70),
  // new StripConfig("big-long-section-strip9", new float[] {255, -1, 0}, new float[] {0, 0, 138}, 69),

  // new StripConfig("big-long-section-strip10", new float[] {112+237-2, 2, 0}, new float[] {0, 0, 34}, 72),
  // new StripConfig("big-long-section-strip11", new float[] {112+237+6, 2, 0}, new float[] {0, 0, 34}, 71),
};

/**
 * Stage and Entrance
 *---------------------------------------------------------------------------------------------*/
static final float stage_and_entrance_offset_x = 155;
static final float stage_and_entrance_offset_y = 295;
static final float stage_and_entrance_offset_z = 0;
static final float stage_and_entrance_rotation_x = 0;
static final float stage_and_entrance_rotation_y = 0;
static final float stage_and_entrance_rotation_z = 0;

static final StripConfig[] STAGE_AND_ENTRANCE_STRIP_CONFIG = {
  // strip id, {x, y, z}, {xRot, yRot, zRot}, num leds, length

  // bottom long horizontal
  new StripConfig("stage-and-entrance-strip1", new float[] {0, 0, 0}, new float[] {0, 0, 0}, 150),
  new StripConfig("stage-and-entrance-strip2", new float[] {188, 0, 0}, new float[] {0, 0, 0}, 150),
  new StripConfig("stage-and-entrance-strip3", new float[] {376, 0, 0}, new float[] {0, 0, 0}, 60),

  // bathrooms
  new StripConfig("stage-and-entrance-strip4", new float[] {451, 0, 0}, new float[] {0, 0, 90}, 72),
  new StripConfig("stage-and-entrance-strip5", new float[] {451, 90, 0}, new float[] {0, 0, 180}, 29),

  // stage (angle, then stage)
  new StripConfig("stage-and-entrance-strip6", new float[] {1, 1, 0}, new float[] {0, 0, 33}, 101),
  new StripConfig("stage-and-entrance-strip7", new float[] {109, 70, 0}, new float[] {0, 0, 0}, 158), // NEEDS TO BE AN ANGLE!! UGH

  // floor
  new StripConfig("stage-and-entrance-strip8", new float[] {355, 65, DISTANCE_TO_FLOOR}, new float[] {0, 0, -90}, 26),
  new StripConfig("stage-and-entrance-strip9", new float[] {355, 65-32, DISTANCE_TO_FLOOR}, new float[] {0, 0, 0}, 75),
  new StripConfig("stage-and-entrance-strip10", new float[] {355+94, 65-32, DISTANCE_TO_FLOOR}, new float[] {0, 0, 90}, 49),
};

static final StripConfig[] TEST_STRIP_CONFIG = {
  // // 1
  // new StripConfig("0",           0,  0,  0,    0,     0,     0,       170,                 0.25),
  // new StripConfig("0",           0,  10,  0,    0,     0,     0,       170,                 0.25),
  // new StripConfig("0",           0,  20,  0,    0,     0,     0,       170,                 0.25),
  // new StripConfig("0",           0,  30,  0,    0,     0,     0,       170,                 0.25),
  // new StripConfig("0",           0,  40,  0,    0,     0,     0,       170,                 0.25),
  // new StripConfig("0",           0,  50,  0,    0,     0,     0,       170,                 0.25),
  // new StripConfig("0",           0,  60,  0,    0,     0,     0,       170,                 0.25),
  // new StripConfig("0",           0,  70,  0,    0,     0,     0,       170,                 0.25),

  // //2
  // new StripConfig("0",           0,  80,  0,    0,     0,     0,       170,                 0.25),
  // new StripConfig("0",           0,  90,  0,    0,     0,     0,       170,                 0.25),
  // new StripConfig("0",           0,  100,  0,    0,     0,     0,       170,                 0.25),
  // new StripConfig("0",           0,  110,  0,    0,     0,     0,       170,                 0.25),
  // new StripConfig("0",           0,  120,  0,    0,     0,     0,       170,                 0.25),
  // new StripConfig("0",           0,  130,  0,    0,     0,     0,       170,                 0.25),
  // new StripConfig("0",           0,  140,  0,    0,     0,     0,       170,                 0.25),
  // new StripConfig("0",           0,  150,  0,    0,     0,     0,       170,                 0.25),

  // // 3
  // new StripConfig("0",           0,  160,  0,    0,     0,     0,       170,                 0.25),
  // new StripConfig("0",           0,  170,  0,    0,     0,     0,       170,                 0.25),
  // new StripConfig("0",           0,  180,  0,    0,     0,     0,       170,                 0.25),
  // new StripConfig("0",           0,  190,  0,    0,     0,     0,       170,                 0.25),
  // new StripConfig("0",           0,  200,  0,    0,     0,     0,       170,                 0.25),
  // new StripConfig("0",           0,  210,  0,    0,     0,     0,       170,                 0.25),
  // new StripConfig("0",           0,  220,  0,    0,     0,     0,       170,                 0.25),
  // new StripConfig("0",           0,  230,  0,    0,     0,     0,       170,                 0.25),

  // // 4
  // new StripConfig("0",           0,  240,  0,    0,     0,     0,       170,                 0.25),
  // new StripConfig("0",           0,  250,  0,    0,     0,     0,       170,                 0.25),
  // new StripConfig("0",           0,  260,  0,    0,     0,     0,       170,                 0.25),
  // new StripConfig("0",           0,  270,  0,    0,     0,     0,       170,                 0.25),
  // new StripConfig("0",           0,  280,  0,    0,     0,     0,       170,                 0.25),
  // new StripConfig("0",           0,  290,  0,    0,     0,     0,       170,                 0.25),
  // new StripConfig("0",           0,  300,  0,    0,     0,     0,       170,                 0.25),
  // new StripConfig("0",           0,  310,  0,    0,     0,     0,       170,                 0.25),

  // // 5
  // new StripConfig("0",           0,  320,  0,    0,     0,     0,       170,                 0.25),
  // new StripConfig("0",           0,  330,  0,    0,     0,     0,       170,                 0.25),
  // new StripConfig("0",           0,  340,  0,    0,     0,     0,       170,                 0.25),
  // new StripConfig("0",           0,  350,  0,    0,     0,     0,       170,                 0.25),
  // new StripConfig("0",           0,  360,  0,    0,     0,     0,       170,                 0.25),
  // new StripConfig("0",           0,  370,  0,    0,     0,     0,       170,                 0.25),
  // new StripConfig("0",           0,  380,  0,    0,     0,     0,       170,                 0.25),
  // new StripConfig("0",           0,  390,  0,    0,     0,     0,       170,                 0.25),

  // // 6
  // new StripConfig("0",           0,  400,  0,    0,     0,     0,       170,                 0.25),
  // new StripConfig("0",           0,  410,  0,    0,     0,     0,       170,                 0.25),
  // new StripConfig("0",           0,  420,  0,    0,     0,     0,       170,                 0.25),
  // new StripConfig("0",           0,  430,  0,    0,     0,     0,       170,                 0.25),
  // new StripConfig("0",           0,  440,  0,    0,     0,     0,       170,                 0.25),
  // new StripConfig("0",           0,  450,  0,    0,     0,     0,       170,                 0.25),
  // new StripConfig("0",           0,  460,  0,    0,     0,     0,       170,                 0.25),
  // new StripConfig("0",           0,  470,  0,    0,     0,     0,       170,                 0.25),

  // // 7
  // new StripConfig("0",           0,  480,  0,    0,     0,     0,       170,                 0.25),
  // new StripConfig("0",           0,  490,  0,    0,     0,     0,       170,                 0.25),
  // new StripConfig("0",           0,  500,  0,    0,     0,     0,       170,                 0.25),
  // new StripConfig("0",           0,  510,  0,    0,     0,     0,       170,                 0.25),
  // new StripConfig("0",           0,  520,  0,    0,     0,     0,       170,                 0.25),
  // new StripConfig("0",           0,  530,  0,    0,     0,     0,       170,                 0.25),
  // new StripConfig("0",           0,  540,  0,    0,     0,     0,       170,                 0.25),
  // new StripConfig("0",           0,  550,  0,    0,     0,     0,       170,                 0.25),

  // // 8
  // new StripConfig("0",           0,  560,  0,    0,     0,     0,       170,                 0.25),
  // new StripConfig("0",           0,  570,  0,    0,     0,     0,       170,                 0.25),
  // new StripConfig("0",           0,  580,  0,    0,     0,     0,       170,                 0.25),
  // new StripConfig("0",           0,  590,  0,    0,     0,     0,       170,                 0.25),
  // new StripConfig("0",           0,  600,  0,    0,     0,     0,       170,                 0.25),
  // new StripConfig("0",           0,  610,  0,    0,     0,     0,       170,                 0.25),
  // new StripConfig("0",           0,  620,  0,    0,     0,     0,       170,                 0.25),
  // new StripConfig("0",           0,  630,  0,    0,     0,     0,       170,                 0.25),

  // // 9
  // new StripConfig("0",           0,  640,  0,    0,     0,     0,       170,                 0.25),
  // new StripConfig("0",           0,  650,  0,    0,     0,     0,       170,                 0.25),
  // new StripConfig("0",           0,  660,  0,    0,     0,     0,       170,                 0.25),
  // new StripConfig("0",           0,  670,  0,    0,     0,     0,       170,                 0.25),
  // new StripConfig("0",           0,  680,  0,    0,     0,     0,       170,                 0.25),
  // new StripConfig("0",           0,  690,  0,    0,     0,     0,       170,                 0.25),
  // new StripConfig("0",           0,  700,  0,    0,     0,     0,       170,                 0.25),
  // new StripConfig("0",           0,  710,  0,    0,     0,     0,       170,                 0.25),

  // // 10
  // new StripConfig("0",           0,  720,  0,    0,     0,     0,       170,                 0.25),
  // new StripConfig("0",           0,  730,  0,    0,     0,     0,       170,                 0.25),
  // new StripConfig("0",           0,  740,  0,    0,     0,     0,       170,                 0.25),
  // new StripConfig("0",           0,  750,  0,    0,     0,     0,       170,                 0.25),
  // new StripConfig("0",           0,  760,  0,    0,     0,     0,       170,                 0.25),
  // new StripConfig("0",           0,  770,  0,    0,     0,     0,       170,                 0.25),
  // new StripConfig("0",           0,  780,  0,    0,     0,     0,       170,                 0.25),
  // new StripConfig("0",           0,  790,  0,    0,     0,     0,       170,                 0.25),
};

static class StripConfig {
  String id;
  int numPoints;
  float length;
  float x;
  float y;
  float z;
  float xRot;
  float yRot;
  float zRot;

  StripConfig(String id, float[] coordinates, float[] rotations, int numPoints) {
    this(id, coordinates[0], coordinates[1], coordinates[2], rotations[0], rotations[1], rotations[2], numPoints);
  }

  StripConfig(String id, float x, float y, float z, float xRot, float yRot, float zRot, int numPoints) {
    this.id = id;
    this.numPoints = numPoints;
    this.x = x;
    this.y = y;
    this.z = z;
    this.xRot = xRot;
    this.yRot = yRot;
    this.zRot = zRot;
  }
}

static class TowerConfig {

  final Cube.Type type;
  final float x;
  final float y;
  final float z;
  final float xRot;
  final float yRot;
  final float zRot;
  final String[] ids;
  final float[] yValues;

  TowerConfig(float x, float y, float z, String[] ids) {
    this(Cube.Type.LARGE, x, y, z, ids);
  }

  TowerConfig(float x, float y, float z, float yRot, String[] ids) {
    this(x, y, z, 0, yRot, 0, ids);
  }

  TowerConfig(Cube.Type type, float x, float y, float z, String[] ids) {
    this(type, x, y, z, 0, 0, 0, ids);
  }

  TowerConfig(Cube.Type type, float x, float y, float z, float yRot, String[] ids) {
    this(type, x, y, z, 0, yRot, 0, ids);
  }

  TowerConfig(float x, float y, float z, float xRot, float yRot, float zRot, String[] ids) {
    this(Cube.Type.LARGE, x, y, z, xRot, yRot, zRot, ids);
  }

  TowerConfig(Cube.Type type, float x, float y, float z, float xRot, float yRot, float zRot, String[] ids) {
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

Map<String, String> macToPhysid = new HashMap<String, String>();
Map<String, String> physidToMac = new HashMap<String, String>();

public SLModel buildModel() {

  byte[] bytes = loadBytes("physid_to_mac.json");
  if (bytes != null) {
    try {
      JsonObject json = new Gson().fromJson(new String(bytes), JsonObject.class);
      for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
        macToPhysid.put(entry.getValue().getAsString(), entry.getKey());
        physidToMac.put(entry.getKey(), entry.getValue().getAsString());
      }
    } catch (JsonSyntaxException e) {
      e.printStackTrace();
    }
  }

  // Any global transforms
  LXTransform globalTransform = new LXTransform();
  globalTransform.translate(globalOffsetX, globalOffsetY, globalOffsetZ);
  globalTransform.rotateY(globalRotationY * PI / 180.);
  globalTransform.rotateX(globalRotationX * PI / 180.);
  globalTransform.rotateZ(globalRotationZ * PI / 180.);

  /* Cubes ----------------------------------------------------------*/
  List<Tower> towers = new ArrayList<Tower>();
  List<Cube> allCubes = new ArrayList<Cube>();

  for (TowerConfig config : TOWER_CONFIG) {
    List<Cube> cubes = new ArrayList<Cube>();
    float x = config.x;
    float z = config.z;
    float xRot = config.xRot;
    float yRot = config.yRot;
    float zRot = config.zRot;
    Cube.Type type = config.type;

    for (int i = 0; i < config.ids.length; i++) {
      float y = config.yValues[i];
      Cube cube = new Cube(config.ids[i], x, y, z, xRot, yRot, zRot, globalTransform, type);
      cubes.add(cube);
      allCubes.add(cube);
    }
    towers.add(new Tower("", cubes));
  }
  /*-----------------------------------------------------------------*/

  /* Strips ----------------------------------------------------------*/
  List<Strip> strips = new ArrayList<Strip>();

  // Vip Lounge
  globalTransform.push();
  globalTransform.translate(vip_lounge_offset_x, vip_lounge_offset_y, vip_lounge_offset_z);
  globalTransform.rotateX(vip_lounge_rotation_x * PI / 180.);
  globalTransform.rotateY(vip_lounge_rotation_y * PI / 180.);
  globalTransform.rotateZ(vip_lounge_rotation_z * PI / 180.);

  for (StripConfig stripConfig : VIP_LOUGNE_STRIP_CONFIG) {
    Strip.Metrics metrics = new Strip.Metrics(stripConfig.numPoints, PIXEL_PITCH);

    globalTransform.push();
    globalTransform.translate(stripConfig.x, stripConfig.y, stripConfig.z);
    globalTransform.rotateY(stripConfig.xRot * PI / 180.);
    globalTransform.rotateX(stripConfig.yRot * PI / 180.);
    globalTransform.rotateZ(stripConfig.zRot * PI / 180.);

    strips.add(new Strip(stripConfig.id, metrics, stripConfig.yRot, globalTransform, true));

    globalTransform.pop();
  }
  globalTransform.pop();
  /*-----------------------------------------------------------------*/

  // Long Skinny
  globalTransform.push();
  globalTransform.translate(long_skinny_offset_x, long_skinny_offset_y, long_skinny_offset_z);
  globalTransform.rotateX(long_skinny_rotation_x *  PI / 180.);
  globalTransform.rotateY(long_skinny_rotation_y *  PI / 180.);
  globalTransform.rotateZ(long_skinny_rotation_z *  PI / 180.);

  for (StripConfig stripConfig : LONG_SKINNY_RUN_STRIP_CONFIG) {
    Strip.Metrics metrics = new Strip.Metrics(stripConfig.numPoints, PIXEL_PITCH);

    globalTransform.push();
    globalTransform.translate(stripConfig.x, stripConfig.y, stripConfig.z);
    globalTransform.rotateY(stripConfig.xRot * PI / 180.);
    globalTransform.rotateX(stripConfig.yRot * PI / 180.);
    globalTransform.rotateZ(stripConfig.zRot * PI / 180.);

    strips.add(new Strip(stripConfig.id, metrics, stripConfig.yRot, globalTransform, true));

    globalTransform.pop();
  }
  globalTransform.pop();
  /*-----------------------------------------------------------------*/

  // Columns (and ceiling above)
  globalTransform.push();
  globalTransform.translate(columns_offset_x, columns_offset_y, columns_offset_z);
  globalTransform.rotateX(columns_rotation_x *  PI / 180.);
  globalTransform.rotateY(columns_rotation_y *  PI / 180.);
  globalTransform.rotateZ(columns_rotation_z *  PI / 180.);

  for (StripConfig stripConfig : COLUMNS_STRIP_CONFIG) {
    Strip.Metrics metrics = new Strip.Metrics(stripConfig.numPoints, PIXEL_PITCH);

    globalTransform.push();
    globalTransform.translate(stripConfig.x, stripConfig.y, stripConfig.z);
    globalTransform.rotateY(stripConfig.xRot * PI / 180.);
    globalTransform.rotateX(stripConfig.yRot * PI / 180.);
    globalTransform.rotateZ(stripConfig.zRot * PI / 180.);

    strips.add(new Strip(stripConfig.id, metrics, stripConfig.yRot, globalTransform, true));

    globalTransform.pop();
  }
  globalTransform.pop();
  /*-----------------------------------------------------------------*/

  // Stairs
  globalTransform.push();
  globalTransform.translate(stairs_offset_x, stairs_offset_y, stairs_offset_z);
  globalTransform.rotateX(stairs_rotation_x *  PI / 180.);
  globalTransform.rotateY(stairs_rotation_y *  PI / 180.);
  globalTransform.rotateZ(stairs_rotation_z *  PI / 180.);

  for (StripConfig stripConfig : STAIRS_STRIP_CONFIG) {
    Strip.Metrics metrics = new Strip.Metrics(stripConfig.numPoints, PIXEL_PITCH);

    globalTransform.push();
    globalTransform.translate(stripConfig.x, stripConfig.y, stripConfig.z);
    globalTransform.rotateY(stripConfig.xRot * PI / 180.);
    globalTransform.rotateX(stripConfig.yRot * PI / 180.);
    globalTransform.rotateZ(stripConfig.zRot * PI / 180.);

    strips.add(new Strip(stripConfig.id, metrics, stripConfig.yRot, globalTransform, true));

    globalTransform.pop();
  }
  globalTransform.pop();
  /*-----------------------------------------------------------------*/

  // Big Long Section
  globalTransform.push();
  globalTransform.translate(big_long_section_offset_x, big_long_section_offset_y, big_long_section_offset_z);
  globalTransform.rotateX(big_long_section_rotation_x *  PI / 180.);
  globalTransform.rotateY(big_long_section_rotation_y *  PI / 180.);
  globalTransform.rotateZ(big_long_section_rotation_z *  PI / 180.);

  for (StripConfig stripConfig : BIG_LONG_SECTION_STRIP_CONFIG) {
    Strip.Metrics metrics = new Strip.Metrics(stripConfig.numPoints, PIXEL_PITCH);

    globalTransform.push();
    globalTransform.translate(stripConfig.x, stripConfig.y, stripConfig.z);
    globalTransform.rotateY(stripConfig.xRot * PI / 180.);
    globalTransform.rotateX(stripConfig.yRot * PI / 180.);
    globalTransform.rotateZ(stripConfig.zRot * PI / 180.);

    strips.add(new Strip(stripConfig.id, metrics, stripConfig.yRot, globalTransform, true));

    globalTransform.pop();
  }
  globalTransform.pop();
  /*-----------------------------------------------------------------*/

  // Stage and Entrance
  globalTransform.push();
  globalTransform.translate(stage_and_entrance_offset_x, stage_and_entrance_offset_y, stage_and_entrance_offset_z);
  globalTransform.rotateX(stage_and_entrance_rotation_x *  PI / 180.);
  globalTransform.rotateY(stage_and_entrance_rotation_y *  PI / 180.);
  globalTransform.rotateZ(stage_and_entrance_rotation_z *  PI / 180.);

  for (StripConfig stripConfig : STAGE_AND_ENTRANCE_STRIP_CONFIG) {
    Strip.Metrics metrics = new Strip.Metrics(stripConfig.numPoints, PIXEL_PITCH);

    globalTransform.push();
    globalTransform.translate(stripConfig.x, stripConfig.y, stripConfig.z);
    globalTransform.rotateY(stripConfig.xRot * PI / 180.);
    globalTransform.rotateX(stripConfig.yRot * PI / 180.);
    globalTransform.rotateZ(stripConfig.zRot * PI / 180.);

    strips.add(new Strip(stripConfig.id, metrics, stripConfig.yRot, globalTransform, true));

    globalTransform.pop();
  }
  globalTransform.pop();
  /*-----------------------------------------------------------------*/

  Cube[] allCubesArr = new Cube[allCubes.size()];
  for (int i = 0; i < allCubesArr.length; i++) {
    allCubesArr[i] = allCubes.get(i);
  }

  return new SLModel(towers, allCubesArr, strips);
}

public SLModel getModel() {
  return buildModel();
}

/*
 * Mapping Pattern
 *---------------------------------------------------------------------------*/
public class MappingPattern extends SLPattern {
  private final SinLFO pulse = new SinLFO(20, 100, 800);

  public color mappedAndOnNetworkColor    = LXColor.GREEN;
  public color mappedButNotOnNetworkColor = LXColor.BLACK;
  public color unMappedButOnNetworkColor  = LXColor.BLACK;

  public MappingPattern(LX lx) {
    super(lx);
    addModulator(pulse).start();

    final LXParameterListener resetBasis = new LXParameterListener() {
      public void onParameterChanged(LXParameter p) {
        pulse.setBasis(0);
      }
    };

    mappingMode.mode.addListener(resetBasis);
    mappingMode.displayMode.addListener(resetBasis);
    mappingMode.selectedMappedFixture.addListener(resetBasis);
    mappingMode.selectedUnMappedFixture.addListener(resetBasis);
  }

  public void run(double deltaMs) {
    if (!mappingMode.enabled.isOn()) return;
    setColors(0);
    updateColors();

    if (mappingMode.inMappedMode())
      loopMappedFixtures(deltaMs);
    else loopUnMappedFixtures(deltaMs);
  }

  private void updateColors() {
    mappedButNotOnNetworkColor = lx.hsb(LXColor.h(LXColor.RED), 100, pulse.getValuef());
    unMappedButOnNetworkColor = lx.hsb(LXColor.h(LXColor.BLUE), 100, pulse.getValuef());
  }

  private void loopMappedFixtures(double deltaMs) {
    if (mappingMode.inDisplayAllMode()) {

      for (String id : mappingMode.fixturesMappedAndOnTheNetwork)
        setFixtureColor(id, mappedAndOnNetworkColor);

      for (String id : mappingMode.fixturesMappedButNotOnNetwork)
        setFixtureColor(id, mappedButNotOnNetworkColor);

    } else {
      String selectedId = mappingMode.selectedMappedFixture.getOption();

      for (String id : mappingMode.fixturesMappedAndOnTheNetwork)
        setFixtureColor(id, mappedAndOnNetworkColor, true);

      if (mappingMode.fixturesMappedAndOnTheNetwork.contains(selectedId))
        setFixtureColor(selectedId, mappedAndOnNetworkColor);

      if (mappingMode.fixturesMappedButNotOnNetwork.contains(selectedId))
        setFixtureColor(selectedId, mappedButNotOnNetworkColor);
    }
  }

  private void loopUnMappedFixtures(double deltaMs) {
    for (String id : mappingMode.fixturesMappedAndOnTheNetwork)
      setFixtureColor(id, mappedAndOnNetworkColor, true);
  }

  private void setFixtureColor(String id, color col) {
    setFixtureColor(id, col, false);
  }

  private void setFixtureColor(String id, color col, boolean dotted) {
    if (id.equals("-")) return;

    // we iterate all cubes and call continue here because multiple cubes might have zero as id
    for (Cube c : model.cubes) {
      if (!c.id.equals(id)) continue;

      LXPoint[] points = c.points;
      for (int i = 0; i < points.length; i++) {
        if (dotted)
          col = (i % 2 == 0) ? LXColor.scaleBrightness(LXColor.GREEN, 0.2) : LXColor.BLACK;

        setColor(points[i].index, col);
      }
    }
  }
}

/*
 * Mapping Mode
 * (TODO) 
 *  1) iterate through mapped cubes in order (tower by tower, cube by cube)
 *  2) get cubes not mapped but on network to pulse
 *  3) get a "display orientation" mode
 *---------------------------------------------------------------------------*/
public static enum MappingModeType {MAPPED, UNMAPPED};
public static enum MappingDisplayModeType {ALL, ITERATE};

public class MappingMode {
  private LXChannel mappingChannel = null;
  private LXPattern mappingPattern = null;

  public final BooleanParameter enabled;
  public final EnumParameter<MappingModeType> mode;
  public final EnumParameter<MappingDisplayModeType> displayMode;
  //public final BooleanParameter displayOrientation;

  public final DiscreteParameter selectedMappedFixture;
  public final DiscreteParameter selectedUnMappedFixture;

  public final List<String> fixturesMappedAndOnTheNetwork = new ArrayList<String>();
  public final List<String> fixturesMappedButNotOnNetwork = new ArrayList<String>();
  public final List<String> fixturesOnNetworkButNotMapped = new ArrayList<String>();

  MappingMode(LX lx) {
    this.enabled = new BooleanParameter("enabled", false)
     .setDescription("Mapping Mode: toggle on/off");

    this.mode = new EnumParameter<MappingModeType>("mode", MappingModeType.MAPPED)
     .setDescription("Mapping Mode: toggle between mapped/unmapped fixtures");

    this.displayMode = new EnumParameter<MappingDisplayModeType>("displayMode", MappingDisplayModeType.ALL)
     .setDescription("Mapping Mode: display all mapped/unmapped fixtures");

    // this.displayOrientation = new BooleanParameter("displayOrientation", false)
    //  .setDescription("Mapping Mode: display colors on strips to indicate it's orientation");

    for (Cube cube : model.cubes)
      fixturesMappedButNotOnNetwork.add(cube.id);

    this.selectedMappedFixture = new DiscreteParameter("selectedMappedFixture", fixturesMappedButNotOnNetwork.toArray());
    this.selectedUnMappedFixture = new DiscreteParameter("selectedUnMappedFixture", new String[] {"-"});

    controllers.addListener(new ListListener<SLController>() {
      void itemAdded(final int index, final SLController c) {
        if (isFixtureMapped(c.cubeId)) {
          fixturesMappedButNotOnNetwork.remove(c.cubeId);
          fixturesMappedAndOnTheNetwork.add(c.cubeId);
        } else {
          fixturesOnNetworkButNotMapped.add(c.cubeId);
        }

        Object[] arr1 = fixturesMappedAndOnTheNetwork.toArray();
        Object[] arr2 = fixturesOnNetworkButNotMapped.toArray();

        selectedMappedFixture.setObjects(arr1.length > 0 ? arr1 : new String[] {"-"});
        selectedUnMappedFixture.setObjects(arr2.length > 0 ? arr2 : new String[] {"-"});
      }
      void itemRemoved(final int index, final SLController c) {}
    });

    enabled.addListener(new LXParameterListener() {
      public void onParameterChanged(LXParameter p) {
        if (((BooleanParameter)p).isOn())
          addChannel();
        else removeChannel();
      }
    });
  }

  private boolean isFixtureMapped(String id) {
    for (Cube fixture : model.cubes) {
      if (fixture.id.equals(id))
        return true;
    }
    return false;
  }

  public boolean inMappedMode() {
    return mode.getObject() == MappingModeType.MAPPED;
  }

  public boolean inUnMappedMode() {
    return mode.getObject() == MappingModeType.UNMAPPED;
  }

  public boolean inDisplayAllMode() {
    return displayMode.getObject() == MappingDisplayModeType.ALL;
  }

  public boolean inIterateFixturesMode() {
    return displayMode.getObject() == MappingDisplayModeType.ITERATE;
  }

  public String getSelectedMappedFixtureId() {
    return (String)mappingMode.selectedMappedFixture.getOption();
  }

  public String getSelectedUnMappedFixtureId() {
    return (String)mappingMode.selectedUnMappedFixture.getOption();
  }

  public boolean isSelectedUnMappedFixture(String id) {
    return id.equals(mappingMode.selectedUnMappedFixture.getOption());
  }

  public color getUnMappedColor() {
    // if (mappingPattern != null)
    //   return mappingPattern.getUnMappedButOnNetworkColor;
    // return 0;
    return LXColor.RED; // temp
  }

  private void addChannel() {
    mappingPattern = new MappingPattern(lx);
    mappingChannel = lx.engine.addChannel(new LXPattern[] {mappingPattern});

    for (LXChannel channel : lx.engine.channels)
      channel.cueActive.setValue(false);

    mappingChannel.fader.setValue(1);
    mappingChannel.label.setValue("Mapping");
    mappingChannel.cueActive.setValue(true);
  }

  private void removeChannel() {
    lx.engine.removeChannel(mappingChannel);
    mappingChannel = null;
    mappingPattern = null;
  }
}

class UIUniverse extends UICollapsibleSection {

  UIUniverse(LX lx, UI ui, float x, float y, float w) {
    super(ui, x, y, w, 124);
    setTitle("UNIVERSES");
    setTitleX(20);

    final UIButton decrementUniverse = new UIButton(122, 2, 25, 18) {
      @Override
      protected void onToggle(boolean active) {
        if (!active) return;
        universe.setValue(Math.max(((int)universe.getValue())-1, 0));
      }
    }.setLabel("-").setMomentary(true);
    decrementUniverse.addToContainer(this);

    final UIButton incrementUniverse = new UIButton(147, 2, 25, 18) {
      @Override
      protected void onToggle(boolean active) {
        if (!active) return;
        universe.setValue(Math.min(((int)universe.getValue())+1, 79));
      }
    }.setLabel("+").setMomentary(true);
    incrementUniverse.addToContainer(this);


    final UILabel selectedUniverseLabel = new UILabel(0, 24, getContentWidth(), 54)
      .setLabel("1");
    selectedUniverseLabel.setBackgroundColor(#333333)
      .setFont(createFont("ArialUnicodeMS-10.vlw", 43))
      .setTextAlignment(PConstants.CENTER, PConstants.TOP);
    selectedUniverseLabel.addToContainer(this);

    universe.addListener(new LXParameterListener() {
      public void onParameterChanged(LXParameter p) {
        selectedUniverseLabel.setLabel(Integer.toString(((int)universe.getValue())+1));
      }
    });
  }
}

/*
 * Mapping Mode: UI Window
 *---------------------------------------------------------------------------*/
class UIMapping extends UICollapsibleSection {

  UIMapping(LX lx, UI ui, float x, float y, float w) {
    super(ui, x, y, w, 124);
    setTitle("MAPPING");
    setTitleX(20);

    addTopLevelComponent(new UIButton(4, 4, 12, 12) {
      @Override
      public void onToggle(boolean isOn) {
        redraw();
      }
    }.setParameter(mappingMode.enabled).setBorderRounding(4));

    final UIToggleSet toggleMode = new UIToggleSet(0, 2, getContentWidth(), 18)
     .setEvenSpacing().setParameter(mappingMode.mode);
    toggleMode.addToContainer(this);

    final UIMappedPanel mappedPanel = new UIMappedPanel(ui, 0, 20, getContentWidth(), 40);
    mappedPanel.setVisible(mappingMode.inMappedMode());
    mappedPanel.addToContainer(this);

    final UIUnMappedPanel unMappedPanel = new UIUnMappedPanel(ui, 0, 20, getContentWidth(), 40);
    unMappedPanel.setVisible(mappingMode.inUnMappedMode());
    unMappedPanel.addToContainer(this);

    mappingMode.mode.addListener(new LXParameterListener() {
      public void onParameterChanged(LXParameter p) {
        mappedPanel.setVisible(mappingMode.inMappedMode());
        unMappedPanel.setVisible(mappingMode.inUnMappedMode());
        redraw();
      }
    });
  }

  private class UIMappedPanel extends UI2dContainer {
    UIMappedPanel(UI ui, float x, float y, float w, float h) {
      super(x, y, w, h);

      final UIToggleSet toggleDisplayMode = new UIToggleSet(0, 2, 112, 18)
       .setEvenSpacing().setParameter(mappingMode.displayMode);
      toggleDisplayMode.addToContainer(this);

      final UILabel selectedFixtureLabel = new UILabel(0, 24, getContentWidth(), 54)
        .setLabel("");
      selectedFixtureLabel.setBackgroundColor(#333333)
        .setFont(createFont("ArialUnicodeMS-10.vlw", 43))
        .setTextAlignment(PConstants.CENTER, PConstants.TOP);
      selectedFixtureLabel.addToContainer(this);
      mappingMode.selectedMappedFixture.addListener(new LXParameterListener() {
        public void onParameterChanged(LXParameter p) {
          if (mappingMode.inMappedMode())
            selectedFixtureLabel.setLabel(mappingMode.getSelectedMappedFixtureId());
        }
      });

      mappingMode.displayMode.addListener(new LXParameterListener() {
        public void onParameterChanged(LXParameter p) {
          String label = mappingMode.inDisplayAllMode()
            ? "" : mappingMode.getSelectedMappedFixtureId();
          selectedFixtureLabel.setLabel(label);
          redraw();
        }
      });

      final UIButton decrementSelectedFixture = new UIButton(122, 2, 25, 18) {
        @Override
        protected void onToggle(boolean active) {
          if (mappingMode.inDisplayAllMode() || !active) return;
          mappingMode.selectedMappedFixture.decrement(1);
        }
      }.setLabel("-").setMomentary(true);
      decrementSelectedFixture.addToContainer(this);

      final UIButton incrementSelectedFixture = new UIButton(147, 2, 25, 18) {
        @Override
        protected void onToggle(boolean active) {
          if (mappingMode.inDisplayAllMode() || !active) return;
          mappingMode.selectedMappedFixture.increment(1);
        }
      }.setLabel("+").setMomentary(true);
      incrementSelectedFixture.addToContainer(this);
    }
  }

  private class UIUnMappedPanel extends UI2dContainer {
    UIUnMappedPanel(UI ui, float x, float y, float w, float h) {
      super(x, y, w, h);

      final UIToggleSet toggleDisplayMode = new UIToggleSet(0, 2, 112, 18)
       .setEvenSpacing().setParameter(mappingMode.displayMode);
      toggleDisplayMode.addToContainer(this);

      final UILabel selectedFixtureLabel = new UILabel(0, 24, getContentWidth(), 54)
        .setLabel("");
      selectedFixtureLabel.setBackgroundColor(#333333)
        .setFont(createFont("ArialUnicodeMS-10.vlw", 43))
        .setTextAlignment(PConstants.CENTER, PConstants.TOP);
      selectedFixtureLabel.addToContainer(this);
      mappingMode.selectedUnMappedFixture.addListener(new LXParameterListener() {
        public void onParameterChanged(LXParameter p) {
          if (mappingMode.inUnMappedMode())
            selectedFixtureLabel.setLabel(mappingMode.getSelectedUnMappedFixtureId());
        }
      });

      mappingMode.displayMode.addListener(new LXParameterListener() {
        public void onParameterChanged(LXParameter p) {
          String label = mappingMode.inDisplayAllMode()
            ? "" : mappingMode.getSelectedUnMappedFixtureId();
          selectedFixtureLabel.setLabel(label);
          redraw();
        }
      });

      final UIButton decrementSelectedFixture = new UIButton(122, 2, 25, 18) {
        @Override
        protected void onToggle(boolean active) {
          if (mappingMode.inDisplayAllMode() || !active) return;
          mappingMode.selectedUnMappedFixture.decrement(1);
        }
      }.setLabel("-").setMomentary(true);
      decrementSelectedFixture.addToContainer(this);

      final UIButton incrementSelectedFixture = new UIButton(147, 2, 25, 18) {
        @Override
        protected void onToggle(boolean active) {
          if (mappingMode.inDisplayAllMode() || !active) return;
          mappingMode.selectedUnMappedFixture.increment(1);
        }
      }.setLabel("+").setMomentary(true);
      incrementSelectedFixture.addToContainer(this);
    }

  }
}
