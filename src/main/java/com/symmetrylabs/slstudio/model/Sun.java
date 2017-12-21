package com.symmetrylabs.slstudio.model;

import com.symmetrylabs.slstudio.mappings.FultonStreetLayout;
import com.symmetrylabs.slstudio.util.NullOutputStream;
import heronarts.lx.model.LXAbstractFixture;
import heronarts.lx.model.LXModel;
import heronarts.lx.model.LXPoint;
import heronarts.lx.transform.LXTransform;
import org.jetbrains.annotations.NotNull;
import processing.core.PVector;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.symmetrylabs.slstudio.util.Utils.sketchFile;
import static processing.core.PApplet.println;
import static processing.core.PConstants.PI;


public class Sun extends StripsModel<Strip> {

    public enum Type {
        FULL, TWO_THIRDS, ONE_HALF, ONE_THIRD
    }

    public final String id;
    public final Type type;

    protected final List<Slice> slices = new ArrayList<>();
    protected final Map<String, Slice> sliceTable = new HashMap<>();

    private final List<Slice> slicesUnmodifiable = Collections.unmodifiableList(slices);

    public BoundingBox boundingBox;
    public final PVector center;
    public final float[] distances;

    protected Sun masterSun;
    protected int[] masterIndexes;

    public Sun(String id, Type type, float[] coordinates, float[] rotations,
            LXTransform transform, int[][] numPointsPerStrip) {

        super(new Fixture(id, type, coordinates, rotations, transform, numPointsPerStrip));

        Fixture fixture = (Fixture) this.fixtures.get(0);

        this.id = id;
        this.type = type;

        this.slices.addAll(fixture.slices);
        this.strips.addAll(fixture.strips);
        this.center = fixture.origin;

        for (Slice slice : slices) {
            sliceTable.put(slice.id, slice);
        }

        computeBoundingBox();
        distances = computeDistances();
        applyStripRotations();
    }

    public List<Slice> getSlices() {
        return slicesUnmodifiable;
    }

    public Sun getMasterSun() {
        return masterSun;
    }

    public int[] getMasterIndexes() {
        return masterIndexes;
    }

    private void applyStripRotations() {
        // Apply the rotations to the strips
        for (int i = 0, stripsSize = strips.size(); i < stripsSize; i++) {
            final CurvedStrip strip = (CurvedStrip) strips.get(i);

            strip.updateOffset(FultonStreetLayout.rotationForStrip(this, i));
        }
    }

    void computeMasterIndexes(Sun masterSun) {
        this.masterSun = masterSun;
        masterIndexes = new int[points.length];
        new ComputeMasterIndexThread().start();
    }

    class ComputeMasterIndexThread extends Thread {
        private final String pointsHash;
        ComputeMasterIndexThread() {
            this.pointsHash = computePointsHash();
        }

        private String computePointsHash() {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-1");
                ObjectOutputStream stream = new ObjectOutputStream(new DigestOutputStream(new NullOutputStream(), md));
                for (final LXPoint point : masterSun.points) {
                    stream.writeFloat(point.x);
                    stream.writeFloat(point.y);
                    stream.writeFloat(point.z);
                }
                for (final LXPoint point : points) {
                    stream.writeFloat(point.x);
                    stream.writeFloat(point.y);
                    stream.writeFloat(point.z);
                }
                return Base64.getEncoder().encodeToString(md.digest());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public void run() {
            try {
                loadFromCache();
                println("Loaded master points from cache for " + id);
            } catch (Exception e) {
                doCompute();

                try {
                    saveToCache();
                    println("Saved master points from cache for " + id);
                } catch (Exception x) {
                    println("Failed to save master points from cache for " + id);
                    x.printStackTrace();
                }
            }
        }

        private void saveToCache() throws IOException {
            cacheFile().getParentFile().mkdirs();
            ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(cacheFile()));
            output.writeObject(pointsHash);

            for (int i = 0; i < points.length; i++) {
                output.writeInt(masterIndexes[i]);
            }

            output.close();
        }

        private void loadFromCache() throws IOException, ClassNotFoundException {
            ObjectInputStream input = new ObjectInputStream(new FileInputStream(cacheFile()));
            if (! input.readObject().equals(pointsHash)) {
                throw new IOException("Hash values don't match");
            }

            for (int i = 0; i < points.length; i++) {
                masterIndexes[i] = input.readInt();
            }

            input.close();
        }

        @NotNull
        private File cacheFile() {
            return sketchFile("cache/master-indexes/" + id);
        }

