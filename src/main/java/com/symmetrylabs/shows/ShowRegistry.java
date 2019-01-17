package com.symmetrylabs.shows;

import java.util.*;


import com.symmetrylabs.shows.mae.MaeShow;
import processing.core.PApplet;

public class ShowRegistry {
    static ShowBuilder DEFAULT_BUILDER = () -> new MaeShow();

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
        
        map.put(MaeShow.SHOW_NAME, () -> new MaeShow());
    }

    static interface ShowBuilder {
        Show build();
    }
}
