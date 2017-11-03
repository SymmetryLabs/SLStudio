public OutputGroup[] setupOutputGroups() {
  HalfHeart heartA = model.getHeartById("half_heart_A");
  HalfHeart heartB = model.getHeartById("half_heart_B");
  HalfHeart heartC = model.getHeartById("half_heart_C");
  HalfHeart heartD = model.getHeartById("half_heart_D");

  return new OutputGroup[] {
    /*
     * Heart Half A
     *----------------------------------------------------*/
    new OutputGroup("175") // 1 (DOES IT WORK??)
      .addPoints(heartA.getPanel("1"))
      .addPoints(heartA.getPanel("2"))
      .addPoints(heartA.getPanel("3")),

    new OutputGroup("100") // 2
      .addPoints(heartA.getPanel("4"))
      .addPoints(heartA.getPanel("5"))
      .addPoints(heartA.getPanel("6")),

    new OutputGroup("380") // 3
      .addPoints(heartA.getPanel("7"))
      .addPoints(heartA.getPanel("8"))
      .addPoints(heartA.getPanel("17")),

    new OutputGroup("d8:80:39:9b:e:70") // 4
      .addPoints(heartA.getPanel("27"))
      .addPoints(heartA.getPanel("18"))
      .addPoints(heartA.getPanel("9")),

    new OutputGroup("420") // 5
      .addPoints(heartA.getPanel("10"))
      .addPoints(heartA.getPanel("19"))
      .addPoints(heartA.getPanel("20")),

    new OutputGroup("167") // 6
      .addPoints(heartA.getPanel("11"))
      .addPoints(heartA.getPanel("12"))
      .addPoints(heartA.getPanel("13")),

    new OutputGroup("102") // 7
      .addPoints(heartA.getPanel("14"))
      .addPoints(heartA.getPanel("15"))
      .addPoints(heartA.getPanel("16")),

    new OutputGroup("329") // 8
      .addPoints(heartA.getPanel("21"))
      .addPoints(heartA.getPanel("22"))
      .addPoints(heartA.getPanel("23")),

    new OutputGroup("310") // 9
      .addPoints(heartA.getPanel("24"))
      .addPoints(heartA.getPanel("25"))
      .addPoints(heartA.getPanel("26")),

    new OutputGroup("0") // 10
      .addPoints(heartA.getPanel("28"))
      .addPoints(heartA.getPanel("29")),

    new OutputGroup("146") // 11
      .addPoints(heartA.getPanel("30"))
      .addPoints(heartA.getPanel("31"))
      .addPoints(heartA.getPanel("32")),

    new OutputGroup("374") // 12
      .addPoints(heartA.getPanel("33"))
      .addPoints(heartA.getPanel("34"))
      .addPoints(heartA.getPanel("35")),

    new OutputGroup("423") // 13
      .addPoints(heartA.getPanel("36"))
      .addPoints(heartA.getPanel("37"))
      .addPoints(heartA.getPanel("44")),

    new OutputGroup("d8:80:39:9a:d3:69") // 14
      .addPoints(heartA.getPanel("38"))
      .addPoints(heartA.getPanel("39"))
      .addPoints(heartA.getPanel("40")),

    new OutputGroup("325") // 15
      .addPoints(heartA.getPanel("41"))
      .addPoints(heartA.getPanel("42"))
      .addPoints(heartA.getPanel("43")),

    new OutputGroup("d8:80:39:9a:d4:3b") // 16
      .addPoints(heartA.getPanel("45"))
      .addPoints(heartA.getPanel("46"))
      .addPoints(heartA.getPanel("47")),

    new OutputGroup("425") // 17
      .addPoints(heartA.getPanel("48"))
      .addPoints(heartA.getPanel("49"))
      .addPoints(heartA.getPanel("50")),

    new OutputGroup("190") // 18
      .addPoints(heartA.getPanel("51"))
      .addPoints(heartA.getPanel("52"))
      .addPoints(heartA.getPanel("53"))
      .addPoints(heartA.getPanel("54")),

    // /*
    //  * Heart Half B
    //  *----------------------------------------------------*/
    // new OutputGroup("0") // 1
    //   .addPoints(heartB.getPanel("1"))
    //   .addPoints(heartB.getPanel("2"))
    //   .addPoints(heartB.getPanel("3")),

    // new OutputGroup("0") // 2
    //   .addPoints(heartB.getPanel("4"))
    //   .addPoints(heartB.getPanel("5"))
    //   .addPoints(heartB.getPanel("6")),

    // new OutputGroup("0") // 3
    //   .addPoints(heartB.getPanel("7"))
    //   .addPoints(heartB.getPanel("8"))
    //   .addPoints(heartB.getPanel("17")),

    // new OutputGroup("0") // 4
    //   .addPoints(heartB.getPanel("9"))
    //   .addPoints(heartB.getPanel("18"))
    //   .addPoints(heartB.getPanel("27")),

    // new OutputGroup("0") // 5
    //   .addPoints(heartB.getPanel("10"))
    //   .addPoints(heartB.getPanel("19"))
    //   .addPoints(heartB.getPanel("20")),

    // new OutputGroup("0") // 6
    //   .addPoints(heartB.getPanel("11"))
    //   .addPoints(heartB.getPanel("12"))
    //   .addPoints(heartB.getPanel("13")),

    // new OutputGroup("0") // 7
    //   .addPoints(heartB.getPanel("14"))
    //   .addPoints(heartB.getPanel("15"))
    //   .addPoints(heartB.getPanel("16")),

    // new OutputGroup("0") // 8
    //   .addPoints(heartB.getPanel("21"))
    //   .addPoints(heartB.getPanel("22"))
    //   .addPoints(heartB.getPanel("23")),

    // new OutputGroup("0") // 9
    //   .addPoints(heartB.getPanel("24"))
    //   .addPoints(heartB.getPanel("25"))
    //   .addPoints(heartB.getPanel("26")),

    // new OutputGroup("0") // 10
    //   .addPoints(heartB.getPanel("28"))
    //   .addPoints(heartB.getPanel("29")),

    // new OutputGroup("0") // 11
    //   .addPoints(heartB.getPanel("30"))
    //   .addPoints(heartB.getPanel("31"))
    //   .addPoints(heartB.getPanel("32")),

    // new OutputGroup("0") // 12
    //   .addPoints(heartB.getPanel("33"))
    //   .addPoints(heartB.getPanel("34"))
    //   .addPoints(heartB.getPanel("35")),

    // new OutputGroup("0") // 13
    //   .addPoints(heartB.getPanel("36"))
    //   .addPoints(heartB.getPanel("37"))
    //   .addPoints(heartB.getPanel("44")),

    // new OutputGroup("0") // 14
    //   .addPoints(heartB.getPanel("38"))
    //   .addPoints(heartB.getPanel("39"))
    //   .addPoints(heartB.getPanel("40")),

    // new OutputGroup("0") // 15
    //   .addPoints(heartB.getPanel("41"))
    //   .addPoints(heartB.getPanel("42"))
    //   .addPoints(heartB.getPanel("43")),

    // new OutputGroup("0") // 16
    //   .addPoints(heartB.getPanel("45"))
    //   .addPoints(heartB.getPanel("46"))
    //   .addPoints(heartB.getPanel("47")),

    // new OutputGroup("0") // 17
    //   .addPoints(heartB.getPanel("48"))
    //   .addPoints(heartB.getPanel("49"))
    //   .addPoints(heartB.getPanel("50")),

    // new OutputGroup("0") // 18
    //   .addPoints(heartB.getPanel("51"))
    //   .addPoints(heartB.getPanel("52"))
    //   .addPoints(heartB.getPanel("53"))
    //   .addPoints(heartB.getPanel("54")),

    // /*
    //  * Heart Half C
    //  *----------------------------------------------------*/
    // new OutputGroup("0") // 1
    //   .addPoints(heartC.getPanel("1"))
    //   .addPoints(heartC.getPanel("2"))
    //   .addPoints(heartC.getPanel("3")),

    // new OutputGroup("0") // 2
    //   .addPoints(heartC.getPanel("4"))
    //   .addPoints(heartC.getPanel("5"))
    //   .addPoints(heartC.getPanel("6")),

    // new OutputGroup("0") // 3
    //   .addPoints(heartC.getPanel("7"))
    //   .addPoints(heartC.getPanel("8"))
    //   .addPoints(heartC.getPanel("17")),

    // new OutputGroup("0") // 4
    //   .addPoints(heartC.getPanel("9"))
    //   .addPoints(heartC.getPanel("18"))
    //   .addPoints(heartC.getPanel("27")),

    // new OutputGroup("0") // 5
    //   .addPoints(heartC.getPanel("10"))
    //   .addPoints(heartC.getPanel("19"))
    //   .addPoints(heartC.getPanel("20")),

    // new OutputGroup("0") // 6
    //   .addPoints(heartC.getPanel("11"))
    //   .addPoints(heartC.getPanel("12"))
    //   .addPoints(heartC.getPanel("13")),

    // new OutputGroup("0") // 7
    //   .addPoints(heartC.getPanel("14"))
    //   .addPoints(heartC.getPanel("15"))
    //   .addPoints(heartC.getPanel("16")),

    // new OutputGroup("0") // 8
    //   .addPoints(heartC.getPanel("21"))
    //   .addPoints(heartC.getPanel("22"))
    //   .addPoints(heartC.getPanel("23")),

    // new OutputGroup("0") // 9
    //   .addPoints(heartC.getPanel("24"))
    //   .addPoints(heartC.getPanel("25"))
    //   .addPoints(heartC.getPanel("26")),

    // new OutputGroup("0") // 10
    //   .addPoints(heartC.getPanel("28"))
    //   .addPoints(heartC.getPanel("29")),

    // new OutputGroup("0") // 11
    //   .addPoints(heartC.getPanel("30"))
    //   .addPoints(heartC.getPanel("31"))
    //   .addPoints(heartC.getPanel("32")),

    // new OutputGroup("0") // 12
    //   .addPoints(heartC.getPanel("33"))
    //   .addPoints(heartC.getPanel("34"))
    //   .addPoints(heartC.getPanel("35")),

    // new OutputGroup("0") // 13
    //   .addPoints(heartC.getPanel("36"))
    //   .addPoints(heartC.getPanel("37"))
    //   .addPoints(heartC.getPanel("44")),

    // new OutputGroup("0") // 14
    //   .addPoints(heartC.getPanel("38"))
    //   .addPoints(heartC.getPanel("39"))
    //   .addPoints(heartC.getPanel("40")),

    // new OutputGroup("0") // 15
    //   .addPoints(heartC.getPanel("41"))
    //   .addPoints(heartC.getPanel("42"))
    //   .addPoints(heartC.getPanel("43")),

    // new OutputGroup("0") // 16
    //   .addPoints(heartC.getPanel("45"))
    //   .addPoints(heartC.getPanel("46"))
    //   .addPoints(heartC.getPanel("47")),

    // new OutputGroup("0") // 17
    //   .addPoints(heartC.getPanel("48"))
    //   .addPoints(heartC.getPanel("49"))
    //   .addPoints(heartC.getPanel("50")),

    // new OutputGroup("0") // 18
    //   .addPoints(heartC.getPanel("51"))
    //   .addPoints(heartC.getPanel("52"))
    //   .addPoints(heartC.getPanel("53"))
    //   .addPoints(heartC.getPanel("54")),

    // /*
    //  * Heart Half D
    //  *----------------------------------------------------*/
    // new OutputGroup("0") // 1
    //   .addPoints(heartD.getPanel("1"))
    //   .addPoints(heartD.getPanel("2"))
    //   .addPoints(heartD.getPanel("3")),

    // new OutputGroup("0") // 2
    //   .addPoints(heartD.getPanel("4"))
    //   .addPoints(heartD.getPanel("5"))
    //   .addPoints(heartD.getPanel("6")),

    // new OutputGroup("0") // 3
    //   .addPoints(heartD.getPanel("7"))
    //   .addPoints(heartD.getPanel("8"))
    //   .addPoints(heartD.getPanel("17")),

    // new OutputGroup("0") // 4
    //   .addPoints(heartD.getPanel("9"))
    //   .addPoints(heartD.getPanel("18"))
    //   .addPoints(heartD.getPanel("27")),

    // new OutputGroup("0") // 5
    //   .addPoints(heartD.getPanel("10"))
    //   .addPoints(heartD.getPanel("19"))
    //   .addPoints(heartD.getPanel("20")),

    // new OutputGroup("0") // 6
    //   .addPoints(heartD.getPanel("11"))
    //   .addPoints(heartD.getPanel("12"))
    //   .addPoints(heartD.getPanel("13")),

    // new OutputGroup("0") // 7
    //   .addPoints(heartD.getPanel("14"))
    //   .addPoints(heartD.getPanel("15"))
    //   .addPoints(heartD.getPanel("16")),

    // new OutputGroup("0") // 8
    //   .addPoints(heartD.getPanel("21"))
    //   .addPoints(heartD.getPanel("22"))
    //   .addPoints(heartD.getPanel("23")),

    // new OutputGroup("0") // 9
    //   .addPoints(heartD.getPanel("24"))
    //   .addPoints(heartD.getPanel("25"))
    //   .addPoints(heartD.getPanel("26")),

    // new OutputGroup("0") // 10
    //   .addPoints(heartD.getPanel("28"))
    //   .addPoints(heartD.getPanel("29")),

    // new OutputGroup("0") // 11
    //   .addPoints(heartD.getPanel("30"))
    //   .addPoints(heartD.getPanel("31"))
    //   .addPoints(heartD.getPanel("32")),

    // new OutputGroup("0") // 12
    //   .addPoints(heartD.getPanel("33"))
    //   .addPoints(heartD.getPanel("34"))
    //   .addPoints(heartD.getPanel("35")),

    // new OutputGroup("0") // 13
    //   .addPoints(heartD.getPanel("36"))
    //   .addPoints(heartD.getPanel("37"))
    //   .addPoints(heartD.getPanel("44")),

    // new OutputGroup("0") // 14
    //   .addPoints(heartD.getPanel("38"))
    //   .addPoints(heartD.getPanel("39"))
    //   .addPoints(heartD.getPanel("40")),

    // new OutputGroup("0") // 15
    //   .addPoints(heartD.getPanel("41"))
    //   .addPoints(heartD.getPanel("42"))
    //   .addPoints(heartD.getPanel("43")),

    // new OutputGroup("0") // 16
    //   .addPoints(heartD.getPanel("45"))
    //   .addPoints(heartD.getPanel("46"))
    //   .addPoints(heartD.getPanel("47")),

    // new OutputGroup("0") // 17
    //   .addPoints(heartD.getPanel("48"))
    //   .addPoints(heartD.getPanel("49"))
    //   .addPoints(heartD.getPanel("50")),

    // new OutputGroup("0") // 18
    //   .addPoints(heartD.getPanel("51"))
    //   .addPoints(heartD.getPanel("52"))
    //   .addPoints(heartD.getPanel("53"))
    //   .addPoints(heartD.getPanel("54")),
  };
}

