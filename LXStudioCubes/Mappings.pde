/**
 *     DOUBLE BLACK DIAMOND        DOUBLE BLACK DIAMOND
 *
 *         //\\   //\\                 //\\   //\\  
 *        ///\\\ ///\\\               ///\\\ ///\\\
 *        \\\/// \\\///               \\\/// \\\///
 *         \\//   \\//                 \\//   \\//
 *
 *        EXPERTS ONLY!!              EXPERTS ONLY!!
 */

static final float BAR_SPACING = 17.72;
static final float GLOBAL_Y_OFFSET = 50.39;

// mappings
static final BarConfig[] BAR_CONFIG = {
  //            (id)       (controller)  (x Rotation)  (inside trim)   (#LEDs inside)   (#LEDs outside)
/* in studio debug setup:
  new BarConfig("1",          "201",         20.0,           0,             25,              30),  
  new BarConfig("2",          "118",         25.5,           0,             24,              25),
  new BarConfig("3",          "116",         31.0,           0,             28,              28),
  new BarConfig("4",          "306",         36.5,           0,             25,              25) 
*/
  new BarConfig("1",          "118",         20.0,           0,             75,              78),  // 304
  new BarConfig("2",          "116",         25.5,           0,             60,              63), 
  new BarConfig("3",          "144",         31.0,           0,             50,              53), 
  new BarConfig("4",          "144",         36.5,           0,             43,              46),  
  new BarConfig("5",          "201",         42.0,           0,             39,              41),  // 305
  new BarConfig("6",          "201",         47.5,           0,             35,              37),
  new BarConfig("7",          "201",         53.0,           0,             32,              34),
  new BarConfig("8",          "304",         58.5,           0,             31,              34),
  new BarConfig("9",          "304",         64.0,           0,             29,              33), 
  new BarConfig("10",         "304",         69.5,           0,             28,              32),  // 118
  new BarConfig("11",         "306",         69.5,           0,             27,              32),
  new BarConfig("12",         "306",         64.0,           0,             26,              29),
  new BarConfig("13",         "306",         58.5,           0,             27,              30),
  new BarConfig("14",         "400",         53.0,           0,             28,              32),
  new BarConfig("15",         "400",         47.5,           0,             30,              33),
  new BarConfig("16",         "400",         42.0,           0,             35,              37),
  new BarConfig("17",         "401",         36.5,           0,             38,              40),  // 306
  new BarConfig("18",         "401",         31.0,           0,             45,              47),
  new BarConfig("19",         "403",         25.5,           0,             55,              58),
  new BarConfig("20",         "404",         20.0,           0,             69,              72) 

};  

static class BarConfig {
  final String id;
  final String controllerId;
  final float rotX;
  final float insideTrim;
  final int numPointsInside;
  final int numPointsOutside;

  BarConfig(String id, String controllerId, float rotX, float insideTrim, int numPointsInside, int numPointsOutside) {
    this.id = id;
    this.controllerId = controllerId;
    this.rotX = rotX;
    this.insideTrim = insideTrim;
    this.numPointsInside = numPointsInside;
    this.numPointsOutside = numPointsOutside;
  }
}

Map<String, String> macToPhysid = new HashMap<String, String>();

