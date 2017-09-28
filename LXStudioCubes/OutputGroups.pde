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

public OutputGroup[] buildOutputGroups() {
  return new OutputGroup[] {
    new OutputGroup(IP_ADDRESS_0, 2)
      .addPoints(model.splicePoints("vip-lounge-strip17", 0, 71))
      .addPoints(model.splicePoints("vip-lounge-strip7",  0, 70), OutputGroup.REVERSE)
      .addPoints(model.splicePoints("vip-lounge-strip10", 41, 8), OutputGroup.REVERSE),

    new OutputGroup(IP_ADDRESS_0, 3)
      .addPoints(model.splicePoints("vip-lounge-strip10", 0,  41), OutputGroup.REVERSE)
      .addPoints(model.splicePoints("vip-lounge-strip5",  0, 104)),

    new OutputGroup(IP_ADDRESS_0, 4)
      .addPoints(model.splicePoints("vip-lounge-strip7",   0, 206), OutputGroup.REVERSE)
      .addPoints(model.splicePoints("vip-lounge-strip18", 47,  25), OutputGroup.REVERSE),

    new OutputGroup(IP_ADDRESS_0, 7)
      .addPoints(model.splicePoints("vip-lounge-strip18", 0, 47), OutputGroup.REVERSE)
      .addPoints(model.splicePoints("vip-lounge-strip6",  0, 64))
      .addPoints(model.splicePoints("vip-lounge-strip14", 0, 59)),

    new OutputGroup(IP_ADDRESS_0, 10)
      .addPoints(model.splicePoints("vip-lounge-strip3", 0, 92), OutputGroup.REVERSE)
      .addPoints(model.splicePoints("vip-lounge-strip9", 0, 48), OutputGroup.REVERSE)
      .addPoints(model.splicePoints("vip-lounge-strip9", 0, 9)),

    new OutputGroup(IP_ADDRESS_0, 12)
      .addDeadPoints(2)
      .addPoints(model.splicePoints("vip-lounge-strip4", 0, 44), OutputGroup.REVERSE)
  };
}

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

    println("indices length: " + indices.length + ", points length: " + points.size() + " | ");
    for (int i = 0; i < points.size(); i++) {
      print(i + ", ");
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

  public OutputGroup addPoints(List<LXPoint> pointsToAdd) {
    for (LXPoint p : pointsToAdd) {
      this.points.add(p);
    }
    return this;
  }

  public OutputGroup addPoints(List<LXPoint> pointsToAdd, boolean reverseOrdering) {
    if (reverseOrdering) {
      Collections.reverse(Arrays.asList(pointsToAdd));
    }
    for (LXPoint p : pointsToAdd) {
      this.points.add(p);
    }
    return this;
  }

  public OutputGroup addDeadPoints(int numPointsToAdd) {
    LXPoint deadPoint = ((SLModel)model).points[((SLModel)model).points.length-1];

    for (int i = 0; i < numPointsToAdd; i++) {
      this.points.add(deadPoint);
    }

    return this;
  }
}