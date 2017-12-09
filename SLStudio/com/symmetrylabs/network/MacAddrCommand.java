package com.symmetrylabs.network;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public static class MacAddrCommand extends ControllerCommand {
    public MacAddrCommand(InetAddress addr, final MacAddrCommandCallback callback) {
        super(addr, new byte[]{(byte) 136, 4}, 6, new ControllerCommandCallback() {
            void onResponse(java.net.DatagramPacket response) {
                if (callback != null && response.getLength() == 6) {
                    callback.onResponse(response, response.getData());
                }
            }

            void onFinish() {
                if (callback != null) callback.onFinish();
            }
        });
    }
}
