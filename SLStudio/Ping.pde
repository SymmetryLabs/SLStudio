import com.symmetrylabs.util.ModelIndex;
import com.symmetrylabs.util.OctreeModelIndex;
import com.symmetrylabs.util.LinearModelIndex;
import com.symmetrylabs.util.Marker;
import com.symmetrylabs.util.MarkerSource;
import com.symmetrylabs.util.CubeMarker;
import com.symmetrylabs.util.Octahedron;
import com.symmetrylabs.util.BlobFollower;

public abstract class SLPatternWithMarkers extends SLPattern implements MarkerSource {
  public SLPatternWithMarkers(LX lx) {
    super(lx);
  }

  @Override
  public void onActive() {
    super.onActive();
    ((LXStudio) lx).ui.addMarkerSource(this);
  }

  @Override
  public void onInactive() {
    super.onInactive();
    ((LXStudio) lx).ui.removeMarkerSource(this);
  }
}

public class PaletteViewer extends SLPattern {
  DiscreteParameter palette = new DiscreteParameter("palette", ((LXStudio) lx).paletteLibrary.getNames());  // selected colour palette
  CompoundParameter palStart = new CompoundParameter("palStart", 0, 0, 1);  // palette start point (fraction 0 - 1)
  CompoundParameter palStop = new CompoundParameter("palStop", 1, 0, 1);  // palette stop point (fraction 0 - 1)
  CompoundParameter palBias = new CompoundParameter("palBias", 0, -6, 6);  // bias colour palette toward zero (dB)
  CompoundParameter palShift = new CompoundParameter("palShift", 0, -1, 1);  // shift in colour palette (fraction 0 - 1)
  CompoundParameter palCutoff = new CompoundParameter("palCutoff", 0, 0, 1);  // palette value cutoff (fraction 0 - 1)

  ZigzagPalette pal = new ZigzagPalette();

  public PaletteViewer(LX lx) {
    super(lx);
    addParameter(palette);
    addParameter(palStart);
    addParameter(palStop);
    addParameter(palShift);
    addParameter(palBias);
    addParameter(palCutoff);
  }

  public void run(double deltaMs) {
    pal.setPalette(((LXStudio) lx).paletteLibrary.get(palette.getOption()));
    pal.setBottom(palStart.getValue());
    pal.setTop(palStop.getValue());
    pal.setBias(palBias.getValue());
    pal.setShift(palShift.getValue());
    pal.setCutoff(palCutoff.getValue());
    for (LXPoint p : model.points) {
      colors[p.index] = pal.getColor((p.y - model.yMin) / (model.yMax - model.yMin));
    }
  }
}

public class BlobViewer extends SLPattern {
  DiscreteParameter mode = new DiscreteParameter("mode", new String[] {"planes", "spheres"});
  CompoundParameter tolerance = new CompoundParameter("tolerance", 2, 0, 8); // in
  CompoundParameter radius = new CompoundParameter("radius", 12, 0, 240); // in
  CompoundParameter y = new CompoundParameter("y", model.cy, model.yMin, model.yMax);
  CompoundParameter oscMergeRadius = new CompoundParameter("bMrgRad", 30, 0, 100);  // blob merge radius (in)
  CompoundParameter oscMaxSpeed = new CompoundParameter("bMaxSpd", 240, 0, 1000);  // max blob speed (in/s)
  CompoundParameter oscMaxDeltaSec = new CompoundParameter("bMaxDt", 0.5, 0, 1);  // max interval to calculate blob velocities (s)

  private BlobTracker blobTracker;

  public BlobViewer(LX lx) {
    super(lx);
    addParameter(mode);
    addParameter(tolerance);
    addParameter(radius);
    addParameter(y);
    addParameter(oscMergeRadius);
    addParameter(oscMaxSpeed);
    blobTracker = BlobTracker.getInstance(lx);
  }

  void updateBlobTrackerParameters() {
      blobTracker.setBlobY(y.getValuef());
      blobTracker.setMergeRadius(oscMergeRadius.getValuef());
      blobTracker.setMaxSpeed(oscMaxSpeed.getValuef());
      blobTracker.setMaxDeltaSec(oscMaxDeltaSec.getValuef());
    }

  public void run(double deltaMs) {
    updateBlobTrackerParameters();
    List<BlobTracker.Blob> blobs = blobTracker.getBlobs();
    int[] highlightColors = {0xffff0000, 0xff00ff00, 0xff0000ff};
    float tol = tolerance.getValuef();
    float rad = radius.getValuef();
    boolean sphereMode = mode.getOption().equals("spheres");

    println("blobs: " + blobs.size());
    for (LXPoint p : model.points) {
      PVector pv = new PVector(p.x, p.y, p.z);
      int c = 0;
      for (int b = 0; b < blobs.size(); b++) {
        PVector pos = blobs.get(b).pos;
        boolean hit = sphereMode ?
            (PVector.sub(pv, pos).mag() < rad) :
            (Math.abs(p.x - pos.x) < tol || Math.abs(p.z - pos.z) < tol);
        if (hit) {
          c = c | highlightColors[b % highlightColors.length];
        }
      }
      colors[p.index] = c;
    }
  }
}

