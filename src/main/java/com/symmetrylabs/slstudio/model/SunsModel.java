package com.symmetrylabs.slstudio.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import heronarts.lx.model.LXAbstractFixture;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;

import static com.symmetrylabs.slstudio.model.SunsModel.PointBatches.NUM_POINT_BATCHES;
import static processing.core.PApplet.*;

/**
 * Top-level model of the entire sculpture. This contains a list of every cube on the sculpture, which forms a hierarchy
 * of faces, strips and points.
 */
public class SunsModel extends StripsModel<CurvedStrip> implements SectionalModel<Sun> {
    public final List<LXModel> objModels;

    // Suns
    protected final List<Sun> suns = new ArrayList<>();
    protected final Map<String, Sun> sunTable = new HashMap<>();
    protected final Sun masterSun;

    // Slices
    protected final List<Slice> slices = new ArrayList<>();
    protected final Map<String, Slice> sliceTable = new HashMap<>();

    private final List<Sun> sunsUnmodifiable = Collections.unmodifiableList(suns);
    private final List<Slice> slicesUnmodifiable = Collections.unmodifiableList(slices);

    // Array of points stored as contiguous floats for performance
    public final float[] pointsArray;
    public final float[] pointsX;
    public final float[] pointsY;
    public final float[] pointsZ;

    protected final PointBatches pointBatches;

    public SunsModel() {
        this(new ArrayList<>());
    }

    public SunsModel(List<Sun> suns) {
        super(new Fixture(suns));

        for (Sun sun : suns) {
            this.suns.add(sun);
            this.sunTable.put(sun.id, sun);

            for (Slice slice : sun.slices) {
                this.slices.add(slice);
                this.sliceTable.put(slice.id, slice);
                this.strips.addAll(slice.getStrips());
            }
        }

        masterSun = this.sunTable.get("sun9"); // a full sun
        for (Sun sun : suns) {
            if (sun != masterSun) {
                sun.computeMasterIndexes(masterSun);
            }
        }

        this.objModels = new ArrayList<>();

        this.pointsArray = new float[this.points.length * 3];
        this.pointsX = new float[this.points.length];
        this.pointsY = new float[this.points.length];
        this.pointsZ = new float[this.points.length];
        for (int i = 0; i < this.points.length; i++) {
            LXPoint point = this.points[i];
            this.pointsArray[3 * i] = point.x;
            this.pointsArray[3 * i + 1] = point.y;
            this.pointsArray[3 * i + 2] = point.z;
            this.pointsX[i] = point.x;
            this.pointsY[i] = point.y;
            this.pointsZ[i] = point.z;
        }

        this.pointBatches = new PointBatches(Arrays.asList(points), NUM_POINT_BATCHES);
    }

    @Override
    public List<Sun> getSections() {
        return getSuns();
    }

    public List<Sun> getSuns() {
        return sunsUnmodifiable;
    }

    public List<Slice> getSlices() {
        return slicesUnmodifiable;
    }

    public Sun getMasterSun() {
        return masterSun;
    }

    private static class Fixture extends LXAbstractFixture {
        private Fixture(List<Sun> suns) {
            for (Sun sun : suns) {
                for (LXPoint point : sun.points) {
                    this.points.add(point);
                }
            }
        }
    }

    public Sun getSunById(String id) {
        return sunTable.get(id);
    }

    public Slice getSliceById(String id) {
        Slice slice = sliceTable.get(id);
        if (slice == null) {
            println("Missing slice id: " + id);
            print("Valid ids: ");
            for (String key : sliceTable.keySet()) {
                print(key + ", ");
            }
            println();
            throw new IllegalArgumentException("Invalid slice id:" + id);
        }
        return slice;
    }

    public void forEachPoint(final BatchConsumer consumer) {
        this.pointBatches.forEachPoint(consumer);
    }

    public static class PointBatches {
        private final List<LXPoint> points;
        private final int batchCount;
        private final ArrayList<PointBatch> pointBatches;

        public static final int NUM_POINT_BATCHES = 64;

        public PointBatches(List<LXPoint> points, int batchCount) {
            this.points = points;
            this.batchCount = batchCount;

            this.pointBatches = new ArrayList<>(batchCount);

            int batchStride = ceil(points.size() / batchCount);
            for (int i = 0; i < batchCount; i++) {
                int start = i * batchStride;
                int end = min(start + batchStride, points.size() - 1);
                pointBatches.add(new PointBatch(start, end));
            }
        }

        public void forEachPoint(
            final BatchConsumer consumer
        ) {
            pointBatches.parallelStream().forEach(batch -> consumer.accept(batch.start, batch.end));
        }
    }

    public static class PointBatch {
        public final int start;
        public final int end;

        public PointBatch(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }
}
