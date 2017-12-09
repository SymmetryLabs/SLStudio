package com.symmetrylabs.network;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public static interface MacAddrCommandCallback {
    public void onResponse(java.net.DatagramPacket response, byte[] macAddr);

    public void onFinish();
}
