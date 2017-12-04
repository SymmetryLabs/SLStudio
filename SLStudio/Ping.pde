import com.symmetrylabs.util.ModelIndex;
import com.symmetrylabs.util.OctreeModelIndex;

public class PaletteViewer extends SLPattern {
  DiscreteParameter palette = new DiscreteParameter("palette", paletteLibrary.getNames());  // selected colour palette
  CompoundParameter palShift = new CompoundParameter("palShift", 0, -1, 1);  // shift in colour palette (fraction 0 - 1)
  CompoundParameter palBias = new CompoundParameter("palBias", 0, -6, 6);  // bias colour palette toward zero (dB)
  CompoundParameter palStart = new CompoundParameter("palStart", 0, 0, 1);  // palette start point (fraction 0 - 1)
  CompoundParameter palStop = new CompoundParameter("palStop", 1, 0, 1);  // palette stop point (fraction 0 - 1)

  public PaletteViewer(LX lx) {
    super(lx);
    addParameter(palette);
    addParameter(palStart);
    addParameter(palStop);
    addParameter(palShift);
    addParameter(palBias);
  }

  public void run(double deltaMs) {
    ColorPalette pal = paletteLibrary.get(palette.getOption());
    double shift = palShift.getValue();
    if (pal instanceof ZigzagPalette) {
      ((ZigzagPalette) pal).setBias(palBias.getValue());
      ((ZigzagPalette) pal).setStart(palStart.getValue());
      ((ZigzagPalette) pal).setStop(palStop.getValue());
    }
    for (LXPoint p : model.points) {
      colors[p.index] = pal.getColor((p.y - model.yMin) / (model.yMax - model.yMin) + shift);
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

public class FlockWave extends SLPattern {
  CompoundParameter timeScale = new CompoundParameter("timeScale", 1, 0, 1);  // time scaling factor
  BooleanParameter oscBlobs = new BooleanParameter("oscBlobs");
  CompoundParameter oscMergeRadius = new CompoundParameter("bMrgRad", 30, 0, 100);  // blob merge radius (in)
  CompoundParameter oscMaxSpeed = new CompoundParameter("bMaxSpd", 360, 0, 1000);  // max blob speed (in/s)
  CompoundParameter oscMaxDeltaSec = new CompoundParameter("bMaxDt", 0.5, 0, 1);  // max interval to calculate blob velocities (s)
  CompoundParameter x = new CompoundParameter("x", model.cx, model.xMin, model.xMax);  // focus coordinates (in)
  CompoundParameter y = new CompoundParameter("y", model.cy, model.yMin, model.yMax);
  CompoundParameter z = new CompoundParameter("z", model.cz, model.zMin, model.zMax);
  CompoundParameter zScale = new CompoundParameter("zScale", 0, -6, 12);  // z scaling factor (dB)

  CompoundParameter spawnMinSpeed = new CompoundParameter("spnMin", 2, 0, 40);  // minimum focus speed (in/s) that spawns birds
  CompoundParameter spawnMaxSpeed = new CompoundParameter("spnMax", 20, 0, 40);  // maximum focus speed (in/s) that spawns birds
  CompoundParameter spawnRadius = new CompoundParameter("spnRad", 100, 0, 200);  // radius (in) within which to spawn birds
  CompoundParameter density = new CompoundParameter("density", 0.2, 0, 0.5);  // maximum spawn rate (birds/s)
  CompoundParameter scatter = new CompoundParameter("scatter", 100, 0, 1000);  // initial velocity randomness (in/s)
  CompoundParameter speedMult = new CompoundParameter("spdMult", 1, 0, 2);  // (ratio) target bird speed / focus speed
  CompoundParameter maxSpeed = new CompoundParameter("maxSpd", 10, 0, 100);  // max bird speed (in/s)
  CompoundParameter turnSec = new CompoundParameter("turnSec", 1, 0, 2);  // time (s) to complete 90% of a turn

  CompoundParameter fadeInSec = new CompoundParameter("fadeInSec", 0.5, 0, 2);  // time (s) to fade up to 100% intensity
  CompoundParameter fadeOutSec = new CompoundParameter("fadeOutSec", 1, 0, 2);  // time (s) to fade down to 10% intensity
  CompoundParameter size = new CompoundParameter("size", 100, 0, 2000);  // render radius of each bird (in)
  CompoundParameter detail = new CompoundParameter("detail", 4, 0, 10);  // ripple spatial frequency (number of waves)
  CompoundParameter ripple = new CompoundParameter("ripple", 0, -10, 10);  // ripple movement (waves/s)

  DiscreteParameter palette = new DiscreteParameter("palette", paletteLibrary.getNames());  // selected colour palette
  CompoundParameter palStart = new CompoundParameter("palStart", 0, 0, 1);  // palette start point (fraction 0 - 1)
  CompoundParameter palStop = new CompoundParameter("palStop", 1, 0, 1);  // palette stop point (fraction 0 - 1)
  CompoundParameter palShift = new CompoundParameter("palShift", 0, 0, 1);  // shift in colour palette (fraction 0 - 1)
  CompoundParameter palBias = new CompoundParameter("palBias", 0, -6, 6);  // bias colour palette toward start or stop

  PVector prevFocus = null;
  Set<Bird> birds = new HashSet<Bird>();

  private BlobTracker blobTracker;
  private ModelIndex modelIndex;

  public FlockWave(LX lx) {
    super(lx);

    blobTracker = BlobTracker.getInstance(lx);
    modelIndex = new OctreeModelIndex(lx.model);

    addParameter(timeScale);
    addParameter(oscBlobs);
    addParameter(oscMergeRadius);
    addParameter(oscMaxSpeed);
    addParameter(x);
    addParameter(y);
    addParameter(z);
    addParameter(zScale);

    addParameter(spawnRadius);
    addParameter(spawnMinSpeed);
    addParameter(spawnMaxSpeed);
    addParameter(density);
    addParameter(scatter);
    addParameter(speedMult);
    addParameter(maxSpeed);

    addParameter(turnSec);
    addParameter(fadeInSec);
    addParameter(fadeOutSec);
    addParameter(size);
    addParameter(detail);
    addParameter(ripple);

    addParameter(palette);
    addParameter(palStart);
    addParameter(palStop);
    addParameter(palShift);
    addParameter(palBias);
  }

  public void run(double deltaMs) {
    println("deltaMs: " + deltaMs + " / birds: " + birds.size());
    advanceSimulation((float) deltaMs * 0.001 * timeScale.getValuef());
    render();
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

  void updateBlobTrackerParameters() {
    blobTracker.setBlobY(y.getValuef());
    blobTracker.setMergeRadius(oscMergeRadius.getValuef());
    blobTracker.setMaxSpeed(oscMaxSpeed.getValuef());
    blobTracker.setMaxDeltaSec(oscMaxDeltaSec.getValuef());
  }

  void spawnBirds(float deltaSec, PVector focus, PVector vel, float weight) {
    float spawnMin = spawnMinSpeed.getValuef();
    float spawnMax = spawnMaxSpeed.getValuef();
    float speed = vel.mag();
    float numToSpawn = deltaSec * density.getValuef() * weight * (speed - spawnMin) / (spawnMax - spawnMin);

    List<Bird> newBirds = new ArrayList<Bird>();

    while (numToSpawn >= 1.0) {
      spawnBird(focus);
      numToSpawn -= 1.0;
    }
    if (FastMath.random() < numToSpawn) {
      spawnBird(focus);
    }
  }

  void spawnBird(PVector focus) {
    PVector pos = getRandomUnitVector();
    pos.mult(spawnRadius.getValuef());
    pos.add(focus);
    birds.add(new Bird(pos, LXColor.hsb(FastMath.random()*360, FastMath.random()*100, 100)));
  }

  void advanceBirds(float deltaSec, PVector vel) {
    PVector targetVel = PVector.mult(vel, speedMult.getValuef());
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

      PVector targetVel = PVector.mult(velSum, speedMult.getValuef());
      b.run(deltaSec, targetVel);
    }
  }

  void removeExpiredBirds() {
    List<Bird> expired = new ArrayList<Bird>();
    for (Bird b : birds) {
      if (b.hasExpired) {
        expired.add(b);
      }
    }
    birds.removeAll(expired);
  }

  void render() {  // choose a rendering style
    renderPlasma();
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
    ColorPalette pal = paletteLibrary.get(palette.getOption());
    if (pal instanceof ZigzagPalette) {
      ((ZigzagPalette) pal).setBias(palBias.getValuef());
      ((ZigzagPalette) pal).setStart(palStart.getValuef());
      ((ZigzagPalette) pal).setStop(palStop.getValuef());
    }
    return pal;
  }

  void renderPlasma() {
    birds.parallelStream().forEach(new Consumer<Bird>() {
      public void accept(Bird bird) {
        renderPlasmaLayer(bird);
      }
    });

    final ColorPalette pal = getPalette();
    final double shift = palShift.getValue();

    Arrays.asList(model.points).parallelStream().forEach(new Consumer<LXPoint>() {
      public void accept(LXPoint point) {
        double sum = 0;
        for (Bird bird : birds) {
          sum += bird.renderedValues[point.index];
        }
        colors[point.index] = pal.getColor(sum + shift);
      }
    });
  }

  void renderPlasmaLayer(final Bird bird) {
    double waveNumber = detail.getValue();
    double extent = size.getValue();
    double rippleSpeed = ripple.getValue();
    double zFactor = FastMath.pow(10, zScale.getValue()/10);
    double zSqFactor = zFactor*zFactor - 1;
    double dx, dy, dz, sqDist, phase, a;

    LXPoint pos = new LXPoint(bird.pos.x, bird.pos.y, bird.pos.z);
    for (LXPoint point : modelIndex.pointsWithin(pos, size.getValuef())) {
      dx = (bird.pos.x - point.x)/extent;
      dy = (bird.pos.y - point.y)/extent;
      dz = (bird.pos.z - point.z)/extent;
      sqDist = dx*dx + dy*dy + dz*dz;
      phase = FastMath.sqrt(sqDist + dz*dz*zSqFactor);
      a = 1 - sqDist;
      bird.renderedValues[point.index] =
          a * a * bird.value
              * FastMath.sin(waveNumber * 2 * FastMath.PI * phase - bird.elapsedSec * rippleSpeed)
              * FastMath.cos(waveNumber * 5/4 * phase);
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

  public class Bird implements Comparable<Bird> {
    public PVector pos;
    public PVector vel;
    public PVector prevPos;
    public int rgb;
    public float value;
    public float elapsedSec;
    public boolean hasExpired;
    public double[] renderedValues;

    Bird(PVector pos, int rgb) {
      this.pos = pos;
      this.vel = PVector.mult(getRandomUnitVector(), scatter.getValuef());
      this.rgb = rgb;
      this.value = 0;
      this.elapsedSec = 0;
      this.hasExpired = false;
      this.renderedValues = new double[colors.length];
    }

    void run(float deltaSec, PVector targetVel) {
      advance(deltaSec);
      turn(deltaSec, targetVel);

      elapsedSec += deltaSec;
      if (elapsedSec < fadeInSec.getValuef()) {
        value = elapsedSec / fadeInSec.getValuef();
      } else {
        value = (float)FastMath.pow(0.1, (elapsedSec - fadeInSec.getValuef()) / fadeOutSec.getValuef());
        if (value < 0.001) hasExpired = true;
      }
    }

    void advance(float deltaSec) {
      prevPos = pos;
      pos.add(PVector.mult(vel, (float) deltaSec));
    }

    void turn(float deltaSec, PVector targetVel) {
      float speed = vel.mag();
      float targetSpeed = targetVel.mag();

      float frac = (float)FastMath.pow(0.1, deltaSec / turnSec.getValuef());
      vel = PVector.add(PVector.mult(vel, frac), PVector.mult(targetVel, 1 - frac));
      speed = speed * frac + targetSpeed * (1 - frac);
      if (targetSpeed > maxSpeed.getValuef()) targetSpeed = maxSpeed.getValuef();

      float mag = vel.mag();
      if (mag > 0 && mag < speed) vel.div(mag / speed);
    }

    public int compareTo(Bird other) {
      return Float.compare(pos.x, other.pos.x);
    }
  }
}

public class LightSource extends SLPattern {
  CompoundParameter x = new CompoundParameter("x", model.cx, model.xMin, model.xMax);
  CompoundParameter y = new CompoundParameter("y", model.cy, model.yMin, model.yMax);
  CompoundParameter z = new CompoundParameter("z", model.cz, model.zMin, model.zMax);
  CompoundParameter falloff = new CompoundParameter("falloff", 0.75, 0, 1);
  CompoundParameter gain = new CompoundParameter("gain", 1, 0, 3);

  public LightSource(LX lx) {
    super(lx);
    addParameter(x);
    addParameter(y);
    addParameter(z);
    addParameter(falloff);
    addParameter(gain);
  }

  public void run(double deltaMs) {
    PVector light = new PVector(x.getValuef(), y.getValuef(), z.getValuef());
    for (LXPoint p : model.points) {
      if (p instanceof LXPointNormal) {
        LXPointNormal pn = (LXPointNormal) p;
        PVector pv = new PVector(p.x, p.y, p.z);
        PVector toLight = PVector.sub(light, pv);
        float dist = toLight.mag();

        dist /= falloff.getValue();
        if (dist < 1) dist = 1; // avoid division by zero or excessive brightness
        float brightness = 1.0 / (dist * dist);

        float cosAngle = PVector.dot(toLight.normalize(), pn.normal);
        if (cosAngle < 0) cosAngle = 0;

        float value = cosAngle * brightness * gain.getValuef();
        colors[p.index] = LX.hsb(palette.getHuef(), 100f, 100f * (value > 1 ? 1 : value));
      }
    }
  }
}