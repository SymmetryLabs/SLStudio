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

  private ModelIndex modelIndex;
  private Map<Bird, float[]> colorLayers = new HashMap<Bird, float[]>();
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
  Bird spawnBird(PVector focus) {
    Bird bird = super.spawnBird(focus);

    synchronized (colorLayers) {
      colorLayers.put(bird, new float[colors.length]);
    }

    return bird;
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
  List<Bird> removeExpiredBirds() {
    List<Bird> expired = super.removeExpiredBirds();

    synchronized (colorLayers) {
      colorLayers.keySet().removeAll(expired);
    }

    return expired;
  }

  @Override
  public void run(double deltaMs) {
    System.out.println("deltaMs: " + deltaMs + " / birds: " + birds.size());

    //fakeSimulate((float)deltaMs * 0.001f * timeScale.getValuef());
    advanceSimulation((float)deltaMs * 0.001f * timeScale.getValuef());

    final List<Bird> birdList = new ArrayList<Bird>(birds);
    final Map<Bird, float[]> layersMap = new HashMap<Bird, float[]>(colorLayers);
    final List<float[]> layersList = new ArrayList<float[]>(birds.size());

    synchronized (colorLayers) {
      for (Bird bird : birdList) {
        if (colorLayers.containsKey(bird)) {
          layersList.add(colorLayers.get(bird));
        }
      }
    }

    birdList.parallelStream().forEach(new Consumer<Bird>() {
      public void accept(Bird bird) {
        if (layersMap.containsKey(bird)) {
          renderBird(bird, layersMap.get(bird));
        }
      }
    });

    Arrays.asList(model.points).parallelStream().forEach(new Consumer<LXPoint>() {
      public void accept(LXPoint point) {
        int c = 0;
        for (float[] layer : layersList) {
          c += layer[point.index];
        }

        colorValues[point.index] = c;
      }
    });

    ColorPalette pal = getPalette();
    float shift = palShift.getValuef();
    for (LXPoint point : model.points) {
      double val = colorValues[point.index];
      colors[point.index] = pal.getColor(val + shift);
    }
  }

  void renderBird(final Bird bird, final float[] colorLayer) {
    final double waveNumber = detail.getValue();
    final double extent = size.getValue();
    final double rippleSpeed = ripple.getValue();
    final double zFactor = FastMath.pow(10, zScale.getValue() / 10);

    LXPoint bp = new LXPoint(bird.pos.x, bird.pos.y, bird.pos.z);
    List<LXPoint> nearbyPoints = modelIndex.pointsWithin(bp, (float)extent);
    //System.out.println("point count: " + nearbyPoints.size());

    for (LXPoint point : nearbyPoints) {
      colorLayer[point.index] = (float)renderPlasma(bird, point);
    }
  }
}
