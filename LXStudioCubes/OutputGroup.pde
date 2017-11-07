public OutputGroup[] setupOutputGroups() {
  final SLModel m = (SLModel)model;

  return new OutputGroup[] {
    new OutputGroup("1")
      .addPoints(m.getStripById("A"))
      .addPoints(m.getStripById("B"), true),

    new OutputGroup("2")
      .addPoints(m.getStripById("C"))
      .addPoints(m.getStripById("D"), true),

    new OutputGroup("3")
      .addPoints(m.getStripById("E"))
      .addPoints(m.getStripById("F"), true),

    new OutputGroup("4")
      .addPoints(m.getStripById("G"))
      .addPoints(m.getStripById("H"), true),

    new OutputGroup("5")
      .addPoints(m.getStripById("I"))
      .addPoints(m.getStripById("J"), true),

    new OutputGroup("6")
      .addPoints(m.getStripById("K"))
      .addPoints(m.getStripById("L"), true),

    new OutputGroup("7")
      .addPoints(m.getStripById("M"))
      .addPoints(m.getStripById("N"), true)
      .addPoints(m.getStripById("O")),

    new OutputGroup("8")
      .addPoints(m.getStripById("P"))
      .addPoints(m.getStripById("Q"), true)
      .addPoints(m.getStripById("R"))
      .addPoints(m.getStripById("S"), true)
      .addPoints(m.getStripById("T"))
      .addPoints(m.getStripById("U"), true)
  };
}

public class OutputGroup {

  public final String id;
  
  private final List<LXPoint> points = new ArrayList<LXPoint>();

  public OutputGroup(String id) {
    this.id = id;
  }

  public OutputGroup(String id, List<LXPoint> points) {
    this.id = id;
    addPoints(points);
  }

  public OutputGroup(String id, LXModel fixture) {
    this.id = id;
    addPoints(Arrays.asList(fixture.points));
  }

  public OutputGroup(List<LXPoint> points) {
    this("", points);
  }

  public OutputGroup(LXModel fixture) {
    this("", Arrays.asList(fixture.points));
  }

  public List<LXPoint> getPoints() {
    return points;
  }

  public OutputGroup addPoints(List<LXPoint> points) {
    for (LXPoint point : points) {
      this.points.add(point);
    }
    return this;
  }

  public OutputGroup addPoints(List<LXPoint> points, boolean reverse) {
    List<LXPoint> localPoints = new ArrayList<LXPoint>(points);

    if (reverse) {
      Collections.reverse(localPoints);
    }

    addPoints(localPoints);
    return this;
  }

  public OutputGroup addPoints(LXModel fixture) {
    addPoints(Arrays.asList(fixture.points));
    return this;
  }

  public OutputGroup addPoints(LXModel fixture, boolean reverse) {
    addPoints(Arrays.asList(fixture.points), reverse);
    return this;
  }

  public OutputGroup reversePoints() {
    Collections.reverse(points);
    return this;
  }
}