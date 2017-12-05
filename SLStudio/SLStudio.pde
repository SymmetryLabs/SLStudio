import java.util.*;
import java.net.*;
import java.lang.reflect.*;
import java.text.DecimalFormat;
import heronarts.p3lx.ui.studio.modulation.UIModulator;

import com.symmetrylabs.util.PackageUtils;
import com.symmetrylabs.util.BlobTracker;

public PApplet applet;
public LXStudio lx;
public Dispatcher dispatcher;
public NetworkMonitor networkMonitor;
public OutputControl outputControl;
public Pixlite[] pixlites;
public PaletteLibrary paletteLibrary;
public BlobTracker blobTracker;
public SLModel model;

public DiscreteParameter selectedStrip = new DiscreteParameter("selectedStrip", 1, 70);

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

  paletteLibrary = initializePaletteLibrary();
  
  lx = new LXStudio(this, model, true) {
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
      ui.preview.setCenter(model.cx, model.cy, model.cz);
      ui.preview.setPhi(0).setMinRadius(0*FEET).setMaxRadius(150*FEET).setRadius(150*FEET);

      new UISpeed(ui, lx, 0, 0, ui.leftPane.global.getContentWidth()).addToContainer(ui.leftPane.global, 1);
    }
  };

  lx.engine.audio.enabled.setValue(true);
  lx.engine.output.enabled.setValue(false);
  blobTracker = BlobTracker.getInstance(lx);

  long setupFinish = System.nanoTime();
  println("Initialization time: " + ((setupFinish - setupStart) / 1000000) + "ms"); 
}

PaletteLibrary initializePaletteLibrary() {
  PaletteLibrary pl = new PaletteLibrary();

  /* Images loaded remotely from the Internet */
  /*
  pl.set("cities.london", new DeckChairSource("5568230b7b2853502527fd4e"), new ArcPaletteExtractor(0.44, 100));
  pl.set("cities.paris", new DeckChairSource("5568862a7b28535025280c72"), new ArcPaletteExtractor(0.46, 100));
  pl.set("cities.sydney", new DeckChairSource("599d6375096641f2272bacf4"), new ArcPaletteExtractor(1, 100));
  pl.set("cities.san_francisco", new UrlImageSource("http://icons.wunderground.com/webcamramdisk/a/m/ampledata/1/current.jpg"), new ArcPaletteExtractor(0.65, 100));
  pl.set("sunsets.sunset", new UrlImageSource("https://pbs.twimg.com/media/DO9Ok2JU8AEjXa1.jpg"), new ArcPaletteExtractor(0.622, 100));
  pl.set("sunsets.orange", new UrlImageSource("https://c.pxhere.com/photos/0e/29/sunrise_beach_sea_ocean_water_sunset_sky_sun-1332581.jpg!d"), new ArcPaletteExtractor(0.5, 100));
  pl.set("galaxies", new UrlImageSource("https://apod.nasa.gov/apod/image/1711/BeltStars_nouroozi2000.jpg"), new ArcPaletteExtractor(0.9, 1000));
  */

  pl.put("sky.orange", new ZigzagPalette(new int[] {
    0x230402, 0x2d0a06, 0x340b05, 0x3a0b05, 0x3a0501, 0x420602, 0x520701,
    0x7c1103, 0xfe9100, 0xfdc200, 0xfdee00, 0xfdfc00, 0xfefe00, 0xfefb00,
    0xfff507, 0xfde80a, 0xfcd905, 0xfec601, 0xfdbc00, 0xfeb500, 0xfdb000,
    0xfeb000, 0xfdac00, 0xfea700, 0xfda800, 0xfea800, 0xfea700, 0xfda600,
    0xfda100, 0xfe9900, 0xfc8500, 0xfd6a00, 0xfc5000, 0x3b0401, 0x2b0400,
    0x260402, 0x270907, 0x220905, 0x1e0606, 0x200b08, 0x1d0a07
  }));
  
  ImageLibrary il = new ImageLibrary(applet.dataPath("images"));
  PaletteExtractor horiz = new LinePaletteExtractor(0.5);
  PaletteExtractor vert = new LinePaletteExtractor(0.5, 1, 0.5, 0);
  pl.put("sky.purple", new LinePaletteExtractor(0, 0.7, 1, 0.3).getPalette(il.get("maxpixel-cc0-pink-beautiful-sunset-1858600.jpg")));
  pl.put("sky.red", vert.getPalette(il.get("pexels-cc0-animals-birds-dawn-giraffe-417142.jpg")));
  pl.put("sky.cloudy", vert.getPalette(il.get("pixnio-cc0-sunset-sky-mountain-landscape-cloud.jpg")));
  pl.put("sky.green", vert.getPalette(il.get("pexels-cc0-nature-sky-sunset-the-mountains-66997.jpg")));
  pl.put("sky.pastel", new LinePaletteExtractor(0.11, 0.41, 0.5, 0).getPalette(il.get("maxpixel-cc0-Abendstimmung-Landscape-Mountain-Sky-Nature-Lake-1504197.jpg")));
  pl.put("sky.dkgn", new LinePaletteExtractor(0, 1, 0, 0).getPalette(il.get("pexels-cc0-sunset-sunrise-sea-horizon-11434.jpg")));
  pl.put("sky.yellow", new LinePaletteExtractor(0.5, 1, 0.5, 0).getPalette(il.get("pexels-cc0-sunset-sunrise-sea-horizon-11434.jpg")));
  pl.put("land", new LinePaletteExtractor(1, 0.5, 1, 0).getPalette(il.get("maxpixel-cc0-Abendstimmung-Landscape-Mountain-Sky-Nature-Lake-1504197.jpg")));
  pl.put("neon", horiz.getPalette(il.get("pixabay-cc0-neon-art-color-colorful-light-1596205.jpg")));
  pl.put("lake", new LinePaletteExtractor(0, 1, 1, 0).getPalette(il.get("maxpixel-cc0-Sky-Lake-Luener-Lake-Water-Blue-Mirroring-Clouds-475819.jpg")));
  pl.put("scotland", new LinePaletteExtractor(0, 1, 0.6, 0).getPalette(il.get("maxpixel-cc0-Elgol-Coast-Scotland-Stones-Lake-Isle-Of-Skye-540123.jpg")));
  return pl;
}

void draw() {
  background(lx.ui.theme.getDarkBackgroundColor());
  DrawHelper.runAll();
  dispatcher.draw();
}