        private void doCompute() {
            float cx = center.x;
            float cy = center.y;
            float cz = center.z;
            float mcx = masterSun.center.x;
            float mcy = masterSun.center.y;
            float mcz = masterSun.center.z;

            for (int i = 0; i < points.length; i++) {
                float minSqDist = 1e18f;
                masterIndexes[i] = 0;
                float px = (points[i].x - cx);
                float py = (points[i].y - cy);
                float pz = (points[i].z - cz);
                float xLow = px - 12;
                float xHigh = px + 12;
                float yLow = py - 12;
                float yHigh = py + 12;
                for (int j = 0; j < masterSun.points.length; j++) {
                    LXPoint masterPoint = masterSun.points[j];
                    float mx = masterPoint.x - mcx;
                    float my = masterPoint.y - mcy;
                    if (mx > xLow && mx < xHigh && my > yLow && my < yHigh) {
                        float dx = px - mx;
                        float dy = py - (masterPoint.y - mcy);
                        float dz = pz - (masterPoint.z - mcz);
                        float sqDist = dx * dx + dy * dy + dz * dz;
                        if (sqDist < minSqDist) {
                            minSqDist = sqDist;
                            masterIndexes[i] = masterPoint.index;
                        }
                    }
                }
            }
            println("computed master indexes for " + id);
        }
    }

    public void copyFromMasterSun(int[] colors) {
        if (masterIndexes != null) {
            for (int i = 0; i < points.length; i++) {
                colors[points[i].index] = colors[masterIndexes[i]];
            }
        }
    }

    void computeBoundingBox() {
        // So, this used to be done using Float.MIN_VALUE and Float.MAX_VALUE and using simple </> checks, rather than null.
        // In some cases (suns 3, 5 and 8), the xMax value remained at Float.MIN_VALUE, which makes no sense at all, but
        // I didn't have time (or a debugger >_<) to figure it out. So, I replaced the logic with null-based logic. - Yona
        Float xMin = null;
        Float xMax = null;

        Float yMin = null;
        Float yMax = null;

        Float zMin = null;
        Float zMax = null;

        LXPoint xMinPt = null;
        LXPoint xMaxPt = null;
        LXPoint yMinPt = null;
        LXPoint yMaxPt = null;
        LXPoint zMinPt = null;
        LXPoint zMaxPt = null;

        for (LXPoint p : points) {
            if (xMin == null || p.x < xMin) {
                xMin = p.x;
                xMinPt = p;
            }
            if (xMax == null || p.x > xMax) {
                xMax = p.x;
                xMaxPt = p;
            }

            if (yMin == null || p.y < yMin) {
                yMin = p.y;
                yMinPt = p;
            }
            if (yMax == null || p.y > yMax) {
                yMax = p.y;
                yMaxPt = p;
            }

            if (zMin == null || p.z < zMin) {
                zMin = p.z;
                zMinPt = p;
            }
            if (zMax == null || p.z > zMax) {
                zMax = p.z;
                zMaxPt = p;
            }
        }


        boundingBox = new BoundingBox(xMin, yMin, zMin, xMax - xMin, yMax - yMin, zMax - zMin);

//    if (xMinPt == null) println(id + "-xMin: NULL!!"); else println(id + "-xMin: (" + xMinPt.x + ", " + xMinPt.y + ", " + xMinPt.z + ")");
//    if (xMaxPt == null) println(id + "-xMax: NULL!!"); else println(id + "-xMax: (" + xMaxPt.x + ", " + xMaxPt.y + ", " + xMaxPt.z + ")");
//    if (yMinPt == null) println(id + "-yMin: NULL!!"); else println(id + "-yMin: (" + yMinPt.x + ", " + yMinPt.y + ", " + yMinPt.z + ")");
//    if (yMaxPt == null) println(id + "-yMax: NULL!!"); else println(id + "-yMax: (" + yMaxPt.x + ", " + yMaxPt.y + ", " + yMaxPt.z + ")");
//    if (zMinPt == null) println(id + "-zMin: NULL!!"); else println(id + "-zMin: (" + zMinPt.x + ", " + zMinPt.y + ", " + zMinPt.z + ")");
//    if (zMaxPt == null) println(id + "-zMax: NULL!!"); else println(id + "-zMax: (" + zMaxPt.x + ", " + zMaxPt.y + ", " + zMaxPt.z + ")");
    }

    float[] computeDistances() {
        float[] distances = new float[points.length];
        for (int i = 0; i < points.length; i++) {
            LXPoint p = points[i];
            distances[i] = PVector.sub(center, new PVector(p.x, p.y, p.z)).mag();
        }
        return distances;
    }