public class FlockWave extends SLPatternWithMarkers {
  CompoundParameter timeScale = new CompoundParameter("timeScale", 1, 0, 1);  // time scaling factor
  BooleanParameter oscFollowers = new BooleanParameter("atBlobs");
  BooleanParameter oscBlobs = new BooleanParameter("nearBlobs");
  BooleanParameter everywhere = new BooleanParameter("everywhere");
  BooleanParameter perSun = new BooleanParameter("perSun");
  CompoundParameter x = new CompoundParameter("x", model.cx, model.xMin, model.xMax);  // focus coordinates (in)
  CompoundParameter y = new CompoundParameter("y", model.cy, model.yMin, model.yMax);
  CompoundParameter z = new CompoundParameter("z", model.cz, model.zMin, model.zMax);
  CompoundParameter zScale = new CompoundParameter("zScale", 0, -6, 12);  // z scaling factor (dB)
  CompoundParameter maxBirds = new CompoundParameter("maxBirds", 8, 0, 40);

  CompoundParameter spnRad = new CompoundParameter("spnRad", 100, 0, 400);  // radius (in) within which to spawn birds
  CompoundParameter spnRate = new CompoundParameter("spnRate", 0.2, 0, 2);  // maximum spawn rate (birds/s)
  CompoundParameter spnVary = new CompoundParameter("spnVary", 0, 0, 1);  // vary spawn rate according to focus speed (0 = don't vary, 1 = determine entirely by speed)
  CompoundParameter scatter = new CompoundParameter("scatter", 100, 0, 1000);  // initial velocity randomness (in/s)
  CompoundParameter spdMult = new CompoundParameter("spdMult", 1, 0, 2);  // (ratio) bird target speed / focus speed
  CompoundParameter maxSpd = new CompoundParameter("maxSpd", 10, 0, 100);  // max bird speed (in/s)
  CompoundParameter turnSec = new CompoundParameter("turnSec", 1, 0, 2);  // time (s) to complete 90% of a turn
  CompoundParameter fadeInSec = new CompoundParameter("fadeInSec", 0.5, 0, 2);  // time (s) to fade up to 100% intensity
  CompoundParameter fadeOutSec = new CompoundParameter("fadeOutSec", 1, 0, 2);  // time (s) to fade down to 10% intensity

  CompoundParameter size = new CompoundParameter("size", 100, 0, 2000);  // render radius of each bird (in)
  CompoundParameter detail = new CompoundParameter("detail", 4, 0, 10);  // ripple spatial frequency (number of waves)
  CompoundParameter ripple = new CompoundParameter("ripple", 0, -10, 10);  // ripple movement (waves/s)
  DiscreteParameter palette = new DiscreteParameter("palette", ((LXStudio) lx).paletteLibrary.getNames());  // selected colour palette
  CompoundParameter palStart = new CompoundParameter("palStart", 0, 0, 1);  // palette start point (fraction 0 - 1)
  CompoundParameter palStop = new CompoundParameter("palStop", 1, 0, 1);  // palette stop point (fraction 0 - 1)
  CompoundParameter palShift = new CompoundParameter("palShift", 0, 0, 1);  // shift in colour palette (fraction 0 - 1)
  CompoundParameter palBias = new CompoundParameter("palBias", 0, -6, 6);  // bias colour palette toward start or stop
  CompoundParameter palCutoff = new CompoundParameter("palCutoff", 0, 0, 1);  // palette value cutoff (fraction 0 - 1)

  PVector prevFocus = null;
  Set<Bird> birds = new HashSet<Bird>();
  float numToSpawn = 0f;

  private BlobTracker blobTracker;
  private BlobFollower blobFollower;
  private ModelIndex modelIndex;
  private ZigzagPalette pal = new ZigzagPalette();
  private long lastRun;

  public FlockWave(LX lx) {
    super(lx);

    blobTracker = BlobTracker.getInstance(lx);
    blobFollower = new BlobFollower(blobTracker);
    modelIndex = new LinearModelIndex(lx.model);

    addParameter(oscFollowers);
    addParameter(oscBlobs);
    addParameter(everywhere);
    addParameter(perSun);

    addParameter(timeScale);
    addParameter(size);
    addParameter(detail);
    addParameter(ripple);

    addParameter(x);
    addParameter(y);
    addParameter(z);
    addParameter(zScale);

    addParameter(palette);
    addParameter(palShift);
    addParameter(palBias);
    addParameter(palCutoff);

    addParameter(palStart);
    addParameter(palStop);
    addParameter(spnRad);
    addParameter(maxBirds);

    addParameter(spnRate);
    addParameter(spnVary);
    addParameter(scatter);

    addParameter(spdMult);
    addParameter(maxSpd);
    addParameter(turnSec);
    addParameter(fadeInSec);
    addParameter(fadeOutSec);
  }

  public void run(double deltaMs) {
    advanceSimulation((float) deltaMs * 0.001 * timeScale.getValuef());
    blobFollower.advance((float) deltaMs * 0.001);
    render();
    lastRun = new Date().getTime();
  }

