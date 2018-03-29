package com.symmetrylabs.layouts;

import com.symmetrylabs.layouts.cubes.CubesLayout;
import com.symmetrylabs.layouts.dynamic_JSON.DynamicLayout;
import com.symmetrylabs.layouts.oslo.OsloLayout;
import com.symmetrylabs.layouts.oslo.TreeModel;
import com.symmetrylabs.layouts.icicles.KearnyStreetLayout;
import processing.core.PApplet;

import java.util.*;

public class LayoutRegistry {
    static LayoutBuilder DEFAULT_BUILDER = () -> new CubesLayout();

    /** Builds and returns the layout with the given name. */
    public static Layout getLayout(PApplet applet, String name) {
        Map<String, LayoutBuilder> builders = new HashMap<>();
        registerLayouts(applet, builders);
        return builders.getOrDefault(name, DEFAULT_BUILDER).build();
    }

    /** Returns a sorted list of the names of available layouts. */
    public static List<String> getNames() {
        Map<String, LayoutBuilder> builders = new HashMap<>();
        registerLayouts(null, builders);
        List<String> names = new ArrayList<>(builders.keySet());
        Collections.sort(names);
        return names;
    }

    /** Registers all available layout builders in the given map. */
    private static void registerLayouts(PApplet applet, Map<String, LayoutBuilder> map) {
        // This is the central registry of layouts.  Add an entry here for each available layout.
        map.put("cubes", () -> new CubesLayout());
        map.put("oslo", () -> new OsloLayout(applet, TreeModel.ModelMode.MAJOR_LIMBS));
        map.put("dynamic_json", () -> new DynamicLayout());
        map.put("kearny_street", () -> new KearnyStreetLayout());
    }

    static interface LayoutBuilder {
        Layout build();
    }
}
