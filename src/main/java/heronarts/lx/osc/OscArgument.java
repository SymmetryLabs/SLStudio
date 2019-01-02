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

    public boolean toBoolean();

}
