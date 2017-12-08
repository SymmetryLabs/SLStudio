import heronarts.lx.model.LXAbstractFixture;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.transform.LXTransform;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static processing.core.PApplet.*;

final static float INCHES = 1;
final static float FEET = 12*INCHES;
  /**
 *     DOUBLE BLACK DIAMOND        DOUBLE BLACK DIAMOND
 *
 *         //\\   //\\                 //\\   //\\  
 *        ///\\\ ///\\\               ///\\\ ///\\\
 *        \\\/// \\\///               \\\/// \\\///
 *         \\//   \\//                 \\//   \\//
 *
 *        EXPERTS ONLY!!              EXPERTS ONLY!!
 *
 * Contains the model definitions for the cube structures.
 */



/**
 * Top-level model of the entire sculpture. This contains a list of
 * every cube on the sculpture, which forms a hierarchy of faces, strips
 * and points.
 */

public static class SLModel extends LXModel {
  public final List<LXModel> objModels;

  // Suns
  public final List<Sun> suns;
  private final Map<String, Sun> sunTable;
  public final Sun masterSun;

  // Slices
  public final List<Slice> slices;
  private final Map<String, Slice> sliceTable;

  // Strips
  public final List<Strip> strips;

  public final int NUM_POINT_BATCHES = 16;

  // Array of points stored as contiguous floats for performance
  private final float[] pointsArray;
  private final List<PointBatch> pointBatches = new ArrayList<PointBatch>(NUM_POINT_BATCHES);

  public SLModel(List<Sun> suns) {
    super(new Fixture(suns));
    Fixture fixture = (Fixture)this.fixtures.get(0);

    // Suns
    List<Sun> sunList = new ArrayList<Sun>();
    Map<String, Sun> _sunTable = new HashMap<String, Sun>();

    // Slices
    List<Slice> sliceList = new ArrayList<Slice>();
    Map<String, Slice> _sliceTable = new HashMap<String, Slice>();

    // Strips
    List<Strip> stripList = new ArrayList<Strip>();

    for (Sun sun : suns) {
      sunList.add(sun);
      _sunTable.put(sun.id, sun);

      for (Slice slice : sun.slices) {
        sliceList.add(slice);
        _sliceTable.put(slice.id, slice);

        for (Strip strip : slice.strips) {
          stripList.add(strip);
        }
      }
    }

    masterSun = _sunTable.get("sun9"); // a full sun
    for (Sun sun : suns) {
      if (sun != masterSun) {
        sun.computeMasterIndexes(masterSun);
      }
    }

    this.objModels  = new ArrayList<LXModel>();
    
    // Suns
    this.suns       = Collections.unmodifiableList(sunList);
    this.sunTable   = Collections.unmodifiableMap(_sunTable);

    // Slices
    this.slices     = Collections.unmodifiableList(sliceList);
    this.sliceTable = Collections.unmodifiableMap(_sliceTable);

    // Strips
    this.strips     = Collections.unmodifiableList(stripList);

    this.pointsArray = new float[this.points.length * 3];
    for (int i = 0; i < this.points.length; i++) {
      LXPoint point = this.points[i];
      this.pointsArray[3 * i] = point.x;
      this.pointsArray[3 * i + 1] = point.y;
      this.pointsArray[3 * i + 2] = point.z;
    }

    int batchStride = ceil(this.points.length / NUM_POINT_BATCHES);
    for (int i = 0; i < NUM_POINT_BATCHES; i++) {
      int start = i * batchStride;
      int end = min(start + batchStride, this.points.length - 1);
      this.pointBatches.add(new PointBatch(start, end));
    }
  }

  private static class Fixture extends LXAbstractFixture {
    private Fixture(List<Sun> suns) {
      for (Sun sun : suns) {
        for (LXPoint point : sun.points) {
          this.points.add(point);
        }
      }
    }
  }

  public Sun getSunById(String id) {
    return this.sunTable.get(id);
  }

  public Slice getSliceById(String id) {
    Slice slice = sliceTable.get(id);
    if (slice == null) {
      println("Missing slice id: " + id);
      print("Valid ids: ");
      for (String key : sliceTable.keySet()) {
        print(key + ", ");
      }
      println();
      throw new IllegalArgumentException("Invalid slice id:" + id);
    }
    return slice;
  }

  public void forEachPoint(final BatchConsumer consumer) {
    this.pointBatches.parallelStream().forEach(new Consumer<PointBatch>() {
      public void accept(PointBatch batch) {
        consumer.accept(batch.start, batch.end);
      }
    });
  }

