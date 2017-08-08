import java.math.*;


public class CVFixture extends UI3dComponent implements Comparable<CVFixture> {

  String id;
  float[] rotation;
  float[] translation;
  PMatrix3D mat;
  PVector rvec;
  PVector tvec;
  int ms;

  boolean flashOn = false;

  boolean selected = false;

  private ArrayList<LXPoint> points;
  private float w;
  private float h;
  private float d;

  float minX;
  float maxX;
  float minY;
  float maxY;
  float minZ;
  float maxZ;

  PVector currentCorner = null;


  void printMat(PMatrix3D mat) {
    println("==================");
    float target[] = new float[16];
    mat.get(target);

    for (int r = 0; r < 4; r++) {
      for (int c = 0; c < 4; c++) {
        int i = (r * 4) + c;
        print(target[i]);
        print(" ");
      }
      println(" ");
    }
    println("==================");
  }


  public CVFixture(ArrayList<ArrayList<Double>> rawMatrix, ArrayList<Double> rvec, ArrayList<Double> tvec, String id) {

    int rows = rawMatrix.size();
    int cols = rawMatrix.get(0).size();

    float[] values = new float[rows * cols];

    int i = 0;
    for (ArrayList<Double> row : rawMatrix) {
      for (Double col : row) {
        double v_ = col;
        float v = (float)v_;
        values[i++] = v;
      }
    }

    this.id = id;



    mat = new PMatrix3D();
    mat.set(values);

    this.rvec = new PVector(floatify(rvec.get(0)), floatify(rvec.get(1)), floatify(rvec.get(2)));  
    this.tvec = new PVector(floatify(tvec.get(0)), floatify(tvec.get(1)), floatify(tvec.get(2)));  

    ms = millis();

    LXPoint[] raw = automappingController.getRawPointsForId(id);


    points = new ArrayList<LXPoint>(Arrays.asList(raw));

    minX = points.get(0).x;
    maxX = points.get(0).x;
    minY = points.get(0).y;
    maxY = points.get(0).y;
    minZ = points.get(0).z;
    maxZ = points.get(0).z;

    for (LXPoint p : points) {
      minX = min(p.x, minX);
      maxX = max(p.x, maxX);
      minY = min(p.y, minY);
      maxY = max(p.y, maxY);
      minZ = min(p.z, minZ);
      maxZ = max(p.z, maxZ);
    }

    w = maxX - minX;
    h = maxY - minY;
    d = maxZ - minZ;
  }

  float floatify(Double v) {
    double v_ = v;
    return (float)v_;
  }


  ArrayList<ArrayList<Double>> getRawMatrix() {
    ArrayList<ArrayList<Double>> raw = new ArrayList<ArrayList<Double>>();



    float target[] = new float[16];
    mat.get(target);

    for (int r = 0; r < 4; r++) {
      ArrayList<Double> row = new ArrayList<Double>();
      for (int c = 0; c < 4; c++) {
        int i = (r * 4) + c;
        row.add((double)target[i]);
      }
      raw.add(row);
    }

    return raw;
  }

  void setSelected(boolean sel) {
    ms = millis();
    flashOn = true;
    selected = sel;
  }

  @Override
    public int compareTo(CVFixture other) {
    int me = parseInt(getLabel());
    int o = parseInt(other.getLabel());

    if (me < o) return -1;
    if (me > o) return 1;
    return 0;
  }


  String getLabel() {
    if (macToPhysid.containsKey(id)) {
      return macToPhysid.get(id);
    } else {
      return id.substring(id.length() - 3);
    }
  }

  color getColor() {
    int hash = id.hashCode();

    // println("HASHER", id, abs(hash) % 256);

    return color(abs(hash * 17) % 360, 100, 100);
  }

  void drawSideIfSelected(PGraphics pg, String label) {
    if (!selected) return;

    pg.translate(0, 7, 0);
    pg.textSize(5);
    pg.text(label, 0, 0, 0);
    pg.textSize(8);
    // pg.translate()
  }




