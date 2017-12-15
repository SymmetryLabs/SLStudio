package com.symmetrylabs.slstudio.network;


public interface MacAddrCommandCallback {
    public void onResponse(java.net.DatagramPacket response, byte[] macAddr);

    public void onFinish();
}
