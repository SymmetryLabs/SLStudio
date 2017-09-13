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
    outputControl.enabled.setValue(get(0) > 0);

    // [1] (speed)
    lx.engine.speed.setNormalized(getNormalized(1));

    // [2] (brightness)
    lx.engine.output.brightness.setNormalized(getNormalized(2));

    // [3] (blend mode)
    lx.engine.crossfaderBlendMode.setValue((int)Math.min(5, get(3)));

    // [4] (crossfader)
    lx.engine.crossfader.setNormalized(getNormalized(4));

    // [5, 6, 7] (rgb color)
    lx.palette.clr.setColor(LXColor.rgb(get(5), get(6), get(7)));

    // CHANNEL 1
    LXChannel channel1 = lx.engine.getChannel(0);
    channel1.enabled.setValue(get(8) > 0); // [8]
    channel1.fader.setNormalized(getNormalized(9)); // [9]
    channel1.blendMode.setValue((int)Math.min(4, get(10))); // [10]
    channel1.goIndex(get(11)); // [11]

    LXChannel.CrossfadeGroup channel1CrossfadeGroup = LXChannel.CrossfadeGroup.BYPASS;
    if (get(12) == 0) {
      channel1CrossfadeGroup = LXChannel.CrossfadeGroup.BYPASS;
    } else if (get(12) == 1) {
      channel1CrossfadeGroup = LXChannel.CrossfadeGroup.A;
    } else if (get(12) == 2) {
      channel1CrossfadeGroup = LXChannel.CrossfadeGroup.B;
    }
    channel1.crossfadeGroup.setValue(channel1CrossfadeGroup); // [12]

    int channel1MaskValue = get(13); // [13]
    for (LXEffect effect : channel1.effects) {
      if (effect.label.getString().equals("HangingCubesMask")) {
        if (channel1MaskValue == 1) {
          effect.enabled.setValue(true);
        } else {
          effect.enabled.setValue(false);
        }
      }
      if (effect.label.getString().equals("FloorCubesMask")) {
        if (channel1MaskValue == 2) {
          effect.enabled.setValue(true);
        } else {
          effect.enabled.setValue(false);
        }
      }
    }

    // CHANNEL 2
    LXChannel channel2 = lx.engine.getChannel(1);
    channel2.enabled.setValue(get(14) > 0); // [14]
    channel2.fader.setNormalized(getNormalized(15)); // [15]
    channel2.blendMode.setValue((int)Math.min(4, get(16))); // [16]
    channel2.goIndex(get(17)); // [17]

    LXChannel.CrossfadeGroup channel2CrossfadeGroup = LXChannel.CrossfadeGroup.BYPASS;
    if (get(18) == 0) {
      channel2CrossfadeGroup = LXChannel.CrossfadeGroup.BYPASS;
    } else if (get(18) == 1) {
      channel2CrossfadeGroup = LXChannel.CrossfadeGroup.A;
    } else if (get(18) == 2) {
      channel2CrossfadeGroup = LXChannel.CrossfadeGroup.B;
    }
    channel2.crossfadeGroup.setValue(channel2CrossfadeGroup); // [18]

    int channel2MaskValue = get(19); // [19]
    for (LXEffect effect : channel2.effects) {
      if (effect.label.getString().equals("HangingCubesMask")) {
        if (channel2MaskValue == 1) {
          effect.enabled.setValue(true);
        } else {
          effect.enabled.setValue(false);
        }
      }
      if (effect.label.getString().equals("FloorCubesMask")) {
        if (channel2MaskValue == 2) {
          effect.enabled.setValue(true);
        } else {
          effect.enabled.setValue(false);
        }
      }
    }

    // CHANNEL 3
    LXChannel channel3 = lx.engine.getChannel(2);
    channel3.enabled.setValue(get(20) > 0); // [20]
    channel3.fader.setNormalized(getNormalized(21)); // [21]
    channel3.blendMode.setValue((int)Math.min(4, get(22))); // [22]
    channel3.goIndex(get(23)); // [23]

    LXChannel.CrossfadeGroup channel3CrossfadeGroup = LXChannel.CrossfadeGroup.BYPASS;
    if (get(24) == 0) {
      channel3CrossfadeGroup = LXChannel.CrossfadeGroup.BYPASS;
    } else if (get(24) == 1) {
      channel3CrossfadeGroup = LXChannel.CrossfadeGroup.A;
    } else if (get(24) == 2) {
      channel3CrossfadeGroup = LXChannel.CrossfadeGroup.B;
    }
    channel3.crossfadeGroup.setValue(channel3CrossfadeGroup); // [24]

    int channel3MaskValue = get(25); // [25]
    for (LXEffect effect : channel3.effects) {
      if (effect.label.getString().equals("HangingCubesMask")) {
        if (channel3MaskValue == 1) {
          effect.enabled.setValue(true);
        } else {
          effect.enabled.setValue(false);
        }
      }
      if (effect.label.getString().equals("FloorCubesMask")) {
        if (channel3MaskValue == 2) {
          effect.enabled.setValue(true);
        } else {
          effect.enabled.setValue(false);
        }
      }
    }

    // CHANNEL 4
    LXChannel channel4 = lx.engine.getChannel(3);
    channel4.enabled.setValue(get(26) > 0); // [26]
    channel4.fader.setNormalized(getNormalized(27)); // [27]
    channel4.blendMode.setValue((int)Math.min(4, get(28))); // [28]
    channel4.goIndex(get(29)); // [29]

    LXChannel.CrossfadeGroup channel4CrossfadeGroup = LXChannel.CrossfadeGroup.BYPASS;
    if (get(30) == 0) {
      channel4CrossfadeGroup = LXChannel.CrossfadeGroup.BYPASS;
    } else if (get(30) == 1) {
      channel4CrossfadeGroup = LXChannel.CrossfadeGroup.A;
    } else if (get(30) == 2) {
      channel4CrossfadeGroup = LXChannel.CrossfadeGroup.B;
    }
    channel4.crossfadeGroup.setValue(channel4CrossfadeGroup); // [30]

    int channel4MaskValue = get(31); // [30]
    for (LXEffect effect : channel4.effects) {
      if (effect.label.getString().equals("HangingCubesMask")) {
        if (channel4MaskValue == 1) {
          effect.enabled.setValue(true);
        } else {
          effect.enabled.setValue(false);
        }
      }
      if (effect.label.getString().equals("FloorCubesMask")) {
        if (channel4MaskValue == 2) {
          effect.enabled.setValue(true);
        } else {
          effect.enabled.setValue(false);
        }
      }
    }

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