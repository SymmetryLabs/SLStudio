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

public abstract class OscPacket {

    static OscPacket parse(byte[] data, int offset, int len) throws OscException {
        if (data == null) {
            throw new IllegalArgumentException();
        }
        if (len <= 0) {
            throw new OscEmptyPacketException();
        }
        if (data[0] == '#') {
            return OscBundle.parse(data, offset, len);
        } else if (data[0] == '/') {
            return OscMessage.parse(data, offset, len);
        } else {
            throw new OscMalformedDataException("Osc Packet does not start with # or /", data, offset, len);
        }
    }

    static OscPacket parse(byte[] data, int len) throws OscException {
        return OscPacket.parse(data, 0, len);
    }

    static OscPacket parse(DatagramPacket datagram) throws OscException {
        return OscPacket.parse(datagram.getData(), datagram.getLength());
    }
}
