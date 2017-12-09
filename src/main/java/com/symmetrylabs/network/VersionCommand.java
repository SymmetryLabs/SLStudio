package com.symmetrylabs.network;

import java.net.InetAddress;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class VersionCommand extends ControllerCommand {
    public VersionCommand(InetAddress addr, final VersionCommandCallback callback) {
        super(addr, new byte[]{(byte) 136, 3}, 1, new ControllerCommandCallback() {
            public void onResponse(java.net.DatagramPacket response) {
                if (callback != null && response.getLength() == 1) {
                    callback.onResponse(response, response.getData()[0]);
                }
            }

            public void onFinish() {
                if (callback != null) callback.onFinish();
            }
        });
    }
}
