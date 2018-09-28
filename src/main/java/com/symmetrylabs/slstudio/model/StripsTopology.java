package com.symmetrylabs.slstudio.model;

import com.google.common.base.Preconditions;
import com.symmetrylabs.util.FixedWidthOctree;
import heronarts.lx.transform.LXVector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Topology structure for representing axis-aligned grids of equal-length strips
 * <p>
 * StripsTopology is capable of representing the topology of any structure that is
 * comprised entirely of approximately-equal-length strips where all strips are aligned
 * to a cartesian axis.
 * <p>
 * Topology is represented as a graph of "bundles", where each bundle can have up to
 * four strips in it. A bundle is a set of parallel strips that are close to one another.
 * In a dense grid made out of cubes, most bundles will have four strips in them.
 * Here's a drawing; imagine that each square is a cube in plan view, and all four
 * cubes are resting on the ground and are a couple inches from one another.
 * Each junction in this diagram labelled with a letter would be a bundle; A would have
 * one strip, B would have two and E would have 4.
 * <p><pre>
 *                          A           B           C
 *                            +-------+   +-------+
 *                            |       |   |       |
 *                            |       |   |       |
 *                            |       |   |       |
 *                            +-------+   +-------+
 *                          D           E           F
 *                            +-------+   +-------+
 *                            |       |   |       |
 *                            |       |   |       |
 *                            |       |   |       |
 *                            +-------+   +-------+
 *                          G           H           I
 * </pre><p>
 * Each bundle has pointers to the junction at each end of it. Ends are identified
 * by which end points towards the positive end of the axis it's aligned with; an
 * X-aligned bundle has a positive end towards X+ and a negative end towards X-. Since
 * you can't use + and - in field names, these are called P and N for positive and
 * negative.
 * <p>
 * Each junction has pointers to up to 6 attached bundles, one in every cardinal
 * direction. These follow the same naming convention: nx is negative-X, pz is
 * positive-Z, etc.
 */
public class StripsTopology {
    private static final int NO_STRIP = Integer.MAX_VALUE;

    public enum Dir {
        X, Y, Z;

        /**
         * Returns the first orthogonal direction to this direction
         */
        public Dir ortho1() {
            switch (this) {
                case X:
                    return Y;
                case Y:
                case Z:
                    return X;
            }
            return null;
        }

        /**
         * Returns the second orthogonal direction to this direction
         */
        public Dir ortho2() {
            switch (this) {
                case X:
                case Y:
                    return Z;
                case Z:
                    return Y;
            }
            return null;
        }

    }

    public enum Sign {
        POS, NEG;

        public Sign other() {
            if (this == POS) {
                return NEG;
            }
            if (this == NEG) {
                return POS;
            }
            return null;
        }
    }

    public class BundleEndpoints {
        public LXVector negative;
        public LXVector positive;

        public LXVector get(Sign s) {
            if (s == Sign.NEG) {
                return negative;
            }
            if (s == Sign.POS) {
                return positive;
            }
            return null;
        }
    }

    /**
     * The meeting point of up to 6 bundles.
     */
    public static class Junction {
        /**
         * The bundle in the positive X direction
         */
        private StripsTopology.Bundle px;
        /**
         * The bundle in the negative X direction
         */
        private StripsTopology.Bundle nx;
        /**
         * The bundle in the positive Y direction
         */
        private StripsTopology.Bundle py;
        /**
         * The bundle in the negative Y direction
         */
        private StripsTopology.Bundle ny;
        /**
         * The bundle in the positive Z direction
         */
        private StripsTopology.Bundle pz;
        /**
         * The bundle in the negative X direction
         */
        private StripsTopology.Bundle nz;
        /**
         * The approximate location of this junction.
         * This is calculated as the midpoint of the bundles attached to the junction.
         */
        public LXVector loc;

        /** The number of bundles attached to this junction */
        public int degree() {
            int degree = 0;
            if (px != null) degree++;
            if (nx != null) degree++;
            if (py != null) degree++;
            if (ny != null) degree++;
            if (pz != null) degree++;
            if (nz != null) degree++;
            return degree;
        }

        public Bundle get(Dir e, Sign s) {
            switch (e) {
                case X:
                    return s == Sign.POS ? px : nx;
                case Y:
                    return s == Sign.POS ? py : ny;
                case Z:
                    return s == Sign.POS ? pz : nz;
            }
            return null;
        }

