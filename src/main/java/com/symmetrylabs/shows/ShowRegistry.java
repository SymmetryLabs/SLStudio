package com.symmetrylabs.shows;

import java.util.*;

import com.symmetrylabs.shows.composite.CompositeShow;
import com.symmetrylabs.shows.demo.DemoShow;
import com.symmetrylabs.shows.googlehq.GoogleHqShow;
import com.symmetrylabs.shows.kalpa.KalpaShow;
import com.symmetrylabs.shows.magicleap.MagicLeapShow;
import com.symmetrylabs.shows.mikey.MikeyShow;
import com.symmetrylabs.shows.obj.ObjShow;
import com.symmetrylabs.shows.office.OfficeShow;
import com.symmetrylabs.shows.oslo.OsloShow;
import com.symmetrylabs.shows.oslo.TreeModel;
import com.symmetrylabs.shows.pilots.PilotsShow;
import com.symmetrylabs.shows.streetlamp.StreetlampShow;
import com.symmetrylabs.shows.summerbbq.SummerBBQShow;
import com.symmetrylabs.shows.summerstage.SummerStageShow;
import com.symmetrylabs.shows.penfoldswine.PenfoldsWineShow;
import processing.core.PApplet;

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
        map.put("summerbbq", () -> new SummerBBQShow());
        map.put(StreetlampShow.SHOW_NAME, () -> new StreetlampShow());
        map.put(MagicLeapShow.SHOW_NAME, () -> new MagicLeapShow());
        map.put(GoogleHqShow.SHOW_NAME, () -> new GoogleHqShow());
        map.put(PenfoldsWineShow.SHOW_NAME, () -> new PenfoldsWineShow());
        map.put(MikeyShow.SHOW_NAME, () -> new MikeyShow());
    }

    static interface ShowBuilder {
        Show build();
    }
}
