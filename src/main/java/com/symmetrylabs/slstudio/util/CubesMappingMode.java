package com.symmetrylabs.slstudio.util;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import heronarts.lx.LX;
import heronarts.lx.LXPattern;
import heronarts.lx.LXChannel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.EnumParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.color.LXColor;

import com.symmetrylabs.slstudio.SLStudio;
import com.symmetrylabs.slstudio.model.CubesModel;
import com.symmetrylabs.slstudio.pattern.CubesMappingPattern;
import com.symmetrylabs.slstudio.output.SLController;
import com.symmetrylabs.slstudio.util.listenable.ListListener;

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
    private CubesModel cubesModel;

    private static Map<LX, CubesMappingMode> instanceByLX = new HashMap<>();

    public static synchronized CubesMappingMode getInstance(LX lx) {
        if (!instanceByLX.containsKey(lx)) {
            instanceByLX.put(lx, new CubesMappingMode(lx));
        }
        return instanceByLX.get(lx);
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
            fixturesMappedButNotOnNetwork.add(cube.id);
        }

        // this is dumb
        String[] stringArr = new String[fixturesMappedButNotOnNetwork.size()];
        for (int i = 0; i < stringArr.length; i++) {
            stringArr[i] = fixturesMappedButNotOnNetwork.get(i);
        }

        selectedMappedFixture = new DiscreteParameter("selectedMappedFixture", stringArr);
        selectedUnMappedFixture = new DiscreteParameter("selectedUnMappedFixture", new String[] {"-"});

        SLStudio.applet.controllers.addListener(new ListListener<SLController>() {
            public void itemAdded(final int index, final SLController c) {
                if (isFixtureMapped(c.cubeId)) {
                    fixturesMappedButNotOnNetwork.remove(c.cubeId);
                    fixturesMappedAndOnTheNetwork.add(c.cubeId);
                } else {
                    fixturesOnNetworkButNotMapped.add(c.cubeId);
                }

                String[] stringArr1 = new String[fixturesMappedAndOnTheNetwork.size()];
                for (int i = 0; i < stringArr1.length; i++) {
                    stringArr1[i] = fixturesMappedAndOnTheNetwork.get(i);
                }

                String[] stringArr2 = new String[fixturesOnNetworkButNotMapped.size()];
                for (int i = 0; i < stringArr2.length; i++) {
                    stringArr2[i] = fixturesOnNetworkButNotMapped.get(i);
                }

                selectedMappedFixture.setOptions(stringArr1.length > 0 ? stringArr1 : new String[] {"-"});
                selectedUnMappedFixture.setOptions(stringArr2.length > 0 ? stringArr2 : new String[] {"-"});
            }
            public void itemRemoved(final int index, final SLController c) {}
        });

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
        for (CubesModel.Cube fixture : cubesModel.getCubes()) {
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
