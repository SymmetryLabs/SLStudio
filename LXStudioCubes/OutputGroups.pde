public final String IP_ADDRESS_0 = "10.200.1.11";
public final String IP_ADDRESS_1 = "10.200.1.12";
public final String IP_ADDRESS_2 = "10.200.1.13";
public final String IP_ADDRESS_3 = "10.200.1.14";
public final String IP_ADDRESS_4 = "10.200.1.15";
public final String IP_ADDRESS_5 = "10.200.1.16";
public final String IP_ADDRESS_6 = "10.200.1.17";
public final String IP_ADDRESS_7 = "10.200.1.18";
public final String IP_ADDRESS_8 = "10.200.1.19";
public final String IP_ADDRESS_9 = "10.200.1.20";

public final List<OutputGroup> OUTPUT_GROUP_CONFIG = {
  new OutputGroup(IP_ADDRESS_0, 1)
    .addPoints(LedTape.splicePoints("strip1", 0, 70, OutputGroup.REVERSE))
    .addPoints(LedTape.splicePoints("strip2", 0, 70)
    .reversePoints(),

  new OutputGroup(IP_ADDRESS_0, 2)
    .addPoints(LedTape.splicePoints("strip3", 0, 170)),

  new OutputGroup(IP_ADDRESS_0, 3)
    .addPoints(LedTape.splicePoints("strip4", 0, 170)),

  new OutputGroup(IP_ADDRESS_0, 4)
    .addPoints(LedTape.splicePoints("strip5", 0, 170, OutputGroup.REVERSE))
    .reversePoints(),

  new OutputGroup(IP_ADDRESS_0, 5)
    .addPoints(LedTape.splicePoints("strip6", 0, 170, OutputGroup.REVERSE)),

  new OutputGroup(IP_ADDRESS_0, 6)
    .addPoints(LedTape.splicePoints("strip7", 0, 170)),

  new OutputGroup(IP_ADDRESS_0, 7)
    .addPoints(LedTape.splicePoints("strip8", 0, 170))
    .reversePoints(),

  new OutputGroup(IP_ADDRESS_0, 8)
    .addPoints(LedTape.splicePoints("strip9", 0, 170)),
};

public class OutputGroup {

  public static final boolean REVERSE = true;

  private final int MAX_NUMBER_LEDS_PER_UNVERSE = 170;

  private final List<LXPoint> points = new ArrayList<LXPoint>();

  public final String ipAddress;
  public final int universe;

  public OutputGroup(String ipAddress, int universe) {
    this.ipAddress = ipAddress;
    this.universe = universe;
  }

  public List<LXPoint> getPoints() {
    return points;
  }

  public int[] getIndices() {
    int[] indices = new int[points.size()];

    for (int i = 0; i < points.length; i++) {
      indices[i] = points.get(i).index;
    }
    return indices;
  }

  public OutputGroup reversePoints() {
    Collections.reverse(Arrays.asList(points));
    return this;
  }

  public OutputGroup addPoints(LXPoint[] pointsToAdd) {
    for (LXPoint p : pointsToAdd) {
      this.points.add(p);
    }
    return this;
  }

  public OutputGroup addPoints(LXPoint[] pointsToAdd, boolean reverseOrdering) {
    if (reverseOrdering) {
      Collections.reverse(Arrays.asList(pointsToAdd));
    }
    for (LXPoint p : pointsToAdd) {
      this.points.add(p);
    }
    return this;
  }

  public LedTape addPoints(List<LXPoint> pointsToAdd) {
    for (LXPoint p : pointsToAdd) {
      this.points.add(p);
    }
    return this;
  }

  public LedTape addPoints(List<LXPoint> pointsToAdd, boolean reverseOrdering) {
    if (reverseOrdering) {
      Collections.reverse(Arrays.asList(pointsToAdd));
    }
    for (LXPoint p : pointsToAdd) {
      this.points.add(p);
    }
    return this;
  }
}