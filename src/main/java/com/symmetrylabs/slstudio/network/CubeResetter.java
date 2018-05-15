package com.symmetrylabs.slstudio.network;

import com.symmetrylabs.util.NetworkUtils;

import java.net.InetAddress;


public class CubeResetter {
    void run() {
        for (InetAddress addr : NetworkUtils.getBroadcastAddresses()) {
            new ResetCommand(addr);
        }
    }
}
