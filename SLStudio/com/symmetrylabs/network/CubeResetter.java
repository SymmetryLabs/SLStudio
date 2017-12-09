package com.symmetrylabs.network;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
static class CubeResetter {
    void run() {
        for (InetAddress addr : NetworkInfo.getBroadcastAddresses()) {
            new ResetCommand(addr);
        }
    }
}
