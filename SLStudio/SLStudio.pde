import java.util.*;
import java.net.*;
import java.lang.reflect.*;
import java.text.DecimalFormat;
import heronarts.p3lx.ui.studio.modulation.UIModulator;

public PApplet applet;
public LXStudio lx;
public SLModel model;
public Dispatcher dispatcher;
public NetworkMonitor networkMonitor;
public OutputControl outputControl;

void setup() {
  long setupStart = System.nanoTime();
  size(displayWidth, displayHeight, P3D);
  applet = this;

  model = buildModel();
  println("-- Model ----");
  println("# of suns: " + model.suns.size());
  println("# of slices: " + model.slices.size());
  println("# of strips: " + model.strips.size());
  println("# of points: " + model.points.length);
  println("model.xMin: " + model.xMin); println("model.xMax: " + model.xMax); println("model.xRange: " + model.xRange);
  println("model.yMin: " + model.yMin); println("model.yMax: " + model.yMax); println("model.yRange: " + model.yRange);
  println("model.zMin: " + model.zMin); println("model.zMax: " + model.zMax); println("model.zRange: " + model.zRange + "\n");

  lx = new LXStudio(this, model, false) {
    @Override
    protected void initialize(LXStudio lx, LXStudio.UI ui) {
      
      // Output
      (dispatcher = new Dispatcher(lx)).start();
      (networkMonitor = new NetworkMonitor(lx)).start();
      setupGammaCorrection();
      setupGammaCorrection();
      setupOutputs(lx);
      outputControl = new OutputControl(lx);
      lx.engine.registerComponent("outputControl", outputControl);
        
      lx.registerPatterns(new Class[]{
        heronarts.p3lx.pattern.SolidColorPattern.class,
        IteratorTestPattern.class
      });
      lx.registerEffects(new Class[]{ 
        FlashEffect.class,
        BlurEffect.class,
        DesaturationEffect.class
      });
    
      ui.theme.setPrimaryColor(#008ba0);
      ui.theme.setSecondaryColor(#00a08b);
      ui.theme.setAttentionColor(#a00044);
      ui.theme.setFocusColor(#0094aa);
      ui.theme.setControlBorderColor(#292929);
    } 
    
    @Override
    protected void onUIReady(LXStudio lx, LXStudio.UI ui) {
      ui.leftPane.audio.setVisible(true);
      ui.preview.setPhi(0).setMinRadius(0*FEET).setMaxRadius(150*FEET).setRadius(150*FEET);

      new UISpeed(ui, lx, 0, 0, ui.leftPane.global.getContentWidth()).addToContainer(ui.leftPane.global, 1);
    }
  };

  lx.engine.audio.enabled.setValue(true);

  long setupFinish = System.nanoTime();
  println("Initialization time: " + ((setupFinish - setupStart) / 1000000) + "ms"); 
}

void draw() {
  background(lx.ui.theme.getDarkBackgroundColor());
}