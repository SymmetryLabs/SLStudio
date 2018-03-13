package com.symmetrylabs.layouts.icicles;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

import com.symmetrylabs.slstudio.model.Strip;
import com.symmetrylabs.slstudio.model.StripsModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.model.LXAbstractFixture;
import heronarts.lx.transform.LXTransform;

import static com.symmetrylabs.util.MathUtils.*;
import static com.symmetrylabs.util.DistanceConstants.*;
import static com.symmetrylabs.util.MathConstants.*;

/**
 * Top-level model of the entire sculpture. This contains a list of
 * every cube on the sculpture, which forms a hierarchy of faces, strips
 * and points.
 */
public class IcicleModel extends StripsModel<Strip> {
    protected final List<Icicle> icicles = new ArrayList<>();
    protected final Map<String, Icicle> icicleTable = new HashMap<>();

    private final List<Icicle> iciclesUnmodifiable = Collections.unmodifiableList(icicles);

    private final Icicle[] _icicles;

    public IcicleModel() {
        this(new ArrayList<>(), new Icicle[0]);
    }

    public IcicleModel(List<Icicle> icicles, Icicle[] icicleArr) {
        super(new Fixture(icicleArr));
        Fixture fixture = (Fixture) this.fixtures.get(0);

        _icicles = icicleArr;

        for (Icicle icicle : icicles) {
            if (icicle != null) {
                this.icicleTable.put(icicle.id, icicle);
                this.icicles.add(icicle);
            }
        }
    }

    public List<Icicle> getIcicles() {
        return iciclesUnmodifiable;
    }

    private static class Fixture extends LXAbstractFixture {
        private Fixture(Icicle[] icicleArr) {
            for (Icicle icicle : icicleArr) {
                if (icicle != null) {
                    for (LXPoint point : icicle.points) {
                        this.points.add(point);
                    }
                }
            }
        }
    }

    /**
     * TODO(mcslee): figure out better solution
     *
     * @param index
     * @return
     */
    public Icicle getIcicleByRawIndex(int index) {
        return _icicles[index];
    }

    public Icicle getIcicleById(String id) {
        return this.icicleTable.get(id);
    }

    public static class Icicle extends Strip {

        public static final float NUM_POINTS = 72;
        public static final float PIXEL_PITCH = 1;

        public final String id;
        public final float x;
        public final float y;
        public final float z;
        public final float rx;
        public final float ry;
        public final float rz;

        public Icicle(String id, float x, float y, float z, float rx, float ry, float rz, LXTransform t) {
            super(id, new Metrics(144), new Fixture(x, y, z, rx, ry, rz, t));
            Fixture fixture = (Fixture) this.fixtures.get(0);
            this.id = id;

            while (rx < 0) rx += 360;
            while (ry < 0) ry += 360;
            while (rz < 0) rz += 360;
            rx = rx % 360;
            ry = ry % 360;
            rz = rz % 360;

            this.x = x;
            this.y = y;
            this.z = z;
            this.rx = rx;
            this.ry = ry;
            this.rz = rz;
        }

        private static class Fixture extends LXAbstractFixture {

            private Fixture(float x, float y, float z, float rx, float ry, float rz, LXTransform t) {
                // LXTransform t = new LXTransform();
                t.push();
                t.translate(x, y, z);
                t.rotateX(rx * Math.PI / 180.);
                t.rotateY(ry * Math.PI / 180.);
                t.rotateZ(rz * Math.PI / 180.);

                for (int i1 = 0; i1 < 2; i1++) {
                    for (int i = 0; i < NUM_POINTS; i++) {
                        LXPoint point = new LXPoint(t.x(), t.y(), t.z());
                        this.points.add(point);
                        t.translate(PIXEL_PITCH, 0, 0);
                    }
                    t.rotateZ(PI);
                    t.translate(0, 0.25f, 0);
                }

                t.pop();
            }
        }
    }
}
