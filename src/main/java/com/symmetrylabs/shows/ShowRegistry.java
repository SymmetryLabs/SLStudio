package com.symmetrylabs.shows;

import java.util.*;

import com.symmetrylabs.shows.cubes.OfficeShow;
import com.symmetrylabs.shows.pilots.PilotsShow;
import processing.core.PApplet;

import com.symmetrylabs.shows.cubes.*;
import com.symmetrylabs.shows.oslo.OsloShow;
import com.symmetrylabs.shows.oslo.TreeModel;
import com.symmetrylabs.shows.composite.CompositeShow;
import com.symmetrylabs.shows.obj.ObjShow;
import com.symmetrylabs.shows.kalpa.*;

public class ShowRegistry {
    static ShowBuilder DEFAULT_BUILDER = () -> new DemoShow();

    /** Builds and returns the show with the given name. */
    public static Show getShow(PApplet applet, String name) {
        Map<String, ShowBuilder> builders = new HashMap<>();
        registerShows(applet, builders);
        return builders.getOrDefault(name, DEFAULT_BUILDER).build();
    }

    /** Returns a sorted set of the names of available shows. */
    public static List<String> getNames() {
        Map<String, ShowBuilder> builders = new HashMap<>();
        registerShows(null, builders);
        List<String> names = new ArrayList<>(builders.keySet());
        Collections.sort(names);
        return names;
    }

    /** Registers all available show builders in the given map. */
    private static void registerShows(PApplet applet, Map<String, ShowBuilder> map) {
        // This is the central registry of shows.  Add an entry here for each available show.
        map.put("demo", () -> new DemoShow());
        map.put("oslo", () -> new OsloShow(applet, TreeModel.ModelMode.MAJOR_LIMBS));
        map.put("composite", () -> new CompositeShow());
        map.put("obj", () -> new ObjShow());
        map.put("office", () -> new OfficeShow());
        map.put("kalpa", () -> new KalpaShow());
        map.put("summerstage", () -> new SummerStageShow());
        map.put("pilots", () -> new PilotsShow());
        map.put("usopen", () -> new USOpenLayout());
    }

    static interface ShowBuilder {
        Show build();
    }
}
