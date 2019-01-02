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

import heronarts.lx.PolyBuffer;
import heronarts.lx.model.LXFixture;

import static heronarts.lx.PolyBuffer.Space.RGB16;
import static heronarts.lx.PolyBuffer.Space.RGB8;

/**
 * UDP implementation of http://openpixelcontrol.org/
 */
public class OPCDatagram extends LXDatagram implements OPCConstants {

    protected final int[] indices;
    protected byte channel = CHANNEL_BROADCAST;
    protected boolean is16BitColorEnabled = false;
    protected byte[] buffer16;

    public OPCDatagram(LXFixture fixture) {
        this(fixture, CHANNEL_BROADCAST);
    }

    public OPCDatagram(LXFixture fixture, byte channel) {
        this(LXOutput.fixtureToIndices(fixture), channel);
    }

    public OPCDatagram(int[] indices) {
        this(indices, CHANNEL_BROADCAST);
    }

    public OPCDatagram(int[] indices, byte channel) {
        super(HEADER_LEN + BYTES_PER_PIXEL * indices.length);
        this.indices = indices;
        this.channel = channel;

        int dataLength = BYTES_PER_PIXEL * indices.length;
        this.buffer[INDEX_CHANNEL] = channel;
        this.buffer[INDEX_COMMAND] = COMMAND_SET_PIXEL_COLORS;
        this.buffer[INDEX_DATA_LEN_MSB] = (byte)(dataLength >>> 8);
        this.buffer[INDEX_DATA_LEN_LSB] = (byte)(dataLength & 0xFF);
    }

    public void set16BitColorEnabled(boolean enable) {
        is16BitColorEnabled = enable;
    }

    public OPCDatagram setChannel(byte channel) {
        this.channel = channel;
        return this;
    }

    public byte getChannel() {
        return channel;
    }

    @Override
    public void onSend(PolyBuffer src) {
        if (is16BitColorEnabled && src.isFresh(RGB16)) {
            int dataLength = BYTES_PER_16BIT_PIXEL * indices.length;
            if (buffer16 == null) {
                buffer16 = new byte[HEADER_LEN + dataLength];
            }
            buffer16[INDEX_CHANNEL] = channel;
            buffer16[INDEX_COMMAND] = COMMAND_SET_16BIT_PIXEL_COLORS;
            buffer16[INDEX_DATA_LEN_MSB] = (byte) (dataLength >>> 8);
            buffer16[INDEX_DATA_LEN_LSB] = (byte) (dataLength & 0xFF);
            copyPoints16((long[]) src.getArray(RGB16), indices, buffer16, INDEX_DATA);
            packet.setData(buffer16);
            packet.setLength(buffer16.length);
        } else {
            buffer[INDEX_CHANNEL] = channel;
            copyPoints((int[]) src.getArray(RGB8), indices, INDEX_DATA);
            packet.setData(buffer);
            packet.setLength(buffer.length);
        }
    }
}
