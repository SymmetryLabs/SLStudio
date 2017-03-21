/**
 * Copyright 2013- Mark C. Slee, Heron Arts LLC
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * @author Mark C. Slee <mark@heronarts.com>
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
