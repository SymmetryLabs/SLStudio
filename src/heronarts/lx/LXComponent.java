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

package heronarts.lx;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import heronarts.lx.color.ColorParameter;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.LXListenableParameter;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.lx.parameter.StringParameter;

/**
 * Utility base class for objects that have parameters.
 */
public abstract class LXComponent implements LXParameterListener, LXSerializable {

    private LX lx;

    public final StringParameter label = new StringParameter("Label");

    private static final int ID_UNASSIGNED = -1;
    static final int ID_ENGINE = 1;

    static class Registry {
        private int idCounter = ID_ENGINE+1;
        private final Map<Integer, LXComponent> components = new HashMap<Integer, LXComponent>();

        LXComponent get(int id) {
            return this.components.get(id);
        }

        void register(LXComponent component) {
            if (component.id == ID_UNASSIGNED) {
                component.id = this.idCounter++;
            } else if (component.id <= 0) {
                throw new IllegalStateException("Component has bunk ID: " + component.id + " " + component);
            }
            if (this.components.containsKey(component.id)) {
                throw new IllegalStateException("Component id already registered: " + component.id);
            }
            this.components.put(component.id, component);
        }

        int getIdCounter() {
            return this.idCounter;
        }

        void setIdCounter(int idCounter) {
            this.idCounter = idCounter;
        }

        void setId(LXComponent component, int id) {
            if (id <= 0) {
                throw new IllegalArgumentException("Cannot setId to non-positive value: " + id + " " + component);
            }
            if (component.id > 0) {
                this.components.remove(component.id);
            }
            if (this.components.containsKey(id)) {
                throw new IllegalArgumentException("Component id already in use: " + id + " (requesting: " + component + ") (owner: " + this.components.get(id) + ")");
            }
            component.id = id;
            this.components.put(id, component);
        }

        void dispose(LXComponent component) {
            this.components.remove(component.id);
        }
    }

    private LXComponent parent;

    private int id;

    protected LXComponent() {
        this(null, ID_UNASSIGNED);
    }

    protected LXComponent(LX lx) {
        this(lx, ID_UNASSIGNED);
    }

    protected LXComponent(LX lx, int id) {
        this.lx = lx;
        this.id = id;
        if (id != ID_UNASSIGNED && lx == null) {
            throw new IllegalArgumentException("Cannot specify id on component with no LX instance");
        }
        if (lx != null) {
            lx.componentRegistry.register(this);
        }
        addParameter("__label", label);
    }

    protected LX getLX() {
        return this.lx;
    }

    protected LXComponent addSubcomponent(LXComponent child) {
        child.setParent(this);
        return this;
    }

    final LXComponent setParent(LXComponent parent) {
        if (this.parent != null) {
            throw new IllegalStateException("Component already has parent set: " + this + " " + parent);
        }
        if (parent == null) {
            throw new IllegalArgumentException("Cannot set null parent on component: " + this);
        }
        if (parent.lx == null) {
            throw new IllegalStateException("Cannot set component parent with no lx instance: " + this + " " + parent);
        }
        if (parent == this) {
            throw new IllegalStateException("Component cannot be its own parent: " + parent);
        }
        this.parent = parent;
        if (this.lx == null) {
            this.lx = parent.lx;
            this.lx.componentRegistry.register(this);
        }
        return this;
    }

    public final LXComponent getParent() {
        return this.parent;
    }

    public final int getId() {
        return this.id;
    }

    public String getCanonicalPath() {
        String path = getLabel();
        if (this.parent != null && this.parent != this.lx.engine) {
            return this.parent.getCanonicalPath() + " | " + path;
        }
        return path;
    }

    public String getLabel() {
        return label.getString();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + getCanonicalPath() + "]";
    }

    public void dispose() {
        if (this.lx == null) {
            throw new IllegalStateException("LXComponent never had lx reference set: " + this);
        }
        this.lx.engine.midi.removeMappings(this);
        this.lx.engine.modulation.removeModulations(this);
        for (LXParameter parameter : this.parameters.values()) {
            parameter.dispose();
        }
        this.parameters.clear();
        this.parent = null;
        this.lx.componentRegistry.dispose(this);
    }

    protected final Map<String, LXParameter> parameters = new LinkedHashMap<String, LXParameter>();

    public final LXComponent addParameter(LXParameter parameter) {
        return addParameter(parameter.getLabel(), parameter);
    }

    public final LXComponent addParameter(String path, LXParameter parameter) {
        if (this.parameters.containsKey(path)) {
            throw new IllegalStateException("Cannot add parameter at existing path: " + path);
        }
        LXComponent component = parameter.getComponent();
        if (component != null) {
            throw new IllegalStateException("Parameter " + parameter + " already owned by " + component);
        }
        parameter.setComponent(this, path);
        this.parameters.put(path, parameter);
        if (parameter instanceof LXListenableParameter) {
            ((LXListenableParameter) parameter).addListener(this);
        }
        return this;
    }

    public final LXComponent addParameters(List<LXParameter> parameters) {
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
    public void onParameterChanged(LXParameter parameter) {}

    protected final static String KEY_ID = "id";
    protected final static String KEY_CLASS = "class";
    private final static String KEY_PARAMETERS = "parameters";

    @Override
    public void save(LX lx, JsonObject obj) {
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

        obj.addProperty(KEY_ID, this.id);
        obj.addProperty(KEY_CLASS, getClass().getName());
        obj.add(KEY_PARAMETERS, parameters);
    }

    @Override
    public void load(LX lx, JsonObject obj) {
        if (obj.has(KEY_ID)) {
            lx.componentRegistry.setId(this, obj.get(KEY_ID).getAsInt());
        }
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
