import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import java.util.stream.Stream;
import java.util.function.Consumer;
import java.nio.IntBuffer;

import processing.core.PVector;

import org.apache.commons.math3.util.FastMath;

import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.DiscreteParameter;

import com.symmetrylabs.util.BlobTracker;
import com.symmetrylabs.util.ModelIndex;
import com.symmetrylabs.util.OctreeModelIndex;
import com.symmetrylabs.pattern.ThreadedPattern;

public class FlockWaveThreaded extends ThreadedPattern {

  private CompoundParameter timeScale = new CompoundParameter("timeScale", 1, 0, 1);  // time scaling factor
  private BooleanParameter oscBlobs = new BooleanParameter("oscBlobs");
  private CompoundParameter oscMergeRadius = new CompoundParameter("bMrgRad", 30, 0, 100);  // blob merge radius (in)
  private CompoundParameter oscMaxSpeed = new CompoundParameter("bMaxSpd", 240, 0, 1000);  // max blob speed (in/s)
  private CompoundParameter oscMaxDeltaSec = new CompoundParameter("bMaxDt", 0.5, 0, 1);  // max interval to calculate blob velocities (s)
  private CompoundParameter x = new CompoundParameter("x", model.cx, model.xMin, model.xMax);  // focus coordinates (in)
  private CompoundParameter y = new CompoundParameter("y", model.cy, model.yMin, model.yMax);
  private CompoundParameter z = new CompoundParameter("z", model.cz, model.zMin, model.zMax);
  private CompoundParameter zScale = new CompoundParameter("zScale", 0, -6, 12);  // z scaling factor (dB)

  private CompoundParameter spawnMinSpeed = new CompoundParameter("spnMin", 2, 0, 40);  // minimum focus speed (in/s) that spawns birds
  private CompoundParameter spawnMaxSpeed = new CompoundParameter("spnMax", 20, 0, 40);  // maximum focus speed (in/s) that spawns birds
  private CompoundParameter spawnRadius = new CompoundParameter("spnRad", 100, 0, 200);  // radius (in) within which to spawn birds
  private CompoundParameter density = new CompoundParameter("density", 1, 0, 2);  // maximum spawn rate (birds/s)
  private CompoundParameter scatter = new CompoundParameter("scatter", 100, 0, 1000);  // initial velocity randomness (in/s)
  private CompoundParameter speedMult = new CompoundParameter("spdMult", 1, 0, 2);  // (ratio) target bird speed / focus speed
  private CompoundParameter maxSpeed = new CompoundParameter("maxSpd", 10, 0, 100);  // max bird speed (in/s)
  private CompoundParameter turnSec = new CompoundParameter("turnSec", 1, 0, 2);  // time (s) to complete 90% of a turn
  private CompoundParameter fadeInSec = new CompoundParameter("fadeInSec", 0.5, 0, 2);  // time (s) to fade up to 100% intensity
  private CompoundParameter fadeOutSec = new CompoundParameter("fadeOutSec", 1, 0, 2);  // time (s) to fade down to 10% intensity
  private CompoundParameter size = new CompoundParameter("size", 100, 0, 2000);  // render radius of each bird (in)
  private CompoundParameter detail = new CompoundParameter("detail", 4, 0, 10);  // ripple spatial frequency (number of waves)
  private CompoundParameter ripple = new CompoundParameter("ripple", 0, -10, 10);  // ripple movement (waves/s)

  private DiscreteParameter palette = new DiscreteParameter("palette", skyPalettes.getNames());  // selected colour palette
  private CompoundParameter palShift = new CompoundParameter("palShift", 0, 0, 1);  // shift in colour palette (fraction 0 - 1)
  private CompoundParameter palBias = new CompoundParameter("palBias", 0, 0, 20);  // bias colour palette toward zero (dB)

  private PVector prevFocus = null;
  private Set<Bird> birds = new HashSet<Bird>();

  private BlobTracker blobTracker;

  private SimulationThread simThread;
  private ModelIndex modelIndex;
  private float[] colorValues;

  public FlockWaveThreaded(LX lx) {
    super(lx);

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
    addParameter(palShift);
    addParameter(palBias);

    blobTracker = BlobTracker.getInstance(lx);

    colorValues = new float[colors.length];
    modelIndex = new OctreeModelIndex(lx.model);
    simThread = new SimulationThread();

    //spawnBirds(1, new PVector(0, 0, 0), new PVector(10, 10, 10), 1000);
  }

  @Override
  public void onActive() {
    //simThread.start();
  }

  @Override
  public void onInactive() {
    //simThread.shutdown();
    //simThread = new SimulationThread();
  }

  private void fakeSimulate(float deltaSec) {
    PVector vel = new PVector(1, 1, 1);
    advanceBirds(deltaSec, vel);
  }

