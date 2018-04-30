/**
 * Copyright 2013- Mark C. Slee, Heron Arts LLC
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

package heronarts.lx.output;

public interface OPCConstants {

    public static final int HEADER_LEN = 4;

    public static final int BYTES_PER_PIXEL = 3;
    public static final int BYTES_PER_16BIT_PIXEL = 6;

    public static final int INDEX_CHANNEL = 0;
    public static final int INDEX_COMMAND = 1;
    public static final int INDEX_DATA_LEN_MSB = 2;
    public static final int INDEX_DATA_LEN_LSB = 3;
    public static final int INDEX_DATA = 4;

    public static final byte CHANNEL_BROADCAST = 0;

    public static final byte COMMAND_SET_PIXEL_COLORS = 0;
    public static final byte COMMAND_SET_16BIT_PIXEL_COLORS = 2;
    public static final byte COMMAND_SYSTEM_EXCLUSIVE = (byte) 0xff;
}
