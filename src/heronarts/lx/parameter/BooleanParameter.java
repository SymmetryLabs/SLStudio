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
 * A simple parameter that has a binary value of off or on
 */
public class BooleanParameter extends LXListenableNormalizedParameter {

    public BooleanParameter(String label) {
        this(label, false);
    }

    public BooleanParameter(String label, boolean on) {
        super(label, on ? 1. : 0.);
    }

    public boolean isOn() {
        return getValueb();
    }

    public boolean getValueb() {
        return this.getValue() > 0.;
    }

    public BooleanParameter setValue(boolean value) {
        setValue(value ? 1. : 0.);
        return this;
    }

    public BooleanParameter toggle() {
        setValue(!isOn());
        return this;
    }

    @Override
    protected double updateValue(double value) {
        return (value > 0) ? 1. : 0.;
    }

    public double getNormalized() {
        return (getValue() > 0) ? 1. : 0.;
    }

    public float getNormalizedf() {
        return (float) getNormalized();
    }

    public BooleanParameter setNormalized(double normalized) {
        setValue(normalized >= 0.5);
        return this;
    }

}
