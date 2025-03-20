package com.symmetrylabs.util.dmx;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

import heronarts.lx.LX;
import heronarts.lx.LXComponent;
import heronarts.lx.LXSerializable;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXNormalizedParameter;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.HashMap;
import java.util.WeakHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;


public class DMXParameterMapper extends LXComponent {

    private static Map<LX, WeakReference<DMXParameterMapper>> instanceByLX = new WeakHashMap<>();

    public static synchronized DMXParameterMapper getInstance(LX lx) {
        WeakReference<DMXParameterMapper> weakRef = instanceByLX.get(lx);
        DMXParameterMapper ref = weakRef == null ? null : weakRef.get();
        if (ref == null) {
            instanceByLX.put(lx, new WeakReference<>(ref = new DMXParameterMapper(lx)));
        }
        return ref;
    }

    Map<LXParameter, Integer> parameterMapping = new HashMap<>();
    Set<MappingListener> mappingListeners = new HashSet<>();

    private DMXParameterMapper(LX lx) {
        super(lx, "DMXParameterMapper");

        lx.engine.dmx.addHandler(new Handler());

        lx.engine.registerComponent("DMXParameterMapper", this);
    }

    public List<Mapping> getMappings() {
        List<Mapping> mappings = new ArrayList<>();
        for (Map.Entry<LXParameter, Integer> entry : parameterMapping.entrySet()) {
            mappings.add(new Mapping(entry.getKey(), entry.getValue()));
        }
        return mappings;
    }

    public void mapParameter(LXParameter parameter, int channel) {
        parameterMapping.put(parameter, channel);
    }

    public void unmapParameter(LXParameter parameter) {
        parameterMapping.remove(parameter);
    }

    public static final class Mapping implements LXSerializable {
        private static final String KEY_CHANNEL = "channel";

        public final LXParameter parameter;
        public final int channel;

        private Mapping(LXParameter parameter, int channel) {
            this.parameter = parameter;
            this.channel = channel;
        }

        @Override
        public void save(LX lx, JsonObject object) {
            object.addProperty(KEY_CHANNEL, this.channel);
            object.addProperty(LXComponent.KEY_COMPONENT_ID, parameter.getComponent().getId());
            object.addProperty(LXComponent.KEY_PARAMETER_PATH, parameter.getPath());
        }

        @Override
        public void load(LX lx, JsonObject object) {
            throw new UnsupportedOperationException("Use DMXParameterMapper.Mapping.create() to load from JsonObject");
        }

        private static Mapping create(LX lx, JsonObject object) {
            LXParameter parameter = lx.getProjectComponent(object.get(LXComponent.KEY_COMPONENT_ID).getAsInt()).getParameter(object.get(LXComponent.KEY_PARAMETER_PATH).getAsString());
            int channel = object.get(KEY_CHANNEL).getAsInt();
            return new Mapping(parameter, channel);
        }
    }

    private static final String KEY_MAPPINGS = "mappings";

    @Override
    public void save(LX lx, JsonObject object) {
        super.save(lx, object);

        object.add(KEY_MAPPINGS, LXSerializable.Utils.toArray(lx, getMappings()));
    }

    @Override
    public void load(LX lx, JsonObject object) {
        synchronized (DMXParameterMapper.class) {
            WeakReference<DMXParameterMapper> weakRef = instanceByLX.get(lx);
            DMXParameterMapper ref = weakRef == null ? null : weakRef.get();
            if (ref == null) {
                instanceByLX.put(lx, new WeakReference<>(this));
            }
        }

        for (Mapping mapping : getMappings()) {
            for (MappingListener listener : mappingListeners) {
                listener.mappingRemoved(mapping);
            }
        }

        parameterMapping.clear();

        if (object.has(KEY_MAPPINGS)) {
            JsonArray mappings = object.getAsJsonArray(KEY_MAPPINGS);
            for (JsonElement element : mappings) {
                try {
                    Mapping mapping = Mapping.create(lx, element.getAsJsonObject());
                    mapParameter(mapping.parameter, mapping.channel);
                } catch (Exception x) {
                    System.err.println("Could not load DMX mapping: " + element.toString());
                }
            }
        }

        for (Mapping mapping : getMappings()) {
            for (MappingListener listener : mappingListeners) {
                listener.mappingAdded(mapping);
            }
        }
    }

    public static interface MappingListener {
        default void mappingAdded(Mapping mapping) {}
        default void mappingRemoved(Mapping mapping) {}
    }

    public DMXParameterMapper addMappingListener(MappingListener listener) {
        mappingListeners.add(listener);
        return this;
    }

    public class Handler implements DMXHandler {

        private double getRawParameterValue(LXParameter parameter) {
            if (parameter instanceof LXNormalizedParameter) {
                return ((LXNormalizedParameter) parameter).getNormalized();
            } else {
                return parameter.getValue();
            }
        }

        private void setRawParameterValue(LXParameter parameter, double value) {
            if (parameter instanceof LXNormalizedParameter) {
                ((LXNormalizedParameter) parameter).setNormalized(value);
            } else {
                parameter.setValue(value);
            }
        }

        @Override
        public void onDMXDataReceived(DMXStream stream) {
            for (Map.Entry<LXParameter, Integer> entry : parameterMapping.entrySet()) {
                LXParameter parameter = entry.getKey();
                int channel = entry.getValue();

                if (channel < 0 || channel > 255)
                    continue;

                int value = stream.data[channel];
                double normalized = value / 255.0;
                if (getRawParameterValue(parameter) != normalized) {
                    setRawParameterValue(parameter, normalized);
                }
            }
        }
    }
}
