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

import heronarts.lx.LX;

public class OPCOutput extends LXSocketOutput {

    static final int HEADER_LEN = 4;

    static final int BYTES_PER_PIXEL = 3;

    static final int INDEX_CHANNEL = 0;
    static final int INDEX_COMMAND = 1;
    static final int INDEX_DATA_LEN_MSB = 2;
    static final int INDEX_DATA_LEN_LSB = 3;
    static final int INDEX_DATA = 4;

    static final int OFFSET_R = 0;
    static final int OFFSET_G = 1;
    static final int OFFSET_B = 2;

    static final int COMMAND_SET_PIXEL_COLORS = 0;

    private final byte[] packetData;

    private final int[] pointIndices;

    private static int[] allPoints(LX lx) {
        int[] points = new int[lx.total];
        for (int i = 0; i < points.length; ++i) {
            points[i] = i;
        }
        return points;
    }

    public OPCOutput(LX lx, String host, int port) {
        this(lx, host, port, allPoints(lx));
    }

    public OPCOutput(LX lx, String host, int port, int[] pointIndices) {
        super(lx, host, port);
        this.pointIndices = pointIndices;

        int dataLength = BYTES_PER_PIXEL*pointIndices.length;
        this.packetData = new byte[HEADER_LEN + dataLength];
        this.packetData[INDEX_CHANNEL] = 0;
        this.packetData[INDEX_COMMAND] = COMMAND_SET_PIXEL_COLORS;
        this.packetData[INDEX_DATA_LEN_MSB] = (byte)(dataLength >>> 8);
        this.packetData[INDEX_DATA_LEN_LSB] = (byte)(dataLength & 0xFF);
    }

    @Override
    protected byte[] getPacketData(int[] colors) {
        for (int i = 0; i < this.pointIndices.length; ++i) {
            int dataOffset = INDEX_DATA + i * BYTES_PER_PIXEL;
            int c = colors[this.pointIndices[i]];
            this.packetData[dataOffset + OFFSET_R] = (byte) (0xFF & (c >> 16));
            this.packetData[dataOffset + OFFSET_G] = (byte) (0xFF & (c >> 8));
            this.packetData[dataOffset + OFFSET_B] = (byte) (0xFF & c);
        }
        return this.packetData;
    }

    public OPCOutput setChannel(int channel) {
        this.packetData[INDEX_CHANNEL] = (byte) channel;
        return this;
    }

}
