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

/* Upstairs Ring Chandelier -----------------------------------------------*/
static final float upstairsOffsetX = 140;
static final float upstairsOffsetY = 120;
static final float upstairsOffsetZ = 60;

static final float upstairsRotationX = 0;
static final float upstairsRotationY = 0;
static final float upstairsRotationZ = 0;

// Upstairs Rings Chandelier
RingChandelierConfig upstairsRingsConfig = new RingChandelierConfig(
  new float[] {0, 0, 0},
  new float[] {0, 15, 0},
  new RingConfig[] {
    new RingConfig("600", new float[] {0, 0, 0}, new float[] {90, 0, 0}, 0, 0)
  }
);
/*-------------------------------------------------------------------------*/

/* Kitchen Ring Chandeliers -----------------------------------------------*/
static final float kitchenOffsetX = 30;
static final float kitchenOffsetY = 50;
static final float kitchenOffsetZ = -180;

static final float kitchenRotationX = 0;
static final float kitchenRotationY = 0;
static final float kitchenRotationZ = 0;

// Level Rings Chandelier
RingChandelierConfig levelRingsConfig = new RingChandelierConfig(
  new float[] {0, 0, 0},
  new float[] {0, 0, 0},
  new RingConfig[] {
    new RingConfig("p2r1", new float[] {0,  0, 0}, new float[] {90, 0, 0}, 0, 0),
    new RingConfig("p2r2", new float[] {0, 10, 0}, new float[] {90, 0, 0}, 0, 0),
    new RingConfig("p2r3", new float[] {0, 20, 0}, new float[] {90, 0, 0}, 0, 0)
  }
);

// Rotated Rings Chandelier
RingChandelierConfig rotatedRingsConfig = new RingChandelierConfig(
  new float[] {65, 0, 0},
  new float[] {180, -20, 0},
  new RingConfig[] {
    new RingConfig("p1r1", new float[] {0, 0, 0}, new float[] {220, 0, 0}, 0, 0),
    new RingConfig("p1r2", new float[] {0, 0, 0}, new float[] {90, -35, 0}, 0, 0),
    new RingConfig("p1r3", new float[] {0, 0, 0}, new float[] {90,  35, 0}, 0, 0)
  }
);
/*-------------------------------------------------------------------------*/

/* Skylight Bars ----------------------------------------------------------*/
static final float skylightOffsetX = 0;
static final float skylightOffsetY = 0;
static final float skylightOffsetZ = 0;

static final float skylightRotationX = 0;
static final float skylightRotationY = 0;
static final float skylightRotationZ = 0;



static final BarConfig[] SKYLIGHT_CONFIG = {
  // new BarConfig("510", new float[] {10*0, 70, 0}, new float[]  {0, -90, 0}, 139, 0.7, Bar.Metrics.NumStrips.TWO),
  // new BarConfig("339", new float[] {10*1, 70, 0}, new float[]  {0, -90, 0}, 139, 0.7, Bar.Metrics.NumStrips.TWO),
  // new BarConfig("506", new float[] {10*2, 70, 0}, new float[]  {0, -90, 0}, 139, 0.7, Bar.Metrics.NumStrips.TWO),
  // new BarConfig("507", new float[] {10*3, 70, 0}, new float[]  {0, -90, 0}, 139, 0.7, Bar.Metrics.NumStrips.TWO),
  // new BarConfig("504", new float[] {10*4, 70, 0}, new float[]  {0, -90, 0}, 139, 0.7, Bar.Metrics.NumStrips.TWO),
  // new BarConfig("d8:80:39:9a:d9:14", new float[] {10*5, 70, 0}, new float[]  {0, -90, 0}, 139, 0.7, Bar.Metrics.NumStrips.TWO),
  // new BarConfig("501", new float[] {10*6, 70, 0}, new float[]  {0, -90, 0}, 139, 0.7, Bar.Metrics.NumStrips.TWO),
  // new BarConfig("511", new float[] {10*7, 70, 0}, new float[]  {0, -90, 0}, 139, 0.7, Bar.Metrics.NumStrips.TWO),
  // new BarConfig("509", new float[] {85, 50, 40}, new float[]  {0, 0, 0}, 139, 0.7, Bar.Metrics.NumStrips.TWO),
  //   new BarConfig("510", new float[] {85, 50+10, 40}, new float[]  {0, 0, 0}, 139, 0.7, Bar.Metrics.NumStrips.TWO),
  //       new BarConfig("511", new float[] {85, 50+20, 40}, new float[]  {0, 0, 0}, 139, 0.7, Bar.Metrics.NumStrips.TWO),
  //           new BarConfig("512", new float[] {85, 50+30, 40}, new float[]  {0, 0, 0}, 139, 0.7, Bar.Metrics.NumStrips.TWO),
  //               new BarConfig("513", new float[] {85, 50+40, 40}, new float[]  {0, 0, 0}, 139, 0.7, Bar.Metrics.NumStrips.TWO),

  // new BarConfig("502", new float[] {10*9, 70, 0}, new float[]  {0, -90, 0}, 139, 0.7, Bar.Metrics.NumStrips.TWO),
  // new BarConfig("508", new float[] {10*10,70, 0}, new float[] {0, -90, 0}, 139, 0.7, Bar.Metrics.NumStrips.TWO),
};
/*-------------------------------------------------------------------------*/

