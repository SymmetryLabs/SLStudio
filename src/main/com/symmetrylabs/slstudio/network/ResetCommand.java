package com.symmetrylabs.slstudio.network;

import java.net.InetAddress;


public class ResetCommand extends ControllerCommand {
    public ResetCommand(InetAddress addr) {
        super(addr, new byte[]{(byte) 136, 2});
    }
}
