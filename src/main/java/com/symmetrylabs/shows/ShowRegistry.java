package com.symmetrylabs.shows;

import java.util.*;

import com.symmetrylabs.shows.arlo.ArloShow;
import com.symmetrylabs.shows.arlosohoroof.ArloSohoRoofShow;
import com.symmetrylabs.shows.arlotree.ArloTreeShow;
import com.symmetrylabs.shows.artbasel.ArtBaselShow;
import com.symmetrylabs.shows.base.BaseDemoShow;
import com.symmetrylabs.shows.demo.DemoShow;
import com.symmetrylabs.shows.exploratorium.ExploratoriumShow;
import com.symmetrylabs.shows.hblamp.HbLampShow;
import com.symmetrylabs.shows.hhgarden.HHFlowerShow;
import com.symmetrylabs.shows.hhgarden.HHGardenShow;
import com.symmetrylabs.shows.googlehq.GoogleHqShow;
import com.symmetrylabs.shows.japantree.JapanTreeShow;
import com.symmetrylabs.shows.kalpa.KalpaShow;
import com.symmetrylabs.shows.magicleap.MagicLeapShow;
import com.symmetrylabs.shows.mikey.MikeyShow;
import com.symmetrylabs.shows.office.OfficeShow;
import com.symmetrylabs.shows.penfoldswine.PenfoldsWineShow;
import com.symmetrylabs.shows.pilots.PilotsShow;
import com.symmetrylabs.shows.related.RelatedShow;
import com.symmetrylabs.shows.streetlamp.StreetlampShow;
import com.symmetrylabs.shows.summerbbq.SummerBBQShow;
import com.symmetrylabs.shows.summerstage.SummerStageShow;
import com.symmetrylabs.shows.summerstage19.SummerStage19Show;
import com.symmetrylabs.shows.thiel18.Thiel18Show;
import com.symmetrylabs.shows.ysiadsparty.YsiadsPartyShow;
import com.symmetrylabs.shows.absinthedemo.AbsintheDemoShow;
import com.symmetrylabs.shows.loveburn.LoveBurnShow;
import com.symmetrylabs.shows.sundance19.Sundance19Show;
import com.symmetrylabs.shows.absinthe.AbsintheShow;
import com.symmetrylabs.shows.twigtest.TwigTestShow;
//import com.symmetrylabs.shows.empirewall.EmpireWallPrototypeShow;
import com.symmetrylabs.shows.empirewall.EmpireWallShow;
import com.symmetrylabs.shows.wingportal.WingPortalShow;
import processing.core.PApplet;

public class ShowRegistry {
    static ShowBuilder DEFAULT_BUILDER = () -> new DemoShow();
    static boolean showedProcessingWarning = false;

    /** Builds and returns the show with the given name. */
    public static Show getShow(String name) {
        Map<String, ShowBuilder> builders = new HashMap<>();
        registerShows(null, builders);
        return builders.getOrDefault(name, DEFAULT_BUILDER).build();
    }

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
        map.put(DemoShow.SHOW_NAME, DemoShow::new);
        map.put(BaseDemoShow.SHOW_NAME, BaseDemoShow::new);
        map.put(OfficeShow.SHOW_NAME, OfficeShow::new);
        map.put(KalpaShow.SHOW_NAME, KalpaShow::new);
        map.put(SummerStageShow.SHOW_NAME, SummerStageShow::new);
        map.put(PilotsShow.SHOW_NAME, PilotsShow::new);
        map.put(SummerBBQShow.SHOW_NAME, SummerBBQShow::new);
        map.put(StreetlampShow.SHOW_NAME, StreetlampShow::new);
        map.put(MagicLeapShow.SHOW_NAME, MagicLeapShow::new);
        map.put(GoogleHqShow.SHOW_NAME, GoogleHqShow::new);
        map.put(PenfoldsWineShow.SHOW_NAME, PenfoldsWineShow::new);
        map.put(MikeyShow.SHOW_NAME, MikeyShow::new);
        map.put(HHFlowerShow.SHOW_NAME, HHFlowerShow::new);
        map.put(ArloShow.SHOW_NAME, ArloShow::new);
        map.put(HHGardenShow.SHOW_NAME, HHGardenShow::new);
        map.put(ArloTreeShow.SHOW_NAME, ArloTreeShow::new);
        map.put(ArloSohoRoofShow.SHOW_NAME, ArloSohoRoofShow::new);
        map.put(ExploratoriumShow.SHOW_NAME, ExploratoriumShow::new);
        map.put(ArtBaselShow.SHOW_NAME, ArtBaselShow::new);
        map.put(Thiel18Show.SHOW_NAME, Thiel18Show::new);
        map.put(RelatedShow.SHOW_NAME, RelatedShow::new);
        map.put(YsiadsPartyShow.SHOW_NAME, YsiadsPartyShow::new);
        map.put(AbsintheDemoShow.SHOW_NAME, AbsintheDemoShow::new);
        map.put(HbLampShow.SHOW_NAME, HbLampShow::new);
        map.put(LoveBurnShow.SHOW_NAME, LoveBurnShow::new);
        map.put(Sundance19Show.SHOW_NAME, Sundance19Show::new);
        map.put(AbsintheShow.SHOW_NAME, AbsintheShow::new);
        map.put(JapanTreeShow.SHOW_NAME, JapanTreeShow::new);
        map.put(TwigTestShow.SHOW_NAME, TwigTestShow::new);
        map.put(SummerStage19Show.SHOW_NAME, SummerStage19Show::new);
        map.put(EmpireWallShow.SHOW_NAME, EmpireWallShow::new);
        map.put(WingPortalShow.SHOW_NAME, WingPortalShow::new);
    }

    interface ShowBuilder {
        Show build();
    }
}
