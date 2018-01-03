package com.symmetrylabs.slstudio.model;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.Collections;

import heronarts.lx.model.LXAbstractFixture;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;

/**
 * Top-level model of the entire sculpture. This contains a list of every cube on the sculpture, which forms a hierarchy
 * of faces, strips and points.
 */
public class SunsModel extends StripsModel<CurvedStrip> implements SectionalModel<Sun> {

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
            System.out.println("Missing slice id: " + id);
            System.out.print("Valid ids: ");
            for (String key : sliceTable.keySet()) {
                System.out.print(key + ", ");
            }
            System.out.println();
            throw new IllegalArgumentException("Invalid slice id:" + id);
        }
        return slice;
    }
}
