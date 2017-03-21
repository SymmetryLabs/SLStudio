/**
 * Copyright 2017- Mark C. Slee, Heron Arts LLC
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

package heronarts.lx.osc;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;

public abstract class OscPacket {

    public static OscPacket parse(InetAddress source, byte[] data, int offset, int len) throws OscException {
        if (data == null) {
            throw new IllegalArgumentException("OscPacket cannot parse null data array");
        }
        if (len <= 0) {
            throw new OscEmptyPacketException();
        }
        if (data[offset] == '#') {
            return OscBundle.parse(source, data, offset, len);
        } else if (data[offset] == '/') {
            return OscMessage.parse(source,data, offset, len);
        } else {
            throw new OscMalformedDataException("Osc Packet does not start with # or /", data, offset, len);
        }
    }

    public static OscPacket parse(DatagramPacket datagram) throws OscException {
        return OscPacket.parse(datagram.getAddress(), datagram.getData(), datagram.getOffset(), datagram.getLength());
    }

    abstract void serialize(ByteBuffer buffer);
}
