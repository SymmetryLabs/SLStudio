package com.symmetrylabs.slstudio.network;

import java.net.InetAddress;


public class CubeResetter {
    void run() {
        for (InetAddress addr : NetworkInfo.getBroadcastAddresses()) {
            new ResetCommand(addr);
        }
    }
}
