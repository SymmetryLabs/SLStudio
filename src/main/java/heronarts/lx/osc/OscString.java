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

import java.nio.ByteBuffer;

public class OscString implements OscArgument {

    private String value;
    private int byteLength;

    public OscString(char[] value) {
        this(new String(value));
    }

    public OscString(String value) {
        setValue(value);
    }

    public OscString setValue(String value) {
        this.value = value;
        this.byteLength = value.length() + 1;
        while (this.byteLength % 4 > 0) {
            ++this.byteLength;
        }
        return this;
    }

    public String getValue() {
        return this.value;
    }

    public int getByteLength() {
        return this.byteLength;
    }

    public static OscString parse(byte[] data, int offset, int len) throws OscException {
        for (int i = offset; i < len; ++i) {
            if (data[i] == 0) {
                return new OscString(new String(data, offset, i-offset));
            }
        }
        throw new OscMalformedDataException("OscString has no terminating null character", data, offset, len);
    }

    @Override
    public char getTypeTag() {
        return OscTypeTag.STRING;
    }

    @Override
    public String toString() {
        return this.value;
    }

    public void serialize(ByteBuffer buffer) {
        byte[] bytes = this.value.getBytes();
        buffer.put(bytes);
        for (int i = bytes.length; i < this.byteLength; ++i) {
            buffer.put((byte) 0);
        }
    }

    @Override
    public int toInt() {
        return Integer.parseInt(this.value);
    }

    @Override
    public float toFloat() {
        return Float.parseFloat(this.value);
    }

    @Override
    public double toDouble() {
        return Double.parseDouble(this.value);
    }

    @Override
    public boolean toBoolean() {
        return this.value.equals("true") || this.value.equals("TRUE");
    }
}
