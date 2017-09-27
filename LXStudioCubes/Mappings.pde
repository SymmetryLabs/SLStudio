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

static final TowerConfig[] TOWER_CONFIG = {};

static final StripConfig[] VIP_LOUGNE_STRIP_CONFIG = {
  // strip id, {x, y, z}, {xRot, yRot, zRot}, num leds, length

  // horizontals - bottom
  new StripConfig("vip-lounge-strip1", new float[] {0, 0, 0}, new float[] {0, 0, 0}, 43, 55),
  new StripConfig("vip-lounge-strip2", new float[] {0, 0, 0}, new float[] {0, 0, 0}, 43), // need count

  // horizontals - middle bottom
  new StripConfig("vip-lounge-strip3", new float[] {0, 0, 0}, new float[] {0, 0, 0}, 43), // need count
  new StripConfig("vip-lounge-strip4", new float[] {0, 0, 0}, new float[] {0, 0, 0}, 43), // need count

  // horizontals - middle top
  new StripConfig("vip-lounge-strip5", new float[] {0, 0, 0}, new float[] {0, 0, 0}, 43), // need count
  new StripConfig("vip-lounge-strip6", new float[] {0, 0, 0}, new float[] {0, 0, 0}, 43), // need count

  // horizontals - top
  new StripConfig("vip-lounge-strip7", new float[] {0, 0, 0}, new float[] {0, 0, 0}, 43), // need count
  new StripConfig("vip-lounge-strip8", new float[] {0, 0, 0}, new float[] {0, 0, 0}, 43), // need count

  // verticals - left
  new StripConfig("vip-lounge-strip9",  new float[] {0, 0, 0}, new float[] {0, 0, 90}, 43), // need count
  new StripConfig("vip-lounge-strip10", new float[] {0, 0, 0}, new float[] {0, 0, 90}, 43), // need count

  // verticals - right 
  new StripConfig("vip-lounge-strip11", new float[] {0, 0, 0}, new float[] {0, 0, 90}, 43), // need count
  new StripConfig("vip-lounge-strip12", new float[] {0, 0, 0}, new float[] {0,  0, 0}, 43), // need count (little guy)
  new StripConfig("vip-lounge-strip13", new float[] {0, 0, 0}, new float[] {0, 0, 90}, 43), // need count

  // bottom angle
  new StripConfig("vip-lounge-strip14", new float[] {0, 0, 0}, new float[] {0, 0, 45}, 43), // need count
  new StripConfig("vip-lounge-strip15", new float[] {0, 0, 0}, new float[] {0, 0, 45}, 43), // need count

  // top angle
  new StripConfig("vip-lounge-strip16", new float[] {0, 0, 0}, new float[] {0, 0, 135}, 43), // need count
  new StripConfig("vip-lounge-strip17", new float[] {0, 0, 0}, new float[] {0, 0, 135}, 43), // need count

};

static final StringConfig[] VJ_BOOTH_STRIP_CONFIG = {

}

static final StringConfig[] LONG_SKINNY_RUN_STRIP_CONFIG = {

};

static final StringConfig[] COLUMNS_STRIP_CONFIG = {
  // ceiling

  // first square
  // second square
  // third square
  // fourth square
};

static final StringConfig[] TEST_STRIP_CONFIG = {
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

  StripConfig(String id, float[] coordinates, float[] rotations, float length, int numPoints) {
    this(id, coordinates[0], coordinates[1], coordinates[2], rotations[0], rotations[1], rotations[2], length, numPoints);
  }

  StripConfig(String id, float x, float y, float z, float xRot, float yRot, float zRot, , float length, int numPoints) {
    this.id = id;
    this.numPoints = numPoints;
    this.length = length;
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

  for (StripConfig stripConfig : VIP_LOUGNE_STRIP_CONFIG) {
    Strip.Metrics metrics = new Strip.Metrics(stripConfig.numPoints, stripConfig.numPoints/stripConfig.length);

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