public class OutputGroup {

  public final String id;
  public final List<LXPoint> points = new ArrayList<LXPoint>();

  public OutputGroup(String id) {
    this(id, new ArrayList<LXPoint>());
  }

  public OutputGroup(List<LXPoint> points) {
    this("", points);
  }

  public OutputGroup(LXModel model) {
    this("", Arrays.asList(model.points));
  }

  public OutputGroup(String id, LXModel model) {
    this(id, Arrays.asList(model.points));
  }

  public OutputGroup(String id, List<LXPoint> points) {
    this.id = id;
    addPoints(points);
  }

  public List<LXPoint> getPoints() {
    return points;
  }

  public OutputGroup addPoint(LXPoint p) {
    this.points.add(p);
    return this;
  }

  public OutputGroup addPoints(LXModel model) {
    return addPoints(Arrays.asList(model.points), false);
  }

  public OutputGroup addPoints(LXModel model, boolean reverse) {
    return addPoints(Arrays.asList(model.points), reverse);
  }

  public OutputGroup addPoints(List<LXPoint> points) {
    for (LXPoint p : points) {
      this.points.add(p);
    }
    return this;
  }

  public OutputGroup addPoints(List<LXPoint> points, boolean reverse) {
    List<LXPoint> localPoints = new ArrayList(points);

    if (reverse) {
      Collections.reverse(localPoints);
    }

    for (LXPoint p : localPoints) {
      this.points.add(p);
    }
    return this;
  }

  public void reverseOrder() {
    Collections.reverse(points);
  }
}
