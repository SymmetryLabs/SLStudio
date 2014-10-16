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

import java.util.ArrayList;
import java.util.List;

/**
 * Utility base class for objects that have parameters.
 */
public abstract class LXParameterized implements LXParameterListener {

    protected final List<LXParameter> parameters = new ArrayList<LXParameter>();

    public final LXParameterized addParameter(LXParameter parameter) {
        this.parameters.add(parameter);
        if (parameter instanceof LXListenableParameter) {
            ((LXListenableParameter) parameter).addListener(this);
        }
        return this;
    }

    public final LXParameterized addParameters(List<LXParameter> parameters) {
        for (LXParameter parameter : parameters) {
            addParameter(parameter);
        }
        return this;
    }

    public final List<LXParameter> getParameters() {
        return this.parameters;
    }

    public final LXParameter getParameter(String label) {
        for (LXParameter parameter : this.parameters) {
            if (parameter.getLabel().equals(label)) {
                return parameter;
            }
        }
        return null;
    }

    /**
     * Subclasses are free to override this, but in case they don't care a default
     * implementation is provided.
     */
    public/* abstract */void onParameterChanged(LXParameter parameter) {
    }

}
