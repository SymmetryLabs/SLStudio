package com.symmetrylabs.network;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public abstract class AbstractVersionCommandCallback implements VersionCommandCallback {
    public void onResponse(java.net.DatagramPacket response, int version) {
    }

    public void onFinish() {
    }
}
