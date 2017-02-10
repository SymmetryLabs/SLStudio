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

public interface OscArgument {
    /**
     * Gets the number of bytes used by this argument uses in its encoded representation.
     *
     * @return Number of bytes of data this argument consumes
     */
    public int getByteLength();

    /**
     * Returns the type tag used to encode this argument
     *
     * @return Type-tag character for this argument
     */
    public char getTypeTag();

    /**
     * Serializes this argument to a buffer
     *
     * @param buffer ByteByffer to write to
     */
    public void serialize(ByteBuffer buffer);

    /**
     * Gets an integer value of this argument
     *
     * @return Integer value of this argument
     */
    public int toInt();

    public float toFloat();

    public double toDouble();

}
