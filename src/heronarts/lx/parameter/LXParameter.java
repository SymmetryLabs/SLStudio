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