  private void advanceSimulation(float deltaSec) {
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

  private void updateBlobTrackerParameters() {
    blobTracker.setBlobY(y.getValuef());
    blobTracker.setMergeRadius(oscMergeRadius.getValuef());
    blobTracker.setMaxSpeed(oscMaxSpeed.getValuef());
    blobTracker.setMaxDeltaSec(oscMaxDeltaSec.getValuef());
  }

  private List<Bird> spawnBirds(float deltaSec, PVector focus, PVector vel, float weight) {
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

  private Bird spawnBird(PVector focus) {
    PVector pos = getRandomUnitVector();
    pos.mult(spawnRadius.getValuef());
    pos.add(focus);

    Bird newBird = new Bird(pos, LXColor.hsb(FastMath.random()*360, FastMath.random()*100, 100));
    birds.add(newBird);

    return newBird;
  }

  private void advanceBirds(final float deltaSec, PVector vel) {
    final PVector targetVel = PVector.mult(vel, speedMult.getValuef());
    birds.parallelStream().forEach(new Consumer<Bird>() {
      public void accept(Bird bird) {
        bird.run(deltaSec, targetVel);
      }
    });
  }

  private void advanceBirdsWithBlobs(float deltaSec, List<BlobTracker.Blob> blobs) {
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

  private List<Bird> removeExpiredBirds() {
    List<Bird> expired = new ArrayList<Bird>();
    for (Bird b : birds) {
      if (b.hasExpired) {
        expired.add(b);
      }
    }

    birds.removeAll(expired);

    return expired;
  }

  private PVector getRandomUnitVector() {
    PVector pos = new PVector();
    while (true) {
      pos.set((float)FastMath.random() * 2 - 1, (float)FastMath.random() * 2 - 1, (float)FastMath.random() * 2 - 1);
      if (pos.mag() < 1)
        return pos;
    }
  }

  @Override
  public synchronized void run(double deltaMs) {
    System.out.println("deltaMs: " + deltaMs + " / birds: " + birds.size());

    //fakeSimulate((float)deltaMs * 0.001f * timeScale.getValuef());
    advanceSimulation((float)deltaMs * 0.001f * timeScale.getValuef());

    Arrays.fill(colorValues, 0);

    final List<Bird> birdList = new ArrayList<Bird>(birds);
    birdList.parallelStream().forEach(new Consumer<Bird>() {
      public void accept(Bird bird) {
        renderBird(bird);
      }
    });

    super.run(deltaMs);
  }

  @Override
  public void render(double deltaMs, List<LXPoint> points, IntBuffer pointColors) {
    ColorPalette pal = skyPalettes.getPalette(palette.getOption());

    double bias = FastMath.pow(0.1, palBias.getValuef() / 10);

    for (int i = 0; i < points.size(); ++i) {
      LXPoint p = points.get(i);

      double val = colorValues[p.index];
      int c = pal.getColor(FastMath.signum(val) * FastMath.pow(FastMath.abs(val), bias) + palShift.getValuef());
      pointColors.put(i, c);
    }
  }

  protected void renderBird(final Bird bird) {
    final double waveNumber = detail.getValue();
    final double extent = size.getValue();
    final double rippleSpeed = ripple.getValue();
    final double zFactor = FastMath.pow(10, zScale.getValue() / 10);

    LXPoint bp = new LXPoint(bird.pos.x, bird.pos.y, bird.pos.z);
    List<LXPoint> nearbyPoints = modelIndex.pointsWithin(bp, (float)extent);
    //System.out.println("point count: " + nearbyPoints.size());

    nearbyPoints.stream().forEach(new Consumer<LXPoint>() {
      public void accept(LXPoint p) {
        double dx = bird.pos.x - p.x;
        double dy = bird.pos.y - p.y;
        double dz = bird.pos.z - p.z;
        double squareDistRatio = (dx * dx + dy * dy + dz * dz) / (extent * extent);
        if (squareDistRatio < 1) {
          double phase = FastMath.sqrt(dx * dx + dy * dy + dz * dz * zFactor * zFactor) / extent;
          double a = 1 - squareDistRatio;
          colorValues[p.index] += a * a * bird.value
              * FastMath.sin(waveNumber * 2 * Math.PI * phase - bird.elapsedSec * rippleSpeed)
              * FastMath.cos(waveNumber * 5 / 4 * phase);
        }
      }
    });
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
        value = (float) FastMath.pow(0.1, (elapsedSec - fadeInSec.getValuef()) / fadeOutSec.getValuef());
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

      float frac = (float) FastMath.pow(0.1, deltaSec / turnSec.getValuef());
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

  private class SimulationThread extends Thread {
    private final int PERIOD = 8;

    private boolean running = true;

    public void shutdown() {
      running = false;
      interrupt();
    }

    @Override
    public void run() {
      long lastTime = System.currentTimeMillis();
      while (running) {
        long t = System.currentTimeMillis();
        double deltaMs = t - lastTime;

        synchronized (FlockWaveThreaded.this) {
          advanceSimulation((float)deltaMs * 0.001f * timeScale.getValuef());
        }

        long elapsed = System.currentTimeMillis() - lastTime;
        lastTime = t;

        if (elapsed < PERIOD) {
          try {
            sleep(PERIOD - elapsed);
          }
          catch (InterruptedException e) { /* pass */ }
        }
      }
    }
  }
}