  void advanceSimulation(float deltaSec) {
    if (oscBlobs.isOn()) {
      updateBlobTrackerParameters();

      List<BlobTracker.Blob> blobs = blobTracker.getBlobs();
      for (BlobTracker.Blob b : blobs) {
        spawnBirds(deltaSec, b.pos, b.vel, b.size);
      }

      advanceBirdsWithBlobs(deltaSec, blobs);
    } else {
      PVector focus = new PVector(x.getValuef(), y.getValuef(), z.getValuef());

      if (prevFocus != null) {
        PVector vel = PVector.sub(focus, prevFocus);
        if (deltaSec > 0) {
          vel.div(deltaSec);
        }

        spawnBirds(deltaSec, focus, vel, 1);
        advanceBirds(deltaSec, vel);
      }

      prevFocus = focus;
    }

    removeExpiredBirds();
  }

  Collection<Marker> getMarkers() {
    List<Marker> markers = new ArrayList<Marker>();
    if (lastRun + 1000 < new Date().getTime()) return markers; // hack to hide markers if inactive
    if (oscFollowers.isOn()) {
      markers.addAll(blobFollower.getMarkers());
    } else {
      for (Bird bird : birds) {
        markers.add(new Octahedron(bird.pos, 1 + bird.value*12, 0x00ffff));
      }
      if (oscBlobs.isOn()) {
        for (BlobTracker.Blob b : blobTracker.getBlobs()) {
          markers.add(new CubeMarker(b.pos, spnRad.getValuef(), 0x00ff00));
        }
      } else if (everywhere.isOn()) {
        markers.add(new CubeMarker(new PVector(model.cx, model.cy, model.cz), new PVector(model.xRange/2, model.yRange/2, model.zRange/2), 0x00ff00));
      } else {
        markers.add(new CubeMarker(new PVector(x.getValuef(), y.getValuef(), z.getValuef()), spnRad.getValuef(), 0x00ff00));
      }
    }
    return markers;
  }

  void updateBlobTrackerParameters() {
    blobTracker.setBlobY(y.getValuef());
  }

  void spawnBirds(float deltaSec, PVector focus, PVector vel, float weight) {
    float speed = vel.mag();
    float vary = spnVary.getValuef();
    float spawnFactor = vary * (speed / 240.0f) + (1.0f - vary);
    numToSpawn += deltaSec * spnRate.getValuef() * weight * spawnFactor;

    while (numToSpawn >= 1.0) {
      spawnBird(focus);
      numToSpawn -= 1.0;
    }

    if (vary > 0.01) {
      if (FastMath.random() < numToSpawn) {
        spawnBird(focus);
      }
      numToSpawn = 0;
    }
  }

  void spawnBird(PVector focus) {
    if ((birds.size() + 1) <= maxBirds.getValue()) {
      PVector pos = getRandomUnitVector();
      if (everywhere.isOn()) {
        pos.x = model.xMin + (float) Math.random() * (model.xMax - model.xMin);
        pos.y = model.yMin + (float) Math.random() * (model.yMax - model.yMin);
        pos.z = model.zMin + (float) Math.random() * (model.zMax - model.zMin);
      } else {
        pos.mult(spnRad.getValuef());
        pos.add(focus);
      }
      birds.add(new Bird(pos, LXColor.hsb(FastMath.random()*360, FastMath.random()*100, 100)));
    }
  }

  void advanceBirds(float deltaSec, PVector vel) {
    PVector targetVel = PVector.mult(vel, spdMult.getValuef());
    for (Bird b : birds) {
      b.run(deltaSec, targetVel);
    }
  }

  void advanceBirdsWithBlobs(float deltaSec, List<BlobTracker.Blob> blobs) {
    for (Bird b : birds) {
      PVector velSum = new PVector(0, 0, 0);
      float totalWeight = 0;
      for (BlobTracker.Blob blob : blobs) {
        float distance = PVector.sub(b.pos, blob.pos).mag();
        float weight = 1.0f / (distance * distance);
        PVector.add(velSum, PVector.mult(blob.vel, weight), velSum);
        totalWeight += weight;
      }

      if (totalWeight > 0) {
        velSum.div(totalWeight);
      }

      PVector targetVel = PVector.mult(velSum, spdMult.getValuef());
      b.run(deltaSec, targetVel);
    }
  }

  void removeExpiredBirds() {
    List<Bird> expired = new ArrayList<Bird>();
    for (Bird b : birds) {
      if (b.expired) {
        expired.add(b);
      }
    }
    birds.removeAll(expired);
  }

  void render() {  // choose a rendering style
    if (oscFollowers.isOn()) {
      List<Bird> followBirds = new ArrayList<Bird>();
      for (BlobFollower.Follower f : blobFollower.getFollowers()) {
        Bird b = new Bird(f.pos, 0);
        b.vel = f.vel;
        b.value = f.value;
        b.elapsedSec = f.ageSec;
        followBirds.add(b);
      }
      renderPlasma(followBirds);
    } else {
      renderPlasma(birds);
    }
  }

