package com.symmetrylabs.slstudio.automapping;

import java.util.Map;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.net.InetAddress;
import javax.jmdns.JmDNS;
import javax.jmdns.JmmDNS;
import javax.jmdns.ServiceInfo;

import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.LXParameterListener;

public class ServerDiscovery {
    public static final int PORT = 8723;
    public static final String SUGARCUBES_SERVICE = "_sugarcubes._tcp.local.";

    private final ExecutorService EXEC_QUEUE = Executors.newSingleThreadExecutor();

    private JmDNS jmdns = null;
    //private final JmmDNS jmmdns = JmmDNS.Factory.getInstance();
    private String hostname;
    private InetAddress localAddress;

    ServerDiscovery() {
        //java.util.logging.Logger.getLogger("javax.jmdns").setLevel(Level.SEVERE);
        try {
            localAddress = InetAddress.getLocalHost();
            hostname = InetAddress.getLocalHost().getHostName();
            jmdns = JmDNS.create(localAddress);
        }
        catch (Exception e) {
            System.err.println("Error reading local address: " + e.getMessage());
            e.printStackTrace();
            return;
        }
    }

    public void start() {
        if (jmdns == null)
            return;

        EXEC_QUEUE.submit(() -> {
            if (hostname == null) {
                Map<String, String> env = System.getenv();
                if (env.containsKey("HOSTNAME")) {
                    hostname = env.get("HOSTNAME");
                }
                else if (env.containsKey("COMPUTERNAME")) {
                    hostname = env.get("COMPUTERNAME");
                }
                else if (env.containsKey("LOGNAME")) {
                    hostname = env.get("LOGNAME");
                }
            }

            if (hostname == null) {
                hostname = "Unknown";
            }

            String serviceName = "sugarcubes";
            if (hostname != null) {
                serviceName += "@" + hostname;
            }

            try {
                Map<String, Object> props = new HashMap<>();
                props.put("name", hostname);
                ServiceInfo serviceInfo = ServiceInfo.create(SUGARCUBES_SERVICE,
                        serviceName, PORT, 0, 0, props);
                jmdns.registerService(serviceInfo);
                //jmmdns.registerService(serviceInfo);

                System.out.println("mDNS service registered: " + serviceInfo.getQualifiedName());
            }
            catch (Exception e) {
                System.err.println("Error registering services: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    public void stop() {
        if (jmdns == null)
            return;

        EXEC_QUEUE.submit(() -> {
            try {
                //jmmdns.unregisterAllServices();
                jmdns.unregisterAllServices();
            }
            catch (Exception e) {
                System.err.println("Error unregistering services: " + e.getMessage());
                e.printStackTrace();
            }

            System.out.println("mDNS service unregistered");
        });
    }
}