        public List<Bundle> getBundles() {
            List<Bundle> bundles = new ArrayList<>();
            if (px != null) bundles.add(px);
            if (nx != null) bundles.add(nx);
            if (py != null) bundles.add(py);
            if (ny != null) bundles.add(ny);
            if (pz != null) bundles.add(pz);
            if (nz != null) bundles.add(nz);
            return bundles;
        }

        private void updateLocationEstimate() {
            float xsum = 0;
            float ysum = 0;
            float zsum = 0;
            int xcount = 0;
            int ycount = 0;
            int zcount = 0;

            if (px != null) {
                ysum += px.planarLocation().a;
                zsum += px.planarLocation().b;
                ycount++;
                zcount++;
            }
            if (nx != null) {
                ysum += nx.planarLocation().a;
                zsum += nx.planarLocation().b;
                ycount++;
                zcount++;
            }
            if (py != null) {
                xsum += py.planarLocation().a;
                zsum += py.planarLocation().b;
                xcount++;
                zcount++;
            }
            if (ny != null) {
                xsum += ny.planarLocation().a;
                zsum += ny.planarLocation().b;
                xcount++;
                zcount++;
            }
            if (pz != null) {
                xsum += pz.planarLocation().a;
                ysum += pz.planarLocation().b;
                xcount++;
                ycount++;
            }
            if (nz != null) {
                xsum += nz.planarLocation().a;
                ysum += nz.planarLocation().b;
                xcount++;
                ycount++;
            }

            if (xcount == 0) {
                if (px != null) {
                    xsum += px.endpoints().negative.x;
                    xcount++;
                }
                if (nx != null) {
                    xsum += nx.endpoints().positive.x;
                    xcount++;
                }
            }
            if (ycount == 0) {
                if (py != null) {
                    ysum += py.endpoints().negative.y;
                    ycount++;
                }
                if (ny != null) {
                    ysum += ny.endpoints().positive.y;
                    ycount++;
                }
            }
            if (zcount == 0) {
                if (pz != null) {
                    zsum += pz.endpoints().negative.z;
                    zcount++;
                }
                if (nz != null) {
                    zsum += nz.endpoints().positive.z;
                    zcount++;
                }
            }

            if (xcount != 0 && ycount != 0 && zcount != 0) {
                loc = new LXVector(xsum / xcount, ysum / ycount, zsum / zcount);
            } else {
                loc = new LXVector(0, 0, 0);
            }
        }
    }

    /**
     * A set of parallel, adjacent strips
     */
    public class Bundle {
        /* Note that this is just a hit for initialization; the strips array
         * is resized down once the bundle is finished being built. */
        private static final int MAX_STRIPS = 4;

        /**
         * A unique ID representing this bundle
         */
        public final int index;

        /**
         * The direction this bundle points in
         */
        public Dir dir;

        /**
         * The model indexes of the strips in this bundle.
         * These index into model.strips and can be used with model.getStripByIndex()
         */
        public int[] strips;

        /**
         * The sign of each strip in the bundle. A positive sign means the strip
         * points in the positive direction of the axis of the bundle.
         */
        public Sign[] stripSign;

        /**
         * The junction at the positive end of this bundle
         */
        private Junction p = null;
        /**
         * The junction at the negative end of this bundle
         */
        private Junction n = null;

        /* Set to true once we're finished adding strips to the bundle; the cached
         * values below can only be used once this is set to true. */
        private boolean finished = false;

        /* Caches for expensive-to-compute properties of the bundle, only populated
         * after the bundle is finished. */
        private BundleEndpoints endpoints = null;
        private float minProjection = Float.MAX_VALUE;
        private float maxProjection = Float.MIN_VALUE;
        private float projection = Float.MIN_VALUE;

        private Bundle(int index) {
            strips = new int[MAX_STRIPS];
            stripSign = new Sign[MAX_STRIPS];
            Arrays.fill(strips, NO_STRIP);
            Arrays.fill(stripSign, null);
            this.index = index;
        }

        public Junction get(Sign s) {
            return s == Sign.POS ? p : s == Sign.NEG ? n : null;
        }

        public Junction getOpposite(Junction j) {
            return j == p ? n : p;
        }

