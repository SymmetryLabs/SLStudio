import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

static class NetworkManager {

  private static NetworkManager instance;

  private final ExecutorService executor = Executors.newCachedThreadPool();

  public static NetworkManager getInstance() {
    if (instance == null) {
      instance = new NetworkManager();
    }
    return instance;
  }

  public ExecutorService getExecutor() {
    return executor;
  }
}

static class NetworkInfo {
  static List<InetAddress> getBroadcastAddresses() {
    List<InetAddress> addresses = new ArrayList<InetAddress>();
    Enumeration<NetworkInterface> nets = null;
    try {
      nets = NetworkInterface.getNetworkInterfaces();
    } catch (SocketException e) {
      e.printStackTrace();
    }
    if (nets != null) {
      while (nets.hasMoreElements()) {
        NetworkInterface iface = nets.nextElement();
        for (InterfaceAddress addr : iface.getInterfaceAddresses()) {
          InetAddress broadcast = addr.getBroadcast();
          if (broadcast != null) {
            addresses.add(broadcast);
          }
        }
      }
    }
    return addresses;
  }
  static List<InetAddress> getInetAddresses() {
    List<InetAddress> addresses = new ArrayList<InetAddress>();
    Enumeration<NetworkInterface> nets = null;
    try {
      nets = NetworkInterface.getNetworkInterfaces();
    } catch (SocketException e) {
      e.printStackTrace();
    }
    if (nets != null) {
      while (nets.hasMoreElements()) {
        NetworkInterface iface = nets.nextElement();
        for (InterfaceAddress addr : iface.getInterfaceAddresses()) {
          InetAddress inetAddress = addr.getAddress();
          if (inetAddress != null) {
            addresses.add(inetAddress);
          }
        }
      }
    }
    return addresses;
  }
}

static class ControllerCommand {

  public final InetAddress addr;
  public final byte[] command;
  public final int responseSize;

  private ControllerCommandCallback callback;

  ControllerCommand(InetAddress addr, byte[] command) {
    this(addr, command, -1, null);
  }

  ControllerCommand(final InetAddress addr, final byte[] command,
      final int responseSize, final ControllerCommandCallback callback) {
    this.addr = addr;
    this.command = command;
    this.responseSize = responseSize;
    this.callback = callback;

    NetworkManager.getInstance().getExecutor().submit(new Runnable() {
      public void run() {
        DatagramSocket socket = null;
        try {
          socket = new DatagramSocket();
        } catch (SocketException e) {
          return;
        }
        try {
          try {
            socket.setSoTimeout(1000);
          } catch (SocketException e) {
            return;
          }
          try {
            socket.send(new java.net.DatagramPacket(command, command.length,
              new InetSocketAddress(addr, 7890)));
          } catch (IOException e) {
            return;
          }

          if (responseSize < 1 || callback == null) return;

          do {
            byte[] response = new byte[responseSize];
            java.net.DatagramPacket packet = new java.net.DatagramPacket(response, response.length);
            try {
              socket.receive(packet);
              if (callback != null) callback.onResponse(packet);
            } catch (IOException e) {
              break;
            }
          } while (!addr.isMulticastAddress());
        } finally {
          socket.close();
          if (callback != null) callback.onFinish();
        }
      }
    });
  }
}

public static interface ControllerCommandCallback {
  public void onResponse(java.net.DatagramPacket response);
  public void onFinish();
}

public static abstract class AbstractControllerCommandCallback implements ControllerCommandCallback {
  public void onResponse(java.net.DatagramPacket response) {}
  public void onFinish() {}
}

static class PingCommand extends ControllerCommand {
  PingCommand(InetAddress addr, final ControllerCommandCallback callback) {
    super(addr, new byte[] { (byte)136, 1 }, 1, new ControllerCommandCallback() {
      void onResponse(java.net.DatagramPacket response) {
        if (callback != null && response.getLength() == 1 && response.getData()[0] == 1) {
          callback.onResponse(response);
        }
      }
      void onFinish() { if (callback != null) callback.onFinish(); }
    });
  }
}

public static class ResetCommand extends ControllerCommand {
  public ResetCommand(InetAddress addr) {
    super(addr, new byte[] { (byte)136, 2 });
  }
}

public static class VersionCommand extends ControllerCommand {
  public VersionCommand(InetAddress addr, final VersionCommandCallback callback) {
    super(addr, new byte[] { (byte)136, 3 }, 1, new ControllerCommandCallback() {
      void onResponse(java.net.DatagramPacket response) {
        if (callback != null && response.getLength() == 1) {
          callback.onResponse(response, response.getData()[0]);
        }
      }
      void onFinish() { if (callback != null) callback.onFinish(); }
    });
  }
}

public static interface VersionCommandCallback {
  public void onResponse(java.net.DatagramPacket response, int version);
  public void onFinish();
}

public static abstract class AbstractVersionCommandCallback implements VersionCommandCallback {
  public void onResponse(java.net.DatagramPacket response, int version) {}
  public void onFinish() {}
}

public static class MacAddrCommand extends ControllerCommand {
  public MacAddrCommand(InetAddress addr, final MacAddrCommandCallback callback) {
    super(addr, new byte[] { (byte)136, 4 }, 6, new ControllerCommandCallback() {
      void onResponse(java.net.DatagramPacket response) {
        if (callback != null && response.getLength() == 6) {
          callback.onResponse(response, response.getData());
        }
      }
      void onFinish() { if (callback != null) callback.onFinish(); }
    });
  }
}

