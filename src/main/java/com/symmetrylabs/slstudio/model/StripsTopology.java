package com.symmetrylabs.slstudio.model;

import com.google.common.base.Preconditions;
import com.symmetrylabs.util.FixedWidthOctree;
import heronarts.lx.model.LXPoint;
import heronarts.lx.transform.LXVector;

import java.util.*;

/** Topology structure for representing axis-aligned grids of equal-length strips
 *
 * StripsTopology is capable of representing the topology of any structure that is
 * comprised entirely of approximately-equal-length strips where all strips are aligned
 * to a cartesian axis.
 *
 * Topology is represented as a graph of "bundles", where each bundle can have up to
 * four strips in it. A bundle is a set of parallel strips that are close to one another.
 * In a dense grid made out of cubes, most bundles will have four strips in them.
 * Here's a drawing; imagine that each square is a cube in plan view, and all four
 * cubes are resting on the ground and are a couple inches from one another.
 * Each junction in this diagram labelled with a letter would be a bundle; A would have
 * one strip, B would have two and E would have 4.
 *
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
 *
 * Each bundle has pointers to the junction at each end of it. Ends are identified
 * by which end points towards the positive end of the axis it's aligned with; an
 * X-aligned bundle has a positive end towards X+ and a negative end towards X-. Since
 * you can't use + and - in field names, these are called P and N for positive and
 * negative.
 *
 * Each junction has pointers to up to 6 attached bundles, one in every cardinal
 * direction. These follow the same naming convention: nx is negative-X, pz is
 * positive-Z, etc.
 */
public class StripsTopology {
    private static final int NO_STRIP = Integer.MAX_VALUE;

    /* All in inches */
    private static final float ORDER_TOLERANCE = 1;
    private static final float BUCKET_TOLERANCE = 6;
    private static final float ENDPOINT_TOLERANCE = 6;

    public enum EdgeDirection {
        X, Y, Z, Other
    }

    public class BundleEndpoints {
        public LXVector negative;
        public LXVector positive;
    }

    /** The meeting point of up to 6 bundles. */
    public static class Junction {
        /** The bundle in the positive X direction */
        public StripsTopology.Bundle px;
        /** The bundle in the negative X direction */
        public StripsTopology.Bundle nx;
        /** The bundle in the positive Y direction */
        public StripsTopology.Bundle py;
        /** The bundle in the negative Y direction */
        public StripsTopology.Bundle ny;
        /** The bundle in the positive Z direction */
        public StripsTopology.Bundle pz;
        /** The bundle in the negative X direction */
        public StripsTopology.Bundle nz;
        /** The approximate location of this junction.
         *  This is calculated as the midpoint of the bundles attached to the junction. */
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
    }

    /** A set of parallel, adjacent strips */
    public class Bundle {
        /* Note that this is just a hit for initialization; the strips array
         * is resized down once the bundle is finished being built. */
        private static final int MAX_STRIPS = 4;

        /** The direction this bundle points in */
        public EdgeDirection dir = EdgeDirection.Other;

        /** The model indexes of the strips in this bundle.
         *  These index into model.strips and can be used with model.getStripByIndex() */
        public int[] strips;

        /** The junction at the positive end of this bundle */
        public Junction p = null;
        /** The junction at the negative end of this bundle */
        public Junction n = null;

        /* Set to true once we're finished adding strips to the bundle; the cached
         * values below can only be used once this is set to true. */
        private boolean finished = false;

        /* Caches for expensive-to-compute properties of the bundle, only populated
         * after the bundle is finished. */
        private BundleEndpoints endpoints = null;
        private float minOrder = Float.MAX_VALUE;
        private float maxOrder = Float.MIN_VALUE;
        private float order = Float.MIN_VALUE;

        private Bundle() {
            strips = new int[MAX_STRIPS];
            Arrays.fill(strips, NO_STRIP);
        }

