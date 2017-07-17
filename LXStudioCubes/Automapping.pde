import java.math.*;


public class CVCube extends UI3dComponent implements Comparable<CVCube> {

  private final float SIZE = Cube.Type.LARGE.EDGE_WIDTH;

  String id;
  float[] rotation;
  float[] translation;
  PMatrix3D mat;
  PVector rvec;
  PVector tvec;
  int ms;

  boolean flashOn = false;


  boolean selected = false;
  boolean regenerate = false;


  PImage selectedTex = null;
  PImage notSelectedTex = null;
  PShape s = null;

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


  public CVCube(ArrayList<ArrayList<Double>> rawMatrix, ArrayList<Double> rvec, ArrayList<Double> tvec, String id) {
    println("OTHER EDIGE WIDTH", Cube.Type.LARGE.EDGE_WIDTH);

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
    public int compareTo(CVCube other) {
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

    return color(abs(hash) % 360, 100, 100);
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

      // int delta = millis() - ms;
      // sinLfo.run(delta);



    pg.pushMatrix();

    // PMatrix3D copy = new PMatrix3D(mat);



    // PMatrix3D t = new PMatrix3D();
    // t.m01 = copy.m10;
    // t.m02 = copy.m20;
    // t.m12 = copy.m21;

    // t.m10 = copy.m01;
    // t.m20 = copy.m02;
    // t.m21 = copy.m12;

    // copy.m01 = t.m01;
    // copy.m02 = t.m02;
    // copy.m12 = t.m12;
    // copy.m10 = t.m10;
    // copy.m20 = t.m20;
    // copy.m21 = t.m21;

    // copy.m23 *= -1;

    // pg.translate(tvec.x, tvec.y, tvec.z);


    // pg.rotateX(rvec.x);
    // pg.rotateY(rvec.y);
    // pg.rotateZ(rvec.z);

    PMatrix3D copy = new PMatrix3D(mat);

        PMatrix3D inverter = new PMatrix3D();
    inverter.m00 = -1;
    // inverter.m00 = textScaleX;
    inverter.m22 = -1;


    copy.preApply(inverter);




    // copy.m23 *= -1;

    // copy.m03 += globalOffsetX;
    // copy.m13 += globalOffsetY;
    // copy.m23 += globalOffsetZ;

    // pg.translate(globalOffsetX, globalOffsetY, globalOffsetZ);




    pg.applyMatrix(copy);

    // pg.colorMode(RGB, 256);

     pg.beginShape(QUADS);

     // pg.colorMode(RGB, 1);

     // pg.noStroke();

     // pg.fill(selected ? ui.theme.getPrimaryColor() : 255);
     pg.colorMode(HSB, 360, 100, 100);
     pg.fill(getColor());
     pg.colorMode(RGB, 256, 256, 256);

     if (millis() - ms > 500) {
      flashOn = !flashOn;
      ms = millis();
     }

     if (flashOn && selected) {
      pg.stroke(255);
      pg.strokeWeight(5);
     } else {
      pg.stroke(0);
      pg.strokeWeight(1);
     }

    float s = 0;
    float e = SIZE;

    pg.vertex(s, s,  e);
    pg.vertex(s,  e,  e);
    pg.vertex( e,  e,  e);
    pg.vertex( e, s,  e);

    pg.vertex( e, s,  e);
    pg.vertex( e,  e,  e);
    pg.vertex( e,  e, s);
    pg.vertex( e, s, s);

    pg.vertex( e, s, s);
    pg.vertex( e,  e, s);
    pg.vertex(s,  e, s);
    pg.vertex(s, s, s);

    pg.vertex(s, s, s);
    pg.vertex(s,  e, s);
    pg.vertex(s,  e,  e);
    pg.vertex(s, s,  e);

    pg.vertex(s, s,  e);
    pg.vertex(s, s, s);
    pg.vertex( e, s, s);
    pg.vertex( e, s,  e);

    pg.vertex(s,  e,  e);
    pg.vertex(s,  e, s);
    pg.vertex( e,  e, s);
    pg.vertex( e,  e,  e);





    pg.endShape();

  pg.pushMatrix();
  pg.pushStyle();
  // pg.fill(255);
  // pg.noStroke();
  pg.translate(1.5, 4.1, 0.4);
  pg.rotateX(-PI/3);
  pg.box(2, 2, 5);
  pg.popStyle();
  pg.popMatrix();


  pg.fill(color(0, 255, 0));
  pg.stroke(color(0, 255, 0));

  pg.scale(-1, -1);
  pg.textMode(SHAPE);
  pg.textSize(8);
  pg.textAlign(CENTER, CENTER);

  // pg.pushMatrix();
  // pg.rotateX(textRotX);
  // pg.rotateY(textRotY);
  // pg.rotateZ(textRotZ);
  // pg.text(getLabel(), textCoordX, textCoordY, textCoordZ);
  // pg.popMatrix();

  pg.pushMatrix();
  pg.translate(-SIZE/2, -SIZE/2, SIZE + 0.1);
  pg.text(getLabel(), 0, 0, 0);
  drawSideIfSelected(pg, "f");
  pg.popMatrix();



  pg.pushMatrix();
  pg.rotateY(PI/2);
  pg.translate(-SIZE/2, -SIZE/2, 0.1);
  pg.text(getLabel(), 0, 0, 0);
  drawSideIfSelected(pg, "r");
  pg.popMatrix();


  pg.pushMatrix();
  pg.rotateY(PI);
  pg.translate(SIZE/2, -SIZE/2, 0.1);
  pg.text(getLabel(), 0, 0, 0);
  drawSideIfSelected(pg, "b");
  pg.popMatrix();


  pg.pushMatrix();
  pg.rotateY(3.0 *PI / 2.0);
  pg.translate(SIZE/2, -SIZE/2, SIZE + 0.1);
  pg.text(getLabel(), 0, 0, 0);
  drawSideIfSelected(pg, "l");
  pg.popMatrix();


  pg.pushMatrix();
  pg.rotateX(PI/2);
  pg.translate(-SIZE/2, SIZE/2, SIZE + 0.1);
  pg.text(getLabel(), 0, 0, 0);
  drawSideIfSelected(pg, "u");
  pg.popMatrix();


  pg.pushMatrix();
  pg.rotateX(-PI/2);
  pg.translate(-SIZE/2, -SIZE/2, 0.1);
  pg.text(getLabel(), 0, 0, 0);
  drawSideIfSelected(pg, "d");
  pg.popMatrix();




  // pg.text(getLabel(), -SIZE/2, -SIZE/2, -0.1);

  // pg.pushMatrix();
  // pg.rotateX(PI/2);
  // pg.text(getLabel(), SIZE/2, SIZE/2, -0.1);
  // pg.rotateX(PI/2);
  // pg.text(getLabel(), SIZE/2, SIZE/2, -SIZE -0.1);
  // pg.rotateX(PI/2);
  // pg.text(getLabel(), SIZE/2, -SIZE + SIZE/2, -SIZE -0.1);
  // pg.popMatrix();

  // pg.pushMatrix();
  // pg.rotateY(PI/2);
  // pg.text(getLabel(), -SIZE + SIZE/2, -SIZE/2, -0.1);
  // pg.popMatrix();

  // pg.pushMatrix();
  // pg.rotateY(- PI/2);
  // pg.text(getLabel(), SIZE/2, -SIZE/2, -SIZE -0.1);
  // pg.popMatrix();

  pg.popMatrix();
  }
}

CVCube previous = null;


class MappedCubeItem extends UIItemList.AbstractItem {
  final CVCube cube;

