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

public class OscBlob implements OscArgument {
    private int byteLength;
    private byte[] data;

    public OscBlob(byte[] data) {
        if (data == null) {
            throw new IllegalArgumentException("Cannot pass null array to OscBlob constructor");
        }
        setData(data);
    }

    public byte[] getData() {
        return this.data;
    }

    public OscBlob setData(byte[] data) {
        this.data = data;
        this.byteLength = 4 + data.length;
        while (this.byteLength % 4 > 0) {
            ++this.byteLength;
        }
        return this;
    }

    public int getByteLength() {
        return this.byteLength;
    }

    @Override
    public char getTypeTag() {
        return OscTypeTag.BLOB;
    }

    @Override
    public String toString() {
        return "<" + data.length + "-byte blob>";
    }

    @Override
    public void serialize(ByteBuffer buffer) {
        buffer.putInt(this.byteLength);
        buffer.put(this.data);
        for (int i = this.data.length; i < this.byteLength; ++i) {
            buffer.put((byte) 0);
        }
    }

    @Override
    public int toInt() {
        return 0;
    }

    @Override
    public float toFloat() {
        return 0;
    }

    @Override
    public double toDouble() {
        return 0;
    }
}
