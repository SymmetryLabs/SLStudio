import heronarts.lx.*;
import heronarts.lx.pattern.*;

// These imports are required by the LX library
import processing.video.*;
import ddf.minim.*;

void setup() {
  size(400, 400);
  LX lx = new LX(this, 20, 20);
  lx.setSimulationEnabled(true);

  // This would enable remote control over UDP
  // lx.enableClientListener();
  
  lx.setPatterns(new LXPattern[]{
    new LifePattern(lx),
    new BaseHuePattern(lx),
    new GraphicEqualizerPattern(lx),
    new VideoCapturePattern(lx),
    new TouchTestPattern(lx),
  });
}

void draw() {
  // Necessary for Processing to run the render loop
}
