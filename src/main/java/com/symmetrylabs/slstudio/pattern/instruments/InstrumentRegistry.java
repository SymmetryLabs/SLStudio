package com.symmetrylabs.slstudio.pattern.instruments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** The singleton registry of all Instrument classes by name. */
public class InstrumentRegistry {
    private static Map<String, InstrumentBuilder> map;

    public static Instrument getInstrument(String name) {
        return getMap().get(name).build();
    }

    public static List<String> getNames() {
        List<String> names = new ArrayList<>(getMap().keySet());
        Collections.sort(names);
        return names;
    }

    private static synchronized Map<String, InstrumentBuilder> getMap() {
        if (map == null) {
            map = new HashMap<>();
            map.put("Jet", () -> new EmitterInstrument(new JetEmitter()));
            map.put("Sprinkle", () -> new EmitterInstrument(new SprinkleEmitter()));
            map.put("Swim", () -> new EmitterInstrument(new SwimEmitter()));
        }
        return map;
    }

    private static interface InstrumentBuilder {
        Instrument build();
    }
}
