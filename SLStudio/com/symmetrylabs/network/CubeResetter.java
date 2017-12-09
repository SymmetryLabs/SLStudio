package com.symmetrylabs.network;

import java.net.InetAddress;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class CubeResetter {
    void run() {
        for (InetAddress addr : NetworkInfo.getBroadcastAddresses()) {
            new ResetCommand(addr);
        }
    }
}
