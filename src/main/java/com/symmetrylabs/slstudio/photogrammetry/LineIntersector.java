package com.symmetrylabs.slstudio.photogrammetry;

import com.symmetrylabs.slstudio.model.SLModel;
import com.symmetrylabs.slstudio.output.DiscoverableController;
import com.symmetrylabs.slstudio.pattern.base.SLPattern;
import com.symmetrylabs.util.Marker;
import com.symmetrylabs.util.MarkerSource;
import com.symmetrylabs.util.SphereMarker;
import heronarts.lx.LX;
import heronarts.lx.color.LXColor;
import heronarts.lx.parameter.DiscreteParameter;
import processing.core.PVector;

import javax.sound.sampled.Line;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class LineIntersector extends SLPattern<SLModel> implements MarkerSource {
    DiscreteParameter x = new DiscreteParameter("x", 300);
    DiscreteParameter y = new DiscreteParameter("y", 300);
    DiscreteParameter z = new DiscreteParameter("z", 300);

    DiscreteParameter x0 = new DiscreteParameter("x0", -100, 300);
    DiscreteParameter y0 = new DiscreteParameter("y0", -100, 300);
    DiscreteParameter z0 = new DiscreteParameter("z0", -100, 300);

    public LineIntersector(LX lx){
        super(lx);
        addParam(x);
        addParam(y);
        addParam(z);
        addParam(x0);
        addParam(y0);
        addParam(z0);
    }

    @Override
    public Collection<Marker> getMarkers() {
        List<Marker> markers = new ArrayList<>();


        int OFFSET = 100;

        // line one - camera projection one
        markers.add(new SphereMarker(new PVector(x.getValuei(), 0, 0), 12f, LXColor.RED));
        markers.add(new VectorMarker(new PVector(x.getValuei(), 0, 0), new PVector(200, y.getValuei(), z.getValuei()), 12f, LXColor.GREEN));

        Line3D line2 = new Line3D(new PVector(x0.getValuei() + OFFSET, 0 + OFFSET, 0 + OFFSET), new PVector(200, y0.getValuei(), z0.getValuei()));
        // line two - camera projection two
        markers.add(new SphereMarker(new PVector(x0.getValuei() + OFFSET, 0 + OFFSET, 0 + OFFSET), 12f, LXColor.RED));
        markers.add(new VectorMarker(line2, 12f, LXColor.GREEN));
        return markers;
    }
}
