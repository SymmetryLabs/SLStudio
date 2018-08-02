package com.symmetrylabs.slstudio.model;

import com.google.common.base.Preconditions;
import heronarts.lx.model.LXPoint;

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
 * Each bundle also has pointers to adjacent bundles, where adjacency is defined as
 * sharing an endpoint. Each bundle can have up to 10 neighbors, 5 at each end, but
 * there are 12 fields for storing neighbors on the bundle object. This is because
 * adjacent bundles are stored with respect to their location on the bundle.
 *
 * Each bundle is axis aligned, which induces on the bundle a positive and negative
 * end (i.e., if a bundle is along X, it's positive end is towards X+ and its negative
 * end is towards X-). At each end, there are bundles that could go in any of the Cartesian
 * directions (X+, X-, Y+, Y-, Z+, Z-). Since you can't stick + and - in a field name,
 * we instead use P for positive and N for negative. So the edge that goes in the X+
 * direction on the negative end of a bundle is stored in the field NXP (negative end,
 * x positive).
 *
 * If a bundle is oriented along axis A, nAp and pAn will always be null. This is
 * a design decision; you could think of a bundle A along the X axis referencing
 * itself in its negative-end-positive-X field, since it is what is in the positive-X
 * direction of that endpoint, but I instead leave them as null because the risk of
 * infinite loops seems like it outweighs the conceptual purity of making A.nxp = A.
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
        public LXPoint negative;
        public LXPoint positive;
    }

    public class Bundle {
        /* Note that this is just a hit for initialization; the strips array
         * is resized down once the bundle is finished being built. */
        private static final int MAX_STRIPS = 4;

        /** The direction this bundle points in */
        public EdgeDirection dir = EdgeDirection.Other;

        /** The model indexes of the strips in this bundle.
         *  These index into model.strips and can be used with model.getStripByIndex() */
        public int[] strips;

        /** The bundle at the positive end of this bundle, in the positive X direction */
        public Bundle pxp = null;
        /** The bundle at the positive end of this bundle, in the negative X direction */
        public Bundle pxn = null;
        /** The bundle at the positive end of this bundle, in the positive Y direction */
        public Bundle pyp = null;
        /** The bundle at the positive end of this bundle, in the negative Y direction */
        public Bundle pyn = null;
        /** The bundle at the positive end of this bundle, in the positive Z direction */
        public Bundle pzp = null;
        /** The bundle at the positive end of this bundle, in the negative Z direction */
        public Bundle pzn = null;
        /** The bundle at the negative end of this bundle, in the positive X direction */
        public Bundle nxp = null;
        /** The bundle at the negative end of this bundle, in the negative X direction */
        public Bundle nxn = null;
        /** The bundle at the negative end of this bundle, in the positive Y direction */
        public Bundle nyp = null;
        /** The bundle at the negative end of this bundle, in the negative Y direction */
        public Bundle nyn = null;
        /** The bundle at the negative end of this bundle, in the positive Z direction */
        public Bundle nzp = null;
        /** The bundle at the negative end of this bundle, in the negative Z direction */
        public Bundle nzn = null;

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

            switch (dir) {
                case X:
                    xs = min;
                    xe = max;
                    ys = sort.a;
                    ye = sort.a;
                    zs = sort.b;
                    ze = sort.b;
                    break;
                case Y:
                    xs = sort.a;
                    xe = sort.a;
                    ys = min;
                    ye = max;
                    zs = sort.b;
                    ze = sort.b;
                    break;
                case Z:
                    xs = sort.a;
                    xe = sort.a;
                    ys = sort.b;
                    ye = sort.b;
                    zs = min;
                    ze = max;
                    break;
                default:
                    throw new IllegalArgumentException(
                        "cannot find endpoints of non-aligned bundle");
            }
            BundleEndpoints e = new BundleEndpoints();
            e.negative = new LXPoint(xs, ys, zs);
            e.positive = new LXPoint(xe, ye, ze);
            return e;
        }

        private PlanarLocation planarLocation() {
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
    private static class PlanarLocation {
        EdgeDirection dir;
        float a;
        float b;

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

    StripsTopology(StripsModel model) {
        this.model = model;
        int N = model.getStrips().size();
        bundles = new ArrayList<>(N);

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

        /* Now figure out adjacency. This used to use com.harium...KDTree but it was
         * actually slower than the full quadratic check. */
        ArrayList<BundleNode> nodes = new ArrayList<>(bundles.size() * 2);
        for (Bundle e : bundles) {
            BundleNode start = new BundleNode();
            start.bundle = e;
            start.isNegativeEnd = true;
            start.x = e.endpoints().negative.x;
            start.y = e.endpoints().negative.y;
            start.z = e.endpoints().negative.z;
            nodes.add(start);

            BundleNode end = new BundleNode();
            end.bundle = e;
            end.isNegativeEnd = false;
            end.x = e.endpoints().positive.x;
            end.y = e.endpoints().positive.y;
            end.z = e.endpoints().positive.z;
            nodes.add(end);
        }

        for (Bundle e : bundles) {
            BundleEndpoints ee = e.endpoints();

            /* We split up the start point and end point searches, because the start point
             * adjacencies will go in NXP, NXN, NYP, etc. while the end point adjacency
             * goes in PXP, PXN, PYP, etc. Splitting them up like this makes the code a
             * little simpler at almost no added computational cost. We start by looking
             * for adjacency at the negative end of the bundle. */
            for (BundleNode node : nodes) {
                Bundle o = node.bundle;
                if (o == e)
                    continue;

                float xd = Math.abs(ee.negative.x - node.x);
                float yd = Math.abs(ee.negative.y - node.y);
                float zd = Math.abs(ee.negative.z - node.z);
                if (xd > ENDPOINT_TOLERANCE || yd > ENDPOINT_TOLERANCE || zd > ENDPOINT_TOLERANCE)
                    continue;

                if (o.dir == e.dir) {
                    if (node.isNegativeEnd)
                        throw new IllegalStateException("found parallel and overlapping bundles");
                    switch (e.dir) {
                        case X: e.nxn = o; break;
                        case Y: e.nyn = o; break;
                        case Z: e.nzn = o; break;
                    }
                } else if (node.isNegativeEnd) {
                    /* This is a neg-to-neg connection, meaning this is on the
                     * negative side of both e and o, so o is in a positive direction
                     * wrt its axis compared to e. */
                    switch (e.dir) {
                        case X:
                            if (o.dir == EdgeDirection.Y)
                                e.nyp = o;
                            else
                                e.nzp = o;
                            break;
                        case Y:
                            if (o.dir == EdgeDirection.X)
                                e.nxp = o;
                            else
                                e.nzp = o;
                            break;
                        case Z:
                            if (o.dir == EdgeDirection.X)
                                e.nxp = o;
                            else
                                e.nyp = o;
                            break;
                    }
                } else {
                    /* The neg of e but the pos of o means that o is below e
                     * along the axis of o. */
                    switch (e.dir) {
                        case X:
                            if (o.dir == EdgeDirection.Y)
                                e.nyn = o;
                            else
                                e.nzn = o;
                            break;
                        case Y:
                            if (o.dir == EdgeDirection.X)
                                e.nxn = o;
                            else
                                e.nzn = o;
                            break;
                        case Z:
                            if (o.dir == EdgeDirection.X)
                                e.nxn = o;
                            else
                                e.nyn = o;
                            break;
                    }
                }
            }

            /* Look for adjacency at the positive end of this bundle */
            for (BundleNode node : nodes) {
                Bundle o = node.bundle;
                if (o == e)
                    continue;

                float xd = Math.abs(ee.positive.x - node.x);
                float yd = Math.abs(ee.positive.y - node.y);
                float zd = Math.abs(ee.positive.z - node.z);
                if (xd > ENDPOINT_TOLERANCE || yd > ENDPOINT_TOLERANCE || zd > ENDPOINT_TOLERANCE)
                    continue;

                if (o.dir == e.dir) {
                    if (!node.isNegativeEnd)
                        throw new IllegalStateException("found parallel and overlapping bundles");
                    switch (e.dir) {
                        case X: e.pxp = o; break;
                        case Y: e.pyp = o; break;
                        case Z: e.pzp = o; break;
                    }
                } else if (node.isNegativeEnd) {
                    /* The pos-end of e but the neg-end of o, so o is in the positive
                     * direction along o's axis. */
                    switch (e.dir) {
                        case X:
                            if (o.dir == EdgeDirection.Y)
                                e.pyp = o;
                            else
                                e.pzp = o;
                            break;
                        case Y:
                            if (o.dir == EdgeDirection.X)
                                e.pxp = o;
                            else
                                e.pzp = o;
                            break;
                        case Z:
                            if (o.dir == EdgeDirection.X)
                                e.pxp = o;
                            else
                                e.pyp = o;
                            break;
                    }
                } else {
                    /* The pos-end of e and the pos-end of o means that o is below e
                     * along the axis of o. */
                    switch (e.dir) {
                        case X:
                            if (o.dir == EdgeDirection.Y)
                                e.pyn = o;
                            else
                                e.pzn = o;
                            break;
                        case Y:
                            if (o.dir == EdgeDirection.X)
                                e.pxn = o;
                            else
                                e.pzn = o;
                            break;
                        case Z:
                            if (o.dir == EdgeDirection.X)
                                e.pxn = o;
                            else
                                e.pyn = o;
                            break;
                    }
                }
            }
        }
    }
}
