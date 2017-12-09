package com.symmetrylabs.network;


public interface ControllerCommandCallback {
    public void onResponse(java.net.DatagramPacket response);

    public void onFinish();
}
