import java.net.SocketException;
import artnet4j.ArtNetException;
import artnet4j.ArtNetServer;
import artnet4j.events.ArtNetServerListener;
import artnet4j.packets.ArtDmxPacket;
import artnet4j.packets.ArtNetPacket;

class ArtNetRunner extends LXRunnableComponent {
  //private final int NUM_CHANNELS = 100;
  private ArtNetListener artNetListener;
  //private String[] bindings = new String[NUM_CHANNELS];
  private byte[] channels;

  public ArtNetRunner(LX lx) {
    this.artNetListener = new ArtNetListener();

    // byte[] bytes = loadBytes("artnet_bindings.json");
    // if (bytes != null) {
    //   try {
    //     JsonObject json = new Gson().fromJson(new String(bytes), JsonObject.class);
    //     for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
    //       int i = Integer.parseInt(entry.getKey());
    //       this.bindings[i] = entry.getValue().getAsString();
    //     }
    //   } catch (JsonSyntaxException e) {
    //     e.printStackTrace();
    //   }
    // }

    // println("\n-- ArtNet Bindings ----");
    // for (int i = 0; i < bindings.length; i++) {
    //   if (bindingExists(i)) {
    //     println("[" + i + "] = " + bindings[i]);
    //   }
    // }
    // println("\n");

  }

  protected void run(double deltaMs) {
    this.channels = artNetListener.getCurrentInputDmxArray();

    // [0] (live)
    outputControl.enabled.setValue(channels[0] > 0);

    // [1] (speed)
    lx.engine.speed.setValue(normalize(channels[1]));

    // [2] (brightness)
    lx.engine.output.brightness.setValue(normalize(channels[2]));

    // [3, 4, 5] (rgb color)
    lx.palette.clr.setColor(LXColor.rgb(channels[3], channels[4], channels[5]));

    // CHANNEL 1
    LXChannel channel1 = lx.engine.getChannel(0);
    channel1.enabled.setValue(channels[6] > 0); // [6]
    channel1.fader.setValue(normalize(channels[7])); // [7]
    channel1.goIndex(channels[8]); // [8]

    // CHANNEL 2
    LXChannel channel2 = lx.engine.getChannel(1);
    channel2.enabled.setValue(channels[9] > 0); // [9]
    channel2.fader.setValue(normalize(channels[10])); // [10]
    channel2.goIndex(channels[11]); // [11]

    // CHANNEL 3
    LXChannel channel3 = lx.engine.getChannel(2);
    channel3.enabled.setValue(channels[12] > 0); // [12]
    channel3.fader.setValue(normalize(channels[13])); // [13]
    channel3.goIndex(channels[14]); // [14]

    // CHANNEL 4
    LXChannel channel4 = lx.engine.getChannel(3);
    channel4.enabled.setValue(channels[15] > 0); // [15]
    channel4.fader.setValue(normalize(channels[16])); // [16]
    channel4.goIndex(channels[17]); // [17]

    // for (int i = 0; i < bindings.length; i++) {
    //   if (bindingExists(i))
    //     runCommand(bindings[i], channels[i]);
    // }
  }

  // private void runCommand(String command, int value) {
  //   //println(command + ": " + value);

  //   switch (command) {

  //     case "global/live":
  //       outputControl.enabled.setValue(value > 0);
  //       break;

  //     case "global/speed":
  //       lx.engine.speed.setValue(normalize(value));
  //       break;

  //     case "global/level":
  //       lx.engine.output.brightness.setValue(normalize(value));
  //       break;

  //     // case "channel1/fader":
  //     //   lx.engine.getChannel(0).fader.setValue(normalize(value));
  //     //   break;

  //     default: return;
  //   }
  // }

  // private boolean bindingExists(int i) {
  //   return bindings[i] != null && bindings[i].length() > 0;
  // }

  private float normalize(int value) {
    return value / 255.;
  }
}

class ArtNetListener {
  public static final int DMX_CHANNELS_COUNT = 512;

  private static final int SUBNET_COUNT = 16;
  private static final int UNIVERSE_COUNT = 16;

  private int inPort = ArtNetServer.DEFAULT_PORT;
  private int outPort = ArtNetServer.DEFAULT_PORT;
  private String broadcastAddress = ArtNetServer.DEFAULT_BROADCAST_IP;
  private int currentSubnet = 0;
  private int currentUniverse = 0;
  private int sequenceId = 0;

  private byte[][][] inputDmxArrays = new byte[SUBNET_COUNT][UNIVERSE_COUNT][DMX_CHANNELS_COUNT];

  private ArtNetServer artNetServer = null;

  ArtNetListener() {
    try {
      startArtNet();
    } catch (SocketException e) {
      println(e);
    } catch (ArtNetException e) {
      println(e);
    }
  }

  public void startArtNet() throws SocketException, ArtNetException {
    if (this.artNetServer != null) {
      stopArtNet();
    }

    this.artNetServer = new ArtNetServer(this.inPort, this.outPort);
    this.artNetServer.setBroadcastAddress(this.broadcastAddress);
    this.artNetServer.start();
    initArtNetReceiver();

    // println("ArtNet Started (broadcast: " + this.broadcastAddress
    //        + ", in: " + this.inPort + ", out: " + this.outPort + ")");
  }

  public void stopArtNet() {
    if (this.artNetServer != null) {
      this.artNetServer.stop();
      this.artNetServer = null;
      println("ArtNet Stopped");
    }
  }

  private void initArtNetReceiver() {
    this.artNetServer.addListener(
      new ArtNetServerListener() {
        @Override
        public void artNetPacketReceived(final ArtNetPacket artNetPacket) {
          switch(artNetPacket.getType()) {
            case ART_OUTPUT:
              ArtDmxPacket artDmxPacket = (ArtDmxPacket) artNetPacket;
              int subnet = artDmxPacket.getSubnetID();
              int universe = artDmxPacket.getUniverseID();

              System.arraycopy(
                artDmxPacket.getDmxData(), 0,
                inputDmxArrays[subnet][universe], 0,
                artDmxPacket.getNumChannels()
              );

              //println("Received packet in universe " + universe
              //  + " / subnet " + subnet + " containing "
              //  + artDmxPacket.getNumChannels() + " channel values:");
              //printArray(artDmxPacket.getDmxData());
              break;

            default:
              break;
          }
        }

        @Override
        public void artNetServerStopped(final ArtNetServer artNetServer) {
        }

        @Override
        public void artNetServerStarted(final ArtNetServer artNetServer) {
        }

        @Override
        public void artNetPacketUnicasted(final ArtNetPacket artNetPacket) {
        }

        @Override
        public void artNetPacketBroadcasted(final ArtNetPacket artNetPacket) {
        }
    });
  }

  public byte[] getInputDmxArray(final int subnet, final int universe) {
    return inputDmxArrays[subnet][universe];
  }

  public byte[] getCurrentInputDmxArray() {
    return getInputDmxArray(this.currentSubnet, this.currentUniverse);
  }
 
  public Integer toInt(Byte dmxChannelValue) {
    int intValue = dmxChannelValue.intValue();
    return intValue < 0 ? intValue + 256 : intValue;
  } 
  
  public Integer getValueAt(final int index) {
    return toInt((Byte)getCurrentInputDmxArray()[index]);
  }
}