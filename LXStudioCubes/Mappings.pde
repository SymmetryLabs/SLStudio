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

import heronarts.lx.parameter.*;
import java.util.*;


static final float globalOffsetX = 19;
static final float globalOffsetY = 1;
static final float globalOffsetZ = 233;

static final float globalRotationX = 0;
static final float globalRotationY = -45;
static final float globalRotationZ = 0;
static final float CUBE_WIDTH = 24;
static final float CUBE_HEIGHT = 24;
static final float TOWER_WIDTH = 24;
static final float TOWER_HEIGHT = 24;
static final float CUBE_SPACING = 2.5;

static final float TOWER_VERTICAL_SPACING = 2.5;
static final float TOWER_VERTICAL_SPACING_BIG = 5;

static final float TOWER_RISER = 14;

static final float JUMP = TOWER_HEIGHT+TOWER_VERTICAL_SPACING;

static final float RIGHT_SIDE_SPACING = 100;

// static final BulbConfig[] BULB_CONFIG = {
//     // new BulbConfig("lifx-1", -50, 50, -30),
//     // new BulbConfig("lifx-2", 0, 50, 0),
//     // new BulbConfig("lifx-3", -65, 20, -100),
//     // new BulbConfig("lifx-4", 0, 0, 0),
//     // new BulbConfig("lifx-5", 0, 0, 0),
// };

static final TowerConfig[] TOWER_CONFIG = {
  // group 1
  new TowerConfig(0, 0, 48, new String[] {"92", "203", "64"}),
  new TowerConfig(0, 0, 0, new String[] {"87", "351", "366"}),
  new TowerConfig(-24, TOWER_RISER, 12, new String[] {"22", "10"}),
  new TowerConfig(12, JUMP+TOWER_RISER, -25, new String[] {"85"}),
  new TowerConfig(12, JUMP+TOWER_RISER, 25, new String[] {"94"}),

  // group 2
  new TowerConfig(25, TOWER_RISER, 11, new String[] {"115"}),
  new TowerConfig(26, 2*JUMP+TOWER_RISER, 36, new String[] {"15"}),
  new TowerConfig(51, 0, 24, new String[] {"63", "314", "71", "43"}),

  // group 3
  new TowerConfig(38, 0, -14, new String[] {"70", "337", "76", "31"}),
  new TowerConfig(63, TOWER_RISER, -2, new String[] {"182"}),
  new TowerConfig(50, TOWER_RISER, -39, new String[] {"137"}),

  // group 4
  new TowerConfig(88, 0, -14, new String[] {"17", "356", "132", "83"}),
  new TowerConfig(76, JUMP+TOWER_RISER, 12, new String[] {"52"}),
  new TowerConfig(63, 2*JUMP+TOWER_RISER, -26, new String[] {"11"}),
  new TowerConfig(100, TOWER_RISER, -39, new String[] {"56"}),
  new TowerConfig(113, 2*JUMP+TOWER_RISER, -26, new String[] {"352"}),

  // group 5
  new TowerConfig(88, 0, -65, new String[] {"120", "68", "54", "46"}),
  new TowerConfig(63, JUMP+TOWER_RISER, -53, new String[] {"181"}),
  new TowerConfig(76, TOWER_RISER, -90, new String[] {"38"}), // check
  new TowerConfig(100, 2*JUMP+TOWER_RISER, -90, new String[] {"62"}),

  // group 6
  new TowerConfig(125, 0, -78, new String[] {"7", "127", "86"}),
  new TowerConfig(113, JUMP+TOWER_RISER, -53, new String[] {"320"}),

  // group 7
  new TowerConfig(112, 0, -115, new String[] {"90", "121", "32"}), // first one check
  new TowerConfig(87, JUMP+TOWER_RISER, -103, new String[] {"156"}),

  // group 7
  new TowerConfig(124, TOWER_RISER, -139, new String[] {"82", "4"}),


  // other side
  new TowerConfig(124, TOWER_RISER, -RIGHT_SIDE_SPACING, -90, new String[] {"383"}),
  new TowerConfig(124, JUMP*2+TOWER_RISER, -RIGHT_SIDE_SPACING, -90, new String[] {"358"}),

  new TowerConfig(112, 0, -RIGHT_SIDE_SPACING-24, -90, new String[] {"91", "40", "340", "360"}), // 240 or 340?, 360?

  new TowerConfig(88, JUMP+TOWER_RISER, -RIGHT_SIDE_SPACING-12, -90, new String[] {"172"}), // check

  new TowerConfig(100, JUMP*2+TOWER_RISER, -RIGHT_SIDE_SPACING-48, -90, new String[] {"57"}),

  new TowerConfig(64, 0, -RIGHT_SIDE_SPACING-24, -90, new String[] {"390", "408", "185"}),

  new TowerConfig(52, TOWER_RISER, -RIGHT_SIDE_SPACING-48, -90, new String[] {"369"}),
  new TowerConfig(52, JUMP*2+TOWER_RISER, -RIGHT_SIDE_SPACING-48, -90, new String[] {"398"}),

  new TowerConfig(76, 0, -RIGHT_SIDE_SPACING-60, -90, new String[] {"326", "128", "391", "341"}),

  new TowerConfig(28, 0, -RIGHT_SIDE_SPACING-60, -90, new String[] {"129", "13", "77"}),
};



static final StripConfig[] STRIP_CONFIG = {
          // controller id         x   y   z  xRot   yRot   zRot   num leds      pitch in inches
//new StripConfig("206",            0,  0,  0,    0,     0,     0,        10,                 0.25),

};

static class StripConfig {
  String id;
  int numPoints;
  float spacing;
  float x;
  float y;
  float z;
  float xRot;
  float yRot;
  float zRot;

  StripConfig(String id, float x, float y, float z, float xRot, float yRot, float zRot, int numPoints, float spacing) {
    this.id = id;
    this.numPoints = numPoints;
    this.spacing = spacing;
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
      Cube cube = new Cube(config.ids[i], x, y, z, xRot+180, yRot+270, zRot, globalTransform, type);
      cubes.add(cube);
      allCubes.add(cube);
    }
    towers.add(new Tower("", cubes));
  }
  /*-----------------------------------------------------------------*/

  /* Strips ----------------------------------------------------------*/
  List<Strip> strips = new ArrayList<Strip>();

  for (StripConfig stripConfig : STRIP_CONFIG) {
    Strip.Metrics metrics = new Strip.Metrics(stripConfig.numPoints, stripConfig.spacing);

    globalTransform.push();
    globalTransform.translate(stripConfig.x, stripConfig.y, stripConfig.z);
    globalTransform.rotateY(stripConfig.xRot * PI / 180.);
    globalTransform.rotateX(stripConfig.yRot * PI / 180.);
    globalTransform.rotateZ(stripConfig.zRot * PI / 180.);

    strips.add(new Strip(metrics, stripConfig.yRot, globalTransform, true));

    globalTransform.pop();
  }
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
