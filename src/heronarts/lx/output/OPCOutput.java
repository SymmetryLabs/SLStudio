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
import heronarts.lx.PolyBuffer;
import heronarts.lx.color.LXColor;
import heronarts.lx.color.LXColor16;
import heronarts.lx.model.LXFixture;

/**
 * TCP/IP streaming socket implementation of http://openpixelcontrol.org/
 */
public class OPCOutput extends LXSocketOutput implements OPCConstants {

    protected final int[] pointIndices;
    protected byte[] packetData;
    protected byte channel = CHANNEL_BROADCAST;
    protected boolean is16BitColorEnabled = false;

    protected static int[] allPoints(LX lx) {
        int[] points = new int[lx.total];
        for (int i = 0; i < points.length; ++i) {
            points[i] = i;
        }
        return points;
    }

    public OPCOutput(LX lx, String host, int port) {
        this(lx, host, port, allPoints(lx));
    }

    public OPCOutput(LX lx, String host, int port, LXFixture fixture) {
        this(lx, host, port, LXOutput.fixtureToIndices(fixture));
    }

    public OPCOutput(LX lx, String host, int port, int[] pointIndices) {
        super(lx, host, port);
        this.pointIndices = pointIndices;
        }

    public void set16BitColorEnabled(boolean enable) {
        is16BitColorEnabled = enable;
    }

    @Override
    protected byte[] getPacketData(PolyBuffer src) {
        boolean send16 = is16BitColorEnabled && src.isFresh(PolyBuffer.Space.RGB16);
        byte command = send16 ? COMMAND_SET_16BIT_PIXEL_COLORS : COMMAND_SET_PIXEL_COLORS;
        int dataLength = (send16 ? BYTES_PER_16BIT_PIXEL : BYTES_PER_PIXEL) * pointIndices.length;
        if (packetData == null || packetData.length != HEADER_LEN + dataLength) {
            packetData = new byte[HEADER_LEN + dataLength];
        }
        fillPacketHeader(channel, command, dataLength);
        int p = INDEX_DATA;
        if (send16) {
            long[] srcLongs = (long[]) src.getArray(PolyBuffer.Space.RGB16);
            for (int index : pointIndices) {
                long c = srcLongs[index];
                int red = LXColor16.red(c);
                int green = LXColor16.green(c);
                int blue = LXColor16.blue(c);
                packetData[p++] = (byte) (red >>> 8);
                packetData[p++] = (byte) (red & 0xff);
                packetData[p++] = (byte) (green >>> 8);
                packetData[p++] = (byte) (green & 0xff);
                packetData[p++] = (byte) (blue >>> 8);
                packetData[p++] = (byte) (blue & 0xff);
            }
        } else {
            int[] srcInts = (int[]) src.getArray(PolyBuffer.Space.RGB8);
            for (int index : pointIndices) {
                int c = srcInts[index];
                packetData[p++] = LXColor.red(c);
                packetData[p++] = LXColor.green(c);
                packetData[p++] = LXColor.blue(c);
            }
        }
        return packetData;
    }

    protected void fillPacketHeader(byte channel, byte command, int dataLength) {
        packetData[INDEX_CHANNEL] = channel;
        packetData[INDEX_COMMAND] = command;
        packetData[INDEX_DATA_LEN_MSB] = (byte) (dataLength >>> 8);
        packetData[INDEX_DATA_LEN_LSB] = (byte) (dataLength & 0xFF);
    }

    public OPCOutput setChannel(byte channel) {
        this.channel = channel;
        return this;
    }
}
