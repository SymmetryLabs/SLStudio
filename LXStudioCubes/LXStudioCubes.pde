import java.util.*;
import java.net.*;
import java.lang.reflect.*;
import java.text.DecimalFormat;

public SLStudio lx;
public SLModel model;
public Dispatcher dispatcher;
public NetworkMonitor networkMonitor;
public OutputControl outputControl;
public MappingMode mappingMode = null;
public AppServer appServer;
public AutomappingController automappingController;

final boolean MAPPING_MODE = true;


// public boolean envelopOn = false;
// public Envelop envelop = null;

void setup() {
  long setupStart = System.nanoTime();
  size(1280, 800, P3D);

  // Automapping
  automappingController = new AutomappingController(lx);

  model = buildModel();
  println("-- Model ----");
  println("# of cubes: " + model.cubes.size());
  println("# of points: " + model.points.length);
  println("model.xMin: " + model.xMin); println("model.xMax: " + model.xMax); println("model.xRange: " + model.xRange);
  println("model.yMin: " + model.yMin); println("model.yMax: " + model.yMax); println("model.yRange: " + model.yRange);
  println("model.zMin: " + model.zMin); println("model.zMax: " + model.zMax); println("model.zRange: " + model.zRange + "\n");

  lx = new SLStudio(this, model) {
    @Override
    protected void initialize(SLStudio lx, SLStudio.UI ui) {
      automappingController.lx = lx;
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
      if (((SLModel)model).cubes.size() > 0)
        mappingMode = new MappingMode(lx);


    //   HashMap<String, LXPoint[]> debugs = new HashMap();


      Cube mod = new Cube(
        "no",
        0,
        0,
        0,
        0,
        0,
        0,
        new LXTransform(),
        Cube.Type.LARGE
      );



      println("PVector[] fromLX = {");
      int i = 0;
      for (LXPoint p : mod.points) {
        System.out.printf("new PVector(%.5f, %.5f, %.5f)", p.x, p.y, p.z);
        if (i < mod.points.length - 1) {
          System.out.printf(",\n");
        }
        i++;
      }
      i = 0;
      println("};");
    //   LXPoint[] kP = automappingController.getRawPointsForId("SMALLBOI");
    //   debugs.put("kyle_model", kP);

    //   for (LXPoint p : kP) {
    //     System.out.printf("new PVector(%.5f, %.5f, %.5f)", p.x, p.y, p.z);
    //     if (i < kP.length - 1) {
    //       System.out.printf(",\n");
    //     }
    //     i++;
    //   }
    //   println("};");

    // Gson gson = new GsonBuilder().setPrettyPrinting().create();


    // saveBytes(dataPath("model_points.json"), gson.toJson(debugs).getBytes());

      // Setup server listeners
      //    Adaptor for mapping osc messages from Essentia to lx osc engine
      //    TCP server for iOS Automapper
      try {
        lx.engine.osc.receiver(1331).addListener(new EssentiaOSCListener(lx));
        appServer = new AppServer(lx);
      } catch (SocketException sx) {
        javax.swing.JOptionPane.showMessageDialog(null, "Looks like you're already running LXStudioCubes. Quitting now.");
        exit();
        // throw new RuntimeException(sx);
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
      ui.preview.setPhi(0).setMinRadius(2*FEET).setMaxRadius(48*FEET).setRadius(30*FEET);

      new UISpeed(ui, lx, 0, 0, ui.leftPane.global.getContentWidth()).addToContainer(ui.leftPane.global, 1);

      //new UIOutputs(lx, ui, 0, 0, ui.leftPane.global.getContentWidth()).addToContainer(ui.leftPane.global, 3);
      
      // if (((SLModel)model).cubes.size() > 0)
      //   new UIMapping(lx, ui, 0, 0, ui.leftPane.global.getContentWidth()).addToContainer(ui.leftPane.global, 4);

      // if (envelopOn) {
      //   new UIEnvelopSource(ui, 0, 0, ui.leftPane.global.getContentWidth()).addToContainer(ui.leftPane.global, 5);
      //   new UIEnvelopDecode(ui, 0, 0, ui.leftPane.global.getContentWidth()).addToContainer(ui.leftPane.global, 6);
      // }



    }
  };

  // lx.engine.audio.enabled.setValue(true);

  //    LXPattern mappingPattern = new Bubbles(lx);
  //   LXChannel mappingChannel = lx.engine.addChannel(new LXPattern[] {mappingPattern});

  //   for (LXChannel channel : lx.engine.channels)
  //     channel.cueActive.setValue(false);

  //   mappingChannel.fader.setValue(1);
  //   mappingChannel.label.setValue("Bubbles");
  //   mappingChannel.cueActive.setValue(true);


  long setupFinish = System.nanoTime();
  println("Initialization time: " + ((setupFinish - setupStart) / 1000000) + "ms"); 
}

void draw() {
  background(lx.ui.theme.getDarkBackgroundColor());
}