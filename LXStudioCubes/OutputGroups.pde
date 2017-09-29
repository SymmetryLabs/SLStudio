public final String[] IP_ADDRESSES = {
  "10.200.1.11",
  "10.200.1.12",
  "10.200.1.13",
  "10.200.1.14",
  "10.200.1.15",
  "10.200.1.16",
  "10.200.1.17",
  "10.200.1.18",
  "10.200.1.19",
  "10.200.1.20"
};

int UNIVERSE_1  = 1;  int UNIVERSE_2  = 2;  int UNIVERSE_3  = 3;  int UNIVERSE_4  = 4;  int UNIVERSE_5  = 5;  int UNIVERSE_6  = 6;  int UNIVERSE_7  = 7;  int UNIVERSE_8  = 8;  int UNIVERSE_9  = 9;  int UNIVERSE_10 = 10;
int UNIVERSE_11 = 11; int UNIVERSE_12 = 12; int UNIVERSE_13 = 13; int UNIVERSE_14 = 14; int UNIVERSE_15 = 15; int UNIVERSE_16 = 16; int UNIVERSE_17 = 17; int UNIVERSE_18 = 18; int UNIVERSE_19 = 19; int UNIVERSE_20 = 20;
int UNIVERSE_21 = 21; int UNIVERSE_22 = 22; int UNIVERSE_23 = 23; int UNIVERSE_24 = 24; int UNIVERSE_25 = 25; int UNIVERSE_26 = 26; int UNIVERSE_27 = 27; int UNIVERSE_28 = 28; int UNIVERSE_29 = 29; int UNIVERSE_30 = 30;
int UNIVERSE_31 = 31; int UNIVERSE_32 = 32; int UNIVERSE_33 = 33; int UNIVERSE_34 = 34; int UNIVERSE_35 = 35; int UNIVERSE_36 = 36; int UNIVERSE_37 = 37; int UNIVERSE_38 = 38; int UNIVERSE_39 = 39; int UNIVERSE_40 = 40;
int UNIVERSE_41 = 41; int UNIVERSE_42 = 42; int UNIVERSE_43 = 43; int UNIVERSE_44 = 44; int UNIVERSE_45 = 45; int UNIVERSE_46 = 46; int UNIVERSE_47 = 47; int UNIVERSE_48 = 48; int UNIVERSE_49 = 49; int UNIVERSE_50 = 50;
int UNIVERSE_51 = 51; int UNIVERSE_52 = 52; int UNIVERSE_53 = 53; int UNIVERSE_54 = 54; int UNIVERSE_55 = 55; int UNIVERSE_56 = 56; int UNIVERSE_57 = 57; int UNIVERSE_58 = 58; int UNIVERSE_59 = 59; int UNIVERSE_60 = 60;
int UNIVERSE_61 = 61; int UNIVERSE_62 = 62; int UNIVERSE_63 = 63; int UNIVERSE_64 = 64; int UNIVERSE_65 = 65; int UNIVERSE_66 = 66; int UNIVERSE_67 = 67; int UNIVERSE_68 = 68; int UNIVERSE_69 = 69; int UNIVERSE_70 = 70;
int UNIVERSE_71 = 71; int UNIVERSE_72 = 72; int UNIVERSE_73 = 73; int UNIVERSE_74 = 74; int UNIVERSE_75 = 75; int UNIVERSE_76 = 76; int UNIVERSE_77 = 77; int UNIVERSE_78 = 78; int UNIVERSE_79 = 79; int UNIVERSE_80 = 80;

