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

public class OscMalformedDataException extends OscException {

    private static final long serialVersionUID = 1L;

    private final byte[] data;
    private final int offset;
    private final int len;

    OscMalformedDataException(String message, byte[] data, int offset, int len) {
        super(message);
        this.data = data;
        this.offset = offset;
        this.len = len;
    }

    public byte[] getData() {
        return this.data;
    }

    public int getOffset() {
        return this.offset;
    }

    public int getLength() {
        return this.len;
    }

}