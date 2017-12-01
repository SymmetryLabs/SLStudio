import java.util.*;
import java.net.*;
import java.lang.reflect.*;
import java.text.DecimalFormat;
import heronarts.p3lx.ui.studio.modulation.UIModulator;

import com.symmetrylabs.util.PackageUtils;

public PApplet applet;
public LXStudio lx;
public Dispatcher dispatcher;
public NetworkMonitor networkMonitor;
public OutputControl outputControl;
public Pixlite[] pixlites;
public SkyPaletteLibrary skyPalettes;
public BlobTracker blobTracker;

public DiscreteParameter selectedStrip = new DiscreteParameter("selectedStrip", 1, 70);

void setup() {
  long setupStart = System.nanoTime();
  size(displayWidth, displayHeight, P3D);
  applet = this;

  SLModel model = buildModel();

  println("-- Model ----");
  println("# of suns: " + model.suns.size());
  println("# of slices: " + model.slices.size());
  println("# of strips: " + model.strips.size());
  println("# of points: " + model.points.length);
  println("model.xMin: " + model.xMin); println("model.xMax: " + model.xMax); println("model.xRange: " + model.xRange);
  println("model.yMin: " + model.yMin); println("model.yMax: " + model.yMax); println("model.yRange: " + model.yRange);
  println("model.zMin: " + model.zMin); println("model.zMax: " + model.zMax); println("model.zRange: " + model.zRange + "\n");

  skyPalettes = new SkyPaletteLibrary();
  skyPalettes.addSky("london", new DeckChairSource("5568230b7b2853502527fd4e"), new ArcPaletteExtractor(0.44, 100));
  skyPalettes.addSky("paris", new DeckChairSource("5568862a7b28535025280c72"), new ArcPaletteExtractor(0.46, 100));
  skyPalettes.addSky("sydney", new DeckChairSource("599d6375096641f2272bacf4"), new ArcPaletteExtractor(1, 100));
  skyPalettes.addSky("san francisco", new UrlImageSource("http://icons.wunderground.com/webcamramdisk/a/m/ampledata/1/current.jpg"), new ArcPaletteExtractor(0.65, 100));
  skyPalettes.addSky("sunset sunset", new UrlImageSource("https://pbs.twimg.com/media/DO9Ok2JU8AEjXa1.jpg"), new ArcPaletteExtractor(0.622, 100));
  skyPalettes.addSky("orange sunset", new UrlImageSource("https://c.pxhere.com/photos/0e/29/sunrise_beach_sea_ocean_water_sunset_sky_sun-1332581.jpg!d"), new ArcPaletteExtractor(0.5, 100));
  skyPalettes.addSky("galaxies", new UrlImageSource("https://apod.nasa.gov/apod/image/1711/BeltStars_nouroozi2000.jpg"), new ArcPaletteExtractor(0.9, 1000));
  
  lx = new LXStudio(this, model, false) {
    @Override
    protected void initialize(LXStudio lx, LXStudio.UI ui) {

      for (Class<? extends LXPattern> c : PackageUtils.getPatternClassesInPackage("com.symmetrylabs")) {
        lx.registerPattern(c);
      }

      // Output
      (dispatcher = new Dispatcher(lx)).start();
      (networkMonitor = new NetworkMonitor(lx)).start();
      setupGammaCorrection();
      setupGammaCorrection();

      outputControl = new OutputControl(lx);
      lx.engine.registerComponent("outputControl", outputControl);

      pixlites = setupPixlites(lx);
      setupOutputs(lx);
        
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
  blobTracker = new BlobTracker(lx);

  long setupFinish = System.nanoTime();
  println("Initialization time: " + ((setupFinish - setupStart) / 1000000) + "ms"); 
}

void draw() {
  background(lx.ui.theme.getDarkBackgroundColor());
  //DrawHelper.runAll();
  dispatcher.draw();
}