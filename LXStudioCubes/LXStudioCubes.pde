import java.util.*;
import java.net.*;
import java.lang.reflect.*;
import java.text.DecimalFormat;

public SLStudio lx;
public SLModel model;
public Dispatcher dispatcher;
public NetworkMonitor networkMonitor;
public OutputControl outputControl;
//public MappingMode mappingMode = null;

// public boolean envelopOn = false;
// public Envelop envelop = null;

void setup() {
  long setupStart = System.nanoTime();
  size(1280, 800, P3D);

  model = buildModel();
  println("-- Model ----");
  //println("# of cubes: " + model.cubes.size());
  println("# of points: " + model.points.length);
  println("model.xMin: " + model.xMin); println("model.xMax: " + model.xMax); println("model.xRange: " + model.xRange);
  println("model.yMin: " + model.yMin); println("model.yMax: " + model.yMax); println("model.yRange: " + model.yRange);
  println("model.zMin: " + model.zMin); println("model.zMax: " + model.zMax); println("model.zRange: " + model.zRange + "\n");

  lx = new SLStudio(this, model) {
    @Override
    protected void initialize(SLStudio lx, SLStudio.UI ui) {
      // if (envelopOn) {
      //   envelop = new Envelop(lx);
      //   lx.engine.registerComponent("envelop", envelop);
      //   lx.engine.addLoopTask(envelop);
      //   // OSC drivers
      //   try {
      //     lx.engine.osc.receiver(3344).addListener(new EnvelopOscControlListener(lx));
      //     lx.engine.osc.receiver(3355).addListener(new EnvelopOscSourceListener());
      //     lx.engine.osc.receiver(3366).addListener(new EnvelopOscMeterListener());
      //   } catch (SocketException sx) {
      //     throw new RuntimeException(sx);
      //   } 
      // }

      // Output
      (dispatcher = new Dispatcher(lx)).start();
      (networkMonitor = new NetworkMonitor(lx)).start();
      setupGammaCorrection();
      setupGammaCorrection();
      setupOutputs(lx);
      outputControl = new OutputControl(lx);
      lx.engine.registerComponent("outputControl", outputControl);

      // Mapping
      // if (((SLModel)model).cubes.size() > 0)
      //   mappingMode = new MappingMode(lx);

      // Adaptor for mapping osc messages from Essentia to lx osc engine
      try {
        lx.engine.osc.receiver(1331).addListener(new EssentiaOSCListener(lx));
      } catch (SocketException sx) {
        throw new RuntimeException(sx);
      } 
        
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
    protected void onUIReady(SLStudio lx, SLStudio.UI ui) {
      ui.leftPane.audio.setVisible(true);
      ui.preview.setPhi(0).setTheta(15*PI/8).setMinRadius(2*FEET).setMaxRadius(48*FEET).setRadius(30*FEET);
      //new UIOutputs(lx, ui, 0, 0, ui.leftPane.global.getContentWidth()).addToContainer(ui.leftPane.global, 3);
      
      new UISpeed(ui, lx, 0, 0, ui.leftPane.global.getContentWidth()).addToContainer(ui.leftPane.global, 1);
      
      // if (((SLModel)model).cubes.size() > 0)
      //   new UIMapping(lx, ui, 0, 0, ui.leftPane.global.getContentWidth()).addToContainer(ui.leftPane.global, 4);

      // if (envelopOn) {
      //   new UIEnvelopSource(ui, 0, 0, ui.leftPane.global.getContentWidth()).addToContainer(ui.leftPane.global, 5);
      //   new UIEnvelopDecode(ui, 0, 0, ui.leftPane.global.getContentWidth()).addToContainer(ui.leftPane.global, 6);
      // }
     
    }
  };

  lx.engine.audio.enabled.setValue(true);

  long setupFinish = System.nanoTime();
  println("Initialization time: " + ((setupFinish - setupStart) / 1000000) + "ms"); 
}

void draw() {
  background(lx.ui.theme.getDarkBackgroundColor());
}