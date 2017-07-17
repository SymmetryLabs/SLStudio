import javax.jmdns.JmDNS;
import javax.jmdns.*;
import javax.jmdns.ServiceInfo;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;

class ServerDiscovery {

    final BooleanParameter enabled = new BooleanParameter("Server discovery enabled");

    String hostname = null;
    String serviceName = "sugarcubes";

    JmDNS jmdns = null;
    JmmDNS jmmdns = null;

    ServerDiscovery() {
        java.util.logging.Logger.getLogger("javax.jmdns").setLevel(Level.SEVERE);

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
        jmmdns = JmmDNS.Factory.getInstance();

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
            jmmdns.registerService(ServiceInfo.create("_sugarcubes._tcp.local.", serviceName, 8723, info));
            println("mDNS service registered");
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
