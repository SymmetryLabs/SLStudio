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

int nPointsPerPin = 252;

void buildOutputs(final LX lx) {
  networkMonitor.networkDevices.addListener(new ListListener<NetworkDevice>() {
    public void itemAdded(int index, NetworkDevice device) {
      String macAddr = NetworkUtils.macAddrToString(device.macAddress);
      String physid = macToPhysid.get(macAddr);
      if (physid == null) {
        physid = macAddr;
        println("WARNING: MAC address not in physid_to_mac.json: " + macAddr);
      }
      final Beagle beagle = new Beagle(lx, device, physid);
      beagles.add(index, beagle);
      dispatcher.dispatchEngine(new Runnable() {
        public void run() {
          lx.addOutput(beagle);
        }
      });
    }
    public void itemRemoved(int index, NetworkDevice device) {
      final Beagle beagle = beagles.remove(index);
      dispatcher.dispatchEngine(new Runnable() {
        public void run() {
          //lx.removeOutput(beagle);
        }
      });
    }
  });

  lx.addOutput(new Beagle(lx, "10.200.1.255"));
}

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


ListenableList<Beagle> beagles = new ListenableList<Beagle>();

class Beagle extends LXOutput {
  Socket        socket;
  DatagramSocket  dsocket;
  OutputStream    output;
  NetworkDevice   networkDevice;
  String        controllerId;
  InetAddress   host;
  boolean       isBroadcast;

  static final int HEADER_LENGTH = 4;
  static final int BYTES_PER_PIXEL = 3;

  //final int numStrips = STRIP_ORD.length;
  int numPixels;
  int contentSizeBytes;
  int packetSizeBytes;
  byte[] packetData;

  Beagle(LX lx, NetworkDevice device, String controllerId) {
    this(lx, device, device.ipAddress, controllerId, false);
  }

  Beagle(LX lx, String _host, String _controllerId) {
    this(lx, _host, _controllerId, false);
  }

  Beagle(LX lx, String _host) {
    this(lx, _host, "", true);
  }

  private Beagle(LX lx, String host, String controllerId, boolean isBroadcast) {
    this(lx, null, NetworkUtils.ipAddrToInetAddr(host), controllerId, isBroadcast);
  }

  private Beagle(LX lx, NetworkDevice networkDevice, InetAddress host, String controllerId, boolean isBroadcast) {
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
    //if (isBroadcast != broadcastPacketTester.enabled.isOn()) return;

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

    // Get a list of the bars we are outputting to
    List<Bar> bars = new ArrayList<Bar>();
    for (Bar bar : model.bars) {
      if (bar.controllerId != null && bar.controllerId.equals(controllerId)) {
        bars.add(bar);
      }
    }

    // Initialize packet data
    int numPixels = 900; // highest number of leds in a bar;
    if (packetData == null || packetData.length != numPixels) {
      initPacketData(numPixels);
    }

    // Fill the datagram with pixel data
    // Fill with all black if we don't have bar data
    int packetIndex = 0;
    for (Bar bar : bars) {
      for (LXPoint p : bar.outerStrip.points) {
        setPixel(packetIndex++, colors[p.index]);
      }

      // because we flipped strip in model,
      // but he has them wired so all strip inputs are at top
      LXPoint[] innerPoints = bar.innerStrip.points;
      for (int i2 = innerPoints.length - 1; i2 > -1; i2--) {
        setPixel(packetIndex++, colors[innerPoints[i2].index]);
      }
    }

    // Send the bar data to the bar. yay!
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
//---------------------------------------------------------------------------------------------

// Helper class for testing bar output
// When a bar is recognized on the network,
// if this option is enabled, the output classes will
// send the color data for bar 0 regardless of if the
// bar that just connected is modelled yet or not
class BarOutputTester {
  final BooleanParameter enabled = new BooleanParameter("Test output enabled");
  BarOutputTester(LX lx) {
    //moduleRegistrar.modules.add(new Module("Test output", enabled));
  }
}

class BroadcastPacketTester {
  final BooleanParameter enabled = new BooleanParameter("Broadcast packet enabled");
  BroadcastPacketTester(LX lx) {
    //moduleRegistrar.modules.add(new Module("Broadcast packet", enabled));
  }
}

//---------------------------------------------------------------------------------------------
// class UIOutput extends SCWindow {
//   UIOutput(float x, float y, float w, float h) {
//     super(lx.ui,"OUTPUT",x,y,w,h);
//     float yPos = UIWindow.TITLE_LABEL_HEIGHT - 2;
//     final SortedSet<Beagle> sortedBeagles = new TreeSet<Beagle>(new Comparator<Beagle>() {
//       int compare(Beagle o1, Beagle o2) {
//         try {
//           return Integer.parseInt(o1.controllerId) - Integer.parseInt(o2.controllerId);
//         } catch (NumberFormatException e) {
//           return o1.controllerId.compareTo(o2.controllerId);
//         }
//       }
//     });
//     final List<UIItemList.Item> items = new ArrayList<UIItemList.Item>();
//     for (Beagle b : beagles) { sortedBeagles.add(b); }
//     for (Beagle b : sortedBeagles) { items.add(new BeagleItem(b)); }
//     final UIItemList outputList = new UIItemList(1, yPos, width-2, height-yPos-1);
//     outputList
//       .setItems     (items    )
//       .addToContainer   (this   );
//     beagles.addListener(new ListListener<Beagle>() {
//       void itemAdded(final int index, final Beagle b) {
//         dispatcher.dispatchUi(new Runnable() {
//           public void run() {
//             if (b.networkDevice != null) b.networkDevice.version.addListener(deviceVersionListener);
//             sortedBeagles.add(b);
//             items.clear();
//             for (Beagle b : sortedBeagles) { items.add(new BeagleItem(b)); }
//             outputList.setItems(items);
//             setTitle(items.size());
//             redraw();
//           }
//         });
//       }
//       void itemRemoved(final int index, final Beagle b) {
//         dispatcher.dispatchUi(new Runnable() {
//           public void run() {
//             if (b.networkDevice != null) b.networkDevice.version.removeListener(deviceVersionListener);
//             sortedBeagles.remove(b);
//             items.clear();
//             for (Beagle b : sortedBeagles) { items.add(new BeagleItem(b)); }
//             outputList.setItems(items);
//             setTitle(items.size());
//             redraw();
//           }
//         });
//       }
//     });
//     setTitle(items.size());
//   }

//   private final IntListener deviceVersionListener = new IntListener() {
//     public void onChange(int version) {
//       dispatcher.dispatchUi(new Runnable() {
//         public void run() {
//           redraw();
//         }
//       });
//     }
//   };

//   private void setTitle(int count) {
//     setTitle("OUTPUT (" + count + ")");
//   }

//   class BeagleItem extends UIItemList.AbstractItem {
//     final Beagle beagle;
//     BeagleItem(Beagle _beagle) {
//       this.beagle = _beagle;
//       beagle.enabled.addListener(new LXParameterListener() {
//         public void onParameterChanged(LXParameter parameter) { redraw(); }
//       });
//     }
//     String  getLabel  () {
//       if (beagle.networkDevice != null && beagle.networkDevice.version.get() != -1) {
//         return beagle.controllerId + " (v" + beagle.networkDevice.version + ")";
//       } else {
//         return beagle.controllerId;
//       }
//     }
//     boolean isSelected() { return beagle.enabled.isOn(); }
//     void onMousePressed(boolean hasFocus) { beagle.enabled.toggle(); }
//   }
// }
//---------------------------------------------------------------------------------------------