  @Override
    protected void onDraw(UI ui, PGraphics pg) {

    pg.pushMatrix();


    PMatrix3D copy = new PMatrix3D(mat);

    PMatrix3D inverter = new PMatrix3D();
    inverter.m00 = -1;
    inverter.m22 = -1;


    copy.preApply(inverter);



    pg.applyMatrix(copy);


    if (millis() - ms > 500) {
      flashOn = !flashOn;
      ms = millis();
    }

    color mainColor;

    pg.noFill();
    pg.strokeWeight(3);
    if (flashOn && selected) {
      mainColor = 255;
    } else {
      pg.colorMode(HSB, 360, 100, 100);
      mainColor = getColor();
    }
    pg.stroke(mainColor);

    PVector a = new PVector(minX, minY, minZ);
    PVector b = new PVector(maxX, minY, minZ);

    float[] dX = {minX, maxX};
    float[] dY = {minY, maxY};
    float[] dZ = {minZ, maxZ};

    for (float startX : dX) {
    for (float endX : dX) {
    for (float startY : dY) {
    for (float endY : dY) {
    for (float startZ : dZ) {
    for (float endZ : dZ) {
      boolean drawX = startX != endX && startY == endY && startZ == endZ;
      boolean drawY = startX == endX && startY != endY && startZ == endZ;
      boolean drawZ = startX == endX && startY == endY && startZ != endZ;
      
      if (drawX || drawY || drawZ) {
        pg.line(startX, startY, startZ, endX, endY, endZ);
      }

    }
    }
    }
    }
    }
    }

    pg.pushMatrix();
    pg.pushStyle();

    PMatrix before = pg.getMatrix();

    boolean[] signs = {true, false};

    PVector[] corners = new PVector[8];

    int i = 0;
    for (boolean x : signs) {
      for (boolean y : signs) {
        for (boolean z : signs) {
          corners[i++] = new PVector(x ? minX : maxX, y ? minY : maxY, z ? minZ : maxZ);
        }
      }
    }

    PVector minCorner = null;
    PVector target = new PVector();
    float minDist = Float.POSITIVE_INFINITY;
    for (PVector corner : corners) {
      before.mult(corner, target);
      float d = target.magSq();
      if (d < minDist) {
        minCorner = corner;
        minDist = d;
      }
    }

    float cDist = Float.POSITIVE_INFINITY;
    if (currentCorner != null) {
      before.mult(currentCorner, target);
      cDist = target.magSq(); 
    }



    if (minDist < (cDist - 750)) {
      currentCorner = minCorner;
    }



    pg.translate(currentCorner.x, currentCorner.y, currentCorner.z);

    PMatrix current = pg.getMatrix();
    PMatrix3D currentCopy = new PMatrix3D(current);
    current.invert();

    pg.applyMatrix(current);
    pg.translate(currentCopy.m03, currentCopy.m13, currentCopy.m23);
    // pg.noFill();
    // pg.noStroke();
    pg.textMode(SHAPE);
    pg.textSize(5);
    pg.textAlign(CENTER, CENTER);

    if (!automappingController.hideLabels) {
      pg.fill(ui.theme.getDarkBackgroundColor());
      pg.noStroke();
      pg.box(10, 7, 1);
      pg.fill(mainColor);
      pg.stroke(mainColor);
      pg.text(getLabel(), 0, 0, 0.50000001);
    }
    // pg.sphere(0.5);
    pg.popStyle();
    pg.popMatrix();

   


    pg.popMatrix();
  }
}

CVFixture previous = null;


class MappedCubeItem extends UIItemList.AbstractItem {
  final CVFixture cube;

  MappedCubeItem(CVFixture _cube) {
    this.cube = _cube;
  }

  String getLabel() {
    return cube.getLabel();
  }

  @Override
    public void onFocus() {
    if (previous != null && previous != cube) {
      previous.setSelected(false);
    }
    cube.setSelected(true);
    previous = cube;
  }
}

class MappedCubeList extends UIItemList.ScrollList {

  MappedCubeList(UI ui, float x, float y, float w, float h) {
    super(ui, x, y, w, h);
  }

  @Override
    void onKeyPressed(KeyEvent keyEvent, 
    char keyChar, 
    int keyCode) {

    MappedCubeItem item = (MappedCubeItem)getFocusedItem();
    if (item == null) return;

    CVFixture cube = item.cube;

    if (keyCode == BACKSPACE) {
      automappingController.removeCube(cube.id);
      return;
    }

    if (keyChar == 'h') {
      println("SWAPPING LABELS");
      automappingController.hideLabels = !automappingController.hideLabels;
      return;
    }

    String dirs = "udlrbf";

    if (dirs.indexOf(keyChar) != -1) {
      automappingController.rotateCubes(keyChar);
      return;
    }


    super.onKeyPressed(keyEvent, keyChar, keyCode);

  }

  @Override
    void onBlur() {
    if (previous != null) {
      previous.setSelected(false);
      previous = null;
    }
  }
}


class UIAutomapping extends UICollapsibleSection {
  UI ui;
  LX lx;

  UIAutomapping(LX lx_, UI ui_, float x, float y, float w) {
    super(ui_, x, y, w, 140);

    setTitle("AUTOMAPPING");
    setTitleX(20);

    this.ui = ui_;
    this.lx = lx_;

    int yOffset = 0;
    int padding = 3;

    yOffset += buildEnabledButton(yOffset, w);
    yOffset += buildStartMappingButton(yOffset, w);
    yOffset += padding;
    yOffset += buildMappedCubeList(yOffset, w);
    yOffset += padding;
    yOffset += buildJSONSaver(yOffset, w);
    yOffset += padding;
  }

