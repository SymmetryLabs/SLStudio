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

import java.util.HashSet;
import java.util.Set;

/**
 * This is a parameter instance that can be listened to, meaning we are able to
 * deterministically know when the value has changed. This means that all
 * modifications *must* come through setValue().
 */
public abstract class LXListenableParameter implements LXParameter {

    private final String label;

    private double defaultValue, value;

    private final Set<LXParameterListener> listeners = new HashSet<LXParameterListener>();

    protected LXListenableParameter() {
        this(null, 0);
    }

    protected LXListenableParameter(String label) {
        this(label, 0);
    }

    protected LXListenableParameter(double value) {
        this(null, value);
    }

    protected LXListenableParameter(String label, double value) {
        this.label = label;
        this.defaultValue = this.value = value;
    }

    public final LXListenableParameter addListener(LXParameterListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
        return this;
    }

    public final LXListenableParameter removeListener(LXParameterListener listener) {
        listeners.remove(listener);
        return this;
    }

    public LXParameter reset() {
        return setValue(this.defaultValue);
    }

    /**
     * Resets the value of the parameter, giving it a new default. Future calls to
     * reset() with no parameter will use this value.
     *
     * @param value New default value
     * @return this
     */
    public LXParameter reset(double value) {
        this.defaultValue = value;
        return setValue(this.defaultValue);
    }

    public final LXParameter incrementValue(double amount) {
        return setValue(this.value + amount);
    }

    public final LXParameter setValue(double value) {
        if (this.value != value) {
            this.value = updateValue(value);
            for (LXParameterListener l : listeners) {
                l.onParameterChanged(this);
            }
        }
        return this;
    }

    public final double getValue() {
        return this.value;
    }

    public final float getValuef() {
        return (float) getValue();
    }

    public String getLabel() {
        return this.label;
    }

    /**
     * Invoked when the value has changed. Subclasses should update any special
     * internal state according to this new value.
     *
     * @param value New value
     * @return this
     */
    protected abstract double updateValue(double value);

}
