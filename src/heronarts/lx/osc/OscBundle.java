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

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class OscBundle extends OscPacket implements Iterable<OscMessage> {

    private final static byte[] BUNDLE_HEADER = {
        '#', 'b', 'u', 'n', 'd', 'l', 'e', 0
    };

    private long timeTag = OscTimeTag.NOW;

    private final List<OscPacket> elements = new ArrayList<OscPacket>();

    public OscBundle() {}

    public List<OscPacket> getElements() {
        return this.elements;
    }

    public OscBundle addElement(OscPacket packet) {
        this.elements.add(packet);
        return this;
    }

    public long getTimeTag() {
        return this.timeTag;
    }

    public OscBundle setTimeTag(long timeTag) {
        this.timeTag = timeTag;
        return this;
    }

    public static OscBundle parse(InetAddress source, byte[] data, int offset, int len) throws OscException {
        for (int i = 0; i < BUNDLE_HEADER.length; ++i) {
            if (data[offset+i] != BUNDLE_HEADER[i]) {
                throw new OscMalformedDataException("Missing #bundle header in OscBundle", data, offset, len);
            }
        }
        offset += BUNDLE_HEADER.length;
        OscBundle bundle = new OscBundle();
        ByteBuffer buffer = ByteBuffer.wrap(data);
        bundle.setTimeTag(buffer.getLong(offset));
        offset += 8;
        while (offset < len) {
            int packetLength = buffer.getInt(offset);
            offset += 4;
            bundle.addElement(OscPacket.parse(source, data, offset, offset + packetLength));
            offset += packetLength;
        }
        return bundle;
    }

    @Override
    public Iterator<OscMessage> iterator() {
        List<OscMessage> messages = new ArrayList<OscMessage>(this.elements.size());
        flattenMessages(messages, this);
        return messages.iterator();
    }

    private static void flattenMessages(List<OscMessage> messages, OscBundle bundle) {
        for (int i = 0; i < bundle.elements.size(); ++i) {
            OscPacket element = bundle.elements.get(i);
            if (element instanceof OscMessage) {
                messages.add((OscMessage) element);
            } else if (element instanceof OscBundle) {
                flattenMessages(messages, (OscBundle) element);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (OscMessage message : this) {
            sb.append(message.toString());
            sb.append('\n');
        }
        return sb.toString();
    }

    @Override
    void serialize(ByteBuffer buffer) {
        buffer.put(BUNDLE_HEADER);
        buffer.putLong(this.timeTag);
        for (OscPacket packet : this.elements) {
            int sizePosition = buffer.position();
            buffer.position(sizePosition + 4);
            packet.serialize(buffer);
            buffer.putInt(sizePosition, buffer.position() - sizePosition + 4);
        }
    }
}
