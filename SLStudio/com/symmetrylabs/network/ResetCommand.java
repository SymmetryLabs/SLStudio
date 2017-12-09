package com.symmetrylabs.network;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public static class ResetCommand extends ControllerCommand {
    public ResetCommand(InetAddress addr) {
        super(addr, new byte[]{(byte) 136, 2});
    }
}
