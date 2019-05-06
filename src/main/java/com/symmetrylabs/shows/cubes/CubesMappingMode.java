package com.symmetrylabs.shows.cubes;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.WeakHashMap;

import com.symmetrylabs.slstudio.output.CubeModelControllerMapping.PhysIdAssignment;
import com.symmetrylabs.slstudio.output.CubeModelControllerMapping;
import com.symmetrylabs.slstudio.pattern.cubes.CubesMappingPattern;
import com.symmetrylabs.util.CubeInventory.PhysicalCube;
import com.symmetrylabs.util.CubeInventory;
import com.symmetrylabs.util.listenable.SetListener;
import heronarts.lx.LX;
import heronarts.lx.LXChannel;
import heronarts.lx.LXPattern;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.parameter.EnumParameter;

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

    public final SortedSet<String> fixturesMappedAndOnTheNetwork = new TreeSet<String>();
    public final SortedSet<String> fixturesMappedButNotOnNetwork = new TreeSet<String>();
    public final SortedSet<String> fixturesOnNetworkButNotMapped = new TreeSet<String>();

    private LX lx;
    private CubesModel cubesModel;

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

        cubesModel = lx.model instanceof CubesModel ? (CubesModel)lx.model : new CubesModel();
        for (CubesModel.Cube cube : cubesModel.getCubes()) {
            CubeModelControllerMapping.PhysIdAssignment map = cubesModel.controllers.lookUpModel(cube.modelId);
            if (map != null) {
                CubeInventory.PhysicalCube pc = cubesModel.inventory.lookUpByPhysId(map.physicalId);
                for (String ctrl : pc.getControllerIds()) {
                    fixturesMappedButNotOnNetwork.add(ctrl);
                }
            }
        }

        String[] emptyOptions = new String[] {"-"};

        String[] initialMappedFixtures = fixturesMappedButNotOnNetwork.isEmpty()
                ? emptyOptions : fixturesMappedButNotOnNetwork.toArray(new String[0]);

        selectedMappedFixture = new DiscreteParameter("selectedMappedFixture", initialMappedFixtures);
        selectedUnMappedFixture = new DiscreteParameter("selectedUnMappedFixture", emptyOptions);

        CubesShow show = CubesShow.getInstance(lx);

        if (show != null) {
            show.addControllerSetListener(new SetListener<CubesController>() {
                public void onItemAdded(final CubesController c) {
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
                public void onItemRemoved(final CubesController c) {}
            });
        }
        else {
            System.err.println("**WARNING** CubesMappingMode used before CubesShow has been initialized.");
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
        return cubesModel.controllers.lookUpByControllerId(id) != null;
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
        mappingChannel = lx.engine.getFocusedLook().addChannel();
        mappingChannel.addPattern(mappingPattern);

        for (LXChannel channel : lx.engine.getFocusedLook().channels) {
            channel.cueActive.setValue(false);
        }
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