  int buildEnabledButton(float yOffset, float w) {
    addTopLevelComponent(new UIButton(4, 4, 12, 12) {
      @Override
        public void onToggle(boolean isOn) {
      }
    }
    .setParameter(automappingController.enabled)
      .setBorderRounding(4));

    return 0;
  }

  void centerView() {
    SLStudio.UI sUI = ((SLStudio)lx).ui;

    sUI.preview.setCenter(0, 0, 0);
    sUI.preview.setPhi(0);
    sUI.preview.setPerspective(0);
    sUI.preview.setTheta(0);
  }

  int buildStartMappingButton(float yOffset, float w) {
    final String disconnected = "No App Connected";
    final String connected = "Start Mapping";
    final String inProgress = "Mapping in Progress";

    int h = 18;

    int buttonW = 120;
    int margin = 3;
    float bigW = w - (buttonW + margin + 8);
    float smallerW =  (buttonW - margin) / 2;



    final UIButton startMapping = new UIButton(0, yOffset, bigW, h) {
      @Override
        protected void onToggle(boolean active) {
        if (!active) return;


        switch (automappingController.state.getEnum()) {
        case DISCONNECTED:
          break;
        case CONNECTED:
          automappingController.communicator.sendCommand("app.start", null);
          break;
        case RUNNING:
          break;
        default:
          throw new RuntimeException("Invalid state in mapping button");
        }
      }
    }
    .setLabel(disconnected).setMomentary(true);
    startMapping.addToContainer(this);

    automappingController.state.addListener(new LXParameterListener() {
      public void onParameterChanged(LXParameter p) {
        AutomappingState state = ((EnumParameter<AutomappingState>)p).getEnum();
        String label;
        switch (state) {
        case DISCONNECTED: 
          label = disconnected; 
          break;
        case CONNECTED: 
          label = connected; 
          break;
        case RUNNING: 
          label = inProgress; 
          break;
        default:
          throw new RuntimeException("Invalid state in button label");
        }

        startMapping.setLabel(label);
      }
    }
    );


    new UIButton(bigW + margin, yOffset, smallerW, h) {
      @Override
        protected void onToggle(boolean active) {
        if (!active) return;

        centerView();
        // automappingController.center();
      }
    }
    .setLabel("C. View")
      .setMomentary(true)
      .addToContainer(this);

    new UIButton(bigW + 2*margin + smallerW, yOffset, smallerW, h) {
      @Override
        protected void onToggle(boolean active) {
        if (!active) return;

        // centerView();
        automappingController.center();
      }
    }
    .setLabel("C. Model")
      .setMomentary(true)
      .addToContainer(this);

    return h;
  }

  int buildMappedCubeList(float yOffset, float w) {

    int h = 78;

    final List<UIItemList.Item> items = new ArrayList<UIItemList.Item>();
    final MappedCubeList outputList = new MappedCubeList(ui, 0, yOffset, w-8, h);


    for (CVFixture c : automappingController.mappedFixtures) { 
      items.add(new MappedCubeItem(c));
    }


    outputList.setItems(items).setSingleClickActivate(true);
    outputList.addToContainer(this);

    final Runnable update = new Runnable() {
      public void run() {
        final List<UIItemList.Item> localItems = new ArrayList<UIItemList.Item>();
        int i = 0;
        for (CVFixture c : automappingController.mappedFixtures) { 
          localItems.add(new MappedCubeItem(c));
        }
        outputList.setItems(localItems);
        redraw();
      }
    };


    automappingController.mappedFixtures.addListener(new ListListener<CVFixture>() {
      public void itemAdded(final int index, final CVFixture c) {
        dispatcher.dispatchUi(update);
      }
      public void itemRemoved(final int index, final CVFixture c) {
        dispatcher.dispatchUi(update);
      }
    }
    );

    return h;
  }


  int buildJSONSaver(float yOffset, float w) {
    int h = 16;
    int buttonW = 120;
    int margin = 3;
    float textW = w - (buttonW + margin + 8);
    float subButtonW =  (buttonW - margin) / 2;

    new UIButton(textW + margin, yOffset, subButtonW, h) {
      @Override
        public void onToggle(boolean isOn) {
        if (!isOn) return;

        automappingController.saveToJSON();
      }
    }
    .setLabel("Save")
      .setMomentary(true)
      .addToContainer(this);

    new UIButton(textW + (margin * 2) + subButtonW, yOffset, subButtonW, h) {
      @Override
        public void onToggle(boolean isOn) {
        if (!isOn) return;

        automappingController.loadJSON();
        automappingController.center();
      }
    }
    .setLabel("Load")
      .setMomentary(true)
      .addToContainer(this);

    new UITextBox(0, yOffset, textW, h) {
      public String getDescription() {
        return "JSON file (inside data folder) to save mapped cubes. Hit enter to edit.";
      }
    }
    .setParameter(automappingController.saveFile)
      .addToContainer(this);
    return h;
  }
}

