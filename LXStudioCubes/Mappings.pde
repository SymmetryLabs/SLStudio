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

static final float PIXEL_PITCH = 0.65;
static final int LONG_NUM_LEDS = 139;
static final int SHORT_NUM_LEDS = 116;

static final float LONG_BAR_LENGTH = LONG_NUM_LEDS * PIXEL_PITCH;
static final float SHORT_BAR_LENGTH = SHORT_NUM_LEDS * PIXEL_PITCH;


/**
 * Global
 */
static final float globalOffsetX = 0;
static final float globalOffsetY = 0;
static final float globalOffsetZ = 0;

static final float globalRotationX = 0;
static final float globalRotationY = 0;
static final float globalRotationZ = 0;

// X (positive to the right), (probably positive/counter-clockwise)
// Y (positive upward), (rotates positive/counter-clockwise)
// Z (positive goes backward), (rotates positive/counter-clockwise)

/**
 * Left Face
 */
static final float leftFaceOffsetX = 0;
static final float leftFaceOffsetY = 0;
static final float leftFaceOffsetZ = 0;

static final float leftFaceRotationX = 0;
static final float leftFaceRotationY = -90;
static final float leftFaceRotationZ = 0;

static final BarConfig[] LEFT_FACE_CONFIG = {
  new BarConfig("335",               0,                 0, 0,       0, 0,  0,        LONG_NUM_LEDS, PIXEL_PITCH), // bottom
  new BarConfig("0", LONG_BAR_LENGTH/4, LONG_BAR_LENGTH/2, 0,       0, 0,  0,        LONG_NUM_LEDS, PIXEL_PITCH), // middle
  new BarConfig("0",                 0,   LONG_BAR_LENGTH, 0,       0, 0,  0,        LONG_NUM_LEDS, PIXEL_PITCH), // top 
  new BarConfig("0",                 0,                 0, 0,       0, 0, 90,        LONG_NUM_LEDS, PIXEL_PITCH), // vertical
};

/**
 * Right Face
 */
static final float rightFaceOffsetX = LONG_BAR_LENGTH;
static final float rightFaceOffsetY = 0;
static final float rightFaceOffsetZ = 0;

static final float rightFaceRotationX = 0;
static final float rightFaceRotationY = -90;
static final float rightFaceRotationZ = 0;

static final BarConfig[] RIGHT_FACE_CONFIG = {
  new BarConfig("0",    0,                 0, 0,       0, 0,  0,         LONG_NUM_LEDS, PIXEL_PITCH), // bottom
  new BarConfig("0",    0, LONG_BAR_LENGTH/2, 0,       0, 0,  0,        SHORT_NUM_LEDS, PIXEL_PITCH), // middle
  new BarConfig("0",    0,   LONG_BAR_LENGTH, 0,       0, 0,  0,         LONG_NUM_LEDS, PIXEL_PITCH), // top 
  new BarConfig("0",    0,                 0, 0,       0, 0, 90,         LONG_NUM_LEDS, PIXEL_PITCH), // vertical
};

/**
 * Square
 */
static final float squareOffsetX = 0;
static final float squareOffsetY = 8;
static final float squareOffsetZ = LONG_BAR_LENGTH;

static final float squareRotationX = 0;
static final float squareRotationY = 0;
static final float squareRotationZ = 0;

static final BarConfig[] SQUARE_CONFIG = {
  new BarConfig("0",                  0,                0, 0,       0, 0,  0,        LONG_NUM_LEDS, PIXEL_PITCH), // bottom
  new BarConfig("0",                  0, SHORT_BAR_LENGTH, 0,       0, 0,  0,        LONG_NUM_LEDS, PIXEL_PITCH), // top
  new BarConfig("0",                  0,                0, 0,       0, 0, 90,       SHORT_NUM_LEDS, PIXEL_PITCH), // left
  new BarConfig("0",    LONG_BAR_LENGTH,                0, 0,       0, 0, 90,       SHORT_NUM_LEDS, PIXEL_PITCH), // right
};

/**
 * Back Vertical
 */
