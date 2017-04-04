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
public static class Model extends LXModel {

  public final List<Tower> towers;
  public final List<Cube> cubes;
  public final List<Face> faces;
  public final List<Strip> strips;
  public final Map<String, Cube> cubeTable;
  private final Cube[] _cubes;

  public Model(List<Tower> towers, Cube[] cubeArr) {
    super(new Fixture(cubeArr));
    Fixture fixture = (Fixture) this.fixtures.get(0);

    _cubes = cubeArr;

    // Make unmodifiable accessors to the model data
    List<Tower> towerList = new ArrayList<Tower>();
    List<Cube> cubeList = new ArrayList<Cube>();
    List<Face> faceList = new ArrayList<Face>();
    List<Strip> stripList = new ArrayList<Strip>();
    Map<String, Cube> _cubeTable = new HashMap<String, Cube>();
    
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

    this.towers    = Collections.unmodifiableList(towerList);
    this.cubes     = Collections.unmodifiableList(cubeList);
    this.faces     = Collections.unmodifiableList(faceList);
    this.strips    = Collections.unmodifiableList(stripList);
    this.cubeTable = Collections.unmodifiableMap (_cubeTable);
  }

  private static class Fixture extends LXAbstractFixture {
    private Fixture(Cube[] cubeArr) {
      for (Cube cube : cubeArr) { 
        if (cube != null) { 
          for (LXPoint point : cube.points) { 
            this.points.add(point); 
          } 
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
      transform.translate(0, metrics.vertical.length, 0);
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


EnvelopModel getModel() {
  switch (environment) {
  case SATELLITE: return new Satellite();
  case MIDWAY: return new Midway();
  }
  return null;
}

static abstract class EnvelopModel extends LXModel {
    
  static abstract class Config {
    
    static class Rail {
      public final PVector position;
      public final int numPoints;
      public final float pointSpacing;
      
      Rail(PVector position, int numPoints, float pointSpacing) {
        this.position = position;
        this.numPoints = numPoints;
        this.pointSpacing = pointSpacing;
      }
    }
    
    public abstract PVector[] getColumns();
    public abstract float[] getArcs();
    public abstract Rail[] getRails();
  }
  
  public final List<Column> columns;
  public final List<Arc> arcs;
  public final List<Rail> rails;
  
  protected EnvelopModel(Config config) {
    super(new Fixture(config));
    Fixture f = (Fixture) fixtures.get(0);
    columns = Collections.unmodifiableList(Arrays.asList(f.columns));
    final Arc[] arcs = new Arc[columns.size() * config.getArcs().length];
    final Rail[] rails = new Rail[columns.size() * config.getRails().length];
    int a = 0;
    int r = 0;
    for (Column column : columns) {
      for (Arc arc : column.arcs) {
        arcs[a++] = arc;
      }
      for (Rail rail : column.rails) {
        rails[r++] = rail;
      }
    }
    this.arcs = Collections.unmodifiableList(Arrays.asList(arcs));
    this.rails = Collections.unmodifiableList(Arrays.asList(rails));
  }
  
  private static class Fixture extends LXAbstractFixture {
    
    final Column[] columns;
    
    Fixture(Config config) {
      columns = new Column[config.getColumns().length];
      LXTransform transform = new LXTransform();
      int ci = 0;
      for (PVector pv : config.getColumns()) {
        transform.push();
        transform.translate(pv.x, 0, pv.y);
        float theta = atan2(pv.y, pv.x) - HALF_PI;
        transform.rotateY(theta);
        addPoints(columns[ci] = new Column(config, ci, transform, theta));
        transform.pop();
        ++ci;
      }
    }
  }
}

static class Midway extends EnvelopModel {
  
  final static float WIDTH = 20*FEET + 10.25*INCHES;
  final static float DEPTH = 41*FEET + 6*INCHES;
  
  final static float INNER_OFFSET_X = WIDTH/2. - 1*FEET - 8.75*INCHES;
  final static float OUTER_OFFSET_X = WIDTH/2. - 5*FEET - 1.75*INCHES;
  final static float INNER_OFFSET_Z = -DEPTH/2. + 15*FEET + 10.75*INCHES;
  final static float OUTER_OFFSET_Z = -DEPTH/2. + 7*FEET + 8*INCHES;
  
  final static float SUB_OFFSET_X = 36*INCHES;
  final static float SUB_OFFSET_Z = 20*INCHES;
  
  final static EnvelopModel.Config CONFIG = new EnvelopModel.Config() {
    public PVector[] getColumns() {
      return COLUMN_POSITIONS;
    }
    
    public float[] getArcs() {
      return ARC_POSITIONS;
    }
    
    public EnvelopModel.Config.Rail[] getRails() {
      return RAILS;
    }
  };
  
  final static int NUM_POINTS = 109;
  final static float POINT_SPACING = 1.31233596*INCHES;
  
  final static EnvelopModel.Config.Rail[] RAILS = {
    new EnvelopModel.Config.Rail(new PVector(-1, 0, 0), NUM_POINTS, POINT_SPACING),
    new EnvelopModel.Config.Rail(new PVector(1, 0, 0), NUM_POINTS, POINT_SPACING)
  };
  
  final static float[] ARC_POSITIONS = { 1/3.f, 2/3.f };
  
  final static PVector[] COLUMN_POSITIONS = {
    new PVector(-OUTER_OFFSET_X, -OUTER_OFFSET_Z, 101),
    new PVector(-INNER_OFFSET_X, -INNER_OFFSET_Z, 102),
    new PVector(-INNER_OFFSET_X,  INNER_OFFSET_Z, 103),
    new PVector(-OUTER_OFFSET_X,  OUTER_OFFSET_Z, 104),
    new PVector( OUTER_OFFSET_X,  OUTER_OFFSET_Z, 105),
    new PVector( INNER_OFFSET_X,  INNER_OFFSET_Z, 106),
    new PVector( INNER_OFFSET_X, -INNER_OFFSET_Z, 107),
    new PVector( OUTER_OFFSET_X, -OUTER_OFFSET_Z, 108)
  };
    
  final static PVector[] SUB_POSITIONS = {
    COLUMN_POSITIONS[0].copy().add(-SUB_OFFSET_X, -SUB_OFFSET_Z),
    COLUMN_POSITIONS[3].copy().add(-SUB_OFFSET_X, SUB_OFFSET_Z),
    COLUMN_POSITIONS[4].copy().add(SUB_OFFSET_X, SUB_OFFSET_Z),
    COLUMN_POSITIONS[7].copy().add(SUB_OFFSET_X, -SUB_OFFSET_Z),
  };
  
  Midway() {
    super(CONFIG);
  }
}

static class Satellite extends EnvelopModel {
  
  final static float EDGE_LENGTH = 12*FEET;
  final static float HALF_EDGE_LENGTH = EDGE_LENGTH / 2;
  final static float INCIRCLE_RADIUS = HALF_EDGE_LENGTH + EDGE_LENGTH / sqrt(2);
  
  final static PVector[] PLATFORM_POSITIONS = {
    new PVector(-HALF_EDGE_LENGTH,  INCIRCLE_RADIUS, 101),
    new PVector(-INCIRCLE_RADIUS,  HALF_EDGE_LENGTH, 102),
    new PVector(-INCIRCLE_RADIUS, -HALF_EDGE_LENGTH, 103),
    new PVector(-HALF_EDGE_LENGTH, -INCIRCLE_RADIUS, 104),
    new PVector( HALF_EDGE_LENGTH, -INCIRCLE_RADIUS, 105),
    new PVector( INCIRCLE_RADIUS, -HALF_EDGE_LENGTH, 106),
    new PVector( INCIRCLE_RADIUS,  HALF_EDGE_LENGTH, 107),
    new PVector( HALF_EDGE_LENGTH,  INCIRCLE_RADIUS, 108)
  };
  
  final static PVector[] COLUMN_POSITIONS;
  static {
    float ratio = (INCIRCLE_RADIUS - Column.RADIUS - 6*INCHES) / INCIRCLE_RADIUS;
    COLUMN_POSITIONS = new PVector[PLATFORM_POSITIONS.length];
    for (int i = 0; i < PLATFORM_POSITIONS.length; ++i) {
      COLUMN_POSITIONS[i] = PLATFORM_POSITIONS[i].copy().mult(ratio);
    }
  };
  
  final static float POINT_SPACING = 1.31233596*INCHES;
  
  final static EnvelopModel.Config.Rail[] RAILS = {
    new EnvelopModel.Config.Rail(new PVector(-1, 0, 0), 108, POINT_SPACING),
    new EnvelopModel.Config.Rail(new PVector(0, 0, 1), 100, POINT_SPACING),
    new EnvelopModel.Config.Rail(new PVector(1, 0, 0), 108, POINT_SPACING)
  };
  
  final static float[] ARC_POSITIONS = { };
  
  final static EnvelopModel.Config CONFIG = new EnvelopModel.Config() {
    public PVector[] getColumns() {
      return COLUMN_POSITIONS;
    }
    
    public float[] getArcs() {
      return ARC_POSITIONS;
    }
    
    public EnvelopModel.Config.Rail[] getRails() {
      return RAILS;
    }
  };
  
  Satellite() {
    super(CONFIG);
  }
}


static class Column extends LXModel {
  
  final static float SPEAKER_ANGLE = 22./180.*PI;
  
  final static float HEIGHT = Rail.HEIGHT;
  final static float RADIUS = 20*INCHES;
  
  final int index;
  final float theta;
  
  final List<Arc> arcs;
  final List<Rail> rails;
  
  Column(EnvelopModel.Config config, int index, LXTransform transform, float theta) {
    super(new Fixture(config, transform));
    this.index = index;
    this.theta = theta;
    Fixture f = (Fixture) fixtures.get(0);
    arcs = Collections.unmodifiableList(Arrays.asList(f.arcs));
    rails = Collections.unmodifiableList(Arrays.asList(f.rails));
  }
  
  private static class Fixture extends LXAbstractFixture {
    final Arc[] arcs;
    final Rail[] rails;
    
    Fixture(EnvelopModel.Config config, LXTransform transform) {
      
      // Transform begins on the floor at center of column
      transform.push();
      
      // Rails
      this.rails = new Rail[config.getRails().length];
      for (int i = 0; i < config.getRails().length; ++i) {
        EnvelopModel.Config.Rail rail = config.getRails()[i]; 
        transform.translate(RADIUS * rail.position.x, 0, RADIUS * rail.position.z);
        addPoints(rails[i] = new Rail(rail, transform));
        transform.translate(-RADIUS * rail.position.x, 0, -RADIUS * rail.position.z);
      }
      
      // Arcs
      this.arcs = new Arc[config.getArcs().length];
      for (int i = 0; i < config.getArcs().length; ++i) {
        float y = config.getArcs()[i] * HEIGHT;
        transform.translate(0, y, 0);      
        addPoints(arcs[i] = new Arc(transform));
        transform.translate(0, -y, 0);
      }
      
      transform.pop();
    }
  }
}

static class Rail extends LXModel {
  
  final static int LEFT = 0;
  final static int RIGHT = 1;
  
  final static float HEIGHT = 12*FEET;
  
  Rail(EnvelopModel.Config.Rail rail, LXTransform transform) {
    super(new Fixture(rail, transform));
  }
  
  private static class Fixture extends LXAbstractFixture {
    Fixture(EnvelopModel.Config.Rail rail, LXTransform transform) {
      transform.push();
      transform.translate(0, rail.pointSpacing / 2., 0);
      for (int i = 0; i < rail.numPoints; ++i) {
        addPoint(new LXPoint(transform));
        transform.translate(0, rail.pointSpacing, 0);
      }
      transform.pop();
    }
  }
}

static class Arc extends LXModel {
  
  final static float RADIUS = Column.RADIUS;
  
  final static int BOTTOM = 0;
  final static int TOP = 1;
  
  final static int NUM_POINTS = 34;
  final static float POINT_ANGLE = PI / NUM_POINTS;
  
  Arc(LXTransform transform) {
    super(new Fixture(transform));
  }
  
  private static class Fixture extends LXAbstractFixture {
    Fixture(LXTransform transform) {
      transform.push();
      transform.rotateY(POINT_ANGLE / 2.);
      for (int i = 0; i < NUM_POINTS; ++i) {
        transform.translate(-RADIUS, 0, 0);
        addPoint(new LXPoint(transform));
        transform.translate(RADIUS, 0, 0);
        transform.rotateY(POINT_ANGLE);
      }
      transform.pop();
    }
  }
}