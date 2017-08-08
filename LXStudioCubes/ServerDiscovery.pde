import javax.jmdns.JmDNS;
import javax.jmdns.*;
import javax.jmdns.ServiceInfo;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.io.*;
import java.net.*;
import java.util.*;

class ServerDiscovery {

    final BooleanParameter enabled = new BooleanParameter("Server discovery enabled");

    String hostname = null;
    String serviceName = "sugarcubes";

    JmDNS jmdns = null;
    JmmDNS jmmdns = null;

    ServerDiscovery() {
        java.util.logging.Logger.getLogger("javax.jmdns").setLevel(Level.SEVERE);

        // JmmDNS.Factory.newJmmDNS();

        

        enabled.addListener(new LXParameterListener() {
            public void onParameterChanged(LXParameter parameter) {
                if (enabled.isOn()) {
                    startAsync();
                } else {
                    stopAsync();
                }
            }
        });
        enabled.setValue(true);

    }

    void start() {


        ArrayList<InetAddress> addresses = new ArrayList();


        try {
            
            Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
            for (NetworkInterface netint : Collections.list(nets)) {

                Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
                for (InetAddress inetAddress : Collections.list(inetAddresses)) {
                  if (inetAddress != null && inetAddress.isSiteLocalAddress()) {
                    System.out.printf("Display name: %s\n", netint.getDisplayName());
                    System.out.printf("Name: %s\n", netint.getName());
                    System.out.printf("InetAddress: %s %d\n", inetAddress, 1);
                    addresses.add(inetAddress);
                  }
                }
                System.out.printf("\n");
            }



        } catch (Exception e) {
            println("GOT AN EXCEPTION", e);
        }


        Map<String, String> env = System.getenv();
        if (env.containsKey("COMPUTERNAME"))
            hostname = env.get("COMPUTERNAME");
        else if (env.containsKey("HOSTNAME"))
            hostname = env.get("HOSTNAME");
        else if (env.containsKey("LOGNAME"))
            hostname = env.get("LOGNAME");

        if (hostname == null) {
            hostname = "Unknown";
        }
        println("hostname: "+hostname);
        if (hostname != null) {
            serviceName += "@";
            serviceName += hostname;
        }
        try {
            String name = hostname != null ? hostname : "Unknown";
            String info = "name=" + name;


            for (InetAddress address : addresses) {
                JmDNS jmdns = JmDNS.create(address);
                ServiceInfo serviceInfo = ServiceInfo.create("_sugarcubes._tcp.local.", serviceName, 8723, info);
                jmdns.registerService(serviceInfo);

            }

            System.out.printf("mDNS service registered on %d interfaces", addresses.size());
        } catch (IOException e) {
            println("exception: "+e);
        }
    }

    void stop() {
        if (jmmdns == null) return;
        jmmdns.unregisterAllServices();
        println("mDNS service unregistered");
    }

    void startAsync() {
        new Thread(new Runnable() {
            public void run() {
                synchronized (ServerDiscovery.this) {
                    println("INSIDE THE THREAD");
                    // todo: handle closed dns exception
                    start();
                }
            }
        }).start();
    }

    void stopAsync() {
        new Thread(new Runnable() {
            public void run() {
                synchronized (ServerDiscovery.this) {
                    stop();
                }
            }
        }).start();
    }

}
