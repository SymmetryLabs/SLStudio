package com.symmetrylabs.network;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public interface MacAddrCommandCallback {
    public void onResponse(java.net.DatagramPacket response, byte[] macAddr);

    public void onFinish();
}