public static enum AutomappingState {
  DISCONNECTED, 
    CONNECTED, 
    RUNNING
}

public static enum PatternState {
  S0_IDENTIFY, 
    S1_BLACK, 
    S2_WHITE, 
    S3_BLACK, 
    STATE_END;
  public static final PatternState values[] = values();
}


// TODO RENAME
public static enum PatternMode {
  CALIBRATING, 
    MAPPING, 
    SHOW_PIXEL, 
    ALL_ON, 
    ALL_OFF, 
    SHOW_CUBE
}

class AutomappingController extends LXComponent {
  LX lx;

  private LXChannel mappingChannel = null;
  private LXPattern mappingPattern = null;

  int timesCalled = 0;


  private final BooleanParameter enabled = new BooleanParameter("Enabled");
  private final EnumParameter<AutomappingState> state = new EnumParameter<AutomappingState>("State", AutomappingState.RUNNING);
  private final StringParameter saveFile = new StringParameter("Save File", "cube_transforms.json");

  private String[] macAddresses = null;
  private int[] pixelOrder = null;
  public ClientCommunicator communicator;


  public ListenableList<CVFixture> mappedFixtures;


  PatternState patternState;
  PatternMode mode = PatternMode.ALL_OFF;


  int baseColor = LXColor.WHITE;
  int resetFrameBaseColor = LXColor.scaleBrightness(LXColor.WHITE, 1);

  int NUM_RUNTHROUGHS = 1;
  int NUM_CALIBRATION_RUNTHROUGHS = -1;

  int RESET_FRAMES = 30;
  int RESET_BLACK_FRAMES = 16;
  int S0_FRAMES = 3;
  int S1_FRAMES = 3;
  int S2_FRAMES = 3;
  int S3_FRAMES = 3;

  int numPoints = 0;
  int runthroughCount = -1;
  int patternPixelIndex = 0;
  int frameCounter = 0;
  int showPixelIndex = 0;
  int mod = 1;
  boolean hideLabels = false;
  String currentCubeId = null;

  HashMap<String, Cube.Type> knownCubeTypes;

  final JLabel label = new JLabel();




  AutomappingController(LX lx) {
    super(lx);
    this.lx = lx;

    addParameter(enabled);
    addParameter(state);

    mappedFixtures = new ListenableList<CVFixture>();

    // pixelOrder = new int[180];
    // for (int i = 0; i < pixelOrder.length; i++) {
    //   pixelOrder[i] = i;
    // }

    enabled.addListener(new LXParameterListener() {
      public void onParameterChanged(LXParameter p) {
        if (((BooleanParameter)p).isOn()) {
          addChannel();
        } else {
          println("ABOUT TO REMOVE");
          removeChannel();
        }
      }
    });

    knownCubeTypes = loadKnownCubeTypes();
  }

  HashMap<String, Cube.Type> loadKnownCubeTypes() {
    HashMap<String, Cube.Type> types = new HashMap();

    String data = new String(loadBytes("physid_to_size.json"));
    HashMap<String, String> map = (new Gson()).fromJson(data, new TypeToken<HashMap<String, String>>() {}.getType());

    for (Map.Entry<String, String> entry : map.entrySet()) {
      String k = entry.getKey();
      String stringType = entry.getValue();
      Cube.Type v;
      if (stringType.equals("SMALL")) {
        v = Cube.Type.SMALL;
      } else if (stringType.equals("MEDIUM")) {
        v = Cube.Type.MEDIUM;
      } else if (stringType.equals("LARGE")) {
        v = Cube.Type.LARGE;
      } else {
        throw new RuntimeException("UNKNOWN CUBE TYPE: " + stringType);
      }
      types.put(k, v);
    }

    return types;
  }


  void showError(String message, boolean sendToClient) {
    label.setText(message);

    Runnable showError = new Runnable() {
      public void run() {
        JOptionPane.showMessageDialog(null, label);
        communicator.sendCommand("dismissError", null);
      }
    };

    (new Thread(showError)).start();

    if (sendToClient) {
      HashMap<String, String> msg = new HashMap<String, String>();
      msg.put("message", message);
      automappingController.communicator.sendCommand("showError", msg);
    }
  }

  void dismissError() {
    Window win = SwingUtilities.getWindowAncestor(label);
    if (win != null) {
      win.setVisible(false);
    }
  }

