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

static final int BAR_NUM_POINTS = 139;
static final float BAR_PIXEL_PITCH = 0.656168;
static final float BAR_SPACING_X = 4*12;



// static final BulbConfig[] BULB_CONFIG = {
//     // new BulbConfig("lifx-1", -50, 50, -30),
//     // new BulbConfig("lifx-2", 0, 50, 0),
//     // new BulbConfig("lifx-3", -65, 20, -100),
//     // new BulbConfig("lifx-4", 0, 0, 0),
//     // new BulbConfig("lifx-5", 0, 0, 0),
// };

static final BarConfig[] BAR_CONFIG = {

  // row 7
  new BarConfig("140", new float[] {180+BAR_SPACING_X*0, 70, 0}, new float[] {0, 0, -90}, BAR_NUM_POINTS, BAR_PIXEL_PITCH),
  new BarConfig("347", new float[] {180+BAR_SPACING_X*1, 70, 0}, new float[] {0, 0, -90}, BAR_NUM_POINTS, BAR_PIXEL_PITCH),
  new BarConfig("501",   new float[] {180+BAR_SPACING_X*2, 70, 0}, new float[] {0, 0, -90}, BAR_NUM_POINTS, BAR_PIXEL_PITCH),
  new BarConfig("413", new float[] {180+BAR_SPACING_X*3, 70, 0}, new float[] {0, 0, -90}, BAR_NUM_POINTS, BAR_PIXEL_PITCH),
  new BarConfig("158", new float[] {180+BAR_SPACING_X*4, 70, 0}, new float[] {0, 0, -90}, BAR_NUM_POINTS, BAR_PIXEL_PITCH),
  new BarConfig("95",  new float[] {180+BAR_SPACING_X*5, 70, 0}, new float[] {0, 0, -90}, BAR_NUM_POINTS, BAR_PIXEL_PITCH),
  new BarConfig("74",  new float[] {180+BAR_SPACING_X*6, 70, 0}, new float[] {0, 0, -90}, BAR_NUM_POINTS, BAR_PIXEL_PITCH),


  // row 6
  new BarConfig("210", new float[] {67+BAR_SPACING_X*0, 117, 0}, new float[] {0, 0, -90}, BAR_NUM_POINTS, BAR_PIXEL_PITCH),
  new BarConfig("141", new float[] {67+BAR_SPACING_X*1, 117, 0}, new float[] {0, 0, -90}, BAR_NUM_POINTS, BAR_PIXEL_PITCH),
  new BarConfig("180", new float[] {67+BAR_SPACING_X*2, 117, 0}, new float[] {0, 0, -90}, BAR_NUM_POINTS, BAR_PIXEL_PITCH),
  new BarConfig("159", new float[] {67+BAR_SPACING_X*3, 117, 0}, new float[] {0, 0, -90}, BAR_NUM_POINTS, BAR_PIXEL_PITCH),
  new BarConfig("503",   new float[] {67+BAR_SPACING_X*4, 117, 0}, new float[] {0, 0, -90}, BAR_NUM_POINTS, BAR_PIXEL_PITCH),
  new BarConfig("502",   new float[] {67+BAR_SPACING_X*5, 117, 0}, new float[] {0, 0, -90}, BAR_NUM_POINTS, BAR_PIXEL_PITCH),
  new BarConfig("506",   new float[] {67+BAR_SPACING_X*6, 117, 0}, new float[] {0, 0, -90}, BAR_NUM_POINTS, BAR_PIXEL_PITCH),
  new BarConfig("107", new float[] {67+BAR_SPACING_X*6, 117, 0}, new float[] {0, 0, -90}, BAR_NUM_POINTS, BAR_PIXEL_PITCH),

  // row 5
  new BarConfig("176", new float[] {BAR_SPACING_X*0, 64, 0}, new float[] {0, 0, 90}, BAR_NUM_POINTS, BAR_PIXEL_PITCH),
  new BarConfig("189", new float[] {BAR_SPACING_X*1, 64, 0}, new float[] {0, 0, 90}, BAR_NUM_POINTS, BAR_PIXEL_PITCH),
  // gap
  new BarConfig("206", new float[] {500+BAR_SPACING_X*0, 64, 0}, new float[] {0, 0, 90}, BAR_NUM_POINTS, BAR_PIXEL_PITCH),
  new BarConfig("362", new float[] {500+BAR_SPACING_X*1, 64, 0}, new float[] {0, 0, 90}, BAR_NUM_POINTS, BAR_PIXEL_PITCH),

  // row 4 
  new BarConfig("402", new float[] {92+BAR_SPACING_X*0, 108, 0}, new float[] {0, 0, 90}, BAR_NUM_POINTS, BAR_PIXEL_PITCH),
  new BarConfig("411", new float[] {92+BAR_SPACING_X*1, 108, 0}, new float[] {0, 0, 90}, BAR_NUM_POINTS, BAR_PIXEL_PITCH),
  new BarConfig("350", new float[] {92+BAR_SPACING_X*2, 108, 0}, new float[] {0, 0, 90}, BAR_NUM_POINTS, BAR_PIXEL_PITCH),
  new BarConfig("505", new float[] {92+BAR_SPACING_X*3, 108, 0}, new float[] {0, 0, 90}, BAR_NUM_POINTS, BAR_PIXEL_PITCH),
  new BarConfig("389", new float[] {92+BAR_SPACING_X*4, 108, 0}, new float[] {0, 0, 90}, BAR_NUM_POINTS, BAR_PIXEL_PITCH),
  new BarConfig("153", new float[] {92+BAR_SPACING_X*5, 108, 0}, new float[] {0, 0, 90}, BAR_NUM_POINTS, BAR_PIXEL_PITCH),
  new BarConfig("163", new float[] {92+BAR_SPACING_X*6, 108, 0}, new float[] {0, 0, 90}, BAR_NUM_POINTS, BAR_PIXEL_PITCH),

  // row 3 
  new BarConfig("375", new float[] {112+BAR_SPACING_X*0, 200+90, 0}, new float[] {0, 0, -90}, BAR_NUM_POINTS, BAR_PIXEL_PITCH),
  new BarConfig("138", new float[] {112+BAR_SPACING_X*1, 200+90, 0}, new float[] {0, 0, -90}, BAR_NUM_POINTS, BAR_PIXEL_PITCH),
  new BarConfig("507",   new float[] {112+BAR_SPACING_X*2, 200+90, 0}, new float[] {0, 0, -90}, BAR_NUM_POINTS, BAR_PIXEL_PITCH),
  new BarConfig("405", new float[] {112+BAR_SPACING_X*3, 200+90, 0}, new float[] {0, 0, -90}, BAR_NUM_POINTS, BAR_PIXEL_PITCH),
  new BarConfig("147", new float[] {112+BAR_SPACING_X*4, 200+90, 0}, new float[] {0, 0, -90}, BAR_NUM_POINTS, BAR_PIXEL_PITCH),
  new BarConfig("105", new float[] {112+BAR_SPACING_X*5, 200+90, 0}, new float[] {0, 0, -90}, BAR_NUM_POINTS, BAR_PIXEL_PITCH),
  new BarConfig("183", new float[] {112+BAR_SPACING_X*6, 200+90, 0}, new float[] {0, 0, -90}, BAR_NUM_POINTS, BAR_PIXEL_PITCH),

  // row 2
  new BarConfig("55",  new float[] {5+BAR_SPACING_X*0,  265, 0}, new float[] {0, 0, 90}, BAR_NUM_POINTS, BAR_PIXEL_PITCH),
  new BarConfig("321", new float[] {5+BAR_SPACING_X*1,  265, 0}, new float[] {0, 0, 90}, BAR_NUM_POINTS, BAR_PIXEL_PITCH),
  new BarConfig("348", new float[] {5+BAR_SPACING_X*2,  265, 0}, new float[] {0, 0, 90}, BAR_NUM_POINTS, BAR_PIXEL_PITCH),
  new BarConfig("145", new float[] {5+BAR_SPACING_X*3,  265, 0}, new float[] {0, 0, 90}, BAR_NUM_POINTS, BAR_PIXEL_PITCH),
  new BarConfig("111", new float[] {5+BAR_SPACING_X*4,  265, 0}, new float[] {0, 0, 90}, BAR_NUM_POINTS, BAR_PIXEL_PITCH),
  new BarConfig("14",  new float[] {5+BAR_SPACING_X*5,  265, 0}, new float[] {0, 0, 90}, BAR_NUM_POINTS, BAR_PIXEL_PITCH),
  new BarConfig("35",  new float[] {5+BAR_SPACING_X*6,  265, 0}, new float[] {0, 0, 90}, BAR_NUM_POINTS, BAR_PIXEL_PITCH),
  new BarConfig("154", new float[] {5+BAR_SPACING_X*7,  265, 0}, new float[] {0, 0, 90}, BAR_NUM_POINTS, BAR_PIXEL_PITCH),
  new BarConfig("392", new float[] {5+BAR_SPACING_X*8,  265, 0}, new float[] {0, 0, 90}, BAR_NUM_POINTS, BAR_PIXEL_PITCH),
  new BarConfig("332", new float[] {5+BAR_SPACING_X*9,  265, 0}, new float[] {0, 0, 90}, BAR_NUM_POINTS, BAR_PIXEL_PITCH),
  new BarConfig("73",  new float[] {5+BAR_SPACING_X*10, 265, 0}, new float[] {0, 0, 90}, BAR_NUM_POINTS, BAR_PIXEL_PITCH),

  // row 1
  new BarConfig("500",   new float[] {108+BAR_SPACING_X*0, 315, 0}, new float[] {0, 0, 90}, BAR_NUM_POINTS, BAR_PIXEL_PITCH),
  new BarConfig("170", new float[] {108+BAR_SPACING_X*1, 315, 0}, new float[] {0, 0, 90}, BAR_NUM_POINTS, BAR_PIXEL_PITCH),
  new BarConfig("212", new float[] {108+BAR_SPACING_X*2, 315, 0}, new float[] {0, 0, 90}, BAR_NUM_POINTS, BAR_PIXEL_PITCH),
  new BarConfig("184", new float[] {108+BAR_SPACING_X*3, 315, 0}, new float[] {0, 0, 90}, BAR_NUM_POINTS, BAR_PIXEL_PITCH),
  new BarConfig("133", new float[] {108+BAR_SPACING_X*4, 315, 0}, new float[] {0, 0, 90}, BAR_NUM_POINTS, BAR_PIXEL_PITCH),
  new BarConfig("134", new float[] {108+BAR_SPACING_X*5, 315, 0}, new float[] {0, 0, 90}, BAR_NUM_POINTS, BAR_PIXEL_PITCH),
  new BarConfig("504",   new float[] {108+BAR_SPACING_X*6, 315, 0}, new float[] {0, 0, 90}, BAR_NUM_POINTS, BAR_PIXEL_PITCH),
};


