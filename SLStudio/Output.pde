//import org.timothyb89.lifx.net.BroadcastListener;
//import java.math.BigInteger;

  /*
 *     DOUBLE BLACK DIAMOND        DOUBLE BLACK DIAMOND
 *
 *         //\\   //\\                 //\\   //\\
 *        ///\\\ ///\\\               ///\\\ ///\\\
 *        \\\/// \\\///               \\\/// \\\///
 *         \\//   \\//                 \\//   \\//
 *
 *        EXPERTS ONLY!!              EXPERTS ONLY!!
 */

public static ListenableList<SLController> controllers = new ListenableList<SLController>();

void setupOutputs(final LX lx) {
  networkMonitor.networkDevices.addListener(new ListListener<NetworkDevice>() {
    public void itemAdded(int index, NetworkDevice device) {
      String macAddr = NetworkUtils.macAddrToString(device.macAddress);
      String physid = macToPhysid.get(macAddr);
      if (physid == null) {
        physid = macAddr;
        println("WARNING: MAC address not in physid_to_mac.json: " + macAddr);
      }
      final SLController controller = new SLController(lx, device, physid);
      controllers.add(index, controller);
      dispatcher.dispatchEngine(new Runnable() {
        public void run() {
          lx.addOutput(controller);
        }
      });
      //controller.enabled.setValue(false);
    }
    public void itemRemoved(int index, NetworkDevice device) {
      final SLController controller = controllers.remove(index);
      dispatcher.dispatchEngine(new Runnable() {
        public void run() {
          //lx.removeOutput(controller);
        }
      });
    }
  });

  lx.addOutput(new SLController(lx, "10.200.1.255"));
  //lx.addOutput(new LIFXOutput());

  lx.addOutput(new Pixlite(lx, "10.1.10.255"));
}

/*
 * Pixlite
 *---------------------------------------------------------------------------*/
public class Pixlite extends LXOutputGroup {

  private final int NUM_OUTPUTS = 16;

  public final String ipAddress;