  void rotateCubes(char dir) {
    CVFixture selected = null;
    for (CVFixture cube : mappedFixtures) {
      if (cube.selected) {
        selected = cube;
        break;
      }
    }

    if (selected == null) {
      return;
    }

    PMatrix3D inv = selected.mat.get();
    inv.invert();

    PMatrix3D rot = new PMatrix3D();

    switch(dir) {
    case 'u': 
      break;
    case 'd': 
      rot.rotateX(PI); 
      break;
    case 'l': 
      rot.rotateZ(PI/2); 
      break;
    case 'r': 
      rot.rotateZ(-PI/2); 
      break;
    case 'b': 
      rot.rotateX(PI/2); 
      break;
    case 'f': 
      rot.rotateX(-PI/2); 
      break;
    default: 
      throw new RuntimeException("Invalid direction in rotate");
    }

    for (CVFixture cube : mappedFixtures) {
      cube.mat.preApply(inv);
      cube.mat.preApply(rot);
    }
  }

  void center() {
    if (mappedFixtures.size() == 0) {
      return;
    }

    PVector sum = new PVector(0, 0, 0);
    for (CVFixture c : mappedFixtures) {
      PVector t = new PVector(c.mat.m03, c.mat.m13, c.mat.m23);
      sum.add(t);
    }
    sum.div(mappedFixtures.size());

    for (CVFixture c : mappedFixtures) {
      c.mat.m03 -= sum.x;
      c.mat.m13 -= sum.y;
      c.mat.m23 -= sum.z;

    }
  }

  void addCube(ArrayList<ArrayList<Double>> matrix, ArrayList<Double> rvec, ArrayList<Double> tvec, String id) {
    SLStudio.UI sUI = ((SLStudio)lx).ui;

    CVFixture c = new CVFixture(matrix, rvec, tvec, id);

    mappedFixtures.add(c);
    Collections.sort(mappedFixtures.list);


    sUI.preview.addComponent(c);
  }



  void removeCube(String id) {
    CVFixture cube = null;
    int cubeIndex = -1;
    for (int i = 0; i < mappedFixtures.size(); i++) {
      CVFixture temp = mappedFixtures.get(i);
      if (temp.id.equals(id)) {
        cube = temp;
        cubeIndex = i;
        break;
      }
    }

    if (cube == null) {
      return;
    }

    SLStudio.UI sUI = ((SLStudio)lx).ui;
    sUI.preview.removeComponent(cube);

    mappedFixtures.remove(cubeIndex);
  }

  boolean alreadyMapped(String id) {
    for (CVFixture c : mappedFixtures) {
      if (id.equals(c.id)) {
        return true;
      }
    }
    return false;
  }



  ArrayList<Object> getMappedTransforms() {
    ArrayList<Object> transforms = new ArrayList<Object>();
    for (CVFixture c : mappedFixtures) {
      transforms.add(c.getRawMatrix());
    }
    return transforms;
  }

  ArrayList<String> getMappedIds() {
    ArrayList<String> ids = new ArrayList<String>();
    for (CVFixture c : mappedFixtures) {
      ids.add(c.id);
    }
    return ids;
  }

  ArrayList<String> getUnmappedIds() {
    ArrayList<String> ids = new ArrayList<String>();
    for (String id : macAddresses) {
      if (!alreadyMapped(id)) {
        ids.add(id);
      }
    }
    return ids;
  }

  LXModel getModelForId(String id) {
    return getModelForId(id, new LXTransform());
  }

  Cube.Type getCubeTypeForId(String id) {
    if (id.equals("SMALLBOI")) {
      return Cube.Type.SMALL;
    }


    if (!macToPhysid.containsKey(id)) {
      return Cube.Type.LARGE;
    }


    String physId = macToPhysid.get(id);
    if (!knownCubeTypes.containsKey(physId)) {
      return Cube.Type.LARGE;
    }
        
    return knownCubeTypes.get(physId);
  }

  LXModel getModelForId(String id, LXTransform t) {
    Cube.Type type = getCubeTypeForId(id);

    String realId;
    if (macToPhysid.containsKey(id)) {
      realId = macToPhysid.get(id);
    } else {
      realId = "UNKNOWN";
    }

    return new Cube(realId, 0, 0, 0, 0, 0, 0, t, type);
  }

  ArrayList<LXPoint> centerPoints(ArrayList<LXPoint> input) {
    ArrayList<LXPoint> output = new ArrayList<LXPoint>();
    LXVector avgPoint = new LXVector(0, 0, 0);
    for (LXPoint p : input) {
      avgPoint.add(new LXVector(p));
    }
    avgPoint.div(input.size());
    for (LXPoint p : input) {
      output.add(new LXPoint(p.x - avgPoint.x, p.y - avgPoint.y, p.z - avgPoint.z));
    }
    return output;
  }

