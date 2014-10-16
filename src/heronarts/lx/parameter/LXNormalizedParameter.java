/**
 * Copyright 2013- Mark C. Slee, Heron Arts LLC
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

}
