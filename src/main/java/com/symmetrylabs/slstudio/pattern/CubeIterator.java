package com.symmetrylabs.slstudio.pattern;

import java.util.Arrays;
import java.util.List;

import heronarts.lx.LX;
import heronarts.lx.PolyBuffer;
import heronarts.lx.model.LXPoint;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.DiscreteParameter;

import static heronarts.lx.PolyBuffer.Space.SRGB8;

import com.symmetrylabs.layouts.cubes.CubesModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import com.symmetrylabs.color.Ops8;

public class CubeIterator extends SLPattern<CubesModel> {

    private final DiscreteParameter cubeIndex = new DiscreteParameter("Cube Index", 0, 1);

    public CubeIterator(LX lx) {
        super(lx);

        cubeIndex.setRange(0, model.getCubes().size());
        cubeIndex.addListener(param -> {
            CubesModel.Cube cube = model.getCubes().get(cubeIndex.getValuei());
            System.out.println("Cube ID: " + cube.id);
        });

        addParameter(cubeIndex);
    }

    public void run(double deltaMs, PolyBuffer.Space space) {
        int[] colors = (int[]) getArray(SRGB8);
        Arrays.fill(colors, Ops8.BLACK);

        List<CubesModel.Cube> cubes = model.getCubes();
        if (cubeIndex.getValuei() < cubes.size()) {
            for (LXPoint p : cubes.get(cubeIndex.getValuei()).getPoints()) {
                colors[p.index] = Ops8.WHITE;
            }
        }

        markModified(SRGB8);
    }
}
