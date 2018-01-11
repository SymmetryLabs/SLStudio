package com.symmetrylabs.slstudio.automapping;

import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.LXParameterListener;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.*;
import java.util.logging.Level;
import javax.jmdns.JmDNS;
import javax.jmdns.JmmDNS;
import javax.jmdns.ServiceInfo;

public class ServerDiscovery {
    public static final int PORT = 8723;

    public static final String SUGARCUBES_SERVICE = "_sugarcubes._tcp.local.";

    private String hostname = null;
    private String serviceName = "sugarcubes";

    List<JmDNS> jmdnsList = new ArrayList<>();

    ServerDiscovery() {
        java.util.logging.Logger.getLogger("javax.jmdns").setLevel(Level.SEVERE);
    }

    public synchronized void start() {
        stop();

        List<InetAddress> addresses = new ArrayList<>();

        System.out.println("Discovering network interfaces for mDNS");

        try {
            Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();

            for (NetworkInterface netint : Collections.list(nets)) {
                Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();

                for (InetAddress inetAddress : Collections.list(inetAddresses)) {
                  if (inetAddress == null || !inetAddress.isSiteLocalAddress())
                        continue;

                    System.out.println("Display name: " + netint.getDisplayName());
                    System.out.println("Name: " + netint.getName());
                    System.out.println("InetAddress: " + inetAddress);

                    addresses.add(inetAddress);
                }

                System.out.println();
            }
        }
        catch (Exception e) {
            System.err.println("Caught error while enumerating interfaces: " + e.getMessage());
        }

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

        if (hostname == null) {
            hostname = "Unknown";
        }

        System.out.println("Hostname: " + hostname);

        if (hostname != null) {
            serviceName += "@" + hostname;
        }

        try {
            String info = "name=" + hostname;
            ServiceInfo serviceInfo = ServiceInfo.create(SUGARCUBES_SERVICE, serviceName, PORT, info);

            for (InetAddress address : addresses) {
                try {
                    JmDNS jmdns = JmDNS.create(address);
                    jmdns.registerService(serviceInfo);
                    jmdnsList.add(jmdns);
                }
                catch (Exception e) {
                    System.err.println("Caught error while registering mDNS service on address"
                            + " (" + address + "): " + e.getMessage());
                }
            }

            System.out.printf("mDNS service registered on %d interfaces\n", jmdnsList.size());
        }
        catch (Exception e) {
            System.err.println("Caught error while registering mDNS services: " + e.getMessage());
        }
    }

    public synchronized void stop() {
        if (jmdnsList.isEmpty())
            return;

        for (JmDNS jmdns : jmdnsList) {
            try {
                jmdns.close();
            }
            catch (Exception e) {
                System.err.println("Caught error while closing JmDNS: " + e.getMessage());
            }
        }

        jmdnsList.clear();

        System.out.println("mDNS service unregistered");
    }
}
