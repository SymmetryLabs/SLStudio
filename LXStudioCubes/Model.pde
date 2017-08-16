import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.function.Function;
import java.util.function.IntFunction;

final static float INCHES = 1;
final static float FEET = 12*INCHES;
final static float INCHES_PER_METER = 39.3701;
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
  public final List<Tower> towers;
  public final List<Cube> cubes;
  public final List<Face> faces;
  public final List<Strip> strips;
  public final List<Bar> bars;
  public final Map<String, Cube> cubeTable;
  private final Cube[] _cubes;

  public final List<Ring> rings;

  public final Skylight skylight;
  public final WallBars wallBars;
  public final LevelRings levelRings;
  public final RotatedRings rotatedRings;
  public final UpstairsRings upstairsRings;

  public SLModel(List<Tower> towers, Cube[] cubeArr, List<Strip> strips, List<Bar> bars, Skylight skylight, WallBars wallBars, LevelRings levelRings, RotatedRings rotatedRings, UpstairsRings upstairsRings, List<Ring> rings) {
    super(new Fixture(cubeArr, strips, rings));
    Fixture fixture = (Fixture) this.fixtures.get(0);

    _cubes = cubeArr;

    // Make unmodifiable accessors to the model data
    List<Tower> towerList = new ArrayList<Tower>();
    List<Cube> cubeList = new ArrayList<Cube>();
    List<Face> faceList = new ArrayList<Face>();
    List<Strip> stripList = new ArrayList<Strip>();
    List<Bar> barList = new ArrayList<Bar>();
    Map<String, Cube> _cubeTable = new HashMap<String, Cube>();

    List<Ring> ringList = new ArrayList<Ring>();
    
    for (Tower tower : towers) {
      towerList.add(tower);
      for (Cube cube : tower.cubes) {
        if (cube != null) {
          _cubeTable.put(cube.id, cube);
          cubeList.add(cube);
          for (Face face : cube.faces) {
            faceList.add(face);
            for (Strip strip : face.strips) {
              stripList.add(strip);
            }
          }
        }
      }
    }

    for (Strip strip : strips) {
      stripList.add(strip);
    }

    for (Ring ring : levelRings.rings) {
      ringList.add(ring);
    }

    this.towers    = Collections.unmodifiableList(towerList);
    this.cubes     = Collections.unmodifiableList(cubeList);
    this.faces     = Collections.unmodifiableList(faceList);
    this.strips    = Collections.unmodifiableList(stripList);
    this.bars      = Collections.unmodifiableList(barList);
    this.cubeTable = Collections.unmodifiableMap (_cubeTable);

    this.rings     = Collections.unmodifiableList(ringList);

    this.skylight = skylight;
    this.wallBars = wallBars;
    this.levelRings = levelRings;
    this.rotatedRings = rotatedRings;
    this.upstairsRings = upstairsRings;
  }

  private static class Fixture extends LXAbstractFixture {
    private Fixture(Cube[] cubeArr, List<Strip> strips, List<Ring> rings) {
      for (Cube cube : cubeArr) { 
        if (cube != null) { 
          for (LXPoint point : cube.points) { 
            this.points.add(point); 
          } 
        } 
      } 
      for (Strip strip : strips) {
        for (LXPoint point : strip.points) {
          this.points.add(point);
        }
      }
      for (Ring ring : rings) {
        for (LXPoint point : ring.points) {
          this.points.add(point);
        }
      }
    }
  }

  /**
   * TODO(mcslee): figure out better solution
   * 
   * @param index
   * @return
   */
  public Cube getCubeByRawIndex(int index) {
    return _cubes[index];
  }
  
  public Cube getCubeById(String id) {
    return this.cubeTable.get(id);
  }
}

/**
 * Model of a set of cubes stacked in a tower
 */
public static class Tower extends LXModel {
  
  /**
   * Tower id
   */
  public final String id;
  
  /**
   * Immutable list of cubes
   */
  public final List<Cube> cubes;

  /**
   * Immutable list of faces
   */
  public final List<Face> faces;

  /**
     * Immutable list of strips
     */
  public final List<Strip> strips;

  /**
   * Constructs a tower model from these cubes
   * 
   * @param cubes Array of cubes
   */
  public Tower(String id, List<Cube> cubes) {
    super(cubes.toArray(new Cube[] {}));
    this.id   = id;

    List<Cube>  cubeList  = new ArrayList<Cube>();
    List<Face>  faceList  = new ArrayList<Face>();
    List<Strip> stripList = new ArrayList<Strip>();

    for (Cube cube : cubes) {
      cubeList.add(cube);
      for (Face face : cube.faces) {
        faceList.add(face);
        for (Strip strip : face.strips) {
          stripList.add(strip);
        }
      }
    }
    this.cubes = Collections.unmodifiableList(cubeList);
    this.faces = Collections.unmodifiableList(faceList);
    this.strips = Collections.unmodifiableList(stripList);
  }
}

