package com.symmetrylabs.shows;

import java.util.*;

import com.symmetrylabs.shows.arlo.ArloShow;
import com.symmetrylabs.shows.arlosohoroof.ArloSohoRoofShow;
import com.symmetrylabs.shows.arlotree.ArloTreeShow;
import com.symmetrylabs.shows.artbasel.ArtBaselShow;
import com.symmetrylabs.shows.composite.CompositeShow;
import com.symmetrylabs.shows.demo.DemoShow;
import com.symmetrylabs.shows.exploratorium.ExploratoriumShow;
import com.symmetrylabs.shows.hblamp.HbLampShow;
import com.symmetrylabs.shows.hhgarden.HHFlowerShow;
import com.symmetrylabs.shows.hhgarden.HHGardenShow;
import com.symmetrylabs.shows.googlehq.GoogleHqShow;
import com.symmetrylabs.shows.kalpa.KalpaShow;
import com.symmetrylabs.shows.magicleap.MagicLeapShow;
import com.symmetrylabs.shows.mikey.MikeyShow;
import com.symmetrylabs.shows.obj.ObjShow;
import com.symmetrylabs.shows.office.OfficeShow;
import com.symmetrylabs.shows.oslo.OsloShow;
import com.symmetrylabs.shows.oslo.TreeModel;
import com.symmetrylabs.shows.penfoldswine.PenfoldsWineShow;
import com.symmetrylabs.shows.pilots.PilotsShow;
import com.symmetrylabs.shows.related.RelatedShow;
import com.symmetrylabs.shows.streetlamp.StreetlampShow;
import com.symmetrylabs.shows.summerbbq.SummerBBQShow;
import com.symmetrylabs.shows.summerstage.SummerStageShow;
import com.symmetrylabs.shows.thiel18.Thiel18Show;
import com.symmetrylabs.shows.ysiadsparty.YsiadsPartyShow;
import com.symmetrylabs.shows.absinthedemo.AbsintheDemoShow;
import com.symmetrylabs.shows.loveburn.LoveBurnShow;
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
        map.put(HHFlowerShow.SHOW_NAME, () -> new HHFlowerShow());
        map.put(ArloShow.SHOW_NAME, () -> new ArloShow());
        map.put(HHGardenShow.SHOW_NAME, () -> new HHGardenShow());
        map.put(ArloTreeShow.SHOW_NAME, () -> new ArloTreeShow());
        map.put(ArloSohoRoofShow.SHOW_NAME, () -> new ArloSohoRoofShow());
        map.put(ExploratoriumShow.SHOW_NAME, () -> new ExploratoriumShow());
        map.put(ArtBaselShow.SHOW_NAME, () -> new ArtBaselShow());
        map.put(Thiel18Show.SHOW_NAME, () -> new Thiel18Show());
        map.put(RelatedShow.SHOW_NAME, () -> new RelatedShow());
        map.put(YsiadsPartyShow.SHOW_NAME, () -> new YsiadsPartyShow());
        map.put(AbsintheDemoShow.SHOW_NAME, () -> new AbsintheDemoShow());
        map.put(HbLampShow.SHOW_NAME, () -> new HbLampShow());
        map.put(LoveBurnShow.SHOW_NAME, () -> new LoveBurnShow());
    }

    static interface ShowBuilder {
        Show build();
    }
}
