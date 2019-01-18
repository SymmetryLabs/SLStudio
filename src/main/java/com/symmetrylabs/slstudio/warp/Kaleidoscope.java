package com.symmetrylabs.slstudio.warp;

import com.symmetrylabs.util.CubeMarker;
import com.symmetrylabs.util.Marker;

import java.util.ArrayList;
import java.util.List;

import heronarts.lx.LX;
import heronarts.lx.parameter.BooleanParameter;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import heronarts.lx.transform.LXProjection;
import heronarts.lx.transform.LXVector;
import heronarts.lx.warp.LXWarp;
import processing.core.PVector;

public class Kaleidoscope extends SLWarp {
    private CompoundParameter cxParam = new CompoundParameter("cx", model.cx, model.xMin, model.xMax);
    private CompoundParameter cyParam = new CompoundParameter("cy", model.cy, model.yMin, model.yMax);
    private CompoundParameter czParam = new CompoundParameter("cz", model.cz, model.zMin, model.zMax);
    private CompoundParameter latParam = new CompoundParameter("lat", 0, -90, 90);
    private CompoundParameter lonParam = new CompoundParameter("lon", 0, -180, 180);
    private CompoundParameter turnParam = new CompoundParameter("turn", 0, -180, 180);
    private DiscreteParameter orderParam = new DiscreteParameter("order", 6, 1, 12);
    private BooleanParameter flipParam = new BooleanParameter("flip", true);

    private static final float TAU = (float) (2 * Math.PI);

    public Kaleidoscope(LX lx) {
        super(lx);

        addParameter(cxParam);
        addParameter(cyParam);
        addParameter(czParam);
        addParameter(latParam);
        addParameter(lonParam);
        addParameter(turnParam);
        addParameter(orderParam);
        addParameter(flipParam);
    }

    @Override
    public boolean run(double deltaMs, boolean inputVectorsChanged) {
        if (inputVectorsChanged || getAndClearParameterChangeDetectedFlag()) {
            System.out.println("Recomputing Kaleidoscope warp (" + inputVectors.length + " vectors)...");
            float cx = cxParam.getValuef();
            float cy = cyParam.getValuef();
            float cz = czParam.getValuef();
            float lat = latParam.getValuef();
            float lon = lonParam.getValuef();
            float turn = turnParam.getValuef();
            int order = orderParam.getValuei();
            boolean flip = flipParam.getValueb();

            LXProjection proj = new LXProjection(inputVectors);
            proj.translate(-cx, -cy, -cz);
            proj.rotateY(-lon * TAU / 360);
            proj.rotateX(-lat * TAU / 360);
            LXVector[] projected = proj.getArray();

            for (int i = 0; i < inputVectors.length; i++) {
                if (projected[i] != null) {
                    LXVector v = projected[i];
                    double r = Math.hypot(v.x, v.y);
                    double revs = Math.atan2(v.y, v.x)/TAU;
                    revs += turn/360;

                    // Replicate the rotation according to the order of rotational symmetry.
                    if (revs < 0) revs += 1 - ((int) revs);
                    revs -= Math.floor(revs);
                    revs *= order;
                    int count = (int) revs;
                    double frac = revs - count;
                    if (flip && count % 2 > 0) frac = 1 - frac;
                    frac /= order;

                    v.x = (float) (r * Math.cos(frac * TAU));
                    v.y = (float) (r * Math.sin(frac * TAU));
                }
            }

            proj.rotateX(lat * TAU / 360);
            proj.rotateY(lon * TAU / 360);
            proj.translate(cx, cy, cz);

            for (int i = 0; i < inputVectors.length; i++) {
                outputVectors[i] = projected[i];
            }
            return true;
        }
        return false;
    }

    @Override public List<Marker> getMarkers() {
        List<Marker> markers = new ArrayList<>();
        return markers;
    }
}
