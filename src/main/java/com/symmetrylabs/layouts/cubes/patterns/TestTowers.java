package com.symmetrylabs.layouts.cubes.patterns;

import com.symmetrylabs.layouts.cubes.CubesModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import heronarts.lx.LX;
import heronarts.lx.PolyBuffer;
import heronarts.lx.color.LXColor;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.modulator.SinLFO;

import static com.symmetrylabs.util.MathUtils.random;
import static heronarts.lx.PolyBuffer.Space.SRGB8;
import static processing.core.PApplet.floor;


public class TestTowers extends SLPattern<CubesModel> {

    public final DiscreteParameter selectedTower;

    public final SinLFO pulse = new SinLFO("pulse", 35, 90, 1500);

    public TestTowers(LX lx) {
        super(lx);
        this.selectedTower = new DiscreteParameter("selectedTower", 0, 0, model.getTowers().size()-1);
        selectedTower.setOptions(new String[] {"LEFT_FACE", "RIGHT_FACE", "A", "B", "C", "D", "E", "Z", "Y", "X", "W", "V", "U", "R", "Q"});
        addParameter(selectedTower);
        addModulator(pulse).start();
    }

    public void run(double deltaMs) {
        int i = 0;

        for (CubesModel.Tower tower : model.getTowers()) {
            int hue = (i++*35) % 360;

            for (LXPoint p : tower.getPoints()) {
                colors[p.index] = lx.hsb(
                    hue, 100, 30
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
