package com.symmetrylabs.slstudio.model;

import heronarts.lx.model.LXAbstractFixture;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static processing.core.PApplet.*;

/**
 * Top-level model of the entire sculpture. This contains a list of every cube on the sculpture, which forms a hierarchy
 * of faces, strips and points.
 */

public class SLModel extends LXModel {
    public final List<LXModel> objModels;

    // Suns
    public final List<Sun> suns;
    public final Map<String, Sun> sunTable;
    public final Sun masterSun;

    // Slices
    public final List<Slice> slices;
    private final Map<String, Slice> sliceTable;

    // Strips
    public final List<Strip> strips;

    public final int NUM_POINT_BATCHES = 16;

    // Array of points stored as contiguous floats for performance
    private final float[] pointsArray;
    private final List<PointBatch> pointBatches = new ArrayList<PointBatch>(NUM_POINT_BATCHES);

    public SLModel(List<Sun> suns) {
        super(new Fixture(suns));
        Fixture fixture = (Fixture) this.fixtures.get(0);

        // Suns
        List<Sun> sunList = new ArrayList<Sun>();
        Map<String, Sun> _sunTable = new HashMap<String, Sun>();

        // Slices
        List<Slice> sliceList = new ArrayList<Slice>();
        Map<String, Slice> _sliceTable = new HashMap<String, Slice>();

        // Strips
        List<Strip> stripList = new ArrayList<Strip>();

        for (Sun sun : suns) {
            sunList.add(sun);
            _sunTable.put(sun.id, sun);

            for (Slice slice : sun.slices) {
                sliceList.add(slice);
                _sliceTable.put(slice.id, slice);

                for (Strip strip : slice.strips) {
                    stripList.add(strip);
                }
            }
        }

        masterSun = _sunTable.get("sun9"); // a full sun
        for (Sun sun : suns) {
            if (sun != masterSun) {
                sun.computeMasterIndexes(masterSun);
            }
        }

        this.objModels = new ArrayList<LXModel>();

        // Suns
        this.suns = Collections.unmodifiableList(sunList);
        this.sunTable = Collections.unmodifiableMap(_sunTable);

        // Slices
        this.slices = Collections.unmodifiableList(sliceList);
        this.sliceTable = Collections.unmodifiableMap(_sliceTable);

        // Strips
        this.strips = Collections.unmodifiableList(stripList);

        this.pointsArray = new float[this.points.length * 3];
        for (int i = 0; i < this.points.length; i++) {
            LXPoint point = this.points[i];
            this.pointsArray[3 * i] = point.x;
            this.pointsArray[3 * i + 1] = point.y;
            this.pointsArray[3 * i + 2] = point.z;
        }

        int batchStride = ceil(this.points.length / NUM_POINT_BATCHES);
        for (int i = 0; i < NUM_POINT_BATCHES; i++) {
            int start = i * batchStride;
            int end = min(start + batchStride, this.points.length - 1);
            this.pointBatches.add(new PointBatch(start, end));
        }
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
        return this.sunTable.get(id);
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
        this.pointBatches.parallelStream().forEach(new Consumer<PointBatch>() {
            public void accept(PointBatch batch) {
                consumer.accept(batch.start, batch.end);
            }
        });
    }

    private class PointBatch {
        int start;
        int end;

        PointBatch(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }
}
