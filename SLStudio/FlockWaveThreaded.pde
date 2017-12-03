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

public class FlockWaveThreaded extends FlockWave {

  private PVector prevFocus = null;
  private Set<Bird> birds = new HashSet<Bird>();

  private ModelIndex modelIndex;
  private float[] colorValues;

  public FlockWaveThreaded(LX lx) {
    super(lx);

    colorValues = new float[colors.length];
    modelIndex = new OctreeModelIndex(lx.model);

    //spawnBirds(1, new PVector(0, 0, 0), new PVector(10, 10, 10), 1000);
  }

  void fakeSimulate(float deltaSec) {
    PVector vel = new PVector(1, 1, 1);
    advanceBirds(deltaSec, vel);
  }

  @Override
  void advanceBirds(final float deltaSec, PVector vel) {
    final PVector targetVel = PVector.mult(vel, speedMult.getValuef());
    birds.parallelStream().forEach(new Consumer<Bird>() {
      public void accept(Bird bird) {
        bird.run(deltaSec, targetVel);
      }
    });
  }

  @Override
  public void run(double deltaMs) {
    System.out.println("deltaMs: " + deltaMs + " / birds: " + birds.size());

    //fakeSimulate((float)deltaMs * 0.001f * timeScale.getValuef());
    advanceSimulation((float)deltaMs * 0.001f * timeScale.getValuef());

    Arrays.fill(colorValues, 0);

    final List<Bird> birdList = new ArrayList<Bird>(birds);
    birdList.stream().forEach(new Consumer<Bird>() {
      public void accept(Bird bird) {
        renderBird(bird);
      }
    });

    ColorPalette pal = getPalette();
    float shift = palShift.getValuef();
    for (LXPoint point : model.points) {
      double val = colorValues[point.index];
      colors[point.index] = pal.getColor(val + shift);
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
}
