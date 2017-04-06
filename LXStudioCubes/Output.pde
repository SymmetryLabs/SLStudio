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

void buildOutputs(final LX lx) {
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

  lx.addOutput(new SLController(lx, "10.200.1.255"));
  
  //lx.addOutput(new LIFXOutput());
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



// public class LIFXOutput extends LXOutput {
//   DatagramSocket dsocket;

//   int packetSizeBytes = 49;
//   byte[] packetData;

//   public LIFXOutput() {
//     super(lx);
//     packetData = new byte[packetSizeBytes];
//   }

//   void onSend(int[] colors) {
//     // Create data socket connection if needed
//     if (dsocket == null) {
//       try {
//         dsocket = new DatagramSocket();
//         dsocket.connect(new InetSocketAddress("10.1.10.255", 56700));
//         //socket.setTcpNoDelay(true);
//         // output = socket.getOutputStream();
//       }
//       catch (ConnectException e) {  dispose();  }
//       catch (IOException      e) {  dispose();  }
//       if (dsocket == null) return;
//     }

//     for (LIFXBulb bulb : model.lifxBulbs)
//       sendPacket(bulb.macAddress, colors[bulb.point.index]);
//   }

//   void sendPacket(String macAddressString, int col) {
//     // format mac address
//     String[] stringBytes = macAddressString.split(":");
//     byte[] macAddress = new byte[stringBytes.length];

//     for (int x = 0; x < stringBytes.length; x++) {
//         BigInteger temp = new BigInteger(stringBytes[x], 16);
//         byte[] raw = temp.toByteArray();
//         macAddress[x] = raw[raw.length - 1];
//     }

//     // format colors
//     int hue = (int)(LXColor.h(col)/360 * 65535);
//     int sat = (int)(LXColor.s(col)/100 * 65535);
//     int bri = (int)(LXColor.b(col)/100 * 65535);

//     // build packet
//     packetData[0] = (byte)0x00;
//     packetData[1] = (byte)0x00;
//     packetData[2] = (byte)0x00;
//     packetData[3] = (byte)0x14;

//     // client id (source)
//     packetData[4] = (byte)0x00;
//     packetData[5] = (byte)0x00;
//     packetData[6] = (byte)0x00;
//     packetData[7] = (byte)0x00;

//     // frame address
//     packetData[8]  = macAddress[0];
//     packetData[9]  = macAddress[1];
//     packetData[10] = macAddress[2];
//     packetData[11] = macAddress[3];
//     packetData[12] = macAddress[4];
//     packetData[13] = macAddress[5];
//     packetData[14] = (byte)0x00;
//     packetData[15] = (byte)0x00;

//     // *reserved*
//     packetData[16] = (byte)0x00;
//     packetData[17] = (byte)0x00;
//     packetData[18] = (byte)0x00;
//     packetData[19] = (byte)0x00;
//     packetData[20] = (byte)0x00;
//     packetData[21] = (byte)0x00;

//     // response
//     packetData[22] = (byte)0x00;

//     // sequence number
//     packetData[23] = (byte)0x00;

//     // protocol header
//     packetData[24] = (byte)0x00;
//     packetData[25] = (byte)0x00;
//     packetData[26] = (byte)0x00;
//     packetData[27] = (byte)0x00;
//     packetData[28] = (byte)0x00;
//     packetData[29] = (byte)0x00;
//     packetData[30] = (byte)0x00;
//     packetData[31] = (byte)0x00;

//     // message type
//     packetData[32] = (byte)0x66;
//     packetData[33] = (byte)0x00;

//     // *reserved*
//     packetData[34] = (byte)0x00;
//     packetData[35] = (byte)0x00;

//     /* payload ------*/
//     // reserved
//     packetData[36] = (byte)0x00;

//     // hue
//     packetData[37] = (byte)hue;
//     packetData[38] = (byte)(hue >> 8);

//     // saturation
//     packetData[39] = (byte)sat;
//     packetData[40] = (byte)(sat >> 8);

//     // brightness
//     packetData[41] = (byte)bri;
//     packetData[42] = (byte)(bri >> 8);

//     // kelvin
//     packetData[43] = (byte)0xAC;
//     packetData[44] = (byte)0x0D;

//     // milliseconds
//     packetData[45] = (byte)0x00;
//     packetData[46] = (byte)0x00;
//     packetData[47] = (byte)0x00;
//     packetData[48] = (byte)0x00;

//     try {
//       dsocket.send(new java.net.DatagramPacket(packetData, packetSizeBytes));
//     } catch (Exception e) {dispose();
//   }
// }


  // List<Bulb> bulbs;

  // public LIFXOutput() throws IOException {
  //  super(lx);

  //  bulbs = new ArrayList<Bulb>();
  //  BroadcastListener listener = new BroadcastListener();
  //  listener.bus().register(this);
  //  listener.startListen();

  //  println(listener);
  //  enabled.setValue(true);
  // }

  // void onSend(int[] colors) {
  //  if (model.lifxBulbs.size() < 1) return;

  //  LIFXBulb bulbModel = model.lifxBulbs.get(0);
  //  //for (LIFXBulb bulb : model.lifxBulbs) {
  //    for (Bulb bulbOutput : this.bulbs) {
  //      //if (THEY_DONT_MATCH) continue; <- make them all light up for now
        
  //      color c = colors[bulbModel.point.index];
  //      int r = (int)LXColor.red(c);
  //      int g = (int)LXColor.green(c);
  //      int b = (int)LXColor.blue(c);
  //      try {
  //        bulbOutput.setColor(LIFXColor.fromRGB(r, g, b) , 1);
  //      } catch (IOException e) {}
  //      //break;
  //    }
  //  //}
  // }
  
  // public void gatewayFound(GatewayDiscoveredEvent ev) {
  //  Gateway g = ev.getGateway();
  //  g.bus().register(this);

  //  println("gatewayFound");
  //  try {
  //    g.connect(); // automatically discovers bulbs
  //  } catch (IOException ex) { }
  // }
  
  // public void bulbDiscovered(GatewayBulbDiscoveredEvent event) throws IOException {
  //  this.bulbs.add(event.getBulb());

  //  // register for bulb events
  //  event.getBulb().bus().register(this);

  //  println("bulb discovered");
    
  //  // send some packets
  //  // event.getBulb().turnOff();
  //  // event.getBulb().setColor(LIFXColor.fromRGB(0, 255, 0));
  // }

  // public void bulbUpdated(BulbStatusUpdatedEvent event) {
  //  //System.out.println("bulb updated");
  // }
  
//}


ListenableList<SLController> controllers = new ListenableList<SLController>();

class SLController extends LXOutput {
  Socket        socket;
  DatagramSocket dsocket;
  OutputStream    output;
  NetworkDevice networkDevice;
  String        cubeId;
  InetAddress   host;
  boolean       isBroadcast;

  // Trip had to change order for Cisco as workaround for rotation bug
  final int[]   STRIP_ORD      = new int[] { 
                                             //RED
                                             9, 10, 11, 
                                             // GREEN 
                                             0, 1, 2,   
                                             // BLUE
                                             3, 4, 5,
                                             // WHITE
                                             6, 7, 8 }; 
  // final int[]  STRIP_ORD      = new int[] { 6, 7, 8, 9, 10, 11, 0, 1, 2, 3, 4, 5 };

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
    if (isBroadcast != broadcastPacketTester.enabled.isOn()) return;

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
    if ((cubeOutputTester.enabled.isOn() || isBroadcast) && model.cubes.size() > 0) {
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
//---------------------------------------------------------------------------------------------

// Helper class for testing cube output
// When a cube is recognized on the network,
// if this option is enabled, the output classes will
// send the color data for cube 0 regardless of if the
// cube that just connected is modelled yet or not
class CubeOutputTester {
  final BooleanParameter enabled = new BooleanParameter("Test output enabled");
  CubeOutputTester(LX lx) {
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
// class UIOutput extends UIWindow {
//   UIOutput(float x, float y, float w, float h) {
//     super(lx.ui,"Output",x,y,w,h);
//     float yPos = UIWindow.TITLE_LABEL_HEIGHT - 2;
//     final SortedSet<Beagle> sortedBeagles = new TreeSet<Beagle>(new Comparator<Beagle>() {
//       int compare(Beagle o1, Beagle o2) {
//         try {
//           return Integer.parseInt(o1.cubeId) - Integer.parseInt(o2.cubeId);
//         } catch (NumberFormatException e) {
//           return o1.cubeId.compareTo(o2.cubeId);
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

//     BooleanParameter allOutputsEnabled = new BooleanParameter("allOutputsEnabled", true);
//     UIButton outputsEnabledButton = new UIButton(87, 4, 50, 15);
//     outputsEnabledButton.setLabel("Connect").setParameter(allOutputsEnabled);
//     outputsEnabledButton.addToContainer(this);
//     allOutputsEnabled.addListener(new LXParameterListener() {
//       void onParameterChanged(LXParameter parameter) {
//         for (Beagle beagle : beagles) {
//           beagle.enabled.setValue(((BooleanParameter)parameter).isOn());
//         }
//       }
//     });
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
//     setTitle("Output (" + count + ")");
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
//         return beagle.cubeId + " (v" + beagle.networkDevice.version + ")";
//       } else {
//         return beagle.cubeId;
//       }
//     }
//     boolean isSelected() { return beagle.enabled.isOn(); }
//     void onMousePressed(boolean hasFocus) { beagle.enabled.toggle(); }
//   }
// }
//---------------------------------------------------------------------------------------------
