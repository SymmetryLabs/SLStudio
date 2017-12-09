package com.symmetrylabs.network;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public interface ControllerCommandCallback {
    public void onResponse(java.net.DatagramPacket response);

    public void onFinish();
}
