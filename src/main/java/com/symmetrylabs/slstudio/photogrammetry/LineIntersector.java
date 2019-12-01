package com.symmetrylabs.slstudio.photogrammetry;

import com.symmetrylabs.slstudio.ApplicationState;
import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import com.symmetrylabs.util.CubeMarker;
import com.symmetrylabs.util.Marker;
import com.symmetrylabs.util.MarkerSource;
import com.symmetrylabs.util.SphereMarker;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.CompoundParameter;
import heronarts.lx.parameter.DiscreteParameter;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class LineIntersector extends SLPattern<SLModel> implements MarkerSource {
    DiscreteParameter x0p = new DiscreteParameter("x0p", -300, 300);
    DiscreteParameter y0p = new DiscreteParameter("y0p", -300, 300);
    DiscreteParameter z0p = new DiscreteParameter("z0p", -300, 300);

    DiscreteParameter x1p = new DiscreteParameter("x1p", -300, 300);
    DiscreteParameter y1p = new DiscreteParameter("y1p", -300, 300);
    DiscreteParameter z1p = new DiscreteParameter("z1p", -300, 300);

    CompoundParameter t = new CompoundParameter("t");
    CompoundParameter k = new CompoundParameter("k");

    public LineIntersector(LX lx){
        super(lx);
        addParam(x0p);
        addParam(y0p);
        addParam(z0p);

        addParam(x1p);
        addParam(y1p);
        addParam(z1p);

        addParam(t);
        addParam(k);
    }

    @Override
    public Collection<Marker> getMarkers() {
        List<Marker> markers = new ArrayList<>();


        int x0 = x0p.getValuei();
        int y0 = y0p.getValuei();
        int z0 = z0p.getValuei();

        int x1 = x1p.getValuei();
        int y1 = y1p.getValuei();
        int z1 = z1p.getValuei();


        int OFFSET = 100;

        PVector vec0 = new PVector(x0, y0, z0);
        PVector vec1 = new PVector(x1, y1, z1);
        Line3D V_s = new Line3D(vec0, vec1);

        // line one - camera projection one
        markers.add(new SphereMarker(vec0, 12f, LXColor.RED));
        markers.add(new VectorMarker(V_s, 12f, LXColor.BLUE));

        PVector vec2 = new PVector(-500, 0, 0);
        PVector vec3 = new PVector(500, 0, 0);

        Line3D V_r = new Line3D(vec2, vec3);
        // line two - camera projection two
        markers.add(new SphereMarker(vec2, 12f, LXColor.RED));
        markers.add(new VectorMarker(V_r, 12f, LXColor.GREEN));

        // scan through the points on one of the two lines and converge on the smallest distance
        float interval = 0.001f;
        float bestDist = 1E15f; // a big number
        float bestScan = 0;
        for (float scan = 0; scan < 1; scan += interval){
            PVector pointOn_V_r = V_r.getLerpPoint(scan);
            float dist = V_s.distFromPoint(pointOn_V_r);
            if (dist < bestDist){
                bestDist = dist;
                bestScan = scan;
            }
        }

        // match point
        PVector pointOn_V_r = V_r.getLerpPoint(bestScan);
        markers.add(new SphereMarker(pointOn_V_r, 12f, 0xfff0f0f0));

        bestDist = 1E15f; // a big number
        bestScan = 0;

        for (float scan = 0; scan < 1; scan += interval){
            PVector pointOn_V_s = V_s.getLerpPoint(scan);
            float dist = V_r.distFromPoint(pointOn_V_s);
            if (dist < bestDist){
                bestDist = dist;
                bestScan = scan;
            }
        }

        // match point
        PVector pointOn_V_s = V_s.getLerpPoint(bestScan);
        markers.add(new SphereMarker(pointOn_V_s, 12f, 0xfff0f0f0));

        Line3D V_best = new Line3D(pointOn_V_s, pointOn_V_r);
        markers.add(new VectorMarker(V_best, 36f, LXColor.RED));

        ApplicationState.setWarning("lerp", "" + bestScan);


        return markers;
    }
}
