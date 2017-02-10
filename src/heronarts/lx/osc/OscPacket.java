/**
 * Copyright 2017- Mark C. Slee, Heron Arts LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
