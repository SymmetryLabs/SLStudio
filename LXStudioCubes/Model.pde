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

import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.function.Function;
import java.util.function.IntFunction;

final static float INCHES = 1;
final static float FEET = 12*INCHES;

/**
 * Top-level model of the entire sculpture. This contains a list of
 * every cube on the sculpture, which forms a hierarchy of faces, strips
 * and points.
 */
public static class SLModel extends LXModel {

  public final List<Bar> bars;
  private final Bar[] _bars;
  public final Map<String, Bar> barTable;
  public final List<Strip> strips;
  public final List<Strip> innerStrips;
  public final List<Strip> outerStrips; 

  public SLModel(List<Bar> bars, Bar[] barArr) {
    super(new Fixture(barArr));

    _bars = barArr;

    // Make unmodifiable accessors to the model data
    List<Bar> barList  = new ArrayList<Bar>();
    Map<String, Bar> _barTable = new HashMap<String, Bar>();

    List<Strip> stripList = new ArrayList<Strip>();
    List<Strip> innerStripList = new ArrayList<Strip>();
    List<Strip> outerStripList = new ArrayList<Strip>();

    for (Bar bar : bars) {
      barList.add(bar);
      _barTable.put(bar.id, bar);

      stripList.addAll(bar.strips);
      innerStripList.add(bar.innerStrip);
      outerStripList.add(bar.outerStrip);
    }

    this.bars = Collections.unmodifiableList(barList);
    this.barTable = Collections.unmodifiableMap (_barTable);

    this.strips = Collections.unmodifiableList(stripList);
    this.innerStrips = Collections.unmodifiableList(innerStripList);
    this.outerStrips = Collections.unmodifiableList(outerStripList);
  }

