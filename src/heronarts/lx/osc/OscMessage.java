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

public class OscMessage extends OscPacket {
    private OscString addressPattern;
    private OscString typeTag;

    private List<OscArgument> arguments;

    public OscMessage() {
        this.arguments = new ArrayList<OscArgument>();
    }

    public OscMessage addArgument(OscArgument argument) {
        this.arguments.add(argument);
        return this;
    }

    public OscMessage addArgument(int argument) {
        return addArgument(new OscInt(argument));
    }

    public OscMessage addArgument(String argument) {
        return addArgument(new OscString(argument));
    }

    public OscMessage addArgument(double argument) {
        return addArgument(new OscDouble(argument));
    }

    public OscMessage addArgument(float argument) {
        return addArgument(new OscFloat(argument));
    }

    public List<OscArgument> getArguments() {
        return this.arguments;
    }

    public OscMessage setAddressPattern(String addressPattern) {
        this.addressPattern = new OscString(addressPattern);
        return this;
    }

    public OscMessage setAddressPattern(OscString addressPattern) {
        this.addressPattern = addressPattern;
        return this;
    }

    public OscMessage setTypeTag(String typeTag) {
        this.typeTag = new OscString(typeTag);
        return this;
    }

    public OscMessage setTypeTag(OscString typeTag) {
        this.typeTag = typeTag;
        return this;
    }

    public OscString getAddressPattern() {
        return this.addressPattern;
    }

    public OscString getTypeTag() {
        return this.typeTag;
    }

    public static OscMessage parse(byte[] data, int offset, int len) throws OscException {
        OscMessage message = new OscMessage();
        OscString addressPattern = OscString.parse(data, offset, len);
        offset += addressPattern.getByteLength();

        if (offset < len) {
            OscString typeTag = OscString.parse(data, offset, len);
            offset += typeTag.getByteLength();
            message.setTypeTag(typeTag);

            ByteBuffer buffer = ByteBuffer.wrap(data);
            String typeTagValue = typeTag.getValue();
            for (int i = 1; i < typeTagValue.length(); ++i) {
                char tag = typeTagValue.charAt(i);
                OscArgument argument = null;
                switch (tag) {
                    case OscTypeTag.INT:
                        argument = new OscInt(buffer.getInt(offset));
                        break;
                    case OscTypeTag.FLOAT:
                        argument = new OscFloat(buffer.getFloat(offset));
                        break;
                    case OscTypeTag.STRING:
                        argument = OscString.parse(data, offset, len);
                        break;
                    case OscTypeTag.BLOB:
                        int blobLength = buffer.getInt(offset);
                        byte[] blobData = new byte[blobLength];
                        System.arraycopy(buffer, offset, blobData, 0, blobLength);
                        argument = new OscBlob(blobData);
                        break;
                    case OscTypeTag.LONG:
                        argument = new OscLong(buffer.getLong(offset));
                        break;
                    case OscTypeTag.TIMETAG:
                        argument = new OscTimeTag(buffer.getLong(offset));
                        break;
                    case OscTypeTag.DOUBLE:
                        argument = new OscDouble(buffer.getDouble(offset));
                        break;
                    case OscTypeTag.SYMBOL:
                        argument = OscSymbol.parse(data, offset, len);
                        break;
                    case OscTypeTag.CHAR:
                        argument = new OscChar((char) buffer.getInt(offset));
                        break;
                    case OscTypeTag.RGBA:
                        argument = new OscRgba(buffer.getInt(offset));
                        break;
                    case OscTypeTag.MIDI:
                        argument = new OscMidi(buffer.getInt(offset));
                        break;
                    case OscTypeTag.TRUE:
                        argument = new OscTrue();
                        break;
                    case OscTypeTag.FALSE:
                        argument = new OscFalse();
                        break;
                    case OscTypeTag.NIL:
                        argument = new OscNil();
                        break;
                    case OscTypeTag.INFINITUM:
                        argument = new OscInfinitum();
                        break;
                    default:
                        throw new OscMalformedDataException("Unrecognized type tag: " + tag, data, offset, len);
                }
                offset += argument.getByteLength();
                message.addArgument(argument);
            }
        }
        return message;
    }

}
