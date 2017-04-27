enum Direction {DOWN, UP, LEFT, RIGHT, BACK, FORWARD}; 
enum AudioType { BASS, MID, TREBLE}
enum CursorState {
    RUNNING,
    STOPPING,
    STOPPED
}

enum AutomappingEffectMode {
    AUTOMAPPING_PATTERN,
    SHOW_PIXEL,
    ALL_ON,
    ALL_OFF,
    CALIBRATION,
}

class UIAutomapping extends UICollapsibleSection {
  UIAutomapping(LX lx, UI ui, float x, float y, float w) {
      super(ui, x, y, w, 20);
      setTitle("AUTOMAPPING");
      setTitleX(20);

      addTopLevelComponent(new UIButton(4, 4, 12, 12) {
        @Override
        public void onToggle(boolean isOn) {
          redraw();
        }
      }.setParameter(automappingController.start).setBorderRounding(4));
  }
}

class AutomappingController {
  final LX lx;
  final AutomappingEffect automappingEffect;

  private final BooleanParameter start = new BooleanParameter("Start");

  private boolean running = false;
  private String[] macAddresses = null;
  private int[] pixelOrder = null;

  AutomappingController(LX lx) {
    this.lx = lx;
    automappingEffect = new AutomappingEffect(lx);
    lx.addEffect(automappingEffect);

    start.addListener(new LXParameterListener() {
      public void onParameterChanged(LXParameter parameter) {
        if (start.isOn()) {
          startMapping();
          showAutomappingPattern();
        } else {
          disableAutomapping();
        }
      }
    });

    automappingEffect.enabled.addListener(new LXParameterListener() {
      public void onParameterChanged(LXParameter parameter) {
        if (automappingEffect.enabled.isOn() != start.isOn()) {
          start.setValue(automappingEffect.enabled.isOn());
        }
      }
    });

    //moduleRegistrar.modules.add(new Module("Automap", start));
  }

  void setPixelOrder(int[] pixelOrder) {
    this.pixelOrder = pixelOrder;
  }

  int[] getPixelOrder() {
    return pixelOrder;
  }

  boolean isRunning() {
    return running;
  }

  void startCalibration() {
    automappingEffect.startCalibration();
  }

  void startMapping() {
    running = true;
    println("networkMonitor.networkDevices.size(): "+networkMonitor.networkDevices.size());
    macAddresses = new String[networkMonitor.networkDevices.size()];
    int i = 0;
    for (NetworkDevice device : networkMonitor.networkDevices) {
      macAddresses[i++] = device.macAddress.toString();
    }
    automappingEffect.init(macAddresses);
  }

  void nextCube() {
    automappingEffect.enabled.setValue(true);
    automappingEffect.nextCube();
  }

  String[] getMacAddresses() {
    return macAddresses;
  }

  void showAutomappingPattern() {
    // if (!start.isOn()) {
    //   start.setValue(true);
    // }
    automappingEffect.enabled.setValue(true);
    automappingEffect.mode = AutomappingEffectMode.AUTOMAPPING_PATTERN;
    automappingEffect.resetPattern();
  }

  void showImageForPixel(int pixelIndex) {
    automappingEffect.enabled.setValue(true);
    automappingEffect.mode = AutomappingEffectMode.SHOW_PIXEL;
    automappingEffect.pixelIndex = pixelIndex;
  }

  void showAll(boolean on) {
    automappingEffect.enabled.setValue(true);
    automappingEffect.mode = on ? AutomappingEffectMode.ALL_ON : AutomappingEffectMode.ALL_OFF;
  }

  void disableAutomapping() {
    running = false;
    macAddresses = null;
    automappingEffect.deinit();
    // if (start.isOn()) {
    //   start.setValue(false);
    // }
    automappingEffect.enabled.setValue(false);
  }
}

class AutomappingEffect extends SLEffect {

  AutomappingEffectMode mode;
  int pixelIndex;

  int baseColor = LXColor.WHITE;
  int resetFrameBaseColor = LXColor.scaleBrightness(LXColor.WHITE, 1);

  int NUM_RUNTHROUGHS = 1;
  int NUM_CALIBRATION_RUNTHROUGHS = -1;

  int S0_IDENTIFY = 0;
  int S1_BLACK = 1;
  int S2_WHITE = 2;
  int S3_BLACK = 3;
  int STATE_END = 4;

  int RESET_FRAMES = 100;
  int RESET_BLACK_FRAMES = 16;
  int S0_FRAMES = 12;
  int S1_FRAMES = 12;
  int S2_FRAMES = 12;
  int S3_FRAMES = 12;