  void renderTrails() {
    float radius = size.getValuef();
    float sqRadius = radius*radius;

    for (LXPoint p : model.points) {
      int rgb = 0;
      for (Bird b : birds) {
        if (Math.abs(b.pos.x - p.x) < radius) {
          if ((b.pos.x - p.x)*(b.pos.x - p.x) + (b.pos.y - p.y)*(b.pos.y - p.y) < sqRadius) {
            rgb = LXColor.add(rgb, LXColor.lerp(0, b.rgb, b.value));
          }
        }
      }
      colors[p.index] = LXColor.add(LXColor.lerp(colors[p.index], 0, 0.1), rgb);
    }
  }

  void renderVoronoi() {
    float radius = size.getValuef();

    radius = 10000;
    for (LXPoint p : model.points) {
      Bird closestBird = null;
      float minSqDist = 1e6;
      for (Bird b : birds) {
        if (Math.abs(b.pos.x - p.x) < radius) {
          float sqDist = (b.pos.x - p.x)*(b.pos.x - p.x) + (b.pos.y - p.y)*(b.pos.y - p.y);
          if (sqDist < minSqDist) {
            minSqDist = sqDist;
            closestBird = b;
          }
        }
      }
      colors[p.index] = minSqDist > radius ? 0 : LXColor.lerp(0, closestBird.rgb, closestBird.value);
    }
  }

  ColorPalette getPalette() {
    pal.setPalette(((LXStudio) lx).paletteLibrary.get(palette.getOption()));
    pal.setBottom(palStart.getValue());
    pal.setTop(palStop.getValue());
    pal.setBias(palBias.getValue());
    pal.setShift(palShift.getValue());
    pal.setCutoff(palCutoff.getValue());
    return pal;
  }

  void renderPlasma(final Collection<Bird> birds) {
    birds.parallelStream().forEach(new Consumer<Bird>() {
      public void accept(Bird bird) {
        renderPlasmaLayer(bird);
      }
    });

    final ColorPalette pal = getPalette();
    final double shift = palShift.getValue();
    final SLModel slModel = (SLModel) model;
    final LXPoint[] points = model.points;
    if (perSun.isOn()) {
      points = slModel.masterSun.points;
    }

    Arrays.asList(points).parallelStream().forEach(new Consumer<LXPoint>() {
      public void accept(LXPoint point) {
        double sum = 0;
        for (Bird bird : birds) {
          sum += bird.renderedValues[point.index];
        }
        colors[point.index] = pal.getColor(sum + shift);
      }
    });

    if (perSun.isOn()) {
      for (Sun sun : slModel.suns) {
        copyFromMasterSun();
      }
    }
  }

  void renderPlasmaLayer(final Bird bird) {
    double waveNumber = detail.getValue();
    double extent = size.getValue();
    double rippleSpeed = ripple.getValue();
    double zFactor = FastMath.pow(10, zScale.getValue()/10);
    double zSqFactor = zFactor*zFactor - 1;
    double dx, dy, dz, sqDist, phase, a;

    LXPoint pos = new LXPoint(bird.pos.x, bird.pos.y, bird.pos.z);
    for (LXPoint point : modelIndex.pointsWithin(pos, (float) extent)) {
      dx = (bird.pos.x - point.x)/extent;
      dy = (bird.pos.y - point.y)/extent;
      dz = (bird.pos.z - point.z)/extent;
      sqDist = dx*dx + dy*dy + dz*dz;
      if (sqDist < 1) {
        phase = FastMath.sqrt(sqDist + dz*dz*zSqFactor);
        a = 1 - sqDist;
        bird.renderedValues[point.index] =
            a * a * bird.value
                * FastMath.sin(waveNumber * 2 * FastMath.PI * phase - bird.elapsedSec * rippleSpeed)
                * FastMath.cos(waveNumber * 5/4 * phase);
      } else {
        bird.renderedValues[point.index] = 0;
      }
    }
  }

  PVector getRandomUnitVector() {
    PVector pos = new PVector();
    while (true) {
      pos.set((float)FastMath.random() * 2 - 1, (float)FastMath.random() * 2 - 1, (float)FastMath.random() * 2 - 1);
      if (pos.mag() < 1) {
        return pos;
      }
    }
  }

  void setPalette(String name) {
    String[] options = palette.getOptions();
    for (int i = 0; i < options.length; i++) {
      if (options[i].equals(name)) {
        palette.setValue(i);
      }
    }
  }

  public class Bird implements Comparable<Bird> {
    public PVector pos;
    public PVector vel;
    public int rgb;
    public float value;
    public float elapsedSec;
    public boolean expired;
    public double[] renderedValues;

    Bird(PVector pos, int rgb) {
      this.pos = pos;
      this.vel = PVector.mult(getRandomUnitVector(), scatter.getValuef());
      this.rgb = rgb;
      this.value = 0;
      this.elapsedSec = 0;
      this.expired = false;
      this.renderedValues = new double[colors.length];
    }

    void run(float deltaSec, PVector targetVel) {
      advance(deltaSec);
      turn(deltaSec, targetVel);

      elapsedSec += deltaSec;
      if (elapsedSec < fadeInSec.getValuef()) {
        value = elapsedSec / fadeInSec.getValuef();
      } else {
        value = (float) FastMath.pow(0.1, (elapsedSec - fadeInSec.getValuef()) / fadeOutSec.getValuef());
        if (value < 0.004) expired = true;
      }
    }

