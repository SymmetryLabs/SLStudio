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

public class OscDouble implements OscArgument {

    private double value = 0;

    public OscDouble() {}

    public OscDouble(double value) {
        this.value = value;
    }

    public OscDouble setValue(double value) {
        this.value = value;
        return this;
    }

    public double getValue() {
        return this.value;
    }

    public int getByteLength() {
        return 8;
    }

    @Override
    public char getTypeTag() {
        return OscTypeTag.DOUBLE;
    }

    @Override
    public String toString() {
        return Double.toString(value);
    }

    @Override
    public void serialize(ByteBuffer buffer) {
        buffer.putDouble(this.value);
    }

    @Override
    public int toInt() {
        return (int) this.value;
    }

    @Override
    public float toFloat() {
        return (float) this.value;
    }

    @Override
    public double toDouble() {
        return this.value;
    }
}
