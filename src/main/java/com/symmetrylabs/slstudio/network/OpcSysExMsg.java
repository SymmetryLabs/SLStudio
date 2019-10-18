package com.symmetrylabs.slstudio.network;

public class OpcSysExMsg extends OpcMessage {
    public OpcSysExMsg(int channel, int systemId, int sysexCode, byte[] sysexContent) {
        super(channel, systemId, sysexCode, sysexContent);
    }
}