    void advance(float deltaSec) {
      pos.add(PVector.mult(vel, (float) deltaSec));
    }

    void turn(float deltaSec, PVector targetVel) {
      float speed = vel.mag();
      float targetSpeed = targetVel.mag();

      float frac = (float)FastMath.pow(0.1, deltaSec / turnSec.getValuef());
      vel = PVector.add(PVector.mult(vel, frac), PVector.mult(targetVel, 1 - frac));
      speed = speed * frac + targetSpeed * (1 - frac);
      if (targetSpeed > maxSpd.getValuef()) targetSpeed = maxSpd.getValuef();

      float mag = vel.mag();
      if (mag > 0 && mag < speed) vel.div(mag / speed);
    }

    public int compareTo(Bird other) {
      return Float.compare(pos.x, other.pos.x);
    }
  }
}

public class FlockWaveBlues extends FlockWave {
  public FlockWaveBlues(LX lx) {
    super(lx);

    parameters.get("everywhere").setValue(1);
    parameters.get("nearBlobs").setValue(0);
    parameters.get("spnRate").setValue(2.000);
    parameters.get("detail").setValue(2.000);
    parameters.get("fadeInSec").setValue(1.700);
    parameters.get("fadeOutSec").setValue(0.780);
    parameters.get("atBlobs").setValue(0);
    parameters.get("maxBirds").setValue(8.000);
    parameters.get("maxSpd").setValue(10.000);
    parameters.get("palBias").setValue(-0.800);
    parameters.get("palCutoff").setValue(0);
    parameters.get("palShift").setValue(0);
    parameters.get("palStart").setValue(0);
    parameters.get("palStop").setValue(1);
    setPalette("jupiter1");
    parameters.get("ripple").setValue(0.600);
    parameters.get("scatter").setValue(300.000);
    parameters.get("size").setValue(620.000);
    parameters.get("spdMult").setValue(0.240);
    parameters.get("spnVary").setValue(0);
    parameters.get("spnRad").setValue(280.000);
    parameters.get("timeScale").setValue(0.170);
    parameters.get("turnSec").setValue(1.560);
    parameters.get("x").setValue(300);
    parameters.get("y").setValue(0.400);
    parameters.get("z").setValue(78.398);
    parameters.get("zScale").setValue(0.540);
  }
}

public class FlockWaveFiery extends FlockWave {
  public FlockWaveFiery(LX lx) {
    super(lx);

    parameters.get("everywhere").setValue(1);
    parameters.get("nearBlobs").setValue(0);
    parameters.get("spnRate").setValue(2.000);
    parameters.get("detail").setValue(6.600);
    parameters.get("fadeInSec").setValue(1.700);
    parameters.get("fadeOutSec").setValue(0.780);
    parameters.get("atBlobs").setValue(0);
    parameters.get("maxBirds").setValue(8.000);
    parameters.get("maxSpd").setValue(10.000);
    parameters.get("palBias").setValue(-1.640);
    parameters.get("palCutoff").setValue(0.210);
    parameters.get("palShift").setValue(0);
    parameters.get("palStart").setValue(0.250);
    parameters.get("palStop").setValue(1);
    setPalette("sky.red");
    parameters.get("ripple").setValue(0.600);
    parameters.get("scatter").setValue(150.000);
    parameters.get("size").setValue(500.000);
    parameters.get("spdMult").setValue(0.240);
    parameters.get("spnVary").setValue(0);
    parameters.get("spnRad").setValue(280.000);
    parameters.get("timeScale").setValue(0.150);
    parameters.get("turnSec").setValue(1.560);
    parameters.get("x").setValue(300);
    parameters.get("y").setValue(0.400);
    parameters.get("z").setValue(78.398);
    parameters.get("zScale").setValue(5.580);
  }
}

public class FlockWaveGalaxies extends FlockWave {
  public FlockWaveGalaxies(LX lx) {
    super(lx);

    parameters.get("everywhere").setValue(1);
    parameters.get("nearBlobs").setValue(0);
    parameters.get("spnRate").setValue(2.000);
    parameters.get("detail").setValue(6.600);
    parameters.get("fadeInSec").setValue(1.700);
    parameters.get("fadeOutSec").setValue(0.780);
    parameters.get("atBlobs").setValue(0);
    parameters.get("maxBirds").setValue(8.000);
    parameters.get("maxSpd").setValue(10.000);
    parameters.get("palBias").setValue(-1.400);
    parameters.get("palCutoff").setValue(0.320);
    parameters.get("palShift").setValue(0);
    parameters.get("palStart").setValue(0.250);
    parameters.get("palStop").setValue(1);
    setPalette("galaxies");
    parameters.get("ripple").setValue(0.400);
    parameters.get("scatter").setValue(130.000);
    parameters.get("size").setValue(560.000);
    parameters.get("spdMult").setValue(0.240);
    parameters.get("spnVary").setValue(0);
    parameters.get("spnRad").setValue(280.000);
    parameters.get("timeScale").setValue(0.090);
    parameters.get("turnSec").setValue(1.560);
    parameters.get("x").setValue(300.000);
    parameters.get("y").setValue(0.400);
    parameters.get("z").setValue(78.398);
    parameters.get("zScale").setValue(6.300);
  }
}

