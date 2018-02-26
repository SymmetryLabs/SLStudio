package com.symmetrylabs.slstudio.network;

import java.net.InetAddress;

import com.symmetrylabs.slstudio.util.listenable.ListenableInt;

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