static final TowerConfig[] TOWER_CONFIG = {
 
};

static class BarConfig {
  final String id;
  final float x;
  final float y;
  final float z;
  final float xRot;
  final float yRot;
  final float zRot;
  final Strip.Metrics metrics;
  final float length;

  BarConfig(String id, float[] coordinates, float[] rotations, int numPoints, float pixelPitch) {
    this.id = id;
    this.x = coordinates[0];
    this.y = coordinates[1];
    this.z = coordinates[2];
    this.xRot = rotations[0];
    this.yRot = rotations[1];
    this.zRot = rotations[2];
    this.metrics = new Strip.Metrics(numPoints, pixelPitch);
    this.length = numPoints * pixelPitch;
  }
}

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
  globalTransform.rotateX(globalRotationY * PI / 180.);
  globalTransform.rotateY(globalRotationX * PI / 180.);
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

  List<Bar> allBars = new ArrayList<Bar>();

  for (BarConfig barConfig : BAR_CONFIG) {
    globalTransform.push();
    //globalTransform.translate(djBoothOffsetX, djBoothOffsetY, djBoothOffsetZ);

    Bar bar = new Bar(barConfig, globalTransform);
    allBars.add(bar);

    globalTransform.pop();
  }

  /* Strips ----------------------------------------------------------*/
  List<Strip> strips = new ArrayList<Strip>();

  for (StripConfig stripConfig : STRIP_CONFIG) {
    Strip.Metrics metrics = new Strip.Metrics(stripConfig.numPoints, stripConfig.spacing);

    globalTransform.push();
    globalTransform.translate(stripConfig.x, stripConfig.y, stripConfig.z);
    globalTransform.rotateX(stripConfig.xRot * PI / 180.);
    globalTransform.rotateY(stripConfig.yRot * PI / 180.);
    globalTransform.rotateZ(stripConfig.zRot * PI / 180.);

    strips.add(new Strip(metrics, stripConfig.yRot, globalTransform, true));

    globalTransform.pop();
  }
  /*-----------------------------------------------------------------*/

  Cube[] allCubesArr = new Cube[allCubes.size()];
  for (int i = 0; i < allCubesArr.length; i++) {
    allCubesArr[i] = allCubes.get(i);
  }

  return new SLModel(towers, allCubesArr, allBars,    strips);
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
    for (Bar c : model.bars) {
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

    for (Bar cube : model.bars)
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
