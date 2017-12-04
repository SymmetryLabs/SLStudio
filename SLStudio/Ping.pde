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
    float shift = palShift.getValuef();
    if (pal instanceof ZigzagPalette) {
      ((ZigzagPalette) pal).setBias(palBias.getValuef());
      ((ZigzagPalette) pal).setStart(palStart.getValuef());
      ((ZigzagPalette) pal).setStop(palStop.getValuef());
    }
    for (LXPoint p : model.points) {
      colors[p.index] = pal.getColor((p.y - model.yMin) / (model.yMax - model.yMin) + shift);
    }
  }
}

public class BlobViewer extends SLPattern {
  CompoundParameter tolerance = new CompoundParameter("tolerance", 2, 0, 12); // in
  private BlobTracker blobTracker;

  public BlobViewer(LX lx) {
    super(lx);
    addParameter(tolerance);
    blobTracker = BlobTracker.getInstance(lx);
  }

  public void run(double deltaMs) {
    List<BlobTracker.Blob> blobs = blobTracker.getBlobs();
    int[] planeColors = {0xffff0000, 0xff00ff00, 0xff0000ff};
    float tol = tolerance.getValuef();

    for (LXPoint p : model.points) {
      int c = 0;
      for (int b = 0; b < blobs.size(); b++) {
        PVector pos = blobs.get(b).pos;
        if (Math.abs(p.x - pos.x) < tol ||
            Math.abs(p.y - pos.y) < tol ||
            Math.abs(p.z - pos.z) < tol) {
          c = c | planeColors[b % planeColors.length];
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
  CompoundParameter oscMaxSpeed = new CompoundParameter("bMaxSpd", 240, 0, 1000);  // max blob speed (in/s)
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

  public FlockWave(LX lx) {
    super(lx);

    blobTracker = BlobTracker.getInstance(lx);

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
    renderBirds();
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

  List<Bird> spawnBirds(float deltaSec, PVector focus, PVector vel, float weight) {
    float spawnMin = spawnMinSpeed.getValuef();
    float spawnMax = spawnMaxSpeed.getValuef();
    float speed = vel.mag();
    float numToSpawn = deltaSec * density.getValuef() * weight * (speed - spawnMin) / (spawnMax - spawnMin);

    List<Bird> newBirds = new ArrayList<Bird>();

    while (numToSpawn >= 1.0) {
      newBirds.add(spawnBird(focus));
      numToSpawn -= 1.0;
    }
    if (FastMath.random() < numToSpawn) {
      newBirds.add(spawnBird(focus));
    }

    return newBirds;
  }

  Bird spawnBird(PVector focus) {
    PVector pos = getRandomUnitVector();
    pos.mult(spawnRadius.getValuef());
    pos.add(focus);

    Bird bird = new Bird(pos, LXColor.hsb(FastMath.random()*360, FastMath.random()*100, 100));
    birds.add(bird);

    return bird;
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

  List<Bird> removeExpiredBirds() {
    List<Bird> expired = new ArrayList<Bird>();
    for (Bird b : birds) {
      if (b.hasExpired) {
        expired.add(b);
      }
    }

    birds.removeAll(expired);

    return expired;
  }

  void renderBirds() {
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

  SortedSet<Bird> getSortedSet(Set<Bird> birds) {
    SortedSet<Bird> result = new TreeSet<Bird>();
    for (Bird b : birds) {
      result.add(b);
    }
    return result;
  }

  SortedSet<Bird> getSubSet(SortedSet<Bird> birds, float xLow, float xHigh) {
    Bird low = new Bird(new PVector(xLow, 0, 0), 0);
    Bird high = new Bird(new PVector(xHigh, 0, 0), 0);
    // return birds.subSet(low, high);
    TreeSet<Bird> result = new TreeSet<Bird>();
    for (Bird b : birds) {
      if (b.compareTo(low) >= 0 && b.compareTo(high) < 0) result.add(b);
    }
    return result;
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
    float extent = size.getValuef();
    SortedSet<Bird> sortedBirds = getSortedSet(birds);
    Bird low = new Bird(new PVector(0, 0, 0), 0);
    Bird high = new Bird(new PVector(0, 0, 0), 0);

    double zFactor = Math.pow(10, zScale.getValuef()/10);
    ColorPalette pal = getPalette();
    float shift = palShift.getValuef();

    for (LXPoint p : model.points) {
      low.pos.x = p.x - extent;
      high.pos.x = p.x + extent;
      double sum = 0;
      for (Bird b : sortedBirds.subSet(low, high)) {
        sum += renderPlasma(b, p);
      }
      colors[p.index] = pal.getColor(sum + shift);
    }
  }

  final double renderPlasma(Bird bird, LXPoint point) {
    float waveNumber = detail.getValuef();
    float extent = size.getValuef();
    float rippleSpeed = ripple.getValuef();
    double zFactor = FastMath.pow(10, zScale.getValuef()/10);

    double dx = bird.pos.x - point.x;
    double dy = bird.pos.y - point.y;
    double dz = bird.pos.z - point.z;
    if (FastMath.abs(dy) < extent) {
      double sqDist = (dx*dx + dy*dy + dz*dz) / (extent*extent);
      if (sqDist < 1) {
        double phase = FastMath.sqrt(dx*dx + dy*dy + dz*dz*zFactor*zFactor) / extent;
        double a = 1 - sqDist;
        return a * a * bird.value
            * FastMath.sin(waveNumber * 2 * FastMath.PI * phase - bird.elapsedSec * rippleSpeed)
            * FastMath.cos(waveNumber * 5/4 * phase);
      }
    }

    return 0;
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

    Bird(PVector pos, int rgb) {
      this.pos = pos;
      this.vel = PVector.mult(getRandomUnitVector(), scatter.getValuef());
      this.rgb = rgb;
      this.value = 0;
      this.elapsedSec = 0;
      this.hasExpired = false;
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