public class FlockWaveMercury extends FlockWave {
  public FlockWaveMercury(LX lx) {
    super(lx);

    parameters.get("everywhere").setValue(1);
    parameters.get("nearBlobs").setValue(0);
    parameters.get("spnRate").setValue(2.000);
    parameters.get("detail").setValue(10.000);
    parameters.get("fadeInSec").setValue(2.000);
    parameters.get("fadeOutSec").setValue(1.500);
    parameters.get("atBlobs").setValue(0);
    parameters.get("maxBirds").setValue(20.000);
    parameters.get("maxSpd").setValue(0);
    parameters.get("palBias").setValue(0);
    parameters.get("palCutoff").setValue(0);
    parameters.get("palShift").setValue(0);
    parameters.get("palStart").setValue(0);
    parameters.get("palStop").setValue(1);
    setPalette("lake");
    parameters.get("ripple").setValue(0.600);
    parameters.get("scatter").setValue(200.000);
    parameters.get("size").setValue(340.000);
    parameters.get("spdMult").setValue(0);
    parameters.get("spnVary").setValue(0);
    parameters.get("spnRad").setValue(280.000);
    parameters.get("timeScale").setValue(1);
    parameters.get("turnSec").setValue(1.560);
    parameters.get("x").setValue(300);
    parameters.get("y").setValue(0.400);
    parameters.get("z").setValue(78.398);
    parameters.get("zScale").setValue(3.900);
  }
}

public class FlockWaveOoze extends FlockWave {
  public FlockWaveOoze(LX lx) {
    super(lx);

    parameters.get("everywhere").setValue(1);
    parameters.get("nearBlobs").setValue(0);
    parameters.get("spnRate").setValue(2.000);
    parameters.get("detail").setValue(6.600);
    parameters.get("fadeInSec").setValue(1.700);
    parameters.get("fadeOutSec").setValue(0.780);
    parameters.get("atBlobs").setValue(0);
    parameters.get("maxBirds").setValue(8.000);
    parameters.get("maxSpd").setValue(10.000);
    parameters.get("palBias").setValue(-2.120);
    parameters.get("palCutoff").setValue(0);
    parameters.get("palShift").setValue(0.120);
    parameters.get("palStart").setValue(0.250);
    parameters.get("palStop").setValue(1);
    setPalette("sky.orange");
    parameters.get("ripple").setValue(0.400);
    parameters.get("scatter").setValue(150.000);
    parameters.get("size").setValue(560.000);
    parameters.get("spdMult").setValue(0.240);
    parameters.get("spnVary").setValue(0);
    parameters.get("spnRad").setValue(280.000);
    parameters.get("timeScale").setValue(0.150);
    parameters.get("turnSec").setValue(1.560);
    parameters.get("x").setValue(300);
    parameters.get("y").setValue(0.400);
    parameters.get("z").setValue(78.398);
    parameters.get("zScale").setValue(6.300);
  }
}

public class FlockWavePlanets extends FlockWave {
  public FlockWavePlanets(LX lx) {
    super(lx);

    parameters.get("everywhere").setValue(1);
    parameters.get("nearBlobs").setValue(0);
    parameters.get("spnRate").setValue(2.000);
    parameters.get("detail").setValue(4.400);
    parameters.get("fadeInSec").setValue(1.700);
    parameters.get("fadeOutSec").setValue(0.780);
    parameters.get("atBlobs").setValue(0);
    parameters.get("maxBirds").setValue(8.000);
    parameters.get("maxSpd").setValue(10.000);
    parameters.get("palBias").setValue(-1.280);
    parameters.get("palCutoff").setValue(0);
    parameters.get("palShift").setValue(0.080);
    parameters.get("palStart").setValue(0);
    parameters.get("palStop").setValue(0.840);
    setPalette("sky.purple");
    parameters.get("ripple").setValue(-3.600);
    parameters.get("scatter").setValue(150.000);
    parameters.get("size").setValue(560.000);
    parameters.get("spdMult").setValue(0);
    parameters.get("spnVary").setValue(0);
    parameters.get("spnRad").setValue(280.000);
    parameters.get("timeScale").setValue(0.150);
    parameters.get("turnSec").setValue(1.560);
    parameters.get("x").setValue(300);
    parameters.get("y").setValue(0.400);
    parameters.get("z").setValue(78.398);
    parameters.get("zScale").setValue(-0.180);
  }
}