  int numPoints = 0;
  int runthroughCount = -1;
  int patternPixelIndex = 0;
  int cubeIndex = 0;
  int patternState = 0;
  int frameCounter = 0;

  private String[] macAddresses = null;
  int[] colors = null;
  int globalColor = LXColor.BLACK;

  AutomappingEffect(LX lx) {
    super(lx);
  }

  void resetPattern() {
    runthroughCount = -1;
    patternPixelIndex = 0;
    patternState = S1_BLACK;
    frameCounter = 0;
    cubeIndex = 0;
  }

  void init(String[] macAddresses) {
    enabled.setValue(true);
    this.macAddresses = macAddresses;
    numPoints = macAddresses.length * 15 * 12;
    colors = new int[numPoints];
    for (int i = 0; i < colors.length; i++) {
      colors[i] = LXColor.BLACK;
    }
    resetPattern();
  }

  void deinit() {
    enabled.setValue(false);
    macAddresses = null;
    colors = null;
  }

  void startCalibration() {
    enabled.setValue(true);
    mode = AutomappingEffectMode.CALIBRATION;
    patternState = S1_BLACK;
    frameCounter = 0;
    colors = null;
    globalColor = LXColor.BLACK;
  }

  void nextCube() {
    enabled.setValue(true);
    cubeIndex++;
    runthroughCount = -1;
    patternPixelIndex = 0;
    frameCounter = 0;
    patternState = S1_BLACK;
  }

  protected void run(double deltaMs, double amount) {
    if (!enabled.isOn()) return;

    if (mode == AutomappingEffectMode.CALIBRATION) {
      if (NUM_CALIBRATION_RUNTHROUGHS >= 0 && runthroughCount >= NUM_CALIBRATION_RUNTHROUGHS) {
        globalColor = LXColor.BLACK;
        enabled.setValue(false);
        return;
      }
      if (patternState == S1_BLACK) {
        globalColor = LXColor.BLACK;
        frameCounter = (frameCounter+1) % S1_FRAMES;
      } else if (patternState == S2_WHITE) {
        globalColor = resetFrameBaseColor;
        frameCounter = (frameCounter+1) % S2_FRAMES;
        if (frameCounter == 0) {
          runthroughCount++;
        }
      }
      if (frameCounter == 0) {
        patternState = patternState == S2_WHITE ? S1_BLACK : S2_WHITE;
      }
    } else if (mode == AutomappingEffectMode.AUTOMAPPING_PATTERN) {
      if (NUM_RUNTHROUGHS >= 0 && runthroughCount >= NUM_RUNTHROUGHS) {
        setColorsAutoMap(LXColor.BLACK);
        enabled.setValue(false);
        return;
      }
      if (patternState == S1_BLACK) {
        setColorsAutoMap(LXColor.BLACK);
        frameCounter = (frameCounter+1) % S1_FRAMES;
      } else if (patternState == S2_WHITE) {
        setColorsAutoMap(resetFrameBaseColor);
        frameCounter = (frameCounter+1) % (patternPixelIndex == 0 ? RESET_FRAMES : S2_FRAMES);
        if (frameCounter == 0 && patternPixelIndex == 0) {
          runthroughCount++;
        }
      } else if (patternState == S3_BLACK) {
        setColorsAutoMap(LXColor.BLACK);
        frameCounter = (frameCounter+1) % (patternPixelIndex == 0 ? RESET_BLACK_FRAMES : S3_FRAMES);
      } else {
        setColorsAutoMap(LXColor.BLACK);

        int pixelIndexInCube = automappingController.getPixelOrder()[patternPixelIndex];
        int adjustedPixelIndex = pixelIndexInCube + cubeIndex*180;
        colors[adjustedPixelIndex] = baseColor;

        frameCounter = (frameCounter+1) % S0_FRAMES;

        if (frameCounter == 0) {
          patternPixelIndex = (patternPixelIndex+1) % 180;
        }
      }
      if (frameCounter == 0) {
        patternState = (patternState+1) % STATE_END;
      }
    } else {
      switch (mode) {
        case SHOW_PIXEL:
          setColorsAutoMap(LXColor.BLACK);
          setColorAutoMap(pixelIndex, LXColor.WHITE);
          break;
        case ALL_ON:
          setColorsAutoMap(LXColor.WHITE);
          break;
        case ALL_OFF:
          setColorsAutoMap(LXColor.BLACK);
          break;
        default:
          break;
      }
    }
  }