/**
 * Model of a single cube, which has an orientation and position on the
 * car. The position is specified in x,y,z coordinates with rotation. The
 * x axis is left->right, y is bottom->top, and z is front->back.
 * 
 * A cube's x,y,z position is specified as the left, bottom, front corner.
 * 
 * Dimensions are all specified in real-world inches.
 */
public static class Cube extends LXModel {

  public enum Type {

    //            Edge     |  LEDs   |  LEDs
    //            Length   |  Per    |  Per
    //            Inches   |  Meter  |  Edge
    SMALL         (12,        72,       15),
    MEDIUM        (18,        60,       23),
    LARGE         (24,        30,       15),
    LARGE_DOUBLE  (24,        60,       30);
    

    public final float EDGE_WIDTH;
    public final float EDGE_HEIGHT;

    public final int POINTS_PER_STRIP;
    public final int POINTS_PER_CUBE;
    public final int POINTS_PER_FACE;

    public final int LEDS_PER_METER;

    public final Face.Metrics FACE_METRICS;

    private Type(float edgeLength, int ledsPerMeter, int ledsPerStrip) {
      this.EDGE_WIDTH = this.EDGE_HEIGHT = edgeLength;

      this.POINTS_PER_STRIP = ledsPerStrip;
      this.POINTS_PER_CUBE = STRIPS_PER_CUBE*POINTS_PER_STRIP;
      this.POINTS_PER_FACE = Face.STRIPS_PER_FACE*POINTS_PER_STRIP;

      this.LEDS_PER_METER = ledsPerMeter;

      this.FACE_METRICS = new Face.Metrics(
        new Strip.Metrics(this.EDGE_WIDTH, POINTS_PER_STRIP, ledsPerMeter), 
        new Strip.Metrics(this.EDGE_HEIGHT, POINTS_PER_STRIP, ledsPerMeter)
      );
    }

  };

  public static final Type CUBE_TYPE_WITH_MOST_PIXELS = Type.LARGE_DOUBLE;

  public final static int FACES_PER_CUBE = 4; 

  public final static int STRIPS_PER_CUBE = FACES_PER_CUBE*Face.STRIPS_PER_FACE;

  public final static float CHANNEL_WIDTH = 1.5f;

  public final Type type;

  public final String id;

  /**
   * Immutable list of all cube faces
   */
  public final List<Face> faces;

  /**
   * Immutable list of all strips
   */
  public final List<Strip> strips;

  /**
   * Front left corner x coordinate 
   */
  public final float x;

  /**
   * Front left corner y coordinate 
   */
  public final float y;

  /**
   * Front left corner z coordinate 
   */
  public final float z;

  /**
   * Rotation about the x-axis 
   */
  public final float rx;

  /**
   * Rotation about the y-axis 
   */
  public final float ry;

  /**
   * Rotation about the z-axis 
   */
  public final float rz;

  public Cube(String id, float x, float y, float z, float rx, float ry, float rz, LXTransform t, Type type) {
    super(new Fixture(x, y, z, rx, ry, rz, t, type));
    Fixture fixture = (Fixture) this.fixtures.get(0);
    this.type     = type;
    this.id       = id;

    while (rx < 0) rx += 360;
    while (ry < 0) ry += 360;
    while (rz < 0) rz += 360;
    rx = rx % 360;
    ry = ry % 360;
    rz = rz % 360;

    this.x = x; 
    this.y = y;
    this.z = z;
    this.rx = rx;
    this.ry = ry;
    this.rz = rz;

    this.faces = Collections.unmodifiableList(fixture.faces);
    this.strips = Collections.unmodifiableList(fixture.strips);
  }

  private static class Fixture extends LXAbstractFixture {

    private final List<Face> faces = new ArrayList<Face>();
    private final List<Strip> strips = new ArrayList<Strip>();

