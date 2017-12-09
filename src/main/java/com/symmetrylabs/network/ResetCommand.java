package com.symmetrylabs.network;

import java.net.InetAddress;

/**
 * @author Yona Appletree (yona@concentricsky.com)
 */
public class ResetCommand extends ControllerCommand {
    public ResetCommand(InetAddress addr) {
        super(addr, new byte[]{(byte) 136, 2});
    }
}