  void setColorsAutoMap(int colr) {
    for (int i = 0; i < colors.length; i++) {
      colors[i] = colr;
    }
  }

  void setColorAutoMap(int index, int colr) {
    colors[index] = colr;
  }
}

class AutomappingPattern extends SLPattern {

  int baseColor = LXColor.WHITE;//LXColor.hsb(0, 100, 5);
  int resetFrameBaseColor = LXColor.scaleBrightness(LXColor.WHITE, 1);

  int NUM_RUNTHROUGHS = 100;

  int S0_IDENTIFY = 0;
  int S1_BLACK = 1;
  int S2_WHITE = 2;
  int S3_BLACK = 3;
  int STATE_END = 4;

  int RESET_FRAMES = 600;
  int RESET_BLACK_FRAMES = 60;
  int S0_FRAMES = 6;
  int S1_FRAMES = 5;
  int S2_FRAMES = 5;
  int S3_FRAMES = 7;

  int pixelIndex = 0;
  int state = 0;
  int frameCounter = 0;
  int numPoints = 0;
  int runthroughCount = -1;

  AutomappingPattern(LX lx) {
    super(lx);
    numPoints = model.strips.size() * 15;
    pixelIndex = numPoints * 3 / 4;
    // println("numPoints: "+numPoints);
  }

  public void run(double deltaMs) {
    if (runthroughCount == NUM_RUNTHROUGHS) {
      setColors(LXColor.BLACK);
      return;
    }
    if (state == S1_BLACK) {
      setColors(LXColor.BLACK);
      frameCounter = (frameCounter+1) % S1_FRAMES;
    } else if (state == S2_WHITE) {
      setColors(resetFrameBaseColor);
      frameCounter = (frameCounter+1) % (pixelIndex == 0 ? RESET_FRAMES : S2_FRAMES);
      if (frameCounter == 0 && pixelIndex == 0) {
        runthroughCount++;
      }
    } else if (state == S3_BLACK) {
      setColors(LXColor.BLACK);
      frameCounter = (frameCounter+1) % (pixelIndex == 0 ? RESET_BLACK_FRAMES : S3_FRAMES);
    } else {
      setColors(LXColor.BLACK);
      if (frameCounter == 0) {
        int stripIndex = pixelIndex / 15;
        int pixelIndexInStrip = pixelIndex % 15;
        int index = stripIndex * 16 + pixelIndexInStrip;
        colors[index] = baseColor;

        pixelIndex = (pixelIndex+1) % numPoints;
      }
      frameCounter = (frameCounter+1) % S0_FRAMES;
    }
    if (frameCounter == 0) {
      state = (state+1) % STATE_END;
    }
  }

}

class CandyCloud extends SLPattern {

  final BoundedParameter darkness = new BoundedParameter("DARK", 6, 0, 12);

  final BoundedParameter scale = new BoundedParameter("SCAL", 2800, 200, 10000);
  final BoundedParameter speed = new BoundedParameter("SPD", 1, 1, 2);

  double time = 0;

  CandyCloud(LX lx) {
    super(lx);

    addParameter(darkness);
    addParameter(scale);
    addParameter(speed);
  }

  public void run(double deltaMs) {
    time += deltaMs;
    for (LXPoint p : model.points) {
      double adjustedX = p.x / scale.getValue();
      double adjustedY = p.y / scale.getValue();
      double adjustedZ = p.z / scale.getValue();
      double adjustedTime = time * speed.getValue() / 5000;

      float hue = ((float)SimplexNoise.noise(adjustedX, adjustedY, adjustedZ, adjustedTime) + 1) / 2 * 1080 % 360;

      float brightness = min(max((float)SimplexNoise.noise(p.x / 250, p.y / 250, p.z / 250 + 10000, time / 5000) * 8 + 8 - darkness.getValuef(), 0), 1) * 100;
      
      colors[p.index] = lx.hsb(hue, 100, brightness);
    }
  }
}

class SolidColor extends SLPattern {

  final BoundedParameter hue = new BoundedParameter("HUE", 0, 0, 360);
  final BoundedParameter saturation = new BoundedParameter("SATUR", 100, 0, 100);
  final BoundedParameter brightness = new BoundedParameter("BRIGHT", 25, 0, 100);

  SolidColor(LX lx) {
    super(lx);

    // hue.setValue(random(360));

    addParameter(hue);
    addParameter(saturation);
    addParameter(brightness);
  }

  public void run(double deltaMs) {
    setColors(lx.hsb(hue.getValuef(), saturation.getValuef(), brightness.getValuef()));
  }
}