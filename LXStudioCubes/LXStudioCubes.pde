import java.util.*;
import java.net.*;
import java.lang.reflect.*;
import java.text.DecimalFormat;

final static float INCHES = 1;
final static float FEET = 12*INCHES;

// //midiMinVar = 21, midiMaxVar = 108, so 88 in total
// int[] r = new int[108]; //setup arrays for R,G & B values - could be a single array of COLOR objects instead
// int[] g = new int[108];
// int[] b = new int[108];

public SLStudio lx;
public CubesModel model;
public Dispatcher dispatcher;
public NetworkMonitor networkMonitor;
public OutputControl outputControl;
public DeviceController deviceController;
public MappingMode mappingMode = null;

// public boolean envelopOn = false;
// public Envelop envelop = null;

void setup() {
  long setupStart = System.nanoTime();
  size(1280, 800, P3D);

  int switchModel = 1;

  LXModel currModel;
  if (switchModel == 0) {
    model = buildModel();
    currModel = model;
  } else {
    model = new CubesModel();
    currModel = new DeviceModel();
  }

  println("-- Model ----");
  println("# of cubes: " + model.cubes.size());
  println("# of points: " + model.points.length);
  println("model.xMin: " + model.xMin); println("model.xMax: " + model.xMax); println("model.xRange: " + model.xRange);
  println("model.yMin: " + model.yMin); println("model.yMax: " + model.yMax); println("model.yRange: " + model.yRange);
  println("model.zMin: " + model.zMin); println("model.zMax: " + model.zMax); println("model.zRange: " + model.zRange + "\n");

  //SLStudio constructor calls super(applet, model)
  lx = new SLStudio(this, currModel) {
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

      for (Class<? extends LXPattern> c : PackageUtils.getPatternClassesInPackage("com.symmetrylabs")) {
        lx.registerPattern(c);
      }

      lx.engine.framesPerSecond.setValue(120);

      // Output
      (dispatcher = new Dispatcher(lx)).start();
      (networkMonitor = new NetworkMonitor(lx)).start();
      setupGammaCorrection();
      setupGammaCorrection();
      setupOutputs(lx);
      outputControl = new OutputControl(lx);
      //outputControl.setValue();
      lx.engine.registerComponent("outputControl", outputControl);

      // Mapping
      // if (((CubesModel)model).cubes.size() > 0)
      //   mappingMode = new MappingMode(lx);
      mappingMode = new MappingMode(lx);

      deviceController = new DeviceController(lx);

      // Adaptor for mapping osc messages from Essentia to lx osc engine
      try {
        lx.engine.osc.receiver(1331).addListener(new EssentiaOSCListener(lx));
      } catch (SocketException sx) {
        throw new RuntimeException(sx);
      }

      // Adaptor for mapping osc messages from Agents/wearables to lx osc engine
      try {
        lx.engine.osc.receiver(5005).addListener(new DeviceOSCListener());
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
      ui.preview.setPhi(0).setMinRadius(2*FEET).setMaxRadius(48*FEET).setRadius(30*FEET);

      new UISpeed(ui, lx, 0, 0, ui.leftPane.global.getContentWidth()).addToContainer(ui.leftPane.global, 1);

      //new UIOutputs(lx, ui, 0, 0, ui.leftPane.global.getContentWidth()).addToContainer(ui.leftPane.global, 3);

      // if (((CubesModel)model).cubes.size() > 0)
      //   new UIMapping(lx, ui, 0, 0, ui.leftPane.global.getContentWidth()).addToContainer(ui.leftPane.global, 4);

      // if (envelopOn) {
      //   new UIEnvelopSource(ui, 0, 0, ui.leftPane.global.getContentWidth()).addToContainer(ui.leftPane.global, 5);
      //   new UIEnvelopDecode(ui, 0, 0, ui.leftPane.global.getContentWidth()).addToContainer(ui.leftPane.global, 6);
      // }

      ui.preview.perspective.setValue(20);

    }
  };

  lx.engine.audio.enabled.setValue(true);

  // LXPattern mappingPattern = new Bubbles(lx);
  //     LXChannel mappingChannel = lx.engine.addChannel(new LXPattern[] {mappingPattern});

  //     for (LXChannel channel : lx.engine.channels)
  //       channel.cueActive.setValue(false);

  //     mappingChannel.fader.setValue(1);
  //     mappingChannel.label.setValue("BUBBLES");
  //     mappingChannel.cueActive.setValue(true);


  // long setupFinish = System.nanoTime();
  // println("Initialization time: " + ((setupFinish - setupStart) / 1000000) + "ms");

  // --------- TONAL SETUP ------------
  // size(400,400);
  // frameRate(25);
  // OscProperties myProperties = new OscProperties();
  // // increase the datagram size to 10000 bytes
  // // by default it is set to 1536 bytes
  // myProperties.setDatagramSize(10000);
  // myProperties.setListeningPort(1331);
  // oscP5 = new OscP5(this,myProperties);

  // --------- END TONAL --------------
}

void draw() {
  background(lx.ui.theme.getDarkBackgroundColor());

}