        private void addStrip(int strip) {
            Preconditions.checkState(!finished, "cannot add to finished bundle");
            Sign sign = null;
            Strip s = model.getStripByIndex(strip);
            if (s.size >= 2) {
                switch (dir) {
                case X:
                    sign = s.points[0].x < s.points[1].x ? Sign.POS : Sign.NEG;
                    break;
                case Y:
                    sign = s.points[0].y < s.points[1].y ? Sign.POS : Sign.NEG;
                    break;
                case Z:
                    sign = s.points[0].z < s.points[1].z ? Sign.POS : Sign.NEG;
                    break;
                }
            }
            for (int i = 0; i < strips.length; i++) {
                if (strips[i] == NO_STRIP) {
                    strips[i] = strip;
                    stripSign[i] = sign;
                    return;
                }
            }
            throw new IllegalStateException(String.format(
                "tried to add more than %d strips to a Bundle", strips.length));
        }

        private void finishedAddingStrips() {
            if (finished) {
                return;
            }

            int count = 0;
            for (int i = 0; i < strips.length; i++) {
                if (strips[i] != NO_STRIP) {
                    count++;
                }
            }
            Integer[] newStrips = new Integer[count];
            int j = 0;
            for (int strip : strips) {
                if (strip != NO_STRIP) {
                    newStrips[j] = strip;
                    j++;
                }
            }
            /* Sort the included strips so that two bundles that are tip-to-tip
             * adjacent will have element 0 line up with element 0, element 1
             * with element 1, etc. */
            Arrays.sort(newStrips, (a, b) -> {
                Strip sa = model.getStripByIndex(a);
                Strip sb = model.getStripByIndex(b);
                if (Math.abs(sa.cx - sb.cx) > 1e-2) {
                    return Float.compare(sa.cx, sb.cx);
                }
                if (Math.abs(sa.cy - sb.cy) > 1e-2) {
                    return Float.compare(sa.cy, sb.cy);
                }
                if (Math.abs(sa.cz - sb.cz) > 1e-2) {
                    return Float.compare(sa.cz, sb.cz);
                }
                return 0;
            });

            Sign[] newSigns = new Sign[count];
            for (int i = 0; i < count; i++) {
                for (int old = 0; old < MAX_STRIPS; old++) {
                    if (newStrips[i] == strips[old]) {
                        newSigns[i] = stripSign[old];
                        break;
                    }
                }
            }
            stripSign = newSigns;

            strips = new int[count];
            for (int i = 0; i < count; i++) {
                strips[i] = newStrips[i];
            }
            endpoints = endpoints();
            minProjection = minProjection();
            maxProjection = maxProjection();
            projection = projection();
        }

        public BundleEndpoints endpoints() {
            if (finished) {
                return endpoints;
            }

            float min = minProjection();
            float max = maxProjection();
            PlanarLocation sort = planarLocation();

            float xs, xe;
            float ys, ye;
            float zs, ze;

            BundleEndpoints e = new BundleEndpoints();
            e.negative = new LXVector(0, 0, 0);
            e.positive = new LXVector(0, 0, 0);

            switch (dir) {
                case X:
                    e.negative.x = min;
                    e.positive.x = max;
                    e.negative.y = sort.a;
                    e.positive.y = sort.a;
                    e.negative.z = sort.b;
                    e.positive.z = sort.b;
                    break;
                case Y:
                    e.negative.x = sort.a;
                    e.positive.x = sort.a;
                    e.negative.y = min;
                    e.positive.y = max;
                    e.negative.z = sort.b;
                    e.positive.z = sort.b;
                    break;
                case Z:
                    e.negative.x = sort.a;
                    e.positive.x = sort.a;
                    e.negative.y = sort.b;
                    e.positive.y = sort.b;
                    e.negative.z = min;
                    e.positive.z = max;
                    break;
                default:
                    throw new IllegalArgumentException(
                        "cannot find endpoints of non-aligned bundle");
            }
            return e;
        }

        public PlanarLocation planarLocation() {
            PlanarLocation pl[] = new PlanarLocation[MAX_STRIPS];
            for (int i = 0; i < strips.length; i++) {
                if (strips[i] == NO_STRIP) {
                    continue;
                }
                Strip s = model.getStripByIndex(strips[i]);
                switch (dir) {
                    case X:
                        pl[i] = new PlanarLocation(dir, s.cy, s.cz);
                        break;
                    case Y:
                        pl[i] = new PlanarLocation(dir, s.cx, s.cz);
                        break;
                    case Z:
                        pl[i] = new PlanarLocation(dir, s.cx, s.cy);
                        break;
                }
            }
            return PlanarLocation.combine(pl);
        }

