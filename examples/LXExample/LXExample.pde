import heronarts.lx.*;
import heronarts.lx.pattern.*;

// These imports are required by the HeronLX library
import processing.video.*;
import ddf.minim.*;

void setup() {
  size(400, 400);
  HeronLX lx = new HeronLX(this, 20, 20);

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