  LXPoint[] kyleCubeModel(float ledDistance, float edgeDistance, int ledsPerStrip) {
    int n = ledsPerStrip * 12;
    int m = ledsPerStrip - 1;
    LXVector[] idealVectors = new LXVector[n];
    LXPoint[] idealPoints = new LXPoint[n];
    for (int pixelIndexInCube = 0; pixelIndexInCube < n; pixelIndexInCube++) {
      int pixelIndexInStrip = pixelIndexInCube % ledsPerStrip;
      int stripIndex = pixelIndexInCube / ledsPerStrip;

      if (stripIndex == 0) {
        idealVectors[pixelIndexInCube] = new LXVector(edgeDistance + pixelIndexInStrip, 2 * edgeDistance + m, 0);
      } else if (stripIndex == 1) {
        idealVectors[pixelIndexInCube] = new LXVector(2*edgeDistance+m, edgeDistance+(m-pixelIndexInStrip), 0);
      } else if (stripIndex == 2) {
        idealVectors[pixelIndexInCube] = new LXVector(edgeDistance+(m-pixelIndexInStrip), 0, 0);
      } else if (stripIndex == 3) {
        idealVectors[pixelIndexInCube] = new LXVector(2*edgeDistance+m, 2*edgeDistance+m, edgeDistance+pixelIndexInStrip);
      } else if (stripIndex == 4) {
        idealVectors[pixelIndexInCube] = new LXVector(2*edgeDistance+m, edgeDistance+(m-pixelIndexInStrip), 2*edgeDistance+m);
      } else if (stripIndex == 5) {
        idealVectors[pixelIndexInCube] = new LXVector(2*edgeDistance+m, 0, edgeDistance+(m-pixelIndexInStrip));
      } else if (stripIndex == 6) {
        idealVectors[pixelIndexInCube] = new LXVector(edgeDistance+(m-pixelIndexInStrip), 2*edgeDistance+m, 2*edgeDistance+m);
      } else if (stripIndex == 7) {
        idealVectors[pixelIndexInCube] = new LXVector(0, edgeDistance+(m-pixelIndexInStrip), 2*edgeDistance+m);
      } else if (stripIndex == 8) {
        idealVectors[pixelIndexInCube] = new LXVector(edgeDistance+pixelIndexInStrip, 0, 2*edgeDistance+m);
      } else if (stripIndex == 9) {
        idealVectors[pixelIndexInCube] = new LXVector(0, 2*edgeDistance+m, edgeDistance+(m-pixelIndexInStrip));
      } else if (stripIndex == 10) {
        idealVectors[pixelIndexInCube] = new LXVector(0, edgeDistance+(m-pixelIndexInStrip), 0);
      } else if (stripIndex == 11) {
        idealVectors[pixelIndexInCube] = new LXVector(0, 0, edgeDistance+pixelIndexInStrip);
      }
    }

    float maxX = 0;
    float maxY = 0;
    float maxZ = 0;


    for (int i = 0; i < n; i++) {
      LXVector v = idealVectors[i];
      v.x *= ledDistance;
      v.y *= ledDistance;
      v.z *= ledDistance;
      maxX = max(maxX, v.x);
      maxY = max(maxY, v.y);
      maxZ = max(maxZ, v.z);
    }

    for (int i = 0; i < n; i++) {
      LXVector v = idealVectors[i];
      idealPoints[i] = new LXPoint(v.x, v.y, v.z);
    }


    return idealPoints;
  }

  private LXPoint[] getRawPointsForId(String id) {
    LXModel model = getModelForId(id);
    return model.points;


  }

  ArrayList<LXPoint> getPointsForId(String id) {
    LXPoint[] rawPoints = getRawPointsForId(id);
    ArrayList<LXPoint> points = new ArrayList<LXPoint>();
    int[] po = getPixelOrder();

    if (po == null || po.length != rawPoints.length) {
      int n = rawPoints.length;
      ArrayList<Integer> orderAr = new ArrayList<Integer>();
      int[] shuffled = new int[n];
      for (int i = 0; i < n; i++) {
        orderAr.add(i);
      }

      Collections.shuffle(orderAr);

      for (int i = 0; i < n; i++) {
        shuffled[i] = orderAr.get(i);
      }

      setPixelOrder(shuffled);

      po = shuffled;
    }

    for (int i : po) {
      points.add(rawPoints[i]);
    }
    return points;
  }

  CVFixture getMappedCube(String id) {
    for (CVFixture c : mappedFixtures) {
      if (c.id.equals(id)) {
        return c;
      }
    }
    return null;
  }

  ArrayList<Double> arrayIfy(PVector vec) {
    ArrayList<Double> ar = new ArrayList<Double>();
    ar.add((double)vec.x);
    ar.add((double)vec.y);
    ar.add((double)vec.z);
    return ar;
  }

  void saveToJSON() {
    ArrayList<HashMap<String, Object>> jsonCubes = new ArrayList<HashMap<String, Object>>();
    for (CVFixture c : mappedFixtures) {
      HashMap<String, Object> m = new HashMap<String, Object>();
      m.put("id", c.id);
      m.put("transform", c.getRawMatrix());
      m.put("debug", c.getLabel());
      m.put("rvec", arrayIfy(c.rvec));
      m.put("tvec", arrayIfy(c.tvec));
      jsonCubes.add(m);
    }

    Gson gson = new GsonBuilder().setPrettyPrinting().create();


    saveBytes(dataPath(saveFile.getString()), gson.toJson(jsonCubes).getBytes());
  }

