Pixlite[] setupPixlites(LX lx) {
  // 10.200.1.26:4 -> 25:8 <-FIX

  return new Pixlite[] {
    // Sun 1 (One Third)
    //new Pixlite(lx, "10.200.1.39", model.getSliceById("sun1_top_back")),
    //new Pixlite(lx, "10.200.1.40", model.getSliceById("sun1_top_front")),

    // Sun 3 (One Third)
    // new Pixlite(lx, "10.200.1.43", model.getSliceById("sun3_top_back")),
    // new Pixlite(lx, "10.200.1.44", model.getSliceById("sun3_top_front")),

    // Sun 4 (One Half)
    // new Pixlite(lx, "10.200.1.35", model.getSliceById("sun4_top_front")),
    // new Pixlite(lx, "10.200.1.36", model.getSliceById("sun4_top_back")),

    // Sun 5 (One Third)
    // new Pixlite(lx, "10.200.1.38", model.getSliceById("sun5_top_back")), // locked
    // new Pixlite(lx, "10.200.1.37", model.getSliceById("sun5_top_front")), // locked

    // Sun 6 (Two Thirds)
    // new Pixlite(lx, "10.200.1.24", model.getSliceById("sun6_top_front")), // locked
    // new Pixlite(lx, "10.200.1.23", model.getSliceById("sun6_bottom_front")), // locked
    // new Pixlite(lx, "10.200.1.26", model.getSliceById("sun6_top_back")), // locked
    // new Pixlite(lx, "10.200.1.25", model.getSliceById("sun6_bottom_back")), // locked

    // // Sun 8 (Two Thirds)
    // new Pixlite(lx, "10.200.1.27", model.getSliceById("sun8_top_front")), // locked
    // new Pixlite(lx, "10.200.1.28", model.getSliceById("sun8_bottom_front")), // locked
    // new Pixlite(lx, "10.200.1.29", model.getSliceById("sun8_top_back")), // locked
    // new Pixlite(lx, "10.200.1.30", model.getSliceById("sun8_bottom_back")), // locked
  };
}
 
public class Pixlite extends LXOutputGroup {
  private Slice slice;
  public final String ipAddress;