/* Wall Bars --------------------------------------------------------------*/
static final float wallBarsOffsetX = 75;
static final float wallBarsOffsetY = 135;
static final float wallBarsOffsetZ = 40;

static final float wallBarsRotationX = 180;
static final float wallBarsRotationY = 180;
static final float wallBarsRotationZ = 90;

static final BarConfig[] WALL_BARS_CONFIG = {
  // new BarConfig("140", new float[] {0, 8*0, 0}, new float[] {0, 0, 0}, 59, 0.5, Bar.Metrics.NumStrips.THREE),
  // new BarConfig("133", new float[] {0, 8*1, 0}, new float[] {0, 0, 0}, 59, 0.5, Bar.Metrics.NumStrips.THREE),
  // new BarConfig("111", new float[] {0, 8*2, 0}, new float[] {0, 0, 0}, 59, 0.5, Bar.Metrics.NumStrips.THREE),
  // new BarConfig("160", new float[] {0, 8*3, 0}, new float[] {0, 0, 0}, 59, 0.5, Bar.Metrics.NumStrips.THREE),
  // new BarConfig("361", new float[] {0, 8*4, 0}, new float[] {0, 0, 0}, 59, 0.5, Bar.Metrics.NumStrips.THREE),
};
/*-------------------------------------------------------------------------*/


static final float cubesOffsetX = 70;
static final float cubesOffsetY = -50;
static final float cubesOffsetZ = 0;

static final float cubesRotationX = 0;
static final float cubesRotationY = 45;
static final float cubesRotationZ = 0;
static float sp = 48;
static float halfsp=24;
 




static final StripConfig[] STRIP_CONFIG = {
          // controller id         x   y   z  xRot   yRot   zRot   num leds      pitch in inches
//new StripConfig("206",            0,  0,  0,    0,     0,     0,        10,                 0.25),

};

static class RingChandelierConfig {
  public final float[] coordinates;
  public final float[] rotations;
  public final RingConfig[] rings;

  public RingChandelierConfig(float[] coordinates, float[] rotations, RingConfig[] rings) {
    this.coordinates = coordinates;
    this.rotations = rotations;
    this.rings = rings;
  }
}

static class RingConfig {
  public final String id;
  public final int numPoints;
  public final float radius;
  public final float[] coordinates;
  public final float[] rotations;

  public RingConfig(String id, float[] coordinates, float[] rotations, int numPoints, float radius) {
    this.id = id;
    this.numPoints = numPoints;
    this.radius = radius;
    this.coordinates = coordinates;
    this.rotations = rotations;
  }
}

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

static class BarConfig {
  final String id;
  final float x;
  final float y;
  final float z;
  final float xRot;
  final float yRot;
  final float zRot;
  final Bar.Metrics metrics;
  final float length;

