package com.symmetrylabs.network;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public interface VersionCommandCallback {
    public void onResponse(java.net.DatagramPacket response, int version);

    public void onFinish();
}
