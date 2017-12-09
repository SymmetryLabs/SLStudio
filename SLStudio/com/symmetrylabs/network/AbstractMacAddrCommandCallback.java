package com.symmetrylabs.network;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public static abstract class AbstractMacAddrCommandCallback implements MacAddrCommandCallback {
    public void onResponse(java.net.DatagramPacket response, byte[] macAddr) {
    }

    public void onFinish() {
    }
}
