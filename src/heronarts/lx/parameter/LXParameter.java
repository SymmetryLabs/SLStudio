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

import heronarts.lx.LXComponent;

/**
 * This class provides a common interface for system components to have
 * parameters that can modify their operation. Any LXComponent can have
 * parameters, such as a pattern, effect, or transition.
 */
public interface LXParameter {

    /**
     * Gets the component to which this parameter is registered.
     *
     * @return Component this parameter belongs to, may be null
     */
    public LXComponent getComponent();

    /**
     * Gets the path that this parameter is registered to in the component
     *
     * @return Component parameter path
     */
    public String getPath();

    /**
     * Sets the component that owns this parameter
     *
     * @param component Component
     * @return this
     */
    public LXParameter setComponent(LXComponent component, String path);

    /**
     * Invoked when the parameter is done being used and none of its resources
     * are needed anymore.
     */
    public void dispose();

    /**
     * A method to reset the value of the parameter, if a default is available.
     * Not necessarily defined for all parameters, may be ignored.
     *
     * @return this
     */
    public abstract Object reset();

    /**
     * Sets the value of the parameter.
     *
     * @param value The value
     * @return this
     */
    public LXParameter setValue(double value);


    /**
     * Retrieves the value of the parameter
     *
     * @return Parameter value
     */
    public double getValue();

    /**
     * Utility helper function to get the value of the parameter as a float.
     *
     * @return Parameter value as float
     */
    public float getValuef();

    /**
     * Gets the label for this parameter
     *
     * @return Label of parameter
     */
    public String getLabel();
}
