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

public class OscInfinitum extends OscSentinel {

    @Override
    public char getTypeTag() {
        return OscTypeTag.INFINITUM;
    }

    @Override
    public int toInt() {
        return Integer.MAX_VALUE;
    }

    @Override
    public float toFloat() {
        return Float.POSITIVE_INFINITY;
    }

    @Override
    public double toDouble() {
        return Double.POSITIVE_INFINITY;
    }
}
