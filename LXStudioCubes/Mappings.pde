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

 import heronarts.lx.transform.*;


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

public SLModel buildModelFromJson(LXTransform globalTransform) {
  String jsonStr;
  try {
    byte[] bytes = loadBytes("cube_transforms.json");
    jsonStr = new String(bytes);
    // float yo = 10 / 0;
  } catch (Exception e) {
    List<Tower> emptyTowers = new ArrayList<Tower>();
    Cube emptyCubes[] = new Cube[0];
    List<Strip> emptyStrips = new ArrayList<Strip>();
    return new SLModel(emptyTowers, emptyCubes, emptyStrips);
  }

  JsonArray json = new Gson().fromJson(jsonStr, JsonArray.class);

  List<Tower> towers = new ArrayList<Tower>();
  Cube[] cubes = new Cube[200];
  int cubeIndex = 0;

  for (JsonElement element : json) {
    LXTransform transform = new LXTransform();
    LXMatrix matrix = transform.getMatrix();

    JsonObject cubeInfo = element.getAsJsonObject();

    int rowNum = 0;
    for (JsonElement row : cubeInfo.getAsJsonArray("transform")) {
      int colNum = 0;
      for (JsonElement entry : row.getAsJsonArray()) {
        float value = entry.getAsFloat();
        switch (rowNum) {
          case 0:
            switch (colNum) {
              case 0: matrix.m11 = value; break;
              case 1: matrix.m12 = value; break;
              case 2: matrix.m13 = value; break;
              case 3: matrix.m14 = value; break;
            }
            break;
          case 1:
            switch (colNum) {
              case 0: matrix.m21 = value; break;
              case 1: matrix.m22 = value; break;
              case 2: matrix.m23 = value; break;
              case 3: matrix.m24 = value; break;
            }
            break;
          case 2:
            switch (colNum) {
              case 0: matrix.m31 = value; break;
              case 1: matrix.m32 = value; break;
              case 2: matrix.m33 = value; break;
              case 3: matrix.m34 = value; break;
            }
            break;
          case 3:
            switch (colNum) {
              case 0: matrix.m41 = value; break;
              case 1: matrix.m42 = value; break;
              case 2: matrix.m43 = value; break;
              case 3: matrix.m44 = value; break;
            }
            break;
        }
        colNum++;
      }
      rowNum++;
    }

        List<Cube> tower = new ArrayList<Cube>();




    String macAddress = cubeInfo.get("id").getAsString();

    // Cube yikes = new Cube(
    //   new Integer(cubeIndex).toString(),
    //   0,
    //   0,
    //   0,
    //   0,
    //   0,
    //   0,
    //   transform,
    //   Cube.Type.LARGE
    // );

    // Cube.Type t = automappingController.getCubeTypeForId(macAddress);
    // float w = t.EDGE_WIDTH;
    // transform.translate(-w/2, -w/2, -w/2);

    LXMatrix inverter = new LXMatrix();
    // inverter.m11 = -1;
    // inverter.m33 = -1;
    // matrix.multiply(inverter);

    Cube cube = (Cube)automappingController.getModelForId(macAddress, transform);

    // println("WIDTHS", w, cube.xRange);

    // Cube cube = new Cube(
    //   new Integer(cubeIndex).toString(),
    //   0,
    //   0,
    //   0,
    //   0,
    //   0,
    //   0,
    //   transform,
    //   Cube.Type.LARGE
    // );
    tower.add(cube);
    cubes[++cubeIndex] = cube;
    towers.add(new Tower(new Integer(cubeIndex).toString(), tower));
  }
  List<Strip> emptyStrips = new ArrayList<Strip>();
  return new SLModel(towers, cubes, emptyStrips);
}

float floatify(Double v) {
  double v_ = v;
  return (float)v_;
}

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

  if (MAPPING_MODE) {
    // if in mapping mode, return an "empty" model
    return buildModelFromJson(globalTransform);
  }

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