        /**
         * An edge's "projection" is the projection of it's centroid onto the axis it's aligned with
         */
        public float projection() {
            if (finished) {
                return projection;
            }
            float proj = 0;
            int count = 0;
            for (int strip : strips) {
                if (strip == NO_STRIP) {
                    continue;
                }
                Strip s = model.getStripByIndex(strip);
                switch (dir) {
                    case X:
                        proj += s.cx;
                        break;
                    case Y:
                        proj += s.cy;
                        break;
                    case Z:
                        proj += s.cz;
                        break;
                }
                count++;
            }
            return proj / count;
        }

        /**
         * Min-projection is the minimum value of the projection of this edge onto its axis
         */
        public float minProjection() {
            if (finished) {
                return minProjection;
            }
            float min = Float.MAX_VALUE;
            for (int strip : strips) {
                if (strip == NO_STRIP) {
                    continue;
                }
                Strip s = model.getStripByIndex(strip);
                switch (dir) {
                    case X:
                        min = Float.min(min, s.xMin);
                        break;
                    case Y:
                        min = Float.min(min, s.yMin);
                        break;
                    case Z:
                        min = Float.min(min, s.zMin);
                        break;
                }
            }
            return min;
        }

        /**
         * Max-projection is the maximum value of the projection of this edge onto its axis
         */
        public float maxProjection() {
            if (finished) {
                return maxProjection;
            }
            float max = -Float.MAX_VALUE;
            for (int strip : strips) {
                if (strip == NO_STRIP) {
                    continue;
                }
                Strip s = model.getStripByIndex(strip);
                switch (dir) {
                    case X:
                        max = Float.max(max, s.xMax);
                        break;
                    case Y:
                        max = Float.max(max, s.yMax);
                        break;
                    case Z:
                        max = Float.max(max, s.zMax);
                        break;
                }
            }
            return max;
        }

        /**
         * Returns the number of points in each strip in each bundle.
         *
         * If the bundles in the strips have different numbers of points in each,
         * throws an IllegalStateException. */
        public int getStripPointCount() {
            int count = -1;
            for (int stripIndex : strips) {
                if (stripIndex != NO_STRIP) {
                    int c = model.getStripByIndex(stripIndex).size;
                    if (count != -1 && c != count) {
                        throw new IllegalStateException("strips in bundle had different point counts");
                    } else {
                        count = c;
                    }
                }
            }
            return count;
        }
    }

    /**
     * Represents the location of a bundle within the plane perpendicular to the bundle.
     * This is used to bucket strips into bundles.
     */
    public static class PlanarLocation {
        public Dir dir;
        /**
         * The first coordinate in the plane. For the X plane, this is the Y coordinate. For Y and Z, this is the X coordinate.
         */
        public float a;
        /**
         * The second coordinate in the plane. For the X and Y plane, this is the Z coordinate. For Z, this is the Y coordinate.
         */
        public float b;

        PlanarLocation(Dir dir, float a, float b) {
            this.dir = dir;
            this.a = a;
            this.b = b;
        }

        boolean equivalent(PlanarLocation pl, float tolerance) {
            return dir == pl.dir && Math.abs(a - pl.a) < tolerance && Math.abs(b - pl.b) < tolerance;
        }

        static PlanarLocation combine(PlanarLocation buckets[]) {
            PlanarLocation res = null;
            int count = 0;
            for (PlanarLocation bucket : buckets) {
                if (bucket == null) {
                    continue;
                }
                if (res == null) {
                    res = new PlanarLocation(bucket.dir, bucket.a, bucket.b);
                } else {
                    res.a += bucket.a;
                    res.b += bucket.b;
                }
                count++;
            }
            if (res == null) {
                return null;
            }
            res.a /= count;
            res.b /= count;
            return res;
        }
    }

    private final StripsModel model;
    public final List<Bundle> bundles;
    public final List<Junction> junctions;