static final float backVerticalOffsetX = 0;
static final float backVerticalOffsetY = 0;
static final float backVerticalOffsetZ = LONG_BAR_LENGTH*3;

static final float backVerticalRotationX = 0;
static final float backVerticalRotationY = 0;
static final float backVerticalRotationZ = 0;

static final BarConfig[] BACK_VERTICAL_CONFIG = {
  new BarConfig("0",               0, 0, 0,       0, 0, 90,        LONG_NUM_LEDS, PIXEL_PITCH), // left
  new BarConfig("0",              15, 0, 0,       0, 0, 90,        LONG_NUM_LEDS, PIXEL_PITCH), // middle
  new BarConfig("0", LONG_BAR_LENGTH, 0, 0,       0, 0, 90,        LONG_NUM_LEDS, PIXEL_PITCH), // right
};

/**
 * Right Vertical
 */
static final float rightVerticalOffsetX = LONG_BAR_LENGTH*2.3;
static final float rightVerticalOffsetY = 0;
static final float rightVerticalOffsetZ = 0;

static final float rightVerticalRotationX = 0;
static final float rightVerticalRotationY = 0;
static final float rightVerticalRotationZ = 0;

static final BarConfig[] RIGHT_VERTICAL_CONFIG = {
  new BarConfig("0",   0, 0,  0,       0, 0, 90,        LONG_NUM_LEDS, PIXEL_PITCH), // back-left
  new BarConfig("0",  10, 0,  0,       0, 0, 90,        LONG_NUM_LEDS, PIXEL_PITCH), // back-right
  new BarConfig("0",   0, 0, 10,       0, 0, 90,        LONG_NUM_LEDS, PIXEL_PITCH), // front-left
  new BarConfig("0",  10, 0, 10,       0, 0, 90,        LONG_NUM_LEDS, PIXEL_PITCH), // front-right
};

/**
 * Long Horizontal
 */
static final float longHorizontalOffsetX = 0;
static final float longHorizontalOffsetY = 0;
static final float longHorizontalOffsetZ = 0;

static final float longHorizontalRotationX = 0;
static final float longHorizontalRotationY = 0;
static final float longHorizontalRotationZ = 0;

