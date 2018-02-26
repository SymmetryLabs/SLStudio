package com.symmetrylabs.slstudio.network;

import java.net.InetAddress;


public class MacAddrCommand extends ControllerCommand {
    public MacAddrCommand(InetAddress addr, final MacAddrCommandCallback callback) {
        super(addr, new byte[]{(byte) 136, 4}, 6, new ControllerCommandCallback() {
            public void onResponse(java.net.DatagramPacket response) {
                if (callback != null && response.getLength() == 6) {
                    callback.onResponse(response, response.getData());
                }
            }

            public void onFinish() {
                if (callback != null) callback.onFinish();
            }
        });
    }
}