  MappedCubeItem(CVCube _cube) {
    this.cube = _cube;
  }

  String getLabel() {
    return cube.getLabel();
  }

  // boolean isSelected() { 
  //     return false;
  // }

  // @Override
  // public int getActiveColor(UI ui) {
  //     return isSelected() ? ui.theme.getPrimaryColor() : ui.theme.getSecondaryColor();
  // }

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

    // setReorderable(true);
    // setMomentary(true);

  }

  @Override
    void onKeyPressed(KeyEvent keyEvent, 
    char keyChar, 
    int keyCode) {

    MappedCubeItem item = (MappedCubeItem)getFocusedItem();
    if (item == null) return;

    CVCube cube = item.cube;

    if (keyCode == BACKSPACE) {
      automappingController.removeCube(cube.id);
      return;
    }

    String dirs = "udlrbf";

    if (dirs.indexOf(keyChar) != -1) {
      automappingController.rotateCubes(keyChar);
      return;
    }


    super.onKeyPressed(keyEvent, keyChar, keyCode);

    // if (keyCode == UP) {
    //   setFocusIndex(max(0, cube.listIndex - 1));
    // }

    // if (keyCode == DOWN) {
    //   setFocusIndex(min(automappingController.mappedCubes.size() - 1, cube.listIndex + 1));
    // }
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

  int buildStartMappingButton(float yOffset, float w) {
    final String disconnected = "No App Connected";
    final String connected = "Start Mapping";
    final String inProgress = "Mapping in Progress";

    int h = 18;

    int margin = 3;
    float buttonW = (w - (8 + margin)) / 2;

    final UIButton startMapping = new UIButton(0, yOffset, buttonW, h) {
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

    new UIButton(buttonW + margin, yOffset, buttonW, h) {
      @Override
        protected void onToggle(boolean active) {
        if (!active) return;

        SLStudio.UI sUI = ((SLStudio)lx).ui;

        sUI.preview.setCenter(0, 0, 0);
        sUI.preview.setPhi(0);
        sUI.preview.setPerspective(0);
        sUI.preview.setTheta(0);

        automappingController.center();
      }
    }
    .setLabel("Center Cubes")
      .setMomentary(true)
      .addToContainer(this);

    return h;
  }

  int buildMappedCubeList(float yOffset, float w) {

    int h = 78;

    final List<UIItemList.Item> items = new ArrayList<UIItemList.Item>();
    final MappedCubeList outputList = new MappedCubeList(ui, 0, yOffset, w-8, h);


    for (CVCube c : automappingController.mappedCubes) { 
      items.add(new MappedCubeItem(c));
    }


    outputList.setItems(items).setSingleClickActivate(true);
    outputList.addToContainer(this);

    final Runnable update = new Runnable() {
      public void run() {
        final List<UIItemList.Item> localItems = new ArrayList<UIItemList.Item>();
        int i = 0;
        for (CVCube c : automappingController.mappedCubes) { 
          localItems.add(new MappedCubeItem(c));
        }
        outputList.setItems(localItems);
        redraw();
        // UIItemList.Item item = outputList.getFocusedItem();
        // if (item != null) {
        //   item.onActivate();
        // }
      }
    };


    automappingController.mappedCubes.addListener(new ListListener<CVCube>() {
      public void itemAdded(final int index, final CVCube c) {
        dispatcher.dispatchUi(update);
      }
      public void itemRemoved(final int index, final CVCube c) {
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
  final LX lx;

  private LXChannel mappingChannel = null;
  private LXPattern mappingPattern = null;

  int timesCalled = 0;


  private final BooleanParameter enabled = new BooleanParameter("Enabled");
  private final EnumParameter<AutomappingState> state = new EnumParameter<AutomappingState>("State", AutomappingState.RUNNING);
  private final StringParameter saveFile = new StringParameter("Save File", "cube_transforms.json");

  private String[] macAddresses = null;
  private int[] pixelOrder = null;
  public ClientCommunicator communicator;


  public ListenableList<CVCube> mappedCubes;


  PatternState patternState;
  PatternMode mode = PatternMode.ALL_OFF;


  int baseColor = LXColor.WHITE;
  int resetFrameBaseColor = LXColor.scaleBrightness(LXColor.WHITE, 1);

  int NUM_RUNTHROUGHS = 1;
  int NUM_CALIBRATION_RUNTHROUGHS = -1;

  int RESET_FRAMES = 100;
  int RESET_BLACK_FRAMES = 16;
  int S0_FRAMES = 4;
  int S1_FRAMES = 4;
  int S2_FRAMES = 4;
  int S3_FRAMES = 4;

  int numPoints = 0;
  int runthroughCount = -1;
  int patternPixelIndex = 0;
  int frameCounter = 0;
  int showPixelIndex = 0;
  String currentCubeId = null;

  final JLabel label = new JLabel();




  AutomappingController(LX lx) {
    this.lx = lx;

    addParameter(enabled);
    addParameter(state);

    mappedCubes = new ListenableList<CVCube>();

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
    }
    );
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
    CVCube selected = null;
    for (CVCube cube : mappedCubes) {
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
      case 'u': break;
      case 'd': rot.rotateX(PI); break;
      case 'l': rot.rotateZ(PI/2); break;
      case 'r': rot.rotateZ(-PI/2); break;
      case 'b': rot.rotateX(PI/2); break;
      case 'f': rot.rotateX(-PI/2); break;
      default: throw new RuntimeException("Invalid direction in rotate");

    }

    for (CVCube cube : mappedCubes) {
      cube.mat.preApply(inv);
      cube.mat.preApply(rot);
    }
  }

  void center() {
    if (mappedCubes.size() == 0) {
      return;
    }

    PVector sum = new PVector(0, 0, 0);
    for (CVCube c : mappedCubes) {
      PVector t = new PVector(c.mat.m03, c.mat.m13, c.mat.m23);
      sum.add(t);
    }
    sum.div(mappedCubes.size());

    for (CVCube c : mappedCubes) {
      c.mat.m03 -= sum.x;
      c.mat.m13 -= sum.y;
      c.mat.m23 -= sum.z;
    }
  }

  void addCube(ArrayList<ArrayList<Double>> matrix, ArrayList<Double> rvec,  ArrayList<Double> tvec, String id) {
    SLStudio.UI sUI = ((SLStudio)lx).ui;

    CVCube c = new CVCube(matrix, rvec, tvec, id);

    mappedCubes.add(c);
    Collections.sort(mappedCubes.list);

    sUI.preview.addComponent(c);
  }



  void removeCube(String id) {
    CVCube cube = null;
    int cubeIndex = -1;
    for (int i = 0; i < mappedCubes.size(); i++) {
      CVCube temp = mappedCubes.get(i);
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

    mappedCubes.remove(cubeIndex);
  }

  boolean alreadyMapped(String id) {
    for (CVCube c : mappedCubes) {
      if (id.equals(c.id)) {
        return true;
      }
    }
    return false;
  }



  ArrayList<Object> getMappedTransforms() {
    ArrayList<Object> transforms = new ArrayList<Object>();
    for (CVCube c : mappedCubes) {
      transforms.add(c.getRawMatrix());
    }
    return transforms;
  }

  ArrayList<String> getMappedIds() {
    ArrayList<String> ids = new ArrayList<String>();
    for (String id : macAddresses) {
      if (alreadyMapped(id)) {
        ids.add(id);
      }
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

  CVCube getMappedCube(String id) {
    for (CVCube c : mappedCubes) {
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
    for (CVCube c : mappedCubes) {
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
        patternPixelIndex = (patternPixelIndex+1) % 180;
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
    AutomappingState s = state.getEnum();

    if (s != AutomappingState.RUNNING) {


      CVCube c = getMappedCube(cubeId);
      if (c == null) {
        return s == AutomappingState.DISCONNECTED ? color(20, 0, 0) : color(20, 20, 20);
      }

      if (c.selected && c.flashOn) {
        return LXColor.WHITE;
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