static final BarConfig[] LONG_HORIZONTAL_CONFIG = {
  // left side, bottom
  new BarConfig("0",  -LONG_BAR_LENGTH*0.4, LONG_BAR_LENGTH*0.5,               0,       0, -90, 0,        LONG_NUM_LEDS, PIXEL_PITCH),
  new BarConfig("0",  -LONG_BAR_LENGTH*0.4, LONG_BAR_LENGTH*0.5, LONG_BAR_LENGTH,       0, -90, 0,        LONG_NUM_LEDS, PIXEL_PITCH),

  // left side, middle
  new BarConfig("0",  -LONG_BAR_LENGTH*0.4, LONG_BAR_LENGTH*0.5+20,               0,       0, -90, 0,        LONG_NUM_LEDS, PIXEL_PITCH),
  new BarConfig("0",  -LONG_BAR_LENGTH*0.4, LONG_BAR_LENGTH*0.5+20, LONG_BAR_LENGTH,       0, -90, 0,        LONG_NUM_LEDS, PIXEL_PITCH),

  // left side, top
  new BarConfig("0",  -LONG_BAR_LENGTH*0.4, LONG_BAR_LENGTH*0.5+30,               0,       0, -90, 0,        LONG_NUM_LEDS, PIXEL_PITCH),
  new BarConfig("0",  -LONG_BAR_LENGTH*0.4, LONG_BAR_LENGTH*0.5+30, LONG_BAR_LENGTH,       0, -90, 0,        LONG_NUM_LEDS, PIXEL_PITCH),

  // top, left
  new BarConfig("0",  8, LONG_BAR_LENGTH+20,               0,       0, -90, 0,        LONG_NUM_LEDS, PIXEL_PITCH),
  new BarConfig("0",  8, LONG_BAR_LENGTH+20, LONG_BAR_LENGTH,       0, -90, 0,        LONG_NUM_LEDS, PIXEL_PITCH),

  // top, middle
  new BarConfig("0",  LONG_BAR_LENGTH*0.7, LONG_BAR_LENGTH+12,               0,       0, -90, 0,        LONG_NUM_LEDS, PIXEL_PITCH),
  new BarConfig("0",  LONG_BAR_LENGTH*0.7, LONG_BAR_LENGTH+12, LONG_BAR_LENGTH,       0, -90, 0,        LONG_NUM_LEDS, PIXEL_PITCH),

  // top, right
  new BarConfig("0",  LONG_BAR_LENGTH*0.9, LONG_BAR_LENGTH+12,               0,       0, -90, 0,        LONG_NUM_LEDS, PIXEL_PITCH),
  new BarConfig("0",  LONG_BAR_LENGTH*0.9, LONG_BAR_LENGTH+12, LONG_BAR_LENGTH,       0, -90, 0,        LONG_NUM_LEDS, PIXEL_PITCH),

  // right, bottom
  new BarConfig("0",  LONG_BAR_LENGTH*1.35, LONG_BAR_LENGTH*0.3,               0,       0, -90, 0,        LONG_NUM_LEDS, PIXEL_PITCH),
  new BarConfig("0",  LONG_BAR_LENGTH*1.35, LONG_BAR_LENGTH*0.3, LONG_BAR_LENGTH,       0, -90, 0,        LONG_NUM_LEDS, PIXEL_PITCH),

  // right, middle/bottom
  new BarConfig("0",  LONG_BAR_LENGTH*1.35, LONG_BAR_LENGTH*0.5,               0,       0, -90, 0,        LONG_NUM_LEDS, PIXEL_PITCH),
  new BarConfig("0",  LONG_BAR_LENGTH*1.35, LONG_BAR_LENGTH*0.5, LONG_BAR_LENGTH,       0, -90, 0,        LONG_NUM_LEDS, PIXEL_PITCH),

  // right, middle/top
  new BarConfig("0",  LONG_BAR_LENGTH*1.35, LONG_BAR_LENGTH*0.6,               0,       0, -90, 0,        LONG_NUM_LEDS, PIXEL_PITCH),
  new BarConfig("0",  LONG_BAR_LENGTH*1.35, LONG_BAR_LENGTH*0.6, LONG_BAR_LENGTH,       0, -90, 0,        LONG_NUM_LEDS, PIXEL_PITCH),

  // right, top
  new BarConfig("0",  LONG_BAR_LENGTH*1.35, LONG_BAR_LENGTH*0.8,               0,       0, -90, 0,        LONG_NUM_LEDS, PIXEL_PITCH),
  new BarConfig("0",  LONG_BAR_LENGTH*1.35, LONG_BAR_LENGTH*0.8, LONG_BAR_LENGTH,       0, -90, 0,        LONG_NUM_LEDS, PIXEL_PITCH),

  // right/right, middle/bottom
  new BarConfig("0",  LONG_BAR_LENGTH*1.65, LONG_BAR_LENGTH*0.5,               0,       0, -90, 0,        LONG_NUM_LEDS, PIXEL_PITCH),
  new BarConfig("0",  LONG_BAR_LENGTH*1.65, LONG_BAR_LENGTH*0.5, LONG_BAR_LENGTH,       0, -90, 0,        LONG_NUM_LEDS, PIXEL_PITCH),
};

static class BarConfig {
  String id;
  String secondId;
  int numPoints;
  float spacing;
  float x;
  float y;
  float z;
  float xRot;
  float yRot;
  float zRot;
  boolean flip;

  BarConfig(String id, float x, float y, float z, float xRot, float yRot, float zRot, int numPoints, float spacing) {
    this(id, null, x, y, z, xRot, yRot, zRot, numPoints, spacing, false);
  }

  BarConfig(String id, float x, float y, float z, float xRot, float yRot, float zRot, int numPoints, float spacing, boolean flip) {
    this(id, null, x, y, z, xRot, yRot, zRot, numPoints, spacing, flip);
  }