  private static class Fixture extends LXAbstractFixture {
    private Fixture(Bar[] barArr) {
      for (Bar bar : barArr) { 
        if (bar != null) { 
          for (LXPoint point : bar.points) { 
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

  public Bar getBarByRawIndex(int index) {
    return _bars[index];
  }

  public Bar getBarById(String id) {
    return this.barTable.get(id);
  }
}

// public static class Body extends Tower {
//   public Body(List<Layer> layers) {
//     super("", Arrays.asList(layers.stream().flatMap(new Function<Layer, Stream<Cube>>() {
//       Stream<Cube> apply(Layer l) { return l.cubes.stream(); }
//     }).toArray(new IntFunction<Cube[]>() {
//       Cube[] apply(int value) { return new Cube[value]; }
//     })));
//   }
// }

/**
 * Layer
 */
// public static class Layer extends Tower {
//   public Layer(List<Cube> cubes) {
//     super("", cubes);
//   }
// }

/**
 * Leg
 */
// public static class Leg extends Tower {
//   public Leg(List<Cube> cubes) {
//     super("", cubes);
//   }
// }

/**
 * Model of a set of cubes stacked in a tower
 */
// public static class Tower extends LXModel {

//   /**
//    * Tower id
//    */
//   public final String id;

//   /**
//    * Immutable list of cubes
//    */
//   public final List<Cube> cubes;

//   /**
//    * Immutable list of faces
//    */
//   public final List<Face> faces;

//   /**
//      * Immutable list of strips
//      */
//   public final List<Strip> strips;

//   /**
//    * Constructs a tower model from these cubes
//    * 
//    * @param cubes Array of cubes
//    */
//   public Tower(String id, List<Cube> cubes) {
//     super(cubes.toArray(new Cube[] {}));
//     this.id   = id;

//     List<Cube>  cubeList  = new ArrayList<Cube>();
//     List<Face>  faceList  = new ArrayList<Face>();
//     List<Strip> stripList = new ArrayList<Strip>();

//     for (Cube cube : cubes) {
//       cubeList.add(cube);
//       for (Face face : cube.faces) {
//         faceList.add(face);
//         for (Strip strip : face.strips) {
//           stripList.add(strip);
//         }
//       }
//     }
//     this.cubes = Collections.unmodifiableList(cubeList);
//     this.faces = Collections.unmodifiableList(faceList);
//     this.strips = Collections.unmodifiableList(stripList);
//   }
// }

/**
 * Model of a single cube, which has an orientation and position on the
 * car. The position is specified in x,y,z coordinates with rotation. The
 * x axis is left->right, y is bottom->top, and z is front->back.
 * 
 * A cube's x,y,z position is specified as the left, bottom, front corner.
 * 
 * Dimensions are all specified in real-world inches.
 */
// public static class Cube extends LXModel {

//   public enum Type {

//     //        Edge     |  LEDs   |  LEDs
//     //        Length   |  Per    |  Per
//     //        Inches   |  Meter  |  Edge
//     SMALL     (12,        72,       15),
//     MEDIUM    (18,        60,       23),
//    LARGE         (24,        30,       15),
//    LARGE_DOUBLE  (24,        60,       30);


//     public final float EDGE_WIDTH;
//     public final float EDGE_HEIGHT;

//     public final int POINTS_PER_STRIP;
//     public final int POINTS_PER_CUBE;
//     public final int POINTS_PER_FACE;

//     public final int LEDS_PER_METER;

//     public final Face.Metrics FACE_METRICS;

//     private Type(float edgeLength, int ledsPerMeter, int ledsPerStrip) {
//       this.EDGE_WIDTH = this.EDGE_HEIGHT = edgeLength;

//       this.POINTS_PER_STRIP = ledsPerStrip;
//       this.POINTS_PER_CUBE = STRIPS_PER_CUBE*POINTS_PER_STRIP;
//       this.POINTS_PER_FACE = Face.STRIPS_PER_FACE*POINTS_PER_STRIP;

//       this.LEDS_PER_METER = ledsPerMeter;

//       this.FACE_METRICS = new Face.Metrics(
//         new Strip.Metrics(this.EDGE_WIDTH, POINTS_PER_STRIP, ledsPerMeter), 
//         new Strip.Metrics(this.EDGE_HEIGHT, POINTS_PER_STRIP, ledsPerMeter)
//       );
//     }

//   };

//  public static final Type CUBE_TYPE_WITH_MOST_PIXELS = Type.LARGE_DOUBLE;

//   public final static int FACES_PER_CUBE = 4; 

//   public final static int STRIPS_PER_CUBE = FACES_PER_CUBE*Face.STRIPS_PER_FACE;

//   public final static float CHANNEL_WIDTH = 1.5f;

//   public final Type type;

//   public final String id;

//   /**
//    * Immutable list of all cube faces
//    */
//   public final List<Face> faces;

//   /**
//    * Immutable list of all strips
//    */
//   public final List<Strip> strips;

//   /**
//    * Front left corner x coordinate 
//    */
//   public final float x;

//   /**
//    * Front left corner y coordinate 
//    */
//   public final float y;

//   /**
//    * Front left corner z coordinate 
//    */
//   public final float z;

//   /**
//    * Rotation about the x-axis 
//    */
//   public final float rx;

//   /**
//    * Rotation about the y-axis 
//    */
//   public final float ry;

//   /**
//    * Rotation about the z-axis 
//    */
//   public final float rz;

//   public Cube(String id, float x, float y, float z, float rx, float ry, float rz, LXTransform t, Type type) {
//     super(new Fixture(x, y, z, rx, ry, rz, t, type));
//     Fixture fixture = (Fixture) this.fixtures.get(0);
//     this.type     = type;
//     this.id       = id;

//     while (rx < 0) rx += 360;
//     while (ry < 0) ry += 360;
//     while (rz < 0) rz += 360;
//     rx = rx % 360;
//     ry = ry % 360;
//     rz = rz % 360;

//     this.x = x; 
//     this.y = y;
//     this.z = z;
//     this.rx = rx;
//     this.ry = ry;
//     this.rz = rz;

//     this.faces = Collections.unmodifiableList(fixture.faces);
//     this.strips = Collections.unmodifiableList(fixture.strips);
//   }

//   private static class Fixture extends LXAbstractFixture {

//     private final List<Face> faces = new ArrayList<Face>();
//     private final List<Strip> strips = new ArrayList<Strip>();

//     private Fixture(float x, float y, float z, float rx, float ry, float rz, LXTransform t, Cube.Type type) {
//       // LXTransform t = new LXTransform();
//       t.push();
//       t.translate(x, y, z);
//       t.translate(type.EDGE_WIDTH/2, type.EDGE_HEIGHT/2, type.EDGE_WIDTH/2);
//       t.rotateX(rx * PI / 180.);
//       t.rotateY(ry * PI / 180.);
//       t.rotateZ(rz * PI / 180.);
//       t.translate(-type.EDGE_WIDTH/2, -type.EDGE_HEIGHT/2, -type.EDGE_WIDTH/2);

//       for (int i = 0; i < FACES_PER_CUBE; i++) {
//         Face face = new Face(type.FACE_METRICS, (ry + 90*i) % 360, t);
//         this.faces.add(face);
//         for (Strip s : face.strips) {
//           this.strips.add(s);
//         }
//         for (LXPoint p : face.points) {
//           this.points.add(p);
//         }
//         t.translate(type.EDGE_WIDTH, 0, 0);
//         t.rotateY(HALF_PI);
//       }
//       t.pop();
//     }
//   }
// }

/**
 * A face is a component of a cube. It is comprised of four strips forming
 * the lights on this side of a cube. A whole cube is formed by four faces.
 */
// public static class Face extends LXModel {

//   public final static int STRIPS_PER_FACE = 3;

//   public static class Metrics {
//     final Strip.Metrics horizontal;
//     final Strip.Metrics vertical;

//     public Metrics(Strip.Metrics horizontal, Strip.Metrics vertical) {
//       this.horizontal = horizontal;
//       this.vertical = vertical;
//     }
//   }

//   /**
//    * Immutable list of strips
//    */
//   public final List<Strip> strips;

//   /**
//    * Rotation of the face about the y-axis
//    */
//   public final float ry;

//   Face(Metrics metrics, float ry, LXTransform transform) {
//     super(new Fixture(metrics, ry, transform));
//     Fixture fixture = (Fixture) this.fixtures.get(0);
//     this.ry = ry;
//     this.strips = Collections.unmodifiableList(fixture.strips);
//   }

//   private static class Fixture extends LXAbstractFixture {

//     private final List<Strip> strips = new ArrayList<Strip>();

//     private Fixture(Metrics metrics, float ry, LXTransform transform) {
//       transform.push();
//       transform.translate(0, metrics.vertical.length, 0);
//       for (int i = 0; i < STRIPS_PER_FACE; i++) {
//         boolean isHorizontal = (i % 2 == 0);
//         Strip.Metrics stripMetrics = isHorizontal ? metrics.horizontal : metrics.vertical;
//         Strip strip = new Strip(stripMetrics, ry, transform, isHorizontal);
//         this.strips.add(strip);
//         transform.translate(isHorizontal ? metrics.horizontal.length : metrics.vertical.length, 0, 0);
//         transform.rotateZ(HALF_PI);
//         for (LXPoint p : strip.points) {
//           this.points.add(p);
//         }
//       }
//       transform.pop();
//     }
//   }
// }

/**
 * Bar
 */
public static class Bar extends LXModel {

  public final static float STRIP_PADDING = 3;

  String id;
  String controllerId;
  float x;
  float y;
  float z;
  float rx;
  float ry;
  float rz;
  float height;
  List<Strip> strips;
  Strip outerStrip;
  Strip innerStrip;

  public Bar(String id, String controllerId, LXTransform transform, float rotX, float insideTrim, int numPointsInside, int numPointsOutside) {
    super(new Fixture(transform, rotX, insideTrim, numPointsInside, numPointsOutside));
    Fixture fixture = (Fixture) this.fixtures.get(0);

    this.id = id;
    this.controllerId = controllerId;
    this.x = transform.x();
    this.y = transform.y();
    this.z = transform.z();
    this.rx = rotX;
    this.ry = 0.;
    this.rz = 0.;
    this.height = numPointsOutside * 1.589608667;
    this.innerStrip = fixture.innerStrip;
    this.outerStrip = fixture.outerStrip;
    this.strips = new ArrayList<Strip>();
    this.strips.add(innerStrip);
    this.strips.add(outerStrip);
  }

  private static class Fixture extends LXAbstractFixture {
    Strip innerStrip;
    Strip outerStrip;

    private Fixture(LXTransform transform, float rotX, float insideTrim, int numPointsInside, int numPointsOutside) {
      transform.push();
      transform.rotateX(rotX * PI / 180.);

      // inner strip
      this.innerStrip = new Strip(transform, numPointsOutside, Strip.Orientation.DOWN);
      for (LXPoint p : innerStrip.points) {
        this.points.add(p);
      }

      transform.push();
      transform.translate(0, insideTrim, +1.5);

      // outer strip
      this.outerStrip = new Strip(transform, numPointsInside, Strip.Orientation.UP);
      for (LXPoint p : outerStrip.points) {
        this.points.add(p);
      }

      transform.pop();
      transform.pop();
    }
  }

  public float height() {
    return height;
  }
}

/**
 * Strip
 */
public static class Strip extends LXModel {

  public static final float POINT_SPACING = 1.589608667;

  public enum Orientation {
    UP, DOWN
  }

  public Object obj1 = null, obj2 = null;

  public final int size;

  Strip(LXTransform transform, int numPoints, Strip.Orientation orientation) {
    super(new Fixture(transform, numPoints, orientation));
    this.size = this.points.length;
  }

  private static class Fixture extends LXAbstractFixture {
    private Fixture(LXTransform transform, int numPoints, Strip.Orientation orientation) {
      transform.push();
      float spacingY = -Strip.POINT_SPACING;

      // bi-directional just to make patterns like swarm and spacetime more interesting
      switch (orientation) {
      case DOWN:
        spacingY = Strip.POINT_SPACING;
        transform.translate(0, -((numPoints-1) * Strip.POINT_SPACING), 0);
      }

      for (int i = 0; i < numPoints; i++) {
        LXPoint point = new LXPoint(transform.x(), transform.y(), transform.z());
        this.points.add(point);
        transform.translate(0, spacingY, 0);
      }
      transform.pop();
    }
  }

  public int size() {
    return size;
  }
}

// public static class Strip extends LXModel {

//   public static final float INCHES_PER_METER = 39.3701;

//   public static class Metrics {

//     public final float length;
//     public final int numPoints;
//     public final int ledsPerMeter;

//     public final float POINT_SPACING;

//     public Metrics(float length, int numPoints, int ledsPerMeter) {
//       this.length = length;
//       this.numPoints = numPoints;
//       this.ledsPerMeter = ledsPerMeter;
//       this.POINT_SPACING = INCHES_PER_METER / ledsPerMeter;
//     }

//     public Metrics(int numPoints, float spacing) {
//       this.length = numPoints * spacing;
//       this.numPoints = numPoints;
//       this.ledsPerMeter = (int)floor((INCHES_PER_METER / this.length) * numPoints);
//       this.POINT_SPACING = spacing;
//     }
//   }

//   public final Metrics metrics;

//   /**
//    * Whether this is a horizontal strip
//    */
//   public final boolean isHorizontal;

//   /**
//    * Rotation about the y axis
//    */
//   public final float ry;

//   public Object obj1 = null, obj2 = null;

//   Strip(Metrics metrics, float ry, List<LXPoint> points, boolean isHorizontal) {
//     super(points);
//     this.isHorizontal = isHorizontal;
//     this.metrics = metrics;   
//     this.ry = ry;
//   }

//   Strip(Metrics metrics, float ry, LXTransform transform, boolean isHorizontal) {
//     super(new Fixture(metrics, ry, transform));
//     this.metrics = metrics;
//     this.isHorizontal = isHorizontal;
//     this.ry = ry;
//   }

//   private static class Fixture extends LXAbstractFixture {
//     private Fixture(Metrics metrics, float ry, LXTransform transform) {
//       float offset = (metrics.length - (metrics.numPoints - 1) * metrics.POINT_SPACING) / 2.f;
//       transform.push();
//       transform.translate(offset, -Cube.CHANNEL_WIDTH/2.f, 0);
//       for (int i = 0; i < metrics.numPoints; i++) {
//         LXPoint point = new LXPoint(transform.x(), transform.y(), transform.z());
//         this.points.add(point);
//         transform.translate(metrics.POINT_SPACING, 0, 0);
//       }
//       transform.pop();
//     }
//   }
// }