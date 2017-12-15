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

import heronarts.lx.parameter.BooleanParameter;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;

public abstract class LXDatagram {

    LXDatagramDestination destination;

    /**
     * Various orderings for RGB buffer data
     */
    public enum ByteOrder {
        RGB, RBG, GRB, GBR, BRG, BGR,
    };

    /**
     * Note that the order here MUST match the order specified above
     */
    protected static final int[][] BYTE_ORDERING = {
        // R G B
        { 0, 1, 2 }, // RGB
        { 0, 2, 1 }, // RBG
        { 1, 0, 2 }, // GRB
        { 2, 0, 1 }, // GBR
        { 1, 2, 0 }, // BRG
        { 2, 1, 0 }, // BGR
    };

    protected ByteOrder byteOrder = ByteOrder.RGB;

    protected final byte[] buffer;

    final DatagramPacket packet;

    /**
     * Whether this datagram is active
     */
    public final BooleanParameter enabled = new BooleanParameter("ON", true);

    protected LXDatagram(int bufferSize) {
        this.buffer = new byte[bufferSize];
        for (int i = 0; i < bufferSize; ++i) {
            this.buffer[i] = 0;
        }
        this.packet = new DatagramPacket(this.buffer, bufferSize);
    }

    /**
     * Sets the byte ordering of data in this datagram buffer
     *
     * @param byteOrder Byte ordering
     * @return this
     */
    public LXDatagram setByteOrder(ByteOrder byteOrder) {
        this.byteOrder = byteOrder;
        return this;
    }

    /**
     * Sets the destination address of this datagram
     *
     * @param ipAddress IP address or hostname as string
     * @return this
     * @throws UnknownHostException Bad address
     */
    public LXDatagram setAddress(String ipAddress) throws UnknownHostException {
        this.packet.setAddress(InetAddress.getByName(ipAddress));
        return this;
    }

    /**
     * Sets the destination address of this datagram
     *
     * @param address Destination address
     * @return this
     */
    public LXDatagram setAddress(InetAddress address) {
        this.packet.setAddress(address);
        return this;
    }

    /**
     * Gets the address this datagram sends to
     *
     * @return Destination address
     */
    public InetAddress getAddress() {
        return this.packet.getAddress();
    }

    /**
     * Sets the destination port number to send this datagram on
     *
     * @param port Port number
     * @return this
     */
    public LXDatagram setPort(int port) {
        this.packet.setPort(port);
        return this;
    }

    /**
     * Helper for subclasses to copy a list of points into the data buffer at a
     * specified offset. For many subclasses which wrap RGB buffers, onSend() will
     * be a simple call to this method with the right parameters.
     *
     * @param colors Array of color values
     * @param pointIndices Array of point indices
     * @param offset Offset in buffer to write
     * @return this
     */
    protected LXDatagram copyPoints(int[] colors, int[] pointIndices, int offset) {
        int i = offset;
        int[] byteOffset = BYTE_ORDERING[this.byteOrder.ordinal()];
        for (int index : pointIndices) {
            int color = (index >= 0) ? colors[index] : 0;
            this.buffer[i + byteOffset[0]] = (byte) ((color >> 16) & 0xff); // R
            this.buffer[i + byteOffset[1]] = (byte) ((color >> 8) & 0xff); // G
            this.buffer[i + byteOffset[2]] = (byte) (color & 0xff); // B
            i += 3;
        }
        return this;
    }

    /**
     * Invoked by engine to send this packet when new color data is available. The
     * LXDatagram should update the packet object accordingly to contain the
     * appropriate buffer.
     *
     * @param colors Color buffer
     */
    public abstract void onSend(int[] colors);
}
