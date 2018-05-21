package com.symmetrylabs.util;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class NetworkUtils {
    public static String macAddrToString(byte[] addr) {
        String[] hexBytes = new String[addr.length];
        for (int i = 0; i < addr.length; i++) {
            hexBytes[i] = String.format("%02x", addr[i]);
        }
        return String.join(":", hexBytes);
    }

    public static InetAddress toInetAddress(String host) {
        try {
            return InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isValidMacAddr(byte[] macAddr) {
        for (byte b : macAddr) {
            if (b == (byte) 0xff) return false;
        }
        return true;
    }

    public static List<InetAddress> getBroadcastAddresses() {
        List<InetAddress> addresses = new ArrayList<InetAddress>();
        try {
            for (NetworkInterface iface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                for (InterfaceAddress addr : iface.getInterfaceAddresses()) {
                    if (addr.getBroadcast() != null) {
                        addresses.add(addr.getBroadcast());
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return addresses;
    }

    public static List<InetAddress> getInetAddresses() {
        List<InetAddress> addresses = new ArrayList<InetAddress>();
        try {
            for (NetworkInterface iface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                for (InterfaceAddress addr : iface.getInterfaceAddresses()) {
                    if (addr.getAddress() != null) {
                        addresses.add(addr.getAddress());
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return addresses;
    }
}
