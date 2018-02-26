package com.symmetrylabs.slstudio.model;

import java.util.List;
import java.util.ArrayList;

import org.apache.commons.math3.util.FastMath;

import heronarts.lx.model.LXFixture;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;

import com.symmetrylabs.slstudio.util.ModelIndex;
import com.symmetrylabs.slstudio.util.LinearModelIndex;
import com.symmetrylabs.slstudio.util.OctreeModelIndex;

public class SLModel extends LXModel {

    public static final int NUM_POINT_BATCHES = 64;
    public static final int OCTREE_INDEX_MIN_POINTS = 1000;

    private ModelIndex modelIndex, modelIndexZFlattened;

    protected PointBatches pointBatches;

    public SLModel() {
    }

    public SLModel(List<LXPoint> points) {
        super(points);
    }

    public SLModel(LXFixture fixture) {
        super(fixture);
    }

    public SLModel(LXFixture[] fixtures) {
        super(fixtures);
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