public class FlockWaveTimewarp extends FlockWave {
  public FlockWaveTimewarp(LX lx) {
    super(lx);

    parameters.get("everywhere").setValue(1);
    parameters.get("nearBlobs").setValue(0);
    parameters.get("spnRate").setValue(2.000);
    parameters.get("detail").setValue(6.600);
    parameters.get("fadeInSec").setValue(1.700);
    parameters.get("fadeOutSec").setValue(0.780);
    parameters.get("atBlobs").setValue(0);
    parameters.get("maxBirds").setValue(8.000);
    parameters.get("maxSpd").setValue(10.000);
    parameters.get("palBias").setValue(-1.280);
    parameters.get("palCutoff").setValue(0);
    parameters.get("palShift").setValue(0.120);
    parameters.get("palStart").setValue(0.250);
    parameters.get("palStop").setValue(1);
    setPalette("sun2");
    parameters.get("ripple").setValue(0.600);
    parameters.get("scatter").setValue(300.000);
    parameters.get("size").setValue(560.000);
    parameters.get("spdMult").setValue(0.240);
    parameters.get("spnVary").setValue(0);
    parameters.get("spnRad").setValue(280.000);
    parameters.get("timeScale").setValue(0.350);
    parameters.get("turnSec").setValue(1.560);
    parameters.get("x").setValue(300);
    parameters.get("y").setValue(0.400);
    parameters.get("z").setValue(78.398);
    parameters.get("zScale").setValue(5.580);
  }
}

public class LightSource extends SLPatternWithMarkers {
  CompoundParameter x = new CompoundParameter("x", model.cx, model.xMin, model.xMax);
  CompoundParameter y = new CompoundParameter("y", model.yMax, 0, 240);
  CompoundParameter z = new CompoundParameter("z", model.cz, model.zMin, model.zMax);
  CompoundParameter hue = new CompoundParameter("hue", 0, 0, 360);
  CompoundParameter sat = new CompoundParameter("sat", 0, 0, 1);
  CompoundParameter gain = new CompoundParameter("gain", 1, 0, 3);
  CompoundParameter falloff = new CompoundParameter("falloff", 0.25, 0, 1);
  CompoundParameter ambient = new CompoundParameter("ambient", 0, 0, 1);
  BooleanParameter useBlobs = new BooleanParameter("useBlobs");

  List<Light> lights = new ArrayList<Light>();
  int numActiveLights = 0;
  BlobFollower bf;

  public LightSource(LX lx) {
    super(lx);
    addParameter(x);
    addParameter(y);
    addParameter(z);
    addParameter(hue);
    addParameter(sat);
    addParameter(gain);
    addParameter(falloff);
    addParameter(ambient);
    addParameter(useBlobs);
    bf = new BlobFollower(BlobTracker.getInstance(lx));
  }

  public List<Marker> getMarkers() {
    List<Marker> markers = new ArrayList<Marker>();
    PVector pos = new PVector(x.getValuef(), y.getValuef(), z.getValuef());
    float value = gain.getValuef()*100f;
    markers.add(new Octahedron(pos, 20, LX.hsb(hue.getValuef(), sat.getValuef()*100f, value > 100 ? 100 : value)));
    markers.addAll(bf.getMarkers());
    return markers;
  }

  public void run(double deltaMs) {
    resetLights();
    if (useBlobs.isOn()) {
      for (BlobFollower.Follower f : bf.getFollowers()) {
        addLight(new PVector(f.pos.x, y.getValuef(), f.pos.z), f.value);
      }
    } else {
      addLight(new PVector(x.getValuef(), y.getValuef(), z.getValuef()), 1);
    }
    renderLights();
    bf.advance((float) deltaMs * 0.001);
  }

  void resetLights() {
    numActiveLights = 0;
  }

  void addLight(PVector pos, float value) {
    int li = numActiveLights;
    if (li >= lights.size()) {
      lights.add(new Light(pos, value));
    } else {
      Light light = lights.get(li);
      light.pos = pos;
      light.value = value;
    }
    numActiveLights++;
  }

  void renderLights() {
    final float h = hue.getValuef();
    final float s = sat.getValuef() * 100f;
    final float g = gain.getValuef();
    final float a = ambient.getValuef();
    final List<Light> activeLights = lights.subList(0, numActiveLights);

    activeLights.parallelStream().forEach(new Consumer<Light>() {
      public void accept(Light light) {
        for (LXPoint p : model.points) {
          LXPointNormal pn = (LXPointNormal) p;
          PVector pv = new PVector(p.x, p.y, p.z);
          PVector toLight = PVector.sub(light.pos, pv);
          float dist = toLight.mag();

          float extent = dist / (falloff.getValuef() * model.xRange);
          if (extent < 1) extent = 1; // avoid division by zero or excessive brightness
          float brightness = 1.0 / (extent * extent);

          float cosAngle = PVector.dot(toLight, pn.normal)/dist;
          if (cosAngle < 0) cosAngle = 0;

          light.levels[p.index] = cosAngle * brightness * light.value * g;
        }
      }
    });

    Arrays.asList(model.points).parallelStream().forEach(new Consumer<LXPoint>() {
      public void accept(LXPoint p) {
        float sum = a;
        for (Light light : activeLights) {
          sum += light.levels[p.index];
        }
        colors[p.index] = LX.hsb(h, s, (sum > 1 ? 1 : sum)*100f);
      }
    });
  }

  class Light {
    public PVector pos;
    public float value;
    public float[] levels;

    Light(PVector pos, float value) {
      this.pos = pos;
      this.value = value;
      this.levels = new float[colors.length];
    }
  }
}