  private class PointBatch {
    int start;
    int end;
    PointBatch(int start, int end) {
      this.start = start;
      this.end = end;
    }
  }
}

public static interface BatchConsumer {
  public void accept(int start, int end);
}

public static class BoundingBox {
  public final PVector origin;
  public final PVector size;

  public BoundingBox(float x, float y, float z, float xSize, float ySize, float zSize) {
    origin = new PVector(x, y, z);
    size = new PVector(xSize, ySize, zSize);
  }
}

public static class Sun extends LXModel {

  public enum Type {
    FULL, TWO_THIRDS, ONE_HALF, ONE_THIRD
  };

  public final String id;
  public final Type type;
  public final List<Slice> slices;
  public final List<Strip> strips;
  private final Map<String, Slice> sliceTable;

  public BoundingBox boundingBox;
  public final PVector center;
  public final float[] distances;

  public Sun masterSun;
  public int[] masterIndexes;

  public Sun(String id, Type type, float[] coordinates, float[] rotations, LXTransform transform, int[][] numPointsPerStrip) {
    super(new Fixture(id, type, coordinates, rotations, transform, numPointsPerStrip));
    Fixture fixture = (Fixture)this.fixtures.get(0);

    this.id = id;
    this.type = type;
    this.slices = Collections.unmodifiableList(fixture.slices);
    this.strips = Collections.unmodifiableList(fixture.strips);
    this.sliceTable = new HashMap<String, Slice>();
    this.center = fixture.origin;

    for (Slice slice : slices) {
      sliceTable.put(slice.id, slice);
    }

    computeBoundingBox();
    distances = computeDistances();
  }

  void computeMasterIndexes(Sun masterSun) {    
    this.masterSun = masterSun;
    masterIndexes = new int[points.length];
    new ComputeMasterIndexThread().start();
  }
  
  class ComputeMasterIndexThread extends Thread {
    ComputeMasterIndexThread() { }
    
    public void run() {
      try {
        Thread.sleep((int) (Math.random()*5000));
      } catch (InterruptedException e) { }
      float cx = center.x;
      float cy = center.y;
      float cz = center.z;
      float mcx = masterSun.center.x;
      float mcy = masterSun.center.y;
      float mcz = masterSun.center.z;
      
      for (int i = 0; i < points.length; i++) {
        float minSqDist = 1e18;
        masterIndexes[i] = 0;
        float px = (points[i].x - cx);
        float py = (points[i].y - cy);
        float pz = (points[i].z - cz);
        float xLow = px - 12;
        float xHigh = px + 12;
        float yLow = py - 12;
        float yHigh = py + 12;
        for (int j = 0; j < masterSun.points.length; j++) {
          LXPoint masterPoint = masterSun.points[j];
          float mx = masterPoint.x - mcx;
          float my = masterPoint.y - mcy;
          if (mx > xLow && mx < xHigh && my > yLow && my < yHigh) {
            float dx = px - mx;
            float dy = py - (masterPoint.y - mcy);
            float dz = pz - (masterPoint.z - mcz);
            float sqDist = dx*dx + dy*dy + dz*dz;
            if (sqDist < minSqDist) {
              minSqDist = sqDist;
              masterIndexes[i] = masterPoint.index;
            }
          }
        }
      }
      println("computed master indexes for " + id);
    }
  }

  void copyFromMasterSun(int[] colors) {
    if (masterIndexes != null) {
      for (int i = 0; i < points.length; i++) {
        colors[points[i].index] = colors[masterIndexes[i]];
      }
    }
  }

