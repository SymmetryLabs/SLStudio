package com.symmetrylabs.slstudio.model;

import com.google.common.base.Preconditions;
import com.symmetrylabs.layouts.cubes.CubesModel;
import com.symmetrylabs.slstudio.model.Strip;
import heronarts.lx.model.LXPoint;

import java.util.*;

public class StripsTopology {
    public static final int NO_EDGE = Integer.MAX_VALUE;

    /* All in inches */
    private static final float ORDER_TOLERANCE = 1;
    private static final float BUCKET_TOLERANCE = 6;
    private static final float ENDPOINT_TOLERANCE = 6;

    public enum EdgeDirection {
        X, Y, Z, Other
    }

    public class BundleEndpoints {
        public LXPoint start;
        public LXPoint end;
    }

    private class BundleNode {
        float x, y, z;
        Bundle bundle;
        boolean isStart;
    }

    public class Bundle {
        public static final int MAX_STRIPS = 4;

        public EdgeDirection dir = EdgeDirection.Other;
        public int[] strips;
        public Bundle pxp = null;
        public Bundle pxn = null;
        public Bundle pyp = null;
        public Bundle pyn = null;
        public Bundle pzp = null;
        public Bundle pzn = null;
        public Bundle nxp = null;
        public Bundle nxn = null;
        public Bundle nyp = null;
        public Bundle nyn = null;
        public Bundle nzp = null;
        public Bundle nzn = null;

        private boolean finished = false;
        private BundleEndpoints endpoints = null;
        private SortBucket sb = null;
        private float minOrder = Float.MAX_VALUE;
        private float maxOrder = Float.MIN_VALUE;
        private float order = Float.MIN_VALUE;

        protected Bundle() {
            strips = new int[MAX_STRIPS];
            Arrays.fill(strips, NO_EDGE);
        }

        public void addStrip(int strip) {
            Preconditions.checkState(!finished, "cannot add to finished bundle");
            for (int i = 0; i < strips.length; i++) {
                if (strips[i] == NO_EDGE) {
                    strips[i] = strip;
                    return;
                }
            }
            throw new IllegalStateException(String.format(
                "tried to add more than %d strips to a Bundle", strips.length));
        }

        public BundleEndpoints endpoints() {
            if (finished)
                return endpoints;

            float min = minOrder();
            float max = maxOrder();
            SortBucket sort = sortBucket();

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
            e.start = new LXPoint(xs, ys, zs);
            e.end = new LXPoint(xe, ye, ze);
            return e;
        }

        public void finishedAddingStrips() {
            if (finished)
                return;

            int count = 0;
            for (int i = 0; i < strips.length; i++) {
                if (strips[i] != NO_EDGE)
                    count++;
            }
            Integer[] newStrips = new Integer[count];
            int j = 0;
            for (int strip : strips) {
                if (strip != NO_EDGE) {
                    newStrips[j] = strip;
                    j++;
                }
            }
            /* Sort them so that two bundles that are tip-to-tip adjacent will
             * have element 0 line up with element 0, element 1 with element 1, etc. */
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
            sb = sortBucket();
            minOrder = minOrder();
            maxOrder = maxOrder();
            order = order();
        }

        public SortBucket sortBucket() {
            if (finished)
                return sb;
            SortBucket sb[] = new SortBucket[MAX_STRIPS];
            for (int i = 0; i < strips.length; i++) {
                if (strips[i] == NO_EDGE)
                    continue;
                Strip s = model.getStripByIndex(strips[i]);
                switch (dir) {
                    case X: sb[i] = new SortBucket(dir, s.cy, s.cz); break;
                    case Y: sb[i] = new SortBucket(dir, s.cx, s.cz); break;
                    case Z: sb[i] = new SortBucket(dir, s.cx, s.cy); break;
                }
            }
            return SortBucket.combine(sb);
        }

