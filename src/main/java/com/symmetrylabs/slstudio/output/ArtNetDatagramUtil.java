package com.symmetrylabs.slstudio.output;

import heronarts.lx.output.LXDatagram;

public class ArtNetDatagramUtil {
    public final static int HEADER_LENGTH = 12;
    public final static int ARTNET_PORT = 6454;
    private static final String HEADER_MAGIC = "Art-Net";

    public static void fillHeader(byte[] buffer, short opCode) {
        for (int i = 0; i < HEADER_MAGIC.length(); i++) {
            buffer[i] = (byte) HEADER_MAGIC.charAt(i);
        }
        buffer[7] = 0;
        buffer[8] = (byte) (opCode & 0xFF); // ArtDMX opcode
        buffer[9] = (byte) ((opCode >> 8) & 0xFF); // ArtDMX opcode
        buffer[10] = 0; // Protcol version
        buffer[11] = 14; // Protcol version
        for (int i = HEADER_LENGTH; i < buffer.length; ++i) {
            buffer[i] = 0;
        }
    }

    public static boolean isArtNetPacket(byte[] buffer) {
        if (buffer.length < HEADER_LENGTH) {
            return false;
        }
        for (int i = 0; i < HEADER_MAGIC.length(); i++) {
            if (buffer[i] != HEADER_MAGIC.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    public static short getOpCode(byte[] buffer) {
        short lsb = buffer[8];
        short msb = buffer[9];
        return (short) (msb << 8 | lsb);
    }
}
