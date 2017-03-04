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
 * A FixedParameter is an immutable parameter. It will throw a RuntimeException
 * if setValue() is attempted. Useful for anonymous placeholder values in places
 * that expect to use LXParameters.
 */
public class FixedParameter implements LXParameter {

    private final double value;

    private LXComponent component;
    private String path;

    public FixedParameter(double value) {
        this.value = value;
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
    public void dispose() {

    }

    @Override
    public LXParameter reset() {
        return this;
    }

    @Override
    public LXParameter setValue(double value) {
        throw new RuntimeException("Cannot invoke setValue on a FixedParameter");
    }

    @Override
    public double getValue() {
        return this.value;
    }

    @Override
    public float getValuef() {
        return (float) this.value;
    }

    @Override
    public String getLabel() {
        return null;
    }

}