public final OutputGroup[] buildOutputGroups() {
  return new OutputGroup[] {

    /**
     * Vip Booth
     *------------------------------------------------------------------------------------*/
    // new OutputGroup(UNIVERSE_1)
    //   .addPoints(model.splicePoints("vip-lounge-strip17", 0, 72))
    //   .addPoints(model.splicePoints("vip-lounge-strip7",  0, 71), OutputGroup.REVERSE)
    //   .addPoints(model.splicePoints("vip-lounge-strip10", 41, 8), OutputGroup.REVERSE),

    // new OutputGroup(UNIVERSE_2).addDeadPoints(3)
    //   .addPoints(model.splicePoints("vip-lounge-strip10", 0,  41), OutputGroup.REVERSE)
    //   .addPoints(model.splicePoints("vip-lounge-strip5",  0, 105))
    //   .addDeadPoints(2),

    // new OutputGroup(UNIVERSE_3)
    //   .addPoints(model.splicePoints("vip-lounge-strip5", 104, 17), OutputGroup.REVERSE),

    // new OutputGroup(UNIVERSE_4)
    //   .addPoints(model.splicePoints("vip-lounge-strip8", 0, 125), OutputGroup.REVERSE)
    //   .addPoints(model.splicePoints("vip-lounge-strip18", 47, 26), OutputGroup.REVERSE),

    // new OutputGroup(UNIVERSE_6).addDeadPoints(1)
    //   .addPoints(model.splicePoints("vip-lounge-strip18", 0, 48), OutputGroup.REVERSE)
    //   .addPoints(model.splicePoints("vip-lounge-strip6", 0, 66))
    //   .addPoints(model.splicePoints("vip-lounge-strip14", 0, 35)),

    // new OutputGroup(UNIVERSE_9)
    //   .addPoints(model.splicePoints("vip-lounge-strip3", 0, 92), OutputGroup.REVERSE)
    //   .addPoints(model.splicePoints("vip-lounge-strip9", 0, 50), OutputGroup.REVERSE)
    //   .addPoints(model.splicePoints("vip-lounge-strip1", 0, 10)),

    // new OutputGroup(UNIVERSE_10).addDeadPoints(1)
    //   .addPoints(model.splicePoints("vip-lounge-strip1", 10, 34))
    //   .addPoints(model.splicePoints("vip-lounge-strip15", 0, 69)),

    // new OutputGroup(UNIVERSE_11).addDeadPoints(2)
    //   .addPoints(model.splicePoints("vip-lounge-strip4", 0, 44), OutputGroup.REVERSE),

    // new OutputGroup(UNIVERSE_64)
    //   .addPoints(model.splicePoints("vip-lounge-strip16", 0, 67), OutputGroup.REVERSE)
    //   .addPoints(model.splicePoints("vip-lounge-strip2", 0, 84))
    //   .addDeadPoints(2),

    // new OutputGroup(UNIVERSE_65).addDeadPoints(1)
    //   .addPoints(model.splicePoints("vip-lounge-strip2", 83, 57))
    //   .addDeadPoints(1)
    //   .addPoints(model.splicePoints("vip-lounge-strip11", 0, 39))
    //   .addPoints(model.splicePoints("vip-lounge-strip12", 0, 3))
    //   .addPoints(model.splicePoints("vip-lounge-strip13", 0, 10))
    //   .addPoints(model.splicePoints("vip-lounge-strip4", 55, 40), OutputGroup.REVERSE),

    // new OutputGroup(UNIVERSE_72).addDeadPoints(1)
    //   .addPoints(model.splicePoints("vip-lounge-strip14", 37, 12)),

    /**
     * Long Skinny
     *------------------------------------------------------------------------------------*/

    new OutputGroup(UNIVERSE_14)
      .addPoints(model.splicePoints("long-skinny-strip6", 0, 42))
      .addPoints(model.splicePoints("long-skinny-strip9", 0, 17), OutputGroup.REVERSE)
      .addPoints(model.splicePoints("long-skinny-strip1", 0, 90)),

    new OutputGroup(UNIVERSE_15)
      .addPoints(model.splicePoints("long-skinny-strip7", 0, 144), OutputGroup.REVERSE)
      .addPoints(model.splicePoints("long-skinny-strip12", 21, 12), OutputGroup.REVERSE),

    new OutputGroup(UNIVERSE_17)
      .addPoints(model.splicePoints("long-skinny-strip2", 0, 121))
      .addPoints(model.splicePoints("long-skinny-strip11", 0, 28)),

    new OutputGroup(UNIVERSE_20)
      .addPoints(model.splicePoints("long-skinny-strip12", 0, 23))
      .addPoints(model.splicePoints("long-skinny-strip3", 0, 84))
      .addPoints(model.splicePoints("long-skinny-strip14", 0, 33)),

    new OutputGroup(UNIVERSE_27)
      .addPoints(model.splicePoints("long-skinny-strip1", 90, 7))
      .addPoints(model.splicePoints("long-skinny-strip10", 0, 32))
      .addPoints(model.splicePoints("long-skinny-strip5", 43, 85), OutputGroup.REVERSE),

  };
}

public class OutputGroup {

  public static final boolean REVERSE = true;

  private final int MAX_NUMBER_LEDS_PER_UNVERSE = 170;

  private final List<LXPoint> points = new ArrayList<LXPoint>();

  public final String ipAddress;
  public final int universe;

  public OutputGroup(int universe) {
    int ipIndex = (int)Math.ceil((float)universe / (float)IP_ADDRESSES.length);
    this.ipAddress = IP_ADDRESSES[ipIndex];
    this.universe = universe;
    println("Creating an OutputGroup with IP Address: [" + ipAddress + "] and universe: [" + universe + "]");
  }

  public List<LXPoint> getPoints() {
    return points;
  }

  public int[] getIndices() {
    int[] indices = new int[points.size()];

    for (int i = 0; i < points.size(); i++) {
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