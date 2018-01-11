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

    private String hostname = null;
    private String serviceName = "sugarcubes";

    JmmDNS jmmdns = null;

    ServerDiscovery() {
        java.util.logging.Logger.getLogger("javax.jmdns").setLevel(Level.SEVERE);
    }

    public synchronized void start() {
        if (jmmdns != null)
            return;

        jmmdns = JmmDNS.Factory.getInstance();

        try {
            String name = hostname != null ? hostname : "Unknown";
            String info = "name=" + name;

            ServiceInfo serviceInfo = ServiceInfo.create("_sugarcubes._tcp.local.", serviceName, PORT, info);
            jmmdns.registerService(serviceInfo);

            System.out.printf("mDNS service registered on %d interfaces", jmmdns.getInterfaces().length);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void stop() {
        if (jmmdns == null)
            return;

        try {
            jmmdns.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        jmmdns = null;

        System.out.println("mDNS service unregistered");
    }
}