        /** An edge's "order" is the projection of it's centroid onto the axis it's aligned with */
        public float order() {
            if (finished)
                return order;
            float order = 0;
            int count = 0;
            for (int strip : strips) {
                if (strip == NO_EDGE)
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

        /** Min-order is the minimum value of the projection of this edge onto its axis */
        public float minOrder() {
            if (finished)
                return minOrder;
            float min = Float.MAX_VALUE;
            for (int strip : strips) {
                if (strip == NO_EDGE)
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

        /** Max-order is the maximum value of the projection of this edge onto its axis */
        public float maxOrder() {
            if (finished)
                return maxOrder;
            float max = Float.MIN_VALUE;
            for (int strip : strips) {
                if (strip == NO_EDGE)
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

    protected static class SortBucket {
        EdgeDirection dir;
        float a;
        float b;

        SortBucket(EdgeDirection dir, float a, float b) {
            this.dir = dir;
            this.a = a;
            this.b = b;
        }

        boolean equivalent(SortBucket sb) {
            return dir == sb.dir && Math.abs(a - sb.a) < BUCKET_TOLERANCE && Math.abs(b - sb.b) < BUCKET_TOLERANCE;
        }

        static SortBucket combine(SortBucket buckets[]) {
            SortBucket res = null;
            int count = 0;
            for (SortBucket bucket : buckets) {
                if (bucket == null)
                    continue;
                if (res == null)
                    res = new SortBucket(bucket.dir, bucket.a, bucket.b);
                else {
                    res.a += bucket.a;
                    res.b += bucket.b;
                }
                count++;
            }
            res.a /= count;
            res.b /= count;
            return res;
        }
    }

    private final StripsModel model;
    public final List<Bundle> edges;

    public StripsTopology(StripsModel model) {
        this.model = model;
        int N = model.getStrips().size();
        edges = new ArrayList<>(N);

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
                System.out.println(String.format(
                    "model strip not axis-aligned: x=%f y=%f z=%f", s.xRange, s.yRange, s.zRange));
                e.dir = EdgeDirection.Other;
            }

            /* TODO: this doesn't need to be quadratic. */
            boolean matchesExisting = false;
            SortBucket sb = e.sortBucket();
            float order = e.order();
            for (Bundle testEdge : edges) {
                float testOrder = testEdge.order();
                float orderDist = Math.abs(order - testOrder);
                if (sb.equivalent(testEdge.sortBucket()) && orderDist < ORDER_TOLERANCE) {
                    testEdge.addStrip(i);
                    matchesExisting = true;
                    break;
                }
            }
            if (!matchesExisting)
                edges.add(e);
        }
        for (Bundle e : edges)
            e.finishedAddingStrips();

        /* This used to use com.harium...KDTree but it was actually slower than the full quadratic check */
        ArrayList<BundleNode> nodes = new ArrayList<>(edges.size() * 2);
        for (Bundle e : edges) {
            BundleNode start = new BundleNode();
            start.bundle = e;
            start.isStart = true;
            start.x = e.endpoints().start.x;
            start.y = e.endpoints().start.y;
            start.z = e.endpoints().start.z;
            nodes.add(start);

            BundleNode end = new BundleNode();
            end.bundle = e;
            end.isStart = false;
            end.x = e.endpoints().end.x;
            end.y = e.endpoints().end.y;
            end.z = e.endpoints().end.z;
            nodes.add(end);
        }

        for (Bundle e : edges) {
            BundleEndpoints ee = e.endpoints();
            List<BundleNode> nearest;

            /* First find points near the start */
            for (BundleNode node : nodes) {
                Bundle o = node.bundle;
                if (o == e)
                    continue;

                float xd = Math.abs(ee.start.x - node.x);
                float yd = Math.abs(ee.start.y - node.y);
                float zd = Math.abs(ee.start.z - node.z);
                if (xd > ENDPOINT_TOLERANCE || yd > ENDPOINT_TOLERANCE || zd > ENDPOINT_TOLERANCE)
                    continue;

                if (o.dir == e.dir) {
                    switch (e.dir) {
                        case X: e.nxn = o; break;
                        case Y: e.nyn = o; break;
                        case Z: e.nzn = o; break;
                    }
                } else if (node.isStart) {
                    /* This is a start-to-start connection, meaning this is on the
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
                    /* The start of e but the end of o means that o is below e
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

            /* ...then near the end. */
            for (BundleNode node : nodes) {
                Bundle o = node.bundle;
                if (o == e)
                    continue;

                float xd = Math.abs(ee.end.x - node.x);
                float yd = Math.abs(ee.end.y - node.y);
                float zd = Math.abs(ee.end.z - node.z);
                if (xd > ENDPOINT_TOLERANCE || yd > ENDPOINT_TOLERANCE || zd > ENDPOINT_TOLERANCE)
                    continue;

                if (o.dir == e.dir) {
                    switch (e.dir) {
                        case X: e.pxp = o; break;
                        case Y: e.pyp = o; break;
                        case Z: e.pzp = o; break;
                    }
                } else if (node.isStart) {
                    /* This is a start-to-start connection, meaning this is on the
                     * negative side of both e and o, so o is in a positive direction
                     * wrt its axis compared to e. */
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
                    /* The start of e but the end of o means that o is below e
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