        private void addStrip(int strip) {
            Preconditions.checkState(!finished, "cannot add to finished bundle");
            for (int i = 0; i < strips.length; i++) {
                if (strips[i] == NO_STRIP) {
                    strips[i] = strip;
                    return;
                }
            }
            throw new IllegalStateException(String.format(
                "tried to add more than %d strips to a Bundle", strips.length));
        }

        private void finishedAddingStrips() {
            if (finished)
                return;

            int count = 0;
            for (int i = 0; i < strips.length; i++) {
                if (strips[i] != NO_STRIP)
                    count++;
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
            strips = new int[count];
            for (int i = 0; i < count; i++)
                strips[i] = newStrips[i];
            endpoints = endpoints();
            minOrder = minProjection();
            maxOrder = maxProjection();
            order = projection();
        }

        public BundleEndpoints endpoints() {
            if (finished)
                return endpoints;

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
                if (strips[i] == NO_STRIP)
                    continue;
                Strip s = model.getStripByIndex(strips[i]);
                switch (dir) {
                    case X: pl[i] = new PlanarLocation(dir, s.cy, s.cz); break;
                    case Y: pl[i] = new PlanarLocation(dir, s.cx, s.cz); break;
                    case Z: pl[i] = new PlanarLocation(dir, s.cx, s.cy); break;
                }
            }
            return PlanarLocation.combine(pl);
        }

        /** An edge's "projection" is the projection of it's centroid onto the axis it's aligned with */
        public float projection() {
            if (finished)
                return order;
            float order = 0;
            int count = 0;
            for (int strip : strips) {
                if (strip == NO_STRIP)
                    continue;
                Strip s = model.getStripByIndex(strip);
                switch (dir) {
                    case X: order += s.cx; break;
                    case Y: order += s.cy; break;
                    case Z: order += s.cz; break;
                }
                count++;
            }
            return order / count;
        }

        /** Min-projection is the minimum value of the projection of this edge onto its axis */
        public float minProjection() {
            if (finished)
                return minOrder;
            float min = Float.MAX_VALUE;
            for (int strip : strips) {
                if (strip == NO_STRIP)
                    continue;
                Strip s = model.getStripByIndex(strip);
                switch (dir) {
                    case X: min = Float.min(min, s.xMin); break;
                    case Y: min = Float.min(min, s.yMin); break;
                    case Z: min = Float.min(min, s.zMin); break;
                }
            }
            return min;
        }

        /** Max-projection is the maximum value of the projection of this edge onto its axis */
        public float maxProjection() {
            if (finished)
                return maxOrder;
            float max = Float.MIN_VALUE;
            for (int strip : strips) {
                if (strip == NO_STRIP)
                    continue;
                Strip s = model.getStripByIndex(strip);
                switch (dir) {
                    case X: max = Float.max(max, s.xMax); break;
                    case Y: max = Float.max(max, s.yMax); break;
                    case Z: max = Float.max(max, s.zMax); break;
                }
            }
            return max;
        }
    }

    /** Represents the location of a bundle within the plane perpendicular to the bundle.
     * This is used to bucket strips into bundles. */
    public static class PlanarLocation {
        public EdgeDirection dir;
        /** The first coordinate in the plane. For the X plane, this is the Y coordinate. For Y and Z, this is the X coordinate. */
        public float a;
        /** The second coordinate in the plane. For the X and Y plane, this is the Z coordinate. For Z, this is the Y coordinate. */
        public float b;

        PlanarLocation(EdgeDirection dir, float a, float b) {
            this.dir = dir;
            this.a = a;
            this.b = b;
        }

        boolean equivalent(PlanarLocation pl) {
            return dir == pl.dir && Math.abs(a - pl.a) < BUCKET_TOLERANCE && Math.abs(b - pl.b) < BUCKET_TOLERANCE;
        }

        static PlanarLocation combine(PlanarLocation buckets[]) {
            PlanarLocation res = null;
            int count = 0;
            for (PlanarLocation bucket : buckets) {
                if (bucket == null)
                    continue;
                if (res == null)
                    res = new PlanarLocation(bucket.dir, bucket.a, bucket.b);
                else {
                    res.a += bucket.a;
                    res.b += bucket.b;
                }
                count++;
            }
            if (res == null)
                return null;
            res.a /= count;
            res.b /= count;
            return res;
        }
    }

