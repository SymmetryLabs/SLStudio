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

public class OscFloat implements OscArgument {

    private float value;

    public OscFloat(float value) {
        this.value = value;
    }

    public OscFloat setValue(float value) {
        this.value = value;
        return this;
    }

    public float getValue() {
        return this.value;
    }

    public int getByteLength() {
        return 4;
    }

    @Override
    public char getTypeTag() {
        return OscTypeTag.FLOAT;
    }

    @Override
    public String toString() {
        return Float.toString(this.value);
    }

    @Override
    public void serialize(ByteBuffer buffer) {
        buffer.putFloat(this.value);
    }

    @Override
    public int toInt() {
        return (int) this.value;
    }

    @Override
    public float toFloat() {
        return this.value;
    }

    @Override
    public double toDouble() {
        return this.value;
    }

    @Override
    public boolean toBoolean() {
        return this.value > 0;
    }
}
