package com.symmetrylabs.slstudio.network;

public class OpcSysExMsg extends OpcMessage {
    byte channel;
    byte command;
    byte length_h;
    byte length_l;
    byte system_id_h;
    byte system_id_l;
    byte command_h;
    byte command_l;
    byte payload[];

    public OpcSysExMsg(int channel, int systemId, int sysexCode, byte[] sysexContent) {
        super(channel, systemId, sysexCode, sysexContent);
    }

    public OpcSysExMsg(byte[] data, int length) {
        super(data, length);
        channel = data[0];
        command = data[1];
        length_h = data[2];
        length_l = data[3];

        if (command != 0xff){
            System.out.println("NOT SYSEX");
        }

        system_id_h = data[4];
        system_id_l = data[5];
        command_h = data[6];
        command_l = data[8];
        payload   = new byte[getLength()];

    }

    private int getLength() {
        return length_h << 8 | length_l & 0xff;
    }
}