  void loadJSON() {
    String data = new String(loadBytes(saveFile.getString()));
    ArrayList<Object> message = (new Gson()).fromJson(data, new TypeToken<ArrayList<Object>>() {
    }
    .getType());

    for (Object cube_ : message) {
      Map<String, Object> cube = (Map<String, Object>)cube_;
      ArrayList<ArrayList<Double>> jsonTransform = (ArrayList<ArrayList<Double>>)cube.get("transform");
      String mac = (String)cube.get("id");

      ArrayList<Double> rvec = (ArrayList<Double>)cube.get("rvec");
      ArrayList<Double> tvec = (ArrayList<Double>)cube.get("tvec");

      addCube(jsonTransform, rvec, tvec, mac);
    }
  }

  void setClientsConnected(boolean connected) {
    switch (state.getEnum()) {
    case DISCONNECTED:
      if (connected) {
        state.setValue(AutomappingState.CONNECTED);
      }
      break;
    case CONNECTED:
    case RUNNING:
      if (!connected) {
        state.setValue(AutomappingState.DISCONNECTED);
      }
      break;
    default:
      throw new RuntimeException("Invalid state in setClientsConnected");
    }
  }

  void setPixelOrder(int[] pixelOrder) {
    this.pixelOrder = pixelOrder;
    mod = pixelOrder.length;
  }

  int[] getPixelOrder() {
    return pixelOrder;
  }

  String[] getMacAddresses() {
    return macAddresses;
  }

  void startRunning() {
    enabled.setValue(true);
    outputControl.enabled.setValue(true);
    state.setValue(AutomappingState.RUNNING);
    reset();
  }


  void startCalibration() {
    startRunning();
    mode = PatternMode.CALIBRATING;
  }

  void startMapping() {
    startRunning();

    println("networkMonitor.networkDevices.size(): "+networkMonitor.networkDevices.size());
    macAddresses = new String[networkMonitor.networkDevices.size()];
    int i = 0;
    for (NetworkDevice device : networkMonitor.networkDevices) {
      macAddresses[i++] = NetworkUtils.macAddrToString(device.macAddress);
    }

    patternState = PatternState.S1_BLACK;
    mode = PatternMode.MAPPING;
  }

  void reset() {
    mode = PatternMode.ALL_OFF;
    runthroughCount = -1;
    patternPixelIndex = 0;
    patternState = PatternState.S1_BLACK;
    frameCounter = 0;
    currentCubeId = null;
  }

  void disableAutomapping() {
    reset();
    state.setValue(AutomappingState.CONNECTED);
  }

  void mapNextCube(String id) {
    startRunning();
    println("THIS IS ID", id);
    currentCubeId = id;
    runthroughCount = -1;
    patternPixelIndex = 0;
    frameCounter = 0;
    patternState = PatternState.S1_BLACK;
    mode = PatternMode.MAPPING;
  }

  void showNextCube(String id) {
    startRunning();
    currentCubeId = id;
    runthroughCount = -1;
    patternPixelIndex = 0;
    frameCounter = 0;
    patternState = PatternState.S1_BLACK;
    mode = PatternMode.SHOW_CUBE;
  }

  void showImageForPixel(int pixelIndex) {
    startRunning();
    mode = PatternMode.SHOW_PIXEL;
    showPixelIndex = pixelIndex;
  }

  void showAll(boolean on) {
    startRunning();
    mode = on ? PatternMode.ALL_ON : PatternMode.ALL_OFF;
  }

  void updateCalibrating() {
    if (NUM_CALIBRATION_RUNTHROUGHS >= 0 && runthroughCount >= NUM_CALIBRATION_RUNTHROUGHS) {
      reset();
      return;
    }

    switch (patternState) {
    case S1_BLACK:
      frameCounter = (frameCounter+1) % S1_FRAMES;
      break;

    case S2_WHITE:
      frameCounter = (frameCounter+1) % S2_FRAMES;
      if (frameCounter == 0) {
        runthroughCount++;
      }
      break;

    default:
      throw new RuntimeException("Invalid pattern state in Calibration mode");
    }


    if (frameCounter == 0) {
      patternState = patternState == PatternState.S2_WHITE ? PatternState.S1_BLACK : PatternState.S2_WHITE;
    }
  }

