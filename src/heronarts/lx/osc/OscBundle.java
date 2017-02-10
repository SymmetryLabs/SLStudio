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

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class OscBundle extends OscPacket {

    private final static byte[] BUNDLE_HEADER = {
        '#', 'b', 'u', 'n', 'd', 'l', 'e', 0
    };

    private long timeTag;
    private List<OscMessage> messages;

    public OscBundle() {
        this.messages = new ArrayList<OscMessage>();
    }

    public List<OscMessage> getMessages() {
        return this.messages;
    }

    public OscBundle addMessage(OscMessage message) {
        this.messages.add(message);
        return this;
    }

    public long getTimeTag() {
        return this.timeTag;
    }

    public OscBundle setTimeTag(long timeTag) {
        this.timeTag = timeTag;
        return this;
    }

    public static OscBundle parse(byte[] data, int offset, int len) throws OscException {
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
            int messageLength = buffer.getInt(offset);
            offset += 4;
            bundle.addMessage(OscMessage.parse(data, offset, offset + messageLength));
            offset += messageLength;
        }
        return bundle;
    }
}
