package com.symmetrylabs.network;

import java.net.InetAddress;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class PingCommand extends ControllerCommand {
    PingCommand(InetAddress addr, final ControllerCommandCallback callback) {
        super(addr, new byte[]{(byte) 136, 1}, 1, new ControllerCommandCallback() {
            public void onResponse(java.net.DatagramPacket response) {
                if (callback != null && response.getLength() == 1 && response.getData()[0] == 1) {
                    callback.onResponse(response);
                }
            }

            public void onFinish() {
                if (callback != null) callback.onFinish();
            }
        });
    }
}