  void computeBoundingBox() {
    // So, this used to be done using Float.MIN_VALUE and Float.MAX_VALUE and using simple </> checks, rather than null.
    // In some cases (suns 3, 5 and 8), the xMax value remained at Float.MIN_VALUE, which makes no sense at all, but
    // I didn't have time (or a debugger >_<) to figure it out. So, I replaced the logic with null-based logic. - Yona
    Float xMin = null;
    Float xMax = null;

    Float yMin = null;
    Float yMax = null;

    Float zMin = null;
    Float zMax = null;

    LXPoint xMinPt = null;
    LXPoint xMaxPt = null;
    LXPoint yMinPt = null;
    LXPoint yMaxPt = null;
    LXPoint zMinPt = null;
    LXPoint zMaxPt = null;

    for (LXPoint p : points) {
      if (xMin == null || p.x < xMin) { xMin = p.x; xMinPt = p; }
      if (xMax == null || p.x > xMax) { xMax = p.x; xMaxPt = p; }

      if (yMin == null || p.y < yMin) { yMin = p.y; yMinPt = p; }
      if (yMax == null || p.y > yMax) { yMax = p.y; yMaxPt = p; }

      if (zMin == null || p.z < zMin) { zMin = p.z; zMinPt = p; }
      if (zMax == null || p.z > zMax) { zMax = p.z; zMaxPt = p; }
    }


    boundingBox = new BoundingBox(xMin, yMin, zMin, xMax - xMin, yMax - yMin, zMax - zMin);

//    if (xMinPt == null) println(id + "-xMin: NULL!!"); else println(id + "-xMin: (" + xMinPt.x + ", " + xMinPt.y + ", " + xMinPt.z + ")");
//    if (xMaxPt == null) println(id + "-xMax: NULL!!"); else println(id + "-xMax: (" + xMaxPt.x + ", " + xMaxPt.y + ", " + xMaxPt.z + ")");
//    if (yMinPt == null) println(id + "-yMin: NULL!!"); else println(id + "-yMin: (" + yMinPt.x + ", " + yMinPt.y + ", " + yMinPt.z + ")");
//    if (yMaxPt == null) println(id + "-yMax: NULL!!"); else println(id + "-yMax: (" + yMaxPt.x + ", " + yMaxPt.y + ", " + yMaxPt.z + ")");
//    if (zMinPt == null) println(id + "-zMin: NULL!!"); else println(id + "-zMin: (" + zMinPt.x + ", " + zMinPt.y + ", " + zMinPt.z + ")");
//    if (zMaxPt == null) println(id + "-zMax: NULL!!"); else println(id + "-zMax: (" + zMaxPt.x + ", " + zMaxPt.y + ", " + zMaxPt.z + ")");
  }

  float[] computeDistances() {
    float[] distances = new float[points.length];
    for (int i = 0; i < points.length; i++) {
      LXPoint p = points[i];
      distances[i] = PVector.sub(center, new PVector(p.x, p.y, p.z)).mag();
    }
    return distances;
  }

  public Slice getSliceById(String id) {
    Slice slice = sliceTable.get(id);
    if (slice == null) throw new IllegalArgumentException("Invalid slice id:" + id);
    return slice;
  }

  private static class Fixture extends LXAbstractFixture {

    private final List<Slice> slices = new ArrayList<Slice>();
    private final List<Strip> strips = new ArrayList<Strip>();
    public final PVector origin;

    private Fixture(String id, Sun.Type type, float[] coordinates, float[] rotations, LXTransform transform, int[][] numPointsPerStrip) {
      transform.push();

      origin = new PVector(coordinates[0], coordinates[1], coordinates[2]);
      if (type == Sun.Type.FULL) {
        origin.y += Slice.RADIUS + 18;
      }
      if (type == Sun.Type.TWO_THIRDS) {
        origin.y += 22*Slice.STRIP_SPACING;
      }
      if (type == Sun.Type.ONE_THIRD) {
        origin.y -= 22*Slice.STRIP_SPACING;
      }

      transform.push();

      transform.translate(origin.x, origin.y, origin.z);
      transform.rotateX(rotations[0] * PI / 180);
      transform.rotateY(rotations[1] * PI / 180);
      transform.rotateZ(rotations[2] * PI / 180);

      // create slices...
      if (type != Sun.Type.ONE_THIRD) {
        slices.add(new Slice(id + "_top_front", Slice.Type.FULL, new float[] {-Slice.RADIUS, Slice.RADIUS, 0}, new float[] {0,   0, 0}, transform, numPointsPerStrip[0]));
        slices.add(new Slice(id + "_top_back",  Slice.Type.FULL, new float[] {Slice.RADIUS, Slice.RADIUS, 0}, new float[] {0, 180, 0}, transform, numPointsPerStrip[1]));
      }

      switch (type) {
        case FULL:
          slices.add(new Slice(id + "_bottom_front", Slice.Type.FULL, new float[] {Slice.RADIUS, -Slice.DIAMETER*0.5, 0}, new float[] {0,   0, 180}, transform, numPointsPerStrip[2]));
          slices.add(new Slice(id + "_bottom_back",  Slice.Type.FULL, new float[] {-Slice.RADIUS, -Slice.DIAMETER*0.5, 0}, new float[] {0, 180, 180}, transform, numPointsPerStrip[3]));
          break;

        case TWO_THIRDS:
          slices.add(new Slice(id + "_bottom_front", Slice.Type.BOTTOM_ONE_THIRD, new float[] {Slice.RADIUS, -Slice.RADIUS+1.5, 0}, new float[] {0,   0, 180}, transform, numPointsPerStrip[2]));
          slices.add(new Slice(id + "_bottom_back",  Slice.Type.BOTTOM_ONE_THIRD, new float[] {-Slice.RADIUS, -Slice.RADIUS+1.5, 0}, new float[] {0, 180, 180}, transform, numPointsPerStrip[3]));

        case ONE_HALF:
          // already done
          break;

        case ONE_THIRD:
          slices.add(new Slice(id + "_top_front", Slice.Type.TWO_THIRDS, new float[] {-Slice.RADIUS, Slice.RADIUS, 0}, new float[] {0,   0, 0}, transform, numPointsPerStrip[0]));
          slices.add(new Slice(id + "_top_back",  Slice.Type.TWO_THIRDS, new float[] {Slice.RADIUS, Slice.RADIUS, 0}, new float[] {0, 180, 0}, transform, numPointsPerStrip[1]));
          break;
      }

      // add pointers to strips
      for (Slice slice : slices) {
        for (Strip strip : slice.strips) {
          strips.add(strip);
          for (LXPoint point : strip.points) {
            points.add(point);
            // estimate normals
            PVector pos = new PVector(point.x, point.y, point.z);
            PVector normal = PVector.sub(pos, origin);
            normal.normalize();
            normal.z += (normal.z > origin.z) ? 0.3 : -0.3;
            ((LXPointNormal) point).setNormal(normal);
          }
        }
      }

      transform.pop();
      transform.pop();
    }
  }
}

