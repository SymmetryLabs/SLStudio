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
 * Distributed Display Protocol is a simple protocol developed by 3waylabs. It's
 * a simple framing of raw color buffers, without DMX size limitations.
 * 
 * The specification is available at http://www.3waylabs.com/ddp/
 */
public class DDPDatagram extends LXDatagram {

    private static final int HEADER_LENGTH = 10;
    private static final int DEFAULT_PORT = 4048;

    private final int[] pointIndices;

    public DDPDatagram(int[] pointIndices) {
        super(HEADER_LENGTH + pointIndices.length * 3);
        setPort(DEFAULT_PORT);
        int dataLen = pointIndices.length * 3;
        this.pointIndices = pointIndices;

        // Flags: V V x T S R Q P
        this.buffer[0] = 0x41;

        // Reserved
        this.buffer[1] = 0x00;

        // Data type
        this.buffer[2] = 0x00;

        // Destination ID, default
        this.buffer[3] = 0x01;

        // Data offset
        this.buffer[4] = 0x00;
        this.buffer[5] = 0x00;
        this.buffer[6] = 0x00;
        this.buffer[7] = 0x00;

        // Data length
        this.buffer[8] = (byte) (0xff & (dataLen >> 8));
        this.buffer[9] = (byte) (0xff & dataLen);
    }

    public void onSend(int[] colors) {
        copyPoints(colors, this.pointIndices, HEADER_LENGTH);
    }
}