public class RipplePads extends SLPattern {
  CompoundParameter intensity = new CompoundParameter("intensity", 1, 0, 3);
  CompoundParameter velocity = new CompoundParameter("velocity", 80, 0, 127);
  CompoundParameter speed = new CompoundParameter("speed", 200, 0, 500);
  CompoundParameter decaySec = new CompoundParameter("decaySec", 1, 0, 10);
  CompoundParameter nextHue = new CompoundParameter("nextHue", 0, 0, 360);
  CompoundParameter nextSat = new CompoundParameter("nextSat", 0, 0, 1);

  String[] sunIds = {"sun3", "sun5", "sun8", "sun11", "sun10", "sun9", "sun7", "sun6", "sun4", "sun1", "sun2"};
  String[] buttonNames = {"K", "J", "I", "H", "G", "F", "E", "D", "C", "B", "A"};
  int[] buttonPitches = {60, 62, 64, 65, 67, 69, 71, 72, 74, 76, 77};

  BooleanParameter[] buttons;
  boolean[] lastState;

  Sun[] sunsByPitch = new Sun[128];
  Ripple[] lastRipple = new Ripple[128];
  List<Ripple> ripples = new ArrayList<Ripple>();

  public RipplePads(LX lx) {
    super(lx);

    addParameter(intensity);
    addParameter(velocity);
    addParameter(speed);
    addParameter(decaySec);
    addParameter(nextHue);
    addParameter(nextSat);


    buttons = new BooleanParameter[sunIds.length];
    lastState = new boolean[sunIds.length];

    for (int i = 0; i < sunIds.length; i++) {
      sunsByPitch[buttonPitches[i]] = model.sunTable.get(sunIds[i]);
      BooleanParameter param = new BooleanParameter(buttonNames[i]);
      param.setMode(BooleanParameter.Mode.MOMENTARY);
      addParameter(param);
      buttons[i] = param;
    }
  }

  public void run(double deltaMs) {
    float deltaSec = (float) deltaMs * 0.001f;

    for (int b = 0; b < buttons.length; b++) {
      boolean state = buttons[b].isOn();
      if (state != lastState[b]) {
        if (state) {
          noteOn(buttonPitches[b], (int) velocity.getValuef());
        } else {
          noteOff(buttonPitches[b]);
        }
        lastState[b] = state;
      }
    }

    List<Ripple> expired = new ArrayList<Ripple>();
    for (Ripple ripple : ripples) {
      ripple.advance(deltaSec);
      if (ripple.isExpired()) {
        expired.add(ripple);
      }
    }
    ripples.removeAll(expired);

    for (Sun sun : sunsByPitch) {
      if (sun == null) continue;
      for (int i = 0; i < sun.points.length; i++) {
        LXPoint point = sun.points[i];
        int sum = 0xff000000;
        for (Ripple ripple : ripples) {
          if (ripple.sun == sun) {
            sum = LXColor.add(sum, ripple.getColor(sun.distances[i]));
          }
        }
        colors[point.index] = sum;
      }
    }
  }

  public void noteOnReceived(MidiNoteOn note) {
    println("note on " + note);
    noteOn(note.getPitch(), note.getVelocity());
  }

  public void noteOffReceived(MidiNote note) {
    println("note off " + note);
    noteOff(note.getPitch());
  }

  void noteOn(int pitch, int velocity) {
    if (pitch > 127) return;
    Sun sun = sunsByPitch[pitch];
    if (sun != null) {
      if (lastRipple[pitch] != null) {
        lastRipple[pitch].release();
      }
      lastRipple[pitch] = addRipple(sun, velocity/128f);
    }
  }

  void noteOff(int pitch) {
    if (pitch > 127) return;
    if (lastRipple[pitch] != null) {
      lastRipple[pitch].release();
    }
    lastRipple[pitch] = null;
  }

  Ripple addRipple(Sun sun, float velocity) {
    Ripple ripple = new Ripple(
        sun, sun.boundingBox.size.z/2,
        intensity.getValuef()*velocity, speed.getValuef()*velocity,
        decaySec.getValuef(), nextHue.getValuef(), nextSat.getValuef()
    );
    ripples.add(ripple);
    return ripple;
  }

  class Ripple {
    Sun sun;
    float intensity;
    float speed;
    float decaySec;
    float hue;
    float sat;

    float ageSec;
    float radius;
    float value;
    int[] layerColors;
    boolean held;

    Ripple(Sun sun, float radius, float intensity, float speed, float decaySec, float hue, float sat) {
      this.sun = sun;
      this.radius = radius;
      this.intensity = intensity;
      this.speed = speed;
      this.decaySec = decaySec;
      this.hue = hue;
      this.sat = sat;
      this.held = true;
      ageSec = 0;
    }

    void advance(float deltaSec) {
      if (!held) ageSec += deltaSec;
      radius += deltaSec * speed;
      value = intensity / (1f + 10f*ageSec/decaySec);
    }

    void release() {
      held = false;
    }

    int getColor(float distance) {
      if (distance < radius) {
        float brightness = value > 1 ? 1 : value;
        return lx.hsb(hue, sat*100f, brightness*100f);
      } else {
        return 0;
      }
    }

    boolean isExpired() {
      return ageSec > decaySec*2;
    }
  }
}
