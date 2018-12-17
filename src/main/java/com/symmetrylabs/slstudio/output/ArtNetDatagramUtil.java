package com.symmetrylabs.slstudio.output;

import heronarts.lx.output.LXDatagram;

public class ArtNetDatagramUtil {
    public final static int HEADER_LENGTH = 12;
    public final static int ARTNET_PORT = 6454;

    public static void fillHeader(byte[] buffer, short opCode) {
        buffer[0] = 'A';
        buffer[1] = 'r';
        buffer[2] = 't';
        buffer[3] = '-';
        buffer[4] = 'N';
        buffer[5] = 'e';
        buffer[6] = 't';
        buffer[7] = 0;
        buffer[8] = (byte) (opCode & 0xFF); // ArtDMX opcode
        buffer[9] = (byte) ((opCode >> 8) & 0xFF); // ArtDMX opcode
        buffer[10] = 0; // Protcol version
        buffer[11] = 14; // Protcol version
        for (int i = HEADER_LENGTH; i < buffer.length; ++i) {
            buffer[i] = 0;
        }
    }
}