  BarConfig(String id, float[] coordinates, float[] rotations, int numPoints, float pixelPitch, Bar.Metrics.NumStrips numStrips) {
    this.id = id;
    this.x = coordinates[0];
    this.y = coordinates[1];
    this.z = coordinates[2];
    this.xRot = rotations[0];
    this.yRot = rotations[1];
    this.zRot = rotations[2];
    this.metrics = new Bar.Metrics(numStrips, numPoints, pixelPitch);
    this.length = numPoints * pixelPitch;
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

  globalTransform.push();
  globalTransform.translate(cubesOffsetX, cubesOffsetY, cubesOffsetZ);
  globalTransform.rotateY(cubesRotationY * PI / 180.);
  globalTransform.rotateX(cubesRotationX * PI / 180.);
  globalTransform.rotateZ(cubesRotationZ * PI / 180.);


String[] lines = loadStrings("/Users/Cameron/Desktop/Programming Stuff/Auto Mapping SL Studio/Cube_Mapping_RAC_Version_4/mappingfile.txt");
  float x1 =  float(lines[0]);
  float z1 =  float(lines[1]);
  float x2 =  float(lines[2]);
  float z2 =  float(lines[3]);
  float x3 =  float(lines[4]);
  float z3 =  float(lines[5]);
  float x4 =  float(lines[6]);
  float z4 =  float(lines[7]);
  float x5 =  float(lines[8]);
  float z5 =  float(lines[9]);
  float x6 =  float(lines[10]);
  float z6 =  float(lines[11]);
  float x7 =  float(lines[12]);
  float z7 =  float(lines[13]);
  float x8 =  float(lines[14]);
  float z8 =  float(lines[15]);
  float x9 =  float(lines[16]);
  float z9 =  float(lines[17]);
  float x10 = float(lines[18]);
  float z10 = float(lines[19]);
  float x11 =  float(lines[20]);
  float z11 =  float(lines[21]);
  float x12 =  float(lines[22]);
  float z12 =  float(lines[23]);
  float x13 =  float(lines[24]);
  float z13 =  float(lines[25]);
  float x14 =  float(lines[26]);
  float z14 =  float(lines[27]);
  float x15 =  float(lines[28]);
  float z15 =  float(lines[29]);
  float x16 =  float(lines[30]);
  float z16 =  float(lines[31]);
  float x17 =  float(lines[32]);
  float z17 =  float(lines[33]);
  float x18 =  float(lines[34]);
  float z18 =  float(lines[35]);
  float x19 =  float(lines[36]);
  float z19 =  float(lines[37]);
  float x20 =  float(lines[38]);
  float z20 =  float(lines[39]);
  
  //+48 to cubes flipped -180
final TowerConfig[] TOWER_CONFIG = {
   new TowerConfig(  x1, 0, z1, 0, 0, 0, new String[] {"21", "335", "343"}),
   new TowerConfig(  x2, 0, z2, 0, 0, 0, new String[] {"63", "202", "33"}),
   new TowerConfig(  x3, 0, z3, 0, 0, 0, new String[] {"2", "40", "199"}),
   new TowerConfig(  x4, 0, z4, 0, 0, 0, new String[] {"314", "337", "26",""}),
  // new TowerConfig(  x5+0, 0, z5, 0, 0, 0,   new String[] {"39", "126", "111"}),
  // new TowerConfig(  x6+0, 0, z6, 0, 0, 0,   new String[] {"11", "94", "120"}),
  // new TowerConfig(  x7+0, 0, z7, 0, 0, 0,   new String[] { "171", "138", "131"}),
  // new TowerConfig(  x8+0, 0, z8, 0, 0, 0,   new String[] { "27", "9", "85"}),
  ////new TowerConfig(  x9, 0, z9, 0, 0, 0, new String[] {"", "", ""}),
  //new TowerConfig(  x10,0, z10,0, 0, 0, new String[] {"", "", ""}),
  //new TowerConfig(  x11, 0, z11, 0, 0, 0, new String[] {"", "", ""}),
  //new TowerConfig(  x12, 0, z12, 0, 0, 0, new String[] {"", "", ""}),
  //new TowerConfig(  x13, 0, z13, 0, 0, 0, new String[] {"", "", ""}),
  //new TowerConfig(  x14, 0, z14, 0, 0, 0, new String[] {"", "", ""}),
  //new TowerConfig(  x15, 0, z15, 0, 0, 0, new String[] {"", "", "",""}),
  //new TowerConfig(  x16, 0, z16, 0, 0, 0, new String[] {"", "", "", ""}),
  //new TowerConfig(  x17, 0, z17, 0, 0, 0, new String[] {"", "", ""}),
  //new TowerConfig(  x18, 0, z18, 0, 0, 0, new String[] {"", "", ""}),
  //new TowerConfig(  x19, 0, z19, 0, 0, 0, new String[] {"", "", ""}),
  //new TowerConfig(  x20,0, z20,0, 0, 0, new String[] {"", "", "", "",""}),
   };


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
      Cube cube = new Cube(config.ids[i], x, y, z, xRot-180, yRot, zRot, globalTransform, type);
      cubes.add(cube);
      allCubes.add(cube);
    }
    towers.add(new Tower("", cubes));
  }
  globalTransform.pop();
  /*-----------------------------------------------------------------*/

  /* Strips ----------------------------------------------------------*/
  List<Strip> strips = new ArrayList<Strip>();

  for (StripConfig stripConfig : STRIP_CONFIG) {
    Strip.Metrics metrics = new Strip.Metrics(stripConfig.numPoints, stripConfig.spacing);

    globalTransform.push();
    globalTransform.translate(stripConfig.x, stripConfig.y, stripConfig.z);
    globalTransform.rotateX(stripConfig.yRot * PI / 180.);    
    globalTransform.rotateY(stripConfig.xRot * PI / 180.);
    globalTransform.rotateZ(stripConfig.zRot * PI / 180.);

    strips.add(new Strip(metrics, stripConfig.yRot, globalTransform, true));

    globalTransform.pop();
  }
  /*-----------------------------------------------------------------*/

  /* Bars -----------------------------------------------------------*/
  List<Bar> allBars = new ArrayList<Bar>();

  // Skylight
  Skylight skylight;
  globalTransform.push();
  globalTransform.translate(skylightOffsetX, skylightOffsetY, skylightOffsetZ);
  globalTransform.rotateX(skylightRotationX * PI / 180.);
  globalTransform.rotateY(skylightRotationY * PI / 180.);
  globalTransform.rotateZ(skylightRotationZ * PI / 180.);
  List<Bar> skylightBars = new ArrayList<Bar>();
  for (BarConfig barConfig : SKYLIGHT_CONFIG) {
    Bar bar = new Bar(barConfig, globalTransform);
    allBars.add(bar);
    skylightBars.add(bar);
  }
  skylight = new Skylight(skylightBars);
  globalTransform.pop();

  // Wall Bars
  WallBars wallBars;
  globalTransform.push();
  globalTransform.translate(wallBarsOffsetX, wallBarsOffsetY, wallBarsOffsetZ);
  globalTransform.rotateX(wallBarsRotationX * PI / 180.);
  globalTransform.rotateY(wallBarsRotationY * PI / 180.);
  globalTransform.rotateZ(wallBarsRotationZ * PI / 180.);
  List<Bar> wallBarsBars = new ArrayList<Bar>();
  for (BarConfig barConfig : WALL_BARS_CONFIG) {
    Bar bar = new Bar(barConfig, globalTransform);
    allBars.add(bar);
    wallBarsBars.add(bar);
  }
  wallBars = new WallBars(wallBarsBars);
  globalTransform.pop();
  /*-----------------------------------------------------------------*/

  /* Kitchen Rings --------------------------------------------------*/
  List<Ring> allRings = new ArrayList<Ring>();

  globalTransform.push();
  globalTransform.translate(kitchenOffsetX, kitchenOffsetY, kitchenOffsetZ);
  globalTransform.rotateX(kitchenRotationX * PI / 180.);
  globalTransform.rotateY(kitchenRotationY * PI / 180.);
  globalTransform.rotateZ(kitchenRotationZ * PI / 180.);

  // Level Ring Chandelier
  LevelRings levelRings = new LevelRings(levelRingsConfig, globalTransform);
  for (Ring r : levelRings.rings) {
    allRings.add(r);
  }

  // Rotated Rings Chandelier
  RotatedRings rotatedRings = new RotatedRings(rotatedRingsConfig, globalTransform);
  for (Ring r : rotatedRings.rings) {
   allRings.add(r);
  }

  //globalTransform.pop();
  /*-----------------------------------------------------------------*/

  /* Upstairs Rings -------------------------------------------------*/
  globalTransform.push();
  globalTransform.translate(upstairsOffsetX, upstairsOffsetY, upstairsOffsetZ);
  globalTransform.rotateX(upstairsRotationX * PI / 180.);
  globalTransform.rotateY(upstairsRotationY * PI / 180.);
  globalTransform.rotateZ(upstairsRotationZ * PI / 180.);

  // Upstairs Ring Chandelier
  UpstairsRings upstairsRings = new UpstairsRings(upstairsRingsConfig, globalTransform);
  for (Ring r : upstairsRings.rings) {
    allRings.add(r);
  }

  globalTransform.pop();
  /*-----------------------------------------------------------------*/

  for (Bar b : allBars) {
    for (Strip s : b.strips) {
      strips.add(s);
    }
  }

  Cube[] allCubesArr = new Cube[allCubes.size()];
  for (int i = 0; i < allCubesArr.length; i++) {
    allCubesArr[i] = allCubes.get(i);
  }

  return new SLModel(towers, allCubesArr, strips, allBars, skylight, wallBars, levelRings, rotatedRings, upstairsRings, allRings);
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
        if (isFixtureMapped(c.controllerId)) {
          fixturesMappedButNotOnNetwork.remove(c.controllerId);
          fixturesMappedAndOnTheNetwork.add(c.controllerId);
        } else {
          fixturesOnNetworkButNotMapped.add(c.controllerId);
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