  BarConfig(String id, String secondId, float x, float y, float z, float xRot, float yRot, float zRot, int numPoints, float spacing, boolean flip) {
    this.id = id;
    this.secondId = secondId;
    this.numPoints = numPoints;
    this.spacing = spacing;
    this.x = x;
    this.y = y;
    this.z = z;
    this.xRot = xRot;
    this.yRot = yRot;
    this.zRot = zRot;
    this.flip = flip;
  }
}

static final TowerConfig[] TOWER_CONFIG = {};

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
      yValues[i] = y + i * (24 + 2.5);
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
      Cube cube = new Cube(config.ids[i], x, y, z, xRot, yRot-90, zRot, globalTransform, type);
      cubes.add(cube);
      allCubes.add(cube);
    }
    towers.add(new Tower("", cubes));
  }
  /*-----------------------------------------------------------------*/

  /* Bars -----------------------------------------------------------*/
  List<Bar> bars = new ArrayList<Bar>();



  /*-----------------------------------------------------------------*
   * Left Face
   */
  globalTransform.push();
  globalTransform.translate(leftFaceOffsetX, leftFaceOffsetY, leftFaceOffsetZ);
  globalTransform.rotateY(leftFaceRotationY * PI / 180.);
  globalTransform.rotateX(leftFaceRotationX * PI / 180.);
  globalTransform.rotateZ(leftFaceRotationZ * PI / 180.);

  for (BarConfig barConfig : LEFT_FACE_CONFIG) {
    Strip.Metrics metrics = new Strip.Metrics(barConfig.numPoints, barConfig.spacing);

    globalTransform.push();
    globalTransform.translate(barConfig.x, barConfig.y, barConfig.z);
    globalTransform.rotateX(barConfig.xRot * PI / 180.);
    globalTransform.rotateY(barConfig.yRot * PI / 180.);
    globalTransform.rotateZ(barConfig.zRot * PI / 180.);

    bars.add(new Bar(barConfig.id, barConfig.secondId, metrics, globalTransform, barConfig.flip));

    globalTransform.pop();
  }
  globalTransform.pop();
  /*-----------------------------------------------------------------*/

  /*-----------------------------------------------------------------*
   * Right Face
   */
  globalTransform.push();
  globalTransform.translate(rightFaceOffsetX, rightFaceOffsetY, rightFaceOffsetZ);
  globalTransform.rotateY(rightFaceRotationY * PI / 180.);
  globalTransform.rotateX(rightFaceRotationX * PI / 180.);
  globalTransform.rotateZ(rightFaceRotationZ * PI / 180.);

  for (BarConfig barConfig : RIGHT_FACE_CONFIG) {
    Strip.Metrics metrics = new Strip.Metrics(barConfig.numPoints, barConfig.spacing);

    globalTransform.push();
    globalTransform.translate(barConfig.x, barConfig.y, barConfig.z);
    globalTransform.rotateX(barConfig.xRot * PI / 180.);
    globalTransform.rotateY(barConfig.yRot * PI / 180.);
    globalTransform.rotateZ(barConfig.zRot * PI / 180.);

    bars.add(new Bar(barConfig.id, barConfig.secondId, metrics, globalTransform, barConfig.flip));

    globalTransform.pop();
  }
  globalTransform.pop();
  /*-----------------------------------------------------------------*/

  /*-----------------------------------------------------------------*
   * Square
   */
  globalTransform.push();
  globalTransform.translate(squareOffsetX, squareOffsetY, squareOffsetZ);
  globalTransform.rotateY(squareRotationY * PI / 180.);
  globalTransform.rotateX(squareRotationX * PI / 180.);
  globalTransform.rotateZ(squareRotationZ * PI / 180.);

  for (BarConfig barConfig : SQUARE_CONFIG) {
    Strip.Metrics metrics = new Strip.Metrics(barConfig.numPoints, barConfig.spacing);

    globalTransform.push();
    globalTransform.translate(barConfig.x, barConfig.y, barConfig.z);
    globalTransform.rotateX(barConfig.xRot * PI / 180.);
    globalTransform.rotateY(barConfig.yRot * PI / 180.);
    globalTransform.rotateZ(barConfig.zRot * PI / 180.);

    bars.add(new Bar(barConfig.id, barConfig.secondId, metrics, globalTransform, barConfig.flip));

    globalTransform.pop();
  }
  globalTransform.pop();
  /*-----------------------------------------------------------------*/

  /*-----------------------------------------------------------------*
   * Back Vertical
   */
  globalTransform.push();
  globalTransform.translate(backVerticalOffsetX, backVerticalOffsetY, backVerticalOffsetZ);
  globalTransform.rotateY(backVerticalRotationY * PI / 180.);
  globalTransform.rotateX(backVerticalRotationX * PI / 180.);
  globalTransform.rotateZ(backVerticalRotationZ * PI / 180.);

  for (BarConfig barConfig : BACK_VERTICAL_CONFIG) {
    Strip.Metrics metrics = new Strip.Metrics(barConfig.numPoints, barConfig.spacing);

    globalTransform.push();
    globalTransform.translate(barConfig.x, barConfig.y, barConfig.z);
    globalTransform.rotateX(barConfig.xRot * PI / 180.);
    globalTransform.rotateY(barConfig.yRot * PI / 180.);
    globalTransform.rotateZ(barConfig.zRot * PI / 180.);

    bars.add(new Bar(barConfig.id, barConfig.secondId, metrics, globalTransform, barConfig.flip));

    globalTransform.pop();
  }
  globalTransform.pop();
  /*-----------------------------------------------------------------*/

  /*-----------------------------------------------------------------*
   * Right Vertical
   */
  globalTransform.push();
  globalTransform.translate(rightVerticalOffsetX, rightVerticalOffsetY, rightVerticalOffsetZ);
  globalTransform.rotateY(rightVerticalRotationY * PI / 180.);
  globalTransform.rotateX(rightVerticalRotationX * PI / 180.);
  globalTransform.rotateZ(rightVerticalRotationZ * PI / 180.);

  for (BarConfig barConfig : RIGHT_VERTICAL_CONFIG) {
    Strip.Metrics metrics = new Strip.Metrics(barConfig.numPoints, barConfig.spacing);

    globalTransform.push();
    globalTransform.translate(barConfig.x, barConfig.y, barConfig.z);
    globalTransform.rotateX(barConfig.xRot * PI / 180.);
    globalTransform.rotateY(barConfig.yRot * PI / 180.);
    globalTransform.rotateZ(barConfig.zRot * PI / 180.);

    bars.add(new Bar(barConfig.id, barConfig.secondId, metrics, globalTransform, barConfig.flip));

    globalTransform.pop();
  }
  globalTransform.pop();
  /*-----------------------------------------------------------------*/

  /*-----------------------------------------------------------------*
   * Long Horiontal
   */
  globalTransform.push();
  globalTransform.translate(longHorizontalOffsetX, longHorizontalOffsetY, longHorizontalOffsetZ);
  globalTransform.rotateY(longHorizontalRotationY * PI / 180.);
  globalTransform.rotateX(longHorizontalRotationX * PI / 180.);
  globalTransform.rotateZ(longHorizontalRotationZ * PI / 180.);

  for (BarConfig barConfig : LONG_HORIZONTAL_CONFIG) {
    Strip.Metrics metrics = new Strip.Metrics(barConfig.numPoints, barConfig.spacing);

    globalTransform.push();
    globalTransform.translate(barConfig.x, barConfig.y, barConfig.z);
    globalTransform.rotateX(barConfig.xRot * PI / 180.);
    globalTransform.rotateY(barConfig.yRot * PI / 180.);
    globalTransform.rotateZ(barConfig.zRot * PI / 180.);

    bars.add(new Bar(barConfig.id, barConfig.secondId, metrics, globalTransform, barConfig.flip));

    globalTransform.pop();
  }
  globalTransform.pop();
  /*-----------------------------------------------------------------*/

  Cube[] allCubesArr = new Cube[allCubes.size()];
  for (int i = 0; i < allCubesArr.length; i++) {
    allCubesArr[i] = allCubes.get(i);
  }

  return new SLModel(towers, allCubesArr, bars);
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