public static class Slice extends LXModel {

  public enum Type {
    FULL, TWO_THIRDS, BOTTOM_ONE_THIRD
  };

  private static final int MAX_NUM_STRIPS_PER_SLICE = 69;
  private static final float STRIP_SPACING = 0.7;
  public final static float DIAMETER = 8*FEET;
  public final static float RADIUS = DIAMETER/2;

  public final String id;
  public final Type type;
  public final List<Strip> strips;
  private final Map<String, Strip> stripMap;

  public Slice(String id, Type type, float[] coordinates, float[] rotations, LXTransform transform, int[] numPointsPerStrip) {
    super(new Fixture(id, type, coordinates, rotations, transform, numPointsPerStrip));
    Fixture fixture = (Fixture)this.fixtures.get(0);

    this.id = id;
    this.type = type;
    this.strips = Collections.unmodifiableList(fixture.strips);
    this.stripMap = new HashMap<String, Strip>();

    for (Strip strip : strips) {
      stripMap.put(strip.id, strip);
    }
  }

  // These are different than sun and slice ids, which are unique. These ids are all the same slice to slice.
  public Strip getStripById(String id) {
    return stripMap.get(id);
  }

  private static class Fixture extends LXAbstractFixture {

    private List<Strip> strips = new ArrayList<Strip>();

    private Fixture(String id, Slice.Type type, float[] coordinates, float[] rotations, LXTransform transform, int[] numPointsPerStrip) {
      transform.push();
      transform.translate(coordinates[0], coordinates[1], coordinates[2]);
      transform.rotateX(rotations[0] * PI / 180);
      transform.rotateY(rotations[1] * PI / 180);
      transform.rotateZ(rotations[2] * PI / 180);

      int numStrips = numPointsPerStrip.length;

      // create curved strips...
      int counter = 0;
      if (type != Slice.Type.BOTTOM_ONE_THIRD) {
        for (int i = 0; i < MAX_NUM_STRIPS_PER_SLICE; i++) {
          if (type == Slice.Type.TWO_THIRDS && i > 45) {
            break;
          }

          int numPoints = numPointsPerStrip[counter++];
          float stripWidth = numPoints * CurvedStrip.PIXEL_PITCH / 2.6;
          float stripX = (DIAMETER - stripWidth) / 2;

          CurvedStrip.CurvedMetrics metrics = new CurvedStrip.CurvedMetrics(stripWidth, numPoints);
          strips.add(new CurvedStrip(Integer.toString(i+1), metrics, new float[] {stripX, -i*STRIP_SPACING, 0}, new float[] {0, 0, 0}, transform));
        }
      } else {
        for (int i = 45; i < MAX_NUM_STRIPS_PER_SLICE-2; i++) {
          int numPoints = numPointsPerStrip[counter++];
          float stripWidth = numPoints * CurvedStrip.PIXEL_PITCH / 2.6;
          float stripX = (DIAMETER - stripWidth) / 2;

          CurvedStrip.CurvedMetrics metrics = new CurvedStrip.CurvedMetrics(stripWidth, numPoints);
          strips.add(new CurvedStrip(Integer.toString(i+1), metrics, new float[] {stripX, -i*STRIP_SPACING, 0}, new float[] {0, 0, 0}, transform));
        }
      }

      for (Strip strip : strips) {
        for (LXPoint point : strip.points) {
          this.points.add(point);
        }
      }

      transform.pop();
    }
  }
}

