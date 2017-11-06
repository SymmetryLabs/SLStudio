public class HeartEventRunner_Hearts extends LXRunnableComponent {

  private final String channelLabel;

  private float elapsedMs = 0;

  public HeartEventRunner_Hearts(String channelLabel) {
    this.channelLabel = channelLabel;
  }

  public void run(double deltaMs) {
    if ((elapsedMs += deltaMs) > 10000) {
      stop();
    }
    
    //LXChannel channel = lx.engine.getChannel(4);
    LXChannel channel = lx.engine.getChannel(channelLabel);
    if (channel != null) {
      
      if (elapsedMs < 4000) {
        if (channel.fader.getValuef() < 0.8) {
          channel.fader.setValue(channel.fader.getValuef() + 0.01);
        }
      } 

      if (elapsedMs > 7000) {
        channel.fader.setValue(channel.fader.getValuef() - 0.02);
      }
    }
  }

  protected void onStart() {
    LXChannel channel = lx.engine.getChannel(4);
    if (channel != null) {
      ((Hearts)channel.getActivePattern()).resetHearts();
    }
  }

  protected void onStop() {
    reset();
  }

  protected void onReset() {
    elapsedMs = 0;

    LXChannel channel = lx.engine.getChannel(4);
    if (channel != null) {
      ((Hearts)channel.getActivePattern()).resetHearts();
    }
  }
}


// public class HeartEventRunner_Bubbles extends LXRunnableComponent {

//   private final String channelLabel;

//   private float elapsedMs = 0;

//   public HeartEventRunner_Bubbles(String channelLabel) {
//     this.channelLabel = channelLabel;
//   }

//   public void run(double deltaMs) {
//     if ((elapsedMs += deltaMs) > 8000) {
//       stop();
//     }

//     //this.channel.fader.setValue(faderEnvelope.getValuef());

//     LXChannel channel = lx.engine.getChannel(channelLabel);

    
//     if (channel != null) {
      
//       if (elapsedMs < 6000) {
//         if (channel.fader.getValuef() < 0.5) {
//           channel.fader.setValue(channel.fader.getValuef() + 0.01);
//         }
//       } else {
//         channel.fader.setValue(channel.fader.getValuef() - 0.01);
//       }
//     }
//   }

//   protected void onStart() {

//   }

//   protected void onStop() {
//     reset();
//   }

//   protected void onReset() {
//     elapsedMs = 0;
//   }
// }

public class HeartEventRunner_PanelFlash extends LXRunnableComponent {

  private final String channelLabel;

  private LXChannel channel = null;

  // public final LinearEnvelope fader = new LinearEnvelope("fader", 0, 1, 1000);
  // public final LinearEnvelope yEnvelope = new LinearEnvelope("yEnvelope", 0, 1, 4000);

  private float elapsedMs = 0;

  public HeartEventRunner_PanelFlash(String channelLabel) {
    this.channelLabel = channelLabel;

    // addModulator(fader);
    // addModulator(yEnvelope);
  }

  public void run(double deltaMs) {
    if ((elapsedMs += deltaMs) > 10000) {
      stop();
    }

    if (channel != null) {
      
      if (elapsedMs < 6000) {
        if (channel.fader.getValuef() < 1) {
          channel.fader.setValue(channel.fader.getValuef() + 0.007);
        }
      } else {

      }


      LXParameter attack = channel.getActivePattern().getParameter("ATTK");
      attack.setValue(0.2);

      LXParameter decay = channel.getActivePattern().getParameter("DECAY");
      decay.setValue(0.5);

      // LXParameter decay = channel.getActivePattern().getParameter("DECAY");
      // decay.setValue(decay.getValuef() - 0.01);


      if (elapsedMs > 6000) {
        channel.fader.setValue(channel.fader.getValuef() - 0.01);

        attack.setValue(attack.getValue() - 0.01);
      }

      if (elapsedMs > 4000) {
        LXParameter blur = channel.getEffect("Blur").getParameter("amount");
        blur.setValue(blur.getValuef() + 0.01);
      }

      LXParameter yThreshold =  channel.getActivePattern().getParameter("YTHRE");
      yThreshold.setValue(yThreshold.getValuef() + 0.005);
      //channel.fader.setValue(fader.getValuef());
      //channel.getActivePattern().getParameter("YTHRE").setValue(yEnvelope.getValuef());
    } else {
      channel = lx.engine.getChannel(channelLabel);
    }
  }

  protected void onStart() {
    // fader.start();
    // yEnvelope.start();

    if (channel != null) {
      channel.fader.setValue(0);

      LXParameter yThreshold =  channel.getActivePattern().getParameter("YTHRE");
      yThreshold.setValue(0);

      LXParameter attack = channel.getActivePattern().getParameter("ATTK");
      attack.setValue(0.2);

      LXParameter decay = channel.getActivePattern().getParameter("DECAY");
      decay.setValue(0.5);

      channel.getEffect("Blur").enabled.setValue(true);
      channel.getEffect("Blur").getParameter("amount").setValue(0);
    }
  }

