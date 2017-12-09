package com.symmetrylabs.network;

import com.symmetrylabs.util.listenable.ListenableInt;

import java.net.InetAddress;


public class NetworkDevice {

    public final InetAddress ipAddress;
    public final byte[] macAddress;
    public final ListenableInt version = new ListenableInt(-1);

    public int connectionRetries = 0;

    public NetworkDevice(InetAddress ipAddress, byte[] macAddress) {
        this.ipAddress = ipAddress;
        this.macAddress = macAddress;
    }

}
