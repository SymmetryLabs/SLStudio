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
      // final SLController controller = controllers.remove(index);
      // dispatcher.dispatchEngine(new Runnable() {
      //   public void run() {
      //     lx.removeOutput(controller);
      //   }
      // });
    }
  });

  lx.addOutput(new SLController(lx, "10.200.1.255")); // 192.168.1.110?
  //lx.addOutput(new LIFXOutput());


  try {
    final String PIXLITE1_IP = "192.168.1.110";

   /** 
    * PixLite 1
    *--------------------------------------------------------------------------*/
    final LXDatagramOutput pixlite1 = new LXDatagramOutput(lx);
    //                                  Ring ID,    IP Addresss,  Output 
    pixlite1.addDatagrams(new RingOutput("p1r1",    PIXLITE1_IP,     1).getDatagrams());
    pixlite1.addDatagrams(new RingOutput("p1r2",    PIXLITE1_IP,     2).getDatagrams());
    pixlite1.addDatagrams(new RingOutput("p1r3",    PIXLITE1_IP,     3).getDatagrams());

    // Add the pixlites
    lx.addOutput(pixlite1);

  } catch (SocketException e) {
    e.printStackTrace();
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

public class RingOutput {

  private Ring ring;
  private LXDatagram[] datagrams = new LXDatagram[2];

  public RingOutput(String id, String ipAddress, int output) {
    this.ring = model.getRingById(id);

    if (ring == null) {
      println("TRYING TO OUTPUT TO A RING ID THAT IS NOT IN MODEL (ID: " + id + ")");
    } else {

      int counter = 0;
      int[] indices = new int[ring.points.length];
      for (int i = 0; i < ring.points.length; i++) {
        indices[counter++] = ring.points[i].index;
      }
      
      // create array of indices for each datagram/universe
      int counter1 = 0;
      int[] firstIndices = new int[indices.length > 170 ? 170 : indices.length];
      for (int i = 0; i < firstIndices.length; i++) {
        firstIndices[i] = indices[counter1++];
      }
      int[] secondIndices = new int[(ring.points.length) - firstIndices.length];
      for (int i = 0; i < secondIndices.length; i++) {
        secondIndices[i] = indices[counter1++];
      }

      println("--------------------------------------------------------------");
      println("Ring: " + id + ", Pixlite: " + ipAddress + ", Output: " + output);
      println("Ring number of points: " + ring.points.length);
      println("First universe number of indices: " + firstIndices.length);
      println("Second universe number of indices: " + secondIndices.length);
      println(" ");

      this.datagrams[0] = new ArtNetDatagram(ipAddress, firstIndices, output*2-2);
      this.datagrams[1] = new ArtNetDatagram(ipAddress, secondIndices, output*2-1);
    }
  }

  public LXDatagram[] getDatagrams() {
    return datagrams;
  }
}

public class BarOutput {

  private Bar bar;
  private LXDatagram[] datagrams = new LXDatagram[2];

  public BarOutput(String id, String ipAddress, int output) {
    this.bar = model.getBarById(id);

    if (bar == null) {
      println("TRYING TO OUTPUT TO A BAR ID THAT IS NOT IN MODEL (ID: " + id + ")");
    } else {

      // build array of indices (note that we output to 2 strips by going up and down the one strip in model)
      int counter = 0;
      int[] indices = new int[bar.points.length*2];
      for (int i = 0; i < bar.points.length; i++) {
        indices[counter++] = bar.points[i].index;
      }
      for (int i = bar.points.length-1; i > -1; i--) {
        indices[counter++] = bar.points[i].index;
      }
      
      // create array of indices for each datagram/universe
      int counter1 = 0;
      int[] firstIndices = new int[indices.length > 170 ? 170 : indices.length];
      for (int i = 0; i < firstIndices.length; i++) {
        firstIndices[i] = indices[counter1++];
      }
      int[] secondIndices = new int[(bar.points.length*2) - firstIndices.length];
      for (int i = 0; i < secondIndices.length; i++) {
        secondIndices[i] = indices[counter1++];
      }

      println("--------------------------------------------------------------");
      println("Bar: " + id + ", Pixlite: " + ipAddress + ", Output: " + output);
      println("Bar number of points: " + bar.points.length);
      println("First universe number of indices: " + firstIndices.length);
      println("Second universe number of indices: " + secondIndices.length);
      println(" ");

      this.datagrams[0] = new ArtNetDatagram(ipAddress, firstIndices, output*2-2);
      this.datagrams[1] = new ArtNetDatagram(ipAddress, secondIndices, output*2-1);
    }
  }

  public LXDatagram[] getDatagrams() {
    return datagrams;
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
        System.out.println("COULD NOT FIND PIXLITE HOST WITH IP: " + ipAddress);
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

    // Ensure zero rest of buffer
    for (int i = ARTNET_HEADER_LENGTH; i < this.buffer.length; ++i) {
     this.buffer[i] = 0;
    }
  }

  /**
   * Set whether to increment and send sequence numbers
   *
   * @param sequenceEnabled true if sequence should be incremented and transmitted
   * @return this
   */
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

/*
 * Controller
 *---------------------------------------------------------------------------*/
class SLController extends LXOutput {
  Socket        socket;
  DatagramSocket dsocket;
  OutputStream    output;
  NetworkDevice networkDevice;
  String        controllerId;
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

  SLController(LX lx, NetworkDevice device, String controllerId) {
    this(lx, device, device.ipAddress, controllerId, false);
  }

  SLController(LX lx, String _host, String _controllerId) {
    this(lx, _host, _controllerId, false);
  }

  SLController(LX lx, String _host) {
    this(lx, _host, "", true);
  }

  private SLController(LX lx, String host, String controllerId, boolean isBroadcast) {
    this(lx, null, NetworkUtils.ipAddrToInetAddr(host), controllerId, isBroadcast);
  }

  private SLController(LX lx, NetworkDevice networkDevice, InetAddress host, String controllerId, boolean isBroadcast) {
    super(lx);

    this.networkDevice = networkDevice;
    this.host = host;
    this.controllerId = controllerId;
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
    Bar bar = null;
    if ((outputControl.testBroadcast.isOn() || isBroadcast) && model.cubes.size() > 0) {
      cube = model.cubes.get(0);
    } else {
      for (Cube c : model.cubes) {
        if (c.id != null && c.id.equals(controllerId)) {
          cube = c;
          break;
        }
      }
      for (Bar b : model.bars) {
        if (b.id != null && b.id.equals(controllerId)) {
          bar = b;
          break;
        }
      }
    }

    if (cube != null) {
      Cube.Type cubeType = cube != null ? cube.type : Cube.CUBE_TYPE_WITH_MOST_PIXELS;
      int numPixels = cubeType.POINTS_PER_CUBE;
      if (packetData == null || packetData.length != numPixels) {
        initPacketData(numPixels);
      }

      for (int stripNum = 0; stripNum < numStrips; stripNum++) {
        int stripId = STRIP_ORD[stripNum];
        Strip strip = cube.strips.get(stripId);

        for (int i = 0; i < strip.metrics.numPoints; i++) {
          LXPoint point = strip.getPoints().get(i);
          setPixel(stripNum * strip.metrics.numPoints + i, colors[point.index]);
        }
      }
    }

    if (bar != null) {
      int numPixels = bar.points.length;
      if (packetData == null || packetData.length != numPixels) {
        initPacketData(numPixels);
      }

      int i = 0;
      for (LXPoint p : bar.points) {
        setPixel(i++, colors[p.index]);
      }
    }

    if (cube == null && bar == null) {
      for (int i = 0; i < numPixels; i++) {
        setPixel(i, LXColor.BLACK);
      }
    }

    // Mapping Mode: manually get color to animate "unmapped" fixtures that are not network
    // TODO: refactor here
    if (mappingMode.enabled.isOn() && !mappingMode.isFixtureMapped(controllerId)) {
      if (mappingMode.inUnMappedMode()) {
        if (mappingMode.inDisplayAllMode()) {
          color col = mappingMode.getUnMappedColor();

          for (int i = 0; i < numPixels; i++)
            setPixel(i, col);
        } else {
          if (mappingMode.isSelectedUnMappedFixture(controllerId)) {
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
                    return Integer.parseInt(o1.controllerId) - Integer.parseInt(o2.controllerId);
                } catch (NumberFormatException e) {
                    return o1.controllerId.compareTo(o2.controllerId);
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
                return controller.controllerId + " (v" + controller.networkDevice.version + ")";
            } else {
                return controller.controllerId;
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