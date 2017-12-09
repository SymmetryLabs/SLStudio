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

public static class SLModel extends LXModel {
  public final List<Wicket> wickets;
  public final List<Strip> strips;
  public final Map<String, Wicket> wicketTable;
  private final Wicket[] _wickets;

  public SLModel(List<Wicket> wickets, Wicket[] wicketArr) {
    super(new Fixture(wicketArr));
    Fixture fixture = (Fixture)this.fixtures.get(0);

    _wickets = wicketArr;

    // Make unmodifiable accessors to the model data
    List<Wicket> wicketList = new ArrayList<Wicket>();
    List<Strip> stripList = new ArrayList<Strip>();
    Map<String, Wicket> _wicketTable = new HashMap<String, Wicket>();

    for (Wicket wicket : wickets) {
      wicketList.add(wicket);
      _wicketTable.put(wicket.id, wicket);

      for (Strip strip : wicket.strips) {
        stripList.add(strip);
      }
    }

    this.wickets     = Collections.unmodifiableList(wicketList);
    this.strips      = Collections.unmodifiableList(stripList);
    this.wicketTable = Collections.unmodifiableMap(_wicketTable);
  }

  private static class Fixture extends LXAbstractFixture {
    private Fixture(Wicket[] wicketArr) {
      for (Wicket wicket : wicketArr) {
        for (LXPoint point : wicket.points) {
          this.points.add(point);
        }
      }
    }
  }

  public Wicket getWicketByRawIndex(int index) {
    return _wickets[index];
  }
  
  public Wicket getWicketById(String id) {
    return this.wicketTable.get(id);
  }
}

public static class Wicket extends LXModel {

  public enum Type {
    INSIDE, OUTSIDE
  }

  public final String id;

  public final String ipAddress;

  public final Type type;

  public final List<Strip> strips;

  public final List<Strip> spotStrips;

  public final List<Strip> washStrips;

  public final float x;

  public final float y;

  public final float z;

  private final Map<String, Strip> stripMap = new HashMap<String, Strip>();

  public Wicket(String id, String ipAddress, Type type, float[] coordinates, float[] rotations, LXTransform transform) {
    super(new Fixture(type, coordinates, rotations, transform));
    Fixture fixture = (Fixture)this.fixtures.get(0);
    this.id = id;
    this.ipAddress = ipAddress;
    this.type = type;
    this.strips = fixture.strips;
    this.spotStrips = fixture.spotStrips;
    this.washStrips = fixture.washStrips;
    this.x = coordinates[0];
    this.y = coordinates[1];
    this.z = coordinates[2];

    for (Strip strip : strips) {
      this.stripMap.put(strip.id, strip);
    }
  }

  public Strip getStripById(String id) {
    return this.stripMap.get(id);
  }

  private static class Fixture extends LXAbstractFixture {

    private final float STRIP_SPACING = 4;

    private final List<Strip> strips = new ArrayList<Strip>();

    private final List<Strip> spotStrips = new ArrayList<Strip>();

    private final List<Strip> washStrips = new ArrayList<Strip>();

