package com.symmetrylabs.slstudio.pattern.cubes;

import com.symmetrylabs.shows.cubes.*;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.color.LXColor;
import heronarts.lx.transform.LXVector;


public class CubesMappingPattern extends SLPattern<CubesModel> {
    private final SinLFO pulse = new SinLFO(20, 100, 800);

    public int mappedAndOnNetworkColor = LXColor.GREEN;
    public int mappedButNotOnNetworkColor = LXColor.RED;
    public int unMappedButOnNetworkColor = LXColor.BLUE;

    private CubesMappingMode mappingMode;

    public CubesMappingPattern(LX lx) {
        super(lx);

        mappingMode = CubesMappingMode.getInstance(lx);

        addModulator(pulse).start();

        final LXParameterListener resetBasis = new LXParameterListener() {
            public void onParameterChanged(LXParameter p) {
                pulse.setBasis(0);
            }
        };

        mappingMode.mode.addListener(resetBasis);
        mappingMode.displayMode.addListener(resetBasis);
        mappingMode.selectedMappedFixture.addListener(resetBasis);
        mappingMode.selectedUnMappedFixture.addListener(resetBasis);
    }

    public void run(double deltaMs) {
        if (!mappingMode.enabled.isOn()) return;
        setColors(0);
        updateColors();

        if (mappingMode.inMappedMode())
            loopMappedFixtures(deltaMs);
        else loopUnMappedFixtures(deltaMs);
    }

    private void updateColors() {
        mappedButNotOnNetworkColor = lx.hsb(LXColor.h(LXColor.RED), 100, pulse.getValuef());
        unMappedButOnNetworkColor = lx.hsb(LXColor.h(LXColor.BLUE), 100, pulse.getValuef());
    }

    private void loopMappedFixtures(double deltaMs) {
        if (mappingMode.inDisplayAllMode()) {

            for (String id : mappingMode.fixturesMappedAndOnTheNetwork)
                setFixtureColor(id, mappedAndOnNetworkColor);

            for (String id : mappingMode.fixturesMappedButNotOnNetwork)
                setFixtureColor(id, mappedButNotOnNetworkColor);

        } else {
            String selectedId = mappingMode.selectedMappedFixture.getOption();

            for (String id : mappingMode.fixturesMappedAndOnTheNetwork)
                setFixtureColor(id, mappedAndOnNetworkColor, true);

            if (mappingMode.fixturesMappedAndOnTheNetwork.contains(selectedId))
                setFixtureColor(selectedId, mappedAndOnNetworkColor);

            if (mappingMode.fixturesMappedButNotOnNetwork.contains(selectedId))
                setFixtureColor(selectedId, mappedButNotOnNetworkColor);
        }
    }

    private void loopUnMappedFixtures(double deltaMs) {
        for (String id : mappingMode.fixturesMappedAndOnTheNetwork)
            setFixtureColor(id, mappedAndOnNetworkColor, true);
    }

    private void setFixtureColor(String id, int col) {
        setFixtureColor(id, col, false);
    }

    private void setFixtureColor(String id, int col, boolean dotted) {
        if (id.equals("-"))
            return;

        // Do something with all the mapped fixtures on the network
        // we iterate all cubes and call continue here because multiple cubes might have zero as id
        for (CubesModel.Cube c : model.getCubes()) {
            if (!c.id.equals(id))
                continue;

            int i = 0;
            for (LXVector v : (c.getVectorArray())) {
                if (dotted) {
                    col = (i++ % 2 == 0) ? LXColor.scaleBrightness(LXColor.GREEN, 0.2f) : LXColor.BLACK;
                }
                setColor(v.index, col);
            }
        }

        // Do something with all the unmapped fixtures on the network
    }
}
