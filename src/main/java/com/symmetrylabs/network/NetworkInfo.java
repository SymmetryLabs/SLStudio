package com.symmetrylabs.network;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;


public class NetworkInfo {
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