  public Pixlite(LX lx, String ipAddress, Slice slice) {
    super(lx);
    this.ipAddress = ipAddress;
    this.slice = slice;

    try {
      // Sun 1
      if(slice.id.equals("sun1_top_front")) {
        new Sun1FrontPixliteConfig(lx, slice, ipAddress, this);
      }
      if(slice.id.equals("sun1_top_back")) {
        new Sun1BackPixliteConfig(lx, slice, ipAddress, this);
      }

      // Sun 3
      if(slice.id.equals("sun3_top_front")) {
        new Sun3FrontTopPixliteConfig(lx, slice, ipAddress, this);
      }
      if(slice.id.equals("sun3_top_back")) {
        new Sun3BackTopPixliteConfig(lx, slice, ipAddress, this);
      }

      // Sun 4
      if(slice.id.equals("sun4_top_back")) {
        new Sun4BackPixliteConfig(lx, slice, ipAddress, this);
      }

      // Sun 5
      if(slice.id.equals("sun5_top_front")) {
        new Sun5FrontTopPixliteConfig(lx, slice, ipAddress, this);
      }
      if(slice.id.equals("sun5_top_back")) {
        new Sun5BackTopPixliteConfig(lx, slice, ipAddress, this);
      }

      // Sun 6
      if(slice.id.equals("sun6_top_front")) {
        new Sun6FrontTopPixliteConfig(lx, slice, ipAddress, this);
      }
      if(slice.id.equals("sun6_bottom_front")) {
        new Sun6FrontBottomPixliteConfig(lx, slice, ipAddress, this);
      }
      if(slice.id.equals("sun6_top_back")) {
        new Sun6BackTopPixliteConfig(lx, slice, ipAddress, this);
      }
      if(slice.id.equals("sun6_bottom_back")) {
        new Sun6BackBottomPixliteConfig(lx, slice, ipAddress, this);
      }

      // Sun 6
      if(slice.id.equals("sun8_top_front")) {
        new Sun8FrontTopPixliteConfig(lx, slice, ipAddress, this);
      }
      if(slice.id.equals("sun8_bottom_front")) {
        new Sun8FrontBottomPixliteConfig(lx, slice, ipAddress, this);
      }
      if(slice.id.equals("sun8_top_back")) {
        new Sun8BackTopPixliteConfig(lx, slice, ipAddress, this);
      }
      if(slice.id.equals("sun8_bottom_back")) {
        new Sun8BackBottomPixliteConfig(lx, slice, ipAddress, this);
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}

public class PixliteOutput extends LXDatagramOutput {
  private final int MAX_NUM_POINTS_PER_UNIVERSE = 170;
  private final int outputIndex;
  private final int firstUniverseOnOutput;

  public PixliteOutput(LX lx, String ipAddress, PointsGrouping pointsGrouping) throws SocketException {
    super(lx);
    this.outputIndex = Integer.parseInt(pointsGrouping.id);
    this.firstUniverseOnOutput = outputIndex * 10;
    setupDatagrams(ipAddress, pointsGrouping);
  }

  private void setupDatagrams(String ipAddress, PointsGrouping pointsGrouping) {
    // the points for one pixlite output have to be spread across multiple universes
    int numPoints = pointsGrouping.size();
    int numUniverses = (numPoints / MAX_NUM_POINTS_PER_UNIVERSE) + 1;
    int counter = 0;

    for (int i = 0; i < numUniverses; i++) {
      int universe = firstUniverseOnOutput + i;
      int numIndices = ((i+1) * MAX_NUM_POINTS_PER_UNIVERSE) > numPoints ? (numPoints % MAX_NUM_POINTS_PER_UNIVERSE) : MAX_NUM_POINTS_PER_UNIVERSE;
      int[] indices = new int[numIndices];
      for (int i1 = 0; i1 < numIndices; i1++) {
        indices[i1] = pointsGrouping.getPoint(counter++).index;
      }
      addDatagram(new ArtNetDatagram(ipAddress, indices, universe-1));
    }
  }
}

public static class PointsGrouping {
  public final static boolean REVERSE_ORDERING = true;

  public enum Shift {
    LEFT, RIGHT
  };

  public String id;
  private final List<LXPoint> points = new ArrayList<LXPoint>();

  public PointsGrouping(String id) {
    this.id = id;
  }

  public List<LXPoint> getPoints() {
    return points;
  }

  public LXPoint getPoint(int i) {
    return points.get(i);
  }

  public int size() {
    return points.size();
  }

  public int[] getIndices() {
    int[] indices = new int[points.size()];

    for (int i = 0; i < points.size(); i++) {
      indices[i] = points.get(i).index;
    }
    return indices;
  }

  public PointsGrouping reversePoints() {
    Collections.reverse(Arrays.asList(points));
    return this;
  }

  public PointsGrouping addPoints(LXPoint[] pointsToAdd) {
    for (LXPoint p : pointsToAdd) {
      this.points.add(p);
    }
    return this;
  }

  public PointsGrouping addPoints(LXPoint[] pointsToAdd, PointsGrouping.Shift shift) {
    LXPoint[] localPointsToAdd = pointsToAdd.clone();
    LXPoint[] shiftedPoints = null;

    if (shift == PointsGrouping.Shift.LEFT) {
      shiftedPoints = new LXPoint[localPointsToAdd.length];

      for (int i = 0; i < shiftedPoints.length-1; i++) {
        shiftedPoints[i] = localPointsToAdd[i+1];
      }
      shiftedPoints[shiftedPoints.length-1] = localPointsToAdd[shiftedPoints.length-1];
    }
    if (shift == PointsGrouping.Shift.RIGHT) {
      shiftedPoints = new LXPoint[localPointsToAdd.length];
      shiftedPoints[0] = localPointsToAdd[0];

      for (int i = 0; i < shiftedPoints.length-1; i++) {
        shiftedPoints[i+1] = localPointsToAdd[i];
      }
    }

    addPoints(shiftedPoints);
    return this;
  }

  public PointsGrouping addPoints(LXPoint[] pointsToAdd, boolean reverseOrdering) {
    LXPoint[] localPointsToAdd = pointsToAdd.clone();

    if (reverseOrdering) {
      Collections.reverse(Arrays.asList(localPointsToAdd));
    }
    addPoints(localPointsToAdd);
    return this;
  }

  public PointsGrouping addPoints(LXPoint[] pointsToAdd, boolean reverseOrdering, PointsGrouping.Shift shift) {
    LXPoint[] localPointsToAdd = pointsToAdd.clone();
    LXPoint[] shiftedPoints = null;

    if (shift == PointsGrouping.Shift.RIGHT) {
      shiftedPoints = new LXPoint[localPointsToAdd.length];

      for (int i = 0; i < shiftedPoints.length-1; i++) {
        shiftedPoints[i] = localPointsToAdd[i+1];
      }
      shiftedPoints[shiftedPoints.length-1] = localPointsToAdd[shiftedPoints.length-1];
    }
    if (shift == PointsGrouping.Shift.LEFT) {
      shiftedPoints = new LXPoint[localPointsToAdd.length];
      shiftedPoints[0] = localPointsToAdd[0];

      for (int i = 0; i < shiftedPoints.length-1; i++) {
        shiftedPoints[i+1] = localPointsToAdd[i];
      }
    }

    if (reverseOrdering) {
      Collections.reverse(Arrays.asList(shiftedPoints));
    }

    addPoints(shiftedPoints);
    return this;
  }
}

public class ArtNetDatagram extends LXDatagram {

  private final static int DEFAULT_UNIVERSE = 0;
  private final static int ARTNET_HEADER_LENGTH = 18;
  private final static int ARTNET_PORT = 6454;
  private final static int SEQUENCE_INDEX = 12;

  private final int[] pointIndices;
  private boolean sequenceEnabled = false;
  private byte sequence = 1;

  public ArtNetDatagram(String ipAddress, int[] indices, int universeNumber) {
    this(ipAddress, indices, 3*indices.length, universeNumber);
  }

  public ArtNetDatagram(String ipAddress, int[] indices, int dataLength, int universeNumber) {
    super(ARTNET_HEADER_LENGTH + dataLength + (dataLength % 2));
    this.pointIndices = indices;

    try {
        setAddress(ipAddress);
        setPort(ARTNET_PORT);
    } catch (UnknownHostException e) {
        System.out.println("Pixlite with ip address (" + ipAddress + ") is not on the network.");
    }

    this.buffer[0] = 'A';
    this.buffer[1] = 'r';
    this.buffer[2] = 't';
    this.buffer[3] = '-';
    this.buffer[4] = 'N';
    this.buffer[5] = 'e';
    this.buffer[6] = 't';
    this.buffer[7] = 0;
    this.buffer[8] = 0x00; // ArtDMX opcode
    this.buffer[9] = 0x50; // ArtDMX opcode
    this.buffer[10] = 0; // Protcol version
    this.buffer[11] = 14; // Protcol version
    this.buffer[12] = 0; // Sequence
    this.buffer[13] = 0; // Physical
    this.buffer[14] = (byte) (universeNumber & 0xff); // Universe LSB
    this.buffer[15] = (byte) ((universeNumber >>> 8) & 0xff); // Universe MSB
    this.buffer[16] = (byte) ((dataLength >>> 8) & 0xff);
    this.buffer[17] = (byte) (dataLength & 0xff);

    for (int i = ARTNET_HEADER_LENGTH; i < this.buffer.length; ++i) {
     this.buffer[i] = 0;
    }
  }

  public ArtNetDatagram setSequenceEnabled(boolean sequenceEnabled) {
    this.sequenceEnabled = sequenceEnabled;
    return this;
  }

  @Override
  public void onSend(int[] colors) {
    copyPoints(colors, this.pointIndices, ARTNET_HEADER_LENGTH);

    if (this.sequenceEnabled) {
      if (++this.sequence == 0) {
        ++this.sequence;
      }
      this.buffer[SEQUENCE_INDEX] = this.sequence;
    }
  }
}