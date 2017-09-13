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
    //if (!artnet.enabled.isOn()) return;

    //this.channels = artNetListener.getCurrentInputDmxArray();

    int i = 0;

    // [0] (live)
    outputControl.enabled.setValue(get(i++) > 0);

    // [1] (speed)
    lx.engine.speed.setValue(getNormalized(i++));

    // [2] (brightness)
    lx.engine.output.brightness.setValue(getNormalized(i++));

    // [3] (blend mode)
    lx.engine.crossfaderBlendMode.setValue((int)Math.max(5, get(i++)));

    // [4] (crossfader)
    lx.engine.crossfader.setValue(getNormalized(i++));

    // [5, 6, 7] (rgb color)
    lx.palette.clr.setColor(LXColor.rgb(get(i++), get(i++), get(i++)));

    // CHANNEL 1
    LXChannel channel1 = lx.engine.getChannel(0);
    channel1.enabled.setValue(get(i++) > 0); // [8]
    channel1.fader.setValue(getNormalized(i++)); // [9]
    channel1.blendMode.setValue((int)Math.max(4, get(i++))); // [10]
    channel1.goIndex(get(i++)); // [11]

    LXChannel.CrossfadeGroup channel1CrossfadeGroup = LXChannel.CrossfadeGroup.BYPASS;
    if (get(i) == 0) {
      channel1CrossfadeGroup = LXChannel.CrossfadeGroup.BYPASS;
    } else if (get(i) == 1) {
      channel1CrossfadeGroup = LXChannel.CrossfadeGroup.A;
    } else if (get(i++) > 127) {
      channel1CrossfadeGroup = LXChannel.CrossfadeGroup.B;
    }
    channel1.crossfadeGroup.setValue(channel1CrossfadeGroup); // [12]

    // CHANNEL 2
    LXChannel channel2 = lx.engine.getChannel(1);
    channel2.enabled.setValue(get(i++) > 0); // [13]
    channel2.fader.setValue(getNormalized(i++)); // [14]
    channel2.blendMode.setValue((int)Math.max(4, get(i++))); // [15]
    channel2.goIndex(get(i++)); // [16]

    LXChannel.CrossfadeGroup channel2CrossfadeGroup = LXChannel.CrossfadeGroup.BYPASS;
    if (get(i) == 0) {
      channel2CrossfadeGroup = LXChannel.CrossfadeGroup.BYPASS;
    } else if (get(i) == 1) {
      channel2CrossfadeGroup = LXChannel.CrossfadeGroup.A;
    } else if (get(i++) > 127) {
      channel2CrossfadeGroup = LXChannel.CrossfadeGroup.B;
    }
    channel2.crossfadeGroup.setValue(channel2CrossfadeGroup); // [17]

    // CHANNEL 3
    LXChannel channel3 = lx.engine.getChannel(2);
    channel3.enabled.setValue(get(i++) > 0); // [18]
    channel3.fader.setValue(getNormalized(i++)); // [19]
    channel3.blendMode.setValue((int)Math.max(4, get(i++))); // [20]
    channel3.goIndex(get(i++)); // [21]

    LXChannel.CrossfadeGroup channel3CrossfadeGroup = LXChannel.CrossfadeGroup.BYPASS;
    if (get(i) == 0) {
      channel3CrossfadeGroup = LXChannel.CrossfadeGroup.BYPASS;
    } else if (get(i) == 1) {
      channel3CrossfadeGroup = LXChannel.CrossfadeGroup.A;
    } else if (get(i++) > 127) {
      channel3CrossfadeGroup = LXChannel.CrossfadeGroup.B;
    }
    channel3.crossfadeGroup.setValue(channel3CrossfadeGroup); // [22]

    // CHANNEL 4
    LXChannel channel4 = lx.engine.getChannel(3);
    channel4.enabled.setValue(get(i++) > 0); // [23]
    channel4.fader.setValue(getNormalized(i++)); // [24]
    channel4.blendMode.setValue((int)Math.max(4, get(i++))); // [25]
    channel4.goIndex(get(i++)); // [26]

    LXChannel.CrossfadeGroup channel4CrossfadeGroup = LXChannel.CrossfadeGroup.BYPASS;
    if (get(i) == 0) {
      channel4CrossfadeGroup = LXChannel.CrossfadeGroup.BYPASS;
    } else if (get(i) == 1) {
      channel4CrossfadeGroup = LXChannel.CrossfadeGroup.A;
    } else if (get(i++) > 127) {
      channel4CrossfadeGroup = LXChannel.CrossfadeGroup.B;
    }
    channel4.crossfadeGroup.setValue(channel4CrossfadeGroup); // [27]

    // for (int i = 0; i < bindings.length; i++) {
    //   if (bindingExists(i))
    //     runCommand(bindings[i], channels[i]);
    // }
  }

  private int get(int i) {
    return artNetListener.getValueAt(i);
  }

  private float getNormalized(int i) {
    return get(i)/255.;
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