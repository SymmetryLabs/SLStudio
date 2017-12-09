package com.symmetrylabs.network;

import com.symmetrylabs.util.listenable.ListenableInt;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
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