public static class CurvedStrip extends Strip {

  public static final int LEDS_PER_METER = 60;
  public static final float INCHES_PER_METER = 39.3701;
  public static final float PIXEL_PITCH = LEDS_PER_METER / INCHES_PER_METER;

  private static int counter = 0;

  public static class CurvedMetrics {
    public final Strip.Metrics metrics;
    public final float pitch;
    public final int numPoints;
    public final float arcWidth;

    public CurvedMetrics(float arcWidth, int numPoints) {
      this.metrics = new Strip.Metrics(numPoints, PIXEL_PITCH);
      this.pitch = metrics.POINT_SPACING;
      this.numPoints = metrics.numPoints;
      this.arcWidth = arcWidth;
    }
  }

  public CurvedStrip(String id, CurvedMetrics metrics, float[] coordinates, float[] rotations, LXTransform transform) {
    super(id, metrics.metrics, new Fixture(id, metrics, coordinates, rotations, transform).getPoints());
  }

  private static class Fixture extends LXAbstractFixture {
    private Fixture(String id, CurvedMetrics metrics, float[] coordinates, float[] rotations, LXTransform transform) {
      transform.push();
      transform.translate(coordinates[0], coordinates[1], coordinates[2]);
      transform.rotateX(rotations[1] * PI / 180);
      transform.rotateY(rotations[2] * PI / 180);
      transform.rotateZ(rotations[0] * PI / 180);

      for (int i = 0; i < metrics.numPoints; i++) {
        transform.push();
        float t = i / (float)metrics.numPoints;
        float x = bezierPoint(0, metrics.arcWidth*0.2, metrics.arcWidth*0.8, metrics.arcWidth, t);
        float z = bezierPoint(0, metrics.arcWidth*-0.3,metrics.arcWidth*-0.3, 0, t);
        transform.translate(x, 0, z);

        points.add(new LXPointNormal(
            transform.x(), transform.y(), transform.z()));
        transform.pop();
      }

      transform.pop();
    }

    private float bezierPoint(float a, float b, float c, float d, float t) {
      float t1 = 1.0f - t;
      return ((a * t1) + (3 * b * t)) * (t1 * t1) + ((3 * c * t1) + (d * t)) * (t * t);
    }
  }
}

/**
 * A strip run of points
 */
public static class Strip extends LXModel {

  public final String id;

  public static final float INCHES_PER_METER = 39.3701;

  public static class Metrics {

    public final float length;
    public final int numPoints;
    public final int ledsPerMeter;

    public final float POINT_SPACING;

    public Metrics(float length, int numPoints, int ledsPerMeter) {
      this.length = length;
      this.numPoints = numPoints;
      this.ledsPerMeter = ledsPerMeter;
      this.POINT_SPACING = INCHES_PER_METER / ledsPerMeter;
    }

    public Metrics(int numPoints, float spacing) {
      this.length = numPoints * spacing;
      this.numPoints = numPoints;
      this.ledsPerMeter = (int)floor((INCHES_PER_METER / this.length) * numPoints);
      this.POINT_SPACING = spacing;
    }
  }

  public final Metrics metrics;

  /**
   * Whether this is a horizontal strip
   */
  public final boolean isHorizontal;

  /**
   * Rotation about the y axis
   */
  public final float ry;

  public Object obj1 = null, obj2 = null;

  Strip(String id, Metrics metrics, List<LXPoint> points) {
    super(points);
    this.id = id;
    this.isHorizontal = true;
    this.metrics = metrics;
    this.ry = 0;
  }
}

public static class LXPointNormal extends LXPoint {
  /** The normal vector is always a unit vector. */
  public final PVector normal;

  public LXPointNormal(float x, float y, float z, PVector normal) {
    super(x, y, z);
    this.normal = new PVector(0, 1, 0); // the default normal points up
    setNormal(normal);
  }

  public LXPointNormal(double x, double y, double z, PVector normal) {
    this((float) x, (float) y, (float) z, normal);
  }

  public LXPointNormal(float x, float y, float z) {
    this(x, y, z, null);
  }

  public LXPointNormal(double x, double y, double z) {
    this(x, y, z, null);
  }

  public void setNormal(PVector normal) {
    if (normal != null && normal.mag() > 0) {
      this.normal.set(normal.x, normal.y, normal.z);
      this.normal.normalize();
    }
  }
}