  public Pixlite(LX lx, String ipAddress) {
    super(lx, "pixlite: " + ipAddress);
    this.ipAddress = ipAddress;

    try {
      for (int i = 0; i < NUM_OUTPUTS; i++) {
        addChild(new PixliteOutput(lx, ipAddress, i, new PointsGrouping()
          .addPoints(model.strips.get(0).points)
        ));
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}

/*
 * Pixlite Output
 *---------------------------------------------------------------------------*/
 private class PixliteOutput extends LXDatagramOutput {
   private final int MAX_NUM_POINTS_PER_UNIVERSE = 170;
   private final int outputIndex;
   private final int firstUniverseOnOutput;

   public PixliteOutput(LX lx, String ipAddress, int outputIndex, PointsGrouping pointsGrouping) throws SocketException {
     super(lx);
     this.outputIndex = outputIndex;
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

/*
 * Artnet Datagram
 *---------------------------------------------------------------------------*/
public class ArtNetDatagram extends LXDatagram {

  private final static int DEFAULT_UNIVERSE = 0;
  private final static int ARTNET_HEADER_LENGTH = 18;
  private final static int ARTNET_PORT = 6454;
  private final static int SEQUENCE_INDEX = 12;

  private final int[] pointIndices;
  private boolean sequenceEnabled = false;
  private byte sequence = 1;

  public ArtNetDatagram(String ipAddress, int[] indices, int universeNumber) {
    this(ipAddress, indices, 3 * indices.length, universeNumber);
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
    copyPointsGamma(colors, this.pointIndices, ARTNET_HEADER_LENGTH);

    if (this.sequenceEnabled) {
      if (++this.sequence == 0) {
        ++this.sequence;
      }
      this.buffer[SEQUENCE_INDEX] = this.sequence;
    }

    // We need to slow down the speed at which we send the packets so that we don't overload our switches. 3us seems to
    // be about right - Yona
    busySleep(3000);
  }

  LXDatagram copyPointsGamma(int[] colors, int[] pointIndices, int offset) {
    int i = offset;
    int[] byteOffset = BYTE_ORDERING[this.byteOrder.ordinal()];
    for (int index : pointIndices) {
      int colorValue = (index >= 0) ? colors[index] : 0;
      this.buffer[i + byteOffset[0]] = (byte) SLStudio.redGamma[((colorValue >> 16) & 0xff)]; // R
      this.buffer[i + byteOffset[1]] = (byte) SLStudio.greenGamma[((colorValue >> 8) & 0xff)]; // G
      this.buffer[i + byteOffset[2]] = (byte) SLStudio.blueGamma[(colorValue & 0xff)]; // B
      i += 3;
    }
    return this;
  }

  public void busySleep(long nanos) {
    long elapsed;
    final long startTime = System.nanoTime();
    do {
      elapsed = System.nanoTime() - startTime;
    } while (elapsed < nanos);
  }
}


/*
 * Points Grouping
 *---------------------------------------------------------------------------*/
public class PointsGrouping {

  public static final boolean REVERSE = true;

  private final int MAX_NUMBER_LEDS_PER_UNVERSE = 170;

  private final List<LXPoint> points = new ArrayList<LXPoint>();

  public List<LXPoint> getPoints() {
    return points;
  }

  public LXPoint getPoint(int index) {
    return points.get(index);
  }

  public int[] getIndices() {
    int[] indices = new int[size()];

    for (int i = 0; i < size(); i++) {
      indices[i] = getPoint(i).index;
    }
    return indices;
  }

  public int size() {
    return points.size();
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

  public PointsGrouping addPoints(LXPoint[] pointsToAdd, boolean reverseOrdering) {
    if (reverseOrdering) {
      Collections.reverse(Arrays.asList(pointsToAdd));
    }
    for (LXPoint p : pointsToAdd) {
      this.points.add(p);
    }
    return this;
  }
}

/*
 * Output Component
 *---------------------------------------------------------------------------*/
public final class OutputControl extends LXComponent {
  public final BooleanParameter enabled;

  public final ControllerResetModule controllerResetModule = new ControllerResetModule(lx);

  public final BooleanParameter broadcastPacket = new BooleanParameter("Broadcast packet enabled", false);
  public final BooleanParameter testBroadcast   = new BooleanParameter("Test broadcast enabled", false);

  public OutputControl(LX lx) {
    super(lx, "Output Control");
    this.enabled = lx.engine.output.enabled;

    addParameter(testBroadcast);
    
    enabled.addListener(new LXParameterListener() {
      public void onParameterChanged(LXParameter parameter) {
        for (SLController c : controllers)
          c.enabled.setValue(((BooleanParameter)parameter).isOn());
      };
    });
  }
}

/*
 * Controller
 *---------------------------------------------------------------------------*/
class SLController extends LXOutput {
  Socket        socket;
  DatagramSocket dsocket;
  OutputStream    output;
  NetworkDevice networkDevice;
  String        cubeId;
  InetAddress   host;
  boolean       isBroadcast;

  final int[] STRIP_ORD = new int[] {
    6, 7, 8,   // white
    9, 10, 11, // red
    0, 1, 2,   // green
    3, 4, 5    // blue
  };

  static final int HEADER_LENGTH = 4;
  static final int BYTES_PER_PIXEL = 3;

  final int numStrips = STRIP_ORD.length;
  int numPixels;
  int contentSizeBytes;
  int packetSizeBytes;
  byte[] packetData;

  SLController(LX lx, NetworkDevice device, String cubeId) {
    this(lx, device, device.ipAddress, cubeId, false);
  }

  SLController(LX lx, String _host, String _cubeId) {
    this(lx, _host, _cubeId, false);
  }

  SLController(LX lx, String _host) {
    this(lx, _host, "", true);
  }

  private SLController(LX lx, String host, String cubeId, boolean isBroadcast) {
    this(lx, null, NetworkUtils.ipAddrToInetAddr(host), cubeId, isBroadcast);
  }

  private SLController(LX lx, NetworkDevice networkDevice, InetAddress host, String cubeId, boolean isBroadcast) {
    super(lx);

    this.networkDevice = networkDevice;
    this.host = host;
    this.cubeId = cubeId;
    this.isBroadcast = isBroadcast;

    enabled.setValue(true);
  }

  void initPacketData(int numPixels) {
    this.numPixels = numPixels;
    contentSizeBytes = BYTES_PER_PIXEL * numPixels;
    packetSizeBytes = HEADER_LENGTH + contentSizeBytes; // add header length
    packetData = new byte[packetSizeBytes];

    setHeader();
  }

  void setHeader() {
    packetData[0] = 0;  // Channel
    packetData[1] = 0;  // Command (Set pixel colors)
    // indices 2,3 = high byte, low byte
    // 3 bytes * 180 pixels = 540 bytes = 0x021C
    packetData[2] = (byte)((contentSizeBytes >> 8) & 0xFF);
    packetData[3] = (byte)((contentSizeBytes >> 0) & 0xFF);
  }

  void setPixel(int number, color c) {
    //println("number: "+number);
    int offset = 4 + number * 3;

    // Extract individual colors
      int r = c >> 16 & 0xFF;
      int g = c >> 8 & 0xFF;
      int b = c & 0xFF;

      // Repack gamma corrected colors
    packetData[offset + 0] = (byte) redGamma[r];
    packetData[offset + 1] = (byte) greenGamma[g];
    packetData[offset + 2] = (byte) blueGamma[b];
  }

  void onSend(int[] colors) {
    if (isBroadcast != outputControl.broadcastPacket.isOn()) return;

    // Create data socket connection if needed
    if (dsocket == null) {
      try {
        dsocket = new DatagramSocket();
        dsocket.connect(new InetSocketAddress(host, 7890));
        //socket.setTcpNoDelay(true);
        // output = socket.getOutputStream();
      }
      catch (ConnectException e) {  dispose();  }
      catch (IOException      e) {  dispose();  }
      if (dsocket == null) return;
    }

    // Find the Cube we're outputting to
    // If we're on broadcast, use cube 0 for all cubes, even
    // if that cube isn't modelled yet
    // Use the mac address to find the cube if we have it
    // Otherwise use the cube id
    Cube cube = null;
    if ((outputControl.testBroadcast.isOn() || isBroadcast) && model.cubes.size() > 0) {
      cube = model.cubes.get(0);
    } else {
      for (Cube c : model.cubes) {
        if (c.id != null && c.id.equals(cubeId)) {
          cube = c;
          break;
        }
      }
    }

    // Initialize packet data base on cube type.
    // If we don't know the cube type, default to
    // using the cube type with the most pixels
    Cube.Type cubeType = cube != null ? cube.type : Cube.CUBE_TYPE_WITH_MOST_PIXELS;
    int numPixels = cubeType.POINTS_PER_CUBE;
    if (packetData == null || packetData.length != numPixels) {
      initPacketData(numPixels);
    }

    // Fill the datagram with pixel data
    // Fill with all black if we don't have cube data
    if (cube != null) {
      for (int stripNum = 0; stripNum < numStrips; stripNum++) {
        int stripId = STRIP_ORD[stripNum];
        Strip strip = cube.strips.get(stripId);

        for (int i = 0; i < strip.metrics.numPoints; i++) {
          LXPoint point = strip.getPoints().get(i);
          setPixel(stripNum * strip.metrics.numPoints + i, colors[point.index]);
        }
      }
    } else {
      for (int i = 0; i < numPixels; i++) {
        setPixel(i, LXColor.BLACK);
      }
    }

    // Mapping Mode: manually get color to animate "unmapped" fixtures that are not network
    // TODO: refactor here
    if (mappingMode.enabled.isOn() && !mappingMode.isFixtureMapped(cubeId)) {
      if (mappingMode.inUnMappedMode()) {
        if (mappingMode.inDisplayAllMode()) {
          color col = mappingMode.getUnMappedColor();

          for (int i = 0; i < numPixels; i++)
            setPixel(i, col);
        } else {
          if (mappingMode.isSelectedUnMappedFixture(cubeId)) {
            color col = mappingMode.getUnMappedColor();

            for (int i = 0; i < numPixels; i++)
              setPixel(i, col);
          } else {
            for (int i = 0; i < numPixels; i++)
              setPixel(i, (i % 2 == 0) ? LXColor.scaleBrightness(LXColor.RED, 0.2) : LXColor.BLACK);
          }
        }
      } else {
        for (int i = 0; i < numPixels; i++)
          setPixel(i, (i % 2 == 0) ? LXColor.scaleBrightness(LXColor.RED, 0.2) : LXColor.BLACK);
      }
    }

    // Send the cube data to the cube. yay!
    try { 
      //println("packetSizeBytes: "+packetSizeBytes);
      dsocket.send(new java.net.DatagramPacket(packetData,packetSizeBytes));} 
    catch (Exception e) {dispose();}
  }  

  void dispose() {
    if (dsocket != null)  println("Disconnected from OPC server");
    println("Failed to connect to OPC server " + host);
    socket = null;
    dsocket = null;
  }
}

/*
 * UIOutput Window
 *---------------------------------------------------------------------------*/
class UIOutputs extends UICollapsibleSection {
    UIOutputs(LX lx, UI ui, float x, float y, float w) {
        super(ui, x, y, w, 124);

        final SortedSet<SLController> sortedControllers = new TreeSet<SLController>(new Comparator<SLController>() {
            int compare(SLController o1, SLController o2) {
                try {
                    return Integer.parseInt(o1.cubeId) - Integer.parseInt(o2.cubeId);
                } catch (NumberFormatException e) {
                    return o1.cubeId.compareTo(o2.cubeId);
                }
            }
        });

        final List<UIItemList.Item> items = new ArrayList<UIItemList.Item>();
        for (SLController c : controllers) { sortedControllers.add(c); }
        for (SLController c : sortedControllers) { items.add(new ControllerItem(c)); }
        final UIItemList.ScrollList outputList = new UIItemList.ScrollList(ui, 0, 22, w-8, 78);

        outputList.setItems(items).setSingleClickActivate(true);
        outputList.addToContainer(this);

        setTitle(items.size());

        controllers.addListener(new ListListener<SLController>() {
          void itemAdded(final int index, final SLController c) {
            dispatcher.dispatchUi(new Runnable() {
                public void run() {
                    if (c.networkDevice != null) c.networkDevice.version.addListener(deviceVersionListener);
                    sortedControllers.add(c);
                    items.clear();
                        for (SLController c : sortedControllers) { items.add(new ControllerItem(c)); }
                    outputList.setItems(items);
                    setTitle(items.size());
                    redraw();
                }
            });
          }
          void itemRemoved(final int index, final SLController c) {
            dispatcher.dispatchUi(new Runnable() {
                public void run() {
                    if (c.networkDevice != null) c.networkDevice.version.removeListener(deviceVersionListener);
                    sortedControllers.remove(c);
                    items.clear();
                        for (SLController c : sortedControllers) { items.add(new ControllerItem(c)); }
                    outputList.setItems(items);
                    setTitle(items.size());
                    redraw();
                }
            });
          }
        });

        UIButton testOutput = new UIButton(0, 0, w/2 - 8, 19) {
          @Override
          public void onToggle(boolean isOn) { }
        }.setLabel("Test Broadcast").setParameter(outputControl.testBroadcast);
        testOutput.addToContainer(this);

        UIButton resetCubes = new UIButton(w/2-6, 0, w/2 - 1, 19) {
          @Override
          public void onToggle(boolean isOn) { 
            outputControl.controllerResetModule.enabled.setValue(isOn);
          }
        }.setMomentary(true).setLabel("Reset Controllers");
        resetCubes.addToContainer(this);

        addTopLevelComponent(new UIButton(4, 4, 12, 12) {}
          .setParameter(outputControl.enabled).setBorderRounding(4));

        outputControl.enabled.addListener(new LXParameterListener() {
          public void onParameterChanged(LXParameter parameter) {
            redraw();
          };
        });
    }

    private final IntListener deviceVersionListener = new IntListener() {
        public void onChange(int version) {
            dispatcher.dispatchUi(new Runnable() {
            public void run() { redraw(); }
            });
        }
    };

    private void setTitle(int count) {
        setTitle("OUTPUT (" + count + ")");
        setTitleX(20);
    }

    class ControllerItem extends UIItemList.AbstractItem {
        final SLController controller;

        ControllerItem(SLController _controller) {
          this.controller = _controller;
          controller.enabled.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter parameter) { redraw(); }
          });
        }

        String getLabel() {
            if (controller.networkDevice != null && controller.networkDevice.version.get() != -1) {
                return controller.cubeId + " (v" + controller.networkDevice.version + ")";
            } else {
                return controller.cubeId;
            }
        }

        boolean isSelected() { 
            return controller.enabled.isOn();
        }

        @Override
        boolean isActive() {
            return controller.enabled.isOn();
        }

        @Override
        public int getActiveColor(UI ui) {
            return isSelected() ? ui.theme.getPrimaryColor() : ui.theme.getSecondaryColor();
        }

        @Override
        public void onActivate() {
            if (!outputControl.enabled.getValueb())
                return;
            controller.enabled.toggle();
        }

        // @Override
        // public void onDeactivate() {
        //     println("onDeactivate");
        //     controller.enabled.setValue(false);
        // }
    }
}

/*
 * Gamma Correction
 *---------------------------------------------------------------------------*/
static final int redGamma[] = new int[256];
static final int greenGamma[] = new int[256];
static final int blueGamma[] = new int[256];

final float[][] gammaSet = {
  { 2, 2.1, 2.8 },
  { 2, 2.2, 2.8 },
};

final DiscreteParameter gammaSetIndex = new DiscreteParameter("GMA", gammaSet.length+1);
final BoundedParameter redGammaFactor = new BoundedParameter("RGMA", 2, 1, 4);
final BoundedParameter greenGammaFactor = new BoundedParameter("GGMA", 2.2, 1, 4);
final BoundedParameter blueGammaFactor = new BoundedParameter("BGMA", 2.8, 1, 4);

void setupGammaCorrection() {
  final float redGammaOrig = redGammaFactor.getValuef();
  final float greenGammaOrig = greenGammaFactor.getValuef();
  final float blueGammaOrig = blueGammaFactor.getValuef();
  gammaSetIndex.addListener(new LXParameterListener() {
    public void onParameterChanged(LXParameter parameter) {
      if (gammaSetIndex.getValuei() == 0) {
        redGammaFactor.reset(redGammaOrig);
        greenGammaFactor.reset(greenGammaOrig);
        blueGammaFactor.reset(blueGammaOrig);
      } else {
        redGammaFactor.reset(gammaSet[gammaSetIndex.getValuei()-1][0]);
        greenGammaFactor.reset(gammaSet[gammaSetIndex.getValuei()-1][1]);
        blueGammaFactor.reset(gammaSet[gammaSetIndex.getValuei()-1][2]);
      }
    }
  });
  redGammaFactor.addListener(new LXParameterListener() {
    public void onParameterChanged(LXParameter parameter) {
      buildGammaCorrection(redGamma, parameter.getValuef());
    }
  });
  buildGammaCorrection(redGamma, redGammaFactor.getValuef());
  greenGammaFactor.addListener(new LXParameterListener() {
    public void onParameterChanged(LXParameter parameter) {
      buildGammaCorrection(greenGamma, parameter.getValuef());
    }
  });
  buildGammaCorrection(greenGamma, greenGammaFactor.getValuef());
  blueGammaFactor.addListener(new LXParameterListener() {
    public void onParameterChanged(LXParameter parameter) {
      buildGammaCorrection(blueGamma, parameter.getValuef());
    }
  });
  buildGammaCorrection(blueGamma, blueGammaFactor.getValuef());
}

void buildGammaCorrection(int[] gammaTable, float gammaCorrection) {
  for (int i = 0; i < 256; i++) {
    gammaTable[i] = (int)(pow(1.0 * i / 255, gammaCorrection) * 255 + 0.5);
  }
}