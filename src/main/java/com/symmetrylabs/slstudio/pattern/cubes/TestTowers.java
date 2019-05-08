package com.symmetrylabs.slstudio.pattern.cubes;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;
import com.symmetrylabs.shows.cubes.CubesModel;
import com.symmetrylabs.shows.cubes.CubesShow;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import com.symmetrylabs.slstudio.ui.v2.GdxGraphicsAdapter;
import heronarts.lx.LX;
import heronarts.lx.model.LXPoint;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.modulator.SinLFO;

import java.util.ArrayList;
import java.util.List;


public class TestTowers extends SLPattern<CubesModel> {
    public static final String GROUP_NAME = CubesShow.SHOW_NAME;

    public final DiscreteParameter selectedTower;
    public final CompoundParameter bright = new CompoundParameter("color", 60, 0, 100);
    public final CompoundParameter textSize = new CompoundParameter("textSize", 0.2, 0.01, 1);

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
        addParameter(bright);
        addParameter(textSize);
        addModulator(pulse).start();
    }

    public void run(double deltaMs) {
        int i = 0;

        for (CubesModel.Tower tower : model.getTowers()) {
            int hue = (i++*55) % 360;

            for (LXPoint p : tower.getPoints()) {
                colors[p.index] = lx.hsb(
                    hue, 100, bright.getValuef()
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

    @Override
    public void drawTextMarkers(GdxGraphicsAdapter g) {
        List<CubesModel.Cube> cubes = model.getCubes();
        if (cubes.isEmpty()) {
            return;
        }
        final float targetWidth = cubes.get(0).xRange;
        final int halign = Align.topLeft;
        final Quaternion rot = new Quaternion();
        final float ts = textSize.getValuef();
        final Vector3 scale = new Vector3(ts, ts, ts);
        for (CubesModel.Cube cube : model.getTowers().get(selectedTower.getValuei()).getCubes()) {
            g.textBatch.setTransformMatrix(new Matrix4(
                new Vector3(
                    cube.xMin + 0.1f * cube.xRange,
                    cube.yMax - 0.1f * cube.yRange,
                    cube.cz),
                rot, scale));
            g.font.draw(g.textBatch, cube.modelId, 0, 0, targetWidth, halign, false);
        }
    }

    @Override
    public boolean drawLineMarkers(GdxGraphicsAdapter g) {
        // mark ourselves as prefering the new direct-draw API
        return true;
    }
}
