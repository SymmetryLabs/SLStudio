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
  public final List<Tower> towers;
  public final List<Cube> cubes;
  public final List<Face> faces;
  public final Map<String, Cube> cubeTable;
  private final Cube[] _cubes;
  public final List<LXModel> objModels;

  // Suns
  public final List<Sun> suns;
  public final Map<String, Sun> sunTable;

  // Slices
  public final List<Slice> slices;
  public final Map<String, Slice> sliceTable;

  // Strips
  public final List<Strip> strips;
  public final Map<String, Strip> stripTable;

  public SLModel(List<LXModel> objModels, List<Tower> towers, Cube[] cubeArr, List<Sun> suns) {
    super(new Fixture(objModels, cubeArr, suns));
    Fixture fixture = (Fixture) this.fixtures.get(0);

    _cubes = cubeArr;

    // Make unmodifiable accessors to the model data
    List<Tower> towerList = new ArrayList<Tower>();
    List<Cube> cubeList = new ArrayList<Cube>();
    List<Face> faceList = new ArrayList<Face>();
    Map<String, Cube> _cubeTable = new HashMap<String, Cube>();

    // Suns
    List<Sun> sunList = new ArrayList<Sun>();
    Map<String, Sun> _sunTable = new HashMap<String, Sun>();

    // Slices
    List<Slice> sliceList = new ArrayList<Slice>();
    Map<String, Slice> _sliceTable = new HashMap<String, Slice>();

    // Strips
    List<Strip> stripList = new ArrayList<Strip>();
    Map<String, Strip> _stripTable = new HashMap<String, Strip>();
    
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

    for (Sun sun : suns) {
      sunList.add(sun);
      _sunTable.put(sun.id, sun);

      for (Slice slice : sun.slices) {
        sliceList.add(slice);
        _sliceTable.put(slice.id, slice);

        for (Strip strip : slice.strips) {
          stripList.add(strip);
          _stripTable.put(strip.id, strip);
        }
      }
    }

    this.towers     = Collections.unmodifiableList(towerList);
    this.cubes      = Collections.unmodifiableList(cubeList);
    this.faces      = Collections.unmodifiableList(faceList);
    this.cubeTable  = Collections.unmodifiableMap (_cubeTable);
    this.objModels  = objModels;
    
    // Suns
    this.suns       = Collections.unmodifiableList(sunList);
    this.sunTable   = Collections.unmodifiableMap (_sunTable);

    // Slices
    this.slices     = Collections.unmodifiableList(sliceList);
    this.sliceTable = Collections.unmodifiableMap (_sliceTable);

    // Strips
    this.strips     = Collections.unmodifiableList(stripList);
    this.stripTable = Collections.unmodifiableMap (_stripTable);
  }

  private static class Fixture extends LXAbstractFixture {
    private Fixture(List<LXModel> objModels, Cube[] cubeArr, List<Sun> suns) {
      println("Number of obj models: " + objModels.size());
      for (LXModel model : objModels) {
        for (LXPoint point : model.points) {
          this.points.add(point);
        }
      }
      for (Cube cube : cubeArr) { 
        if (cube != null) { 
          for (LXPoint point : cube.points) { 
            this.points.add(point); 
          } 
        } 
      } 
      for (Sun sun : suns) {
        for (LXPoint point : sun.points) {
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

  public Sun getSunById(String id) {
    return this.sunTable.get(id);
  }

  public Slice getSliceById(String id) {
    return this.sliceTable.get(id);
  }

  public Strip getStripById(String id) {
    return this.stripTable.get(id);
  }
}

public static class Sun extends LXModel {

  public enum Type {
    FULL, TWO_THIRDS, HALF, ONE_THIRD
  }

  public final String id;
  public final Type type;
  public final List<Slice> slices;
  public final List<Strip> strips;

  public Sun(String id, Type type, float[] coordinates, float[] rotations, LXTransform transform) {
    super(new Fixture(id, type, coordinates, rotations, transform));
    Fixture fixture = (Fixture)this.fixtures.get(0);

    this.id = id;
    this.type = type;
    this.slices = Collections.unmodifiableList(fixture.slices);
    this.strips = Collections.unmodifiableList(fixture.strips);
  }

  private static class Fixture extends LXAbstractFixture {

    private final List<Slice> slices = new ArrayList<Slice>();
    private final List<Strip> strips = new ArrayList<Strip>();

    private Fixture(String id, Sun.Type type, float[] coordinates, float[] rotations, LXTransform transform) {
      transform.push();
      transform.translate(coordinates[0], coordinates[1], coordinates[2]);
      transform.rotateX(rotations[0] * PI / 180);
      transform.rotateY(rotations[1] * PI / 180);
      transform.rotateZ(rotations[2] * PI / 180);

      // create slices...
      this.slices.add(new Slice(id + "_slice1", Slice.Type.FULL, new float[] {0, 0, 0}, new float[] {0, 0, 0}, transform));

      for (Slice slice : slices) {
        for (Strip strip : slice.strips) {
          this.strips.add(strip);
          for (LXPoint point : slice.points) {
            this.points.add(point);
          }
        }
      }

      transform.pop();
    }
  }
}

public static class Slice extends LXModel {

  public enum Type {
    FULL
  };

  private static final int[] NUM_POINTS_PER_STRIP = { // top to bottom
     9,  25,  35,  43,  48,  55,  59,  65,  69,  73,  77,  81,  85,  89,  91,  95,  97, 101,
   103, 107, 109, 111, 113, 115, 119, 121, 123, 125, 127, 129, 129, 131, 133, 135, 137, 137,
   139, 141, 141, 143, 145, 145, 147, 147, 149, 149, 151, 151, 153, 153, 153, 155, 155, 155,
   157, 157, 157, 157, 159, 159, 159, 159, 159, 161, 161, 161, 161, 161, 161, 161, 161
  };

  private static final float STRIP_SPACING = 1; // not final

  public final String id;
  public final Type type;
  public final List<Strip> strips;

  public Slice(String id, Type type, float[] coordinates, float[] rotations, LXTransform transform) {
    super(new Fixture(id, type, coordinates, rotations, transform));
    Fixture fixture = (Fixture)this.fixtures.get(0);

    this.id = id;
    this.type = type;
    this.strips = Collections.unmodifiableList(fixture.strips);
  }

  private static class Fixture extends LXAbstractFixture {

    private List<Strip> strips = new ArrayList<Strip>();

    private Fixture(String id, Slice.Type type, float[] coordinates, float[] rotations, LXTransform transform) {
      transform.push();
      transform.translate(coordinates[0], coordinates[1], coordinates[2]);
      transform.rotateX(rotations[0] * PI / 180);
      transform.rotateY(rotations[1] * PI / 180);
      transform.rotateZ(rotations[2] * PI / 180);

      // create curved strips...
      for (int i = 0; i < NUM_POINTS_PER_STRIP.length; i++) {
        CurvedStrip.CurvedMetrics metrics = new CurvedStrip.CurvedMetrics(20., 15., NUM_POINTS_PER_STRIP[i]);
        this.strips.add(new CurvedStrip(id + "_strip" + i, metrics, new float[] {0, -i*STRIP_SPACING, 0}, new float[] {0, 0, 0}, transform));
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

  public static class CurvedMetrics {
    public final Strip.Metrics metrics;
    public final float pitch;
    public final int numPoints;
    public final float arcLength;
    public final float arcHeight;

    public CurvedMetrics(float arcLength, float arcHeight, int numPoints) {
      this.metrics = new Strip.Metrics(numPoints, LEDS_PER_METER / INCHES_PER_METER);
      this.pitch = metrics.POINT_SPACING;
      this.numPoints = metrics.numPoints;
      this.arcLength = arcLength;
      this.arcHeight = arcHeight;
    }
  }

  public CurvedStrip(String id, CurvedMetrics metrics, float[] coordinates, float[] rotations, LXTransform transform) {
    super(id, metrics.metrics, new Fixture(id, metrics, coordinates, rotations, transform).getPoints());
  }

  private static class Fixture extends LXAbstractFixture {
    private Fixture(String id, CurvedMetrics metrics, float[] coordinates, float[] rotations, LXTransform transform) {
      transform.push();
      transform.translate(coordinates[0], coordinates[1], coordinates[2]);
      transform.rotateX(rotations[0] * PI / 180);
      transform.rotateY(rotations[1] * PI / 180);
      transform.rotateZ(rotations[2] * PI / 180);

      List<LXPoint> points = new ArrayList<LXPoint>();

      // create points for strip...
      for (int i = 0; i < metrics.numPoints; i++) {
        this.points.add(new LXPoint(transform.x(), transform.y(), transform.z()));
        transform.translate(metrics.pitch, 0, 0);
      }

      transform.pop();
    }
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

/**
 * A strip is a linear run of points along a single edge of one cube.
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

  Strip(Metrics metrics, float ry, List<LXPoint> points, boolean isHorizontal) {
    super(points);
    this.id = "";
    this.isHorizontal = isHorizontal;
    this.metrics = metrics;   
    this.ry = ry;
  }

  Strip(Metrics metrics, float ry, LXTransform transform, boolean isHorizontal) {
    super(new Fixture(metrics, ry, transform));
    this.id = "";
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