    private Fixture(Type type, float[] coordinates, float[] rotations, LXTransform transform) {
      transform.push();
      transform.translate(coordinates[0], coordinates[1], coordinates[2]);
      transform.translate(62.13, 0, 0);
      transform.rotateX(rotations[0] * PI / 180);
      transform.rotateY(rotations[1] * PI / 180);
      transform.rotateZ(rotations[2] * PI / 180);
      transform.translate(-62.13, 0, 0);

      // left side
      transform.rotateZ(90 * PI / 180);

      // strip 1
      Strip strip1 = new Strip("1", Strip.Type.LONG, transform, false);
      this.spotStrips.add(strip1);
      Strip strip1wash = new Strip("1w", Strip.Type.LONG, transform, false);
      this.washStrips.add(strip1wash);

      // strip 2
      transform.translate(strip1.type.LENGTH + STRIP_SPACING, 0, 0);
      Strip strip2 = new Strip("2", Strip.Type.LONG, transform, false);
      this.spotStrips.add(strip2);
      Strip strip2wash = new Strip("2w", Strip.Type.LONG, transform, false);
      this.washStrips.add(strip2wash);

      // top 
      transform.translate(strip2.type.LENGTH + STRIP_SPACING, 0, 0);
      transform.rotateZ(-90 * PI / 180);
      transform.translate(STRIP_SPACING, 0, 0);

      // strip 3
      Strip strip3 = new Strip("3", Strip.Type.LONG, transform, true);
      this.spotStrips.add(strip3);
      Strip strip3wash = new Strip("3w", Strip.Type.LONG, transform, false);
      this.washStrips.add(strip3wash);

      // strip 4
      transform.translate(strip3.type.LENGTH + STRIP_SPACING, 0, 0);
      Strip strip4 = new Strip("4", Strip.Type.MEDIUM, transform, true);
      this.spotStrips.add(strip4);
      Strip strip4wash = new Strip("4w", Strip.Type.MEDIUM, transform, false);
      this.washStrips.add(strip4wash);

      // strip 5
      transform.translate(strip4.type.LENGTH + STRIP_SPACING, 0, 0);
      Strip strip5 = new Strip("5", Strip.Type.LONG, transform, true);
      this.spotStrips.add(strip5);
      Strip strip5wash = new Strip("5w", Strip.Type.LONG, transform, false);
      this.washStrips.add(strip5wash);

      // right
      transform.translate(strip5.type.LENGTH + STRIP_SPACING, 0, 0);
      transform.rotateZ(-90 * PI / 180);
      transform.translate(STRIP_SPACING, 0, 0);

      // strip 6
      Strip strip6 = new Strip("6", Strip.Type.LONG, transform, false);
      this.spotStrips.add(strip6);
      Strip strip6wash = new Strip("6w", Strip.Type.LONG, transform, false);
      this.washStrips.add(strip6wash);

      // strip 7
      transform.translate(strip6.type.LENGTH + STRIP_SPACING, 0, 0);
      Strip strip7 = new Strip("7", Strip.Type.LONG, transform, false);
      this.spotStrips.add(strip7);
      Strip strip7wash = new Strip("7w", Strip.Type.LONG, transform, false);
      this.washStrips.add(strip7wash);

      transform.pop();

      for (Strip strip : spotStrips) {
        strips.add(strip);
      }
      for (Strip strip : washStrips) {
        strips.add(strip);
      }
      for (Strip strip : strips) {
        for (LXPoint point : strip.points) {
          this.points.add(point);
        }
      }
    }
  }

}

/**
 * A strip is a linear run of points.
 */
public static class Strip extends LXModel {

  public static final int LEDS_PER_METER = 60;

  public static final float INCHES_PER_METER = 39.3701;

  public static final float PIXEL_PITCH = INCHES_PER_METER / LEDS_PER_METER;

  public enum Type {
    SHORT  (30),
    MEDIUM (45),
    LONG   (60);

    public final int NUM_POINTS;

    public final float LENGTH;

    private Type(int numPoints) {
      this.NUM_POINTS = numPoints;
      this.LENGTH = NUM_POINTS * PIXEL_PITCH;
    }
  }

  public static class Metrics {
    public final float length;
    public final int numPoints;
    public final int ledsPerMeter;

    public final float POINT_SPACING;

    public Metrics(int numPoints, float spacing) {
      this.length = numPoints * spacing;
      this.numPoints = numPoints;
      this.ledsPerMeter = (int)floor((INCHES_PER_METER / this.length) * numPoints);
      this.POINT_SPACING = spacing;
    }
  }

  public final String id;

  public final Type type;

  public final Metrics metrics;

  public final boolean isHorizontal;

  public final float ry;

  public Object obj1 = null, obj2 = null;

  Strip(String id, Type type, LXTransform transform, boolean isHorizontal) {
    this(id, type, new Strip.Metrics(type.NUM_POINTS, PIXEL_PITCH), 0, transform, isHorizontal);
  }

  Strip(String id, Type type, Metrics metrics, float ry, LXTransform transform, boolean isHorizontal) {
    super(new Fixture(metrics, ry, transform));
    this.id = id;
    this.type = type;
    this.metrics = metrics;
    this.isHorizontal = isHorizontal;
    this.ry = ry;
  }

  private static class Fixture extends LXAbstractFixture {
    private Fixture(Metrics metrics, float ry, LXTransform transform) {
      float offset = (metrics.length - (metrics.numPoints - 1) * metrics.POINT_SPACING) / 2.f;
      transform.push();
      for (int i = 0; i < metrics.numPoints; i++) {
        LXPoint point = new LXPoint(transform.x(), transform.y(), transform.z());
        this.points.add(point);
        transform.translate(metrics.POINT_SPACING, 0, 0);
      }
      transform.pop();
    }
  }
}