package com.symmetrylabs.network;


public interface MacAddrCommandCallback {
    public void onResponse(java.net.DatagramPacket response, byte[] macAddr);

    public void onFinish();
}
