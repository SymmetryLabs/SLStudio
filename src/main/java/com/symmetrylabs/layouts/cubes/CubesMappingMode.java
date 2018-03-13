package com.symmetrylabs.layouts.cubes;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.WeakHashMap;
import java.lang.ref.WeakReference;

import com.symmetrylabs.layouts.cubes.patterns.CubesMappingPattern;
import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.LXChannel;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.EnumParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.color.LXColor;

import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.util.listenable.ListListener;

import com.symmetrylabs.layouts.icicles.IcicleModel;
import com.symmetrylabs.layouts.icicles.IcicleLayout;

/**
 * Mapping Mode
 * (TODO)
 *  1) iterate through mapped cubes in order (tower by tower, cube by cube)
 *  2) get cubes not mapped but on network to pulse
 *  3) get a "display orientation" mode
 */
public class CubesMappingMode {

    public static enum MappingModeType {MAPPED, UNMAPPED};
    public static enum MappingDisplayModeType {ALL, ITERATE};

    private LXChannel mappingChannel = null;
    private LXPattern mappingPattern = null;

    public final BooleanParameter enabled;
    public final EnumParameter<MappingModeType> mode;
    public final EnumParameter<MappingDisplayModeType> displayMode;
    //public final BooleanParameter displayOrientation;

    public final DiscreteParameter selectedMappedFixture;
    public final DiscreteParameter selectedUnMappedFixture;

    public final List<String> fixturesMappedAndOnTheNetwork = new ArrayList<String>();
    public final List<String> fixturesMappedButNotOnNetwork = new ArrayList<String>();
    public final List<String> fixturesOnNetworkButNotMapped = new ArrayList<String>();

    private LX lx;
    private IcicleModel cubesModel;

    private static Map<LX, WeakReference<CubesMappingMode>> instanceByLX = new WeakHashMap<>();

    public static CubesMappingMode getInstance(LX lx) {
        WeakReference<CubesMappingMode> weakRef = instanceByLX.get(lx);
        CubesMappingMode ref = weakRef == null ? null : weakRef.get();
        if (ref == null) {
            instanceByLX.put(lx, new WeakReference<>(ref = new CubesMappingMode(lx)));
        }
        return ref;
    }

    private CubesMappingMode(LX lx) {
        this.lx = lx;

        this.enabled = new BooleanParameter("enabled", false)
         .setDescription("Mapping Mode: toggle on/off");

        this.mode = new EnumParameter<MappingModeType>("mode", MappingModeType.MAPPED)
         .setDescription("Mapping Mode: toggle between mapped/unmapped fixtures");

        this.displayMode = new EnumParameter<MappingDisplayModeType>("displayMode", MappingDisplayModeType.ALL)
         .setDescription("Mapping Mode: display all mapped/unmapped fixtures");

        //this.displayOrientation = new BooleanParameter("displayOrientation", false)
        //        .setDescription("Mapping Mode: display colors on strips to indicate it's orientation");

        cubesModel = lx.model instanceof IcicleModel ? (IcicleModel)lx.model : new IcicleModel();
        for (IcicleModel.Icicle cube : cubesModel.getIcicles()) {
            fixturesMappedButNotOnNetwork.add(cube.id);
        }

        String[] emptyOptions = new String[] {"-"};

        String[] initialMappedFixtures = fixturesMappedButNotOnNetwork.isEmpty()
                ? emptyOptions : fixturesMappedButNotOnNetwork.toArray(new String[0]);

        selectedMappedFixture = new DiscreteParameter("selectedMappedFixture", initialMappedFixtures);
        selectedUnMappedFixture = new DiscreteParameter("selectedUnMappedFixture", emptyOptions);

        IcicleLayout layout = IcicleLayout.getInstance(lx);

        if (layout != null) {
            layout.addControllerListListener(new ListListener<CubesController>() {
                public void itemAdded(final int index, final CubesController c) {
                    if (isFixtureMapped(c.id)) {
                        fixturesMappedButNotOnNetwork.remove(c.id);
                        fixturesMappedAndOnTheNetwork.add(c.id);
                    } else {
                        fixturesOnNetworkButNotMapped.add(c.id);
                    }

                    selectedMappedFixture.setOptions(fixturesMappedAndOnTheNetwork.isEmpty() ? emptyOptions
                            : fixturesMappedAndOnTheNetwork.toArray(new String[0]));
                    selectedUnMappedFixture.setOptions(fixturesOnNetworkButNotMapped.isEmpty() ? emptyOptions
                            : fixturesOnNetworkButNotMapped.toArray(new String[0]));
                }
                public void itemRemoved(final int index, final CubesController c) {}
            });
        }
        else {
            System.err.println("**WARNING** CubesMappingMode used before CubesLayout has been initialized.");
        }

        enabled.addListener(p -> {
            if (((BooleanParameter)p).isOn()) {
                addChannel();
            }
            else {
                removeChannel();
            }
        });
    }

    public boolean isFixtureMapped(String id) {
        for (IcicleModel.Icicle fixture : cubesModel.getIcicles()) {
            if (fixture.id.equals(id))
                return true;
        }
        return false;
    }

    public boolean inMappedMode() {
        return mode.getObject() == MappingModeType.MAPPED;
    }

    public boolean inUnMappedMode() {
        return mode.getObject() == MappingModeType.UNMAPPED;
    }

    public boolean inDisplayAllMode() {
        return displayMode.getObject() == MappingDisplayModeType.ALL;
    }

    public boolean inIterateFixturesMode() {
        return displayMode.getObject() == MappingDisplayModeType.ITERATE;
    }

    public String getSelectedMappedFixtureId() {
        return (String)selectedMappedFixture.getOption();
    }

    public String getSelectedUnMappedFixtureId() {
        return (String)selectedUnMappedFixture.getOption();
    }

    public boolean isSelectedUnMappedFixture(String id) {
        return id.equals(selectedUnMappedFixture.getOption());
    }

    public int getUnMappedColor() {
        //if (mappingPattern != null)
        //    return mappingPattern.getUnMappedButOnNetworkColor;
        //return 0;
        return LXColor.RED; // temp
    }

    private void addChannel() {
        mappingPattern = new CubesMappingPattern(lx);
        mappingChannel = lx.engine.addChannel(new LXPattern[] {mappingPattern});

        for (LXChannel channel : lx.engine.channels)
            channel.cueActive.setValue(false);

        mappingChannel.fader.setValue(1);
        mappingChannel.label.setValue("Mapping");
        mappingChannel.cueActive.setValue(true);
    }

    private void removeChannel() {
        lx.engine.removeChannel(mappingChannel);
        mappingChannel = null;
        mappingPattern = null;
    }
}
