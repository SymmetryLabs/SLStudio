/**
 * Copyright 2017- Mark C. Slee, Heron Arts LLC
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
 * Parameter which contains a mutable String value.
 */
public class StringParameter extends LXListenableParameter {

    private String defaultString, string;

    public StringParameter(String label) {
        this(label, "");
    }

    public StringParameter(String label, String string) {
        super(label);
        this.defaultString = this.string = string;
    }

    @Override
    public LXParameter reset() {
        this.string = this.defaultString;
        super.reset();
        return this;
    }

    @Override
    public LXParameter reset(double value) {
        throw new UnsupportedOperationException("StringParamater cannot be reset to a numeric value");
    }

    public StringParameter setValue(String string) {
        this.string = string;
        incrementValue(1);
        return this;
    }

    @Override
    protected double updateValue(double value) {
        return value;
    }

    public String getString() {
        return this.string;
    }

}