    private Fixture(float x, float y, float z, float rx, float ry, float rz, LXTransform t, Cube.Type type) {
      // LXTransform t = new LXTransform();
      t.push();
      t.translate(x, y, z);
      t.translate(type.EDGE_WIDTH/2, type.EDGE_HEIGHT/2, type.EDGE_WIDTH/2);
      t.rotateX(rx * PI / 180.);
      t.rotateY(ry * PI / 180.);
      t.rotateZ(rz * PI / 180.);
      t.translate(-type.EDGE_WIDTH/2, -type.EDGE_HEIGHT/2, -type.EDGE_WIDTH/2);

      for (int i = 0; i < FACES_PER_CUBE; i++) {
        Face face = new Face(type.FACE_METRICS, (ry + 90*i) % 360, t);
        this.faces.add(face);
        for (Strip s : face.strips) {
          this.strips.add(s);
        }
        for (LXPoint p : face.points) {
          this.points.add(p);
        }
        t.translate(type.EDGE_WIDTH, 0, 0);
        t.rotateY(HALF_PI);
      }
      t.pop();
    }
  }
}

/**
 * A face is a component of a cube. It is comprised of four strips forming
 * the lights on this side of a cube. A whole cube is formed by four faces.
 */
public static class Face extends LXModel {

  public final static int STRIPS_PER_FACE = 3;

  public static class Metrics {
    final Strip.Metrics horizontal;
    final Strip.Metrics vertical;

    public Metrics(Strip.Metrics horizontal, Strip.Metrics vertical) {
      this.horizontal = horizontal;
      this.vertical = vertical;
    }
  }

  /**
   * Immutable list of strips
   */
  public final List<Strip> strips;

  /**
   * Rotation of the face about the y-axis
   */
  public final float ry;

  Face(Metrics metrics, float ry, LXTransform transform) {
    super(new Fixture(metrics, ry, transform));
    Fixture fixture = (Fixture) this.fixtures.get(0);
    this.ry = ry;
    this.strips = Collections.unmodifiableList(fixture.strips);
  }

  private static class Fixture extends LXAbstractFixture {

    private final List<Strip> strips = new ArrayList<Strip>();

    private Fixture(Metrics metrics, float ry, LXTransform transform) {
      transform.push();
      for (int i = 0; i < STRIPS_PER_FACE; i++) {
        boolean isHorizontal = (i % 2 == 0);
        Strip.Metrics stripMetrics = isHorizontal ? metrics.horizontal : metrics.vertical;
        Strip strip = new Strip(stripMetrics, ry, transform, isHorizontal);
        this.strips.add(strip);
        transform.translate(isHorizontal ? metrics.horizontal.length : metrics.vertical.length, 0, 0);
        transform.rotateZ(HALF_PI);
        for (LXPoint p : strip.points) {
          this.points.add(p);
        }
      }
      transform.pop();
    }
  }
}

public static class Skylight extends LXModel {
  public final List<Bar> bars;
  public final List<Strip> strips;

  public Skylight(List<Bar> bars) {
    super(new Fixture(bars));
    this.bars = bars;
    this.strips = new ArrayList<Strip>();
    for (Bar bar : bars) {
      for (Strip s : bar.strips) {
        this.strips.add(s);
      }
    }
  }

  private static class Fixture extends LXAbstractFixture {
    private Fixture(List<Bar> bars) {
      for (Bar bar : bars) {
        for (LXPoint p : bar.points) {
          this.points.add(p);
        }
      }
    }
  }
}

public static class WallBars extends LXModel {
  public final List<Bar> bars;
  public final List<Strip> strips;

  public WallBars(List<Bar> bars) {
    super(new Fixture(bars));
    this.bars = bars;
    this.strips = new ArrayList<Strip>();
    for (Bar bar : bars) {
      for (Strip s : bar.strips) {
        this.strips.add(s);
      }
    }
  }

  private static class Fixture extends LXAbstractFixture {
    private Fixture(List<Bar> bars) {
      for (Bar bar : bars) {
        for (LXPoint p : bar.points) {
          this.points.add(p);
        }
      }
    }
  }
}

public static class LevelRings extends LXModel {

  public final List<Ring> rings;

  public LevelRings(RingChandelierConfig config, LXTransform t) {
    super(new Fixture(config, t));
    Fixture fixture = (Fixture)this.fixtures.get(0);
    this.rings = fixture.rings;
  }

  private static class Fixture extends LXAbstractFixture {

    public final List<Ring> rings = new ArrayList<Ring>();

    private Fixture(RingChandelierConfig config, LXTransform t) {
      t.push();
      t.translate(config.coordinates[0], config.coordinates[1], config.coordinates[2]);
      t.rotateX(config.rotations[0] * PI / 180.);
      t.rotateY(config.rotations[1] * PI / 180.);
      t.rotateZ(config.rotations[2] * PI / 180.);

      for (RingConfig rc : config.rings) {
        this.rings.add(new Ring(rc.id, rc.coordinates, rc.rotations, rc.numPoints, rc.radius, t));
      }
      t.pop();
    }
  }
}

