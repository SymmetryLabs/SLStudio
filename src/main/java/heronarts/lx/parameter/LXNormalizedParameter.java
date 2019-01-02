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

package heronarts.lx.parameter;

/**
 * A parameter that supports values in a normalized form, from 0 to 1. This only
 * makes sense for parameters with fixed, finite ranges. The calls to
 * setNormalized() and getNormalized() operate in this space, while getValue()
 * respects the actual given value range.
 */
public interface LXNormalizedParameter extends LXParameter {

    /**
     * Sets the value or the parameter in normalized space from 0 to 1
     *
     * @param value The normalized value, from 0 to 1
     * @return this, for method chaining
     */
    public LXNormalizedParameter setNormalized(double value);

    /**
     * Gets the value of the parameter in a normalized space from 0 to 1
     *
     * @return Value of parameter, normalized to range from 0 to 1
     */
    public double getNormalized();

    /**
     * Gets the value of the parameter in a normalized space as a float
     *
     * @return Normalized value of parameter, in range from 0 to 1
     */
    public float getNormalizedf();

    /**
     * Gets the exponent used for scaling this parameter across its normalized range.
     * Default is 1 which means linear scaling.
     *
     * @return scaling exponent
     */
    public double getExponent();

}