public static interface MacAddrCommandCallback {
  public void onResponse(java.net.DatagramPacket response, byte[] macAddr);
  public void onFinish();
}

public static abstract class AbstractMacAddrCommandCallback implements MacAddrCommandCallback {
  public void onResponse(java.net.DatagramPacket response, byte[] macAddr) {}
  public void onFinish() {}
}

class NetworkMonitor {

  private final ControllerScan controllerScan = new ControllerScan();

  public final ListenableList<NetworkDevice> networkDevices = controllerScan.networkDevices;

  private final java.util.TimerTask scanTask = new ScanTask();
  private final java.util.Timer timer = new java.util.Timer();

  private boolean oldVersionWarningGiven = false;

  NetworkMonitor(LX lx) {
    networkDevices.addListener(new AbstractListListener<NetworkDevice>() {
      void itemAdded(int index, final NetworkDevice result) {
        new VersionCommand(result.ipAddress, new VersionCommandCallback() {
          public void onResponse(java.net.DatagramPacket response, final int version) {
            dispatcher.dispatchEngine(new Runnable() {
              public void run() {
                result.version.set(version);
              }
            });
          }
          public void onFinish() {
            dispatcher.dispatchEngine(new Runnable() {
              public void run() {
                if (!oldVersionWarningGiven) {
                  for (NetworkDevice device : networkDevices) {
                    if (device.version.get() != -1
                        && (device.version.get() < result.version.get()
                            || device.version.get() > result.version.get())) {
                      System.out.println("WARNING: One or more cubes have outdated firmware!");
                      oldVersionWarningGiven = true;
                      return;
                    }
                  }
                }
              }
            });
          }
        });
      }
    });
  }

  void start() {
    timer.schedule(scanTask, 0, 500);
  }

  class ScanTask extends java.util.TimerTask {
    public void run() {
      controllerScan.scan();
    }
  }

}

public static class NetworkDevice {

  public final InetAddress ipAddress;
  public final byte[] macAddress;
  public final ListenableInt version = new ListenableInt(-1);

  public int connectionRetries = 0;

  public NetworkDevice(InetAddress ipAddress, byte[] macAddress) {
    this.ipAddress = ipAddress;
    this.macAddress = macAddress;
  }

}

public class ControllerScan {

  public final ListenableList<NetworkDevice> networkDevices = new ListenableList<NetworkDevice>();

  public void scan() {
    new Runnable() {
      final ListenableList<NetworkDevice> tmpNetworkDevices = new ListenableList<NetworkDevice>();
      int instances = 0;
      public void run() {
        for (InetAddress addr : NetworkInfo.getBroadcastAddresses()) {
          instances++;
          new MacAddrCommand(addr, new MacAddrCommandCallback() {
            public void onResponse(java.net.DatagramPacket response, byte[] macAddr) {
              final NetworkDevice networkDevice = new NetworkDevice(response.getAddress(), macAddr);
              dispatcher.dispatchEngine(new Runnable() {
                public void run() {
                  tmpNetworkDevices.add(networkDevice);
                }
              });
            }

            public void onFinish() {
              dispatcher.dispatchEngine(new Runnable() {
                public void run() {
                  if (--instances == 0) {
                    for (int i = 0; i < networkDevices.size(); i++) {
                      NetworkDevice device = networkDevices.get(i);
                      boolean found = false;
                      boolean removed = false;
                      for (NetworkDevice tmpDevice : tmpNetworkDevices) {
                        if (Arrays.equals(device.macAddress, tmpDevice.macAddress)
                            && device.ipAddress.equals(tmpDevice.ipAddress)) {
                          found = true;
                          device.connectionRetries = 0;
                          break;
                        } else if (Arrays.equals(device.macAddress, tmpDevice.macAddress)
                            || device.ipAddress.equals(tmpDevice.ipAddress)) {
                          removed = true;
                          networkDevices.remove(i--);
                          break;
                        }
                      }
                      if (!found && !removed) {
                        if (++device.connectionRetries == 5) {
                          networkDevices.remove(i--);
                        }
                      }
                    }
                    for (NetworkDevice tmpDevice : tmpNetworkDevices) {
                      boolean found = false;
                      for (NetworkDevice device : networkDevices) {
                        if (Arrays.equals(device.macAddress, tmpDevice.macAddress)) {
                          found = true;
                          break;
                        }
                      }
                      if (!found) {
                        networkDevices.add(tmpDevice);
                      }
                    }
                  }
                }
              });
            }
          });
        }
      }
    }.run();
  }
}

class CubeResetModule {
  final BooleanParameter enabled = new BooleanParameter("Cube reset enabled");
  CubeResetModule(LX lx) {
    //moduleRegistrar.modules.add(new Module("Reset all cubes", enabled, true));
    enabled.addListener(new LXParameterListener() {
      public void onParameterChanged(LXParameter parameter) {
        if (enabled.isOn()) new CubeResetter().run();
      }
    });
  }
}

static class CubeResetter {
  void run() {
    for (InetAddress addr : NetworkInfo.getBroadcastAddresses()) {
      new ResetCommand(addr);
    }
  }
}
