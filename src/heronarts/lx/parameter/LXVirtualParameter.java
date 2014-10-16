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
 * A virtual parameter is one that wraps or forwards to another real parameter.
 * Typically this is done in situations in which the parameter to forward to
 * varies based on some other contextual action or UI, for instance a virtual
 * knob that maps to whatever pattern is currently active.
 * 
 * This type of parameter is not listenable, since the underlying parameter is
 * dynamic.
 */
public abstract class LXVirtualParameter implements LXParameter {

    /**
     * The parameter to operate on.
     * 
     * @return The underlying real parameter to operate on.
     */
    protected abstract LXParameter getRealParameter();

    public final LXParameter reset() {
        LXParameter p = getRealParameter();
        if (p != null) {
            p.reset();
        }
        return this;
    }

    public final LXParameter setValue(double value) {
        LXParameter p = getRealParameter();
        if (p != null) {
            p.setValue(value);
        }
        return this;
    }

    public double getValue() {
        LXParameter p = getRealParameter();
        if (p != null) {
            return p.getValue();
        }
        return 0;
    }

    public float getValuef() {
        return (float) getValue();
    }

    public String getLabel() {
        LXParameter p = getRealParameter();
        if (p != null) {
            return p.getLabel();
        }
        return null;
    }

}
