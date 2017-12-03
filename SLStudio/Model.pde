import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.function.Function;
import java.util.function.IntFunction;

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

  // Slices
  public final List<Slice> slices;
  private final Map<String, Slice> sliceTable;

  // Strips
  public final List<Strip> strips;

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

    this.objModels  = new ArrayList<LXModel>();
    
    // Suns
    this.suns       = Collections.unmodifiableList(sunList);
    this.sunTable   = Collections.unmodifiableMap(_sunTable);

    // Slices
    this.slices     = Collections.unmodifiableList(sliceList);
    this.sliceTable = Collections.unmodifiableMap(_sliceTable);

    // Strips
    this.strips     = Collections.unmodifiableList(stripList);
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
    return this.sliceTable.get(id);
  }
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

  public final BoundingBox boundingBox;

  public Sun(String id, Type type, float[] coordinates, float[] rotations, LXTransform transform, int[][] numPointsPerStrip) {
    super(new Fixture(id, type, coordinates, rotations, transform, numPointsPerStrip));
    Fixture fixture = (Fixture)this.fixtures.get(0);

    this.id = id;
    this.type = type;
    this.slices = Collections.unmodifiableList(fixture.slices);
    this.strips = Collections.unmodifiableList(fixture.strips);
    this.sliceTable = new HashMap<String, Slice>();

    for (Slice slice : slices) {
      sliceTable.put(slice.id, slice);
    }

    float xMin = Float.MAX_VALUE;
    float xMax = Float.MIN_VALUE;
    float yMin = Float.MAX_VALUE;
    float yMax = Float.MIN_VALUE;
    float zMin = Float.MAX_VALUE;
    float zMax = Float.MIN_VALUE;

    for (LXPoint p : getPoints()) {
      if (p.x < xMin) xMin = p.x;
      if (p.x > xMax) xMax = p.x;
      if (p.y < yMin) yMin = p.y;
      if (p.y > yMax) yMax = p.y;
      if (p.z < zMin) zMin = p.z;
      if (p.z > zMax) zMax = p.z;
    }

    boundingBox = new BoundingBox(xMin, yMin, zMin, xMax - xMin, yMax - yMin, zMax - zMin);
  }

  public Slice getSliceById(String id) {
    return sliceTable.get(id);
  }

  private static class Fixture extends LXAbstractFixture {

    private final List<Slice> slices = new ArrayList<Slice>();
    private final List<Strip> strips = new ArrayList<Strip>();

    private Fixture(String id, Sun.Type type, float[] coordinates, float[] rotations, LXTransform transform, int[][] numPointsPerStrip) {
      transform.push();
      transform.translate(coordinates[0], coordinates[1], coordinates[2]);
      transform.rotateX(rotations[0] * PI / 180);
      transform.rotateY(rotations[1] * PI / 180);
      transform.rotateZ(rotations[2] * PI / 180);

      // create slices...
      if (type != Sun.Type.ONE_THIRD) {
        slices.add(new Slice(id + "_top_front", Slice.Type.FULL, new float[] {-Slice.DIAMETER/2, Slice.DIAMETER/2, 0}, new float[] {0,   0, 0}, transform, numPointsPerStrip[0]));
        slices.add(new Slice(id + "_top_back",  Slice.Type.FULL, new float[] {Slice.DIAMETER/2, Slice.DIAMETER/2, 0}, new float[] {0, 180, 0}, transform, numPointsPerStrip[1]));
      }

      switch (type) {
        case FULL:
          slices.add(new Slice(id + "_bottom_front", Slice.Type.FULL, new float[] {Slice.DIAMETER/2, -Slice.DIAMETER*0.5, 0}, new float[] {0,   0, 180}, transform, numPointsPerStrip[2]));
          slices.add(new Slice(id + "_bottom_back",  Slice.Type.FULL, new float[] {-Slice.DIAMETER/2, -Slice.DIAMETER*0.5, 0}, new float[] {0, 180, 180}, transform, numPointsPerStrip[3]));
          break;

        case TWO_THIRDS:
          slices.add(new Slice(id + "_bottom_front", Slice.Type.BOTTOM_ONE_THIRD, new float[] {Slice.DIAMETER/2, -Slice.DIAMETER/2, 0}, new float[] {0,   0, 180}, transform, numPointsPerStrip[2]));
          slices.add(new Slice(id + "_bottom_back",  Slice.Type.BOTTOM_ONE_THIRD, new float[] {-Slice.DIAMETER/2, -Slice.DIAMETER/2, 0}, new float[] {0, 180, 180}, transform, numPointsPerStrip[3]));

        case ONE_HALF:
          // already done
          break;

        case ONE_THIRD:
          slices.add(new Slice(id + "_top_front", Slice.Type.TWO_THIRDS, new float[] {-Slice.DIAMETER/2, Slice.DIAMETER/2, 0}, new float[] {0,   0, 0}, transform, numPointsPerStrip[0]));
          slices.add(new Slice(id + "_top_back",  Slice.Type.TWO_THIRDS, new float[] {Slice.DIAMETER/2, Slice.DIAMETER/2, 0}, new float[] {0, 180, 0}, transform, numPointsPerStrip[1]));
          break;
      }

      // add pointers to strips
      for (Slice slice : slices) {
        for (Strip strip : slice.strips) {
          strips.add(strip);
          for (LXPoint point : strip.points) {
            points.add(point);
          }
        }
      }

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
          println(numPointsPerStrip[counter]);
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

        points.add(new LXPoint(transform.x(), transform.y(), transform.z()));
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
    if (normal.mag() == 0) {
      // If given the null vector, the normal defaults to up.
      this.normal = new PVector(0, 1, 0);
    } else {
      this.normal = normal;
      this.normal.normalize();
    }
  }

  public LXPointNormal(double x, double y, double z, PVector normal) {
    this((float) x, (float) y, (float) z, normal);
  }
}
