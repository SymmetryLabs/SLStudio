package com.symmetrylabs.network;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
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
