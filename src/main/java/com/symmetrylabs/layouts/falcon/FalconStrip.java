package com.symmetrylabs.layouts.falcon;

import com.symmetrylabs.slstudio.model.Strip;
//import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.StripsModel;
import heronarts.lx.model.LXAbstractFixture;
import heronarts.lx.model.LXPoint;
import heronarts.lx.transform.LXTransform;

import java.util.ArrayList;
import java.util.List;

import static com.symmetrylabs.layouts.falcon.FalconLayout.INCHES_PER_METER;
import static com.symmetrylabs.util.MathConstants.PI;
import static com.symmetrylabs.util.MathUtils.floor;

public class FalconStrip extends StripsModel<Strip> {
    public final String id;
    public static class Metrics extends Strip.Metrics {
        public final float length;
        public final int ledsPerMeter;

        public final float POINT_SPACING;
        public final float NUM_STRIPS;
        public Metrics(int numPoints, float spacing, int numStrips) {
            super(numPoints);
            this.length = (numPoints -1)* spacing;
            this.ledsPerMeter = floor((INCHES_PER_METER / this.length) * numPoints);
            this.POINT_SPACING = spacing;
            this.NUM_STRIPS = numStrips;

        }
    }
        //    public final Metrics metrics;
    final FalconStrip.Metrics metrics;
    public FalconStrip(String id, LXTransform transform, Metrics metrics) {
        super(new Fixture(id, transform, metrics))    ;
        this.metrics = metrics;
        //metrics = new FalconStrip.Metrics(numPoints, spacing);

        this.id = id;

        //Metrics stripMetrics = new Metrics(this.numPoints, this.spacing);



    }
    public FalconStrip(String id, float x, float y, float z, float rx, float ry, float rz, LXTransform transform, Metrics metrics) {
        super(new Fixture(id, x, y, z, rx, ry, rz, transform, metrics))    ;
        this.metrics = metrics;
        //metrics = new FalconStrip.Metrics(numPoints, spacing);

        this.id = id;

        //Metrics stripMetrics = new Metrics(this.numPoints, this.spacing);



    }
private static class Fixture extends LXAbstractFixture {

    private Fixture(String id, LXTransform t, FalconStrip.Metrics metrics) {
        t.push();
//        t.translate(x, y, z);
//        t.rotateX(xr * PI / 180f);
//        t.rotateY(yr * PI / 180f);
//        t.rotateZ(zr * PI / 180f);

            List<LXPoint> points = new ArrayList<>();
            for (int iPoint = 0; iPoint < metrics.numPoints; iPoint++) {
                points.add(new LXPoint(t.x(), t.y(), t.z()));
                //t.translate(metrics.ledsPerMeter, 0, 0);
                t.translate(metrics.POINT_SPACING, 0, 0);
            }

            Strip strip = new Strip(id + "_strip" , new Strip.Metrics(metrics.numPoints, metrics.pixelPitch), points);
            //this.strips.add(strip);

            for (LXPoint point : strip.points) {
                this.points.add(point);
            }

            //t.translate(0, 0, 0.5f);
            //t.rotateZ(PI);

        t.pop();
    }
    private Fixture(String id, float x, float y, float z, float xr, float yr, float zr, LXTransform t, FalconStrip.Metrics metrics) {
        t.push();
        t.translate(x, y, z);
        t.rotateX(xr * PI / 180f);
        t.rotateY(yr * PI / 180f);
        t.rotateZ(zr * PI / 180f);

        List<LXPoint> points = new ArrayList<>();
        for (int iPoint = 0; iPoint < (metrics.numPoints); iPoint++) {
            points.add(new LXPoint(t.x(), t.y(), t.z()));
            //t.translate(metrics.ledsPerMeter, 0, 0);
            t.translate(metrics.POINT_SPACING, 0, 0);
        }

        Strip strip = new Strip(id + "_strip" , new Strip.Metrics(metrics.numPoints, metrics.pixelPitch), points);
        //this.strips.add(strip);

        for (LXPoint point : strip.points) {
            this.points.add(point);
        }

        //t.translate(0, 0, 0.5f);
        //t.rotateZ(PI);

        t.pop();
    }

    }
}
