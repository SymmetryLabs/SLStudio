package com.symmetrylabs.slstudio.output;

import java.net.DatagramPacket;
import java.net.InetAddress;

public class ArtNetPollDatagram {
    private static final short ARTNET_POLL_OPCODE = 0x2000;
    private static final int LENGTH = ArtNetDatagramUtil.HEADER_LENGTH + 2;
    private final DatagramPacket packet;

    public ArtNetPollDatagram(InetAddress addr) {
        byte[] buf = new byte[LENGTH];

        packet = new DatagramPacket(buf, LENGTH);
        packet.setAddress(addr);
        packet.setPort(ArtNetDatagramUtil.ARTNET_PORT);

        ArtNetDatagramUtil.fillHeader(buf, ARTNET_POLL_OPCODE);

        // This byte is the TalkToMe options bitfield; this value means do not send
        // diagnostic messages, do not send poll-replys when your parameters change,
        // do not use visible light communication channels
        buf[ArtNetDatagramUtil.HEADER_LENGTH + 0] = 0x10;

        // This byte tells nodes what kind of diagnostic messages we want to receive;
        // since we already said we don't want any in TalkToMe it will be ignored.
        buf[ArtNetDatagramUtil.HEADER_LENGTH + 1] = 0x00;
    }

    public DatagramPacket getPacket() {
        return packet;
    }
}
