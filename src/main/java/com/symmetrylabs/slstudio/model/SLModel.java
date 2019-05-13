    package com.symmetrylabs.slstudio.model;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import org.apache.commons.collections4.IteratorUtils;

import org.apache.commons.math3.util.FastMath;

import heronarts.lx.model.LXFixture;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;

import com.symmetrylabs.util.ModelIndex;
import com.symmetrylabs.util.LinearModelIndex;
import com.symmetrylabs.util.OctreeModelIndex;

public class SLModel extends LXModel {

    public static final int NUM_POINT_BATCHES = 64;
    public static final int OCTREE_INDEX_MIN_POINTS = 1000;

    private ModelIndex modelIndex, modelIndexZFlattened;

    protected PointBatches pointBatches;

    public float[] pointsXYZ;
    public final String modelId;

    public SLModel() {
        super();
        modelId = null;
    }

    public SLModel(List<LXPoint> points) {
        this(null, points);
    }

    public SLModel(LXFixture fixture) {
        this(null, fixture);
    }

    public SLModel(LXFixture[] fixtures) {
        this(null, fixtures);
    }

    public SLModel(String modelId, List<LXPoint> points) {
        super(points);
        this.modelId = modelId;
        setupPointsArray();
    }

    public SLModel(String modelId, LXFixture fixture) {
        super(fixture);
        this.modelId = modelId;
        setupPointsArray();
    }

    public SLModel(String modelId, LXFixture[] fixtures) {
        super(fixtures);
        this.modelId = modelId;
        setupPointsArray();
    }

    private void setupPointsArray() {
        this.pointsXYZ = new float[this.points.length * 3];
        for (int i = 0; i < this.points.length; i++) {
            LXPoint point = this.points[i];
            this.pointsXYZ[3 * i] = point.x;
            this.pointsXYZ[3 * i + 1] = point.y;
            this.pointsXYZ[3 * i + 2] = point.z;
        }
    }

    protected ModelIndex createModelIndex(boolean flattenZ) {
        if (getPoints().size() < OCTREE_INDEX_MIN_POINTS)
            return new LinearModelIndex(this, flattenZ);

        return new OctreeModelIndex(this, flattenZ);
    }

    public ModelIndex getModelIndex() {
        return getModelIndex(false);
    }

    public ModelIndex getModelIndex(boolean flattenZ) {
        if (flattenZ && modelIndexZFlattened == null)
            return modelIndexZFlattened = createModelIndex(true);

        if (!flattenZ && modelIndex == null)
            return modelIndex = createModelIndex(false);

        return flattenZ ? modelIndexZFlattened : modelIndex;
    }

    public void forEachPoint(final BatchConsumer consumer) {
        if (pointBatches == null) {
            pointBatches = new PointBatches(getPoints(), NUM_POINT_BATCHES);
        }

        pointBatches.forEachPoint(consumer);
    }

    public /* abstract */ Iterator<? extends LXModel> getChildren() {
        return IteratorUtils.emptyIterator();
    }

    public static class PointBatches {
        private final List<LXPoint> points;
        private final int batchCount;
        private final List<PointBatch> pointBatches;

        public PointBatches(List<LXPoint> points, int batchCount) {
            this.points = points;
            this.batchCount = batchCount;
            this.pointBatches = new ArrayList<>(batchCount);

            int batchStride = (int)FastMath.ceil(points.size() / batchCount);
            for (int i = 0; i < batchCount; i++) {
                int start = i * batchStride;
                int end = FastMath.min(start + batchStride, points.size() - 1);
                pointBatches.add(new PointBatch(start, end));
            }
        }

        public void forEachPoint(final BatchConsumer consumer) {
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
