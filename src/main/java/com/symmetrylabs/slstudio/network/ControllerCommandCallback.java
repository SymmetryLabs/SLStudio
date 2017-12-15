package com.symmetrylabs.slstudio.network;


public interface ControllerCommandCallback {
    public void onResponse(java.net.DatagramPacket response);

    public void onFinish();
}
