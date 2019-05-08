package com.symmetrylabs.slstudio.pattern.cubes;

import com.symmetrylabs.shows.cubes.CubesModel;
import com.symmetrylabs.shows.cubes.CubesShow;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.modulator.SinLFO;

import java.util.ArrayList;
import java.util.List;


public class TestTowers extends SLPattern<CubesModel> {
    public static final String GROUP_NAME = CubesShow.SHOW_NAME;

    public final DiscreteParameter selectedTower;

    public final SinLFO pulse = new SinLFO("pulse", 35, 90, 1500);

    public TestTowers(LX lx) {
        super(lx);
        this.selectedTower = new DiscreteParameter("selectedTower", 0, 0, model.getTowers().size()-1);
        List<String> towerNames = new ArrayList<>();
        for (CubesModel.Tower tower : model.getTowers()) {
            towerNames.add(tower.id);
        }
        String[] paramOptions = new String[towerNames.size()];
        towerNames.toArray(paramOptions);
        selectedTower.setOptions(paramOptions);
        addParameter(selectedTower);
        addModulator(pulse).start();
    }

    public void run(double deltaMs) {
        int i = 0;

        for (CubesModel.Tower tower : model.getTowers()) {
            int hue = (i++*55) % 360;

            for (LXPoint p : tower.getPoints()) {
                colors[p.index] = lx.hsb(
                    hue, 100, 60
                );
            }
        }

        CubesModel.Tower tower = model.getTowers().get(selectedTower.getValuei());

        for (LXPoint p : tower.getPoints()) {
            colors[p.index] = lx.hsb(
                0, 0, pulse.getValuef()
            );
        }
    }

}