public static class RotatedRings extends LXModel {

  public final List<Ring> rings;

  public RotatedRings(RingChandelierConfig config, LXTransform t) {
    super(new Fixture(config, t));
    Fixture fixture = (Fixture)this.fixtures.get(0);
    this.rings = fixture.rings;
  }

  private static class Fixture extends LXAbstractFixture {

    public final List<Ring> rings = new ArrayList<Ring>();

    private Fixture(RingChandelierConfig config, LXTransform t) {
      t.push();
      t.translate(config.coordinates[0], config.coordinates[1], config.coordinates[2]);
      t.rotateX(config.rotations[0] * PI / 180.);
      t.rotateY(config.rotations[1] * PI / 180.);
      t.rotateZ(config.rotations[2] * PI / 180.);

      for (RingConfig rc : config.rings) {
        this.rings.add(new Ring(rc.id, rc.coordinates, rc.rotations, rc.numPoints, rc.radius, t));
      }
      t.pop();
    }
  }
}

public static class UpstairsRings extends LXModel {

  public final List<Ring> rings;

  public UpstairsRings(RingChandelierConfig config, LXTransform t) {
    super(new Fixture(config, t));
    Fixture fixture = (Fixture)this.fixtures.get(0);
    this.rings = fixture.rings;
  }

  private static class Fixture extends LXAbstractFixture {

    public final List<Ring> rings = new ArrayList<Ring>();

    private Fixture(RingChandelierConfig config, LXTransform t) {
      t.push();
      t.translate(config.coordinates[0], config.coordinates[1], config.coordinates[2]);
      t.rotateX(config.rotations[0] * PI / 180.);
      t.rotateY(config.rotations[1] * PI / 180.);
      t.rotateZ(config.rotations[2] * PI / 180.);

      for (RingConfig rc : config.rings) {
        this.rings.add(new Ring(rc.id, rc.coordinates, rc.rotations, rc.numPoints, rc.radius, t));
      }
      t.pop();
    }
  }
}

// public static class LevelRings extends LXModel {

//   public final List<Ring> rings = new ArrayList<Ring>();

//   public final Ring topRing;
//   public final Ring middleRing;
//   public final Ring bottomRing;

//   public LevelRings(String[] ids, float[] coordinates, float[] rotations, LXTransform t) {
//     super(new Fixture(ids, coordinates, rotations, t));
//     Fixture fixture = (Fixture)this.fixtures.get(0);

//     this.rings.add(this.topRing = fixture.topRing);
//     this.rings.add(this.middleRing = fixture.middleRing);
//     this.rings.add(this.bottomRing = fixture.bottomRing);
//   }

//   private static class Fixture extends LXAbstractFixture {
//     public final Ring topRing;
//     public final Ring middleRing;
//     public final Ring bottomRing;

//     private Fixture(String[] ids, float[] coordinates, float[] rotations, LXTransform t) {
//       t.push();
//       t.translate(coordinates[0], coordinates[1], coordinates[2]);
//       t.rotateX(rotations[0] * PI / 180.);
//       t.rotateY(rotations[1] * PI / 180.);
//       t.rotateZ(rotations[2] * PI / 180.);

//       this.bottomRing = new Ring(ids[0], new float[] {0,  0, 0}, new float[] {90, 0, 0}, 100, 6, t);
//       this.middleRing = new Ring(ids[1], new float[] {0, 10, 0}, new float[] {90, 0, 0}, 150, 12, t);
//       this.topRing    = new Ring(ids[2], new float[] {0, 20, 0}, new float[] {90, 0, 0}, 200, 18, t);
      
//       t.pop();
//     }
//   }
// }

public static class Ring extends LXModel {
  public final String id;
  public final float x;
  public final float y;
  public final float z;
  public final float radius;

  public Ring(String id, float[] cordinates, float[] rotations, int numPoints, float radius, LXTransform t) {
    super(new Fixture(cordinates, rotations, numPoints, radius, t));
    Fixture fixture = (Fixture)this.fixtures.get(0);
    this.id = id;
    this.x = fixture.x;
    this.y = fixture.y;
    this.z = fixture.z;
    this.radius = radius;
  }

  private static class Fixture extends LXAbstractFixture {
    public final float x;
    public final float y;
    public final float z;