    /* Used for endpoint determination */
    private class BundleNode {
        float x, y, z;
        Bundle bundle;
        boolean isNegativeEnd;
    }

    private final StripsModel model;
    public final List<Bundle> bundles;
    public final List<Junction> junctions;

    StripsTopology(StripsModel model) {
        this.model = model;
        int N = model.getStrips().size();
        bundles = new ArrayList<>(N);
        junctions = new ArrayList<>();

        /* Create TopoEdges for each bundle */
        for (int i = 0; i < N; i++) {
            Strip s = model.getStripByIndex(i);

            Bundle e = new Bundle();
            e.addStrip(i);
            if (s.xRange < 1e-3 && s.yRange < 1e-3) {
                e.dir = EdgeDirection.Z;
            } else if (s.xRange < 1e-3 && s.zRange < 1e-3) {
                e.dir = EdgeDirection.Y;
            } else if (s.yRange < 1e-3 && s.zRange < 1e-3) {
                e.dir = EdgeDirection.X;
            } else {
                e.dir = EdgeDirection.Other;
            }

            /* TODO: this doesn't need to be quadratic. */
            boolean matchesExisting = false;
            PlanarLocation pl = e.planarLocation();
            float order = e.projection();
            for (Bundle testEdge : bundles) {
                float testOrder = testEdge.projection();
                float orderDist = Math.abs(order - testOrder);
                if (pl.equivalent(testEdge.planarLocation()) && orderDist < ORDER_TOLERANCE) {
                    testEdge.addStrip(i);
                    matchesExisting = true;
                    break;
                }
            }
            if (!matchesExisting)
                bundles.add(e);
        }
        for (Bundle e : bundles)
            e.finishedAddingStrips();

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
                be.positive.x, be.positive.y, be.positive.z, ENDPOINT_TOLERANCE);
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
                    if (b.p.nx != null)
                        throw new IllegalStateException("found overlapping bundles");
                    b.p.nx = b;
                    break;
                case Y:
                    if (b.p.ny != null)
                        throw new IllegalStateException("found overlapping bundles");
                    b.p.ny = b;
                    break;
                case Z:
                    if (b.p.nz != null)
                        throw new IllegalStateException("found overlapping bundles");
                    b.p.nz = b;
                    break;
            }

            js = junctionTree.withinDistance(
                be.negative.x, be.negative.y, be.negative.z, ENDPOINT_TOLERANCE);
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
                    if (b.n.px != null)
                        throw new IllegalStateException("found overlapping bundles");
                    b.n.px = b;
                    break;
                case Y:
                    if (b.n.py != null)
                        throw new IllegalStateException("found overlapping bundles");
                    b.n.py = b;
                    break;
                case Z:
                    if (b.n.pz != null)
                        throw new IllegalStateException("found overlapping bundles");
                    b.n.pz = b;
                    break;
            }
        }

        /* Find good locations for each of the junctions now that we have the final
         * set of bundles for each of them. We take the junction location to be the
         * midpoint of the endpoints of all of the adjacent bundles. */
        for (Junction j : junctions) {
            j.loc = new LXVector(0, 0, 0);
            int count = 0;

            if (j.px != null) {
                j.loc.add(j.px.endpoints().negative);
                count++;
            }
            if (j.nx != null) {
                j.loc.add(j.nx.endpoints().positive);
                count++;
            }
            if (j.py != null) {
                j.loc.add(j.py.endpoints().negative);
                count++;
            }
            if (j.ny != null) {
                j.loc.add(j.ny.endpoints().positive);
                count++;
            }
            if (j.pz != null) {
                j.loc.add(j.pz.endpoints().negative);
                count++;
            }
            if (j.nz != null) {
                j.loc.add(j.nz.endpoints().positive);
                count++;
            }

            j.loc.mult(1f / count);
        }
    }
}
