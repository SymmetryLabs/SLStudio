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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import heronarts.lx.LXSerializable;
import heronarts.lx.color.ColorParameter;

/**
 * Utility base class for objects that have parameters.
 */
public abstract class LXParameterized implements LXParameterListener, LXSerializable {

    protected final Map<String, LXParameter> parameters = new LinkedHashMap<String, LXParameter>();

    public final LXParameterized addParameter(LXParameter parameter) {
        return addParameter(parameter.getLabel(), parameter);
    }

    public final LXParameterized addParameter(String path, LXParameter parameter) {
        if (this.parameters.containsKey(path)) {
            throw new IllegalArgumentException("Cannot add parameter at existing path: " + path);
        }
        this.parameters.put(path, parameter);
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

    public final Collection<LXParameter> getParameters() {
        return this.parameters.values();
    }

    public final LXParameter getParameter(String path) {
        return this.parameters.get(path);
    }

    /**
     * Subclasses are free to override this, but in case they don't care a default
     * implementation is provided.
     */
    public/* abstract */void onParameterChanged(LXParameter parameter) {
    }

    private final static String KEY_PARAMETERS = "parameters";

    @Override
    public void save(JsonObject obj) {
        JsonObject parameters = new JsonObject();
        for (String path : this.parameters.keySet()) {
            LXParameter parameter = this.parameters.get(path);
            if (parameter instanceof StringParameter) {
                parameters.addProperty(path, ((StringParameter) parameter).getString());
            } else if (parameter instanceof BooleanParameter) {
                parameters.addProperty(path, ((BooleanParameter) parameter).isOn());
            } else if (parameter instanceof DiscreteParameter) {
                parameters.addProperty(path, ((DiscreteParameter) parameter).getValuei());
            } else if (parameter instanceof ColorParameter) {
                parameters.addProperty(path, ((ColorParameter) parameter).getColor());
            } else {
                parameters.addProperty(path, parameter.getValue());
            }
        }
        obj.add(KEY_PARAMETERS, parameters);
    }

    @Override
    public void load(JsonObject obj) {
        if (obj.has(KEY_PARAMETERS)) {
            JsonObject parameters = obj.getAsJsonObject(KEY_PARAMETERS);
            for (String path : this.parameters.keySet()) {
                if (parameters.has(path)) {
                    JsonElement value = parameters.get(path);
                    LXParameter parameter = this.parameters.get(path);
                    if (parameter instanceof StringParameter) {
                        ((StringParameter)parameter).setValue(value.getAsString());
                    } else if (parameter instanceof BooleanParameter) {
                        ((BooleanParameter)parameter).setValue(value.getAsBoolean());
                    } else if (parameter instanceof DiscreteParameter) {
                        parameter.setValue(value.getAsInt());
                    } else if (parameter instanceof ColorParameter) {
                        ((ColorParameter)parameter).setColor(value.getAsInt());
                    } else {
                        parameter.setValue(value.getAsDouble());
                    }
                }
            }
        }

    }

}
