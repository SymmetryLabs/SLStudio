package com.symmetrylabs.slstudio.pattern.cubes;

import com.symmetrylabs.shows.cubes.CubesMappingMode;
import com.symmetrylabs.shows.cubes.CubesModel;
import com.symmetrylabs.shows.cubes.NateMappingMode;
import com.symmetrylabs.slstudio.output.PointsGrouping;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.modulator.SinLFO;
import heronarts.lx.parameter.LXParameter;
import heronarts.lx.parameter.LXParameterListener;


public class NateCubesMappingForkPattern extends SLPattern<CubesModel> {
    private final SinLFO pulse = new SinLFO(20, 100, 800);

    public int mappedAndOnNetworkColor = LXColor.GREEN;
    public int mappedButNotOnNetworkColor = LXColor.RED;
    public int unMappedButOnNetworkColor = LXColor.BLUE;

    private NateMappingMode mappingMode;

    public NateCubesMappingForkPattern(LX lx) {
        super(lx);

        mappingMode = NateMappingMode.getInstance(lx);

        addModulator(pulse).start();

        final LXParameterListener resetBasis = new LXParameterListener() {
            public void onParameterChanged(LXParameter p) {
                pulse.setBasis(0);
            }
        };

        mappingMode.mode.addListener(resetBasis);
        mappingMode.displayMode.addListener(resetBasis);
        mappingMode.selectedModelFixture.addListener(resetBasis);
    }

    public void run(double deltaMs) {
        if (!mappingMode.enabled.isOn()) return;
        setColors(0);
        updateColors();

        if (mappingMode.inMappedMode())
            loopMappedFixtures(deltaMs);
//        else loopUnMappedFixtures(deltaMs);
    }

    private void updateColors() {
        mappedButNotOnNetworkColor = lx.hsb(LXColor.h(LXColor.RED), 100, pulse.getValuef());
        unMappedButOnNetworkColor = lx.hsb(LXColor.h(LXColor.BLUE), 100, pulse.getValuef());
    }

    private void loopMappedFixtures(double deltaMs) {
        String selectedId = mappingMode.selectedModelFixture.getOption();

        for (String id : mappingMode.modelFixtures)
            setFixtureColor(id, mappedAndOnNetworkColor, true);

        if (mappingMode.modelFixtures.contains(selectedId))
            setFixtureColor(selectedId, mappedAndOnNetworkColor);
    }

//    private void loopUnMappedFixtures(double deltaMs) {
//        for (String id : mappingMode.fixturesMappedAndOnTheNetwork)
//            setFixtureColor(id, mappedAndOnNetworkColor, true);
//    }

    private void setFixtureColor(String id, int col) {
        setFixtureColor(id, col, false);
    }

    private void setFixtureColor(String id, int col, boolean dotted) {
        if (id.equals("-"))
            return;

        // Do something with all the mapped fixtures on the network
        // we iterate all cubes and call continue here because multiple cubes might have zero as id

        // made specific double controller cube
//        for (CubesModel.Cube c : model.getCubes()) {
//            CubesModel.DoubleControllerCube cc = (CubesModel.DoubleControllerCube) c;
//            if (!cc.idA.equals(id) && !cc.idB.equals(id))
//                // neither controller a match
//                continue;
//
//            PointsGrouping pointsGrouping = (cc.idA.equals(id)) ? cc.getPointsA() : cc.getPointsB();
//            int i = 0;
//            for (LXPoint v : pointsGrouping.getPoints()) {
//                if (dotted) {
//                    col = (i++ % 2 == 0) ? LXColor.scaleBrightness(LXColor.GREEN, 0.2f) : LXColor.BLACK;
//                }
//                colors[v.index] = col;
//            }
//        }
    }
}
