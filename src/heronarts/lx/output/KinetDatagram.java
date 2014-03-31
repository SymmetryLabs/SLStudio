/**
 * ##library.name##
 * ##library.sentence##
 * ##library.url##
 *
 * Copyright ##copyright## ##author##
 * All Rights Reserved
 * 
 * @author      ##author##
 * @modified    ##date##
 * @version     ##library.prettyVersion## (##library.version##)
 */

package heronarts.lx.output;

/**
 * A datagram implementing the Kinet protocol, used by Color Kinetics devices.
 * These datagrams have a header followed by 512 bytes of color data. A port
 * number on the output device is specified, distinct from the UDP port. For
 * instance, an sPDS-480 has 16 outputs.
 */
public class KinetDatagram extends LXDatagram {

    private final static int HEADER_LENGTH = 24;
    private final static int DATA_LENGTH = 512;
    private final static int PACKET_LENGTH = HEADER_LENGTH + DATA_LENGTH;

    private final static int KINET_PORT = 6038;

    private final int[] pointIndices;

    /**
     * Constructs a datagram that sends on the given kinet supply output port
     * 
     * @param kinetPort Number of the output port on the kinet power supply
     * @param indices A list of the point indices that should be sent on this port
     */
    public KinetDatagram(int kinetPort, int[] indices) {
        super(PACKET_LENGTH);
        setPort(KINET_PORT);

        this.pointIndices = indices;

        // Kinet Header
        this.buffer[0] = (byte) 0x04;
        this.buffer[1] = (byte) 0x01;
        this.buffer[2] = (byte) 0xdc;
        this.buffer[3] = (byte) 0x4a;
        this.buffer[4] = (byte) 0x01;
        this.buffer[5] = (byte) 0x00;
        this.buffer[6] = (byte) 0x08;
        this.buffer[7] = (byte) 0x01;
        this.buffer[8] = (byte) 0x00;
        this.buffer[9] = (byte) 0x00;
        this.buffer[10] = (byte) 0x00;
        this.buffer[11] = (byte) 0x00;
        this.buffer[12] = (byte) 0xff;
        this.buffer[13] = (byte) 0xff;
        this.buffer[14] = (byte) 0xff;
        this.buffer[15] = (byte) 0xff;

        // Port number
        this.buffer[16] = (byte) kinetPort;

        // Maybe a checksum? 0x00 works fine
        this.buffer[17] = (byte) 0x00;
        this.buffer[18] = (byte) 0x00;
        this.buffer[19] = (byte) 0x00;
        this.buffer[20] = (byte) 0x00;

        // Total # of ports on controller? (irrelevant)
        this.buffer[21] = (byte) 0x02;

        // Unused
        this.buffer[22] = (byte) 0x00;
        this.buffer[23] = (byte) 0x00;

    }

    public void onSend(int[] colors) {
        copyPoints(colors, this.pointIndices, HEADER_LENGTH);
    }
}
