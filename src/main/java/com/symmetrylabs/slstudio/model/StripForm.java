package com.symmetrylabs.slstudio.model;

import com.google.gson.annotations.Expose;
import heronarts.lx.model.LXAbstractFixture;
import heronarts.lx.model.LXFixture;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.transform.LXTransform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StripForm extends LXAbstractFixture {
    public final String id;

    // local coordinate system
    @Expose
    public final Metrics metrics;

    // for Lattice
    public Object obj1 = null, obj2 = null;

    public StripForm(String id, Metrics metrics) {
        this.id = id;
        this.metrics = metrics;


        createPointsFromMetrics();
    }

    private void createPointsFromMetrics() {
        LXTransform transform = new LXTransform();
        for (int i = 0; i < this.metrics.numPoints; i++){
            LXPoint point = new LXPoint(transform.x(), transform.y(), transform.z());
            points.add(point);
            transform.translate((float)metrics.pixelPitch, 0, 0);
        }
    }

    @Override
    public List<LXPoint> getPoints() {
        return points;
    }


    public static class Metrics {
        @Expose
        public final int numPoints;

        @Expose
        public final double pixelPitch;

        public Metrics(int numPoints) {
            // 1.0 default pitch i guess...
            this(numPoints, 1.0);
        }

        /*
        Metrics - Enough metadata to fully specify the form of a thing.
        note: this intentionally does not specify the location/orientation of the form
        TODO?> Factor in 'FormMetrics' which are metrics specific to form?
         */
        public Metrics(int numPoints, double pitch) {
            this.numPoints = numPoints;
            this.pixelPitch = pitch;
        }
    }
}
