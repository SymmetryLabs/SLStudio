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
  public final List<Strip> strips;
  public final Map<String, Cube> cubeTable;
  private final Cube[] _cubes;
  public final List<Branch> branches;

  public final List<LXModel> objModels;

  public SLModel(List<LXModel> objModels, List<Tower> towers, Cube[] cubeArr, List<Strip> strips, Branch[] branchArr) {
    super(new Fixture(objModels, cubeArr, strips, branchArr));
    Fixture fixture = (Fixture) this.fixtures.get(0);

    _cubes = cubeArr;

    // Make unmodifiable accessors to the model data
    List<Tower> towerList = new ArrayList<Tower>();
    List<Cube> cubeList = new ArrayList<Cube>();
    List<Face> faceList = new ArrayList<Face>();
    List<Strip> stripList = new ArrayList<Strip>();
    Map<String, Cube> _cubeTable = new HashMap<String, Cube>();
    List<Branch> branchList = new ArrayList<Branch>();
    
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

    for (Branch branch : branchArr) {
      branchList.add(branch);
      for (Strip s : branch.strips) {
        stripList.add(s);
      }
    }

    this.towers    = Collections.unmodifiableList(towerList);
    this.cubes     = Collections.unmodifiableList(cubeList);
    this.faces     = Collections.unmodifiableList(faceList);
    this.strips    = Collections.unmodifiableList(stripList);
    this.cubeTable = Collections.unmodifiableMap (_cubeTable);
    this.objModels = objModels;
    this.branches = Collections.unmodifiableList(branchList);
  }

  private static class Fixture extends LXAbstractFixture {
    private Fixture(List<LXModel> objModels, Cube[] cubeArr, List<Strip> strips, Branch[] branchArr) {
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
      for (Strip strip : strips) {
        for (LXPoint point : strip.points) {
          this.points.add(point);
        }
      }
      for (Branch branch : branchArr) {
        if (branch != null) {
          for (LXPoint point : branch.points) {
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

public static class Heart extends LXModel {

  public Heart(List<LXPoint> points) {
    super(points);
  }

}

public static class Branch extends LXModel {

  public final String id;

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

  public Branch(String id, float x, float y, float z, float rx, float ry, float rz, LXTransform t) {
    super(new Fixture(x, y, z, rx, ry, rz, t));
    Fixture fixture = (Fixture) this.fixtures.get(0);
    this.id = id;

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

    this.strips = Collections.unmodifiableList(fixture.strips);
  }

  private static class Fixture extends LXAbstractFixture {

    private final List<Strip> strips = new ArrayList<Strip>();

    private Fixture(float x, float y, float z, float rx, float ry, float rz, LXTransform t) {
      // LXTransform t = new LXTransform();
      t.push();
      t.translate(x, y, z);
      t.rotateX(rx * PI / 180.);
      t.rotateY(ry * PI / 180.);
      t.rotateZ(rz * PI / 180.);

      Strip.Metrics metrics = new Strip.Metrics(4, 7, 30);

      // strip 1
      t.push();
      t.translate(0, 0, 0);
      t.rotateX(0 * PI / 180.);
      t.rotateY(0 * PI / 180.);
      t.rotateZ(135 * PI / 180.);

      List<LXPoint> points1 = new ArrayList<LXPoint>();
      for (int i = 0; i < 4; i++) {
        points1.add(new LXPoint(t.x(), t.y(), t.z()));
        t.translate(metrics.POINT_SPACING, 0, 0);
      }
      t.translate(-metrics.POINT_SPACING*2, 0, 0);
      for (int i = 0; i < 3; i++) {
        points1.add(new LXPoint(t.x(), t.y(), t.z()));
        t.translate(-metrics.POINT_SPACING, 0, 0);
      }

      Strip strip1 = new Strip(metrics, 0, points1, false);
      this.strips.add(strip1);
      for (LXPoint p : strip1.points) {
        this.points.add(p);
      }
      t.pop();

      // strip 2
      t.push();
      t.translate(-1, 1, 0);
      t.rotateX(0 * PI / 180.);
      t.rotateY(0 * PI / 180.);
      t.rotateZ(170 * PI / 180.);

      List<LXPoint> points2 = new ArrayList<LXPoint>();
      for (int i = 0; i < 4; i++) {
        points2.add(new LXPoint(t.x(), t.y(), t.z()));
        t.translate(metrics.POINT_SPACING, 0, 0);
      }
      t.translate(-metrics.POINT_SPACING*2, 0, 0);
      for (int i = 0; i < 3; i++) {
        points2.add(new LXPoint(t.x(), t.y(), t.z()));
        t.translate(-metrics.POINT_SPACING, 0, 0);
      }

      Strip strip2 = new Strip(metrics, 0, points2, false);
      this.strips.add(strip2);
      for (LXPoint p : strip2.points) {
        this.points.add(p);
      }
      t.pop();

      // strip 3
      t.push();
      t.translate(-4, 5, 0);
      t.rotateX(0 * PI / 180.);
      t.rotateY(0 * PI / 180.);
      t.rotateZ(170 * PI / 180.);

      List<LXPoint> points3 = new ArrayList<LXPoint>();
      for (int i = 0; i < 4; i++) {
        points3.add(new LXPoint(t.x(), t.y(), t.z()));
        t.translate(metrics.POINT_SPACING, 0, 0);
      }
      t.translate(-metrics.POINT_SPACING*2, 0, 0);
      for (int i = 0; i < 3; i++) {
        points3.add(new LXPoint(t.x(), t.y(), t.z()));
        t.translate(-metrics.POINT_SPACING, 0, 0);
      }

      Strip strip3 = new Strip(metrics, 0, points3, false);
      this.strips.add(strip3);
      for (LXPoint p : strip3.points) {
        this.points.add(p);
      }
      t.pop();

      // strip 4
      t.push();
      t.translate(-5, 6, 0);
      t.rotateX(0 * PI / 180.);
      t.rotateY(0 * PI / 180.);
      t.rotateZ(225 * PI / 180.);

      List<LXPoint> points4 = new ArrayList<LXPoint>();
      for (int i = 0; i < 4; i++) {
        points4.add(new LXPoint(t.x(), t.y(), t.z()));
        t.translate(metrics.POINT_SPACING, 0, 0);
      }
      t.translate(-metrics.POINT_SPACING*2, 0, 0);
      for (int i = 0; i < 3; i++) {
        points4.add(new LXPoint(t.x(), t.y(), t.z()));
        t.translate(-metrics.POINT_SPACING, 0, 0);
      }

      Strip strip4 = new Strip(metrics, 0, points4, false);
      this.strips.add(strip4);
      for (LXPoint p : strip4.points) {
        this.points.add(p);
      }
      t.pop();

      // strip 5
      t.push();
      t.translate(2, 5, 0);
      t.rotateX(0 * PI / 180.);
      t.rotateY(0 * PI / 180.);
      t.rotateZ(225 * PI / 180.);

      List<LXPoint> points5 = new ArrayList<LXPoint>();
      for (int i = 0; i < 4; i++) {
        points5.add(new LXPoint(t.x(), t.y(), t.z()));
        t.translate(metrics.POINT_SPACING, 0, 0);
      }
      t.translate(-metrics.POINT_SPACING*2, 0, 0);
      for (int i = 0; i < 3; i++) {
        points5.add(new LXPoint(t.x(), t.y(), t.z()));
        t.translate(-metrics.POINT_SPACING, 0, 0);
      }

      Strip strip5 = new Strip(metrics, 0, points5, false);
      this.strips.add(strip5);
      for (LXPoint p : strip5.points) {
        this.points.add(p);
      }
      t.pop();

      // strip 6
      t.push();
      t.translate(0, 11, 0);
      t.rotateX(0 * PI / 180.);
      t.rotateY(0 * PI / 180.);
      t.rotateZ(170 * PI / 180.);

      List<LXPoint> points6 = new ArrayList<LXPoint>();
      for (int i = 0; i < 4; i++) {
        points6.add(new LXPoint(t.x(), t.y(), t.z()));
        t.translate(metrics.POINT_SPACING, 0, 0);
      }
      t.translate(-metrics.POINT_SPACING*2, 0, 0);
      for (int i = 0; i < 3; i++) {
        points6.add(new LXPoint(t.x(), t.y(), t.z()));
        t.translate(-metrics.POINT_SPACING, 0, 0);
      }

      Strip strip6 = new Strip(metrics, 0, points6, false);
      this.strips.add(strip6);
      for (LXPoint p : strip6.points) {
        this.points.add(p);
      }
      t.pop();

      // strip 7
      t.push();
      t.translate(0, 12, 0);
      t.rotateX(0 * PI / 180.);
      t.rotateY(0 * PI / 180.);
      t.rotateZ(225 * PI / 180.);

      List<LXPoint> points7 = new ArrayList<LXPoint>();
      for (int i = 0; i < 4; i++) {
        points7.add(new LXPoint(t.x(), t.y(), t.z()));
        t.translate(metrics.POINT_SPACING, 0, 0);
      }
      t.translate(-metrics.POINT_SPACING*2, 0, 0);
      for (int i = 0; i < 3; i++) {
        points7.add(new LXPoint(t.x(), t.y(), t.z()));
        t.translate(-metrics.POINT_SPACING, 0, 0);
      }

      Strip strip7 = new Strip(metrics, 0, points7, false);
      this.strips.add(strip7);
      for (LXPoint p : strip7.points) {
        this.points.add(p);
      }
      t.pop();

      // strip 8
      t.push();
      t.translate(3, 13, 0);
      t.rotateX(0 * PI / 180.);
      t.rotateY(0 * PI / 180.);
      t.rotateZ(-90 * PI / 180.);

      List<LXPoint> points8 = new ArrayList<LXPoint>();
      for (int i = 0; i < 4; i++) {
        points8.add(new LXPoint(t.x(), t.y(), t.z()));
        t.translate(metrics.POINT_SPACING, 0, 0);
      }
      t.translate(-metrics.POINT_SPACING*2, 0, 0);
      for (int i = 0; i < 3; i++) {
        points8.add(new LXPoint(t.x(), t.y(), t.z()));
        t.translate(-metrics.POINT_SPACING, 0, 0);
      }

      Strip strip8 = new Strip(metrics, 0, points8, false);
      this.strips.add(strip8);
      for (LXPoint p : strip8.points) {
        this.points.add(p);
      }
      t.pop();

      // strip 9
      t.push();
      t.translate(6, 13, 0);
      t.rotateX(0 * PI / 180.);
      t.rotateY(0 * PI / 180.);
      t.rotateZ(-45 * PI / 180.);

      List<LXPoint> points9 = new ArrayList<LXPoint>();
      for (int i = 0; i < 4; i++) {
        points9.add(new LXPoint(t.x(), t.y(), t.z()));
        t.translate(metrics.POINT_SPACING, 0, 0);
      }
      t.translate(-metrics.POINT_SPACING*2, 0, 0);
      for (int i = 0; i < 3; i++) {
        points9.add(new LXPoint(t.x(), t.y(), t.z()));
        t.translate(-metrics.POINT_SPACING, 0, 0);
      }

      Strip strip9 = new Strip(metrics, 0, points9, false);
      this.strips.add(strip9);
      for (LXPoint p : strip9.points) {
        this.points.add(p);
      }
      t.pop();

      // strip 10
      t.push();
      t.translate(7, 12, 0);
      t.rotateX(0 * PI / 180.);
      t.rotateY(0 * PI / 180.);
      t.rotateZ(10 * PI / 180.);

      List<LXPoint> points10 = new ArrayList<LXPoint>();
      for (int i = 0; i < 4; i++) {
        points10.add(new LXPoint(t.x(), t.y(), t.z()));
        t.translate(metrics.POINT_SPACING, 0, 0);
      }
      t.translate(-metrics.POINT_SPACING*2, 0, 0);
      for (int i = 0; i < 3; i++) {
        points10.add(new LXPoint(t.x(), t.y(), t.z()));
        t.translate(-metrics.POINT_SPACING, 0, 0);
      }

      Strip strip10 = new Strip(metrics, 0, points10, false);
      this.strips.add(strip10);
      for (LXPoint p : strip10.points) {
        this.points.add(p);
      }
      t.pop();

      // strip 11
      t.push();
      t.translate(5, 6, 0);
      t.rotateX(0 * PI / 180.);
      t.rotateY(0 * PI / 180.);
      t.rotateZ(-45 * PI / 180.);
 
      List<LXPoint> points11 = new ArrayList<LXPoint>();
      for (int i = 0; i < 4; i++) {
        points11.add(new LXPoint(t.x(), t.y(), t.z()));
        t.translate(metrics.POINT_SPACING, 0, 0);
      }
      t.translate(-metrics.POINT_SPACING*2, 0, 0);
      for (int i = 0; i < 3; i++) {
        points11.add(new LXPoint(t.x(), t.y(), t.z()));
        t.translate(-metrics.POINT_SPACING, 0, 0);
      }

      Strip strip11 = new Strip(metrics, 0, points11, false);
      this.strips.add(strip11);
      for (LXPoint p : strip11.points) {
        this.points.add(p);
      }
      t.pop();

      // strip 12
      t.push();
      t.translate(10, 7, 0);
      t.rotateX(0 * PI / 180.);
      t.rotateY(0 * PI / 180.);
      t.rotateZ(-45 * PI / 180.);

      List<LXPoint> points12 = new ArrayList<LXPoint>();
      for (int i = 0; i < 4; i++) {
        points12.add(new LXPoint(t.x(), t.y(), t.z()));
        t.translate(metrics.POINT_SPACING, 0, 0);
      }
      t.translate(-metrics.POINT_SPACING*2, 0, 0);
      for (int i = 0; i < 3; i++) {
        points12.add(new LXPoint(t.x(), t.y(), t.z()));
        t.translate(-metrics.POINT_SPACING, 0, 0);
      }

      Strip strip12 = new Strip(metrics, 0, points12, false);
      this.strips.add(strip12);
      for (LXPoint p : strip12.points) {
        this.points.add(p);
      }
      t.pop();

      // strip 13
      t.push();
      t.translate(11, 6, 0);
      t.rotateX(0 * PI / 180.);
      t.rotateY(0 * PI / 180.);
      t.rotateZ(20 * PI / 180.);

      List<LXPoint> points13 = new ArrayList<LXPoint>();
      for (int i = 0; i < 4; i++) {
        points13.add(new LXPoint(t.x(), t.y(), t.z()));
        t.translate(metrics.POINT_SPACING, 0, 0);
      }
      t.translate(-metrics.POINT_SPACING*2, 0, 0);
      for (int i = 0; i < 3; i++) {
        points13.add(new LXPoint(t.x(), t.y(), t.z()));
        t.translate(-metrics.POINT_SPACING, 0, 0);
      }

      Strip strip13 = new Strip(metrics, 0, points13, false);
      this.strips.add(strip13);
      for (LXPoint p : strip13.points) {
        this.points.add(p);
      }
      t.pop();

      // strip 14
      t.push();
      t.translate(8.5, 2.5, 0);
      t.rotateX(0 * PI / 180.);
      t.rotateY(0 * PI / 180.);
      t.rotateZ(0 * PI / 180.);

      List<LXPoint> points14 = new ArrayList<LXPoint>();
      for (int i = 0; i < 4; i++) {
        points14.add(new LXPoint(t.x(), t.y(), t.z()));
        t.translate(metrics.POINT_SPACING, 0, 0);
      }
      t.translate(-metrics.POINT_SPACING*2, 0, 0);
      for (int i = 0; i < 3; i++) {
        points14.add(new LXPoint(t.x(), t.y(), t.z()));
        t.translate(-metrics.POINT_SPACING, 0, 0);
      }

      Strip strip14 = new Strip(metrics, 0, points14, false);
      this.strips.add(strip14);
      for (LXPoint p : strip14.points) {
        this.points.add(p);
      }
      t.pop();

      // strip 15
      t.push();
      t.translate(8.5, 1, 0);
      t.rotateX(0 * PI / 180.);
      t.rotateY(0 * PI / 180.);
      t.rotateZ(45 * PI / 180.);

      List<LXPoint> points15 = new ArrayList<LXPoint>();
      for (int i = 0; i < 4; i++) {
        points15.add(new LXPoint(t.x(), t.y(), t.z()));
        t.translate(metrics.POINT_SPACING, 0, 0);
      }
      t.translate(-metrics.POINT_SPACING*2, 0, 0);
      for (int i = 0; i < 3; i++) {
        points15.add(new LXPoint(t.x(), t.y(), t.z()));
        t.translate(-metrics.POINT_SPACING, 0, 0);
      }

      Strip strip15 = new Strip(metrics, 0, points15, false);
      this.strips.add(strip15);
      for (LXPoint p : strip15.points) {
        this.points.add(p);
      }
      t.pop();

      t.pop();
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