    private Fixture(float[] coordinates, float[] rotations, int numPoints, float radius, LXTransform t) {
      t.push();
      t.translate(coordinates[0], coordinates[1], coordinates[2]);
      t.rotateX(rotations[0] * PI / 180.);
      t.rotateY(rotations[1] * PI / 180.);
      t.rotateZ(rotations[2] * PI / 180.);
      this.x = t.x();
      this.y = t.y();
      this.z = t.z();

      for (int i = 0; i < numPoints; i++) {
        t.push();
        float theta = 360*((float)i / (float)numPoints);
        float x = (float)(radius*Math.cos(Math.toRadians(theta)));
        float y = (float)(radius*Math.sin(Math.toRadians(theta)));
        t.translate(x, y, 0);
        this.points.add(new LXPoint(t.x(), t.y(), t.z()));
        t.pop();
      }
      t.pop();
    }
  }
}

public static class Bar extends LXModel {

  public static class Metrics {

    public static enum StripOrientation {
      CONSISTENT, DOWN_BACK
    }

    public final int numStrips;
    public final StripOrientation stripOrientation;
    public final Strip.Metrics strip;

    public Metrics(int numStrips, int numPointsPerStrip, float spacing, Metrics.StripOrientation stripOrientation) {
      this.strip = new Strip.Metrics(numPointsPerStrip, spacing);
      this.numStrips = numStrips;
      this.stripOrientation = stripOrientation;
    }
  }

  public String id;
  public Metrics metrics;
  public List<Strip> strips;

  public float x;
  public float y;
  public float z;
  public float xRot;
  public float yRot;
  public float zRot;

  public Bar(BarConfig barConfig, LXTransform t) {
    this(barConfig.id, barConfig.metrics, barConfig.x, barConfig.y, barConfig.z, barConfig.xRot, barConfig.yRot, barConfig.zRot, t);
  }

  public Bar(String id, Metrics metrics, float x, float y, float z, float xRot, float yRot, float zRot, LXTransform t) {
    super(new Fixture(metrics, x, y, z, xRot, yRot, zRot, t));
    Fixture fixture = (Fixture)this.fixtures.get(0);

    this.id = id;
    this.metrics = metrics;
    this.x = fixture.x;
    this.y = fixture.y;
    this.z = fixture.z;
    this.xRot = xRot;
    this.yRot = yRot;
    this.zRot = zRot;

    this.strips = fixture.strips;
  }

  private static class Fixture extends LXAbstractFixture {

    public float x;
    public float y;
    public float z;

    private final List<Strip> strips = new ArrayList<Strip>();

    private Fixture(Metrics metrics, float x, float y, float z, float xRot, float yRot, float zRot, LXTransform t) {
      t.push();
      t.translate(x, y, z);
      t.rotateY(xRot * PI / 180.);
      t.rotateX(yRot * PI / 180.);
      t.rotateZ(zRot * PI / 180.);

      this.x = t.x();
      this.y = t.y();
      this.z = t.z();

      this.strips.add(new Strip(metrics.strip, yRot, t, zRot != 0));

      t.push();
      t.translate(metrics.strip.length, 0.5, 0);
      t.rotateZ(180 * PI / 180.);
      this.strips.add(new Strip(metrics.strip, yRot, t, zRot != 0));
      t.pop();

      for (Strip s : strips) {
        for (LXPoint p : s.points) {
          this.points.add(p);
        }
      }
      t.pop();
    }
  }
}

/**
 * A strip is a linear run of points along a single edge of one cube.
 */
public static class Strip extends LXModel {

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

  Strip(Metrics metrics, float ry, List<LXPoint> points, boolean isHorizontal) {
    super(points);
    this.isHorizontal = isHorizontal;
    this.metrics = metrics;   
    this.ry = ry;
  }

  Strip(Metrics metrics, float ry, LXTransform transform, boolean isHorizontal) {
    super(new Fixture(metrics, ry, transform));
    this.metrics = metrics;
    this.isHorizontal = isHorizontal;
    this.ry = ry;
  }

  private static class Fixture extends LXAbstractFixture {
    private Fixture(Metrics metrics, float ry, LXTransform transform) {
      float offset = (metrics.length - (metrics.numPoints - 1) * metrics.POINT_SPACING) / 2.f;
      transform.push();
      transform.translate(offset, -Cube.CHANNEL_WIDTH/2.f, 0);
      for (int i = 0; i < metrics.numPoints; i++) {
        LXPoint point = new LXPoint(transform.x(), transform.y(), transform.z());
        this.points.add(point);
        transform.translate(metrics.POINT_SPACING, 0, 0);
      }
      transform.pop();
    }
  }
}