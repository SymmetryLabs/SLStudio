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
 * An LXParameter that has a value computed by a function, which may combine the
 * values of other parameters, or call some function, etc.
 */
public abstract class FunctionalParameter implements LXParameter {

    private final String label;

    private LXComponent component;
    private String path;

    protected FunctionalParameter() {
        this("FUNC-PARAM");
    }

    protected FunctionalParameter(String label) {
        this.label = label;
    }

    @Override
    public LXParameter setComponent(LXComponent component, String path) {
        this.component = component;
        this.path = path;
        return this;
    }

    @Override
    public LXComponent getComponent() {
        return this.component;
    }

    @Override
    public String getPath() {
        return this.path;
    }

    @Override
    public void dispose() {}

    /**
     * Does nothing, subclass may override.
     */
    public FunctionalParameter reset() {
        return this;
    }

    /**
     * Not supported for this parameter type unless subclass overrides.
     *
     * @param value The value
     */
    public LXParameter setValue(double value) {
        throw new UnsupportedOperationException(
                "FunctionalParameter does not support setValue()");
    }

    /**
     * Retrieves the value of the parameter, subclass must implement.
     *
     * @return Parameter value
     */
    public abstract double getValue();

    /**
     * Utility helper function to get the value of the parameter as a float.
     *
     * @return Parameter value as float
     */
    public float getValuef() {
        return (float) getValue();
    }

    /**
     * Gets the label for this parameter
     *
     * @return Label of parameter
     */
    public final String getLabel() {
        return this.label;
    }

}