  protected void onStop() {
    reset();
  }

  protected void onReset() {
    elapsedMs = 0;
    
    if (channel != null) {
      channel.fader.setValue(0);

      LXParameter yThreshold =  channel.getActivePattern().getParameter("YTHRE");
      yThreshold.setValue(0);

      channel.getEffect("Blur").enabled.setValue(false);
      channel.getEffect("Blur").getParameter("amount").setValue(0);

      // LXParameter attack = channel.getActivePattern().getParameter("ATTK");
      // attack.setValue(1);

      // LXParameter decay = channel.getActivePattern().getParameter("DECAY");
      // decay.setValue(1);

      //Collections.emtpyList(channel.getActivePattern().flashes);
    }

    // yEnvelope.reset();
    // yEnvelope.reset();
  }
}


public class HeartEventRunner extends LXRunnableComponent {

  private final String channelLabel;

  public final LinearEnvelope faderEnvelope;

  private float elapsedMs = 0;

  public HeartEventRunner(String channelLabel) {
    this.channelLabel = channelLabel;

    this.faderEnvelope = new LinearEnvelope("fader", 0, 1, 10000);
  }

  public void run(double deltaMs) {
    if ((elapsedMs += deltaMs) > 6000) {
      stop();
    }

    //this.channel.fader.setValue(faderEnvelope.getValuef());

    LXChannel channel = lx.engine.getChannel(channelLabel);

    if (channel != null) {
      channel.fader.setValue(faderEnvelope.getValuef());
    }
  }

  protected void onStart() {
    faderEnvelope.start();
  }

  protected void onStop() {
    reset();
  }

  protected void onReset() {
    elapsedMs = 0;
    faderEnvelope.stop();
    faderEnvelope.reset();
  }
}

public class HeartEventRunnerFinale extends LXRunnableComponent {

  private final String channelLabel;

  public final LinearEnvelope faderEnvelope;

  private float elapsedMs = 0;

  public HeartEventRunnerFinale(String channelLabel) {
    this.channelLabel = channelLabel;

    this.faderEnvelope = new LinearEnvelope("fader", 0, 1, 10000);
  }

  public void run(double deltaMs) {
    if ((elapsedMs += deltaMs) > 6000) {
      stop();
    }

    //this.channel.fader.setValue(faderEnvelope.getValuef());

    LXChannel channel = lx.engine.getChannel(channelLabel);

    if (channel != null) {
      channel.fader.setValue(faderEnvelope.getValuef());
    }
  }

  protected void onStart() {
    faderEnvelope.start();
  }

  protected void onStop() {
    reset();
  }

  protected void onReset() {
    elapsedMs = 0;
    faderEnvelope.stop();
    faderEnvelope.reset();
  }
}

public class HeartEventListener {

  private final boolean TESTING_ENABLED = false;
  private final int PORT = 8080;

  private DatagramSocket socket;

  private Thread receiver;
  private Thread tester;

  public HeartEventListener() {
    try {
      this.socket = new DatagramSocket(PORT);

      this.receiver = new ReceiverThread();
      this.tester = new TesterThread();

      receiver.start();

      if (TESTING_ENABLED) {
        tester.start();
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private class ReceiverThread extends Thread {
    public void run() {
      println("Running ReceiverThread...");

      while(true) {
        try {
          byte[] buf = new byte[1];

          DatagramPacket packet = new DatagramPacket(buf, buf.length);
          socket.receive(packet);

          int eventIndex = (int)(packet.getData()[0]);

          triggerEvent(eventIndex);

        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
  }

  private class TesterThread extends Thread {
    public void run() {
      println("Running TesterThread...");

      while(true) {
        try {
          Random rand = new Random();
          Thread.sleep(rand.nextInt(10) * 1000);

          byte[] buff = {(byte)rand.nextInt(1)};

          InetAddress address = InetAddress.getByName("localhost");

          DatagramPacket packet = new DatagramPacket(buff, buff.length, address, PORT);
          socket.send(packet);

        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
  }

  private void triggerEvent(int index) {
    println("packet");
    switch(index) {
      case 51: // 1 
        // research event
        heartRunnerResearch.start();
        println("Research: event");
        break;

      case 52: // 1 
        // research finale
        heartRunnerResearchFinale.start();
        println("Research: finale");
        break;

      case 49: // 1 
        // patient event
        heartRunnerPatient.start();
        println("Patient: event");
        break;

      case 50: // 1 
        // patient finale
        heartRunnerPatientFinale.start();
        println("Patient: finale");
        break;

      case 53: // 1 
        // care team event
        heartRunnerCareTeam.start();
        println("Care Team: event");
        break;

      case 54: // 1 
        // care team finale
        heartRunnerCareTeamFinale.start();
        println("Care Team: finale");
        break;

      default:
        println("invalid heart event index");
    }
  }
}