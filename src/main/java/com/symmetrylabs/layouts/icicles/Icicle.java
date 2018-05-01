package com.symmetrylabs.layouts.icicles;

import java.util.List;
import java.util.ArrayList;

import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.StripsModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.model.LXAbstractFixture;
import heronarts.lx.transform.LXTransform;

import static com.symmetrylabs.util.MathUtils.*;
import static com.symmetrylabs.util.DistanceConstants.*;
import static com.symmetrylabs.util.MathConstants.*;

public class Icicle extends StripsModel<Strip> {

    public final String id;

    public final Metrics metrics;

    public Icicle(String id, float x, float y, float z, float rx, float ry, float rz, LXTransform t, Metrics metrics) {
        super(new Fixture(id, x, y, z, rx, ry, rz, t, metrics));
        Fixture fixture = (Fixture) this.fixtures.get(0);

        this.id = id;
        this.metrics = metrics;
        this.strips.addAll(fixture.strips);
    }

    public static class Metrics {
        public final int numPoints;
        public final float pixelPitch;

        public Metrics(int numPoints, float pixelPitch) {
            this.numPoints = numPoints;
            this.pixelPitch = pixelPitch;
        }
    }

    private static class Fixture extends LXAbstractFixture {

        private final List<Strip> strips = new ArrayList<>();

        private Fixture(String id, float x, float y, float z, float xr, float yr, float zr, LXTransform t, Icicle.Metrics metrics) {
            t.push();
            t.translate(x, y, z);
            t.rotateX(xr * PI / 180f);
            t.rotateY(yr * PI / 180f);
            t.rotateZ(zr * PI / 180f);

            for (int iStrip = 0; iStrip < 2; iStrip++) {
                List<LXPoint> points = new ArrayList<>();
                for (int iPoint = 0; iPoint < metrics.numPoints; iPoint++) {
                    points.add(new LXPoint(t.x(), t.y(), t.z()));
                    t.translate(metrics.pixelPitch, 0, 0);
                }

                Strip strip = new Strip(id + "_strip" + iStrip, new Strip.Metrics(metrics.numPoints, metrics.pixelPitch), points);
                this.strips.add(strip);

                for (LXPoint point : strip.points) {
                    this.points.add(point);
                }

                t.translate(0, 0, 0.5f);
                t.rotateZ(PI);
            }
            t.pop();
        }
    }
}