  void updateMapping() {
    if (NUM_RUNTHROUGHS >= 0 && runthroughCount >= NUM_RUNTHROUGHS) {
      reset();
      return;
    }

    switch (patternState) {
    case S0_IDENTIFY:
      frameCounter = (frameCounter+1) % S0_FRAMES;
      if (frameCounter == 0) {
        patternPixelIndex = (patternPixelIndex+1) % mod;
      }
      break;

    case S1_BLACK:
      frameCounter = (frameCounter+1) % S1_FRAMES;
      break;

    case S2_WHITE:
      frameCounter = (frameCounter+1) % (patternPixelIndex == 0 ? RESET_FRAMES : S2_FRAMES);
      if (frameCounter == 0 && patternPixelIndex == 0) {
        runthroughCount++;
      }
      break; 

    case S3_BLACK: 
      frameCounter = (frameCounter+1) % (patternPixelIndex == 0 ? RESET_BLACK_FRAMES : S3_FRAMES);
      break;

    default:
      throw new RuntimeException("Invalid pattern state in Mapping mode");
    }

    // println(patternState);

    if (frameCounter == 0) {
      int endOrdinal = PatternState.STATE_END.ordinal();
      patternState = PatternState.values[(patternState.ordinal() + 1) % endOrdinal];
    }
  }

  void updateFrame() {
    if (!enabled.isOn()) {
      return;
    }

    switch (mode) {
    case CALIBRATING:
      updateCalibrating();
      return;

    case MAPPING:
    case SHOW_CUBE:
      updateMapping();
      return;

    case SHOW_PIXEL:
    case ALL_ON:
    case ALL_OFF:
      return;
    }
  }

  color mappingColor(String cubeId, int i) {

    switch (patternState) {
    case S0_IDENTIFY:
      if (!cubeId.equals(currentCubeId)) {
        return LXColor.BLACK;
      }

      if (i == -1) {
        return LXColor.WHITE;
      }

      int pixelIndexInCube = automappingController.getPixelOrder()[patternPixelIndex];
      return i == pixelIndexInCube ? baseColor : LXColor.BLACK;

    case S1_BLACK:
      return LXColor.BLACK;

    case S2_WHITE:
      return resetFrameBaseColor;

    case S3_BLACK: 
      return LXColor.BLACK;

    default:
      throw new RuntimeException("Invalid pattern state in Mapping mode");
    }
  }




  color getPixelColor(String cubeId, int i) {
    if (macToPhysid.get(cubeId).equals("393")) {
      return LXColor.WHITE; //.hsb(map(i, 0, 105, 0, 360), 50, 100);
      // return LXColor.GREEN;
    }


    AutomappingState s = state.getEnum();

    if (s != AutomappingState.RUNNING) {


      CVFixture c = getMappedCube(cubeId);
      if (c == null) {
        return s == AutomappingState.DISCONNECTED ? LXColor.RED : color(20, 20, 20);
      }

      // if (p.x == minX && p.y == minY) {
      //   pg.stroke(color(0, 100, 100));
      // } else if (p.x == minX && p.z == minZ) {
      //   pg.stroke(color(120, 100, 100));
      // } else if (p.y == minY && p.z == minZ) {
      //   pg.stroke(color(240, 100, 100));
      // } else {
      //   pg.noStroke();
      // }

      if (c.selected && c.flashOn) {
        return LXColor.WHITE;
      // } else if (i == 0 || i == 20) {
      //   return LXColor.RED;
      // } else if (i == 1 || i == 21) {
      //   return LXColor.GREEN;
      // } else if (i == 2 || i == 22) {
      //   return LXColor.BLUE;
      } else {
        return c.getColor();
      }
    }



    switch (mode) {
    case CALIBRATING:
      return patternState == PatternState.S1_BLACK ? LXColor.BLACK : resetFrameBaseColor;
    case MAPPING:
      return mappingColor(cubeId, i);        
    case SHOW_PIXEL:
      return showPixelIndex == i ? LXColor.WHITE : LXColor.BLACK;
    case ALL_ON:
      return LXColor.WHITE;
    case ALL_OFF:
      return LXColor.BLACK;
    case SHOW_CUBE:
      return mappingColor(cubeId, -1);
    default:
      throw new RuntimeException("Invalid mode");
    }
  }


  private void addChannel() {
    mappingPattern = new AutomappingPattern(lx);
    mappingChannel = lx.engine.addChannel(new LXPattern[] {mappingPattern});

    for (LXChannel channel : lx.engine.channels)
      channel.cueActive.setValue(false);

    mappingChannel.fader.setValue(1);
    mappingChannel.label.setValue("Automapping");
    mappingChannel.cueActive.setValue(true);
  }

  private void removeChannel() {
    lx.engine.removeChannel(mappingChannel);
    mappingChannel = null;
    mappingPattern = null;
  }
}

public class AutomappingPattern extends SLPattern {


  public AutomappingPattern(LX lx) {
    super(lx);
  }

  public void run(double deltaMs) {
    automappingController.updateFrame();
  }
}