    public Slice getSliceById(String id) {
        Slice slice = sliceTable.get(id);
        if (slice == null) throw new IllegalArgumentException("Invalid slice id:" + id);
        return slice;
    }

    private static class Fixture extends LXAbstractFixture {

        private final List<Slice> slices = new ArrayList<Slice>();
        private final List<Strip> strips = new ArrayList<Strip>();
        public final PVector origin;

        private Fixture(
            String id,
            Sun.Type type,
            float[] coordinates,
            float[] rotations,
            LXTransform transform,
            int[][] numPointsPerStrip
        ) {
            transform.push();

            origin = new PVector(coordinates[0], coordinates[1], coordinates[2]);
            if (type == Sun.Type.FULL) {
                origin.y += Slice.RADIUS + 18;
            }
            if (type == Sun.Type.TWO_THIRDS) {
                origin.y += 22 * Slice.STRIP_SPACING;
            }
            if (type == Sun.Type.ONE_THIRD) {
                origin.y -= 22 * Slice.STRIP_SPACING;
            }

            transform.push();

            transform.translate(origin.x, origin.y, origin.z);
            transform.rotateX(rotations[0] * PI / 180);
            transform.rotateY(rotations[1] * PI / 180);
            transform.rotateZ(rotations[2] * PI / 180);

            // create slices...
            if (type != Sun.Type.ONE_THIRD) {
                slices.add(new Slice(
                    id + "_top_front",
                    Slice.Type.FULL,
                    new float[]{-Slice.RADIUS, Slice.RADIUS, 0},
                    new float[]{0, 0, 0},
                    transform,
                    numPointsPerStrip[0]
                ));
                slices.add(new Slice(
                    id + "_top_back",
                    Slice.Type.FULL,
                    new float[]{Slice.RADIUS, Slice.RADIUS, 0},
                    new float[]{0, 180, 0},
                    transform,
                    numPointsPerStrip[1]
                ));
            }

            switch (type) {
                case FULL:
                    slices.add(new Slice(
                        id + "_bottom_front",
                        Slice.Type.FULL,
                        new float[]{Slice.RADIUS, -Slice.DIAMETER * 0.5f, 0},
                        new float[]{0, 0, 180},
                        transform,
                        numPointsPerStrip[2]
                    ));
                    slices.add(new Slice(
                        id + "_bottom_back",
                        Slice.Type.FULL,
                        new float[]{-Slice.RADIUS, -Slice.DIAMETER * 0.5f, 0},
                        new float[]{0, 180, 180},
                        transform,
                        numPointsPerStrip[3]
                    ));
                    break;

                case TWO_THIRDS:
                    slices.add(new Slice(
                        id + "_bottom_front",
                        Slice.Type.BOTTOM_ONE_THIRD,
                        new float[]{Slice.RADIUS, -Slice.RADIUS + 1.5f, 0},
                        new float[]{0, 0, 180},
                        transform,
                        numPointsPerStrip[2]
                    ));
                    slices.add(new Slice(
                         id + "_bottom_back",
                        Slice.Type.BOTTOM_ONE_THIRD,
                        new float[]{-Slice.RADIUS, -Slice.RADIUS + 1.5f, 0},
                        new float[]{0, 180, 180},
                        transform,
                        numPointsPerStrip[3]
                    ));

                case ONE_HALF:
                    // already done
                    break;

                case ONE_THIRD:
                    slices.add(new Slice(
                        id + "_top_front",
                        Slice.Type.TWO_THIRDS,
                        new float[]{-Slice.RADIUS, Slice.RADIUS, 0},
                        new float[]{0, 0, 0},
                        transform,
                        numPointsPerStrip[0]
                    ));
                    slices.add(new Slice(
                        id + "_top_back",
                        Slice.Type.TWO_THIRDS,
                        new float[]{Slice.RADIUS, Slice.RADIUS, 0},
                        new float[]{0, 180, 0},
                        transform,
                        numPointsPerStrip[1]
                    ));
                    break;
            }

            // add pointers to strips
            for (Slice slice : slices) {
                for (Strip strip : slice.strips) {
                    strips.add(strip);
                    for (LXPoint point : strip.points) {
                        points.add(point);
                        // estimate normals
                        PVector pos = new PVector(point.x, point.y, point.z);
                        PVector normal = PVector.sub(pos, origin);
                        normal.normalize();
                        normal.z += (normal.z > origin.z) ? 0.3 : -0.3;
                        ((LXPointNormal) point).setNormal(normal);
                    }
                }
            }

            transform.pop();
            transform.pop();
        }
    }
}
