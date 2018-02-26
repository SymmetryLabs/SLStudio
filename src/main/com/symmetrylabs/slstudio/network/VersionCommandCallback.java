package com.symmetrylabs.slstudio.network;


public interface VersionCommandCallback {
    public void onResponse(java.net.DatagramPacket response, int version);

    public void onFinish();
}