public SLModel buildModel() {

  byte[] bytes = loadBytes("physid_to_mac.json");
  if (bytes != null) {
    try {
      JsonObject json = new Gson().fromJson(new String(bytes), JsonObject.class);
      for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
        macToPhysid.put(entry.getValue().getAsString(), entry.getKey());
      }
    } catch (JsonSyntaxException e) {
      e.printStackTrace();
    }
  }

  List<Bar> allBars = new ArrayList<Bar>();

  LXTransform transform = new LXTransform();
  transform.translate(0, GLOBAL_Y_OFFSET, 0);

  for (BarConfig barConfig : BAR_CONFIG) {
    allBars.add(new Bar(barConfig.id, barConfig.controllerId, transform, barConfig.rotX, barConfig.insideTrim, barConfig.numPointsInside, barConfig.numPointsOutside));
    transform.translate(BAR_SPACING, 0, 0);
  }

  // List<Tower> towers = new ArrayList<Tower>();
  // for (TowerConfig tower : config.towers) {
  //   List<Cube> cubes = new ArrayList<Cube>();

  //   for (int i = 0; i < tower.ids.length; i++) {

  //     LXTransform globalTransform = new LXTransform();
  //     globalTransform.translate(config.globalOffsetX, config.globalOffsetY, config.globalOffsetZ);
  //     if (tower.useAdjustment) {
  //       globalTransform.rotateX(config.globalRotationX * PI / 180.);
  //       globalTransform.rotateY(config.globalRotationY * PI / 180.);
  //       globalTransform.rotateZ(config.globalRotationZ * PI / 180.);
  //     }

  //     float x = tower.x;
  //     float y = tower.yValues[i];
  //     float z = tower.z;
  //     float xRot = tower.xRot;
  //     float yRot = tower.yRot;
  //     float zRot = tower.zRot;
  //     String id = tower.ids[i];
  //     Cube.Type type = tower.type;

  //     Cube cube = new Cube(id, x, y, z, xRot, yRot, zRot, globalTransform, type);
  //     cubes.add(cube);
  //     allCubes.add(cube);
  //   }
  //   towers.add(new Tower("", cubes));
  // }

  // Cube[] allCubesArr = new Cube[allCubes.size()];
  // for (int i = 0; i < allCubesArr.length; i++) {
  //   allCubesArr[i] = allCubes.get(i);
  // }


  // List<Cube> allCubes = new ArrayList<Cube>();

  // // list of chandeliers
  // List<Chandelier> chandeliers = new ArrayList<Chandelier>();

  // println("Num of chandeliers: " + config.chandeliers.size());

  // for (CiscoChandelierConfig chandelierConfig : config.chandeliers) {
  //   List<Tower> towers = new ArrayList<Tower>();
  //   List<Cube> chandelierCubes = new ArrayList<Cube>();

  //   globalTransform.push();
  //   globalTransform.translate(chandelierConfig.x, chandelierConfig.y, chandelierConfig.z);
  //   globalTransform.rotateX(chandelierConfig.xRot * PI / 180.);
  //   globalTransform.rotateY(chandelierConfig.yRot * PI / 180.);
  //   globalTransform.rotateZ(chandelierConfig.zRot * PI / 180.);

  //   for (CiscoTowerConfig towerConfig : chandelierConfig.towers) {
  //     List<Cube> towerCubes = new ArrayList<Cube>();

  //     globalTransform.push();
  //     globalTransform.translate(towerConfig.x, towerConfig.y, towerConfig.z);
  //     globalTransform.rotateX(towerConfig.xRot * PI / 180.);
  //     globalTransform.rotateY(towerConfig.yRot * PI / 180.);
  //     globalTransform.rotateZ(towerConfig.zRot * PI / 180.);

  //     for (CiscoCubeConfig cubeConfig : towerConfig.cubes) {
  //       String id = cubeConfig.id;
  //       float x = cubeConfig.x;
  //       float y = cubeConfig.y;
  //       float z = cubeConfig.z;
  //       float xRot = cubeConfig.xRot;
  //       float yRot = cubeConfig.yRot;
  //       float zRot = cubeConfig.zRot;

  //       Cube cube = new Cube(id, x, y, z, xRot, yRot, zRot, globalTransform, Cube.Type.LARGE);
  //       allCubes.add(cube);
  //       chandelierCubes.add(cube);
  //       towerCubes.add(cube);
  //     }

  //     globalTransform.pop();
  //     towers.add(new Tower(towerConfig.id, towerCubes));
  //   }

  //   globalTransform.pop();
  //   chandeliers.add(new Chandelier(chandelierConfig.id, towers, chandelierCubes));
  // }

  Bar[] allBarsArr = new Bar[allBars.size()];
  
  for (int i = 0; i < allBarsArr.length; i++) {
    allBarsArr[i] = allBars.get(i);
  }

  return new SLModel(allBars, allBarsArr);
}