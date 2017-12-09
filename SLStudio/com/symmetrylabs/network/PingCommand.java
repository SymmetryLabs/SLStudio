package com.symmetrylabs.network;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
static class PingCommand extends ControllerCommand {
    PingCommand(InetAddress addr, final ControllerCommandCallback callback) {
        super(addr, new byte[]{(byte) 136, 1}, 1, new ControllerCommandCallback() {
            void onResponse(java.net.DatagramPacket response) {
                if (callback != null && response.getLength() == 1 && response.getData()[0] == 1) {
                    callback.onResponse(response);
                }
            }

            void onFinish() {
                if (callback != null) callback.onFinish();
            }
        });
    }
}
