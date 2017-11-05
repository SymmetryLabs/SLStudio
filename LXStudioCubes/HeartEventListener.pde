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
    //println("received a new heart event! " + index);

    switch(index) {
      case 49: // 1 
        // research event
        heartRunner1.start();
        break;

      case 50: // 1 
        // research finale
        heartRunner1.start();
        break;

      case 51: // 1 
        // patient event
        heartRunner2.start();
        break;

      case 52: // 1 
        // patient finale
        heartRunner2.start();
        break;

      case 53: // 1 
        // care team event
        heartRunner3.start();
        break;

      case 54: // 1 
        // care team finale
        heartRunner3.start();
        break;

      default:
        println("invalid heart event index");
    }
  }
}