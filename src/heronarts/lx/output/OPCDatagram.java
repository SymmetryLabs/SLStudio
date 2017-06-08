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

import heronarts.lx.model.LXFixture;

/**
 * UDP implementation of http://openpixelcontrol.org/
 */
public class OPCDatagram extends LXDatagram implements OPCConstants {

    private final int[] indices;

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
        super(OPCOutput.HEADER_LEN + OPCOutput.BYTES_PER_PIXEL * indices.length);
        this.indices = indices;
        int dataLength = BYTES_PER_PIXEL * indices.length;
        this.buffer[INDEX_CHANNEL] = channel;
        this.buffer[INDEX_COMMAND] = COMMAND_SET_PIXEL_COLORS;
        this.buffer[INDEX_DATA_LEN_MSB] = (byte)(dataLength >>> 8);
        this.buffer[INDEX_DATA_LEN_LSB] = (byte)(dataLength & 0xFF);
    }

    public OPCDatagram setChannel(byte channel) {
        this.buffer[INDEX_CHANNEL] = channel;
        return this;
    }

    @Override
    public void onSend(int[] colors) {
        copyPoints(colors, this.indices, INDEX_DATA);
    }

}
