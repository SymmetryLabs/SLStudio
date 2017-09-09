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


static final float globalOffsetX = 0;
static final float globalOffsetY = 0;
static final float globalOffsetZ = 0;

static final float globalRotationX = 0;
static final float globalRotationY = -45;
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

static final TowerConfig[] TOWER_CONFIG = {

    // left
    new TowerConfig(-SP*3.5, 0, -SP*2.5, new String[] {
      "22", "204"
      }),

    new TowerConfig(-SP*4.5, (JUMP*0)+TOWER_RISER, -SP*3.0, new String[] {
      "86"
      }),

    new TowerConfig(-SP*5.5, (JUMP*0)+0, -SP*3.5, new String[] {
      "5"
      }),

   new TowerConfig(-SP*2.0, 0, -SP*1.0, new String[] {
      "25", "199", "177"
      }),

   new TowerConfig(-SP*1.5, (JUMP*0)+TOWER_RISER, -SP*2.0, new String[] {
      "94"
      }),

   new TowerConfig(-SP*1.0, (JUMP*0)+TOWER_RISER, -SP*0.5, new String[] {
      "90"
      }),

   new TowerConfig(-SP*1.0, (JUMP*2)+TOWER_RISER, -SP*0.5, new String[] {
      "64"
      }),

    // left tower of 5   
    new TowerConfig(0, 0, 0, new String[] {   
      "19", "190", "121", "1", "103"    
    }),   
    new TowerConfig(SP*1.0, (JUMP*0)+TOWER_RISER, -SP*0.5, new String[] {"76"}),    
    new TowerConfig(SP*1.0, (JUMP*2)+TOWER_RISER, -SP*0.5, new String[] {"18"}),    
    new TowerConfig(SP*1.0, (JUMP*1)+TOWER_RISER, +SP*0.5, new String[] {"157"}),   
    new TowerConfig(SP*0.5, (JUMP*3)+TOWER_RISER, -SP*1.0, new String[] {"4"}),   
    new TowerConfig(SP*1.5, (JUMP*2)+0          , -SP*1.5, new String[] {"126"}),   
   
    new TowerConfig(SP*2.0, 0, 0, new String[] {    
      "6", "132", "61", "54"    
    }),   

    new TowerConfig(SP*2.5, (JUMP*1)+TOWER_RISER, -SP*1.0, new String[] {"4"}),   
    new TowerConfig(SP*2.5, (JUMP*3)+TOWER_RISER, -SP*1.0, new String[] {"151"}),   
     
    // middle tower of 5    
    new TowerConfig(SP*3.5, 0, -SP*1.5, new String[] {    
      "111", "166", "187", "158", "101"   
    }),  

    new TowerConfig(SP*4.5, (JUMP*3)+TOWER_RISER, -SP*2.0, new String[] {"11"}),    
    new TowerConfig(SP*3.0, (JUMP*2)+TOWER_RISER, -SP*2.5, new String[] {"163"}),   
    new TowerConfig(SP*2.0, (JUMP*3)+0          , -SP*2.0, new String[] {"34"}),    
    new TowerConfig(SP*4.0, (JUMP*0)+TOWER_RISER, -SP*2.5, new String[] {"17", "44"}),    
     
    new TowerConfig(SP*4.5, 0, -SP*3.5, new String[] {    
      "102", "156", "13", "82"    
    }),   
    new TowerConfig(SP*5.5, (JUMP*2)+TOWER_RISER, -SP*3.5, new String[] {"412"}),   
    new TowerConfig(SP*5.0, (JUMP*0)+TOWER_RISER, -SP*4.0, new String[] {"73"}),    
    new TowerConfig(SP*4.0, (JUMP*1)+TOWER_RISER, -SP*4.0, new String[] {"47"}),    
    new TowerConfig(SP*4.0, (JUMP*3)+TOWER_RISER, -SP*4.0, new String[] {"32"}),    
    new TowerConfig(SP*3.0, (JUMP*3)+0          , -SP*3.5, new String[] {"175"}),   
     
    // right tower of 5   
    new TowerConfig(SP*4.5, 0, -SP*5.0, new String[] {    
      "183", "180", "57", "51", "108"   
    }),   
    new TowerConfig(SP*3.5, (JUMP*0)+TOWER_RISER, -SP*5.5, new String[] {"104"}),   
    new TowerConfig(SP*4.0, (JUMP*2)+TOWER_RISER, -SP*6.0, new String[] {"168"}),   
    new TowerConfig(SP*3.0, (JUMP*2)+3          , -SP*5.5, new String[] {"188"}),   
     
    new TowerConfig(SP*3.0-10, 0, -SP*6.5-12, new String[] {    
     "100", "85", "110zAQ  AZQ"    
    }),   
    new TowerConfig((SP*3.0-10)-(SP*0.5), (JUMP*0)+TOWER_RISER, (-SP*6.5-12)-(SP*1.0), new String[] {"87"}),    
    new TowerConfig((SP*3.0-10)-(SP*0.0), (JUMP*0)+0          , (-SP*6.5-12)-(SP*2.0), new String[] {"33"}),    
    
    // table cubes    
    new TowerConfig(SP*-0.5, 0, -SP*4.0, new String[] {"74"}),    
    new TowerConfig(0, 0, -SP*5.0, new String[] {"171"}),   
    new TowerConfig(SP*1.0, 0, -SP*5.5, new String[] {"9"}),    
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
      Cube cube = new Cube(config.ids[i], x, y, z, xRot, yRot, zRot, globalTransform, type);
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
