package com.symmetrylabs.layouts;

import java.util.*;

import processing.core.PApplet;
import com.symmetrylabs.layouts.cubes.*;
import com.symmetrylabs.layouts.shows.usopen.USOpenLayout;
//import com.symmetrylabs.layouts.dynamic_JSON.DynamicLayout;
import com.symmetrylabs.layouts.oslo.OsloLayout;
import com.symmetrylabs.layouts.oslo.TreeModel;
import com.symmetrylabs.layouts.composite.CompositeLayout;
import com.symmetrylabs.layouts.obj.ObjLayout;
import com.symmetrylabs.layouts.tree.*;
import processing.core.PApplet;


public class LayoutRegistry {
    static LayoutBuilder DEFAULT_BUILDER = () -> new DemoLayout();

    /** Builds and returns the layout with the given name. */
    public static Layout getLayout(PApplet applet, String name) {
        Map<String, LayoutBuilder> builders = new HashMap<>();
        registerLayouts(applet, builders);
        return builders.getOrDefault(name, DEFAULT_BUILDER).build();
    }

    /** Returns a sorted set of the names of available layouts. */
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
        map.put("demo", () -> new DemoLayout());
        map.put("oslo", () -> new OsloLayout(applet, TreeModel.ModelMode.MAJOR_LIMBS));
        //map.put("dynamic_json", () -> new DynamicLayout());
        map.put("composite", () -> new CompositeLayout());
        map.put("obj", () -> new ObjLayout());
        map.put("office", () -> new OfficeLayout());
        map.put("kalpa", () -> new KalpaLayout());
        map.put("summer_stage", () -> new SummerStageLayout());
        map.put("21_pilots", () -> new TwentyOnePilotsLayout());
        map.put("us_open", () -> new USOpenLayout());
    }

    static interface LayoutBuilder {
        Layout build();
    }
}
