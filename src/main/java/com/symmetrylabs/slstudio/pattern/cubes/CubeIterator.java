package com.symmetrylabs.slstudio.pattern.cubes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import com.symmetrylabs.slstudio.ui.v2.GdxGraphicsAdapter;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.utils.Align;

import com.symmetrylabs.shows.cubes.CubesShow;
import heronarts.lx.LX;
import heronarts.lx.PolyBuffer;
import heronarts.lx.model.LXPoint;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import processing.core.PVector;

import static heronarts.lx.PolyBuffer.Space.SRGB8;

import com.symmetrylabs.shows.cubes.CubesModel;
import com.symmetrylabs.slstudio.SLStudioLX;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import com.symmetrylabs.color.Ops8;
import com.symmetrylabs.util.Marker;
import com.symmetrylabs.util.MarkerSource;
import com.symmetrylabs.util.TextMarker;

public class CubeIterator extends SLPattern<CubesModel> implements MarkerSource {
    public static final String GROUP_NAME = CubesShow.SHOW_NAME;

    private final DiscreteParameter cubeIndex = new DiscreteParameter("CubeIndex", 0, 1);
    private final CompoundParameter labelSize = new CompoundParameter("LabelSize", 8, 2, 18);
    private String caption = null;

    public CubeIterator(LX lx) {
        super(lx);

        cubeIndex.setRange(0, model.getCubes().size());
        cubeIndex.addListener(param -> {
            CubesModel.Cube cube = model.getCubes().get(cubeIndex.getValuei());
            caption = String.format(
                Locale.US, "Cube %s: center at (%+6.1f, %+6.1f, %+6.2f)",
                cube.modelId, cube.cx, cube.cy, cube.cz);
            System.out.println(caption);
        });

        addParameter(cubeIndex);
        addParameter(labelSize);
    }

    public void run(double deltaMs, PolyBuffer.Space space) {
        int[] colors = (int[]) getArray(SRGB8);
        Arrays.fill(colors, Ops8.BLACK);

        List<CubesModel.Cube> cubes = model.getCubes();
        if (cubeIndex.getValuei() < cubes.size()) {
            for (LXPoint p : cubes.get(cubeIndex.getValuei()).getPoints()) {
                colors[p.index] = 0xffffff80;
            }
        }

        markModified(SRGB8);
    }

    @Override
    public String getCaption() {
        return caption;
    }

    @Override public Collection<Marker> getMarkers() {
        List<Marker> markers = new ArrayList<>();
        List<CubesModel.Cube> cubes = model.getCubes();
        float size = labelSize.getValuef();
        int cubeI = cubeIndex.getValuei();
        PVector pos = new PVector();
        int i = 0;
        for (CubesModel.Cube cube : cubes) {
            pos.set(cube.cx, cube.cy, cube.cz);
            int c = (i == cubeI) ? 0xffffff00 : 0x80008040;
            String label = cube.id;
            if (cube instanceof CubesModel.DoubleControllerCube) {
                CubesModel.DoubleControllerCube dc = (CubesModel.DoubleControllerCube) cube;
//                label = dc.idA + "\n" + dc.idB;
                label = dc.idB + "\n" + dc.idA;
            }
            markers.add(new TextMarker(pos, size, c, label));
            i++;
        }
        return markers;
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
        final Vector3 scale = new Vector3(0.2f, 0.2f, 0.2f);
        for (CubesModel.Cube cube : cubes) {
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