    StripsTopology(StripsModel model, float orderTolerance, float bucketTolerance, float endpointTolerance) {
        this.model = model;
        int N = model.getStrips().size();
        bundles = new ArrayList<>(N);
        junctions = new ArrayList<>();

        /* Create TopoEdges for each bundle */
        for (int i = 0; i < N; i++) {
            Strip s = model.getStripByIndex(i);

            Bundle e = new Bundle(bundles.size());
            if (s.xRange < 1e-3 && s.yRange < 1e-3) {
                e.dir = Dir.Z;
            } else if (s.xRange < 1e-3 && s.zRange < 1e-3) {
                e.dir = Dir.Y;
            } else if (s.yRange < 1e-3 && s.zRange < 1e-3) {
                e.dir = Dir.X;
            } else {
                e.dir = null;
            }
            e.addStrip(i);

            boolean matchesExisting = false;
            PlanarLocation pl = e.planarLocation();
            float proj = e.projection();
            for (Bundle testEdge : bundles) {
                float testProj = testEdge.projection();
                float projDist = Math.abs(proj - testProj);
                if (pl.equivalent(testEdge.planarLocation(), bucketTolerance) && projDist < orderTolerance) {
                    testEdge.addStrip(i);
                    matchesExisting = true;
                    break;
                }
            }
            if (!matchesExisting) {
                bundles.add(e);
            }
        }
        for (Bundle e : bundles) {
            e.finishedAddingStrips();
        }

        /* Once we have all the bundles, create the junctions at their endpoints. */
        int junctionCountGuess = bundles.size() / 6;
        FixedWidthOctree<Junction> junctionTree =
            new FixedWidthOctree<>(
                model.cx, model.cy, model.cz, 2 * model.rMax,
                /* use the log-base-8 of the number of junctions we're guessing we'll have as the
                 * depth; for perfectly-distributed endpoints this would put four junctions
                 * in each partition. */
                (int) Math.ceil(Math.log(junctionCountGuess) / Math.log(8) / 4));

        /* This makes the junctions and populates connectivity all at once. */
        for (Bundle b : bundles) {
            BundleEndpoints be = b.endpoints();

            List<Junction> js = junctionTree.withinDistance(
                be.positive.x, be.positive.y, be.positive.z, endpointTolerance);
            if (b.index == 91) {
                System.out.println(String.format("91P has %d neighbors", js.size()));
                for (Junction j : js) {
                    System.out.println(String.format("neighbor has px=%s", j.get(Dir.X, Sign.POS)));
                }
            }

            if (!js.isEmpty()) {
                b.p = js.get(0);
                float dist = b.endpoints.positive.dist(js.get(0).loc);
                for (int i = 1; i < js.size(); i++) {
                    float d = b.endpoints.positive.dist(js.get(i).loc);
                    if (d < dist) {
                        b.p = js.get(i);
                        dist = d;
                    }
                }
            } else {
                Junction j = new Junction();
                j.loc = be.positive;
                b.p = j;
                junctionTree.insert(j.loc.x, j.loc.y, j.loc.z, j);
                junctions.add(j);
            }
            switch (b.dir) {
                case X:
                    if (b.p.nx != null) {
                        throw new IllegalStateException("found overlapping bundles");
                    }
                    b.p.nx = b;
                    break;
                case Y:
                    if (b.p.ny != null) {
                        throw new IllegalStateException("found overlapping bundles");
                    }
                    b.p.ny = b;
                    break;
                case Z:
                    if (b.p.nz != null) {
                        throw new IllegalStateException("found overlapping bundles");
                    }
                    b.p.nz = b;
                    break;
            }
            b.p.updateLocationEstimate();

            js = junctionTree.withinDistance(
                be.negative.x, be.negative.y, be.negative.z, endpointTolerance);
            if (!js.isEmpty()) {
                b.n = js.get(0);
                float dist = b.endpoints.negative.dist(js.get(0).loc);
                for (int i = 1; i < js.size(); i++) {
                    float d = b.endpoints.negative.dist(js.get(i).loc);
                    if (d < dist) {
                        b.n = js.get(i);
                        dist = d;
                    }
                }
            } else {
                Junction j = new Junction();
                j.loc = be.negative;
                b.n = j;
                junctionTree.insert(j.loc.x, j.loc.y, j.loc.z, j);
                junctions.add(j);
            }
            switch (b.dir) {
                case X:
                    if (b.n.px != null) {
                        throw new IllegalStateException("found overlapping bundles");
                    }
                    b.n.px = b;
                    break;
                case Y:
                    if (b.n.py != null) {
                        throw new IllegalStateException("found overlapping bundles");
                    }
                    b.n.py = b;
                    break;
                case Z:
                    if (b.n.pz != null) {
                        throw new IllegalStateException("found overlapping bundles");
                    }
                    b.n.pz = b;
                    break;
            }
            b.n.updateLocationEstimate();
        }

        /* Find good locations for each of the junctions now that we have
         * the final set of bundles for each of them. */
        for (Junction j : junctions) {
            j.updateLocationEstimate();
        }
    }
}
